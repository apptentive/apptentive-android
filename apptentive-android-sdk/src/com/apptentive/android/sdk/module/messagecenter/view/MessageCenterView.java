/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.apptentive.android.sdk.AboutModule;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout {

	private boolean keyboardUp = false;
	private static boolean drawerUp = false;

	Context context;
	static OnSendMessageListener onSendMessageListener;
	LinearLayout messageList;
	private List<Message> messages;
	EditText messageEditText;

	public MessageCenterView(Context context, OnSendMessageListener onSendMessageListener) {
		super(context);
		this.context = context;
		this.onSendMessageListener = onSendMessageListener;
		this.setId(R.id.apptentive_message_center_view);
		setup();
	}

	protected void setup() {
		if (!(context instanceof Activity)) {
			Log.e(this.getClass().getSimpleName() + " must be initialized with an Activity Context.");
			return;
		}
		messages = new ArrayList<Message>();

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		messageList = (LinearLayout) findViewById(R.id.aptentive_message_center_list);
		messageEditText = (EditText) findViewById(R.id.apptentive_message_center_message);

		// This is a hack because the tile is bigger than the size of the collapsed drawer view.
		final LinearLayout drawer = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer);
		BitmapDrawable drawerDrawable = new ZeroMinSizeDrawable(getResources(), R.drawable.apptentive_grey_denim);
		drawerDrawable.setTileModeX(Shader.TileMode.REPEAT);
		drawerDrawable.setTileModeY(Shader.TileMode.REPEAT);
		drawer.setBackgroundDrawable(drawerDrawable);

		final LinearLayout drawerContents = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer_contents);

		Button send = (Button) findViewById(R.id.apptentive_message_center_send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String text = messageEditText.getText().toString().trim();
				if (text.length() == 0) {
					return;
				}
				messageEditText.setText("");
				Util.hideSoftKeyboard((Activity) context, messageEditText);
				onSendMessageListener.onSendTextMessage(text);
			}
		});

		final View drawerOpen = findViewById(R.id.apptentive_message_center_drawer_open);
		final View drawerClose = findViewById(R.id.apptentive_message_center_drawer_close);

		drawerOpen.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				openDrawer();
			}
		});

		drawerClose.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				closeDrawer();
			}
		});

		View aboutApptentive = findViewById(R.id.apptentive_message_center_powered_by_apptentive);
		aboutApptentive.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				AboutModule.getInstance().show(context);
			}
		});

		View screenshotButton = findViewById(R.id.apptentive_message_center_button_screenshot);
		screenshotButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
			}
		});

		if(drawerUp) {
			openDrawer();
		} else {
			closeDrawer();
		}
	}

	private void openDrawer() {
		View drawerOpen = findViewById(R.id.apptentive_message_center_drawer_open);
		View drawerClose = findViewById(R.id.apptentive_message_center_drawer_close);
		LinearLayout drawerContents = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer_contents);
		Util.hideSoftKeyboard((Activity) context, messageEditText);
		drawerContents.setVisibility(View.VISIBLE);
		drawerClose.setVisibility(View.VISIBLE);
		drawerOpen.setVisibility(View.GONE);
		drawerUp = true;
	}

	private void closeDrawer() {
		View drawerOpen = findViewById(R.id.apptentive_message_center_drawer_open);
		View drawerClose = findViewById(R.id.apptentive_message_center_drawer_close);
		LinearLayout drawerContents = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer_contents);
		drawerContents.setVisibility(View.GONE);
		drawerClose.setVisibility(View.GONE);
		drawerOpen.setVisibility(View.VISIBLE);
		drawerUp = false;
	}


	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		// We need to put this here because the listener is removed when the View is detached.
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				// Listen for the keyboard opening and closing.
				int heightDiff = getRootView().getHeight() - getHeight();
				if (heightDiff > 100) {
					if (!keyboardUp) { // If the keyboard is up when the change starts, don't hide the actionBar.
						keyboardUp = true;
						View drawer = findViewById(R.id.apptentive_message_center_drawer_contents);
						if (drawer != null) {
							closeDrawer();
						}
					}
				} else {
					keyboardUp = false;
				}
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
		imageView.setImageURI(data);
		dialog.setView(imageView);
		dialog.setTitle("Send attachment?");
		dialog.setButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Log.v("Yes, send attachment.");
				onSendMessageListener.onSendFileMessage(data);
			}
		});
		dialog.setButton2("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Log.v("Don't send attachment.");
			}
		});
		dialog.show();
	}

	public void setMessages(List<Message> messages) {
		messageList.removeAllViews();
		this.messages = new ArrayList<Message>();
		for (Message message : messages) {
			addMessage(message);
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
		switch (message.getTypeEnum()) {
			case text_message:
				messageList.addView(new TextMessageView(context, (TextMessage) message));
				break;
			case upgrade_request:
				break;
			case share_request:
				break;
			case unknown:
				break;
			default:
				break;
		}
	}

	public interface OnSendMessageListener {
		void onSendTextMessage(String text);
		void onSendFileMessage(Uri uri);
	}
}
