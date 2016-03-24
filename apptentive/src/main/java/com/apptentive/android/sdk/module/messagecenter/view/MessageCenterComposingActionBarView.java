/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;


import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.Util;

import java.lang.ref.WeakReference;


public class MessageCenterComposingActionBarView extends FrameLayout implements MessageCenterListItemView {

	public boolean showConfirmation = false;
	public ImageButton sendButton;
	public ImageButton attachButton;
	private WeakReference<MessageAdapter.OnListviewItemActionListener> listenerRef;

	public MessageCenterComposingActionBarView(final Fragment fragment, final MessageCenterComposingItem item, final MessageAdapter.OnListviewItemActionListener listener) {
		super(fragment.getContext());
		this.listenerRef = new WeakReference<MessageAdapter.OnListviewItemActionListener>(listener);

		final Context contextThemeWrapper = new ContextThemeWrapper(fragment.getContext(), ApptentiveInternal.getInstance().getApptentiveTheme());
		// clone the inflater using the ContextThemeWrapper

		LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);

		try {
			inflater.inflate(R.layout.apptentive_message_center_composing_actionbar, this);
		} catch (Exception e) {
			ApptentiveLog.e("Error:", e);
		}

		Resources.Theme theme = contextThemeWrapper.getTheme();
		ColorStateList colors = getResources().getColorStateList(Util.getResourceIdFromAttribute(theme, R.attr.apptentiveButtonTintColorStateList));


		ImageButton closeButton = (ImageButton) findViewById(R.id.cancel_composing);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply color manually.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Drawable d = DrawableCompat.wrap(closeButton.getDrawable());
			DrawableCompat.setTintList(d, colors);
			d.applyTheme(theme);
			closeButton.setImageDrawable(d);
		}
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MessageAdapter.OnListviewItemActionListener locallistener = listenerRef.get();
				if (locallistener == null) {
					return;
				}
				if (showConfirmation) {
					CloseConfirmationDialog closeComposingDialog = new CloseConfirmationDialog();
					closeComposingDialog.setTargetFragment(fragment, 0);
					Bundle bundle = new Bundle();
					bundle.putString("STR_2", item.str_2);
					bundle.putString("STR_3", item.str_3);
					bundle.putString("STR_4", item.str_4);
					closeComposingDialog.setArguments(bundle);
					closeComposingDialog.show(fragment.getFragmentManager(), "CloseConfirmationDialog");
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
		sendButton.setEnabled(false);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply color manually.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Drawable d = DrawableCompat.wrap(sendButton.getDrawable());
			DrawableCompat.setTintList(d, colors);
			d.applyTheme(theme);
			sendButton.setImageDrawable(d);
		} else {
			sendButton.setColorFilter(Util.getThemeColor(fragment.getContext(), R.attr.apptentiveButtonTintColorDisabled));
		}
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
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply color manually.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Drawable d = DrawableCompat.wrap(attachButton.getDrawable());
			DrawableCompat.setTintList(d, colors);
			d.applyTheme(theme);
			attachButton.setImageDrawable(d);
		}
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

	public static class CloseConfirmationDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final AlertDialog d = new AlertDialog.Builder(getActivity())
					.setMessage(getArguments().getString("STR_2"))
					.setPositiveButton(getArguments().getString("STR_3"),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Fragment fragment = getTargetFragment();
									if (fragment instanceof MessageAdapter.OnListviewItemActionListener) {
										((MessageAdapter.OnListviewItemActionListener) fragment).onCancelComposing();
									}
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
			/*d.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					Button b = d.getButton(DialogInterface.BUTTON_POSITIVE);
					b.setTextColor(Util.getThemeColorFromAttrOrRes(getActivity(), R.attr.colorAccent, R.color.apptentive_material_accent));
				}
			});*/
			return d;
		}

	}

}