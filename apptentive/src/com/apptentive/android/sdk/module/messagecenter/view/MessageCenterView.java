/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.text.Editable;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.*;


import com.apptentive.android.sdk.AboutModule;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;

import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.AutomatedMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingTextMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.WeakReferenceHandler;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.AfterSendMessageListener,
		MessageAdapter.OnComposingActionListener {

	private Activity activity;
	private ListView messageCenterListView;
	private View messageCenterFooter;
	private Map<String, String> customData;

	AnimatorSet showAnimatorSet;
	AnimatorSet dismissAnimatorSet;

	private Configuration configGlobal;

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
	private EditText messageEditText;
	private Parcelable composingViewSavedState;

	/**
	 * Used to save the state of the who card if the user closes Message Center for a moment,
	 * , rotate device, attaches a file, etc.
	 */
	private String pendingWhoCardName;
	private String pendingWhoCardEmail;
	private String pendingWhoCardAvatarFile;

	protected static final int MSG_SHOW_BRANDING = 1;
	protected static final int MSG_HIDE_BRANDING = 2;
	protected static final int MSG_SCROLL_TO_BOTTOM = 3;
	protected static final int MSG_SCROLL_FROM_TOP = 4;
	protected static final int MSG_MESSAGE_SENT = 5;
	protected static final int MSG_PAUSE_SENDING = 6;
	protected static final int MSG_RESUME_SENDING = 7;


	private final Handler.Callback messageCenterViewCallback = new Handler.Callback() {

		public boolean handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_SHOW_BRANDING:
					animateShow();
					break;
				case MSG_HIDE_BRANDING:
					animateHide();
					break;
				case MSG_SCROLL_TO_BOTTOM:
					messageCenterListAdapter.notifyDataSetChanged();
					messageCenterListView.setSelection(messages.size() - 1);
					break;
				case MSG_SCROLL_FROM_TOP:
					int index = msg.arg1;
					int top = msg.arg2;
					messageCenterListAdapter.notifyDataSetChanged();
					messageCenterListView.setSelectionFromTop(index, top);
					break;
				case MSG_MESSAGE_SENT:
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
					messageCenterListAdapter.notifyDataSetChanged();
					break;
				case MSG_PAUSE_SENDING:
					if (!isPaused) {
						isPaused = true;
						if (unsendMessagesCount > 0) {
							messageCenterListAdapter.setPaused(isPaused);
							MessageCenterStatus newItem = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION,
									activity.getResources().getString(R.string.apptentive_message_center_status_error_title
									), activity.getResources().getString(R.string.apptentive_message_center_status_error_body));
							addNewStatusItem(newItem);
						}
					}
					break;
				case MSG_RESUME_SENDING:
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
				default:
					return false;
			}
			return true;
		}
	};

	final Handler messageCenterViewHandler = WeakReferenceHandler.create(messageCenterViewCallback);

	public MessageCenterView(Activity activity, Map<String, String> customData,
													 String contextualMessageBody,
													 Parcelable editTextSavedState,
													 String whoCardName, String whoCardEmail, String whoCardAvatar) {
		super(activity.getApplicationContext());
		this.activity = activity;
		this.customData = customData;
		this.setId(R.id.apptentive_message_center_view);
		composingViewSavedState = editTextSavedState;
		pendingWhoCardName = whoCardName;
		pendingWhoCardEmail = whoCardEmail;
		pendingWhoCardAvatarFile = whoCardAvatar;
		if (contextualMessageBody != null) {
			contextualMessage = AutomatedMessage.createAutoMessage(null, contextualMessageBody);
		}
		setup(); // TODO: Move this into a configuration changed handler?
	}

	protected void setup() {
		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onBackPressed();
			}
		});
		configGlobal = Configuration.load(activity);
		TextView titleTextView = (TextView) findViewById(R.id.title);
		String titleText = configGlobal.getMessageCenterTitle();
		if (titleText != null) {
			titleTextView.setText(titleText);
		}

		messageCenterListView = (ListView) findViewById(R.id.message_list);
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);


		messageCenterListView.setItemsCanFocus(true);

		messageCenterFooter = findViewById(R.id.footer_bar);
		// Hide branding if needed.
		if (messageCenterFooter != null) {
			if (Configuration.load(activity).isHideBranding(activity)) {
				messageCenterFooter = null;
			} else {
				messageCenterFooter.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AboutModule.getInstance().show(activity);
					}
				});
			}
		}

		View fab = findViewById(R.id.composing_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addComposingArea();
				messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
			}
		});

		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(activity);
			unsendMessagesCount = countUnsendOutgoingMessages(items);
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
				addWhoCard();
			} else if (items.size() == 1) {
				// If there is only greeting message, show composing
				addComposingArea();
			} else if (messageCenterFooter != null) {
				messageCenterFooter.setVisibility(View.VISIBLE);
			}

			messageCenterListAdapter = new MessageAdapter<>(activity, messages, this);
			messageCenterListView.setAdapter(messageCenterListAdapter);
			updateMessageTimeStamps(); // Force timestamp recompilation.
		}


		View attachButton = findViewById(R.id.attach);
		if (attachButton != null && attachButton.getVisibility() == VISIBLE) {
			// Android devices can't take screenshots until Android OS version 4+
			boolean canTakeScreenshot = Util.getMajorOsVersion() >= 4;
			if (canTakeScreenshot) {
				attachButton.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						MetricModule.sendMetric(activity, Event.EventLabel.message_center__attach);
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
						activity.startActivityForResult(chooserIntent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
					}
				});
			} else {
				attachButton.setVisibility(GONE);
			}
		}

		View profileButton = findViewById(R.id.profile);
		if (profileButton != null) {
			profileButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					// Only allow profile editing when not already editing profile or in message composing
					if (whoCardItem == null && composingItem == null) {
						addWhoCard();
						messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
					}
				}
			});
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
		savePendingComposingMessage();
		clearStatus();
		messages.add(contextualMessage);
		addComposingArea();
	}

	public void addComposingArea() {
		View fab = findViewById(R.id.composing_fab);
		fab.setVisibility(View.INVISIBLE);
		View profileButton = findViewById(R.id.profile);
		profileButton.setVisibility(View.GONE);
		hideBranding();
		clearStatus();
		actionBarItem = new MessageCenterComposingItem(MessageCenterComposingItem.COMPOSING_ITEM_ACTIONBAR);
		messages.add(actionBarItem);
		composingItem = new MessageCenterComposingItem(MessageCenterComposingItem.COMPOSING_ITEM_AREA);
		messages.add(composingItem);
	}

	public void addWhoCard() {
		View fab = findViewById(R.id.composing_fab);
		fab.setVisibility(View.INVISIBLE);
		View profileButton = findViewById(R.id.profile);
		profileButton.setVisibility(View.INVISIBLE);
		hideBranding();
		clearStatus();
		whoCardItem = new MessageCenterComposingItem(MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD);
		messages.add(whoCardItem);
	}

	public void addNewStatusItem(MessageCenterListItem item) {
		clearStatus();

		if (composingItem != null) {
			return;
		}

		statusItem = (MessageCenterStatus) item;
		messages.add(item);
		messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
	}

	public void addNewOutGoingMessageItem(ApptentiveMessage message) {
		clearStatus();

		messages.add(message);
		unsendMessagesCount++;

		isPaused = false;
		messageCenterListAdapter.setPaused(isPaused);

		messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
	}

	public void addNewIncomingMessageItem(ApptentiveMessage message) {
		clearStatus();
		int composingAreaIndex = 0;
		// If user is composing message or WhoCard, new incoming message will be inserted in front
		if (composingItem != null || whoCardItem != null) {
			composingAreaIndex = messages.size() - 2;
			messages.add(composingAreaIndex, message);
		} else {
			messages.add(message);
		}

		int firstIndex = messageCenterListView.getFirstVisiblePosition();
		int lastIndex = messageCenterListView.getLastVisiblePosition();
		boolean composingAreaTakesUpVisibleArea = firstIndex <= composingAreaIndex && composingAreaIndex < lastIndex;
		if (composingAreaTakesUpVisibleArea) {
			View v = messageCenterListView.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			updateMessageTimeStamps();
			// Restore the position of listview to composing view
			messageCenterViewHandler.sendMessage(messageCenterViewHandler.obtainMessage(MSG_SCROLL_FROM_TOP,
					composingAreaIndex, top));
		} else {
			updateMessageTimeStamps();
			messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
		}

	}

	private void clearStatus() {
		// Remove the status message whenever a new incoming message is added
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}
	}

	public void sendImage(final Uri uri) {
		final OutgoingFileMessage message = new OutgoingFileMessage();
		boolean successful = message.internalCreateStoredImage(activity.getApplicationContext(), uri.toString());
		if (successful) {
			message.setRead(true);
			message.setCustomData(customData);

			// Finally, send out the message.
			MessageManager.sendMessage(activity.getApplicationContext(), message);
			addNewOutGoingMessageItem(message);

		} else {
			Log.e("Unable to send file.");
			Toast.makeText(activity, "Unable to send file.", Toast.LENGTH_SHORT).show();
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

	public synchronized void onPauseSending() {
		messageCenterViewHandler.sendEmptyMessage(MSG_PAUSE_SENDING);
	}

	public synchronized void onResumeSending() {
		messageCenterViewHandler.sendEmptyMessage(MSG_RESUME_SENDING);
	}

	public void clearWhoCardUi() {
		if (whoCardItem != null) {
			messages.remove(whoCardItem);
			whoCardItem = null;
			pendingWhoCardName = null;
			pendingWhoCardEmail = null;
			pendingWhoCardAvatarFile = null;
			messageCenterListAdapter.clearWhoCard();
			messageCenterListAdapter.notifyDataSetChanged();
			Util.hideSoftKeyboard(activity, this);
		}
	}

	public void clearComposingUi() {
		if (composingItem != null) {
			messages.remove(actionBarItem);
			messages.remove(composingItem);
			actionBarItem = null;
			composingItem = null;
			messageEditText = null;
			messageCenterListAdapter.clearComposing();
			messageCenterListAdapter.notifyDataSetChanged();
			Util.hideSoftKeyboard(activity, this);
		}
	}

	@Override
	public void onComposingViewCreated() {
		messageEditText = messageCenterListAdapter.getEditTextInComposing();
		if (composingViewSavedState != null) {
			messageEditText.onRestoreInstanceState(composingViewSavedState);
			composingViewSavedState = null;
		} else {
			String messageText = activity.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).
					getString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, null);
			if (messageText != null) {
				messageEditText.setText(messageText);
			} else {
				messageEditText.setText("");
			}
		}
	}

	@Override
	public void onWhoCardViewCreated(EditText nameEditText, EditText emailEditText) {
		if (pendingWhoCardName != null) {
			nameEditText.setText(pendingWhoCardName);
		} else {
			nameEditText.setText(PersonManager.loadPersonName(getContext()));
		}
		if (pendingWhoCardEmail != null) {
			emailEditText.setText(pendingWhoCardEmail);
		} else {
			emailEditText.setText(PersonManager.loadPersonEmail(getContext()));
		}
	}

	@Override
	public void onComposing(String composingStr, boolean scroll) {
	}

	@Override
	public void onCancelComposing() {
		if (contextualMessage != null) {
			messages.remove(contextualMessage);
			contextualMessage = null;
		}
		clearComposingUi();
		View fab = findViewById(R.id.composing_fab);
		fab.setVisibility(View.VISIBLE);
		View profileButton = findViewById(R.id.profile);
		profileButton.setVisibility(View.VISIBLE);
		showBranding();
		savePendingComposingMessage();
	}

	@Override
	public void onFinishComposing() {
		if (contextualMessage != null) {
			unsendMessagesCount++;
			MessageManager.sendMessage(activity.getApplicationContext(), contextualMessage);
			contextualMessage = null;
		}
		String messageText = getPendingComposingContent().toString().trim();
		// Close all composing UI
		onCancelComposing();
		// Send out the new message
		if (!messageText.isEmpty()) {
			OutgoingTextMessage message = new OutgoingTextMessage();
			message.setBody(messageText);
			message.setRead(true);
			message.setCustomData(customData);
			MessageManager.sendMessage(activity.getApplicationContext(), message);
			addNewOutGoingMessageItem(message);
			SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			boolean bWhoCardSet = prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, false);
			if (!bWhoCardSet) {
				addWhoCard();
				messageCenterViewHandler.sendEmptyMessage(MSG_SCROLL_TO_BOTTOM);
			}
		}
	}

	@Override
	public void onCloseWhoCard() {
		clearWhoCardUi();
		saveWhoCardSetState();
		View fab = findViewById(R.id.composing_fab);
		fab.setVisibility(View.VISIBLE);
		View profileButton = findViewById(R.id.profile);
		profileButton.setVisibility(View.VISIBLE);
		animateShow();
	}


	private void saveWhoCardSetState() {
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_SET, true);
		editor.commit();
	}


	public void scrollMessageListViewToBottomDelayed() {
		messageCenterViewHandler.sendEmptyMessageDelayed(MSG_SCROLL_TO_BOTTOM, 100);
	}

	// Retrieve the content from the composing area
	public Editable getPendingComposingContent() {
		return (messageEditText == null) ? null : messageEditText.getText();
	}

	/* When no composing view is presented in the list view, calling this method
	 * will clear the pending composing message previously saved in shared preference
	 */
	public void savePendingComposingMessage() {
		Editable content = getPendingComposingContent();
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, (content != null) ? content.toString().trim() : null);
		editor.commit();
	}

	public Parcelable onSaveListViewInstanceState() {
		return messageCenterListView.onSaveInstanceState();
	}

	public void onRestoreListViewInstanceState(Parcelable state) {
		messageCenterListView.onRestoreInstanceState(state);
	}

	public Parcelable onSaveEditTextInstanceState() {
		savePendingComposingMessage();
		if (messageEditText != null) {
			// Hide keyboard if the keyboard was up prior to rotation
			Util.hideSoftKeyboard(activity, this);
			return messageEditText.onSaveInstanceState();
		}
		return null;
	}

	public String onSaveWhoCardName() {
		return messageCenterListAdapter.getWhoCardName();
	}

	public String onSaveWhoCardEmail() {
		return messageCenterListAdapter.getWhoCardEmail();
	}

	public String onSaveWhoCardAvatar() {
		return messageCenterListAdapter.getWhoCardAvatarFileName();
	}

	// Return true if contextual message needs to be saved
	public boolean onSaveContextualMessage() {
		return (contextualMessage != null);
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

	private void showBranding() {
		if (messageCenterFooter != null) {
			messageCenterViewHandler.removeMessages(MSG_HIDE_BRANDING);
			messageCenterViewHandler.removeMessages(MSG_SHOW_BRANDING);
			messageCenterViewHandler.sendEmptyMessage(MSG_SHOW_BRANDING);
		}
	}

	private void hideBranding() {
		if (messageCenterFooter != null) {
			messageCenterViewHandler.removeMessages(MSG_HIDE_BRANDING);
			messageCenterViewHandler.removeMessages(MSG_SHOW_BRANDING);
			messageCenterViewHandler.sendEmptyMessage(MSG_HIDE_BRANDING);
		}
	}

	private void animateShow() {
		// Clear previous animations
		if (dismissAnimatorSet != null && dismissAnimatorSet.isRunning()) {
			dismissAnimatorSet.cancel();
		}
		if (showAnimatorSet == null || !showAnimatorSet.isRunning()) {
			showAnimatorSet = new AnimatorSet();
			// Bring the view back
			messageCenterFooter.setVisibility(View.VISIBLE);
			ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(messageCenterFooter, "translationY", messageCenterFooter.getTranslationY(), 0f);
			ArrayList<Animator> animators = new ArrayList<>();
			animators.add(footerAnimator);
			showAnimatorSet.setDuration(300);
			showAnimatorSet.playTogether(animators);
			showAnimatorSet.start();
		}
	}

	private void animateHide() {
		// Clear previous animations
		if (showAnimatorSet != null && showAnimatorSet.isRunning()) {
			showAnimatorSet.cancel();
		}
		if (dismissAnimatorSet == null || !dismissAnimatorSet.isRunning()) {
			dismissAnimatorSet = new AnimatorSet();
			ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(messageCenterFooter, "translationY",
					messageCenterFooter.getTranslationY(), messageCenterFooter.getHeight());
			ArrayList<Animator> animators = new ArrayList<>();
			animators.add(footerAnimator);
			dismissAnimatorSet.setDuration(200);
			dismissAnimatorSet.playTogether(animators);
			dismissAnimatorSet.start();
		}
	}

}
