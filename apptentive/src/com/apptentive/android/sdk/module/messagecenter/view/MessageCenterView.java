/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.OnSentMessageListener {

	Activity activity;
	static OnSendMessageListener onSendMessageListener;
	ListView messageCenterListView;

	ArrayList<MessageCenterListItem> messages = new ArrayList<>();
	MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsendMessagesCount = 0;

	private MessageCenterStatus status;

	/**
	 * Used to save the state of the message text box if the user closes Message Center for a moment, attaches a file, etc.
	 */
	private static CharSequence messageText;

	EditText messageEditText;

	public MessageCenterView(Activity activity, OnSendMessageListener onSendMessageListener) {
		super(activity.getApplicationContext());
		this.activity = activity;
		MessageCenterView.onSendMessageListener = onSendMessageListener;
		this.setId(R.id.apptentive_message_center_view);
		setup(); // TODO: Move this into a configuration changed handler?
	}

	protected void setup() {
		LayoutInflater inflater = activity.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		// Hide branding if needed.
		final View branding = findViewById(R.id.apptentive_logo_view);
		if (branding != null) {
			if (Configuration.load(activity).isHideBranding(activity)) {
				branding.setVisibility(View.GONE);
			}
		}

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
		//messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		messageEditText = (EditText) findViewById(R.id.input);

		if (messageText != null) {
			messageEditText.setText(messageText);
			messageEditText.setSelection(messageText.length());
		}

		messageEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				messageText = editable.toString();
			}
		});

		View send = findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String text = messageEditText.getText().toString().trim();
				if (text.length() == 0) {
					return;
				}
				messageEditText.setText("");
				onSendMessageListener.onSendTextMessage(text);
				messageText = null;
			}
		});

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
		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(activity);
			unsendMessagesCount = countUnsendOutgoingMessages(items);
			messages.addAll(items);
			messageCenterListAdapter = new MessageAdapter<>(activity.getApplicationContext(), messages);
			messageCenterListView.setAdapter(messageCenterListAdapter);
		}
	}

	public int countUnsendOutgoingMessages(final List<MessageCenterListItem> items) {
		int count = 0;
		for (MessageCenterListItem item : items) {
			if (item instanceof Message) {
				Message message = (Message) item;
				if (message.isOutgoingMessage() && message.getCreatedAt() == null) {
					count ++;
				}
			}
		}
		return count;
	}

	public void setItems(final List<MessageCenterListItem> items) {
		messages.clear();
		messages.addAll(items);
		unsendMessagesCount = countUnsendOutgoingMessages(items);
		messageCenterListAdapter.notifyDataSetChanged();
	}


	public void addItem(MessageCenterListItem item) {

		if (item instanceof Message) {
			Message message = (Message) item;
			if (message.isHidden()) {
				return;
			}
		}

		if (item instanceof MessageCenterGreeting) {
			messages.add(0, item);
			messageCenterListAdapter.notifyDataSetChanged();
		} else {
			messages.remove(status);
			messages.add(item);
			if (!(item instanceof MessageCenterStatus)) {
				unsendMessagesCount++;
			}
			messageCenterListAdapter.notifyDataSetChanged();
		}

	}

	public static void showAttachmentDialog(Context context, final Uri data) {
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
					onSendMessageListener.onSendFileMessage(data);
				}
			});
			dialog.show();
		} catch (Exception e) {
			Log.e("Error loading attachment preview.", e);
		}
	}

	public interface OnSendMessageListener {
		void onSendTextMessage(String text);

		void onSendFileMessage(Uri uri);
	}

	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onSentMessage(ApptentiveHttpResponse response, final Message message) {
		if (response.isSuccessful()) {
			post(new Runnable() {
				public void run() {
					setItems(MessageManager.getMessageCenterListItems(activity));
					status = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_thank_you), null);
					addItem(status);
					messageCenterListView.setSelection(messageCenterListAdapter.getCount() - 1);
				}
			});
		}

	}

	public synchronized void onPause() {
		if (!isPaused) {
			isPaused = true;
			post(new Runnable() {
				public void run() {
					int messageCount = messages.size();
					MessageCenterListItem lastitem = messages.get(messageCount - 1);
					if (lastitem instanceof MessageCenterStatus) {
						messages.remove(messageCount - 1);
					}
					if (unsendMessagesCount > 0) {
						status = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_message_center_status_error_title), activity.getResources().getString(R.string.apptentive_message_center_status_error_body));
						addItem(status);
						messageCenterListAdapter.setPaused(isPaused);
						messageCenterListAdapter.notifyDataSetChanged();
						messageCenterListView.setSelection(messages.size() - 1);
					}
				}
			});
		}
	}

	public synchronized void onResume() {
		if (isPaused) {
			isPaused = false;
			post(new Runnable() {
				public void run() {
					messages.remove(status);
					if (unsendMessagesCount > 0) {
						messageCenterListAdapter.setPaused(isPaused);
						messageCenterListAdapter.notifyDataSetChanged();
					}

				}
			});
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
}
