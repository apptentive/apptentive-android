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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;

import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.InteractionView;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.MessagePollingWorker;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.AutomatedMessage;
import com.apptentive.android.sdk.module.messagecenter.model.IncomingTextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingTextMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.WeakReferenceHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Barry Li
 */

public class MessageCenterActivityContent extends InteractionView<MessageCenterInteraction>
		implements MessageManager.AfterSendMessageListener,
		MessageAdapter.OnComposingActionListener,
		MessageManager.OnNewIncomingMessagesListener,
		AbsListView.OnScrollListener {

	// keys used to save instance in the event of rotation
	private final static String LIST_INSTANCE_STATE = "list";
	private final static String COMPOSING_EDITTEXT_STATE = "edittext";
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

	// Data backing of the listview
	private ArrayList<MessageCenterListItem> messages = new ArrayList<>();
	private MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsendMessagesCount = 0;


	private MessageCenterStatus statusItem;
	private MessageCenterComposingItem composingItem;
	private MessageCenterComposingItem actionBarItem;
	private MessageCenterComposingItem whoCardItem;
	private AutomatedMessage contextualMessage;
	/**
	 * Used to save the state of the message text box if the user closes Message Center for a moment,
	 * , rotate device, attaches a file, etc.
	 */
	private Parcelable composingViewSavedState;

	/**
	 * Used to save the state of the who card if the user closes Message Center for a moment,
	 * , rotate device, attaches a file, etc.
	 */
	private int pendingWhoCardMode;
	private String pendingWhoCardName;
	private String pendingWhoCardEmail;
	private String pendingWhoCardAvatarFile;


	protected static final int MSG_SCROLL_TO_BOTTOM = 1;
	protected static final int MSG_SCROLL_FROM_TOP = 2;
	protected static final int MSG_MESSAGE_SENT = 3;
	protected static final int MSG_START_SENDING = 4;
	protected static final int MSG_PAUSE_SENDING = 5;
	protected static final int MSG_RESUME_SENDING = 6;
	protected static final int MSG_MESSAGE_ADD_INCOMING = 7;


	private final Handler.Callback messageCenterViewCallback = new Handler.Callback() {

		public boolean handleMessage(Message msg) {

			switch (msg.what) {
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
					updateMessageTimeStamps();
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
						messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
						break;
					}
					MessageCenterStatus newItem = interaction.getRegularStatus();
					if (newItem != null) {
						addNewStatusItem(newItem);
					}
					messageCenterListAdapter.notifyDataSetChanged();
					break;
				}
				case MSG_START_SENDING: {
					String messageText = (String) msg.obj;
					OutgoingTextMessage message = new OutgoingTextMessage();
					message.setBody(messageText);
					message.setRead(true);
					message.setCustomData(ApptentiveInternal.getAndClearCustomData());
					MessageManager.sendMessage(viewActivity.getApplicationContext(), message);
					addNewOutGoingMessageItem(message);
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
						}
					}
					break;
				}
				case MSG_RESUME_SENDING: {
					if (isPaused) {
						isPaused = false;
						if (statusItem != null) {
							messages.remove(statusItem);
							statusItem = null;
						}
						if (unsendMessagesCount > 0) {
							messageCenterListAdapter.setPaused(isPaused);
							messageCenterListAdapter.notifyDataSetChanged();
						}
					}
					break;
				}
				case MSG_MESSAGE_ADD_INCOMING: {
					ApptentiveMessage apptentiveMessage = (ApptentiveMessage) msg.obj;
					addNewIncomingMessageItem(apptentiveMessage);
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

		boolean bRestoreListView = onSavedInstanceState != null &&
				onSavedInstanceState.getParcelable(LIST_INSTANCE_STATE) != null;
		composingViewSavedState = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getParcelable(COMPOSING_EDITTEXT_STATE);
		pendingWhoCardName = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_NAME);
		pendingWhoCardEmail = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_EMAIL);
		pendingWhoCardAvatarFile = (onSavedInstanceState == null) ? null :
				onSavedInstanceState.getString(WHO_CARD_AVATAR_FILE);
		pendingWhoCardMode = (onSavedInstanceState == null) ? 0 :
				onSavedInstanceState.getInt(WHO_CARD_MODE);
		String contextualMessageBody = interaction.getContextualMessageBody();
		contextualMessage = AutomatedMessage.createAutoMessage(null, contextualMessageBody);

		setup();

		// This listener will run when messages are retrieved from the server, and will start a new thread to update the view.
		MessageManager.addInternalOnMessagesUpdatedListener(this);
		// Give the MessageCenterView a callback when a message is sent.
		MessageManager.setAfterSendMessageListener(this);
		// Needed to prevent the window from being pushed up when a text input area is focused.
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED |
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		if (!bRestoreListView) {
			messageCenterViewHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, DEFAULT_DELAYMILLIS);
		}
	}

	protected void setup() {
		ImageButton closeButton = (ImageButton) viewActivity.findViewById(R.id.close);
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
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
		messageCenterListView.setItemsCanFocus(true);
		messageCenterListView.setOnScrollListener(this);

		fab = viewActivity.findViewById(R.id.composing_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addComposingArea();
				messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
			}
		});
		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(viewActivity);
			unsendMessagesCount = countUnsendOutgoingMessages(items);
			// Add greeting message
			items.add(0, interaction.getGreeting());
			messages.addAll(items);

			if (contextualMessage != null) {
				addContextualMessage();
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
			} else if (items.size() == 1) {
				/* If there is only greeting message, show composing.
				 * If Who Card is required, show Who Card first
				 */
				if (!addWhoCardInitRequired()) {
					addComposingArea();
				}
			} else {
				addNewStatusItem(new MessageCenterStatus(null, null));
			}

			messageCenterListAdapter = new MessageAdapter<>(viewActivity, messages, this, interaction);
			messageCenterListView.setAdapter(messageCenterListAdapter);
			updateMessageTimeStamps(); // Force timestamp recompilation.
		}


		View attachButton = viewActivity.findViewById(R.id.attach);
		if (attachButton != null && attachButton.getVisibility() == View.VISIBLE) {
			// Android devices can't take screenshots until Android OS version 4+
			boolean canTakeScreenshot = Util.getMajorOsVersion() >= 4;
			if (canTakeScreenshot) {
				attachButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_ATTACH);
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						Bundle extras = new Bundle();
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						if (Build.VERSION.SDK_INT >= 11) {
							extras.putBoolean(Intent.EXTRA_LOCAL_ONLY, true);
						}
						intent.setType("image/*");
						if (!extras.isEmpty()) {
							intent.putExtras(extras);
						}
						Intent chooserIntent = Intent.createChooser(intent, null);
						viewActivity.startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
					}
				});
			} else {
				attachButton.setVisibility(View.GONE);
			}
		}

		View profileButton = viewActivity.findViewById(R.id.profile);
		if (profileButton != null) {
			profileButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// Only allow profile editing when not already editing profile or in message composing
					if (whoCardItem == null && composingItem == null) {
						SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
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
						messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
					}
				}
			});
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(LIST_INSTANCE_STATE, messageCenterListView.onSaveInstanceState());
		outState.putParcelable(COMPOSING_EDITTEXT_STATE, saveEditTextInstanceState());
		outState.putString(WHO_CARD_NAME, messageCenterListAdapter.getWhoCardName());
		outState.putString(WHO_CARD_EMAIL, messageCenterListAdapter.getWhoCardEmail());
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
		messageCenterListView.onRestoreInstanceState(savedInstanceState.getParcelable(LIST_INSTANCE_STATE));
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		cleanup();
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_CANCEL);
		return true;
	}

	public boolean cleanup() {
		saveOrClearPendingComposingMessage();
		clearPendingMessageCenterPushNotification();
		clearComposingUi(null, null);
		clearWhoCardUi(null, null);
		// Set to null, otherwise they will hold reference to the activity context
		MessageManager.clearInternalOnMessagesUpdatedListeners();
		MessageManager.setAfterSendMessageListener(null);
		ApptentiveInternal.getAndClearCustomData();
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
				case Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER:
					showAttachmentDialog(viewActivity, data.getData());
					break;
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

	public int countUnsendOutgoingMessages(final List<MessageCenterListItem> items) {
		int count = 0;
		for (MessageCenterListItem item : items) {
			if (item instanceof ApptentiveMessage) {
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) item;
				if (apptentiveMessage.isOutgoingMessage() && apptentiveMessage.getCreatedAt() == null) {
					count++;
				}
			}
		}
		return count;
	}

	public void addContextualMessage() {
		// Clear any pending composing message to present an empty composing area
		saveOrClearPendingComposingMessage();
		clearStatus();
		messages.add(contextualMessage);
		// If addWhoCardInitRequired returns true, it will add WhoCard
		if (!addWhoCardInitRequired()) {
			addComposingArea();
		}
	}

	public void addComposingArea() {
		fab.setVisibility(View.INVISIBLE);
		clearStatus();
		actionBarItem = interaction.getComposerBar();
		messages.add(actionBarItem);
		composingItem = interaction.getComposerArea();
		messages.add(composingItem);
	}

	private boolean addWhoCardInitRequired() {
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);
		if (interaction.getWhoCardRequestEnabled() &&
				interaction.getWhoCardRequired() && !bWhoCardSet) {
			addWhoCard(WHO_CARD_MODE_INIT);
			return true;
		}
		return false;
	}

	public void addWhoCard(int mode) {
		if (!interaction.getWhoCardRequestEnabled()) {
			return;
		}
		pendingWhoCardMode = mode;
		fab.setVisibility(View.INVISIBLE);
		clearStatus();
		whoCardItem = (mode == WHO_CARD_MODE_INIT) ? interaction.getWhoCardInit()
				: interaction.getWhoCardEdit();
		messages.add(whoCardItem);
	}

	public void addNewStatusItem(MessageCenterListItem item) {
		clearStatus();

		if (composingItem != null) {
			return;
		}

		statusItem = (MessageCenterStatus) item;
		messages.add(item);
		if (!statusItem.isEmpty) {
			EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_STATUS);
			messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
		}
	}

	public void addNewOutGoingMessageItem(ApptentiveMessage message) {
		clearStatus();

		messages.add(message);
		unsendMessagesCount++;
    // Add empty elevation for FAB
		addNewStatusItem(new MessageCenterStatus(null, null));

		isPaused = false;
		messageCenterListAdapter.setPaused(isPaused);

		messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
	}

	public void addNewIncomingMessageItem(ApptentiveMessage message) {
		boolean hasEmptyStatus = false;
		boolean needEmptyStatus = false;
		if (statusItem != null && statusItem.isEmpty) {
			hasEmptyStatus = true;
		} else {
			clearStatus();
		}
		// Determine where to insert the new incoming message
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
		} else if (hasEmptyStatus) {
			insertIndex -= 1;
		} else {
			needEmptyStatus = true;
		}
		messages.add(insertIndex, message);

		int firstIndex = messageCenterListView.getFirstVisiblePosition();
		int lastIndex = messageCenterListView.getLastVisiblePosition();
		boolean composingAreaTakesUpVisibleArea = firstIndex <= insertIndex && insertIndex < lastIndex;
		if (composingAreaTakesUpVisibleArea) {
			View v = messageCenterListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			updateMessageTimeStamps();
			// Restore the position of listview to composing view
			messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
					insertIndex, top));
		} else {
			updateMessageTimeStamps();
			if (needEmptyStatus) {
				addNewStatusItem(new MessageCenterStatus(null, null));
			}
			messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
		}

	}

	private void clearStatus() {
		// Remove the status message whenever a new incoming message is added
		if (statusItem != null) {
			messages.remove(statusItem);
			messageCenterListAdapter.notifyDataSetChanged();
			statusItem = null;
		}
	}

	public void sendImage(final Uri uri) {
		final OutgoingFileMessage message = new OutgoingFileMessage();
		boolean successful = message.internalCreateStoredImage(viewActivity.getApplicationContext(), uri.toString());
		if (successful) {
			message.setRead(true);
			message.setCustomData(ApptentiveInternal.getAndClearCustomData());

			// Finally, send out the message.
			MessageManager.sendMessage(viewActivity.getApplicationContext(), message);
			addNewOutGoingMessageItem(message);

		} else {
			Log.e("Unable to send file.");
			Toast.makeText(viewActivity, "Unable to send file.", Toast.LENGTH_SHORT).show();
		}
	}

	public void showAttachmentDialog(Context context, final Uri data) {
		if (data == null) {
			Log.d("No attachment found.");
			return;
		}

		try {
			AttachmentPreviewDialog dialog = new AttachmentPreviewDialog(context);
			dialog.setImage(data);
			dialog.setOnAttachmentAcceptedListener(new AttachmentPreviewDialog.OnAttachmentAcceptedListener() {
				@Override
				public void onAttachmentAccepted() {
					sendImage(data);
				}
			});
			dialog.show();
		} catch (Exception e) {
			Log.e("Error loading attachment preview.", e);
		}
	}


	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onMessageSent(ApptentiveHttpResponse response, final ApptentiveMessage apptentiveMessage) {
		if (response.isSuccessful()) {
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
														 ValueAnimator.AnimatorUpdateListener vl) {
		if (whoCardItem != null) {
			if (al != null) {
				deleteItemWithAnimation(messageCenterListAdapter.getWhoCardView(), al, vl);
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
															 ValueAnimator.AnimatorUpdateListener vl) {
		if (composingItem != null) {
			if (al != null) {
				deleteItemWithAnimation(messageCenterListAdapter.getComposingAreaView(), al, vl);
			} else {
				actionBarItem = null;
				composingItem = null;
				messageEditText = null;
				messageCenterListAdapter.clearComposing();
			}
		}
	}

	@Override
	public void onComposingViewCreated() {
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_COMPOSE_OPEN);
		messageEditText = messageCenterListAdapter.getEditTextInComposing();
		if (composingViewSavedState != null) {
			messageEditText.onRestoreInstanceState(composingViewSavedState);
			composingViewSavedState = null;
		} else {
			String messageText = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).
					getString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, null);
			if (messageText != null) {
				messageEditText.setText(messageText);
			} else {
				messageEditText.setText("");
			}
		}
		Util.showSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
	}

	@Override
	public void onWhoCardViewCreated(EditText nameEditText, EditText emailEditText) {
		if (pendingWhoCardName != null) {
			nameEditText.setText(pendingWhoCardName);
		} else {
			nameEditText.setText(PersonManager.loadPersonName(viewActivity.getApplicationContext()));
		}
		if (pendingWhoCardEmail != null) {
			emailEditText.setText(pendingWhoCardEmail);
		} else {
			emailEditText.setText(PersonManager.loadPersonEmail(viewActivity.getApplicationContext()));
		}
		Util.showSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
	}

	@Override
	public void onComposing(String composingStr) {
	}

	@Override
	public void onCancelComposing() {
		if (contextualMessage != null) {
			messages.remove(contextualMessage);
			contextualMessage = null;
		}
		JSONObject data = new JSONObject();
		try {
			int bodyLength = getPendingComposingContent().toString().trim().length();
			data.put("body_length", bodyLength);
		} catch (JSONException e) {
			//
		}
		EngagementModule.engageInternal(viewActivity, interaction, MessageCenterInteraction.EVENT_NAME_COMPOSE_CLOSE, data.toString());

		final View view = messageCenterListAdapter.getComposingAreaView();
		final int height = view.getMeasuredHeight();

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
				messageCenterListAdapter.needInflateComposing = true;
				messageCenterListAdapter.notifyDataSetChanged();
				Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
				showFab();
				// messageEditText has been set to null, pending composing message will reset
				saveOrClearPendingComposingMessage();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		},
				createValueAnimatiorListenerHelper(messageCenterListAdapter.getComposingAreaView()));
	}

	@Override
	public void onFinishComposing() {
		if (contextualMessage != null) {
			unsendMessagesCount++;
			MessageManager.sendMessage(viewActivity.getApplicationContext(), contextualMessage);
			contextualMessage = null;
		}
		final String messageText = getPendingComposingContent().toString().trim();
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
				Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
				messages.remove(actionBarItem);
				messages.remove(composingItem);
				actionBarItem = null;
				composingItem = null;
				messageEditText = null;
				messageCenterListAdapter.clearComposing();
				messageCenterListAdapter.needInflateComposing = true;
				messageCenterListAdapter.notifyDataSetChanged();
				saveOrClearPendingComposingMessage();
				// Send out the new message. The delay is added to ensure the CardView showing animation
				// is visible after the keyboard is hidden
				if (!messageText.isEmpty()) {
					messageCenterViewHandler.sendMessageDelayed(messageCenterViewHandler.obtainMessage(MSG_START_SENDING,
							messageText), DEFAULT_DELAYMILLIS);
				}
				showFab();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		},
				createValueAnimatiorListenerHelper(messageCenterListAdapter.getComposingAreaView()));
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
						messageCenterListAdapter.needInflateWhoCard = true;
						whoCardItem = null;
						pendingWhoCardName = null;
						pendingWhoCardEmail = null;
						pendingWhoCardAvatarFile = null;
						pendingWhoCardMode = 0;
						messageCenterListAdapter.clearWhoCard();
						messageCenterListAdapter.notifyDataSetChanged();
						SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
						boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);
						saveWhoCardSetState();
						if (!bWhoCardSet && interaction.getWhoCardRequired()) {
							addComposingArea();
						} else {
							Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
							showFab();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				},
				createValueAnimatiorListenerHelper(messageCenterListAdapter.getWhoCardView())
				);
	}

	@Override
	public void onMessagesUpdated(final IncomingTextMessage apptentiveMsg) {
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

	private void saveWhoCardSetState() {
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, true);
		editor.commit();
	}

	// Retrieve the content from the composing area
	public Editable getPendingComposingContent() {
		return (messageEditText == null) ? null : messageEditText.getText();
	}

	/* When no composing view is presented in the list view, calling this method
	 * will clear the pending composing message previously saved in shared preference
	 */
	public void saveOrClearPendingComposingMessage() {
		Editable content = getPendingComposingContent();
		SharedPreferences prefs = viewActivity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, (content != null) ? content.toString().trim() : null);
		editor.commit();
	}


	private Parcelable saveEditTextInstanceState() {
		saveOrClearPendingComposingMessage();
		if (messageEditText != null) {
			// Hide keyboard if the keyboard was up prior to rotation
			Util.hideSoftKeyboard(viewActivity, viewActivity.findViewById(android.R.id.content));
			return messageEditText.onSaveInstanceState();
		}
		return null;
	}

	Set<String> dateStampsSeen = new HashSet<>();

	public void updateMessageTimeStamps() {
		dateStampsSeen.clear();
		for (MessageCenterListItem message : messages) {
			if (message instanceof ApptentiveMessage) {
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
			}
		}
	}

	protected String createDatestamp(Double seconds) {
		if (seconds != null) {
			Date date = new Date(Math.round(seconds * 1000));
			DateFormat mediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return mediumDateFormat.format(date);
		}
		return null;
	}


	private void deleteItemWithAnimation(final View v, final Animator.AnimatorListener al,
																			 final ValueAnimator.AnimatorUpdateListener vl) {
		if (v == null) {
			return;
		}
		AnimatorSet animatorSet = Util.buildListViewRowRemoveAnimator(v, al, vl);
		animatorSet.start();
	}

	private void showFab() {
		// add an empty status at the bottom so that fab does not block any message
		addNewStatusItem(new MessageCenterStatus(null, null));
		fab.setVisibility(View.VISIBLE);
	}

	private ValueAnimator.AnimatorUpdateListener createValueAnimatiorListenerHelper(final View view) {
		final int height = view.getMeasuredHeight();
		return new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Float value = (Float) animation.getAnimatedValue();
				if (value >= 1) {
					view.setVisibility(View.GONE);
					showFab();
				} else {
					view.getLayoutParams().height = height
							- (int) (height * value);
					view.requestLayout();
					fab.setVisibility(View.VISIBLE);
					fab.setAlpha(value);
					fab.setScaleX(value);
					fab.setScaleY(value);
				}
			}
		};
	}
}
