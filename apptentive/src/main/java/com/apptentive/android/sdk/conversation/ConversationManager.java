/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.ConversationMetadata.Filter;
import com.apptentive.android.sdk.migration.Migrator;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.ConversationItem;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
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
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveLog.Level.VERY_VERBOSE;
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
	private static final String TAG_FETCH_APP_CONFIGURATION_REQUEST = "fetch_app_configuration";

	private final WeakReference<Context> contextRef;

	/**
	 * A basic directory for storing conversation-related data.
	 */
	private final File apptentiveConversationsStorageDir;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	private Conversation activeConversation;

	public ConversationManager(Context context, File apptentiveConversationsStorageDir) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		this.contextRef = new WeakReference<>(context.getApplicationContext());
		this.apptentiveConversationsStorageDir = apptentiveConversationsStorageDir;

		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_APP_ENTERED_FOREGROUND, new ApptentiveNotificationObserver() {
				@Override
				public void onReceiveNotification(ApptentiveNotification notification) {
					assertMainThread();
					if (activeConversation != null && activeConversation.hasActiveState()) {
						ApptentiveLog.v(CONVERSATION, "App entered foreground notification received. Trying to fetch app configuration and interactions...");
						final Context context = getContext();
						if (context != null) {
							fetchAppConfiguration(activeConversation);
							activeConversation.fetchInteractions(context);
						} else {
							ApptentiveLog.w(CONVERSATION, "Can't fetch app configuration and conversation interactions: context is lost");
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
			assertMainThread();

			// resolving metadata
			ApptentiveLog.vv(CONVERSATION, "Resolving metadata...");
			conversationMetadata = resolveMetadata();
			if (ApptentiveLog.canLog(VERY_VERBOSE)) {
				printMetadata(conversationMetadata, "Loaded Metadata");
			}

			// attempt to load existing conversation
			ApptentiveLog.vv(CONVERSATION, "Loading active conversation...");
			activeConversation = loadActiveConversationGuarded();

			if (activeConversation != null) {
				activeConversation.startListeningForChanges();
				activeConversation.scheduleSaveConversationData();

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
			ApptentiveLog.v(CONVERSATION, "Loading anonymous pending conversation...");
			final Conversation conversation = loadConversation(item);
			fetchConversationToken(conversation);
			return conversation;
		}

		// check if we have a 'legacy pending' conversation
		item = conversationMetadata.findItem(LEGACY_PENDING);
		if (item != null) {
			ApptentiveLog.v(CONVERSATION, "Loading legacy pending conversation...");
			final Conversation conversation = loadConversation(item);
			fetchLegacyConversation(conversation);
			return conversation;
		}

		// Check for only LOGGED_OUT Conversations
		if (conversationMetadata.hasItems()) {
			ApptentiveLog.v(CONVERSATION, "Can't load conversation: only 'logged-out' conversations available");
			return null;
		}

		// No conversation exists: Create a new one
		ApptentiveLog.v(CONVERSATION, "Can't load conversation: creating anonymous conversation...");
		File dataFile = new File(apptentiveConversationsStorageDir, "conversation-" + Util.generateRandomFilename());
		File messagesFile = new File(apptentiveConversationsStorageDir, "messages-" + Util.generateRandomFilename());
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

				anonymousConversation.setState(LEGACY_PENDING);
				anonymousConversation.setConversationToken(legacyConversationToken);

				Migrator migrator = new Migrator(getContext(), prefs, anonymousConversation);
				migrator.migrate();

				ApptentiveLog.v("Fetching legacy conversation...");
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
			.createLegacyConversationIdRequest(conversationToken, new HttpRequest.Listener<HttpJsonRequest>() {
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
						conversation.setConversationToken(conversationJWT);
						conversation.setConversationId(conversationId);

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
					ApptentiveLog.e("Failed to fetch legacy conversation id: %s", reason);
				}
			});

		request.setCallbackQueue(DispatchQueue.mainQueue()); // we only deal with conversation on the main queue
		request.setTag(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		request.start();
		return request;
	}

	private Conversation loadConversation(ConversationMetadataItem item) throws SerializerException {
		// TODO: use same serialization logic across the project
		final Conversation conversation = new Conversation(item.dataFile, item.messagesFile);
		conversation.setEncryptionKey(item.getEncryptionKey()); // it's important to set encryption key before loading data
		conversation.setState(item.getState()); // set the state same as the item's state
		conversation.setUserId(item.getUserId());
		conversation.setConversationToken(item.getConversationToken()); // TODO: this would be overwritten by the next call
		conversation.loadConversationData();
		conversation.checkInternalConsistency();

		return conversation;
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
		final Sdk sdk = SdkManager.generateCurrentSdk(context);
		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();

		conversationTokenRequest.setDevice(DeviceManager.getDiffPayload(null, device));
		conversationTokenRequest.setSdkAndAppRelease(SdkManager.getPayload(sdk), AppReleaseManager.getPayload(appRelease));

		HttpRequest request = getHttpClient()
			.createConversationTokenRequest(conversationTokenRequest, new HttpRequest.Listener<HttpJsonRequest>() {
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
						conversation.setLastSentDevice(device.clone());
						conversation.setAppRelease(appRelease);
						conversation.setSdk(sdk);
						conversation.setLastSeenSdkVersion(sdk.getVersion());

						String personId = root.getString("person_id");
						ApptentiveLog.d(CONVERSATION, "PersonId: " + personId);
						conversation.getPerson().setId(personId);

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
		request.start();
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
				.postNotification(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE,
					ObjectUtils.toMap(NOTIFICATION_KEY_CONVERSATION, conversation));

			if (conversation.hasActiveState()) {
				conversation.fetchInteractions(getContext());
				conversation.getMessageManager().startPollingMessages();

				// Fetch app configuration
				fetchAppConfiguration(conversation);

				// Update conversation with push configuration changes that happened while it wasn't active.
				SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
				int pushProvider = prefs.getInt(Constants.PREF_KEY_PUSH_PROVIDER, -1);
				String pushToken = prefs.getString(Constants.PREF_KEY_PUSH_TOKEN, null);
				if (pushProvider != -1 && pushToken != null) {
					conversation.setPushIntegration(pushProvider, pushToken);
				}
			}

			updateMetadataItems(conversation);
			if (ApptentiveLog.canLog(VERY_VERBOSE)) {
				printMetadata(conversationMetadata, "Updated Metadata");
			}
		}
	}

	private void fetchAppConfiguration(Conversation conversation) {
		try {
			fetchAppConfigurationGuarded(conversation);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while fetching app configuration");
		}
	}

	private void fetchAppConfigurationGuarded(Conversation conversation) {
		ApptentiveLog.d(APP_CONFIGURATION, "Fetching app configuration...");

		HttpRequest existingRequest = getHttpClient().findRequest(TAG_FETCH_APP_CONFIGURATION_REQUEST);
		if (existingRequest != null) {
			ApptentiveLog.d(APP_CONFIGURATION, "Can't fetch app configuration: another request already pending");
			return;
		}

		if (!Configuration.load().hasConfigurationCacheExpired()) {
			// if configuration hasn't expired we would fetch it anyway for debug apps
			boolean debuggable = RuntimeUtils.isAppDebuggable(getContext());
			if (!debuggable) {
				ApptentiveLog.d(APP_CONFIGURATION, "Can't fetch app configuration: the old configuration is still valid");
				return;
			}
		}

		HttpJsonRequest request = getHttpClient()
				.createAppConfigurationRequest(conversation.getConversationId(), conversation.getConversationToken(),
						new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					String cacheControl = request.getResponseHeader("Cache-Control");
					Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
					if (cacheSeconds == null) {
						cacheSeconds = Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS;
					}
					ApptentiveLog.d(APP_CONFIGURATION, "Caching configuration for %d seconds.", cacheSeconds);
					Configuration config = new Configuration(request.getResponseObject().toString());
					config.setConfigurationCacheExpirationMillis(System.currentTimeMillis() + cacheSeconds * 1000);
					config.save();
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while parsing app configuration response");
				}
			}

			@Override
			public void onCancel(HttpJsonRequest request) {
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				ApptentiveLog.e(APP_CONFIGURATION, "App configuration request failed: %s", reason);
			}
		});
		request.setTag(TAG_FETCH_APP_CONFIGURATION_REQUEST);
		request.start();
	}

	private void updateMetadataItems(Conversation conversation) {
		ApptentiveLog.vv("Updating metadata: state=%s localId=%s conversationId=%s token=%s",
				conversation.getState(),
				conversation.getLocalIdentifier(),
				conversation.getConversationId(),
				conversation.getConversationToken());

		// if the conversation is 'logged-in' we should not have any other 'logged-in' items in metadata
		if (conversation.hasState(LOGGED_IN)) {
			for (ConversationMetadataItem item : conversationMetadata) {
				if (item.state.equals(LOGGED_IN)) {
					item.state = LOGGED_OUT;
				}
			}
		}

		// delete sensitive information
		for (ConversationMetadataItem item : conversationMetadata) {
			item.encryptionKey = null;
			item.conversationToken = null;
		}

		// update the state of the corresponding item
		ConversationMetadataItem item = conversationMetadata.findItem(conversation);
		if (item == null) {
			item = new ConversationMetadataItem(conversation.getLocalIdentifier(), conversation.getConversationId(), conversation.getConversationDataFile(), conversation.getConversationMessagesFile());
			conversationMetadata.addItem(item);
		} else {
			assertTrue(conversation.getConversationId() != null || conversation.hasState(ANONYMOUS_PENDING), "Missing conversation id for state: %s", conversation.getState());
			item.conversationId = conversation.getConversationId();
		}

		item.state = conversation.getState();
		if (conversation.hasActiveState()) {
			item.conversationToken = notNull(conversation.getConversationToken());
		}

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
			File metaFile = new File(apptentiveConversationsStorageDir, CONVERSATION_METADATA_PATH);
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
			if (ApptentiveLog.canLog(VERY_VERBOSE)) {
				ApptentiveLog.vv(CONVERSATION, "Saving metadata: ", conversationMetadata.toString());
			}
			long start = System.currentTimeMillis();
			File metaFile = new File(apptentiveConversationsStorageDir, CONVERSATION_METADATA_PATH);
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
		if (DispatchQueue.isMainQueue()) {
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
			userId = jwt.getPayload().optString("sub");
			if (StringUtils.isNullOrEmpty(userId)) {
				ApptentiveLog.e("Error while extracting user id: Missing field \"sub\"");
				callback.onLoginFail("Error while extracting user id: Missing field \"sub\"");
				return;
			}

		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while extracting user id");
			callback.onLoginFail("Exception while extracting user id");
			return;
		}

		assertMainThread();

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
				ApptentiveLog.w("No conversation found matching user: '%s'. Logging in as new user.", userId);
				sendFirstLoginRequest(userId, token, callback);
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
							assertMainThread();
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
				if (StringUtils.equal(activeConversation.getUserId(), userId)) {
					ApptentiveLog.w("Already logged in as \"%s\"", userId);
					callback.onLoginFinish();
					return;
				}
				// TODO: If they are attempting to login to a different conversation, we need to gracefully end the active conversation here and kick off a login request to the desired conversation.
				callback.onLoginFail("Already logged in. You must log out first.");
				break;
			default:
				assertFail("Unexpected conversation state: " + activeConversation.getState());
				callback.onLoginFail("internal error");
				break;
		}
	}

	private void sendLoginRequest(String conversationId, final String userId, final String token, final LoginCallback callback) {
		HttpJsonRequest request = getHttpClient().createLoginRequest(conversationId, token, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					final JSONObject responseObject = request.getResponseObject();
					final String encryptionKey = responseObject.getString("encryption_key");
					final String incomingConversationId = responseObject.getString("id");
					handleLoginFinished(incomingConversationId, userId, token, encryptionKey);
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

			private void handleLoginFinished(final String conversationId, final String userId, final String token, final String encryptionKey) {
				assertFalse(isNullOrEmpty(encryptionKey),"Login finished with missing encryption key.");
				assertFalse(isNullOrEmpty(token), "Login finished with missing token.");
				assertMainThread();

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

						if (conversationItem != null) {
							conversationItem.conversationToken = token;
							conversationItem.encryptionKey = encryptionKey;
							activeConversation = loadConversation(conversationItem);
						} else {
							ApptentiveLog.v(CONVERSATION, "Creating new logged in conversation...");
							File dataFile = new File(apptentiveConversationsStorageDir, "conversation-" + Util.generateRandomFilename());
							File messagesFile = new File(apptentiveConversationsStorageDir, "messages-" + Util.generateRandomFilename());
							activeConversation = new Conversation(dataFile, messagesFile);

							// TODO: if we don't set these here - device payload would return 4xx error code
							activeConversation.setDevice(DeviceManager.generateNewDevice(getContext()));
							activeConversation.setAppRelease(ApptentiveInternal.getInstance().getAppRelease());
							activeConversation.setSdk(SdkManager.generateCurrentSdk(getContext()));
						}
					}

					activeConversation.setEncryptionKey(encryptionKey);
					activeConversation.setConversationToken(token);
					activeConversation.setConversationId(conversationId);
					activeConversation.setUserId(userId);
					activeConversation.setState(LOGGED_IN);

					activeConversation.startListeningForChanges();
					activeConversation.scheduleSaveConversationData();

					handleConversationStateChange(activeConversation);

					// notify delegate
					callback.onLoginFinish();
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while creating logged-in conversation");
					handleLoginFailed("Internal error");
				}
			}

			private void handleLoginFailed(String reason) {
				callback.onLoginFail(reason);
			}
		});
		request.setCallbackQueue(DispatchQueue.mainQueue());
		request.start();
	}

	private void sendFirstLoginRequest(final String userId, final String token, final LoginCallback callback) {
		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();
		final Sdk sdk = SdkManager.generateCurrentSdk(getContext());
		final Device device = DeviceManager.generateNewDevice(getContext());

		HttpJsonRequest request = getHttpClient().createFirstLoginRequest(token, appRelease, sdk, device, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					final JSONObject responseObject = request.getResponseObject();
					final String encryptionKey = responseObject.getString("encryption_key");
					final String incomingConversationId = responseObject.getString("id");
					handleLoginFinished(incomingConversationId, userId, token, encryptionKey);
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

			private void handleLoginFinished(final String conversationId, final String userId, final String token, final String encryptionKey) {
				assertNull(activeConversation, "Finished logging into new conversation, but one was already active.");
				assertFalse(isNullOrEmpty(encryptionKey),"Login finished with missing encryption key.");
				assertFalse(isNullOrEmpty(token), "Login finished with missing token.");
				assertMainThread();

				try {
					// attempt to find previous logged out conversation
					final ConversationMetadataItem conversationItem = conversationMetadata.findItem(new Filter() {
						@Override
						public boolean accept(ConversationMetadataItem item) {
							return StringUtils.equal(item.getUserId(), userId);
						}
					});

					if (conversationItem != null) {
						conversationItem.conversationToken = token;
						conversationItem.encryptionKey = encryptionKey;
						activeConversation = loadConversation(conversationItem);
					} else {
						ApptentiveLog.v(CONVERSATION, "Creating new logged in conversation...");
						File dataFile = new File(apptentiveConversationsStorageDir, "conversation-" + Util.generateRandomFilename());
						File messagesFile = new File(apptentiveConversationsStorageDir, "messages-" + Util.generateRandomFilename());
						activeConversation = new Conversation(dataFile, messagesFile);

						activeConversation.setAppRelease(appRelease);
						activeConversation.setSdk(sdk);
						activeConversation.setDevice(device);
					}

					activeConversation.setEncryptionKey(encryptionKey);
					activeConversation.setConversationToken(token);
					activeConversation.setConversationId(conversationId);
					activeConversation.setUserId(userId);
					activeConversation.setState(LOGGED_IN);

					activeConversation.startListeningForChanges();
					activeConversation.scheduleSaveConversationData();

					handleConversationStateChange(activeConversation);

					// notify delegate
					callback.onLoginFinish();
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while creating logged-in conversation");
					handleLoginFailed("Internal error");
				}
			}

			private void handleLoginFailed(String reason) {
				callback.onLoginFail(reason);
			}
		});
		request.setCallbackQueue(DispatchQueue.mainQueue());
		request.start();
	}

	public void logout() {
		// we only deal with an active conversation on the main thread
		if (!DispatchQueue.isMainQueue()) {
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
		assertMainThread();
		if (activeConversation != null) {
			switch (activeConversation.getState()) {
				case LOGGED_IN:
					ApptentiveLog.d("Ending active conversation.");
					EngagementModule.engageInternal(getContext(), activeConversation, "logout");
					// Post synchronously to ensure logout payload can be sent before destroying the logged in conversation.
					ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_CONVERSATION_WILL_LOGOUT, ObjectUtils.toMap(NOTIFICATION_KEY_CONVERSATION, activeConversation));
					activeConversation.destroy();
					activeConversation.setState(LOGGED_OUT);
					handleConversationStateChange(activeConversation);
					activeConversation = null;
					ApptentiveInternal.dismissAllInteractions();
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

	//region Debug

	private void printMetadata(ConversationMetadata metadata, String title) {
		List<ConversationMetadataItem> items = metadata.getItems();
		if (items.isEmpty()) {
			ApptentiveLog.vv(CONVERSATION, "%s (%d item(s))", title, items.size());
			return;
		}

		Object[][] rows = new Object[1 + items.size()][];
		rows[0] = new Object[] {
			"state",
			"localId",
			"conversationId",
			"userId",
			"dataFile",
			"messagesFile",
			"conversationToken",
			"encryptionKey"
		};
		int index = 1;
		for (ConversationMetadataItem item : items) {
			rows[index++] = new Object[] {
				item.state,
				item.localConversationId,
				item.conversationId,
				item.userId,
				item.dataFile,
				item.messagesFile,
				item.conversationToken,
				item.encryptionKey
			};
		}

		ApptentiveLog.vv(CONVERSATION, "%s (%d item(s))\n%s", title, items.size(), StringUtils.table(rows));
	}

	//endregion

	//region Getters/Setters

	public Conversation getActiveConversation() {
		// assertMainThread(); TODO: we should still only access the conversation on a dedicated queue
		// but at this time we can't do it by design
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
