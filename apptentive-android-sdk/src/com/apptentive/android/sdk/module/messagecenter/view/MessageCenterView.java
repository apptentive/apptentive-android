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
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.apptentive.android.sdk.AboutModule;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.TextMessage;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class MessageCenterView extends FrameLayout implements MessageManager.OnSentMessageListener {

	private final static int DRAWER_ANIMATION_DURATION = 250;

	private boolean keyboardUp = false;
	private static boolean drawerUp = false;

	private int drawerHeight;
	private int drawerHandleHeight;

	Activity context;
	static OnSendMessageListener onSendMessageListener;
	LinearLayout messageList;
	private Map<String, MessageView> messages;
	EditText messageEditText;

	public MessageCenterView(Activity context, OnSendMessageListener onSendMessageListener) {
		super(context);
		this.context = context;
		this.onSendMessageListener = onSendMessageListener;
		this.setId(R.id.apptentive_message_center_view);
		setup(); // TODO: Move this into a configurationchange handler?
	}

	protected void setup() {
		messages = new HashMap<String, MessageView>();

		LayoutInflater inflater = context.getLayoutInflater();
		inflater.inflate(R.layout.apptentive_message_center, this);

		messageList = (LinearLayout) findViewById(R.id.apptentive_message_center_list);
		messageEditText = (EditText) findViewById(R.id.apptentive_message_center_message);

		final LinearLayout drawer = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer);
		final LinearLayout drawerHandle = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer_handle);

		Drawable drawerBackground = drawer.getBackground();
		if(drawerBackground != null && drawerBackground instanceof BitmapDrawable) {
			BitmapDrawable drawerDrawable = new ZeroMinSizeDrawable(getResources(), (BitmapDrawable) drawerBackground);
			drawerDrawable.setTileModeX(Shader.TileMode.REPEAT);
			drawerDrawable.setTileModeY(Shader.TileMode.REPEAT);
			drawer.setBackgroundDrawable(drawerDrawable);
		}

		Button send = (Button) findViewById(R.id.apptentive_message_center_send);
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

		final View drawerOpen = findViewById(R.id.apptentive_message_center_drawer_open);
		final View drawerClose = findViewById(R.id.apptentive_message_center_drawer_close);

		drawerOpen.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Util.hideSoftKeyboard(context, messageEditText);
				positionDrawerWithAnimation(true);
			}
		});

		drawerClose.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				repositionTopArea(false); // Do this first on close so the background is already there when it closes.
				positionDrawerWithAnimation(false);
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
				context.startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
			}
		});

		// Need to perform a few things after the view has been laid out.
		post(new Runnable() {
			public void run() {
				if (drawerHeight == 0) {
					drawerHeight = drawer.getHeight();
				}
				if (drawerHandleHeight == 0) {
					drawerHandleHeight = drawerHandle.getHeight();
				}
				repositionDrawer(drawerUp);
			}
		});
	}

	private void positionDrawerWithAnimation(final boolean opening) {
		final LinearLayout drawer = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer);

		Animation animation;
		final int contentsHeight = drawerHeight - drawerHandleHeight;

		if (opening) {
			animation = new TranslateAnimation(0, 0, 0, -contentsHeight);
		} else {
			animation = new TranslateAnimation(0, 0, 0, contentsHeight);
		}
		animation.setDuration(DRAWER_ANIMATION_DURATION);
		animation.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				repositionDrawer(opening);
				// This dummy animation is important. For some reason, it fixes a flicker that would otherwise occur.
				animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
				animation.setDuration(1);
				drawer.startAnimation(animation);
			}

			public void onAnimationRepeat(Animation animation) {
			}
		});
		drawer.startAnimation(animation);
	}

	private void repositionTopArea(boolean openingDrawer) {
		View v = findViewById(R.id.apptentive_message_center_top_area);
		if (openingDrawer) {
			v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), drawerHeight);
		} else {
			v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), drawerHandleHeight);
		}
	}

	private void repositionDrawer(final boolean openingDrawer) {
		final LinearLayout drawer = (LinearLayout) findViewById(R.id.apptentive_message_center_drawer);
		final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) drawer.getLayoutParams();
		final View drawerOpen = findViewById(R.id.apptentive_message_center_drawer_open);
		final View drawerClose = findViewById(R.id.apptentive_message_center_drawer_close);

		final int contentsHeight = drawerHeight - drawerHandleHeight;
		if (openingDrawer) {
			params.setMargins(0, 0, 0, 0);
		} else {
			params.setMargins(0, 0, 0, -contentsHeight);
		}
		repositionTopArea(openingDrawer);
		drawerUp = openingDrawer;
		if (openingDrawer) {
			drawerClose.setVisibility(View.VISIBLE);
			drawerOpen.setVisibility(View.GONE);
		} else {
			drawerClose.setVisibility(View.GONE);
			drawerOpen.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		// We need to put this here because the listener is removed when the View is detached.
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				// Listen for the IME (soft keyboard) opening and closing.
				int heightDiff = getRootView().getHeight() - getHeight();
				if (heightDiff > 100) { // IME is shown now.
					if (!keyboardUp) { // If the keyboard is up when the change starts, don't hide the actionBar.
						keyboardUp = true;
						repositionDrawer(false);
					}
				} else { // IME is not shown now.
					if (keyboardUp) {
						keyboardUp = false;
						repositionDrawer(false);
					}
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
		this.messages = new HashMap<String, MessageView>(messages.size());
		for (Message message : messages) {
			addMessage(message);
		}
	}

	public void addMessage(Message message) {
		MessageView messageView = null;
		switch (message.getType()) {
			case TextMessage:
				messageView = new TextMessageView(context, (TextMessage) message);
				break;
			case FileMessage:
				break;
			case unknown:
				break;
			default:
				break;
		}
		if(messageView != null) {
			messageList.addView(messageView);
			messages.put(message.getNonce(), messageView);
		}
	}

	public interface OnSendMessageListener {
		void onSendTextMessage(String text);

		void onSendFileMessage(Uri uri);
	}

	@SuppressWarnings("unchecked") // We should never get a message passed in that is not appropriate for the view it goea into.
	public synchronized void onSentMessage(final Message message) {
		final MessageView messageView = messages.get(message.getNonce());
		messageView.post(new Runnable() {
			public void run() {
				messageView.updateMessage(message);
			}
		});
	}
}
