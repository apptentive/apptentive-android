/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.ConversationMetadata.Filter;
import com.apptentive.android.sdk.encryption.EncryptionFactory;
import com.apptentive.android.sdk.migration.Migrator;
import com.apptentive.android.sdk.model.Configuration;
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
import com.apptentive.android.sdk.storage.DevicePayloadDiff;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.SerializerException;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Jwt;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.conversationQueue;
import static com.apptentive.android.sdk.ApptentiveLog.Level.VERBOSE;
import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.ApptentiveNotifications.*;
import static com.apptentive.android.sdk.conversation.ConversationState.*;
import static com.apptentive.android.sdk.debug.Assert.*;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.util.Constants.CONVERSATION_METADATA_FILE;
import static com.apptentive.android.sdk.util.Constants.CONVERSATION_METADATA_FILE_LEGACY_V1;
import static com.apptentive.android.sdk.util.Constants.PAYLOAD_ENCRYPTION_KEY_TRANSFORMATION;
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
	private static final String TAG_FETCH_CONVERSATION_TOKEN_REQUEST = "fetch_conversation_token";
	private static final String TAG_FETCH_APP_CONFIGURATION_REQUEST = "fetch_app_configuration";

	private final WeakReference<Context> contextRef;

	private boolean appIsInForeground;

	/**
	 * A basic directory for storing conversation-related data.
	 */
	private final File conversationsStorageDir;

	/**
	 * An encryption for securing SDK files.
	 */
	private final Encryption encryption;

	/**
	 * Responsible for generating device payloads.
	 */
	private final DeviceManager deviceManager;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	private Conversation activeConversation;
	private ConversationProxy activeConversationProxy;

	/**
	 * Indicate a failure in resolving active conversation (there was an exception while loading
	 * metadata or conversation). Used to disable conversation-related SDK functionality (like login)
	 */
	private boolean activeConversationFailedToResolve; // TODO: this is a temporary solution until we restore conversation state

	public ConversationManager(@NonNull Context context, @NonNull File conversationsStorageDir, @NonNull Encryption encryption, @NonNull DeviceManager deviceManager) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (conversationsStorageDir == null) {
			throw new IllegalArgumentException("Conversation storage dir is null");
		}

		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}

		if (deviceManager == null) {
			throw new IllegalArgumentException("Device manager is null");
		}

		this.contextRef = new WeakReference<>(context.getApplicationContext());
		this.conversationsStorageDir = conversationsStorageDir;
		this.encryption = encryption;
		this.deviceManager = deviceManager;

		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_APP_ENTERED_FOREGROUND, new ApptentiveNotificationObserver() {
				@Override
				public void onReceiveNotification(ApptentiveNotification notification) {
					checkConversationQueue();
					appIsInForeground = true;
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

		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_APP_ENTERED_BACKGROUND, new ApptentiveNotificationObserver() {
				@Override
				public void onReceiveNotification(ApptentiveNotification notification) {
					checkConversationQueue();
					appIsInForeground = false;
				}
			});
	}

	//region Conversations

	/**
	 * Attempts to load an active conversation. Returns <code>false</code> if active conversation is
	 * missing or cannot be loaded
	 */
	public boolean loadActiveConversation(Context context) {
		checkConversationQueue();

		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		try {
			// resolving metadata
			ApptentiveLog.v(CONVERSATION, "Resolving metadata...");
			conversationMetadata = resolveMetadata();
			if (ApptentiveLog.canLog(VERBOSE)) {
				printMetadata(conversationMetadata, "Loaded Metadata");
			}

			// attempt to load existing conversation
			ApptentiveLog.v(CONVERSATION, "Loading active conversation...");
			setActiveConversation(loadActiveConversationGuarded());

			if (activeConversation != null) {
				ApptentiveNotificationCenter.defaultCenter()
					.postNotification(NOTIFICATION_CONVERSATION_LOAD_DID_FINISH,
						NOTIFICATION_KEY_CONVERSATION, activeConversation,
						NOTIFICATION_KEY_SUCCESSFUL, true);

				activeConversation.startListeningForChanges();
				activeConversation.scheduleSaveConversationData();

				handleConversationStateChange(activeConversation);
				return true;
			}

		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while loading active conversation");
			logException(e);

			activeConversationFailedToResolve = true;
		}

		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_CONVERSATION_LOAD_DID_FINISH,
				NOTIFICATION_KEY_SUCCESSFUL, false);

		return false;
	}

	private @Nullable Conversation loadActiveConversationGuarded() throws ConversationLoadException {
		// try to load an active conversation from metadata first
		try {
			if (conversationMetadata.hasItems()) {
				return loadConversationFromMetadata(conversationMetadata);
			}

			// try to load legacy conversation
			Conversation legacyConversation = migrateLegacyConversation(getContext());
			if (legacyConversation != null) {
				return legacyConversation;
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading conversation");
			logException(e);
			// do not re-create a conversation if the last loading was unsuccessful
			throw new ConversationLoadException("Unable to load conversation", e);
		}

		// no active conversations: create a new one
		ApptentiveLog.i(CONVERSATION, "Creating 'anonymous' conversation...");
		File dataFile = generateConversationDataFilename();
		File messagesFile = generateMessagesFilename();
		Conversation conversation = new Conversation(dataFile, messagesFile, encryption, null);
		conversation.setState(ANONYMOUS_PENDING);
		fetchConversationToken(conversation);
		return conversation;
	}

	/**
	 * Attempts to load an existing conversation based on metadata file
	 *
	 * @return <code>null</code> is only logged out conversations available
	 */
	private @Nullable Conversation loadConversationFromMetadata(ConversationMetadata metadata) throws SerializerException, ConversationLoadException {
		// we're going to scan metadata in attempt to find existing conversations
		ConversationMetadataItem item;

		// if the user was logged in previously - we should have an active conversation
		item = metadata.findItem(LOGGED_IN);
		if (item != null) {
			ApptentiveLog.i(CONVERSATION, "Loading 'logged-in' conversation...");
			return loadConversation(item);
		}

		// if no users were logged in previously - we might have an anonymous conversation
		item = metadata.findItem(ANONYMOUS);
		if (item != null) {
			ApptentiveLog.i(CONVERSATION, "Loading 'anonymous' conversation...");
			return loadConversation(item);
		}

		// check if we have a 'pending' anonymous conversation
		item = metadata.findItem(ANONYMOUS_PENDING);
		if (item != null) {
			ApptentiveLog.i(CONVERSATION, "Loading 'anonymous pending' conversation...");
			final Conversation conversation = loadConversation(item);
			fetchConversationToken(conversation);
			return conversation;
		}

		// check if we have a 'legacy pending' conversation
		item = metadata.findItem(LEGACY_PENDING);
		if (item != null) {
			ApptentiveLog.i(CONVERSATION, "Loading 'legacy pending' conversation...");
			final Conversation conversation = loadConversation(item);
			fetchLegacyConversation(conversation);
			return conversation;
		}

		// we only have LOGGED_OUT conversations
		ApptentiveLog.i(CONVERSATION, "No active conversations to load: only 'logged-out' conversations available");
		return null;
	}

	private @Nullable Conversation migrateLegacyConversation(Context context) {
		// If there is a Legacy Conversation, migrate it into the new Conversation object.
		// Check whether migration is needed.
		// No Conversations exist in the meta-data.
		// Do we have a Legacy Conversation or not?
		final SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		String legacyConversationToken = prefs.getString(Constants.PREF_KEY_CONVERSATION_TOKEN, null);
		if (isNullOrEmpty(legacyConversationToken)) {
			return null;
		}

		ApptentiveLog.i(CONVERSATION, "Migrating an existing legacy conversation to the new format...");

		// remove the legacy data to avoid further migration
		prefs.edit()
			.remove(Constants.PREF_KEY_CONVERSATION_TOKEN)
			.remove(Constants.PREF_KEY_POLL_FOR_INTERACTIONS)
			.apply();

		String lastSeenVersionString = prefs.getString(Constants.PREF_KEY_LAST_SEEN_SDK_VERSION, null);
		Apptentive.Version version4 = new Apptentive.Version();
		version4.setVersion("4.0.0");
		Apptentive.Version lastSeenVersion = new Apptentive.Version();
		lastSeenVersion.setVersion(lastSeenVersionString);
		if (lastSeenVersionString != null && lastSeenVersion.compareTo(version4) < 0) {
			ApptentiveLog.i(CONVERSATION, "Creating 'legacy' conversation...");

			File dataFile = generateConversationDataFilename();
			File messagesFile = generateMessagesFilename();
			Conversation conversation = new Conversation(dataFile, messagesFile, encryption, null);
			conversation.setState(LEGACY_PENDING);
			conversation.setConversationToken(legacyConversationToken);

			// migrate conversation data (events, etc)
			Migrator migrator = new Migrator(context, prefs, conversation);
			migrator.migrate();

			ApptentiveLog.v(CONVERSATION, "Fetching legacy conversation...");
			fetchLegacyConversation(conversation);

			return conversation;
		}

		return null;
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
					                      checkConversationQueue();

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
						                      ApptentiveLog.e(CONVERSATION, e, "Exception while handling legacy conversation id");
						                      logException(e);
					                      }
				                      }

				                      @Override
				                      public void onCancel(HttpJsonRequest request) {
				                      }

				                      @Override
				                      public void onFail(HttpJsonRequest request, String reason) {
					                      ApptentiveLog.e(CONVERSATION, "Failed to fetch legacy conversation id: %s", reason);
				                      }
			                      });

		request.setCallbackQueue(conversationQueue()); // we only deal with conversation on a selected queue
		request.setTag(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		request.start();
		return request;
	}

	private Conversation loadConversation(ConversationMetadataItem item) throws SerializerException, ConversationLoadException {
		checkConversationQueue();

		// logged-in conversations should use an encryption key which was received from the backend.
		Encryption conversationEncryption = encryption;
		String payloadEncryptionKey = null;
		if (LOGGED_IN.equals(item.getConversationState())) {
			payloadEncryptionKey = item.getConversationEncryptionKey();
			if (payloadEncryptionKey == null) {
				throw new ConversationLoadException("Missing conversation encryption key");
			}
			conversationEncryption = createPayloadEncryption(payloadEncryptionKey);
		}

		// TODO: use same serialization logic across the project
		final Conversation conversation = new Conversation(item.getDataFile(), item.getMessagesFile(), conversationEncryption, payloadEncryptionKey);
		conversation.setState(item.getConversationState()); // set the state same as the item's state
		conversation.setUserId(item.getUserId());
		conversation.setConversationToken(item.getConversationToken()); // TODO: this would be overwritten by the next call

		// try to migrate legacy conversation first
		boolean migrated = conversation.migrateConversationData();

		// if failed - load from encrypted data
		if (!migrated) {
			conversation.loadConversationData();
		}

		// check inconsistency
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
		checkConversationQueue();

		// post notification
		notifyFetchStarted(conversation);

		// check if context is lost
		final Context context = getContext();
		if (context == null) {
			ApptentiveLog.w(CONVERSATION, "Unable to fetch conversation token: context reference is lost");
			notifyFetchFinished(conversation, false);
			return null;
		}

		// check for an existing request
		HttpRequest existingRequest = getHttpClient().findRequest(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		if (existingRequest != null) {
			ApptentiveLog.d(CONVERSATION, "Conversation already fetching");
			return existingRequest;
		}

		ApptentiveLog.d(CONVERSATION, "Started fetching conversation token...");

		// Try to fetch a new one from the server.
		ConversationTokenRequest conversationTokenRequest = new ConversationTokenRequest();

		// Send the Device and Sdk now, so they are available on the server from the start.
		final Device device = deviceManager.generateNewDevice(context);
		final Sdk sdk = SdkManager.generateCurrentSdk(context);
		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();

		conversationTokenRequest.setDevice(DevicePayloadDiff.getDiffPayload(null, device));
		conversationTokenRequest.setSdkAndAppRelease(SdkManager.getPayload(sdk), AppReleaseManager.getPayload(appRelease));

		HttpRequest request = getHttpClient()
			                      .createConversationTokenRequest(conversationTokenRequest, new HttpRequest.Listener<HttpJsonRequest>() {
				                      @Override
				                      public void onFinish(HttpJsonRequest request) {
					                      checkConversationQueue();

					                      try {
						                      JSONObject root = request.getResponseObject();
						                      String conversationToken = root.getString("token");
						                      ApptentiveLog.d(CONVERSATION, "ConversationToken: " + hideIfSanitized(conversationToken));
						                      String conversationId = root.getString("id");
						                      ApptentiveLog.d(CONVERSATION, "New Conversation id: %s", conversationId);

						                      if (isNullOrEmpty(conversationToken)) {
							                      ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'token'");
							                      notifyFetchFinished(conversation, false);
							                      return;
						                      }

						                      if (isNullOrEmpty(conversationId)) {
							                      ApptentiveLog.e(CONVERSATION, "Can't fetch conversation: missing 'id'");
							                      notifyFetchFinished(conversation, false);
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

						                      notifyFetchFinished(conversation, true);

						                      handleConversationStateChange(conversation);
					                      } catch (Exception e) {
						                      ApptentiveLog.e(CONVERSATION, e, "Exception while handling conversation token");
						                      logException(e);

						                      notifyFetchFinished(conversation, false);
					                      }
				                      }

				                      @Override
				                      public void onCancel(HttpJsonRequest request) {
					                      notifyFetchFinished(conversation, false);
				                      }

				                      @Override
				                      public void onFail(HttpJsonRequest request, String reason) {
					                      ApptentiveLog.w(CONVERSATION, "Failed to fetch conversation token: %s", reason);
					                      notifyFetchFinished(conversation, false);
				                      }
			                      });

		request.setCallbackQueue(conversationQueue()); // we only deal with conversation on a selected queue
		request.setTag(TAG_FETCH_CONVERSATION_TOKEN_REQUEST);
		request.start();
		return request;
	}

	private void notifyFetchStarted(Conversation conversation) {
		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_CONVERSATION_TOKEN_WILL_FETCH,
				NOTIFICATION_KEY_CONVERSATION, conversation);
	}

	private void notifyFetchFinished(Conversation conversation, boolean successful) {
		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_CONVERSATION_TOKEN_DID_FETCH,
				NOTIFICATION_KEY_CONVERSATION, conversation,
				NOTIFICATION_KEY_SUCCESSFUL, successful ? Boolean.TRUE : Boolean.FALSE);
	}

	//endregion

	//region Conversation fetching

	private void handleConversationStateChange(Conversation conversation) {
		ApptentiveLog.d(CONVERSATION, "Conversation state changed: %s", conversation);
		checkConversationQueue();

		assertTrue(conversation != null && !conversation.hasState(UNDEFINED));

		if (conversation != null && !conversation.hasState(UNDEFINED)) {

			ApptentiveNotificationCenter.defaultCenter()
				.postNotification(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE,
					NOTIFICATION_KEY_CONVERSATION, conversation);

			if (conversation.hasActiveState()) {
				if (appIsInForeground) {
					// ConversationManager listens to the foreground event to fetch interactions when it comes to foreground
					conversation.fetchInteractions(getContext());
					// Message Manager listens to foreground/background events itself
					conversation.getMessageManager().attemptToStartMessagePolling();
				}

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
			if (ApptentiveLog.canLog(VERBOSE)) {
				printMetadata(conversationMetadata, "Updated Metadata");
			}
		}
	}

	private void fetchAppConfiguration(Conversation conversation) {
		checkConversationQueue();
		try {
			fetchAppConfigurationGuarded(conversation);
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while fetching app configuration");
			logException(e);
		}
	}

	private void fetchAppConfigurationGuarded(final Conversation conversation) {
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

							                          ApptentiveNotificationCenter.defaultCenter()
								                          .postNotification(NOTIFICATION_CONFIGURATION_FETCH_DID_FINISH,
									                          NOTIFICATION_KEY_CONFIGURATION, config,
									                          NOTIFICATION_KEY_CONVERSATION, conversation);

						                          } catch (Exception e) {
							                          ApptentiveLog.e(CONVERSATION, e, "Exception while parsing app configuration response");
							                          logException(e);

							                          ApptentiveNotificationCenter.defaultCenter()
								                          .postNotification(NOTIFICATION_CONFIGURATION_FETCH_DID_FINISH, NOTIFICATION_KEY_CONFIGURATION, null);
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
		request.setCallbackQueue(conversationQueue());
		request.start();
	}

	private void updateMetadataItems(Conversation conversation) {
		checkConversationQueue();

		ApptentiveLog.v(CONVERSATION, "Updating metadata: %s", conversation);

		// if the conversation is 'logged-in' we should not have any other 'logged-in' items in metadata
		if (conversation.hasState(LOGGED_IN)) {
			for (ConversationMetadataItem item : conversationMetadata) {
				if (LOGGED_IN.equals(item.getConversationState())) {
					item.setConversationState(LOGGED_OUT);
				}
			}
		}

		// delete sensitive information
		for (ConversationMetadataItem item : conversationMetadata) {
			item.setConversationEncryptionKey(null);
			item.setConversationToken(null);
		}

		// update the state of the corresponding item
		ConversationMetadataItem item = conversationMetadata.findItem(conversation);
		if (item == null) {
			item = new ConversationMetadataItem(conversation.getLocalIdentifier(), conversation.getConversationId(), conversation.getConversationDataFile(), conversation.getConversationMessagesFile());
			conversationMetadata.addItem(item);
		} else {
			assertTrue(conversation.getConversationId() != null || conversation.hasState(ANONYMOUS_PENDING) || conversation.hasState(LEGACY_PENDING), "Missing conversation id for state: %s", conversation.getState());
			item.setConversationId(conversation.getConversationId());
		}

		item.setConversationState(conversation.getState());
		if (conversation.hasActiveState()) {
			item.setConversationToken(notNull(conversation.getConversationToken()));
		}

		if (conversation.hasState(LOGGED_IN)) {
			item.setConversationEncryptionKey(notNull(conversation.getPayloadEncryptionKey()));
			item.setUserId(notNull(conversation.getUserId()));
		}

		// apply changes
		saveMetadata();
	}

	//endregion

	//region Metadata

	private ConversationMetadata resolveMetadata() throws ConversationMetadataLoadException {
		checkConversationQueue();

		try {
			// attempt to load the encrypted metadata file
			File metaFile = new File(conversationsStorageDir, CONVERSATION_METADATA_FILE);
			if (metaFile.exists()) {
				ApptentiveLog.v(CONVERSATION, "Loading metadata file: %s", metaFile);
				return ObjectSerialization.deserialize(metaFile, ConversationMetadata.class, encryption);
			}

			// attempt to load the legacy metadata file
			metaFile = new File(conversationsStorageDir, CONVERSATION_METADATA_FILE_LEGACY_V1);
			if (metaFile.exists()) {
				ApptentiveLog.v(CONVERSATION, "Loading legacy v1 metadata file: %s", metaFile);
				try {
					return ObjectSerialization.deserialize(metaFile, ConversationMetadata.class);
				} finally {
					// we need to delete the legacy file to avoid the data being loaded next time
					boolean fileDeleted = metaFile.delete();
					ApptentiveLog.v(CONVERSATION, "Legacy metadata file deleted: %b", fileDeleted);
				}
			}

			ApptentiveLog.v(CONVERSATION, "No metadata files");
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while loading conversation metadata");
			logException(e);
			// if we fail to load the metadata - we would not create a new one - just throw an exception
			throw new ConversationMetadataLoadException("Unable to load metadata", e);
		}

		return new ConversationMetadata();
	}

	private void saveMetadata() {
		checkConversationQueue();

		try {
			if (ApptentiveLog.canLog(VERBOSE)) {
				ApptentiveLog.v(CONVERSATION, "Saving metadata: ", conversationMetadata.toString());
			}
			long start = System.currentTimeMillis();
			File metaFile = new File(conversationsStorageDir, CONVERSATION_METADATA_FILE);
			ObjectSerialization.serialize(metaFile, conversationMetadata, encryption);
			ApptentiveLog.v(CONVERSATION, "Saved metadata (took %d ms)", System.currentTimeMillis() - start);
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while saving metadata");
			logException(e);
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
		requestLoggedInConversation(token, callback != null ? callback : NULL_LOGIN_CALLBACK); // avoid constant null-pointer checking
	}

	private void requestLoggedInConversation(final String token, final LoginCallback callback) {
		checkConversationQueue();

		if (callback == null) {
			throw new IllegalArgumentException("Callback is null");
		}

		final String userId;
		try {
			final Jwt jwt = Jwt.decode(token);
			userId = jwt.getPayload().optString("sub");
			if (StringUtils.isNullOrEmpty(userId)) {
				ApptentiveLog.e(CONVERSATION, "Error while extracting user id: Missing field \"sub\"");
				callback.onLoginFail("Error while extracting user id: Missing field \"sub\"");
				return;
			}

		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while extracting user id");
			logException(e);

			callback.onLoginFail("Exception while extracting user id");
			return;
		}

		// Check if we have metadata
		if (activeConversationFailedToResolve) {
			ApptentiveLog.e(CONVERSATION, "Unable to login: active conversation was not loaded");
			callback.onLoginFail("Unable to login: active conversation was not loaded");
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
				ApptentiveLog.w(CONVERSATION, "No conversation found matching user: '%s'. Logging in as new user.", userId);
				sendFirstLoginRequest(userId, token, callback);
				return;
			}

			sendLoginRequest(conversationItem.getConversationId(), userId, token, callback);
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
						checkConversationQueue();
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
					ApptentiveLog.w(CONVERSATION, "Refreshing auth token for logged in user: \"%s\"", userId);
					sendLoginRequest(activeConversation.getConversationId(), userId, token, callback);
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
					final String conversationEncryptionKey = responseObject.getString("encryption_key");
					final String incomingConversationId = responseObject.getString("id");
					handleLoginFinished(incomingConversationId, userId, token, conversationEncryptionKey);
				} catch (Exception e) {
					ApptentiveLog.e(CONVERSATION, e, "Exception while parsing login response");
					logException(e);

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

			private void handleLoginFinished(final String conversationId, final String userId, final String token, final String payloadEncryptionKey) {
				checkConversationQueue();
				assertNotNull(payloadEncryptionKey, "Login finished with missing encryption key.");
				assertFalse(isNullOrEmpty(token), "Login finished with missing token.");

				Encryption conversationEncryption = createPayloadEncryption(payloadEncryptionKey);

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
							conversationItem.setConversationState(LOGGED_IN);
							conversationItem.setConversationToken(token);
							conversationItem.setConversationEncryptionKey(payloadEncryptionKey);
							setActiveConversation(loadConversation(conversationItem));
						} else {
							ApptentiveLog.v(CONVERSATION, "Creating new logged in conversation...");
							File dataFile = generateConversationDataFilename();
							File messagesFile = generateMessagesFilename();
							setActiveConversation(new Conversation(dataFile, messagesFile, conversationEncryption, payloadEncryptionKey));

							// TODO: if we don't set these here - device payload would return 4xx error code
							activeConversation.setDevice(deviceManager.generateNewDevice(getContext()));
							activeConversation.setAppRelease(ApptentiveInternal.getInstance().getAppRelease());
							activeConversation.setSdk(SdkManager.generateCurrentSdk(getContext()));
						}
					}

					activeConversation.setEncryption(conversationEncryption);
					activeConversation.setPayloadEncryptionKey(payloadEncryptionKey);
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
					ApptentiveLog.e(CONVERSATION, e, "Exception while creating logged-in conversation");
					logException(e);

					handleLoginFailed("Internal error");
				}
			}

			private void handleLoginFailed(String reason) {
				callback.onLoginFail(reason);
			}
		});
		request.setCallbackQueue(conversationQueue()); // we only deal with conversation on a selected queue
		request.start();
	}

	private void sendFirstLoginRequest(final String userId, final String token, final LoginCallback callback) {
		checkConversationQueue();

		final AppRelease appRelease = ApptentiveInternal.getInstance().getAppRelease();
		final Sdk sdk = SdkManager.generateCurrentSdk(getContext());
		final Device device = deviceManager.generateNewDevice(getContext());

		HttpJsonRequest request = getHttpClient().createFirstLoginRequest(token, appRelease, sdk, device, new HttpRequest.Listener<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				try {
					final JSONObject responseObject = request.getResponseObject();
					final String payloadEncryptionKey = responseObject.getString("encryption_key");
					final String incomingConversationId = responseObject.getString("id");
					handleLoginFinished(incomingConversationId, userId, token, payloadEncryptionKey);
				} catch (Exception e) {
					ApptentiveLog.e(CONVERSATION, e, "Exception while parsing login response");
					logException(e);

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

			private void handleLoginFinished(final String conversationId, final String userId, final String token, final String payloadEncryptionKey) {
				checkConversationQueue();
				assertNull(activeConversation, "Finished logging into new conversation, but one was already active.");
				assertNotNull(payloadEncryptionKey, "Login finished with missing encryption key.");
				assertFalse(isNullOrEmpty(token), "Login finished with missing token.");

				Encryption conversationEncryption = createPayloadEncryption(payloadEncryptionKey);

				try {
					// attempt to find previous logged out conversation
					final ConversationMetadataItem conversationItem = conversationMetadata.findItem(new Filter() {
						@Override
						public boolean accept(ConversationMetadataItem item) {
							return StringUtils.equal(item.getUserId(), userId);
						}
					});

					if (conversationItem != null) {
						conversationItem.setConversationState(LOGGED_IN);
						conversationItem.setConversationToken(token);
						conversationItem.setConversationEncryptionKey(payloadEncryptionKey);
						setActiveConversation(loadConversation(conversationItem));
					} else {
						ApptentiveLog.v(CONVERSATION, "Creating new logged in conversation...");
						File dataFile = generateConversationDataFilename();
						File messagesFile = generateMessagesFilename();
						Conversation conversation = new Conversation(dataFile, messagesFile, conversationEncryption, payloadEncryptionKey);
						setActiveConversation(conversation);

						activeConversation.setAppRelease(appRelease);
						activeConversation.setSdk(sdk);
						activeConversation.setDevice(device);
					}

					activeConversation.setEncryption(conversationEncryption);
					activeConversation.setPayloadEncryptionKey(payloadEncryptionKey);
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
					ApptentiveLog.e(CONVERSATION, e, "Exception while creating logged-in conversation");
					logException(e);

					handleLoginFailed("Internal error");
				}
			}

			private void handleLoginFailed(String reason) {
				callback.onLoginFail(reason);
			}
		});
		request.setCallbackQueue(conversationQueue()); // we only deal with conversation on a selected queue
		request.start();
	}

	public void logout() {
		checkConversationQueue();
		if (activeConversation != null) {
			switch (activeConversation.getState()) {
				case LOGGED_IN:
					ApptentiveLog.d(CONVERSATION, "Ending active conversation.");
					EngagementModule.engageInternal(getContext(), activeConversation, "logout");
					// Post synchronously to ensure logout payload can be sent before destroying the logged in conversation.
					ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_CONVERSATION_WILL_LOGOUT, ObjectUtils.toMap(NOTIFICATION_KEY_CONVERSATION, activeConversation));
					activeConversation.destroy();
					activeConversation.setState(LOGGED_OUT);
					handleConversationStateChange(activeConversation);
					setActiveConversation(null);
					ApptentiveInternal.dismissAllInteractions();
					break;
				default:
					ApptentiveLog.w(CONVERSATION, "Attempted to logout() from Conversation, but the Active Conversation was not in LOGGED_IN state.");
					break;
			}
		} else {
			ApptentiveLog.w(CONVERSATION, "Attempted to logout(), but there was no Active Conversation.");
		}

	}

	//endregion

	//region Debug

	private void printMetadata(ConversationMetadata metadata, String title) {
		List<ConversationMetadataItem> items = metadata.getItems();
		if (items.isEmpty()) {
			ApptentiveLog.v(CONVERSATION, "%s (%d item(s))", title, items.size());
			return;
		}

		Object[][] rows = new Object[1 + items.size()][];
		rows[0] = new Object[]{
			"state",
			"localId",
			"conversationId",
			"userId",
			"dataFile",
			"messagesFile",
			"conversationToken",
			"payloadEncryptionKey"
		};
		int index = 1;
		for (ConversationMetadataItem item : items) {
			rows[index++] = new Object[]{
				item.getConversationState(),
				item.getLocalConversationId(),
				item.getConversationId(),
				item.getUserId(),
				hideIfSanitized(item.getDataFile()),
				hideIfSanitized(item.getMessagesFile()),
				hideIfSanitized(item.getConversationToken()),
				hideIfSanitized(item.getConversationEncryptionKey())
			};
		}

		ApptentiveLog.v(CONVERSATION, "%s (%d item(s))\n%s", title, items.size(), StringUtils.table(rows));
	}

	//endregion

	//region Helpers

	private Encryption createPayloadEncryption(String payloadEncryptionKey) {
		return EncryptionFactory.createEncryption(payloadEncryptionKey, PAYLOAD_ENCRYPTION_KEY_TRANSFORMATION);
	}

	/**
	 * Generates a random name for a conversation data file
	 */
	private @NonNull File generateConversationDataFilename() {
		return Util.getEncryptedFilename(new File(conversationsStorageDir, "conversation-" + Util.generateRandomFilename()));
	}

	/**
	 * Generates a random name for a conversation messages file
	 */
	private @NonNull File generateMessagesFilename() {
		return Util.getEncryptedFilename(new File(conversationsStorageDir, "messages-" + Util.generateRandomFilename()));
	}

	//endregion

	//region Getters/Setters

	public @Nullable Conversation getActiveConversation() {
		checkConversationQueue(); // we should only access the conversation on a dedicated queue
		return activeConversation;
	}

	private void setActiveConversation(@Nullable Conversation conversation) {
		checkConversationQueue(); // we should only access the conversation on a dedicated queue
		this.activeConversation = conversation;
		this.activeConversationProxy = conversation != null ? new ConversationProxy(conversation) : null;
	}

	public synchronized @Nullable ConversationProxy getActiveConversationProxy() {
		return activeConversationProxy;
	}

	public ConversationMetadata getConversationMetadata() {
		checkConversationQueue(); // we should only access the conversation on a dedicated queue
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
