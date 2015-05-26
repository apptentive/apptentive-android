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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.OnSentMessageListener {

	Activity activity;
	static OnSendMessageListener onSendMessageListener;
	ListView messageCenterListView;
	MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

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
/*
		final View branding = findViewById(R.id.apptentive_branding_view);
		if (branding != null) {
			if (Configuration.load(context).isHideBranding(context)) {
				branding.setVisibility(View.GONE);
			} else {
				branding.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						AboutModule.getInstance().show(context);
					}
				});
			}
		}
*/

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
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

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
				Util.hideSoftKeyboard(activity, view);
			}
		});

		View attachButton = findViewById(R.id.attach);
		// Android devices can't take screenshots until Android OS version 4+
		boolean canTakeScreenshot = Util.getMajorOsVersion() >= 4;
		if (canTakeScreenshot) {
			attachButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					MetricModule.sendMetric(activity, Event.EventLabel.message_center__attach);
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image");
					activity.startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
				}
			});
		} else {
			attachButton.setVisibility(GONE);
		}

		messageCenterListAdapter = new MessageAdapter<MessageCenterListItem>(activity);
		messageCenterListView.setAdapter(messageCenterListAdapter);
	}

	public void setItems(final List<MessageCenterListItem> items) {
		messageCenterListView.post(new Runnable() {
			public void run() {
				messageCenterListAdapter.clear();
				for (MessageCenterListItem item : items) {
					if (item instanceof Message) {
						Message message = (Message) item;
						if (message.isHidden()) {
							continue;
						}
					}
					messageCenterListAdapter.add(item);
				}
			}
		});
	}

	public void addItem(MessageCenterListItem item) {

		if (item instanceof Message) {
			Message message = (Message) item;
			if (message.isHidden()) {
				return;
			}
		}

		if (item instanceof MessageCenterGreeting) {
			messageCenterListAdapter.insert(item, 0);
			//messageCenterListAdapter.notifyDataSetChanged();
		} else {
			messageCenterListAdapter.add(item);
		}

		messageCenterListView.post(new Runnable() {
			public void run() {
				scrollMessageListViewToBottom();
			}
		});
	}

	public static void showAttachmentDialog(Context context, final Uri data) {
		if (data == null) {
			Log.d("No attachment found.");
			return;
		}
		AttachmentPreviewDialog dialog = new AttachmentPreviewDialog(context);
		dialog.setImage(data);
		dialog.setOnAttachmentAcceptedListener(new AttachmentPreviewDialog.OnAttachmentAcceptedListener() {
			@Override
			public void onAttachmentAccepted() {
				onSendMessageListener.onSendFileMessage(data);
			}
		});
		dialog.show();
	}

	public interface OnSendMessageListener {
		void onSendTextMessage(String text);

		void onSendFileMessage(Uri uri);
	}

	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onSentMessage(final Message message) {
		setItems(MessageManager.getMessageCenterListItems(activity));
	}

	public void scrollMessageListViewToBottom() {
		messageCenterListView.post(new Runnable() {
			public void run() {
				// Select the last row so it will scroll into view...
				messageCenterListView.setSelection(messageCenterListAdapter.getCount() - 1);
			}
		});
	}
}
