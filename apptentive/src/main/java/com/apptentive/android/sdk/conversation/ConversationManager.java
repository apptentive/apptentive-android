package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.os.Looper;

import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.ConversationMetadata.Filter;
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
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.ApptentiveNotifications.*;
import static com.apptentive.android.sdk.conversation.ConversationState.*;
import static com.apptentive.android.sdk.debug.Assert.*;
import static com.apptentive.android.sdk.debug.Tester.*;
import static com.apptentive.android.sdk.debug.TesterEvent.*;

/**
 * Class responsible for managing conversations.
 * <pre>
 *   - Saving/Loading conversations from/to files.
 *   - Switching conversations when users login/logout.
 *   - Creating anonymous conversation.
 * </pre>
 */
public class ConversationManager {

	protected static final String CONVERSATION_METADATA_PATH = "conversation-v1.meta";
	private static final String TAG_FETCH_CONVERSATION_TOKEN_REQUEST = "fetch_conversation_token";

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

		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_ACTIVITY_STARTED, new ApptentiveNotificationObserver() {
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

			if (activeConversation != null) {
				dispatchDebugEvent(EVT_CONVERSATION_LOAD,
					"successful", Boolean.TRUE,
					"conversation_state", activeConversation.getState().toString(),
					"conversation_identifier", activeConversation.getConversationId());

				handleConversationStateChange(activeConversation);
				return true;
			}

		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading active conversation");
		}

		dispatchDebugEvent(EVT_CONVERSATION_LOAD, "successful", Boolean.FALSE);
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

	/**
	 * Starts fetching conversation token. Returns immediately if conversation is already fetching.
	 *
	 * @return a new http-request object if conversation is not currently fetched or an instance of
	 * the existing request
	 */
	private HttpRequest fetchConversationToken(final Conversation conversation) {
		// check if context is lost
		final Context context = getContext();
		if (context == null) {
			ApptentiveLog.w(CONVERSATION, "Unable to fetch conversation token: context reference is lost");
			return null;
		}

		// check for an existing request
		HttpRequest existingRequest = getHttpClient().findRequest(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		if (existingRequest != null) {
			ApptentiveLog.d(CONVERSATION, "Conversation already fetching");
			return existingRequest;
		}

		ApptentiveLog.i(CONVERSATION, "Fetching Configuration token task started.");
		dispatchDebugEvent(EVT_CONVERSATION_WILL_FETCH_TOKEN);

		// Try to fetch a new one from the server.
		ConversationTokenRequest conversationTokenRequest = new ConversationTokenRequest();

		// Send the Device and Sdk now, so they are available on the server from the start.
		final Device device = DeviceManager.generateNewDevice(context);
		final Sdk sdk = SdkManager.generateCurrentSdk();
		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();

		conversationTokenRequest.setDevice(DeviceManager.getDiffPayload(null, device));
		conversationTokenRequest.setSdk(SdkManager.getPayload(sdk));
		conversationTokenRequest.setAppRelease(AppReleaseManager.getPayload(appRelease));

		HttpRequest request = getHttpClient()
			.getConversationToken(conversationTokenRequest, new HttpRequest.Listener<HttpJsonRequest>() {
				@Override
				public void onFinish(HttpJsonRequest request) {
					assertMainThread();

					try {
						JSONObject root = request.getResponseObject();
						String conversationToken = root.getString("token");
						ApptentiveLog.d(CONVERSATION, "ConversationToken: " + conversationToken);
						String conversationId = root.getString("id");
						ApptentiveLog.d(CONVERSATION, "New Conversation id: %s", conversationId);

						if (StringUtils.isNullOrEmpty(conversationToken)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'token'");
							dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
							return;
						}

						if (StringUtils.isNullOrEmpty(conversationId)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'id'");
							dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
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

						dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, true);

						handleConversationStateChange(conversation);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling conversation token");
						dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
					}
				}

				@Override
				public void onCancel(HttpJsonRequest request) {
					dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
				}

				@Override
				public void onFail(HttpJsonRequest request, String reason) {
					ApptentiveLog.w("Failed to fetch conversation token: %s", reason);
					dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
				}
			});

		request.setCallbackQueue(DispatchQueue.mainQueue()); // we only deal with conversation on the main queue
		request.setTag(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		return request;
	}

	//endregion

	//region Conversation fetching

	private void handleConversationStateChange(Conversation conversation) {
		assertMainThread();
		assertTrue(conversation != null && !conversation.hasState(UNDEFINED));

		if (conversation != null && !conversation.hasState(UNDEFINED)) {
			dispatchDebugEvent(EVT_CONVERSATION_STATE_CHANGE,
				"conversation_state", conversation.getState().toString(),
				"conversation_identifier", conversation.getConversationId());

			ApptentiveNotificationCenter.defaultCenter()
				.postNotificationSync(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE,
					ObjectUtils.toMap(NOTIFICATION_KEY_CONVERSATION, conversation));

			if (conversation.hasActiveState()) {
				conversation.fetchInteractions(getContext());
			}

			updateMetadataItems(conversation);
		}
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

		// delete all existing encryption keys
		for (ConversationMetadataItem item : conversationMetadata) {
			item.encryptionKey = null;
		}

		// update the state of the corresponding item
		ConversationMetadataItem item = conversationMetadata.findItem(conversation);
		if (item == null) {
			item = new ConversationMetadataItem(conversation.getConversationId(), conversation.getConversationDataFile(), conversation.getConversationMessagesFile());
			conversationMetadata.addItem(item);
		}
		item.state = conversation.getState();

		// update encryption key (if necessary)
		if (conversation.hasState(LOGGED_IN)) {
			item.encryptionKey = notNull(conversation.getEncryptionKey());
		}

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

	//region Login/Logout

	private static final LoginCallback NULL_LOGIN_CALLBACK = new LoginCallback() {
		@Override
		public void onLoginFinish() {
		}

		@Override
		public void onLoginFail(String errorMessage) {
		}
	};

	public void login(final String token, final LoginCallback callback) {
		// we only deal with an active conversation on the main thread
		DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				requestLoggedInConversation(token, callback != null ? callback : NULL_LOGIN_CALLBACK); // avoid constant null-pointer checking
			}
		});
	}

	private void requestLoggedInConversation(final String token, final LoginCallback callback) {
		assertMainThread();

		if (callback == null) {
			throw new IllegalArgumentException("Callback is null");
		}

		// check if active conversation exists
		if (activeConversation == null) {
			ApptentiveLog.d(CONVERSATION, "No active conversation. Performing login...");
			sendLoginRequest(token, callback);
			return;
		}

		switch (activeConversation.getState()) {
			case ANONYMOUS_PENDING:
				// start fetching conversation token (if not yet fetched)
				final HttpRequest fetchRequest = fetchConversationToken(activeConversation);
				if (fetchRequest == null) {
					ApptentiveLog.e(CONVERSATION, "Unable to login: fetch request failed to send");
					callback.onLoginFail("fetch request failed to send");
					return;
				}

				// attach a listener to an active request
				fetchRequest.addListener(new HttpRequest.Listener<HttpRequest>() {
					@Override
					public void onFinish(HttpRequest request) {
						assertTrue(activeConversation != null && activeConversation.hasState(ANONYMOUS), "Active conversation is missing or in a wrong state: %s", activeConversation);

						if (activeConversation != null && activeConversation.hasState(ANONYMOUS)) {
							ApptentiveLog.d(CONVERSATION, "Conversation fetching complete. Performing login...");
							sendLoginRequest(token, callback);
						} else {
							callback.onLoginFail("Conversation fetching completed abnormally");
						}
					}

					@Override
					public void onCancel(HttpRequest request) {
						ApptentiveLog.d(CONVERSATION, "Unable to login: conversation fetching cancelled.");
						callback.onLoginFail("Conversation fetching was cancelled");
					}

					@Override
					public void onFail(HttpRequest request, String reason) {
						ApptentiveLog.d(CONVERSATION, "Unable to login: conversation fetching failed.");
						callback.onLoginFail("Conversation fetching failed: " + reason);
					}
				});
				break;
			case ANONYMOUS:
				sendLoginRequest(token, callback);
				break;
			case LOGGED_IN:
				callback.onLoginFail("already logged in"); // TODO: force logout?
				break;
			default:
				assertFail("Unexpected conversation state: " + activeConversation.getState());
				callback.onLoginFail("internal error");
				break;
		}
	}

	private void sendLoginRequest(String token, final LoginCallback callback) {
		getHttpClient().login(token, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					final JSONObject responseObject = request.getResponseObject();
					final String encryptionKey = responseObject.getString("encryption_key");
					notifyLoginFinished(encryptionKey);
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while parsing login response");
				}
			}

			@Override
			public void onCancel(HttpJsonRequest request) {
				notifyLoginFailed("Login request was cancelled");
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				notifyLoginFailed(reason);
			}

			private void notifyLoginFinished(final String encryptionKey) {
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						assertFalse(StringUtils.isNullOrEmpty(encryptionKey));

						try {
							if (activeConversation != null) {
								activeConversation.setEncryptionKey(encryptionKey);
								activeConversation.setState(LOGGED_IN);
								handleConversationStateChange(activeConversation);

								// notify delegate
								callback.onLoginFinish();
							} else {
								notifyLoginFailed("Missing active conversation");
							}
						} catch (Exception e) {
							ApptentiveLog.e(e, "Exception while creating logged-in conversation");
							notifyLoginFailed("Internal error");
						}
					}
				});
			}

			private void notifyLoginFailed(final String reason) {
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						callback.onLoginFail(reason);
					}
				});
			}
		});
	}

	public void logout() {
		// we only deal with an active conversation on the main thread
		if (Looper.myLooper() != Looper.getMainLooper()) {
			DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					doLogout();
				}
			});
		} else {
			doLogout();
		}
	}

	private void doLogout() {
		// 1. Check to make sure we need to log out.
		if (activeConversation != null) {
			switch (activeConversation.getState()) {
				case LOGGED_IN:
					endActiveConversation();
					break;
				default:
					ApptentiveLog.w(CONVERSATION, "Attempted to logout() from Conversation, but the Active Conversation was not in LOGGED_IN state.");
					break;
			}
		} else {
			ApptentiveLog.w(CONVERSATION, "Attempted to logout(), but there was no Active Conversation.");
		}
		dispatchDebugEvent(EVT_LOGOUT);
	}

	//endregion

	//region Getters/Setters

	public Conversation getActiveConversation() {
		return activeConversation;
	}

	public ConversationMetadata getConversationMetadata() {
		return conversationMetadata;
	}

	private ApptentiveHttpClient getHttpClient() {
		return ApptentiveInternal.getInstance().getApptentiveHttpClient(); // TODO: remove coupling
	}

	private Context getContext() {
		return contextRef.get();
	}

	//endregion
}
