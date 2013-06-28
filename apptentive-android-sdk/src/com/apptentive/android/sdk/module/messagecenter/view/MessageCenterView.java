/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import com.apptentive.android.sdk.AboutModule;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.ImageUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.OnSentMessageListener {

	Activity context;
	static OnSendMessageListener onSendMessageListener;
	ListView messageListView;
	MessageAdapter<Message> messageAdapter;

	EditText messageEditText;

	public MessageCenterView(Activity context, OnSendMessageListener onSendMessageListener) {
		super(context);
		this.context = context;
		this.onSendMessageListener = onSendMessageListener;
		this.setId(R.id.apptentive_message_center_view);
		setup(); // TODO: Move this into a configurationchange handler?
	}

	protected void setup() {
		LayoutInflater inflater = context.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		TextView titleTextView = (TextView) findViewById(R.id.apptentive_message_center_header_title);
		String titleText = Configuration.load(context).getMessageCenterTitle();
		if(titleText != null) {
			titleTextView.setText(titleText);
		}

		messageListView = (ListView) findViewById(R.id.apptentive_message_center_list);
		messageListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		messageEditText = (EditText) findViewById(R.id.apptentive_message_center_message);

		View send = findViewById(R.id.apptentive_message_center_send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String text = messageEditText.getText().toString().trim();
				if (text.length() == 0) {
					return;
				}
				messageEditText.setText("");
				onSendMessageListener.onSendTextMessage(text);
				Util.hideSoftKeyboard(context, view);
			}
		});

		View aboutApptentive = findViewById(R.id.apptentive_message_center_powered_by_apptentive);
		aboutApptentive.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				AboutModule.getInstance().show(context);
			}
		});

		View attachButton = findViewById(R.id.apptentive_message_center_attach_button);
		// Android devices can't take screenshots until version 4+
		boolean canTakeScreenshot = Build.VERSION.RELEASE.matches("^4.*");
		if(canTakeScreenshot) {
			attachButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					MetricModule.sendMetric(context, Event.EventLabel.message_center__attach);
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					context.startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
				}
			});
		} else {
			attachButton.setVisibility(GONE);
		}
		messageAdapter = new MessageAdapter<Message>(context);
		messageListView.setAdapter(messageAdapter);
	}

	public void setMessages(final List<Message> messages) {
		messageListView.post(new Runnable() {
			public void run() {
				messageAdapter.clear();
				for (Message message : messages) {
					addMessage(message);
				}
			}
		});
	}

	public void addMessage(Message message) {
		messageAdapter.add(message);
		messageListView.post(new Runnable() {
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
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		ImageView imageView = new ImageView(context);

		// Show a thumbnail version of the image.
		InputStream is = null;
		final Bitmap thumbnail;
		try {
			is = context.getContentResolver().openInputStream(data);
			thumbnail = ImageUtil.createLightweightScaledBitmapFromStream(is, 200, 300, null);
		} catch (FileNotFoundException e) {
			// TODO: Error toast?
			return;
		} finally {
			Util.ensureClosed(is);
		}
		if(thumbnail == null) {
			return;
		}

		imageView.setImageBitmap(thumbnail);
		dialog.setView(imageView);
		Resources resources = context.getResources();
		dialog.setTitle(resources.getString(R.string.apptentive_message_center_attachment_title));
		dialog.setButton(resources.getString(R.string.apptentive_yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Log.v("Yes, send attachment.");
				onSendMessageListener.onSendFileMessage(data);
			}
		});
		dialog.setButton2(resources.getString(R.string.apptentive_no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Log.v("Don't send attachment.");
			}
		});
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialogInterface) {
				thumbnail.recycle();
				System.gc();
			}
		});
		dialog.show();
	}

	public interface OnSendMessageListener {
		void onSendTextMessage(String text);

		void onSendFileMessage(Uri uri);
	}

	@SuppressWarnings("unchecked") // We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onSentMessage(final Message message) {
		setMessages(MessageManager.getMessages(context));
	}

	public void scrollMessageListViewToBottom() {
		messageListView.post(new Runnable() {
			public void run() {
				// Select the last row so it will scroll into view...
				messageListView.setSelection(messageAdapter.getCount() - 1);
			}
		});
	}
}
