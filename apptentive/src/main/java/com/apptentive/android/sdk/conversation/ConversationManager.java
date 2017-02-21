package com.apptentive.android.sdk.conversation;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.serialization.ObjectSerialization;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.debug.Tester.dispatchDebugEvent;
import static com.apptentive.android.sdk.debug.TesterEvent.*;

/**
 * Class responsible for managing conversations.
 * <pre>
 *   - Saving/Loading conversations from/to files.
 *   - Switching conversations when users login/logout.
 *   - Creating default conversation.
 *   - Migrating legacy conversation data.
 * </pre>
 */
public class ConversationManager implements DataChangedListener {

	protected static final String CONVERSATION_METADATA_PATH = "conversation-v1.meta";

	private final WeakReference<Context> contextRef;

	/**
	 * Private serial dispatch queue for background operations
	 */
	private final DispatchQueue operationQueue;

	/**
	 * A basic directory for storing conversation-related data.
	 */
	private final File storageDir;

	/**
	 * Current state of conversation metadata.
	 */
	private ConversationMetadata conversationMetadata;

	private Conversation activeConversation;

	private final AtomicBoolean isConversationTokenFetchPending = new AtomicBoolean(false);

	public ConversationManager(Context context, DispatchQueue operationQueue, File storageDir) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (operationQueue == null) {
			throw new IllegalArgumentException("Operation queue is null");
		}

		this.contextRef = new WeakReference<>(context.getApplicationContext());
		this.operationQueue = operationQueue;
		this.storageDir = storageDir;
	}

	//region Conversations

	/**
	 * Attempts to load an active conversation. Returns <code>false</code> if active conversation is
	 * missing or cannnot be loaded
	 */
	public boolean loadActiveConversation() {
		try {
			// resolving metadata
			conversationMetadata = resolveMetadata();

			// attempt to load existing conversation
			activeConversation = loadActiveConversationGuarded();
			if (activeConversation != null) {
				activeConversation.setDataChangedListener(this);

				boolean featureEverUsed = activeConversation.isMessageCenterFeatureUsed();
				if (featureEverUsed) {
					// messageManager.init(); // // FIXME: 2/21/17 init message messenger
				}

				return true;
			}

			// no conversation - fetch one
			fetchConversationToken();

		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading active conversation");
		}

		return false;
	}

	private Conversation loadActiveConversationGuarded() throws IOException {
		// if the user was logged in previously - we should have an active conversation
		ApptentiveLog.v(CONVERSATION, "Loading active conversation...");
		final ConversationMetadataItem activeItem = conversationMetadata.findItem(new ConversationMetadata.Filter() {
			@Override
			public boolean accept(ConversationMetadataItem item) {
				return item.isActive();
			}
		});
		if (activeItem != null) {
			return loadConversation(activeItem);
		}

		// if no user was logged in previously - we might have a default conversation
		ApptentiveLog.v(CONVERSATION, "Loading default conversation...");
		final ConversationMetadataItem defaultItem = conversationMetadata.findItem(new ConversationMetadata.Filter() {
			@Override
			public boolean accept(ConversationMetadataItem item) {
				return item.isDefault();
			}
		});
		if (defaultItem != null) {
			return loadConversation(defaultItem);
		}

		// TODO: check for legacy conversations
		ApptentiveLog.v(CONVERSATION, "Can't load conversation");
		return null;
	}

	private Conversation loadConversation(ConversationMetadataItem item) {
		// TODO: use same serialization logic across the project
		File file = new File(item.filename);
		FileSerializer serializer = new FileSerializer(file);
		return (Conversation) serializer.deserialize();
	}

	//endregion

	//region Conversation fetching

	private void fetchConversationToken() {
		if (isConversationTokenFetchPending.compareAndSet(false, true)) {
			ApptentiveLog.i(CONVERSATION, "Fetching Configuration token task started.");
			dispatchDebugEvent(EVT_FETCH_CONVERSATION_TOKEN);

			final Context context = getContext();
			if (context == null) {
				ApptentiveLog.w(CONVERSATION, "Unable to fetch convesation token: context reference is lost");
				isConversationTokenFetchPending.set(false);
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

						// create new conversation
						Conversation conversation = new Conversation();
						if (conversationToken != null && !conversationToken.equals("")) { // FIXME: handle "unhappy" path
							conversation.setConversationToken(conversationToken);
							conversation.setConversationId(conversationId);
							conversation.setDevice(device);
							conversation.setSdk(sdk);
							conversation.setAppRelease(appRelease);
						}
						String personId = root.getString("person_id");
						ApptentiveLog.d(CONVERSATION, "PersonId: " + personId);
						conversation.setPersonId(personId);
						conversation.setDataChangedListener(ConversationManager.this);

						// write conversation to the dist
						saveConversation(conversation);

						// update active conversation
						setActiveConversation(conversation);

						// fetch interactions
						conversation.fetchInteractions();

					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling conversation token");
					} finally {
						isConversationTokenFetchPending.set(false);
					}
				}

				@Override
				public void onCancel(HttpJsonRequest request) {
					isConversationTokenFetchPending.set(false);
				}

				@Override
				public void onFail(HttpJsonRequest request, String reason) {
					ApptentiveLog.w("Failed to fetch conversation token: %s", reason);
					isConversationTokenFetchPending.set(false);
				}
			});
		}
	}

	private synchronized void setActiveConversation(Conversation conversation) {
		activeConversation = conversation;
		throw new RuntimeException("Implement me: update metadata");
	}

	//endregion

	//region Serialization

	private void scheduleConversationSave() {
		boolean scheduled = operationQueue.dispatchAsyncOnce(saveSessionTask, 100L);
		if (scheduled) {
			ApptentiveLog.d(CONVERSATION, "Scheduling conversation save.");
		} else {
			ApptentiveLog.d(CONVERSATION, "Conversation save already scheduled.");
		}
	}

	private final DispatchTask saveSessionTask = new DispatchTask() {
		@Override
		protected void execute() {
			if (activeConversation != null) {
				saveConversation(activeConversation);
			} else {
				ApptentiveLog.w(CONVERSATION, "Can't save conversation: active conversation is missing");
			}
		}
	};

	private void saveConversation(Conversation conversation) {
		ApptentiveLog.d(CONVERSATION, "Saving Conversation");
		ApptentiveLog.v(CONVERSATION, "EventData: %s", conversation.getEventData().toString()); // TODO: remove

		File conversationFile = new File(storageDir, conversation.getFilename());
		FileSerializer serializer = new FileSerializer(conversationFile);
		serializer.serialize(conversation);
	}

	//endregion

	//region Metadata

	private ConversationMetadata resolveMetadata() {
		try {
			File metaFile = new File(storageDir, CONVERSATION_METADATA_PATH);
			if (metaFile.exists()) {
				ApptentiveLog.v(CONVERSATION, "Loading meta file: " + metaFile);
				return ObjectSerialization.deserialize(metaFile, ConversationMetadata.class);
			} else {
				ApptentiveLog.v(CONVERSATION, "Meta file does not exist: " + metaFile);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while loading conversation metadata");
		}

		return new ConversationMetadata();
	}

	//endregion

	//region DataChangedListener

	@Override
	public void onDataChanged() {
		scheduleConversationSave();
	}

	//endregion

	//region Getters/Setters

	public Conversation getActiveConversation() {
		return activeConversation;
	}

	private Context getContext() {
		return contextRef.get();
	}

	//endregion
}
