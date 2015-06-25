/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;

import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;

import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;

import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.OutgoingFileMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.ImageUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.AfterSendMessageListener, MessageManager.OnComposingActionListener{

	private Activity activity;
	private OnSendMessageListener onSendMessageListener;
	private ListView messageCenterListView;

	private ArrayList<MessageCenterListItem> messages = new ArrayList<>();
	private MessageAdapter<MessageCenterListItem> messageCenterListAdapter;

	// MesssageCenterView is set to paused when it fails to send message
	private boolean isPaused = false;
	// Count how many paused ongoing messages
	private int unsendMessagesCount = 0;


	private MessageCenterStatus statusItem;
	private MessageCenterComposingItem composingItem;

	/**
	 * Used to save the state of the message text box if the user closes Message Center for a moment, attaches a file, etc.
	 */
	private CharSequence messageText;

	private EditText messageEditText;

	public MessageCenterView(Activity activity, OnSendMessageListener listener) {
		super(activity.getApplicationContext());
		this.activity = activity;
		this.onSendMessageListener = listener;
		this.setId(R.id.apptentive_message_center_view);
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
		messageCenterListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);


		messageCenterListView.setItemsCanFocus(true);

		messageText = activity.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).
								getString(Constants.PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE, null);

		if (messageCenterListAdapter == null) {
			List<MessageCenterListItem> items = MessageManager.getMessageCenterListItems(activity);
			unsendMessagesCount = countUnsendOutgoingMessages(items);
			messages.addAll(items);
			messageCenterListAdapter = new MessageAdapter<>(activity.getApplicationContext(), messages);
			messageCenterListView.setAdapter(messageCenterListAdapter);
		}


		View fab = findViewById(R.id.composing_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View fab = findViewById(R.id.composing_fab);
				fab.setVisibility(View.INVISIBLE);
				composingItem = new MessageCenterComposingItem(null);
				addItem(composingItem);
				post(new Runnable() {
					public void run() {
						messageEditText = messageCenterListAdapter.getEditTextInComposing();
						messageEditText.setText(messageText);
						messageEditText.requestFocus();
						InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(messageEditText, 0);
					}
				});
			}
		});

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
			messages.remove(statusItem);
			messages.add(item);
			if (item instanceof Message )
			{
				unsendMessagesCount++;
			}
			messageCenterListAdapter.notifyDataSetChanged();
		}

	}

	private Bitmap resizeImageForImageView(String imagePath) {
		Bitmap resizedBitmap = null;

		FileInputStream fis = null;
		try {
			fis = activity.openFileInput(imagePath);
			Point point = Util.getScreenSize(activity);
			int maxImageWidth = (int) (0.4 * point.x);
			int maxImageHeight = (int) (0.4 * point.x);
			maxImageWidth = maxImageWidth > 400 ? 400 : maxImageWidth;
			maxImageHeight = maxImageHeight > 400 ? 400 : maxImageHeight;
			resizedBitmap = ImageUtil.createScaledBitmapFromStream(fis, maxImageWidth, maxImageHeight, null, 0);
		} catch (Exception e) {
			Log.e("Error opening stored image.", e);
		} catch (OutOfMemoryError e) {
			// It's generally not a good idea to catch an OutOfMemoryException. But in this case, the OutOfMemoryException
			// had to result from allocating a bitmap, so the system should be in a good state.
			// TODO: Log an event to the server so we know an OutOfMemoryException occurred.
			Log.e("Ran out of memory opening image.", e);
		} finally {
			Util.ensureClosed(fis);
		}

		return resizedBitmap;
	}

	public void appendImageAfterCursor(final Uri uri) {
		if (uri == null) {
			Log.d("No attachment found.");
			return;
		}

		final OutgoingFileMessage message = new OutgoingFileMessage();
		boolean successful = message.internalCreateStoredImage(activity.getApplicationContext(), uri.toString());
		if (successful) {
			StoredFile storedFile = message.getStoredFile(activity);
			String mimeType = storedFile.getMimeType();
			String imagePath;

			if (mimeType != null) {
				imagePath = storedFile.getLocalFilePath();
				if (mimeType.contains("image")) {
					SpannableString ss = new SpannableString(imagePath);
					ImageSpan span = new ImageSpan(activity, resizeImageForImageView(imagePath));
					ss.setSpan(span, 0, imagePath.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					EditText editText = messageCenterListAdapter.getEditTextInComposing();
					editText.setGravity(Gravity.CENTER);
					Editable editable = editText.getText();
					int start = editText.getSelectionStart();
					// Insert a new line before the image if only if there are existing text
					if (start > 0) {
						editable.insert(start, "\n");
						start++;
					}

					editable.insert(start, ss);
					start+=ss.length();
					// Insert a new line after the image
					editable.insert(start, "\n");
					start++;
					editText.setText(editable);
					editText.setSelection(start);
					Util.showSoftKeyboard(activity, editText);
					//editText.setGravity(Gravity.LEFT);
				}
			}
		} else {
			Log.e("Unable to attach image.");
			Toast.makeText(activity, "Unable to send file.", Toast.LENGTH_SHORT).show();
		}
	}

	public interface OnSendMessageListener {
		void onSendMessage(String message);
	}

	@SuppressWarnings("unchecked")
	// We should never get a message passed in that is not appropriate for the view it goes into.
	public synchronized void onMessageSent(ApptentiveHttpResponse response, final Message message) {
		if (response.isSuccessful()) {
			post(new Runnable() {
				public void run() {setItems(MessageManager.getMessageCenterListItems(activity));
					statusItem = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_thank_you), null);
					addItem(statusItem);
				}
			});
		}

	}

	public synchronized void onPauseSending() {
		if (!isPaused) {
			isPaused = true;
			post(new Runnable() {
				public void run() {
					if (statusItem != null) {
						messages.remove(statusItem);
						statusItem = null;
					}
					if (unsendMessagesCount > 0) {
						messageCenterListAdapter.setPaused(isPaused);
						statusItem = new MessageCenterStatus(MessageCenterStatus.STATUS_CONFIRMATION, activity.getResources().getString(R.string.apptentive_message_center_status_error_title
						), activity.getResources().getString(R.string.apptentive_message_center_status_error_body));
						addItem(statusItem);
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

	@Override
	public void onComposing(String composingStr, boolean scroll) {
		messageText = composingStr;
		if (scroll) {
			post(new Runnable() {
				public void run() {
					messageCenterListAdapter.notifyDataSetChanged();
					//.smoothScrollToPositionFromTop(messages.size() - 1, messageCenterListView.getHeight());
				}
			});
		}
	}

	@Override
	public void onCancelComposing() {
		if (composingItem != null) {
			messages.remove(composingItem);
			composingItem = null;
			messageCenterListAdapter.clearComposing();
			messageCenterListAdapter.notifyDataSetChanged();
			Util.hideSoftKeyboard(activity, this);
			View fab = findViewById(R.id.composing_fab);
			fab.setVisibility(View.VISIBLE);

		}
	}

	@Override
	public void onFinishComposing() {
		onCancelComposing();
		if (messageText != null) {
			onSendMessageListener.onSendMessage(messageText.toString());
		}
		messageText = null;
	}

	@Override
	public void onAttachImage() {
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
			return messageEditText.getText();
		}
}
