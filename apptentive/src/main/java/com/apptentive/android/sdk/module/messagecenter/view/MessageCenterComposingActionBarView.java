/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.view.ApptentiveAlertDialog;

import java.lang.ref.WeakReference;


public class MessageCenterComposingActionBarView extends FrameLayout implements MessageCenterListItemView {

	public boolean showConfirmation = false;
	public ImageButton sendButton;
	public ImageButton attachButton;
	private WeakReference<MessageAdapter.OnListviewItemActionListener> listenerRef;

	public MessageCenterComposingActionBarView(final Fragment fragment, final MessageCenterComposingItem item, final MessageAdapter.OnListviewItemActionListener listener) {
		super(fragment.getContext());
		this.listenerRef = new WeakReference<MessageAdapter.OnListviewItemActionListener>(listener);

		LayoutInflater inflater = fragment.getActivity().getLayoutInflater();

		try {
			inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);
		} catch (Exception e) {
			ApptentiveLog.e("Error:", e);
		}

		ColorStateList colors = ContextCompat.getColorStateList(getContext(), Util.getResourceIdFromAttribute(getContext().getTheme(), R.attr.apptentiveButtonTintColorStateList));

		ImageButton closeButton = (ImageButton) findViewById(R.id.cancel_composing);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable closeButtonDrawable = DrawableCompat.wrap(closeButton.getDrawable());
		DrawableCompat.setTintList(closeButtonDrawable, colors);
		closeButton.setImageDrawable(closeButtonDrawable);

		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MessageAdapter.OnListviewItemActionListener locallistener = listenerRef.get();
				if (locallistener == null) {
					return;
				}
				if (showConfirmation) {
					Bundle bundle = new Bundle();
					bundle.putString("message", item.str_2);
					bundle.putString("positive", item.str_3);
					bundle.putString("negative", item.str_4);
					ApptentiveAlertDialog.show(fragment, bundle, Constants.REQUEST_CODE_CLOSE_COMPOSING_CONFIRMATION);
				} else {
					locallistener.onCancelComposing();
				}
			}
		});

		TextView composing = (TextView) findViewById(R.id.composing);

		if (item.str_1 != null) {
			composing.setText(item.str_1);
		}

		sendButton = (ImageButton) findViewById(R.id.btn_send_message);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable sendButtonDrawable = DrawableCompat.wrap(sendButton.getDrawable());
		DrawableCompat.setTintList(sendButtonDrawable, colors);
		sendButton.setImageDrawable(sendButtonDrawable);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			sendButton.setColorFilter(Util.getThemeColor(fragment.getContext(), R.attr.apptentiveButtonTintColorDisabled));
		}
		sendButton.setEnabled(false);
		if (item.button_1 != null) {
			sendButton.setContentDescription(item.button_1);
		}
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MessageAdapter.OnListviewItemActionListener locallistener = listenerRef.get();
				if (locallistener == null) {
					return;
				}
				locallistener.onFinishComposing();
			}
		});

		attachButton = (ImageButton) findViewById(R.id.btn_attach_image);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable attachButtonDrawable = DrawableCompat.wrap(attachButton.getDrawable());
		DrawableCompat.setTintList(attachButtonDrawable, colors);
		attachButton.setImageDrawable(attachButtonDrawable);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			attachButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					MessageAdapter.OnListviewItemActionListener locallistener = listenerRef.get();
					if (locallistener == null) {
						return;
					}
					locallistener.onAttachImage();
				}
			});
		} else {
			attachButton.setVisibility(GONE);
		}
	}


}