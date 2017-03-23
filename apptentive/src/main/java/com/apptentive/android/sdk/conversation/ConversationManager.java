package com.apptentive.android.sdk.conversation;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.ConversationMetadata.Filter;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.serialization.ObjectSerialization;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.SerializerException;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ACTIVITY_STARTED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_STATE_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION;
import static com.apptentive.android.sdk.conversation.ConversationState.ANONYMOUS;
import static com.apptentive.android.sdk.conversation.ConversationState.ANONYMOUS_PENDING;
import static com.apptentive.android.sdk.conversation.ConversationState.LOGGED_IN;
import static com.apptentive.android.sdk.conversation.ConversationState.LOGGED_OUT;
import static com.apptentive.android.sdk.conversation.ConversationState.UNDEFINED;
import static com.apptentive.android.sdk.debug.Tester.dispatchDebugEvent;
import static com.apptentive.android.sdk.debug.TesterEvent.EVT_CONVERSATION_CREATE;
import static com.apptentive.android.sdk.debug.TesterEvent.EVT_CONVERSATION_LOAD_ACTIVE;
import static com.apptentive.android.sdk.debug.TesterEvent.EVT_CONVERSATION_METADATA_LOAD;

/**
 * Class responsible for managing conversations.
 * <pre>
 *   - Saving/Loading conversations from/to files.
 *   - Switching conversations when users login/logout.
 *   - Creating default conversation.
 *   - Migrating legacy conversation data.
 * </pre>
 */
public class ConversationManager {

	protected static final String CONVERSATION_METADATA_PATH = "conversation-v1.meta";

	private final WeakReference<Context> contextRef;

	/**
	 * A basic directory for storing conversation-related data.
	 */
	private final File storageDir;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	private Conversation activeConversation;

	public ConversationManager(Context context, File storageDir) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		this.contextRef = new WeakReference<>(context.getApplicationContext());
		this.storageDir = storageDir;

		ApptentiveNotificationCenter.defaultCenter().addObserver(NOTIFICATION_ACTIVITY_STARTED,
			new ApptentiveNotificationObserver() {
				@Override
				public void onReceiveNotification(ApptentiveNotification notification) {
					if (activeConversation != null && activeConversation.hasActiveState()) {
						ApptentiveLog.v(CONVERSATION, "Activity 'start' notification received. Trying to fetch interactions...");
						final Context context = getContext();
						if (context != null) {
							activeConversation.fetchInteractions(context);
						} else {
							ApptentiveLog.w(CONVERSATION, "Can't fetch conversation interactions: context is lost");
						}
					}
				}
			});
	}

	//region Conversations

	/**
	 * Attempts to load an active conversation. Returns <code>false</code> if active conversation is
	 * missing or cannot be loaded
	 */
	public boolean loadActiveConversation(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		try {
			// resolving metadata
			conversationMetadata = resolveMetadata();

			// attempt to load existing conversation
			activeConversation = loadActiveConversationGuarded();
			dispatchDebugEvent(EVT_CONVERSATION_LOAD_ACTIVE, activeConversation != null);

			if (activeConversation != null) {
				handleConversationStateChange(activeConversation);
				return true;
			}

		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading active conversation");
		}

		return false;
	}

	private Conversation loadActiveConversationGuarded() throws IOException, SerializerException {
		// we're going to scan metadata in attempt to find existing conversations
		ConversationMetadataItem item;

		// if the user was logged in previously - we should have an active conversation
		item = conversationMetadata.findItem(LOGGED_IN);
		if (item != null) {
			ApptentiveLog.v(CONVERSATION, "Loading logged-in conversation...");
			return loadConversation(item);
		}

		// if no users were logged in previously - we might have an anonymous conversation
		item = conversationMetadata.findItem(ANONYMOUS);
		if (item != null) {
			ApptentiveLog.v(CONVERSATION, "Loading anonymous conversation...");
			return loadConversation(item);
		}

		// check if we have a 'pending' anonymous conversation
		item = conversationMetadata.findItem(ANONYMOUS_PENDING);
		if (item != null) {
			final Conversation conversation = loadConversation(item);
			fetchConversationToken(conversation);
			return conversation;
		}

		// seems like we only have 'logged-out' conversations
		if (conversationMetadata.hasItems()) {
			ApptentiveLog.v(CONVERSATION, "Can't load conversation: only 'logged-out' conversations available");
			return null;
		}

		// no conversation available: create a new one
		ApptentiveLog.v(CONVERSATION, "Can't load conversation: creating anonymous conversation...");
		File dataFile = new File(storageDir, Util.generateRandomFilename());
		File messagesFile = new File(storageDir, Util.generateRandomFilename());
		Conversation anonymousConversation = new Conversation(dataFile, messagesFile);
		anonymousConversation.setState(ANONYMOUS_PENDING);
		fetchConversationToken(anonymousConversation);
		return anonymousConversation;
	}

	private Conversation loadConversation(ConversationMetadataItem item) throws SerializerException {
		// TODO: use same serialization logic across the project
		final Conversation conversation = new Conversation(item.dataFile, item.messagesFile);
		conversation.loadConversationData();
		conversation.setState(item.getState()); // set the state same as the item's state
		return conversation;
	}

	/**
	 * Ends active conversation (user logs out, etc)
	 */
	public synchronized boolean endActiveConversation() {
		if (activeConversation != null) {
			activeConversation.setState(LOGGED_OUT);
			handleConversationStateChange(activeConversation);
			activeConversation = null;
			return true;
		}
		return false;
	}

	//endregion

	//region Conversation Token Fetching

	private void fetchConversationToken(final Conversation conversation) {
		ApptentiveLog.i(CONVERSATION, "Fetching Configuration token task started.");

		final Context context = getContext();
		if (context == null) {
			ApptentiveLog.w(CONVERSATION, "Unable to fetch conversation token: context reference is lost");
			return;
		}

		// Try to fetch a new one from the server.
		ConversationTokenRequest request = new ConversationTokenRequest();

		// Send the Device and Sdk now, so they are available on the server from the start.
		final Device device = DeviceManager.generateNewDevice(context);
		final Sdk sdk = SdkManager.generateCurrentSdk();
		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();

		request.setDevice(DeviceManager.getDiffPayload(null, device));
		request.setSdk(SdkManager.getPayload(sdk));
		request.setAppRelease(AppReleaseManager.getPayload(appRelease));

		ApptentiveInternal.getInstance().getApptentiveHttpClient()
			.getConversationToken(request, new HttpRequest.Listener<HttpJsonRequest>() {
				@Override
				public void onFinish(HttpJsonRequest request) {
					try {
						JSONObject root = request.getResponseObject();
						String conversationToken = root.getString("token");
						ApptentiveLog.d(CONVERSATION, "ConversationToken: " + conversationToken);
						String conversationId = root.getString("id");
						ApptentiveLog.d(CONVERSATION, "New Conversation id: %s", conversationId);

						if (StringUtils.isNullOrEmpty(conversationToken)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'token'");
							dispatchDebugEvent(EVT_CONVERSATION_CREATE, false);
							return;
						}

						if (StringUtils.isNullOrEmpty(conversationId)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'id'");
							dispatchDebugEvent(EVT_CONVERSATION_CREATE, false);
							return;
						}

						// set conversation data
						conversation.setState(ANONYMOUS);
						conversation.setConversationToken(conversationToken);
						conversation.setConversationId(conversationId);
						conversation.setDevice(device);
						conversation.setSdk(sdk);
						conversation.setAppRelease(appRelease);

						String personId = root.getString("person_id");
						ApptentiveLog.d(CONVERSATION, "PersonId: " + personId);
						conversation.setPersonId(personId);

						// write conversation to the disk (sync operation)
						conversation.saveConversationData();

						dispatchDebugEvent(EVT_CONVERSATION_CREATE, true);

						handleConversationStateChange(conversation);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling conversation token");
						dispatchDebugEvent(EVT_CONVERSATION_CREATE, false);
					}
				}

				@Override
				public void onCancel(HttpJsonRequest request) {
					dispatchDebugEvent(EVT_CONVERSATION_CREATE, false);
				}

				@Override
				public void onFail(HttpJsonRequest request, String reason) {
					ApptentiveLog.w("Failed to fetch conversation token: %s", reason);
					dispatchDebugEvent(EVT_CONVERSATION_CREATE, false);
				}
			});
	}

	private File getConversationDataFile(Conversation conversation) {
		return new File(storageDir, conversation.getConversationId() + "-conversation.bin");
	}

	private File getConversationMessagesFile(Conversation conversation) {
		return new File(storageDir, conversation.getConversationId() + "-messages.bin");
	}

	//endregion

	//region Conversation fetching

	private void handleConversationStateChange(Conversation conversation) {
		Assert.assertTrue(conversation != null && !conversation.hasState(UNDEFINED)); // sanity check

		ApptentiveNotificationCenter.defaultCenter()
			.postNotificationSync(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE,
				ObjectUtils.toMap(NOTIFICATION_KEY_CONVERSATION, conversation));

		if (conversation != null && conversation.hasActiveState()) {
			conversation.fetchInteractions(getContext());
		}

		updateMetadataItems(conversation);
	}

	/* For testing purposes */
	public synchronized boolean setActiveConversation(final String conversationId) throws SerializerException {
		final ConversationMetadataItem item = conversationMetadata.findItem(new Filter() {
			@Override
			public boolean accept(ConversationMetadataItem item) {
				return item.conversationId.equals(conversationId);
			}
		});

		if (item == null) {
			ApptentiveLog.w(CONVERSATION, "Conversation not found: %s", conversationId);
			return false;
		}

		final Conversation conversation = loadConversation(item);
		if (conversation == null) {
			ApptentiveLog.w(CONVERSATION, "Conversation not loaded: %s", conversationId);
			return false;
		}

		handleConversationStateChange(conversation);
		return true;
	}

	private void updateMetadataItems(Conversation conversation) {

		if (conversation.hasState(ANONYMOUS_PENDING)) {
			ApptentiveLog.v(CONVERSATION, "Skipping updating metadata since conversation is anonymous and pending");
			return;
		}

		// if the conversation is 'logged-in' we should not have any other 'logged-in' items in metadata
		if (conversation.hasState(LOGGED_IN)) {
			for (ConversationMetadataItem item : conversationMetadata) {
				if (item.state.equals(LOGGED_IN)) {
					item.state = LOGGED_OUT;
				}
			}
		}

		// update the state of the corresponding item
		ConversationMetadataItem item = conversationMetadata.findItem(conversation);
		if (item == null) {
			item = new ConversationMetadataItem(conversation.getConversationId(), conversation.getConversationDataFile(), conversation.getConversationMessagesFile());
			conversationMetadata.addItem(item);
		}
		item.state = conversation.getState();

		// apply changes
		saveMetadata();
	}

	//endregion

	//region Metadata

	private ConversationMetadata resolveMetadata() {
		try {
			File metaFile = new File(storageDir, CONVERSATION_METADATA_PATH);
			if (metaFile.exists()) {
				ApptentiveLog.v(CONVERSATION, "Loading meta file: " + metaFile);
				final ConversationMetadata metadata = ObjectSerialization.deserialize(metaFile, ConversationMetadata.class);
				dispatchDebugEvent(EVT_CONVERSATION_METADATA_LOAD, true);
				return metadata;
			} else {
				ApptentiveLog.v(CONVERSATION, "Meta file does not exist: " + metaFile);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading conversation metadata");
		}

		dispatchDebugEvent(EVT_CONVERSATION_METADATA_LOAD, false);
		return new ConversationMetadata();
	}

	private void saveMetadata() {
		try {
			long start = System.currentTimeMillis();
			File metaFile = new File(storageDir, CONVERSATION_METADATA_PATH);
			ObjectSerialization.serialize(metaFile, conversationMetadata);
			ApptentiveLog.v(CONVERSATION, "Saved metadata (took %d ms)", System.currentTimeMillis() - start);
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, "Exception while saving metadata");
		}
	}

	//endregion

	//region Getters/Setters

	public Conversation getActiveConversation() {
		return activeConversation;
	}

	public ConversationMetadata getConversationMetadata() {
		return conversationMetadata;
	}

	private Context getContext() {
		return contextRef.get();
	}

	//endregion
}
