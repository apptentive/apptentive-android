/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveNotifications;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.model.DevicePayload;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PersonPayload;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionManifest;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.EncryptedFileSerializer;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.IntegrationConfig;
import com.apptentive.android.sdk.storage.IntegrationConfigItem;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SerializerException;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Destroyable;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;

import java.io.File;

import static com.apptentive.android.sdk.ApptentiveNotifications.*;
import static com.apptentive.android.sdk.debug.Assert.assertFail;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.debug.Assert.notNull;
import static com.apptentive.android.sdk.debug.Tester.dispatchDebugEvent;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.conversation.ConversationState.*;
import static com.apptentive.android.sdk.debug.TesterEvent.*;

public class Conversation implements DataChangedListener, Destroyable {

	/**
	 * Conversation data for this class to manage
	 */
	private ConversationData conversationData;

	/**
	 * Encryption key for payloads. A hex encoded String.
	 */
	private String encryptionKey;

	/**
	 * Optional user id for logged-in conversations
	 */
	private String userId;

	/**
	 * Optional JWT for active conversations
	 */
	private String JWT;

	/**
	 * File which represents serialized conversation data on the disk
	 */
	private final File conversationDataFile;

	/**
	 * File which represents serialized messages data on the disk
	 */
	private final File conversationMessagesFile;

	/**
	 * Internal flag to turn interaction polling on and off fir testing.
	 */
	private Boolean pollForInteractions;

	private ConversationState state = ConversationState.UNDEFINED;

	private final MessageManager messageManager;

	// we keep references to the tasks in order to dispatch them only once
	private final DispatchTask fetchInteractionsTask = new DispatchTask() {
		@Override
		protected void execute() {
			final boolean updateSuccessful = fetchInteractionsSync();
			dispatchDebugEvent(EVT_CONVERSATION_FETCH_INTERACTIONS, updateSuccessful);

			// Update pending state on UI thread after finishing the task
			DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					if (hasActiveState()) {
						ApptentiveInternal.getInstance().notifyInteractionUpdated(updateSuccessful);
					}
				}
			});
		}
	};

	// we keep references to the tasks in order to dispatch them only once
	private final DispatchTask saveConversationTask = new DispatchTask() {
		@Override
		protected void execute() {
			try {
				saveConversationData();
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while saving conversation data");
			}
		}
	};

	public Conversation(File conversationDataFile, File conversationMessagesFile) {
		if (conversationDataFile == null) {
			throw new IllegalArgumentException("Data file is null");
		}
		if (conversationMessagesFile == null) {
			throw new IllegalArgumentException("Messages file is null");
		}

		this.conversationDataFile = conversationDataFile;
		this.conversationMessagesFile = conversationMessagesFile;

		conversationData = new ConversationData();

		FileMessageStore messageStore = new FileMessageStore(conversationMessagesFile);
		messageManager = new MessageManager(this, messageStore); // it's important to initialize message manager in a constructor since other SDK parts depend on it via Apptentive singleton
	}

	public void startListeningForChanges() {
		conversationData.setDataChangedListener(this);
	}

	//region Payloads

	public void addPayload(Payload payload) {
		payload.setLocalConversationIdentifier(notNull(getLocalIdentifier()));
		payload.setConversationId(getConversationId());
		payload.setToken(getConversationToken());
		payload.setEncryptionKey(getEncryptionKey());

		// TODO: don't use singleton here
		ApptentiveInternal.getInstance().getApptentiveTaskManager().addPayload(payload);
	}

	//endregion

	//region Interactions

	/**
	 * Returns an Interaction for <code>eventLabel</code> if there is one that can be displayed.
	 */
	public Interaction getApplicableInteraction(String eventLabel) {
		String targetsString = getTargets();
		if (targetsString != null) {
			try {
				Targets targets = new Targets(getTargets());
				String interactionId = targets.getApplicableInteraction(eventLabel);
				if (interactionId != null) {
					String interactionsString = getInteractions();
					if (interactionsString != null) {
						Interactions interactions = new Interactions(interactionsString);
						return interactions.getInteraction(interactionId);
					}
				}
			} catch (JSONException e) {
				ApptentiveLog.e(e, "Exception while getting applicable interaction: %s", eventLabel);
			}
		}
		return null;
	}

	boolean fetchInteractions(Context context) {
		boolean cacheExpired = getInteractionExpiration() < Util.currentTimeSeconds();
		if (cacheExpired || RuntimeUtils.isAppDebuggable(context)) {
			return DispatchQueue.backgroundQueue().dispatchAsyncOnce(fetchInteractionsTask); // do not allow multiple fetches at the same time
		}

		ApptentiveLog.v(CONVERSATION, "Interaction cache is still valid");
		return false;
	}

	/**
	 * Fetches interaction synchronously. Returns <code>true</code> if succeed.
	 */
	private boolean fetchInteractionsSync() {
		ApptentiveLog.v(CONVERSATION, "Fetching Interactions");
		ApptentiveHttpResponse response = ApptentiveClient.getInteractions(getConversationToken(), getConversationId());

		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		boolean updateSuccessful = true;

		// We weren't able to connect to the internet.
		if (response.isException()) {
			prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, false).apply();
			updateSuccessful = false;
		}
		// We got a server error.
		else if (!response.isSuccessful()) {
			prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, true).apply();
			updateSuccessful = false;
		}

		if (updateSuccessful) {
			String interactionsPayloadString = response.getContent();

			// Send a notification so other parts of the SDK can use this data for troubleshooting
			ApptentiveNotificationCenter.defaultCenter()
					.postNotification(NOTIFICATION_INTERACTION_MANIFEST_FETCHED, NOTIFICATION_KEY_MANIFEST, interactionsPayloadString);

			// Store new integration cache expiration.
			String cacheControl = response.getHeaders().get("Cache-Control");
			Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
			if (cacheSeconds == null) {
				cacheSeconds = Constants.CONFIG_DEFAULT_INTERACTION_CACHE_EXPIRATION_DURATION_SECONDS;
			}
			setInteractionExpiration(Util.currentTimeSeconds() + cacheSeconds);
			try {
				InteractionManifest payload = new InteractionManifest(interactionsPayloadString);
				Interactions interactions = payload.getInteractions();
				Targets targets = payload.getTargets();
				if (interactions != null && targets != null) {
					setTargets(targets.toString());
					setInteractions(interactions.toString());
				} else {
					ApptentiveLog.e(CONVERSATION, "Unable to save interactionManifest.");
				}
			} catch (JSONException e) {
				ApptentiveLog.e(e, "Invalid InteractionManifest received.");
			}
		}
		ApptentiveLog.v(CONVERSATION, "Fetching new Interactions task finished. Successful: %b", updateSuccessful);

		return updateSuccessful;
	}

	public boolean isPollForInteractions() {
		if (pollForInteractions == null) {
			SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
			pollForInteractions = prefs.getBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, true);
		}
		return pollForInteractions;
	}

	public void setPollForInteractions(boolean pollForInteractions) {
		this.pollForInteractions = pollForInteractions;
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		prefs.edit().putBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, pollForInteractions).apply();
	}

	/**
	 * Made public for testing. There is no other reason to use this method directly.
	 */
	public void storeInteractionManifest(String interactionManifest) {
		try {
			InteractionManifest payload = new InteractionManifest(interactionManifest);
			Interactions interactions = payload.getInteractions();
			Targets targets = payload.getTargets();
			if (interactions != null && targets != null) {
				setTargets(targets.toString());
				setInteractions(interactions.toString());
			} else {
				ApptentiveLog.e("Unable to save InteractionManifest.");
			}
		} catch (JSONException e) {
			ApptentiveLog.w("Invalid InteractionManifest received.");
		}
	}

	//endregion

	//region Saving

	public void scheduleSaveConversationData() {
		boolean scheduled = DispatchQueue.backgroundQueue().dispatchAsyncOnce(saveConversationTask, 100L);
		if (scheduled) {
			ApptentiveLog.d(CONVERSATION, "Scheduling conversation save.");
		} else {
			ApptentiveLog.d(CONVERSATION, "Conversation save already scheduled.");
		}
	}

	/**
	 * Saves conversation data to the disk synchronously. Returns <code>true</code>
	 * if succeed.
	 */
	private synchronized void saveConversationData() throws SerializerException {
		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERY_VERBOSE)) {
			ApptentiveLog.vv(CONVERSATION, "Saving %sconversation data...", hasState(LOGGED_IN) ? "encrypted " : "");
			ApptentiveLog.vv(CONVERSATION, "EventData: %s", getEventData().toString());
			ApptentiveLog.vv(CONVERSATION, "Messages: %s", messageManager.getMessageStore().toString());
		}
		long start = System.currentTimeMillis();

		FileSerializer serializer;
		if (!StringUtils.isNullOrEmpty(encryptionKey)) {
			Assert.assertFalse(hasState(ANONYMOUS, ANONYMOUS_PENDING, LEGACY_PENDING));
			serializer = new EncryptedFileSerializer(conversationDataFile, encryptionKey);
		} else {
			Assert.assertTrue(hasState(ANONYMOUS, ANONYMOUS_PENDING, LEGACY_PENDING), "Unexpected conversation state: %s", getState());
			serializer = new FileSerializer(conversationDataFile);
		}

		serializer.serialize(conversationData);
		ApptentiveLog.vv(CONVERSATION, "Conversation data saved (took %d ms)", System.currentTimeMillis() - start);
	}

	synchronized void loadConversationData() throws SerializerException {
		long start = System.currentTimeMillis();

		FileSerializer serializer;
		if (!StringUtils.isNullOrEmpty(encryptionKey)) {
			serializer = new EncryptedFileSerializer(conversationDataFile, encryptionKey);
		} else {
			serializer = new FileSerializer(conversationDataFile);
		}

		ApptentiveLog.d(CONVERSATION, "Loading %sconversation data...", hasState(LOGGED_IN) ? "encrypted " : "");
		conversationData = (ConversationData) serializer.deserialize();
		ApptentiveLog.d(CONVERSATION, "Conversation data loaded (took %d ms)", System.currentTimeMillis() - start);
	}

	//endregion

	//region Listeners

	@Override
	public void onDataChanged() {
		scheduleSaveConversationData();
	}

	//endregion

	//region Destroyable

	@Override
	public void destroy() {
		messageManager.destroy();
	}

	//endregion

	//region Diffs & Updates

	private final DispatchTask personUpdateTask = new DispatchTask() {
		@Override
		protected void execute() {
			Person lastSentPerson = getLastSentPerson();
			Person currentPerson = getPerson();
			assertNotNull(currentPerson, "Current person object is null");
			PersonPayload personPayload = PersonManager.getDiffPayload(lastSentPerson, currentPerson);
			if (personPayload != null) {
				addPayload(personPayload);
				setLastSentPerson(currentPerson != null ? currentPerson.clone() : null);
			}
		}
	};

	private final DispatchTask deviceUpdateTask = new DispatchTask() {
		@Override
		protected void execute() {
			Device lastSentDevice = getLastSentDevice();
			Device currentDevice = getDevice();
			assertNotNull(currentDevice, "Current device object is null");
			DevicePayload devicePayload = DeviceManager.getDiffPayload(lastSentDevice, currentDevice);
			if (devicePayload != null) {
				addPayload(devicePayload);
				setLastSentDevice(currentDevice != null ? currentDevice.clone() : null);
			}
		}
	};

	public void schedulePersonUpdate() {
		DispatchQueue.mainQueue().dispatchAsyncOnce(personUpdateTask);
	}

	public void scheduleDeviceUpdate() {
		DispatchQueue.mainQueue().dispatchAsyncOnce(deviceUpdateTask);
	}

	//endregion

	//region Getters & Setters

	public String getLocalIdentifier() {
		return getConversationData().getLocalIdentifier();
	}

	public ConversationState getState() {
		return state;
	}

	public void setState(ConversationState state) {
		// TODO: check if state transition would make sense (for example you should not be able to move from 'logged' state to 'anonymous', etc.)
		this.state = state;
	}

	/**
	 * Returns <code>true</code> if conversation is in the given state
	 */
	public boolean hasState(ConversationState s) {
		return state.equals(s);
	}

	/**
	 * Returns <code>true</code> if conversation is in one of the given states
	 */
	public boolean hasState(ConversationState... states) {
		for (ConversationState s : states) {
			if (s.equals(state)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if conversation is in "active" state (after receiving server response)
	 */
	public boolean hasActiveState() {
		return hasState(ConversationState.LOGGED_IN, ANONYMOUS);
	}

	public String getConversationToken() {
		return getConversationData().getConversationToken();
	}

	public void setConversationToken(String conversationToken) {
		getConversationData().setConversationToken(conversationToken);
	}

	public String getConversationId() {
		return getConversationData().getConversationId();
	}

	public void setConversationId(String conversationId) {
		getConversationData().setConversationId(conversationId);
	}

	public Device getDevice() {
		return getConversationData().getDevice();
	}

	public void setDevice(Device device) {
		getConversationData().setDevice(device);
	}

	public Device getLastSentDevice() {
		return getConversationData().getLastSentDevice();
	}

	public void setLastSentDevice(Device lastSentDevice) {
		getConversationData().setLastSentDevice(lastSentDevice);
	}

	public Person getPerson() {
		return getConversationData().getPerson();
	}

	public void setPerson(Person person) {
		getConversationData().setPerson(person);
	}

	public Person getLastSentPerson() {
		return getConversationData().getLastSentPerson();
	}

	public void setLastSentPerson(Person lastSentPerson) {
		getConversationData().setLastSentPerson(lastSentPerson);
	}

	public Sdk getSdk() {
		return getConversationData().getSdk();
	}

	public void setSdk(Sdk sdk) {
		getConversationData().setSdk(sdk);
	}

	public AppRelease getAppRelease() {
		return getConversationData().getAppRelease();
	}

	public void setAppRelease(AppRelease appRelease) {
		getConversationData().setAppRelease(appRelease);
	}

	public EventData getEventData() {
		return getConversationData().getEventData();
	}

	public void setEventData(EventData eventData) {
		getConversationData().setEventData(eventData);
	}

	public String getLastSeenSdkVersion() {
		return getConversationData().getLastSeenSdkVersion();
	}

	public void setLastSeenSdkVersion(String lastSeenSdkVersion) {
		getConversationData().setLastSeenSdkVersion(lastSeenSdkVersion);
	}

	public VersionHistory getVersionHistory() {
		return getConversationData().getVersionHistory();
	}

	public void setVersionHistory(VersionHistory versionHistory) {
		getConversationData().setVersionHistory(versionHistory);
	}

	public boolean isMessageCenterFeatureUsed() {
		return getConversationData().isMessageCenterFeatureUsed();
	}

	public void setMessageCenterFeatureUsed(boolean messageCenterFeatureUsed) {
		getConversationData().setMessageCenterFeatureUsed(messageCenterFeatureUsed);
	}

	public boolean isMessageCenterWhoCardPreviouslyDisplayed() {
		return getConversationData().isMessageCenterWhoCardPreviouslyDisplayed();
	}

	public void setMessageCenterWhoCardPreviouslyDisplayed(boolean messageCenterWhoCardPreviouslyDisplayed) {
		getConversationData().setMessageCenterWhoCardPreviouslyDisplayed(messageCenterWhoCardPreviouslyDisplayed);
	}

	public String getMessageCenterPendingMessage() {
		return getConversationData().getMessageCenterPendingMessage();
	}

	public void setMessageCenterPendingMessage(String messageCenterPendingMessage) {
		getConversationData().setMessageCenterPendingMessage(messageCenterPendingMessage);
	}

	public String getMessageCenterPendingAttachments() {
		return getConversationData().getMessageCenterPendingAttachments();
	}

	public void setMessageCenterPendingAttachments(String messageCenterPendingAttachments) {
		getConversationData().setMessageCenterPendingAttachments(messageCenterPendingAttachments);
	}

	public String getTargets() {
		return getConversationData().getTargets();
	}

	public void setTargets(String targets) {
		getConversationData().setTargets(targets);
	}

	public String getInteractions() {
		return getConversationData().getInteractions();
	}

	public void setInteractions(String interactions) {
		getConversationData().setInteractions(interactions);
	}

	public double getInteractionExpiration() {
		return getConversationData().getInteractionExpiration();
	}

	public void setInteractionExpiration(double interactionExpiration) {
		getConversationData().setInteractionExpiration(interactionExpiration);
	}

	// this is a synchronization hack: both save/load conversation data are synchronized so we can't
	// modify conversation data while it's being serialized/deserialized
	private synchronized ConversationData getConversationData() {
		return conversationData;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	synchronized File getConversationDataFile() {
		return conversationDataFile;
	}

	synchronized File getConversationMessagesFile() {
		return conversationMessagesFile;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	String getUserId() {
		return userId;
	}

	void setUserId(String userId) {
		this.userId = userId;
	}

	public void setPushIntegration(int pushProvider, String token) {
		ApptentiveLog.v(CONVERSATION, "Setting push provider: %d with token %s", pushProvider, token);
		IntegrationConfig integrationConfig = getDevice().getIntegrationConfig();
		IntegrationConfigItem item = new IntegrationConfigItem();
		item.setToken(token);
		switch (pushProvider) {
			case Apptentive.PUSH_PROVIDER_APPTENTIVE:
				integrationConfig.setApptentive(item);
				break;
			case Apptentive.PUSH_PROVIDER_PARSE:
				integrationConfig.setParse(item);
				break;
			case Apptentive.PUSH_PROVIDER_URBAN_AIRSHIP:
				integrationConfig.setUrbanAirship(item);
				break;
			case Apptentive.PUSH_PROVIDER_AMAZON_AWS_SNS:
				integrationConfig.setAmazonAwsSns(item);
				break;
			default:
				ApptentiveLog.e("Invalid pushProvider: %d", pushProvider);
				break;
		}
		scheduleDeviceUpdate();
	}

	/**
	 * Checks the internal consistency of the conversation object (temporary solution)
	 */
	void checkInternalConsistency() throws IllegalStateException {
		switch (state) {
			case LOGGED_IN:
				if (StringUtils.isNullOrEmpty(encryptionKey)) {
					assertFail("Missing encryption key");
					throw new IllegalStateException("Missing encryption key");
				}
				if (StringUtils.isNullOrEmpty(userId)) {
					assertFail("Missing user id");
					throw new IllegalStateException("Missing user id");
				}
				break;
			default:
				break;
		}
	}

	//endregion
}
