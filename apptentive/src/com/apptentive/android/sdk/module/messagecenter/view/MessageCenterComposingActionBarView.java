/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterComposingActionBarView extends FrameLayout implements MessageCenterListItemView {

	public boolean showConfirmation = false;
	public ImageButton sendButton;

	public MessageCenterComposingActionBarView(final Context activityContext, final MessageCenterComposingItem item, final MessageAdapter.OnComposingActionListener listener) {
		super(activityContext);

		LayoutInflater inflater = LayoutInflater.from(activityContext);
		inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);

		View closeButton = findViewById(R.id.cancel_composing);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (showConfirmation) {
					CloseConfirmationDialog editNameDialog = new CloseConfirmationDialog();
					editNameDialog.listener = listener;
					Bundle bundle = new Bundle();
					bundle.putString("STR_2", item.str_2);
					bundle.putString("STR_3", item.str_3);
					bundle.putString("STR_4", item.str_4);
					editNameDialog.setArguments(bundle);
					editNameDialog.show(((Activity) activityContext).getFragmentManager(), "CloseConfirmationDialog");
				} else {
					listener.onCancelComposing();
				}
			}
		});

		TextView composing = (TextView) findViewById(R.id.composing);

		if (item.str_1 != null) {
			composing.setText(item.str_1);
		}

		sendButton = (ImageButton) findViewById(R.id.btn_send_message);
		if (item.button_1 != null) {
			sendButton.setContentDescription(item.button_1);
		}
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				listener.onFinishComposing();
			}
		});

		sendButton.setEnabled(false);
		sendButton.setColorFilter(Util.getThemeColorFromAttrOrRes(activityContext, R.attr.apptentive_material_disabled_icon,
				R.color.apptentive_material_dark_disabled_icon));
	}

	public static class CloseConfirmationDialog extends DialogFragment {
		public MessageAdapter.OnComposingActionListener listener;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage(getArguments().getString("STR_2"))
					.setPositiveButton(getArguments().getString("STR_3"),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									listener.onCancelComposing();
									dialog.dismiss();
								}
							})
					.setNegativeButton(getArguments().getString("STR_4"),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).create();
		}

	}

}