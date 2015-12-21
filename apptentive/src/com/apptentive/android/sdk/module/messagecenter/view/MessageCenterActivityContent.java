/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveInternalActivity;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;

import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.InteractionView;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.MessagePollingWorker;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.AnimationUtil;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.WeakReferenceHandler;
import com.apptentive.android.sdk.util.image.ApptentiveAttachmentLoader;
import com.apptentive.android.sdk.util.image.ImageGridViewAdapter;
import com.apptentive.android.sdk.util.image.ImageItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Barry Li
 */

public class MessageCenterActivityContent extends InteractionView<MessageCenterInteraction>
		implements MessageManager.AfterSendMessageListener,
		MessageAdapter.OnListviewItemActionListener,
		MessageManager.OnNewIncomingMessagesListener,
		AbsListView.OnScrollListener,
		MessageCenterListView.OnListviewResizeListener,
		ImageGridViewAdapter.Callback {

	// keys used to save instance in the event of rotation
	private final static String LIST_TOP_INDEX = "list_top_index";
	private final static String LIST_TOP_OFFSET = "list_top_offset";
	private final static String COMPOSING_EDITTEXT_STATE = "edittext";
	private final static String COMPOSING_ATTACHMENTS = "attachments";
	private final static String WHO_CARD_MODE = "whocardmode";
	private final static String WHO_CARD_NAME = "whocardname";
	private final static String WHO_CARD_EMAIL = "whocardemail";
	private final static String WHO_CARD_AVATAR_FILE = "whocardavatar";

	private final static int WHO_CARD_MODE_INIT = 1;
	private final static int WHO_CARD_MODE_EDIT = 2;

	private final static long DEFAULT_DELAYMILLIS = 200;

	private Activity viewActivity;
	private View messageCenterHeader;
	private View headerDivider;
	private ListView messageCenterListView; // List of apptentive messages
	private EditText messageEditText; // Composing area
	private View fab;
	private View profileButton;

	// Data backing of the listview
	private ArrayList<MessageCenterListItem> messages = new ArrayList<MessageCenterListItem>();
	private MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsendMessagesCount = 0;


	private MessageCenterStatus statusItem;
	private MessageCenterComposingItem composingItem;
	private MessageCenterComposingItem actionBarItem;
	private MessageCenterComposingItem whoCardItem;
	private CompoundMessage contextualMessage;

	private ArrayList<ImageItem> imageAttachmentstList = new ArrayList<ImageItem>();

	/**
	 * Used to save the state of the message text box if the user closes Message Center for a moment,
	 * , rotate device, attaches a file, etc.
	 */
	private Parcelable composingViewSavedState;
	private ArrayList<ImageItem> savedAttachmentstList;

	/*
	 * Set to true when user launches image picker, and set to false once an image is picked
	 * This is used to track if the user tried to attach an image but abandoned the image picker
	 * without picking anything
	 */
	private boolean imagePickerLaunched = false;

	/**
	 * Used to save the state of the who card if the user closes Message Center for a moment,
	 * , rotate device, attaches a file, etc.
	 */
	private int pendingWhoCardMode;
	private Parcelable pendingWhoCardName;
	private Parcelable pendingWhoCardEmail;
	private String pendingWhoCardAvatarFile;


	private int listViewSavedTopIndex = -1;
	private int listViewSavedTopOffset;


	protected static final int MSG_SCROLL_TO_BOTTOM = 1;
	protected static final int MSG_SCROLL_FROM_TOP = 2;
	protected static final int MSG_MESSAGE_SENT = 3;
	protected static final int MSG_START_SENDING = 4;
	protected static final int MSG_PAUSE_SENDING = 5;
	protected static final int MSG_RESUME_SENDING = 6;
	protected static final int MSG_MESSAGE_ADD_INCOMING = 7;
	protected static final int MSG_MESSAGE_NOTIFY_UPDATE = 8;


	private final Handler.Callback messageCenterViewCallback = new Handler.Callback() {

		public boolean handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_MESSAGE_NOTIFY_UPDATE: {
					messageCenterListAdapter.notifyDataSetChanged();
					break;
				}
				case MSG_SCROLL_TO_BOTTOM: {
					messageCenterListAdapter.notifyDataSetChanged();
					messageCenterListView.setSelection(messages.size() - 1);
					break;
				}
				case MSG_SCROLL_FROM_TOP: {
					int index = msg.arg1;
					int top = msg.arg2;
					messageCenterListAdapter.notifyDataSetChanged();
					messageCenterListView.setSelectionFromTop(index, top);
					break;
				}
				case MSG_MESSAGE_SENT: {
					unsendMessagesCount--;
					ApptentiveMessage apptentiveMessage = (ApptentiveMessage) msg.obj;
					for (MessageCenterListItem message : messages) {
						if (message instanceof ApptentiveMessage) {
							String nonce = ((ApptentiveMessage) message).getNonce();
							if (nonce != null) {
								String sentNonce = apptentiveMessage.getNonce();
								if (sentNonce != null && nonce.equals(sentNonce)) {
									((ApptentiveMessage) message).setCreatedAt(apptentiveMessage.getCreatedAt());
									break;
								}
							}
						}
					}
					updateMessageSentStates();
					addExpectationStatusIfNeeded();

					// Update the sent message, make sure it stays in view
					int firstIndex = messageCenterListView.getFirstVisiblePosition();
					View v = messageCenterListView.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();
					updateMessageSentStates();
					messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
							firstIndex, top));
					break;
				}
				case MSG_START_SENDING: {
					Bundle b = msg.getData();
					CompoundMessage message = new CompoundMessage();
					message.setBody(b.getString(COMPOSING_EDITTEXT_STATE));
					message.setRead(true);
					message.setCustomData(ApptentiveInternal.getAndClearCustomData());
					ArrayList<ImageItem> imagesToAttach = b.getParcelableArrayList(COMPOSING_ATTACHMENTS);
					message.setAssociatedImages(viewActivity, imagesToAttach);
					MessageManager.sendMessage(viewActivity.getApplicationContext(), message);
					// Add new outgoing message with animation
					addNewOutGoingMessageItem(message);
					messageCenterListAdapter.notifyDataSetChanged();
					// After the message is sent, check if Who Card need to be shown for the 1st time
					SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
					boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);
					if (!bWhoCardSet) {
						JSONObject data = new JSONObject();
						try {
							data.put("required", interaction.getWhoCardRequired());
							data.put("trigger", "automatic");
						} catch (JSONException e) {
							//
						}
						EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_PROFILE_OPEN, data.toString());
						addWhoCard(WHO_CARD_MODE_INIT);
						messageCenterListAdapter.setForceShowKeyboard(true);
						// The delay is to ensure the animation of adding Who Card play after the animation of new outgoing message
						messageCenterViewHandler.sendEmptyMessageDelayed(MSG_MESSAGE_NOTIFY_UPDATE, DEFAULT_DELAYMILLIS);
					}
					break;
				}
				case MSG_PAUSE_SENDING: {
					if (!isPaused) {
						isPaused = true;
						if (unsendMessagesCount > 0) {
							messageCenterListAdapter.setPaused(isPaused);
							int reason = msg.arg1;
							if (reason == MessageManager.SEND_PAUSE_REASON_NETWORK) {
								EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_MESSAGE_NETWORK_ERROR);
								MessageCenterStatus newItem = interaction.getErrorStatusNetwork();
								addNewStatusItem(newItem);
							} else if (reason == MessageManager.SEND_PAUSE_REASON_SERVER) {
								EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_MESSAGE_HTTP_ERROR);
								MessageCenterStatus newItem = interaction.getErrorStatusServer();
								addNewStatusItem(newItem);
							}
							messageCenterListAdapter.notifyDataSetChanged();
						}
					}
					break;
				}
				case MSG_RESUME_SENDING: {
					if (isPaused) {
						isPaused = false;
						if (unsendMessagesCount > 0) {
							clearStatus();
						}

						messageCenterListAdapter.setPaused(isPaused);
						messageCenterListAdapter.notifyDataSetChanged();
					}
					break;
				}
				case MSG_MESSAGE_ADD_INCOMING: {
					ApptentiveMessage apptentiveMessage = (ApptentiveMessage) msg.obj;
					displayNewIncomingMessageItem(apptentiveMessage);
					break;
				}
				default:
					return false;
			}
			return true;
		}
	};

	final Handler messageCenterViewHandler = WeakReferenceHandler.create(messageCenterViewCallback);

	public MessageCenterActivityContent(MessageCenterInteraction interaction) {
		super(interaction);
	}

	@Override
	public void doOnCreate(Activity activity, Bundle onSavedInstanceState) {
		activity.setContentView(R.layout.apptentive_message_center);
		viewActivity = activity;

		listViewSavedTopIndex = (onSavedInstanceState == null) ? -1 :
				onSavedInstanceState.getInt(LIST_TOP_INDEX);
		listViewSavedTopOffset = (onSavedInstanceState == null) ? 0 :
				onSavedInstanceState.getInt(LIST_TOP_OFFSET);
		composingViewSavedState = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getParcelable(COMPOSING_EDITTEXT_STATE);
		pendingWhoCardName = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getParcelable(WHO_CARD_NAME);
		pendingWhoCardEmail = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getParcelable(WHO_CARD_EMAIL);
		pendingWhoCardAvatarFile = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_AVATAR_FILE);
		pendingWhoCardMode = (onSavedInstanceState == null) ? 0 :
				onSavedInstanceState.getInt(WHO_CARD_MODE);
		String contextualMessageBody = interaction.getContextualMessageBody();
		contextualMessage = CompoundMessage.createAutoMessage(null, contextualMessageBody);

		setup();

		// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
		MessageManager.addInternalOnMessagesUpdatedListener(this);
		// Give the MessageCenterView a callback when a message is sent.
		MessageManager.setAfterSendMessageListener(this);
		// Needed to prevent the window from being pushed up when a text input area is focused.
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED |
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		if (listViewSavedTopIndex == -1) {
			messageCenterViewHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);
		}
	}

	protected void setup() {

		ImageButton closeButton = (ImageButton) viewActivity.findViewById(R.id.close_mc);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanup();
				EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_CLOSE);
				viewActivity.finish();
			}
		});

		TextView titleTextView = (TextView) viewActivity.findViewById(R.id.title);
		String titleText = interaction.getTitle();
		if (titleText != null) {
			titleTextView.setText(titleText);
		}

		messageCenterHeader = viewActivity.findViewById(R.id.header_bar);
		headerDivider = viewActivity.findViewById(R.id.header_divider);
		int defaultColor = Util.getThemeColor(viewActivity, R.attr.colorPrimary);
		int brightColor = Util.lighter(defaultColor, 0.5f);
		headerDivider.setBackgroundColor(brightColor);
		messageCenterListView = (ListView) viewActivity.findViewById(R.id.message_list);
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		messageCenterListView.setOnScrollListener(this);
		((MessageCenterListView) messageCenterListView).setOnListViewResizeListener(this);
		messageCenterListView.setItemsCanFocus(true);
		final SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean showKeyboard = true;

		fab = viewActivity.findViewById(R.id.composing_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addComposingArea();
				messageCenterListAdapter.setForceShowKeyboard(true);
				messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
			}
		});

		profileButton = viewActivity.findViewById(R.id.profile);
		if (profileButton != null) {
			profileButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// Only allow profile editing when not already editing profile or in message composing
					if (whoCardItem == null && composingItem == null) {
						hideProfileButton();
						boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);

						JSONObject data = new JSONObject();
						try {
							data.put("required", interaction.getWhoCardRequired());
							data.put("trigger", "button");
						} catch (JSONException e) {
							//
						}
						EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_PROFILE_OPEN, data.toString());

						if (!bWhoCardSet) {
							addWhoCard(WHO_CARD_MODE_INIT);
						} else {
							addWhoCard(WHO_CARD_MODE_EDIT);
						}
						messageCenterListAdapter.setForceShowKeyboard(true);
						messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
					}
				}
			});
		}

		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(viewActivity);
			prepareMessages(items);

			if (contextualMessage != null) {
				addContextualMessage();
				showKeyboard = false;
			}
			/* Add composing
			** if the user was in composing mode before roatation
			 */
			else if (composingViewSavedState != null) {
				addComposingArea();
			}
			/* Add who card
			** if the user was in composing Who card mode before roatation
			 */
			else if (pendingWhoCardName != null || pendingWhoCardEmail != null || pendingWhoCardAvatarFile != null) {
				addWhoCard(pendingWhoCardMode);
			} else if (!checkAddWhoCardIfRequired()) {
				/* If there is only greeting message, show composing.
				 * If Who Card is required, show Who Card first
				 */
				if (messages.size() == 1) {
					addComposingArea();
					showKeyboard = false;
				} else {
					// Finally check if status message need to be restored
					addExpectationStatusIfNeeded();
				}
			} else {
				// Hide keyboard when Who Card is required and the 1st thing to show
				showKeyboard = false;
			}

			updateMessageSentStates(); // Force timestamp recompilation.
			messageCenterListAdapter = new MessageAdapter<MessageCenterListItem>(viewActivity, messages, this, interaction);
			messageCenterListAdapter.setForceShowKeyboard(showKeyboard);
			messageCenterListView.setAdapter(messageCenterListAdapter);
			if (listViewSavedTopIndex != -1) {
				messageCenterListView.setSelectionFromTop(listViewSavedTopIndex, listViewSavedTopOffset);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		int index = messageCenterListView.getFirstVisiblePosition();
		View v = messageCenterListView.getChildAt(0);
		int top = (v == null) ? 0 : (v.getTop() - messageCenterListView.getPaddingTop());
		outState.putInt(LIST_TOP_INDEX, index);
		outState.putInt(LIST_TOP_OFFSET, top);
		outState.putParcelable(COMPOSING_EDITTEXT_STATE, saveEditTextInstanceState());
		outState.putParcelable(WHO_CARD_NAME, messageCenterListAdapter.getWhoCardNameState());
		outState.putParcelable(WHO_CARD_EMAIL, messageCenterListAdapter.getWhoCardEmailState());
		outState.putString(WHO_CARD_AVATAR_FILE, messageCenterListAdapter.getWhoCardAvatarFileName());
		outState.putInt(WHO_CARD_MODE, pendingWhoCardMode);
		if (contextualMessage == null) {
			interaction.clearContextualMessage();
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		DialogFragment myFrag = (DialogFragment) (((ApptentiveInternalActivity) viewActivity).getSupportFragmentManager()).findFragmentByTag("preview_dialog");
		if (myFrag != null) {
			myFrag.dismiss();
		}
		cleanup();
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_CANCEL);
		return true;

	}

	public boolean cleanup() {
		savePendingComposingMessage();
		clearPendingMessageCenterPushNotification();
		clearComposingUi(null, null, 0);
		clearWhoCardUi(null, null, 0);
		// Set to null, otherwise they will hold reference to the activity context
		MessageManager.clearInternalOnMessagesUpdatedListeners();
		MessageManager.setAfterSendMessageListener(null);
		ApptentiveInternal.getAndClearCustomData();
		ApptentiveAttachmentLoader.getInstance().clearMemoryCache();
		return true;
	}

	public void onStart() {
		MessagePollingWorker.setMessageCenterInForeground(true);
	}

	public void onStop() {
		clearPendingMessageCenterPushNotification();
		MessagePollingWorker.setMessageCenterInForeground(false);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER: {
					if (data == null) {
						Log.d("no image is picked");
						return;
					}
					imagePickerLaunched = false;
					Uri uri;
					//Android SDK less than 19
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
						uri = data.getData();
					} else {
						//for Android 4.4
						uri = data.getData();
						int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
						viewActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
					}

					String originalPath = Util.getRealFilePathFromUri(viewActivity, uri);
					if (originalPath != null) {
						/* If able to retrieve file path and creation time from uri, cache file name will be generated
						 * from the md5 of file path + creation time
						 */
						long creation_time = Util.getContentCreationTime(viewActivity, uri);
						Uri fileUri = Uri.fromFile(new File(originalPath));
						File cacheDir = Util.getDiskCacheDir(viewActivity);
						addAttachmentsToComposer(Arrays.asList(new ImageItem(originalPath, Util.generateCacheFileFullPath(fileUri, cacheDir, creation_time),
								Util.getMimeTypeFromUri(viewActivity, uri), creation_time)));
					} else {
						/* If not able to get image file path due to not having READ_EXTERNAL_STORAGE permission,
						 * cache name will be generated from md5 of uri string
						 */
						File cacheDir = Util.getDiskCacheDir(viewActivity);
						String cachedFileName = Util.generateCacheFileFullPath(uri, cacheDir, 0);
						addAttachmentsToComposer(Arrays.asList(new ImageItem(uri.toString(), cachedFileName, Util.getMimeTypeFromUri(viewActivity, uri), 0)));
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
		MessageManager.onPauseSending(0);
	}

	@Override
	public void onResume() {
		MessageManager.onResumeSending();
		/* imagePickerLaunched was set true when the picker intent was launched. If user had picked an image,
		 * it woud have been set to false. Otherwise, it indicates the user tried to attach an image but
		 * abandoned the image picker without picking anything
		 */
		if (imagePickerLaunched) {
			EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_ATTACHMENT_CANCEL);
			imagePickerLaunched = false;
		}
	}

	private void clearPendingMessageCenterPushNotification() {
		SharedPreferences prefs = viewActivity.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String pushData = prefs.getString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, null);
		if (pushData != null) {
			try {
				JSONObject pushJson = new JSONObject(pushData);
				ApptentiveInternal.PushAction action = ApptentiveInternal.PushAction.unknown;
				if (pushJson.has(ApptentiveInternal.PUSH_ACTION)) {
					action = ApptentiveInternal.PushAction.parse(pushJson.getString(ApptentiveInternal.PUSH_ACTION));
				}
				switch (action) {
					case pmc:
						Log.i("Clearing pending Message Center push notification.");
						prefs.edit().remove(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION).commit();
						break;
				}
			} catch (JSONException e) {
				Log.w("Error parsing JSON from push notification.", e);
				MetricModule.sendError(viewActivity.getApplicationContext(), e, "Parsing Push notification", pushData);
			}
		}
	}

	public void addContextualMessage() {
		// Clear any pending composing message to present an empty composing area
		clearPendingComposingMessage();
		clearStatus();
		messages.add(contextualMessage);
		// If checkAddWhoCardIfRequired returns true, it will add WhoCard
		if (!checkAddWhoCardIfRequired()) {
			addComposingArea();
		}
	}

	public void addComposingArea() {
		hideFab();
		hideProfileButton();
		clearStatus();
		actionBarItem = interaction.getComposerBar();
		messages.add(actionBarItem);
		composingItem = interaction.getComposerArea();
		messages.add(composingItem);
	}

	private boolean checkAddWhoCardIfRequired() {
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);
		if (interaction.getWhoCardRequestEnabled() && interaction.getWhoCardRequired()) {
			if (!bWhoCardSet) {
				addWhoCard(WHO_CARD_MODE_INIT);
				return true;
			} else {
				String savedEmail = Apptentive.getPersonEmail(viewActivity);
				if (TextUtils.isEmpty(savedEmail)) {
					addWhoCard(WHO_CARD_MODE_EDIT);
					return true;
				}
			}
		}
		return false;
	}

	public void addWhoCard(int mode) {
		hideProfileButton();
		if (!interaction.getWhoCardRequestEnabled()) {
			return;
		}
		pendingWhoCardMode = mode;
		hideFab();
		clearStatus();
		whoCardItem = (mode == WHO_CARD_MODE_INIT) ? interaction.getWhoCardInit()
				: interaction.getWhoCardEdit();
		messages.add(whoCardItem);
	}

	private boolean addExpectationStatusIfNeeded() {
		ApptentiveMessage apptentiveMessage = null;
		MessageCenterListItem message = messages.get(messages.size() - 1);

		if (message != null && message instanceof ApptentiveMessage) {
			apptentiveMessage = (ApptentiveMessage) message;
		}
		// Check if the last message in the view is a sent message
		if (apptentiveMessage != null &&
				(apptentiveMessage.isOutgoingMessage())) {
			Double createdTime = apptentiveMessage.getCreatedAt();
			if (createdTime != null && createdTime > Double.MIN_VALUE) {
				MessageCenterStatus newItem = interaction.getRegularStatus();
				if (newItem != null && whoCardItem == null && composingItem == null) {
					// Add expectation status message if the last is a sent
					clearStatus();
					statusItem = newItem;
					messages.add(newItem);
					return true;
				}
			}
		}
		return false;
	}

	public void addNewStatusItem(MessageCenterListItem item) {
		clearStatus();

		if (composingItem != null) {
			return;
		}

		statusItem = (MessageCenterStatus) item;
		messages.add(item);
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_STATUS);

	}

	public void addNewOutGoingMessageItem(ApptentiveMessage message) {
		clearStatus();

		messages.add(message);
		unsendMessagesCount++;

		isPaused = false;
		messageCenterListAdapter.setPaused(isPaused);
	}

	public void displayNewIncomingMessageItem(ApptentiveMessage message) {
		clearStatus();

		// Determine where to insert the new incoming message. It will be in front of any eidting
		// area, i.e. composing, Who Card ...
		int insertIndex = messages.size();
		if (composingItem != null) {
			// when in composing mode, there are composing action bar and composing area
			insertIndex -= 2;
			if (contextualMessage != null) {
				insertIndex--;
			}
		} else if (whoCardItem != null) {
			insertIndex -= 1;
			if (contextualMessage != null) {
				insertIndex--;
			}
		}
		messages.add(insertIndex, message);

		int firstIndex = messageCenterListView.getFirstVisiblePosition();
		int lastIndex = messageCenterListView.getLastVisiblePosition();
		boolean composingAreaTakesUpVisibleArea = firstIndex <= insertIndex && insertIndex < lastIndex;
		if (composingAreaTakesUpVisibleArea) {
			View v = messageCenterListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			updateMessageSentStates();
			// Restore the position of listview to composing view
			messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
					insertIndex, top));
		} else {
			updateMessageSentStates();
			messageCenterListAdapter.notifyDataSetChanged();
		}

	}

	private void clearStatus() {
		// Remove the status message whenever a new incoming message is added
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}
	}

	public void addAttachmentsToComposer(final List<ImageItem> images) {
		int numberOfExistingAttachments = imageAttachmentstList.size();
		ArrayList<ImageItem> uniqueImages = new ArrayList<ImageItem>();
		// only add new images, and filter out duplicates
		if (images != null && images.size() > 0) {
			for (ImageItem newImage : images) {
				boolean bDupFound = false;
				for (ImageItem existingImage : imageAttachmentstList) {
					if (newImage.originalPath.equals(existingImage.originalPath)) {
						bDupFound = true;
						break;
					}
				}
				if (bDupFound) {
					continue;
				} else {
					uniqueImages.add(newImage);
				}
			}
		}
		if (uniqueImages.size() == 0) {
			return;
		}
		imageAttachmentstList.addAll(uniqueImages);
		// New attachments are added to a composer with no prior attachment
		if (imageAttachmentstList.size() > 0 && numberOfExistingAttachments == 0) {
			EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_ATTACHMENT_LIST_SHOWN);
		}
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_ATTACHMENT_ADD);

		View v = messageCenterListView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		// Only update composing view if image is attached successfully
		messageCenterListAdapter.addImagestoComposer(uniqueImages);
		messageCenterListAdapter.setForceShowKeyboard(false);
		int firstIndex = messageCenterListView.getFirstVisiblePosition();

		messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
				firstIndex, top));

		onComposingBarCreated();
	}

	public void restoreSavedAttachmentsToComposer(final List<ImageItem> images) {
		imageAttachmentstList.clear();
		imageAttachmentstList.addAll(images);
		View v = messageCenterListView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		// Only update composing view if image is attached successfully
		messageCenterListAdapter.addImagestoComposer(images);
		messageCenterListAdapter.setForceShowKeyboard(false);
		int firstIndex = messageCenterListView.getFirstVisiblePosition();

		messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
				firstIndex, top));

		onComposingBarCreated();
	}

	public void removeImageFromComposer(final int position) {
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_ATTACHMENT_DELETE);
		imageAttachmentstList.remove(position);
		messageCenterListAdapter.removeImageFromComposer(position);
		int count = imageAttachmentstList.size();
		// Show keyboard if all attachments have been removed
		messageCenterListAdapter.setForceShowKeyboard(count == 0);
		messageCenterViewHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);

		onComposingBarCreated();

	}

	public void openNonImageAttachment(final ImageItem image) {
		if (image == null) {
			Log.d("No attachment argument.");
			return;
		}

		try {
			Util.openFileAttachment(viewActivity, image.originalPath, image.localCachePath, image.mimeType);
		} catch (Exception e) {
			Log.e("Error loading attachment", e);
		}
	}

	public void showAttachmentDialog(final ImageItem image) {
		if (image == null) {
			Log.d("No attachment argument.");
			return;
		}

		try {

			AttachmentPreviewDialog dialog = AttachmentPreviewDialog.newInstance(image);
			dialog.show(((ApptentiveInternalActivity) viewActivity).getSupportFragmentManager(), "preview_dialog");
		} catch (Exception e) {
			Log.e("Error loading attachment preview.", e);
		}
	}


	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onMessageSent(ApptentiveHttpResponse response, final ApptentiveMessage apptentiveMessage) {
		if (response.isSuccessful() || response.isRejectedPermanently() || response.isBadPayload()) {
			messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_MESSAGE_SENT,
					apptentiveMessage));
		}
	}

	public synchronized void onPauseSending(int reason) {
		messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_PAUSE_SENDING,
				reason, 0));
	}

	public synchronized void onResumeSending() {
		messageCenterViewHandler.sendEmptyMessage(MSG_RESUME_SENDING);
	}

	public void clearWhoCardUi(Animator.AnimatorListener al,
							   ValueAnimator.AnimatorUpdateListener vl, long delay) {
		if (whoCardItem != null) {
			if (al != null) {
				deleteItemWithAnimation(messageCenterListAdapter.getWhoCardView(), al, vl, delay);
			} else {
				whoCardItem = null;
				pendingWhoCardName = null;
				pendingWhoCardEmail = null;
				pendingWhoCardAvatarFile = null;
				pendingWhoCardMode = 0;
				messageCenterListAdapter.clearWhoCard();
			}
		}
	}

	public void clearComposingUi(Animator.AnimatorListener al,
								 ValueAnimator.AnimatorUpdateListener vl, long delay) {
		if (composingItem != null) {
			if (al != null) {
				deleteItemWithAnimation(messageCenterListAdapter.getComposingActionBarView(), null, null, delay);
				deleteItemWithAnimation(messageCenterListAdapter.getComposingAreaView(), al, vl, delay);
			} else {
				if (contextualMessage != null) {
					messages.remove(contextualMessage);
					contextualMessage = null;
				}
				messages.remove(actionBarItem);
				messages.remove(composingItem);
				actionBarItem = null;
				composingItem = null;
				messageEditText = null;
				messageCenterListAdapter.clearComposing();
				messageCenterListAdapter.notifyDataSetChanged();
				showFab();
			}
		}
	}

	@Override
	public void onComposingBarCreated() {
		MessageCenterComposingActionBarView barView = messageCenterListAdapter.getComposingActionBarView();
		if (barView != null) {
			barView.showConfirmation = true;
			int attachmentCount = imageAttachmentstList.size();
			if (attachmentCount == 0) {
				Editable content = getPendingComposingContent();
				final String messageText = (content != null) ? content.toString().trim() : "";
				barView.showConfirmation = !(messageText.isEmpty());
			}

			if (attachmentCount == viewActivity.getResources().getInteger(R.integer.apptentive_image_grid_default_attachments_total)) {
				AnimationUtil.fadeOutGone(barView.attachButton);
			} else {
				if (barView.attachButton.getVisibility() != View.VISIBLE) {
					AnimationUtil.fadeIn(barView.attachButton, null);
				}
			}

			if (barView.showConfirmation == true) {
				barView.sendButton.setEnabled(true);
				barView.sendButton.setColorFilter(Util.getThemeColorFromAttrOrRes(viewActivity, R.attr.colorAccent,
						R.color.colorAccent));
			} else {
				barView.sendButton.setEnabled(false);
				barView.sendButton.setColorFilter(Util.getThemeColorFromAttrOrRes(viewActivity, R.attr.apptentive_material_disabled_icon,
						R.color.apptentive_material_dark_disabled_icon));
			}
		}
	}

	@Override
	public void onComposingViewCreated() {
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_COMPOSE_OPEN);
		messageEditText = messageCenterListAdapter.getEditTextInComposing();
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		// Restore composing text editing state, such as cursor position, after rotation
		if (composingViewSavedState != null) {
			messageEditText.onRestoreInstanceState(composingViewSavedState);
			composingViewSavedState = null;
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE).commit();
		}
		// Restore composing text
		if (prefs.contains(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE)) {
			String messageText = prefs.getString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, null);
			if (messageText != null) {
				messageEditText.setText(messageText);
			}
			// Stored pending composing text has been restored, remove it from the persistent storage
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE).commit();
		}


		// Restore composing attachments
		if (prefs.contains(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS)) {
			JSONArray jArray = null;
			try {
				jArray = new JSONArray(prefs.getString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS, ""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jArray != null && jArray.length() > 0) {
				if (savedAttachmentstList == null) {
					savedAttachmentstList = new ArrayList<ImageItem>();
				}
				for (int i = 0; i < jArray.length(); i++) {
					try {
						JSONObject json = jArray.getJSONObject(i);
						if (json != null) {
							savedAttachmentstList.add(new ImageItem(json));
						}
					} catch (JSONException e) {
						continue;
					}
				}
			}
			// Stored pending attachemnts have been restored, remove it from the persistent storage
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS).commit();
		}

		if (savedAttachmentstList != null) {
			restoreSavedAttachmentsToComposer(savedAttachmentstList);
			savedAttachmentstList = null;
		}
		messageCenterListView.setPadding(0, 0, 0, 0);
	}

	@Override
	public void onWhoCardViewCreated(EditText nameEditText, EditText emailEditText) {
		if (pendingWhoCardName != null) {
			nameEditText.onRestoreInstanceState(pendingWhoCardName);
			pendingWhoCardName = null;
		}
		if (pendingWhoCardEmail != null) {
			emailEditText.onRestoreInstanceState(pendingWhoCardEmail);
			pendingWhoCardEmail = null;
		}
		messageCenterListView.setPadding(0, 0, 0, 0);
	}

	@Override
	public void beforeComposingTextChanged(CharSequence str) {

	}

	@Override
	public void onComposingTextChanged(CharSequence str) {
	}

	@Override
	public void afterComposingTextChanged(String str) {
		onComposingBarCreated();
	}

	@Override
	public void onCancelComposing() {
		messageCenterListAdapter.setForceShowKeyboard(false);
		Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));

		JSONObject data = new JSONObject();
		try {
			Editable content = getPendingComposingContent();
			int bodyLength = (content != null) ? content.toString().trim().length() : 0;
			data.put("body_length", bodyLength);
		} catch (JSONException e) {
			//
		}
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_COMPOSE_CLOSE, data.toString());

		clearComposingUi(new Animator.AnimatorListener() {

							 @Override
							 public void onAnimationStart(Animator animation) {
							 }

							 @Override
							 public void onAnimationRepeat(Animator animation) {
							 }

							 @Override
							 public void onAnimationEnd(Animator animation) {
								 if (contextualMessage != null) {
									 messages.remove(contextualMessage);
									 contextualMessage = null;
								 }
								 messages.remove(actionBarItem);
								 messages.remove(composingItem);
								 actionBarItem = null;
								 composingItem = null;
								 messageEditText = null;
								 messageCenterListAdapter.clearComposing();
								 addExpectationStatusIfNeeded();
								 messageCenterListAdapter.notifyDataSetChanged();
								 imageAttachmentstList.clear();
								 showFab();
								 showProfileButton();
								 // messageEditText has been set to null, pending composing message will reset
								 clearPendingComposingMessage();
							 }

							 @Override
							 public void onAnimationCancel(Animator animation) {
							 }
						 },
				null,
				DEFAULT_DELAYMILLIS);
		//clearComposingUi(null, null, 0);
	}

	@Override
	public void onFinishComposing() {
		messageCenterListAdapter.setForceShowKeyboard(false);
		Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
		if (contextualMessage != null) {
			unsendMessagesCount++;
			MessageManager.sendMessage(viewActivity.getApplicationContext(), contextualMessage);
			contextualMessage = null;
		}
		Editable content = getPendingComposingContent();
		final String messageText = (content != null) ? content.toString().trim() : "";
		final ArrayList<ImageItem> messageAttachments = new ArrayList<ImageItem>();
		messageAttachments.addAll(imageAttachmentstList);
		// Close all composing UI
		clearComposingUi(new Animator.AnimatorListener() {

							 @Override
							 public void onAnimationStart(Animator animation) {
							 }

							 @Override
							 public void onAnimationRepeat(Animator animation) {
							 }

							 @Override
							 public void onAnimationEnd(Animator animation) {
								 messages.remove(actionBarItem);
								 messages.remove(composingItem);
								 actionBarItem = null;
								 composingItem = null;
								 messageEditText = null;
								 messageCenterListAdapter.clearComposing();
								 messageCenterListAdapter.notifyDataSetChanged();
								 clearPendingComposingMessage();
								 // Send out the new message. The delay is added to ensure the CardView showing animation
								 // is visible after the keyboard is hidden
								 if (!messageText.isEmpty() || imageAttachmentstList.size() != 0) {
									 Bundle b = new Bundle();
									 b.putString(COMPOSING_EDITTEXT_STATE, messageText);
									 b.putParcelableArrayList(COMPOSING_ATTACHMENTS, messageAttachments);
									 Message msg = messageCenterViewHandler.obtainMessage(MSG_START_SENDING,
											 messageText);
									 msg.setData(b);
									 messageCenterViewHandler.sendMessageDelayed(msg, DEFAULT_DELAYMILLIS);
								 }

								 imageAttachmentstList.clear();
								 showFab();
								 showProfileButton();
							 }

							 @Override
							 public void onAnimationCancel(Animator animation) {
							 }
						 },
				null,
				DEFAULT_DELAYMILLIS);
	}

	@Override
	public void onSubmitWhoCard(String buttonLabel) {
		JSONObject data = new JSONObject();
		try {
			data.put("required", interaction.getWhoCardRequired());
			data.put("button_label", buttonLabel);
		} catch (JSONException e) {
			//
		}
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_PROFILE_SUBMIT, data.toString());

		cleanupWhoCard();
	}

	@Override
	public void onCloseWhoCard(String buttonLabel) {
		JSONObject data = new JSONObject();
		try {
			data.put("required", interaction.getWhoCardRequired());
			data.put("button_label", buttonLabel);
		} catch (JSONException e) {
			//
		}
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_PROFILE_CLOSE, data.toString());

		cleanupWhoCard();
	}

	public void cleanupWhoCard() {
		messageCenterListAdapter.setForceShowKeyboard(false);
		Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
		clearWhoCardUi(
				new Animator.AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						messages.remove(whoCardItem);
						whoCardItem = null;
						pendingWhoCardName = null;
						pendingWhoCardEmail = null;
						pendingWhoCardAvatarFile = null;
						pendingWhoCardMode = 0;
						messageCenterListAdapter.clearWhoCard();
						addExpectationStatusIfNeeded();
						messageCenterListAdapter.notifyDataSetChanged();
						saveWhoCardSetState();
						// If Who card is required, it might be displayed before proceeding to composing, for instance
						// when there was a contextual message or it was the first message. We need to resume composing
						// after dismissing Who Card
						if ((messages.size() == 1 || contextualMessage != null) && interaction.getWhoCardRequired()) {
							addComposingArea();
							messageCenterListAdapter.setForceShowKeyboard(true);
							messageCenterViewHandler.sendEmptyMessageDelayed(MSG_MESSAGE_NOTIFY_UPDATE, DEFAULT_DELAYMILLIS);
						} else {
							showFab();
							showProfileButton();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				},
				null,
				DEFAULT_DELAYMILLIS
		);
	}

	@Override
	public void onNewMessageReceived(final CompoundMessage apptentiveMsg) {
		messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_MESSAGE_ADD_INCOMING,
				apptentiveMsg));
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
		if (bCanScrollUp) {
			if (android.os.Build.VERSION.SDK_INT > 20) {
				messageCenterHeader.setElevation(8);
			} else {
				headerDivider.setVisibility(View.VISIBLE);
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT > 20) {
				messageCenterHeader.setElevation(0);
			} else {
				headerDivider.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void OnListViewResize(int w, int h, int oldw, int oldh) {
		// detect keyboard launching
		if (oldh > h) {
			if (composingItem != null) {
				// When keyboard is up, adjust the scolling such that the cursor is always visible
				final int firstIndex = messageCenterListView.getFirstVisiblePosition();
				int lastIndex = messageCenterListView.getLastVisiblePosition();
				View v = messageCenterListView.getChildAt(lastIndex - firstIndex);
				int top = (v == null) ? 0 : v.getTop();
				if (messageEditText != null) {
					int pos = messageEditText.getSelectionStart();
					Layout layout = messageEditText.getLayout();
					int line = layout.getLineForOffset(pos);
					int baseline = layout.getLineBaseline(line);
					int ascent = layout.getLineAscent(line);
					messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
							lastIndex, Math.max(top - (oldh - h), baseline - ascent)));
				}
			}
		}
	}

	/* Callback when the attach button is clicked
	 *
	 */
	@Override
	public void onAttachImage() {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//prior Api level 19
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				Intent chooserIntent = Intent.createChooser(intent, null);
				viewActivity.startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER);
			} else {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				Intent chooserIntent = Intent.createChooser(intent, null);
				viewActivity.startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER);
			}
			imagePickerLaunched = true;
		} catch (Exception e) {
			e.printStackTrace();
			imagePickerLaunched = false;
			Log.d("can't launch image picker");
		}
	}

	private void saveWhoCardSetState() {
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, true);
		editor.apply();
	}

	// Retrieve the content from the composing area
	public Editable getPendingComposingContent() {
		return (messageEditText == null) ? null : messageEditText.getText();
	}


	public void savePendingComposingMessage() {
		Editable content = getPendingComposingContent();
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		if (content != null) {
			editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, content.toString().trim());
		} else {
			editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE);
		}

		JSONArray jArray = new JSONArray();
		// Save pending attachment
		for (ImageItem pendingAttachment : imageAttachmentstList) {
			JSONObject jobject = pendingAttachment.toJSON();
			if (jobject != null) {
				jArray.put(jobject);
			}
		}

		if (jArray.length() > 0) {
			editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS, jArray.toString());
		} else {
			editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS);
		}
		editor.apply();
	}

	/* When no composing view is presented in the list view, calling this method
	 * will clear the pending composing message previously saved in shared preference
	 */
	public void clearPendingComposingMessage() {
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE);
		editor.remove(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS);
		editor.apply();
	}

	private Parcelable saveEditTextInstanceState() {
		savePendingComposingMessage();
		if (messageEditText != null) {
			// Hide keyboard if the keyboard was up prior to rotation
			Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
			return messageEditText.onSaveInstanceState();
		}
		return null;
	}

	Set<String> dateStampsSeen = new HashSet<String>();

	public void updateMessageSentStates() {
		dateStampsSeen.clear();
		MessageCenterUtil.CompoundMessageCommonInterface lastSent = null;
		Set<String> uniqueNonce = new HashSet<String>();
		Iterator<MessageCenterListItem> messageIterator = messages.iterator();
		while (messageIterator.hasNext()) {
			MessageCenterListItem message = messageIterator.next();
			if (message instanceof ApptentiveMessage) {
				/* Check if there is any duplicate messages and remove if found.
				* add() of a Set returns false if the element already exists.
				 */
				if (!uniqueNonce.add(((ApptentiveMessage) message).getNonce())) {
					messageIterator.remove();
					continue;
				}
				// Update timestamps
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) message;
				Double sentOrReceivedAt = apptentiveMessage.getCreatedAt();
				String dateStamp = createDatestamp(sentOrReceivedAt);
				if (dateStamp != null) {
					if (dateStampsSeen.add(dateStamp)) {
						apptentiveMessage.setDatestamp(dateStamp);
					} else {
						apptentiveMessage.clearDatestamp();
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


	private void deleteItemWithAnimation(final View v, final Animator.AnimatorListener al,
										 final ValueAnimator.AnimatorUpdateListener vl, long delay) {
		if (v == null) {
			return;
		}
		AnimatorSet animatorSet = AnimationUtil.buildListViewRowRemoveAnimator(v, al, vl);
		animatorSet.setStartDelay(delay);
		animatorSet.start();
	}

	private void showFab() {
		float scale = viewActivity.getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (viewActivity.getResources().getDimension(R.dimen.apptentive_message_center_bottom_padding) * scale + 0.5f);
		messageCenterListView.setPadding(0, 0, 0, dpAsPixels);
		AnimationUtil.scaleFadeIn(fab);
	}

	private void hideFab() {
		AnimationUtil.scaleFadeOutGone(fab);
	}

	private void showProfileButton() {
		AnimationUtil.fadeIn(profileButton, null);
	}

	private void hideProfileButton() {
		AnimationUtil.fadeOutGone(profileButton);
	}

	/*
	 * Messages returned from the database was sorted on KEY_ID, which was generated by server
	 * with seconds resolution. If messages were received by server within a second, messages may be out of order
	 * This method uses insertion sort to re-sort the messages retrieved from the database
	 */
	private void prepareMessages(final List<MessageCenterListItem> originalItems) {
		messages.clear();
		unsendMessagesCount = 0;
		// Loop through each message item retrieved from database
		for (MessageCenterListItem item : originalItems) {
			if (item instanceof ApptentiveMessage) {
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) item;
				Double createdAt = apptentiveMessage.getCreatedAt();
				if (apptentiveMessage.isOutgoingMessage() && createdAt == null) {
					unsendMessagesCount++;
				}

				/*
				 * Find proper location to insert into the messages list of the listview.
				 */
				ListIterator<MessageCenterListItem> listIterator = messages.listIterator();
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
		// Finally, add greeting message
		messages.add(0, interaction.getGreeting());
	}

	@Override
	public void onClickAttachment(final int position, final ImageItem image) {
		if (Util.isMimeTypeImage(image.mimeType)) {
			// "+" placeholder is clicked
			if (TextUtils.isEmpty(image.originalPath)) {
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
	 * Called when attachment overlayed "selection" ui is tapped. The "selection" ui could be selection checkbox
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
}
