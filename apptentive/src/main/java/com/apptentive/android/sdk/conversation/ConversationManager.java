package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.migration.Migrator;
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
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Jwt;
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
import static com.apptentive.android.sdk.util.StringUtils.isNullOrEmpty;

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
			.addObserver(NOTIFICATION_APP_ENTERED_FOREGROUND, new ApptentiveNotificationObserver() {
				@Override
				public void onReceiveNotification(ApptentiveNotification notification) {
					if (activeConversation != null && activeConversation.hasActiveState()) {
						ApptentiveLog.v(CONVERSATION, "App entered foreground notification received. Trying to fetch interactions...");
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

		// If there is a Legacy Conversation, migrate it into the new Conversation object.
		// Check whether migration is needed.
		// No Conversations exist in the meta-data.
		// Do we have a Legacy Conversation or not?
		final SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		String legacyConversationToken = prefs.getString(Constants.PREF_KEY_CONVERSATION_TOKEN, null);
		if (!isNullOrEmpty(legacyConversationToken)) {
			String lastSeenVersionString = prefs.getString(Constants.PREF_KEY_LAST_SEEN_SDK_VERSION, null);
			Apptentive.Version version4 = new Apptentive.Version();
			version4.setVersion("4.0.0");
			Apptentive.Version lastSeenVersion = new Apptentive.Version();
			lastSeenVersion.setVersion(lastSeenVersionString);
			if (lastSeenVersionString != null && lastSeenVersion.compareTo(version4) < 0) {

				Migrator migrator = new Migrator(getContext(), prefs, anonymousConversation);
				migrator.migrate();

				anonymousConversation.setState(LEGACY_PENDING);
				anonymousConversation.setConversationToken(legacyConversationToken);

				fetchLegacyConversation(anonymousConversation)
					// remove legacy key when request is finished
					.addListener(new HttpRequest.Adapter<HttpRequest>() {
						@Override
						public void onFinish(HttpRequest request) {
							prefs.edit()
								.remove(Constants.PREF_KEY_CONVERSATION_TOKEN)
								.apply();
						}
					});
				return anonymousConversation;
			}
		}

		// If there is no Legacy Conversation, then just connect it to the server.
		anonymousConversation.setState(ANONYMOUS_PENDING);
		fetchConversationToken(anonymousConversation);
		return anonymousConversation;
	}

	private HttpRequest fetchLegacyConversation(final Conversation conversation) {
		assertNotNull(conversation);
		if (conversation == null) {
			throw new IllegalArgumentException("Conversation is null");
		}

		assertEquals(conversation.getState(), ConversationState.LEGACY_PENDING);

		final String conversationToken = conversation.getConversationToken();
		if (isNullOrEmpty(conversationToken)) {
			throw new IllegalStateException("Missing conversation token");
		}

		assertFalse(isNullOrEmpty(conversationToken));
		if (isNullOrEmpty(conversationToken)) {
			throw new IllegalArgumentException("Conversation is null");
		}

		HttpRequest request = getHttpClient()
			.getLegacyConversationId(conversationToken, new HttpRequest.Listener<HttpJsonRequest>() {
				@Override
				public void onFinish(HttpJsonRequest request) {
					assertMainThread();

					try {
						JSONObject root = request.getResponseObject();
						String conversationId = root.getString("conversation_id");
						ApptentiveLog.d(CONVERSATION, "Conversation id: %s", conversationId);

						if (isNullOrEmpty(conversationId)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch legacy conversation: missing 'id'");
							return;
						}

						String conversationJWT = root.getString("anonymous_jwt_token");
						if (isNullOrEmpty(conversationId)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch legacy conversation: missing 'anonymous_jwt_token'");
							return;
						}

						ApptentiveLog.d(CONVERSATION, "Conversation JWT: %s", conversationJWT);

						// set conversation data
						conversation.setState(ANONYMOUS);
						conversation.setConversationToken(conversationToken);
						conversation.setConversationId(conversationId);
						conversation.setJWT(conversationJWT);

						// handle state change
						handleConversationStateChange(conversation);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling legacy conversation id");
					}
				}

				@Override
				public void onCancel(HttpJsonRequest request) {
				}

				@Override
				public void onFail(HttpJsonRequest request, String reason) {
					ApptentiveLog.w("Failed to fetch legacy conversation id: %s", reason);
				}
			});

		request.setCallbackQueue(DispatchQueue.mainQueue()); // we only deal with conversation on the main queue
		request.setTag(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		return request;
	}

	private Conversation loadConversation(ConversationMetadataItem item) throws SerializerException {
		// TODO: use same serialization logic across the project
		final Conversation conversation = new Conversation(item.dataFile, item.messagesFile);
		conversation.setEncryptionKey(item.getEncryptionKey()); // it's important to set encryption key before loading data
		conversation.setState(item.getState()); // set the state same as the item's state
		conversation.setUserId(item.getUserId());
		conversation.setJWT(item.getJWT());
		conversation.loadConversationData();
		conversation.checkInternalConsistency();

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
	 * Starts fetching conversation. Returns immediately if conversation is already fetching.
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

						if (isNullOrEmpty(conversationToken)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'token'");
							dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
							return;
						}

						if (isNullOrEmpty(conversationId)) {
							ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'id'");
							dispatchDebugEvent(EVT_CONVERSATION_DID_FETCH_TOKEN, false);
							return;
						}

						// set conversation data
						conversation.setState(ANONYMOUS);
						conversation.setConversationToken(conversationToken);
						conversation.setConversationId(conversationId);
						conversation.setDevice(device);
						conversation.setLastSentDevice(device);
						conversation.setAppRelease(appRelease);
						conversation.setSdk(sdk);
						conversation.setLastSeenSdkVersion(sdk.getVersion());

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

				// Update conversation with push configuration changes that happened while it wasn't active.
				SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
				int pushProvider = prefs.getInt(Constants.PREF_KEY_PUSH_PROVIDER, -1);
				String pushToken = prefs.getString(Constants.PREF_KEY_PUSH_TOKEN, null);
				if (pushProvider != -1 && pushToken != null) {
					conversation.setPushIntegration(pushProvider, pushToken);
				}
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

		if (conversation.hasState(ANONYMOUS_PENDING, LEGACY_PENDING)) {
			ApptentiveLog.v(CONVERSATION, "Skipping updating metadata since conversation is %s", conversation.getState());
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
		item.JWT = conversation.getJWT(); // TODO: can it be null for active conversations?

		// update encryption key (if necessary)
		if (conversation.hasState(LOGGED_IN)) {
			item.encryptionKey = notNull(conversation.getEncryptionKey());
			item.userId = notNull(conversation.getUserId());
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
		if (Looper.getMainLooper() == Looper.myLooper()) {
			requestLoggedInConversation(token, callback != null ? callback : NULL_LOGIN_CALLBACK); // avoid constant null-pointer checking
		} else {
			DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					requestLoggedInConversation(token, callback != null ? callback : NULL_LOGIN_CALLBACK); // avoid constant null-pointer checking
				}
			});
		}
	}

	private void requestLoggedInConversation(final String token, final LoginCallback callback) {
		assertMainThread();

		if (callback == null) {
			throw new IllegalArgumentException("Callback is null");
		}

		final String userId;
		try {
			final Jwt jwt = Jwt.decode(token);
			userId = jwt.getPayload().getString("sub");
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error while extracting user id: Missing field \"sub\"");
			callback.onLoginFail(e.getMessage());
			return;
		}

		// Check if there is an active conversation
		if (activeConversation == null) {
			ApptentiveLog.d(CONVERSATION, "No active conversation. Performing login...");

			// attempt to find previous logged out conversation
			final ConversationMetadataItem conversationItem = conversationMetadata.findItem(new Filter() {
				@Override
				public boolean accept(ConversationMetadataItem item) {
					return StringUtils.equal(item.getUserId(), userId);
				}
			});

			if (conversationItem == null) {
				ApptentiveLog.e("Unable to find an existing conversation with for user: '" + userId + "'");
				callback.onLoginFail("No previous conversation found");
				return;
			}

			sendLoginRequest(conversationItem.conversationId, userId, token, callback);
			return;
		}

		switch (activeConversation.getState()) {
			case ANONYMOUS_PENDING:
			case LEGACY_PENDING: {
					// start fetching conversation token (if not yet fetched)
					final HttpRequest fetchRequest = activeConversation.hasState(ANONYMOUS_PENDING) ?
								fetchConversationToken(activeConversation) :
								fetchLegacyConversation(activeConversation);
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
								sendLoginRequest(activeConversation.getConversationId(), userId, token, callback);
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
				}
				break;
			case ANONYMOUS:
				sendLoginRequest(activeConversation.getConversationId(), userId, token, callback);
				break;
			case LOGGED_IN:
				if (activeConversation.getUserId().equals(userId)) {
					ApptentiveLog.w("Already logged in as \"%s\"", userId);
					callback.onLoginFinish();
					return;
				}
				// FIXME: If they are attempting to login to a different conversation, we need to gracefully end the active conversation here and kick off a login request to the desired conversation.
				callback.onLoginFail("Already logged in. You must log out first.");
				break;
			default:
				assertFail("Unexpected conversation state: " + activeConversation.getState());
				callback.onLoginFail("internal error");
				break;
		}
	}

	private void sendLoginRequest(String conversationId, final String userId, final String token, final LoginCallback callback) {
		getHttpClient().login(conversationId, token, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					final JSONObject responseObject = request.getResponseObject();
					final String encryptionKey = responseObject.getString("encryption_key");
					handleLoginFinished(userId, token, encryptionKey);
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while parsing login response");
					handleLoginFailed("Internal error");
				}
			}

			@Override
			public void onCancel(HttpJsonRequest request) {
				handleLoginFailed("Login request was cancelled");
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				handleLoginFailed(reason);
			}

			private void handleLoginFinished(final String userId, final String token, final String encryptionKey) {
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						assertFalse(isNullOrEmpty(encryptionKey),"Login finished with missing encryption key.");
						assertFalse(isNullOrEmpty(token), "Login finished with missing token.");

						try {
							// if we were previously logged out we might end up with no active conversation
							if (activeConversation == null) {
								// attempt to find previous logged out conversation
								final ConversationMetadataItem conversationItem = conversationMetadata.findItem(new Filter() {
									@Override
									public boolean accept(ConversationMetadataItem item) {
										return StringUtils.equal(item.getUserId(), userId);
									}
								});

								if (conversationItem == null) {
									handleLoginFailed("Unable to find an existing conversation with for user: '" + userId + "'");
									return;
								}

								activeConversation = loadConversation(conversationItem);
							}

							activeConversation.setEncryptionKey(encryptionKey);
							activeConversation.setConversationToken(token);
							activeConversation.setUserId(userId);
							activeConversation.setState(LOGGED_IN);
							handleConversationStateChange(activeConversation);

							// notify delegate
							callback.onLoginFinish();
						} catch (Exception e) {
							ApptentiveLog.e(e, "Exception while creating logged-in conversation");
							handleLoginFailed("Internal error");
						}
					}
				});
			}

			private void handleLoginFailed(final String reason) {
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
