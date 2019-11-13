/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewActivity;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.conversation.ConversationProxy;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.OnListviewItemActionListener;
import com.apptentive.android.sdk.module.messagecenter.model.ContextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
import com.apptentive.android.sdk.module.messagecenter.model.WhoCard;
import com.apptentive.android.sdk.module.messagecenter.view.AttachmentPreviewDialog;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerViewAdapter;
import com.apptentive.android.sdk.module.messagecenter.view.holder.MessageComposerHolder;
import com.apptentive.android.sdk.util.AnimationUtil;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ApptentiveAttachmentLoader;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageGridViewAdapter;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.debug.Assert.assertMainThread;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_COMPOSER;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_CONTEXT;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_OUTGOING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.STATUS;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.WHO_CARD;
import static com.apptentive.android.sdk.util.Util.guarded;

public class MessageCenterFragment extends ApptentiveBaseFragment<MessageCenterInteraction> implements
	OnListviewItemActionListener,
	MessageManager.AfterSendMessageListener,
	MessageManager.OnNewIncomingMessagesListener,
	OnMenuItemClickListener,
	AbsListView.OnScrollListener,
	ImageGridViewAdapter.Callback {

	private MenuItem profileMenuItem;
	private boolean bShowProfileMenuItem = true;

	// keys used to save instance in the event of rotation
	private final static String LIST_TOP_INDEX = "key_list_top_index_state";
	private final static String LIST_TOP_OFFSET = "key_list_top_offset_state";
	private final static String COMPOSING_EDITTEXT_STATE = "key_edit_text_state";
	private final static String WHO_CARD_MODE = "key_who_card_mode_state";
	private final static String WHO_CARD_NAME = "key_who_card_name_state";
	private final static String WHO_CARD_EMAIL = "key_who_card_email_state";
	private final static String WHO_CARD_AVATAR_FILE = "key_who_card_avatar_state";

	private final static String DIALOG_IMAGE_PREVIEW = "imagePreviewDialog";

	private final static long DEFAULT_DELAYMILLIS = 200;

	/* Fragment.getActivity() may return null if not attached.
	 * hostingActivityRef is always set in onAttach()
	 * Keeping a cached weak reference ensures it's safe to use
	 */
	private WeakReference<Activity> hostingActivityRef;

	private View fab;

	private ArrayList<MessageCenterListItem> listItems = new ArrayList<>();
	private @Nullable MessageCenterRecyclerViewAdapter messageCenterRecyclerViewAdapter;
	private MessageCenterRecyclerView messageCenterRecyclerView;

	// Holder and view references
	private MessageComposerHolder composer;
	private @Nullable EditText composerEditText;
	private @Nullable EditText whoCardNameEditText;
	private @Nullable EditText whoCardEmailEditText;
	private Parcelable composingViewSavedState;
	/*
	 * Set to true when user launches image picker, and set to false once an image is picked
	 * This is used to track if the user tried to attach an image but abandoned the image picker
	 * without picking anything
	 */
	private boolean imagePickerStillOpen = false;
	private ArrayList<ImageItem> pendingAttachments = new ArrayList<ImageItem>();

	private boolean pendingWhoCardMode;
	private String pendingWhoCardAvatarFile;
	private @Nullable Parcelable pendingWhoCardName;
	private @Nullable Parcelable pendingWhoCardEmail;

	private boolean forceShowKeyboard;


	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsentMessagesCount = 0;

	private int listViewSavedTopIndex = -1;
	private int listViewSavedTopOffset;

	// FAB y-offset in pixels from the bottom edge
	private int fabPaddingPixels;

	protected static final int MSG_SCROLL_TO_BOTTOM = 1;
	protected static final int MSG_SCROLL_FROM_TOP = 2;
	protected static final int MSG_MESSAGE_SENT = 3;
	protected static final int MSG_START_SENDING = 4;
	protected static final int MSG_PAUSE_SENDING = 5;
	protected static final int MSG_RESUME_SENDING = 6;
	protected static final int MSG_MESSAGE_ADD_INCOMING = 7;
	protected static final int MSG_MESSAGE_ADD_WHOCARD = 8;
	protected static final int MSG_MESSAGE_ADD_COMPOSING = 9;
	protected static final int MSG_SEND_PENDING_CONTEXT_MESSAGE = 10;
	protected static final int MSG_REMOVE_COMPOSER = 11;
	protected static final int MSG_REMOVE_STATUS = 12;
	protected static final int MSG_OPT_INSERT_REGULAR_STATUS = 13;
	protected static final int MSG_MESSAGE_REMOVE_WHOCARD = 14;
	protected static final int MSG_ADD_CONTEXT_MESSAGE = 15;
	protected static final int MSG_ADD_GREETING = 16;
	protected static final int MSG_ADD_STATUS_ERROR = 17;
	protected static final int MSG_REMOVE_ATTACHMENT = 18;

	private MessageCenterFragment.MessagingActionHandler messagingActionHandler;

	public static MessageCenterFragment newInstance(Bundle bundle) {
		MessageCenterFragment mcFragment = new MessageCenterFragment();
		mcFragment.setArguments(bundle);
		return mcFragment;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Make Message Center fragment retain its instance on orientation change
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		listViewSavedTopIndex = (savedInstanceState == null) ? -1 : savedInstanceState.getInt(LIST_TOP_INDEX);
		listViewSavedTopOffset = (savedInstanceState == null) ? 0 : savedInstanceState.getInt(LIST_TOP_OFFSET);
		composingViewSavedState = (savedInstanceState == null) ? null : savedInstanceState.getParcelable(COMPOSING_EDITTEXT_STATE);
		pendingWhoCardName = (savedInstanceState == null) ? null : savedInstanceState.getParcelable(WHO_CARD_NAME);
		pendingWhoCardEmail = (savedInstanceState == null) ? null : savedInstanceState.getParcelable(WHO_CARD_EMAIL);
		pendingWhoCardAvatarFile = (savedInstanceState == null) ? null : savedInstanceState.getString(WHO_CARD_AVATAR_FILE);
		pendingWhoCardMode = savedInstanceState != null && savedInstanceState.getBoolean(WHO_CARD_MODE);
		return inflater.inflate(R.layout.apptentive_message_center, container, false);
	}

	public void onViewCreated(final View view, final Bundle onSavedInstanceState) {
		super.onViewCreated(view, onSavedInstanceState);

		// setup UI before fetching messages
		messageCenterRecyclerView = view.findViewById(R.id.message_center_recycler_view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			messageCenterRecyclerView.setNestedScrollingEnabled(true);
		}
		LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		messageCenterRecyclerView.setLayoutManager(layoutManager);

		fab = view.findViewById(R.id.composing_fab);

		fetchMessages(new FetchCallback() {
			@Override
			public void onFetchFinish(@Nullable List<MessageCenterListItem> items) {
				boolean isInitialViewCreation = (onSavedInstanceState == null);
				/* When isInitialViewCreation is false, the view is being recreated after orientation change.
				 * Because the fragment is set to be retained after orientation change, setup() will reuse the retained states
				 */
				setup(view, isInitialViewCreation, items);

				dispatchConversationTask(new ConversationDispatchTask() {
					@Override
					protected boolean execute(Conversation conversation) {
						MessageManager mgr = conversation.getMessageManager();
						// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
						mgr.addInternalOnMessagesUpdatedListener(MessageCenterFragment.this);
						// Give the MessageCenterView a callback when a message is sent.
						mgr.setAfterSendMessageListener(MessageCenterFragment.this);
						return true;
					}
				}, "set message listeners");

				// Restore listview scroll offset to where it was before rotation
				if (listViewSavedTopIndex != -1) {
					messagingActionHandler.sendMessageDelayed(messagingActionHandler.obtainMessage(MSG_SCROLL_FROM_TOP, listViewSavedTopIndex, listViewSavedTopOffset), DEFAULT_DELAYMILLIS);
				} else {
					messagingActionHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);
				}
			}
		});

		// Needed to prevent the window from being pushed up when a text input area is focused.
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	private void fetchMessages(final FetchCallback callback) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				final List<MessageCenterListItem> items = conversation.getMessageManager().getMessageCenterListItems();
				dispatchOnMainQueue(new DispatchTask() {
					@Override
					protected void execute() {
						callback.onFetchFinish(items);
					}
				});

				return true;
			}
		}, "prepare messages");
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			hostingActivityRef = new WeakReference<Activity>((Activity) context);
			messagingActionHandler = new MessagingActionHandler(this);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while attaching");
			logException(e);
		}
	}

	@Override
	public void onDetach() {
		try {
			super.onDetach();
			// messageCenterRecyclerViewAdapter holds a reference to fragment context. Need to set it to null in this and other Views to prevent a memory leak.
			messageCenterRecyclerViewAdapter = null;
			if (messageCenterRecyclerView != null) {
				messageCenterRecyclerView.setAdapter(null);
			}
			composer = null;
			composerEditText = null;
			whoCardNameEditText = null;
			whoCardEmailEditText = null;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onDetach()", getClass().getSimpleName());
			logException(e);
		}
	}

	public void onStart() {
		super.onStart();
		try {
			ConversationProxy conversation = getConversation();
			if (conversation != null) {
				conversation.setMessageCenterInForeground(true);
			}
		} catch (Exception e) {
			ApptentiveLog.e("Exception in %s.onStart()", MessageCenterFragment.class.getSimpleName());
			logException(e);
		}
	}

	public void onStop() {
		super.onStop();
		try {
			ConversationProxy conversation = getConversation();
			if (conversation != null) {
				conversation.setMessageCenterInForeground(false);
			}
		} catch (Exception e) {
			ApptentiveLog.e("Exception in %s.onStop()", MessageCenterFragment.class.getSimpleName());
			logException(e);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case Constants.REQUEST_CODE_CLOSE_COMPOSING_CONFIRMATION: {
					onCancelComposing();
					break;
				}
				case Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER: {
					if (data == null) {
						ApptentiveLog.d(MESSAGES, "no image is picked");
						return;
					}
					imagePickerStillOpen = false;
					Uri uri;
					Activity hostingActivity = hostingActivityRef.get();
					//Android SDK less than 19
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						uri = data.getData();
					} else {
						//for Android 4.4
						uri = data.getData();
						int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
						if (hostingActivity != null) {
							hostingActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
						}
					}

					engageInternal(MessageCenterInteraction.EVENT_NAME_ATTACH);

					String originalPath = Util.getRealFilePathFromUri(hostingActivity, uri);
					if (originalPath != null) {
						/* If able to retrieve file path and creation time from uri, cache file name will be generated
						 * from the md5 of file path + creation time
						 */
						long creation_time = Util.getContentCreationTime(hostingActivity, uri);
						Uri fileUri = Uri.fromFile(new File(originalPath));
						File cacheDir = Util.getDiskCacheDir(hostingActivity);
						addAttachmentsToComposer(new ImageItem(originalPath, Util.generateCacheFileFullPath(fileUri, cacheDir, creation_time), Util.getMimeTypeFromUri(hostingActivity, uri), creation_time));
					} else {
						/* If not able to get image file path due to not having READ_EXTERNAL_STORAGE permission,
						 * cache name will be generated from md5 of uri string
						 */
						File cacheDir = Util.getDiskCacheDir(hostingActivity);
						String cachedFileName = Util.generateCacheFileFullPath(uri, cacheDir, 0);
						addAttachmentsToComposer(new ImageItem(uri.toString(), cachedFileName, Util.getMimeTypeFromUri(hostingActivity, uri), 0));
					}

					break;
				}
				default:
					break;
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getMessageManager().pauseSending(MessageManager.SEND_PAUSE_REASON_ACTIVITY_PAUSE);
				return true;
			}
		}, "pause message center fragment");
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			dispatchConversationTask(new ConversationDispatchTask() {
				@Override
				protected boolean execute(Conversation conversation) {
					conversation.getMessageManager().resumeSending();
					return true;
				}
			}, "resume message center fragment");

			/* imagePickerStillOpen was set true when the picker intent was launched. If user had picked an image,
			 * it would have been set to false. Otherwise, it indicates the user tried to attach an image but
			 * abandoned the image picker without picking anything
			 */
			if (imagePickerStillOpen) {
				engageInternal(MessageCenterInteraction.EVENT_NAME_ATTACHMENT_CANCEL);
				imagePickerStillOpen = false;
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onResume()", MessageCenterFragment.class.getSimpleName());
			logException(e);
		}
	}

	protected int getMenuResourceId() {
		return R.menu.apptentive_message_center;
	}

	@Override
	protected void attachFragmentMenuListeners(Menu menu) {
		profileMenuItem = menu.findItem(R.id.profile);
		profileMenuItem.setOnMenuItemClickListener(this);
		updateMenuVisibility();
	}

	@Override
	protected void updateMenuVisibility() {
		profileMenuItem.setVisible(bShowProfileMenuItem);
		profileMenuItem.setEnabled(bShowProfileMenuItem);
	}

	private void setup(View rootView, boolean isInitialViewCreation, List<MessageCenterListItem> items) {
		boolean addedAnInteractiveCard = false;

		fab.setOnClickListener(guarded(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				forceShowKeyboard = true;
				addComposingCard();
			}
		}));

		messageCenterRecyclerViewAdapter = new MessageCenterRecyclerViewAdapter(this, this, interaction, listItems);

		if (isInitialViewCreation) {
			prepareMessages(items);

			String contextMessageBody = interaction.getContextualMessageBody();
			if (contextMessageBody != null) {
				// Clear any pending composing message to present an empty composing area
				clearPendingComposingMessage();
				messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
				messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_ADD_CONTEXT_MESSAGE, new ContextMessage(contextMessageBody)));
				// If checkAddWhoCardIfRequired returns true, it will add WhoCard, otherwise add composing card
				if (!checkAddWhoCardIfRequired()) {
					addedAnInteractiveCard = true;
					forceShowKeyboard = false;
					addComposingCard();
				}
			}

			/* Add who card with pending contents
			** Pending contents would be saved if the user was in composing Who card mode and exited through back button
			 */
			else if (pendingWhoCardName != null || pendingWhoCardEmail != null || pendingWhoCardAvatarFile != null) {
				addedAnInteractiveCard = true;
				addWhoCard(pendingWhoCardMode);
			} else if (!checkAddWhoCardIfRequired()) {
				/* If there are no items in the list, then it means that the Greeting will be added, but nothing else.
				 * In that case, show the Composer, because Message Center hasn't been opened before.
				 * If Who Card is required, show Who Card first.
				 */
				if (listItems.size() == 0) {
					addedAnInteractiveCard = true;
					addComposingCard();
				} else {
					// Finally check if status message need to be restored
					addExpectationStatusIfNeeded();
				}
			}
		} else {
			// Need to account for an input view that was added before orientation change, etc.
			if (listItems != null) {
				for (MessageCenterListItem item : listItems) {
					if (item.getListItemType() == MESSAGE_COMPOSER || item.getListItemType() == WHO_CARD) {
						addedAnInteractiveCard = true;
					}
				}
			}
		}

		messageCenterRecyclerView.setAdapter(messageCenterRecyclerViewAdapter);

		// Calculate FAB y-offset
		fabPaddingPixels = calculateFabPadding(rootView.getContext());

		if (!addedAnInteractiveCard) {
			showFab();
		}

		// Retrieve any saved attachments
		ConversationProxy conversation = getConversation();
		if (conversation != null && conversation.getMessageCenterPendingAttachments() != null) {
			String pendingAttachmentsString = conversation.getMessageCenterPendingAttachments();
			JSONArray savedAttachmentsJsonArray = null;
			try {
				savedAttachmentsJsonArray = new JSONArray(pendingAttachmentsString);
			} catch (JSONException e) {
				e.printStackTrace();
				logException(e);
			}
			if (savedAttachmentsJsonArray != null && savedAttachmentsJsonArray.length() > 0) {
				if (pendingAttachments == null) {
					pendingAttachments = new ArrayList<ImageItem>();
				} else {
					pendingAttachments.clear();
				}
				for (int i = 0; i < savedAttachmentsJsonArray.length(); i++) {
					try {
						JSONObject savedAttachmentJson = savedAttachmentsJsonArray.getJSONObject(i);
						if (savedAttachmentJson != null) {
							pendingAttachments.add(new ImageItem(savedAttachmentJson));
						}
					} catch (JSONException e) {
						logException(e);
						continue;
					}
				}
			}
			// Stored pending attachments have been restored, remove it from the persistent storage
			conversation.setMessageCenterPendingAttachments(null);
		}
		updateMessageSentStates();
	}

	public boolean onMenuItemClick(MenuItem menuItem) {
		int menuItemId = menuItem.getItemId();

		if (menuItemId == R.id.profile) {
			JSONObject data = new JSONObject();
			try {
				data.put("required", interaction.getWhoCardRequired());
				data.put("trigger", "button");
			} catch (JSONException e) {
				logException(e);
			}
			engageInternal(MessageCenterInteraction.EVENT_NAME_PROFILE_OPEN, data.toString());

			boolean whoCardDisplayedBefore = wasWhoCardAsPreviouslyDisplayed();
			forceShowKeyboard = true;
			addWhoCard(!whoCardDisplayedBefore);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		try {
			savePendingComposingMessage();
			//int index = messageCenterRecyclerView.getFirstVisiblePosition();
			View v = messageCenterRecyclerView.getChildAt(0);
			int top = (v == null) ? 0 : (v.getTop() - messageCenterRecyclerView.getPaddingTop());
			outState.putInt(LIST_TOP_OFFSET, top);
			outState.putParcelable(COMPOSING_EDITTEXT_STATE, saveEditTextInstanceState());
			if (messageCenterRecyclerViewAdapter != null) {
				outState.putParcelable(WHO_CARD_NAME, whoCardNameEditText != null ? whoCardNameEditText.onSaveInstanceState() : null);
				outState.putParcelable(WHO_CARD_EMAIL, whoCardEmailEditText != null ? whoCardEmailEditText.onSaveInstanceState() : null);
				outState.putString(WHO_CARD_AVATAR_FILE, messageCenterRecyclerViewAdapter.getWhoCardAvatarFileName());
			}
			outState.putBoolean(WHO_CARD_MODE, pendingWhoCardMode);
			super.onSaveInstanceState(outState);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onSaveInstanceState()", getClass().getSimpleName());
			logException(e);
		}
	}

	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		savePendingComposingMessage();
		ApptentiveViewActivity hostingActivity = (ApptentiveViewActivity) hostingActivityRef.get();
		if (hostingActivity != null) {
			DialogFragment myFrag = (DialogFragment) (hostingActivity.getSupportFragmentManager()).findFragmentByTag(DIALOG_IMAGE_PREVIEW);
			if (myFrag != null) {
				myFrag.dismiss();
			}
			cleanup();
			if (exitType.equals(ApptentiveViewExitType.BACK_BUTTON)) {
				engageInternal(MessageCenterInteraction.EVENT_NAME_CANCEL);
			} else if (exitType.equals(ApptentiveViewExitType.NOTIFICATION)) {
				engageInternal(MessageCenterInteraction.EVENT_NAME_CANCEL, exitTypeToDataJson(exitType));
			} else {
				engageInternal(MessageCenterInteraction.EVENT_NAME_CLOSE, exitTypeToDataJson(exitType));
			}
		}
		return false;
	}

	public boolean cleanup() {
		// Set to null, otherwise they will hold reference to the activity context
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				MessageManager mgr = conversation.getMessageManager();

				mgr.clearInternalOnMessagesUpdatedListeners();
				mgr.setAfterSendMessageListener(null);

				ApptentiveInternal.getInstance().getAndClearCustomData();
				ApptentiveAttachmentLoader.getInstance().clearMemoryCache();

				return true;
			}
		}, "clean up message center fragment");
		return true;
	}

	public void addComposingCard() {
		hideFab();
		hideProfileButton();
		messagingActionHandler.removeMessages(MSG_MESSAGE_ADD_WHOCARD);
		messagingActionHandler.removeMessages(MSG_MESSAGE_ADD_COMPOSING);
		messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
		messagingActionHandler.sendEmptyMessage(MSG_MESSAGE_ADD_COMPOSING);
		messagingActionHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
	}

	private boolean checkAddWhoCardIfRequired() {
		boolean whoCardDisplayedBefore = wasWhoCardAsPreviouslyDisplayed();
		boolean addedWhoCard = false;
		if (interaction.getWhoCardRequestEnabled() && interaction.getWhoCardRequired()) {
			if (!whoCardDisplayedBefore) {
				forceShowKeyboard = true;
				addWhoCard(true);
				addedWhoCard = true;
			} else {
				String savedEmail = Apptentive.getPersonEmail();
				if (TextUtils.isEmpty(savedEmail)) {
					forceShowKeyboard = true;
					addWhoCard(false);
					addedWhoCard = true;
				}
			}
		}
		if (addedWhoCard) {
			JSONObject data = new JSONObject();
			try {
				data.put("required", interaction.getWhoCardRequired());
				data.put("trigger", "automatic");
			} catch (JSONException e) {
				logException(e);
			}
			engageInternal(MessageCenterInteraction.EVENT_NAME_PROFILE_OPEN, data.toString());
			return true;
		}
		return false;
	}

	public void addWhoCard(boolean initial) {
		hideFab();
		hideProfileButton();
		JSONObject profile = interaction.getProfile();
		if (profile != null) {
			pendingWhoCardMode = initial;
			messagingActionHandler.removeMessages(MSG_MESSAGE_ADD_WHOCARD);
			messagingActionHandler.removeMessages(MSG_MESSAGE_ADD_COMPOSING);
			messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
			messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_MESSAGE_ADD_WHOCARD, initial ? 0 : 1, 0, profile));
		}
	}

	private void addExpectationStatusIfNeeded() {
		messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
		messagingActionHandler.sendEmptyMessage(MSG_OPT_INSERT_REGULAR_STATUS);
	}

	/**
	 * Call only from handler.
	 */
	public void displayNewIncomingMessageItem(ApptentiveMessage message) {
		messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
		// Determine where to insert the new incoming message. It will be in front of any eidting
		// area, i.e. composing, Who Card ...
		int insertIndex = listItems.size(); // If inserted onto the end, then the list will have grown by one.

		outside_loop:
		// Starting at end of list, go back up the list to find the proper place to insert the incoming message.
		for (int i = listItems.size() - 1; i > 0; i--) {
			MessageCenterListItem item = listItems.get(i);
			switch (item.getListItemType()) {
				case MESSAGE_COMPOSER:
				case MESSAGE_CONTEXT:
				case WHO_CARD:
				case STATUS:
					insertIndex--;
					break;
				default:
					// Any other type means we are past the temporary items.
					break outside_loop;
			}
		}
		listItems.add(insertIndex, message);
		messageCenterRecyclerViewAdapter.notifyItemInserted(insertIndex);

		int firstIndex = messageCenterRecyclerView.getFirstVisiblePosition();
		int lastIndex = messageCenterRecyclerView.getLastVisiblePosition();
		boolean composingAreaTakesUpVisibleArea = firstIndex <= insertIndex && insertIndex < lastIndex;
		if (composingAreaTakesUpVisibleArea) {
			View v = messageCenterRecyclerView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			updateMessageSentStates();
			// Restore the position of listview to composing view
			messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_SCROLL_FROM_TOP, insertIndex, top));
		} else {
			updateMessageSentStates();
		}
	}

	public void addAttachmentsToComposer(ImageItem... images) {
		ArrayList<ImageItem> newImages = new ArrayList<ImageItem>();
		// only add new images, and filter out duplicates
		if (images != null && images.length > 0) {
			for (ImageItem newImage : images) {
				boolean bDupFound = false;
				for (ImageItem pendingAttachment : pendingAttachments) {
					if (newImage.originalPath.equals(pendingAttachment.originalPath)) {
						bDupFound = true;
						break;
					}
				}
				if (bDupFound) {
					continue;
				} else {
					pendingAttachments.add(newImage);
					newImages.add(newImage);
				}
			}
		}
		View v = messageCenterRecyclerView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();

		if (newImages.isEmpty()) {
			return;
		}
		messageCenterRecyclerViewAdapter.addImagestoComposer(composer, newImages);
		messageCenterRecyclerViewAdapter.notifyItemChanged(listItems.size() - 1);
		int firstIndex = messageCenterRecyclerView.getFirstVisiblePosition();
		messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_SCROLL_FROM_TOP, firstIndex, top));
	}

	public void setAttachmentsInComposer(final List<ImageItem> images) {
		messageCenterRecyclerViewAdapter.addImagestoComposer(composer, images);
		// The view will resize. Scroll it into view after a short delay to ensure the view has already resized.
		messagingActionHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, 50);

	}

	public void removeImageFromComposer(final int position) {
		engageInternal(MessageCenterInteraction.EVENT_NAME_ATTACHMENT_DELETE);
		messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_REMOVE_ATTACHMENT, position, 0));
		messagingActionHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);
	}

	public void openNonImageAttachment(final ImageItem image) {
		if (image == null) {
			ApptentiveLog.d(MESSAGES, "No attachment argument.");
			return;
		}

		try {
			if (!Util.openFileAttachment(hostingActivityRef.get(), image.originalPath, image.localCachePath, image.mimeType)) {
				ApptentiveLog.d(MESSAGES, "Cannot open file attachment");
			}
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, e, "Error loading attachment");
			logException(e);
		}
	}

	public void showAttachmentDialog(final ImageItem image) {
		if (image == null) {
			ApptentiveLog.d(MESSAGES, "No attachment argument.");
			return;
		}

		try {

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_IMAGE_PREVIEW);
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the dialog.
			String conversationToken = getConversation().getConversationToken();
			AttachmentPreviewDialog dialog = AttachmentPreviewDialog.newInstance(image, conversationToken);
			dialog.show(ft, DIALOG_IMAGE_PREVIEW);

		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, e, "Error loading attachment preview.");
			logException(e);
		}
	}


	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onMessageSent(int responseCode, final ApptentiveMessage apptentiveMessage) {
		final boolean isRejectedPermanently = responseCode >= 400 && responseCode < 500;
		final boolean isSuccessful = responseCode >= 200 && responseCode < 300;

		if (isSuccessful || isRejectedPermanently || responseCode == -1) {
			messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_MESSAGE_SENT, apptentiveMessage));
		}
	}

	public synchronized void onPauseSending(int reason) {
		messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_PAUSE_SENDING, reason, 0));
	}

	public synchronized void onResumeSending() {
		messagingActionHandler.sendEmptyMessage(MSG_RESUME_SENDING);
	}

	/* Guarded */
	@Override
	public void onComposingViewCreated(MessageComposerHolder composer, final EditText composerEditText, final ApptentiveImageGridView attachments) {
		this.composer = composer;
		this.composerEditText = composerEditText;

		ConversationProxy conversation = getConversation();
		assertNotNull(conversation);
		if (conversation == null) {
			return;
		}

		// Restore composing text editing state, such as cursor position, after rotation
		if (composingViewSavedState != null) {
			if (this.composerEditText != null) {
				this.composerEditText.onRestoreInstanceState(composingViewSavedState);
			}
			composingViewSavedState = null;
			// Stored pending composing text has been restored from the saved state, so it's not needed here anymore
			conversation.setMessageCenterPendingMessage(null);
		}
		// Restore composing text
		else if (!StringUtils.isNullOrEmpty(conversation.getMessageCenterPendingMessage())) {
			String messageText = conversation.getMessageCenterPendingMessage();
			if (messageText != null && this.composerEditText != null) {
				this.composerEditText.setText(messageText);
			}
			// Stored pending composing text has been restored, remove it from the persistent storage
			conversation.setMessageCenterPendingMessage(null);
		}

		setAttachmentsInComposer(pendingAttachments);

		messageCenterRecyclerView.setPadding(0, 0, 0, 0);

		if (composerEditText != null) {
			composerEditText.requestFocus();
			if (forceShowKeyboard) {
				composerEditText.post(new Runnable() { // TODO: replace with DispatchQueue
					@Override
					public void run() {
						if (forceShowKeyboard) {
							forceShowKeyboard = false;
							Util.showSoftKeyboard(hostingActivityRef.get(), composerEditText);
						}
					}
				});
			}
		}
		hideFab();
		composer.setSendButtonState();
	}

	@Override
	public void onWhoCardViewCreated(final EditText nameEditText, final EditText emailEditText, final View viewToFocus) {
		this.whoCardNameEditText = nameEditText;
		this.whoCardEmailEditText = emailEditText;
		if (pendingWhoCardName != null) {
			nameEditText.onRestoreInstanceState(pendingWhoCardName);
			pendingWhoCardName = null;
		}
		if (pendingWhoCardEmail != null) {
			emailEditText.onRestoreInstanceState(pendingWhoCardEmail);
			pendingWhoCardEmail = null;
		}
		messageCenterRecyclerView.setPadding(0, 0, 0, 0);

		if (viewToFocus != null) {
			viewToFocus.requestFocus();
			if (forceShowKeyboard) {
				viewToFocus.post(new Runnable() { // TODO: replace with DispatchQueue
					@Override
					public void run() {
						if (forceShowKeyboard) {
							forceShowKeyboard = false;
							Util.showSoftKeyboard(hostingActivityRef.get(), viewToFocus);
						}
					}
				});
			}
		}
		hideFab();
	}

	@Override
	public void beforeComposingTextChanged(CharSequence str) {

	}

	@Override
	public void onComposingTextChanged(CharSequence str) {
	}

	@Override
	public void afterComposingTextChanged(String message) {
		composer.setSendButtonState();
	}

	@Override
	public void onCancelComposing() {
		Util.hideSoftKeyboard(hostingActivityRef.get(), getView());

		JSONObject data = new JSONObject();
		try {
			Editable content = getPendingComposingContent();
			int bodyLength = (content != null) ? content.toString().trim().length() : 0;
			data.put("body_length", bodyLength);
		} catch (JSONException e) {
			logException(e);
		}
		engageInternal(MessageCenterInteraction.EVENT_NAME_COMPOSE_CLOSE, data.toString());
		messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_REMOVE_COMPOSER));
		if (messageCenterRecyclerViewAdapter != null) {
			addExpectationStatusIfNeeded();
		}
		pendingAttachments.clear();
		composerEditText.getText().clear();
		composingViewSavedState = null;
		clearPendingComposingMessage();
		showFab();
		showProfileButton();
	}

	/* Guarded */
	@Override
	public void onFinishComposing() {
		messagingActionHandler.sendEmptyMessage(MSG_REMOVE_COMPOSER);

		Util.hideSoftKeyboard(hostingActivityRef.get(), getView());
		messagingActionHandler.sendEmptyMessage(MSG_SEND_PENDING_CONTEXT_MESSAGE);
		if (!TextUtils.isEmpty(composerEditText.getText().toString().trim()) || pendingAttachments.size() > 0) {
			CompoundMessage compoundMessage = new CompoundMessage();
			compoundMessage.setBody(composerEditText.getText().toString().trim());
			compoundMessage.setRead(true);
			compoundMessage.setCustomData(ApptentiveInternal.getInstance().getAndClearCustomData());
			compoundMessage.setAssociatedImages(new ArrayList<ImageItem>(pendingAttachments));

			ConversationProxy conversation = getConversation();
			if (conversation != null && conversation.hasActiveState()) {
				compoundMessage.setSenderId(conversation.getPerson().getId());
			}

			messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_START_SENDING, compoundMessage));
			composingViewSavedState = null;
			composerEditText.getText().clear();
			pendingAttachments.clear();
			clearPendingComposingMessage();
		}
		showFab();
		showProfileButton();
	}

	@Override
	public void onSubmitWhoCard(String buttonLabel) {
		JSONObject data = new JSONObject();
		try {
			data.put("required", interaction.getWhoCardRequired());
			data.put("button_label", buttonLabel);
		} catch (JSONException e) {
			logException(e);
		}
		engageInternal(MessageCenterInteraction.EVENT_NAME_PROFILE_SUBMIT, data.toString());

		setWhoCardAsPreviouslyDisplayed();
		cleanupWhoCard();

		if (shouldOpenComposerAfterClosingWhoCard()) {
			addComposingCard();
		} else {
			showFab();
			showProfileButton();
		}
	}

	@Override
	public void onCloseWhoCard(String buttonLabel) {
		JSONObject data = new JSONObject();
		try {
			data.put("required", interaction.getWhoCardRequired());
			data.put("button_label", buttonLabel);
		} catch (JSONException e) {
			logException(e);
		}
		engageInternal(MessageCenterInteraction.EVENT_NAME_PROFILE_CLOSE, data.toString());

		setWhoCardAsPreviouslyDisplayed();
		cleanupWhoCard();

		if (shouldOpenComposerAfterClosingWhoCard()) {
			addComposingCard();
		} else {
			showFab();
			showProfileButton();
		}
	}

	private boolean shouldOpenComposerAfterClosingWhoCard() {
		return interaction.getWhoCard().isRequire() && !recyclerViewContainsItemOfType(MESSAGE_OUTGOING);
	}

	public void cleanupWhoCard() {
		messagingActionHandler.sendEmptyMessage(MSG_MESSAGE_REMOVE_WHOCARD);
		Util.hideSoftKeyboard(hostingActivityRef.get(), getView());
		pendingWhoCardName = null;
		pendingWhoCardEmail = null;
		pendingWhoCardAvatarFile = null;
		pendingWhoCardMode = false;
		whoCardNameEditText = null;
		whoCardEmailEditText = null;
		addExpectationStatusIfNeeded();
	}

	@Override
	public void onNewMessageReceived(final CompoundMessage apptentiveMsg) {
		messagingActionHandler.sendMessage(messagingActionHandler.obtainMessage(MSG_MESSAGE_ADD_INCOMING, apptentiveMsg));
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	/* Show header elevation when listview can scroll up; flatten header when listview
	 * scrolls to the top; For pre-llolipop devices, fallback to a divider
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean bCanScrollUp;
		if (android.os.Build.VERSION.SDK_INT < 14) {
			bCanScrollUp = view.getChildCount() > 0
				&& (view.getFirstVisiblePosition() > 0 ||
				view.getChildAt(0).getTop() < view.getPaddingTop());
		} else {
			bCanScrollUp = ViewCompat.canScrollVertically(view, -1);
		}
		showToolbarElevation(bCanScrollUp);
	}

	@Override
	public void onAttachImage() {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//prior Api level 19
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				Intent chooserIntent = Intent.createChooser(intent, null);
				startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER);
			} else {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				Intent chooserIntent = Intent.createChooser(intent, null);
				startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER);
			}
			imagePickerStillOpen = true;
		} catch (Exception e) {
			imagePickerStillOpen = false;
			ApptentiveLog.w(MESSAGES, "can't launch image picker");
			logException(e);
		}
	}

	private void setWhoCardAsPreviouslyDisplayed() {
		ConversationProxy conversation = getConversation();
		if (conversation == null) {
			return;
		}
		conversation.setMessageCenterWhoCardPreviouslyDisplayed(true);
	}

	private boolean wasWhoCardAsPreviouslyDisplayed() {
		ConversationProxy conversation = getConversation();
		if (conversation == null) {
			return false;
		}
		return conversation.isMessageCenterWhoCardPreviouslyDisplayed();
	}

	// Retrieve the content from the composing area
	public Editable getPendingComposingContent() {
		return (composerEditText == null) ? null : composerEditText.getText();
	}

	/* Guarded */
	public void savePendingComposingMessage() {
		Editable content = getPendingComposingContent();
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		SharedPreferences.Editor editor = prefs.edit();

		ConversationProxy conversation = getConversation();
		assertNotNull(conversation);
		if (conversation == null) {
			return;
		}

		if (content != null) {
			conversation.setMessageCenterPendingMessage(content.toString().trim());
		} else {
			conversation.setMessageCenterPendingMessage(null);
		}
		JSONArray pendingAttachmentsJsonArray = new JSONArray();
		// Save pending attachment
		for (ImageItem pendingAttachment : pendingAttachments) {
			pendingAttachmentsJsonArray.put(pendingAttachment.toJSON());
		}

		if (pendingAttachmentsJsonArray.length() > 0) {
			conversation.setMessageCenterPendingAttachments(pendingAttachmentsJsonArray.toString());
		} else {
			conversation.setMessageCenterPendingAttachments(null);
		}
		editor.apply();
	}

	/* When no composing view is presented in the list view, calling this method
	 * will clear the pending composing message previously saved in shared preference
	 */
	public void clearPendingComposingMessage() {
		ConversationProxy conversation = getConversation();
		if (conversation != null) {
			conversation.setMessageCenterPendingMessage(null);
			conversation.setMessageCenterPendingAttachments(null);
		}
	}

	private Parcelable saveEditTextInstanceState() {
		if (composerEditText != null) {
			// Hide keyboard if the keyboard was up prior to rotation
			Util.hideSoftKeyboard(hostingActivityRef.get(), getView());
			return composerEditText.onSaveInstanceState();
		}
		return null;
	}

	Set<String> dateStampsSeen = new HashSet<String>();

	public void updateMessageSentStates() {
		dateStampsSeen.clear();
		MessageCenterUtil.CompoundMessageCommonInterface lastSent = null;
		Set<String> uniqueNonce = new HashSet<String>();
		int removedItems = 0;
		ListIterator<MessageCenterListItem> listItemIterator = listItems.listIterator();
		while (listItemIterator.hasNext()) {
			int adapterMessagePosition = listItemIterator.nextIndex() - removedItems;
			MessageCenterListItem message = listItemIterator.next();
			if (message instanceof ApptentiveMessage) {
				/* Check if there is any duplicate messages and remove if found.
				* add() of a Set returns false if the element already exists.
				 */
				if (!uniqueNonce.add(((ApptentiveMessage) message).getNonce())) {
					listItemIterator.remove();
					messageCenterRecyclerViewAdapter.notifyItemRemoved(adapterMessagePosition);
					removedItems++;
					continue;
				}
				// Update timestamps
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) message;
				Double sentOrReceivedAt = apptentiveMessage.getCreatedAt();
				String dateStamp = createDatestamp(sentOrReceivedAt);
				if (dateStamp != null) {
					if (dateStampsSeen.add(dateStamp)) {
						if (apptentiveMessage.setDatestamp(dateStamp)) {
							messageCenterRecyclerViewAdapter.notifyItemChanged(adapterMessagePosition);
						}
					} else {
						if (apptentiveMessage.clearDatestamp()) {
							messageCenterRecyclerViewAdapter.notifyItemChanged(adapterMessagePosition);
						}
					}
				}

				//Find last sent
				if (apptentiveMessage.isOutgoingMessage()) {
					if (sentOrReceivedAt != null && sentOrReceivedAt > Double.MIN_VALUE) {
						lastSent = (MessageCenterUtil.CompoundMessageCommonInterface) apptentiveMessage;
						lastSent.setLastSent(false);
					}

				}
			}
		}

		if (lastSent != null) {
			lastSent.setLastSent(true);
		}
	}

	protected String createDatestamp(Double seconds) {
		if (seconds != null && seconds > Double.MIN_VALUE) {
			Date date = new Date(Math.round(seconds * 1000));
			DateFormat mediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return mediumDateFormat.format(date);
		}
		return null;
	}

	private int calculateFabPadding(Context context) {
		Resources res = context.getResources();
		float scale = res.getDisplayMetrics().density;
		return (int) (res.getDimension(R.dimen.apptentive_message_center_bottom_padding) * scale + 0.5f);

	}

	private void showFab() {
		messageCenterRecyclerView.setPadding(0, 0, 0, fabPaddingPixels);
		// Re-enable Fab at the beginning of the animation
		if (fab.getVisibility() != View.VISIBLE) {
			fab.setEnabled(true);
			AnimationUtil.scaleFadeIn(fab);
		}
	}

	private void hideFab() {
		// Make sure Fab is not clickable during fade-out animation
		if (fab.getVisibility() != View.GONE) {
			fab.setEnabled(false);
			AnimationUtil.scaleFadeOutGone(fab);
		}
	}

	private void showProfileButton() {
		bShowProfileMenuItem = true;
		updateMenuVisibility();
	}

	private void hideProfileButton() {
		bShowProfileMenuItem = false;
		updateMenuVisibility();
	}

	/*
	 * Messages returned from the database was sorted on KEY_ID, which was generated by server
	 * with seconds resolution. If messages were received by server within a second, messages may be out of order
	 * This method uses insertion sort to re-sort the messages retrieved from the database
	 */
	private void prepareMessages(final List<MessageCenterListItem> originalItems) {
		assertMainThread();

		listItems.clear();
		unsentMessagesCount = 0;
		// Loop through each message item retrieved from database
		for (MessageCenterListItem item : originalItems) {
			if (item instanceof ApptentiveMessage) {
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) item;
				Double createdAt = apptentiveMessage.getCreatedAt();
				if (apptentiveMessage.isOutgoingMessage() && createdAt == null) {
					unsentMessagesCount++;
				}

				/*
				 * Find proper location to insert into the listItems list of the listview.
				 */
				ListIterator<MessageCenterListItem> listIterator = listItems.listIterator();
				ApptentiveMessage next = null;
				while (listIterator.hasNext()) {
					next = (ApptentiveMessage) listIterator.next();
					Double nextCreatedAt = next.getCreatedAt();
					// For unsent and dropped message, move the iterator to the end, and append there
					if (createdAt == null || createdAt <= Double.MIN_VALUE) {
						continue;
					}
					// next message has not received by server or received, but has a later created_at time
					if (nextCreatedAt == null || nextCreatedAt > createdAt) {
						break;
					}
				}

				if (next == null || next.getCreatedAt() == null || createdAt == null || next.getCreatedAt() <= createdAt ||
					createdAt <= Double.MIN_VALUE) {
					listIterator.add(item);
				} else {
					// Add in front of the message that has later created_at time
					listIterator.set(item);
					listIterator.add(next);
				}
			}
		}
		messagingActionHandler.sendEmptyMessage(MSG_ADD_GREETING);
	}

	@Override
	public void onClickAttachment(final int position, final ImageItem image) {
		if (Util.isMimeTypeImage(image.mimeType)) {
			if (TextUtils.isEmpty(image.originalPath)) {
				// "+" placeholder is clicked
				onAttachImage();
			} else {
				// an image thumbnail is clicked
				showAttachmentDialog(image);
			}
		} else {
			// a generic attachment icon is clicked
			openNonImageAttachment(image);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == Constants.REQUEST_READ_STORAGE_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				onAttachImage();
			}
		}
	}


	/*
	 * Called when attachment overlaid "selection" ui is tapped. The "selection" ui could be selection checkbox
	 * or close button
	 */
	@Override
	public void onImageSelected(int index) {
		removeImageFromComposer(index);
	}

	@Override
	public void onImageUnselected(String path) {

	}

	@Override
	public void onCameraShot(File imageFile) {

	}


	private static class MessagingActionHandler extends Handler {

		private final WeakReference messageCenterFragmentWeakReference;

		public MessagingActionHandler(MessageCenterFragment fragment) {
			messageCenterFragmentWeakReference = new WeakReference(fragment);
		}

		public void handleMessage(Message msg) {
			MessageCenterFragment fragment = (MessageCenterFragment) messageCenterFragmentWeakReference.get();
			/* Message can be delayed. If so, make sure fragment is still available and attached to activity
			 * messageCenterRecyclerViewAdapter will always be set null in onDetach(). it's a good indication if
			 * fragment is attached.
			 */
			if (fragment == null || fragment.messageCenterRecyclerViewAdapter == null) {
				return;
			}
			switch (msg.what) {
				case MSG_MESSAGE_ADD_WHOCARD: {
					// msg.arg1 is either WHO_CARD_MODE_INIT or WHO_CARD_MODE_EDIT
					boolean initial = msg.arg1 == 0;
					WhoCard whoCard = fragment.interaction.getWhoCard();
					whoCard.setInitial(initial);
					fragment.listItems.add(whoCard);
					fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);
					fragment.messageCenterRecyclerView.setSelection(fragment.listItems.size() - 1);
					break;
				}
				case MSG_MESSAGE_REMOVE_WHOCARD: {
					ListIterator<MessageCenterListItem> messageIterator = fragment.listItems.listIterator();
					while (messageIterator.hasNext()) {
						int i = messageIterator.nextIndex();
						MessageCenterListItem next = messageIterator.next();
						if (next.getListItemType() == WHO_CARD) {
							messageIterator.remove();
							fragment.messageCenterRecyclerViewAdapter.notifyItemRemoved(i);
						}
					}
					break;
				}
				case MSG_MESSAGE_ADD_COMPOSING: {
					fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_COMPOSE_OPEN);
					fragment.listItems.add(fragment.interaction.getComposer());
					fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);
					fragment.messageCenterRecyclerView.setSelection(fragment.listItems.size() - 1);
					break;
				}
				case MSG_MESSAGE_ADD_INCOMING: {
					ApptentiveMessage apptentiveMessage = (ApptentiveMessage) msg.obj;
					fragment.displayNewIncomingMessageItem(apptentiveMessage);
					break;
				}
				case MSG_SCROLL_TO_BOTTOM: {
					fragment.messageCenterRecyclerView.setSelection(fragment.listItems.size() - 1);
					fragment.messageCenterRecyclerView.scrollToPosition(fragment.listItems.size() - 1);
					break;
				}
				case MSG_SCROLL_FROM_TOP: {
					int index = msg.arg1;
					int top = msg.arg2;
					fragment.messageCenterRecyclerView.setSelectionFromTop(index, top);
					break;
				}
				case MSG_MESSAGE_SENT: {
					// below is callback handling when receiving of message is acknowledged by server through POST response
					fragment.unsentMessagesCount--;
					ApptentiveMessage apptentiveMessage = (ApptentiveMessage) msg.obj;

					for (int i = 0; i < fragment.listItems.size(); i++) {
						MessageCenterListItem message = fragment.listItems.get(i);
						if (message instanceof ApptentiveMessage) {
							String nonce = ((ApptentiveMessage) message).getNonce();
							if (nonce != null) {
								String sentNonce = apptentiveMessage.getNonce();
								if (sentNonce != null && nonce.equals(sentNonce)) {
									((ApptentiveMessage) message).setCreatedAt(apptentiveMessage.getCreatedAt());
									fragment.messageCenterRecyclerViewAdapter.notifyItemChanged(i);
									break;
								}
							}
						}
					}
					//Update timestamp display and add status message if needed
					fragment.updateMessageSentStates();
					fragment.addExpectationStatusIfNeeded();

					// Calculate the listview offset to make sure updating sent timestamp does not push the current view port
					int firstIndex = fragment.messageCenterRecyclerView.getFirstVisiblePosition();
					View v = fragment.messageCenterRecyclerView.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();

					// If Who Card is being shown while a message is sent, make sure Who Card is still in view by scrolling to bottom
					if (fragment.recyclerViewContainsItemOfType(WHO_CARD)) {
						sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);
					} else {
						sendMessageDelayed(obtainMessage(MSG_SCROLL_FROM_TOP, firstIndex, top), DEFAULT_DELAYMILLIS);
					}
					break;
				}
				case MSG_START_SENDING: {
					CompoundMessage message = (CompoundMessage) msg.obj;
					fragment.listItems.add(message);
					fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);
					fragment.unsentMessagesCount++;
					fragment.setPaused(false);

					sendMessage(message);

					// After the message is sent, show the Who Card if it has never been seen before, and the configuration specifies it should be requested.
					if (!fragment.wasWhoCardAsPreviouslyDisplayed() && fragment.interaction.getWhoCardRequestEnabled()) {
						JSONObject data = new JSONObject();
						try {
							data.put("required", fragment.interaction.getWhoCardRequired());
							data.put("trigger", "automatic");
						} catch (JSONException e) {
							logException(e);
						}
						fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_PROFILE_OPEN, data.toString());
						fragment.forceShowKeyboard = true;
						fragment.addWhoCard(true);
					}
					break;
				}
				case MSG_SEND_PENDING_CONTEXT_MESSAGE: {
					ContextMessage contextMessage = null;
					// If the list has a context message, get it, remove it from the list, and notify the RecyclerView to update.
					ListIterator<MessageCenterListItem> iterator = fragment.listItems.listIterator();
					while (iterator.hasNext()) {
						int index = iterator.nextIndex();
						MessageCenterListItem item = iterator.next();
						if (item.getListItemType() == MESSAGE_CONTEXT) {
							contextMessage = (ContextMessage) item;
							iterator.remove();
							fragment.messageCenterRecyclerViewAdapter.notifyItemRemoved(index);
							break;
						}
					}

					if (contextMessage != null) {
						// Create a CompoundMessage for sending and final display
						CompoundMessage message = new CompoundMessage();
						message.setBody(contextMessage.getBody());
						message.setAutomated(true);
						message.setRead(true);

						// Add it to the RecyclerView
						fragment.unsentMessagesCount++;
						fragment.listItems.add(message);
						fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);

						// Send it to the server
						sendMessage(message);
					}
					break;
				}
				case MSG_PAUSE_SENDING: {
					if (!fragment.isPaused()) {
						fragment.setPaused(true);
						if (fragment.unsentMessagesCount > 0) {
							int reason = msg.arg1;
							Message handlerMessage = fragment.messagingActionHandler.obtainMessage(MSG_ADD_STATUS_ERROR, reason, 0);
							fragment.messagingActionHandler.sendMessage(handlerMessage);
						}
					}
					break;
				}
				case MSG_RESUME_SENDING: {
					if (fragment.isPaused()) {
						fragment.setPaused(false);
						if (fragment.unsentMessagesCount > 0) {
							fragment.messagingActionHandler.sendEmptyMessage(MSG_REMOVE_STATUS);
						}
					}
					break;
				}
				case MSG_REMOVE_COMPOSER: {
					for (int i = 0; i < fragment.listItems.size(); i++) {
						MessageCenterListItem item = fragment.listItems.get(i);
						if (item.getListItemType() == MESSAGE_COMPOSER) {
							fragment.listItems.remove(i);
							fragment.messageCenterRecyclerViewAdapter.notifyItemRemoved(i);
						}
					}
					break;
				}
				case MSG_OPT_INSERT_REGULAR_STATUS: {
					List<MessageCenterListItem> listItems = fragment.listItems;
					// Only add status if the last item in the list is a sent message.
					if (listItems.size() > 0) {
						MessageCenterListItem lastItem = listItems.get(listItems.size() - 1);
						if (lastItem != null && lastItem.getListItemType() == MESSAGE_OUTGOING) {
							ApptentiveMessage apptentiveMessage = (ApptentiveMessage) lastItem;
							if (apptentiveMessage.isOutgoingMessage()) {
								Double createdTime = apptentiveMessage.getCreatedAt();
								if (createdTime != null && createdTime > Double.MIN_VALUE) {
									MessageCenterStatus status = fragment.interaction.getRegularStatus();
									if (status != null) {
										fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_STATUS);
										// Add expectation status message if the last is a sent
										listItems.add(status);
										fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(listItems.size() - 1);
									}
								}
							}
						}
					}
					break;
				}
				case MSG_REMOVE_STATUS: {
					List<MessageCenterListItem> listItems = fragment.listItems;
					for (int i = 0; i < listItems.size(); i++) {
						MessageCenterListItem item = listItems.get(i);
						if (item.getListItemType() == STATUS) {
							listItems.remove(i);
							fragment.messageCenterRecyclerViewAdapter.notifyItemRemoved(i);
						}
					}
					break;
				}
				case MSG_ADD_CONTEXT_MESSAGE: {
					ContextMessage contextMessage = (ContextMessage) msg.obj;
					fragment.listItems.add(contextMessage);
					fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);
					break;
				}
				case MSG_ADD_GREETING: {
					fragment.listItems.add(0, fragment.interaction.getGreeting());
					fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(0);
					break;
				}
				case MSG_ADD_STATUS_ERROR: {
					int reason = msg.arg1;
					MessageCenterStatus status = null;
					if (reason == MessageManager.SEND_PAUSE_REASON_NETWORK) {
						status = fragment.interaction.getErrorStatusNetwork();
						fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_MESSAGE_NETWORK_ERROR);
					} else if (reason == MessageManager.SEND_PAUSE_REASON_SERVER) {
						status = fragment.interaction.getErrorStatusServer();
						fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_MESSAGE_HTTP_ERROR);
					}
					if (status != null) {
						fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_STATUS);
						fragment.listItems.add(status);
						fragment.messageCenterRecyclerViewAdapter.notifyItemInserted(fragment.listItems.size() - 1);
					}
					break;
				}
				case MSG_REMOVE_ATTACHMENT: {
					int position = msg.arg1;
					fragment.pendingAttachments.remove(position);
					fragment.messageCenterRecyclerViewAdapter.removeImageFromComposer(fragment.composer, position);
					break;
				}
			}
		}

		private void sendMessage(final ApptentiveMessage message) {
			dispatchConversationTask(new ConversationDispatchTask() {
				@Override
				protected boolean execute(Conversation conversation) {
					conversation.getMessageManager().sendMessage(message);
					return true;
				}
			}, "send message");
		}
	}

	public boolean recyclerViewContainsItemOfType(int type) {
		for (MessageCenterListItem item : listItems) {
			if (item.getListItemType() == type) {
				return true;
			}
		}
		return false;
	}

	public void setPaused(boolean paused) {
		if (isPaused ^ paused) {
			// Invalidate any unsent messages, as these will have status and progress bars that need to change.
			for (int i = 0; i < listItems.size(); i++) {
				MessageCenterListItem item = listItems.get(i);
				if (item instanceof ApptentiveMessage) {
					ApptentiveMessage message = (ApptentiveMessage) item;
					if (message.isOutgoingMessage() && message.getCreatedAt() == null) {
						messageCenterRecyclerViewAdapter.notifyItemChanged(i);
					}
				}
			}
		}
		isPaused = paused;
	}

	public boolean isPaused() {
		return isPaused;
	}

	@Override
	public String getToolbarNavigationContentDescription() {
		return getContext().getString(R.string.apptentive_message_center_content_description_back_button);
	}

	private interface FetchCallback {
		void onFetchFinish(@Nullable List<MessageCenterListItem> items);
	}
}
