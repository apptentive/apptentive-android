/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.view.*;
import android.widget.*;


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
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

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
	private Map<String, String> customData;

	private ArrayList<MessageCenterListItem> messages = new ArrayList<>();
	private MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsendMessagesCount = 0;


	private MessageCenterStatus statusItem;
	private MessageCenterComposingItem composingItem;
	private MessageCenterComposingItem actionBarItem;

	/**
	 * Used to save the state of the message text box if the user closes Message Center for a moment, attaches a file, etc.
	 */

	private EditText messageEditText;

	private Parcelable composingViewSavedState;

	public MessageCenterView(Activity activity, Map<String, String> customData, Parcelable editTextSavedState) {
		super(activity.getApplicationContext());
		this.activity = activity;
		this.customData = customData;
		this.setId(R.id.apptentive_message_center_view);
		composingViewSavedState = editTextSavedState;
		setup(); // TODO: Move this into a configuration changed handler?
	}

	protected void setup() {
		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		// Hide branding if needed.
		/*final View branding = findViewById(R.id.apptentive_logo_view);
		if (branding != null) {
			if (Configuration.load(activity).isHideBranding(activity)) {
				branding.setVisibility(View.GONE);
			}
		}*/

		ImageButton back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});

		TextView titleTextView = (TextView) findViewById(R.id.title);
		String titleText = Configuration.load(activity).getMessageCenterTitle();
		if (titleText != null) {
			titleTextView.setText(titleText);
		}

		messageCenterListView = (ListView) findViewById(R.id.message_list);
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);


		messageCenterListView.setItemsCanFocus(true);

		View fab = findViewById(R.id.composing_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addComposingArea();
				messageCenterListAdapter.notifyDataSetChanged();
				scrollMessageListViewToBottom();
			}
		});

		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(activity);
			unsendMessagesCount = countUnsendOutgoingMessages(items);
			messages.addAll(items);
			if (composingViewSavedState != null) {
				addComposingArea();
			}
			messageCenterListAdapter = new MessageAdapter<>(activity, messages, this);
			messageCenterListView.setAdapter(messageCenterListAdapter);
			messagesUpdated(); // Force timestamp recompilation.
		}


		View attachButton = findViewById(R.id.attach);
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

		//scrollMessageListViewToBottom();
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

	public void addComposingArea() {
		View fab = findViewById(R.id.composing_fab);
		fab.setVisibility(View.INVISIBLE);
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}
		actionBarItem = new MessageCenterComposingItem(MessageCenterComposingItem.COMPOSING_ITEM_ACTIONBAR);
		messages.add(actionBarItem);
		composingItem = new MessageCenterComposingItem(MessageCenterComposingItem.COMPOSING_ITEM_AREA);
		messages.add(composingItem);
	}

	public void addNewStatusItem(MessageCenterListItem item) {
		// Remove the exisiting status item in the list
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}

		if (composingItem != null) {
			return;
		}

		statusItem = (MessageCenterStatus) item;
		messages.add(item);
		messageCenterListAdapter.notifyDataSetChanged();

		scrollMessageListViewToBottom();
	}

	public void addNewOutGoingMessageItem(ApptentiveMessage message) {
		// Remove the status message whenever a new outgoing message is added
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}

		messages.add(message);
		unsendMessagesCount++;

		isPaused = false;
		messageCenterListAdapter.setPaused(isPaused);

		messageCenterListAdapter.notifyDataSetChanged();
		scrollMessageListViewToBottom();
	}

	public void addNewIncomingMessageItem(ApptentiveMessage message) {
		// Remove the status message whenever a new incoming message is added
		if (statusItem != null) {
			messages.remove(statusItem);
			statusItem = null;
		}
		int composingAreaIndex = 0;
		if (composingItem != null) {
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
			messagesUpdated();
			// Restore the position of listview to composing view
			messageCenterListView.setSelectionFromTop(composingAreaIndex, top);
		} else {
			messagesUpdated();
			scrollMessageListViewToBottom();
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
			post(new Runnable() {
				public void run() {
					unsendMessagesCount--;
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
					messagesUpdated();
					MessageCenterStatus newItem = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_thank_you), null);
					addNewStatusItem(newItem);
				}
			});
		}

	}

	public synchronized void onPauseSending() {
		if (!isPaused) {
			isPaused = true;
			post(new Runnable() {
				public void run() {
					if (unsendMessagesCount > 0) {
						messageCenterListAdapter.setPaused(isPaused);
						MessageCenterStatus newItem = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_message_center_status_error_title
						), activity.getResources().getString(R.string.apptentive_message_center_status_error_body));
						addNewStatusItem(newItem);
					}
				}
			});
		}
	}

	public synchronized void onResumeSending() {
		if (isPaused) {
			isPaused = false;
			post(new Runnable() {
				public void run() {
					if (statusItem != null) {
						messages.remove(statusItem);
						statusItem = null;
					}
					if (unsendMessagesCount > 0) {
						messageCenterListAdapter.setPaused(isPaused);
						messageCenterListAdapter.notifyDataSetChanged();
					}

				}
			});
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
			View fab = findViewById(R.id.composing_fab);
			fab.setVisibility(View.VISIBLE);
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
	public void onComposing(String composingStr, boolean scroll) {
	}

	@Override
	public void onCancelComposing() {
		clearComposingUi();
		savePendingComposingMessage();
	}

	@Override
	public void onFinishComposing() {
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
		}
	}

	public void scrollMessageListViewToBottom() {
		post(new Runnable() {
			public void run() {
				// Select the last row so it will scroll into view...
				messageCenterListView.setSelection(messages.size() - 1);
			}
		});
	}

	// Retrieve the content from the composing area
	public Editable getPendingComposingContent() {
		return (messageEditText == null) ? null : messageEditText.getText();
	}

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
		return (messageEditText == null) ? null : messageEditText.onSaveInstanceState();
	}


	Set<String> dateStampsSeen = new HashSet<>();
	public void messagesUpdated() {

		dateStampsSeen.clear();
		for (MessageCenterListItem message : messages) {
			if (message instanceof ApptentiveMessage && !(message instanceof AutomatedMessage)) {
				ApptentiveMessage apptentiveMessage = (ApptentiveMessage) message;
				Double clientCreatedAt = apptentiveMessage.getClientCreatedAt();
				String dateStamp = createDatestamp(clientCreatedAt);
				if (dateStamp != null) {
					if (dateStampsSeen.add(dateStamp)) {
						apptentiveMessage.setDatestamp(dateStamp);
					} else {
						apptentiveMessage.clearDatestamp();
					}
				}
			}
		}
		messageCenterListAdapter.notifyDataSetChanged();
	}

	protected String createDatestamp(Double seconds) {
		if (seconds != null) {
			Date date = new Date(Math.round(seconds * 1000));
			DateFormat mediumDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return mediumDateFormat.format(date);
		}
		return null;
	}
}
