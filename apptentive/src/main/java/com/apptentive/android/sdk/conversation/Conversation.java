/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionManifest;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Saveable;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;

import java.io.File;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.conversation.ConversationState.ANONYMOUS;
import static com.apptentive.android.sdk.debug.Tester.dispatchDebugEvent;
import static com.apptentive.android.sdk.debug.TesterEvent.EVT_INTERACTION_FETCH;

public class Conversation implements Saveable, DataChangedListener {

	private static final long serialVersionUID = 1L;

	private String conversationToken;
	private String conversationId;
	private String personId;
	private String personEmail;
	private String personName;
	private Device device;
	private Device lastSentDevice;
	private Person person;
	private Person lastSentPerson;
	private Sdk sdk;
	private AppRelease appRelease;
	private EventData eventData;
	private String lastSeenSdkVersion;
	private VersionHistory versionHistory;
	private boolean messageCenterFeatureUsed;
	private boolean messageCenterWhoCardPreviouslyDisplayed;
	private String messageCenterPendingMessage;
	private String messageCenterPendingAttachments;
	private String targets;
	private String interactions;
	private double interactionExpiration;

	/**
	 * File which represents this conversation on the disk
	 */
	private transient File file;

	// TODO: Maybe move this up to a wrapping Conversation class?
	private transient InteractionManager interactionManager;

	// TODO: describe why we don't serialize state
	private transient ConversationState state = ConversationState.UNDEFINED;

	// we keep references to the tasks in order to dispatch them only once
	private transient DispatchTask fetchInteractionsTask;
	private transient DispatchTask saveConversationTask;

	public Conversation() {
		this.device = new Device();
		this.person = new Person();
		this.sdk = new Sdk();
		this.appRelease = new AppRelease();
		this.eventData = new EventData();
		this.versionHistory = new VersionHistory();

		// transient fields might not get properly initialized upon de-serialization
		setDataChangedListener(this);
		initDispatchTasks();
	}

	private void initDispatchTasks() {
		fetchInteractionsTask = new DispatchTask() {
			@Override
			protected void execute() {
				final boolean updateSuccessful = fetchInteractionsSync();

				// Update pending state on UI thread after finishing the task
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						if (hasActiveState()) {
							ApptentiveInternal.getInstance().notifyInteractionUpdated(updateSuccessful);
							dispatchDebugEvent(EVT_INTERACTION_FETCH, updateSuccessful);
						}
					}
				});
			}
		};
		saveConversationTask = new DispatchTask() {
			@Override
			protected void execute() {
				save();
			}
		};
	}

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
		boolean cacheExpired = getInteractionExpiration() > Util.currentTimeSeconds();
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
		ApptentiveHttpResponse response = ApptentiveClient.getInteractions();

		// TODO: Move this to global config
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
		ApptentiveLog.v(CONVERSATION, "Fetching new Interactions asyncTask finished. Successful? %b", updateSuccessful);

		return updateSuccessful;
	}

	//endregion

	//region Saving

	/**
	 * Saves conversation data to the disk synchronously. Returns <code>true</code>
	 * if succeed.
	 */
	synchronized boolean save() {
		if (file == null) {
			ApptentiveLog.e(CONVERSATION, "Unable to save conversation: destination file not specified");
			return false;
		}

		ApptentiveLog.d(CONVERSATION, "Saving Conversation");
		ApptentiveLog.v(CONVERSATION, "EventData: %s", getEventData().toString()); // TODO: remove

		try {
			FileSerializer serializer = new FileSerializer(file);
			serializer.serialize(this);
			return true;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Unable to save conversation");
			return false;
		}
	}

	//endregion

	//region Listeners

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		device.setDataChangedListener(this);
		person.setDataChangedListener(this);
		eventData.setDataChangedListener(this);
		versionHistory.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (hasFile()) {
			boolean scheduled = DispatchQueue.backgroundQueue().dispatchAsyncOnce(saveConversationTask, 100L);
			if (scheduled) {
				ApptentiveLog.d(CONVERSATION, "Scheduling conversation save.");
			} else {
				ApptentiveLog.d(CONVERSATION, "Conversation save already scheduled.");
			}
		} else {
			ApptentiveLog.v(CONVERSATION, "Can't save conversation data: storage file is not specified");
		}
	}

	@Override
	public void onDeserialize() {
		setDataChangedListener(this);
		initDispatchTasks();
	}

	@Override
	public void onDataChanged() {
		notifyDataChanged();
	}

	//endregion

	//region Getters & Setters

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
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		if (!TextUtils.equals(this.conversationToken, conversationToken)) {
			this.conversationToken = conversationToken;
			notifyDataChanged();
		}
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		if (!TextUtils.equals(this.conversationId, conversationId)) {
			this.conversationId = conversationId;
			notifyDataChanged();
		}
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		if (!TextUtils.equals(this.personId, personId)) {
			this.personId = personId;
			notifyDataChanged();
		}
	}

	public String getPersonEmail() {
		return personEmail;
	}

	public void setPersonEmail(String personEmail) {
		if (!TextUtils.equals(this.personEmail, personEmail)) {
			this.personEmail = personEmail;
			notifyDataChanged();
		}
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		if (!TextUtils.equals(this.personName, personName)) {
			this.personName = personName;
			notifyDataChanged();
		}
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
		device.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Device getLastSentDevice() {
		return lastSentDevice;
	}

	public void setLastSentDevice(Device lastSentDevice) {
		this.lastSentDevice = lastSentDevice;
		this.lastSentDevice.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
		this.person.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Person getLastSentPerson() {
		return lastSentPerson;
	}

	public void setLastSentPerson(Person lastSentPerson) {
		this.lastSentPerson = lastSentPerson;
		this.lastSentPerson.setDataChangedListener(this);
		notifyDataChanged();
	}

	public Sdk getSdk() {
		return sdk;
	}

	public void setSdk(Sdk sdk) {
		this.sdk = sdk;
		notifyDataChanged();
	}

	public AppRelease getAppRelease() {
		return appRelease;
	}

	public void setAppRelease(AppRelease appRelease) {
		this.appRelease = appRelease;
		notifyDataChanged();
	}

	public EventData getEventData() {
		return eventData;
	}

	public void setEventData(EventData eventData) {
		this.eventData = eventData;
		this.eventData.setDataChangedListener(this);
		notifyDataChanged();
	}

	public String getLastSeenSdkVersion() {
		return lastSeenSdkVersion;
	}

	public void setLastSeenSdkVersion(String lastSeenSdkVersion) {
		this.lastSeenSdkVersion = lastSeenSdkVersion;
		notifyDataChanged();
	}

	public VersionHistory getVersionHistory() {
		return versionHistory;
	}

	public void setVersionHistory(VersionHistory versionHistory) {
		this.versionHistory = versionHistory;
		this.versionHistory.setDataChangedListener(this);
		notifyDataChanged();
	}

	public boolean isMessageCenterFeatureUsed() {
		return messageCenterFeatureUsed;
	}

	public void setMessageCenterFeatureUsed(boolean messageCenterFeatureUsed) {
		if (this.messageCenterFeatureUsed != messageCenterFeatureUsed) {
			this.messageCenterFeatureUsed = messageCenterFeatureUsed;
			notifyDataChanged();
		}
	}

	public boolean isMessageCenterWhoCardPreviouslyDisplayed() {
		return messageCenterWhoCardPreviouslyDisplayed;
	}

	public void setMessageCenterWhoCardPreviouslyDisplayed(boolean messageCenterWhoCardPreviouslyDisplayed) {
		if (this.messageCenterWhoCardPreviouslyDisplayed != messageCenterWhoCardPreviouslyDisplayed) {
			this.messageCenterWhoCardPreviouslyDisplayed = messageCenterWhoCardPreviouslyDisplayed;
			notifyDataChanged();
		}
	}

	public String getMessageCenterPendingMessage() {
		return messageCenterPendingMessage;
	}

	public void setMessageCenterPendingMessage(String messageCenterPendingMessage) {
		if (!TextUtils.equals(this.messageCenterPendingMessage, messageCenterPendingMessage)) {
			this.messageCenterPendingMessage = messageCenterPendingMessage;
			notifyDataChanged();
		}
	}

	public String getMessageCenterPendingAttachments() {
		return messageCenterPendingAttachments;
	}

	public void setMessageCenterPendingAttachments(String messageCenterPendingAttachments) {
		if (!TextUtils.equals(this.messageCenterPendingAttachments, messageCenterPendingAttachments)) {
			this.messageCenterPendingAttachments = messageCenterPendingAttachments;
			notifyDataChanged();
		}
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		if (!TextUtils.equals(this.targets, targets)) {
			this.targets = targets;
			notifyDataChanged();
		}
	}

	public String getInteractions() {
		return interactions;
	}

	public void setInteractions(String interactions) {
		if (!TextUtils.equals(this.interactions, interactions)) {
			this.interactions = interactions;
			notifyDataChanged();
		}
	}

	public double getInteractionExpiration() {
		return interactionExpiration;
	}

	public void setInteractionExpiration(double interactionExpiration) {
		if (this.interactionExpiration != interactionExpiration) {
			this.interactionExpiration = interactionExpiration;
			notifyDataChanged();
		}
	}

	public InteractionManager getInteractionManager() {
		return interactionManager;
	}

	public void setInteractionManager(InteractionManager interactionManager) {
		this.interactionManager = interactionManager;
	}

	synchronized boolean hasFile() {
		return file != null;
	}

	synchronized File getFile() {
		return file;
	}

	synchronized void setFile(File file) {
		this.file = file;
	}

	//endregion
}
