/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.messagecenter.model.Composer;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerViewAdapter;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.view.ApptentiveAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class MessageComposerHolder extends RecyclerView.ViewHolder {

	private List<ImageItem> images;

	private ImageButton closeButton;
	private TextView title;
	private ImageButton attachButton;
	private ImageButton sendButton;
	public EditText message;
	private ApptentiveImageGridView attachments;

	private TextWatcher textWatcher;

	public MessageComposerHolder(View itemView) {
		super(itemView);
		images = new ArrayList<ImageItem>();
		closeButton = (ImageButton) itemView.findViewById(R.id.cancel_composing);
		title = (TextView) itemView.findViewById(R.id.title);
		attachButton = (ImageButton) itemView.findViewById(R.id.btn_attach_image);
		sendButton = (ImageButton) itemView.findViewById(R.id.btn_send_message);
		message = (EditText) itemView.findViewById(R.id.composing_et);
		attachments = (ApptentiveImageGridView) itemView.findViewById(R.id.grid);
	}

	public void bindView(final MessageCenterFragment fragment, final MessageCenterRecyclerViewAdapter adapter, final Composer composer) {
		ApptentiveLog.e("BINDING IMAGE");
		title.setText(composer.title);

		ColorStateList colors = ContextCompat.getColorStateList(itemView.getContext(), Util.getResourceIdFromAttribute(itemView.getContext().getTheme(), R.attr.apptentiveButtonTintColorStateList));

		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable closeButtonDrawable = DrawableCompat.wrap(closeButton.getDrawable());
		DrawableCompat.setTintList(closeButtonDrawable, colors);
		closeButton.setImageDrawable(closeButtonDrawable);

		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!TextUtils.isEmpty(message.getText())) {
					Bundle bundle = new Bundle();
					bundle.putString("message", composer.closeBody);
					bundle.putString("positive", composer.closeDiscard);
					bundle.putString("negative", composer.closeCancel);
					ApptentiveAlertDialog.show(fragment, bundle, Constants.REQUEST_CODE_CLOSE_COMPOSING_CONFIRMATION);
				} else {
					if (adapter.getListener() != null) {
						adapter.getListener().onCancelComposing();
					}
				}
			}
		});

		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable sendButtonDrawable = DrawableCompat.wrap(sendButton.getDrawable());
		DrawableCompat.setTintList(sendButtonDrawable, colors);
		sendButton.setImageDrawable(sendButtonDrawable);
		sendButton.setContentDescription(composer.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (adapter.getListener() != null) {
					adapter.getListener().onFinishComposing();
				}
			}
		});

		message.setHint(composer.messageHint);

		message.removeTextChangedListener(textWatcher);
		textWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				if (adapter.getListener() != null) {
					adapter.getListener().beforeComposingTextChanged(charSequence);
				}
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				if (adapter.getListener() != null) {
					adapter.getListener().onComposingTextChanged(charSequence);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (adapter.getListener() != null) {
					adapter.getListener().afterComposingTextChanged(editable.toString());
				}
				Linkify.addLinks(editable, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.MAP_ADDRESSES);
			}
		};
		message.addTextChangedListener(textWatcher);


		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable attachButtonDrawable = DrawableCompat.wrap(attachButton.getDrawable());
		DrawableCompat.setTintList(attachButtonDrawable, colors);
		attachButton.setImageDrawable(attachButtonDrawable);

		attachButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (adapter.getListener() != null) {
					adapter.getListener().onAttachImage();
				}
			}
		});


		attachments.setupUi();
		attachments.setupLayoutListener();
		attachments.setListener(new ApptentiveImageGridView.ImageItemClickedListener() {
			@Override
			public void onClick(int position, ImageItem image) {
				if (adapter.getListener() != null) {
					adapter.getListener().onClickAttachment(position, image);
				}
			}
		});
		attachments.setAdapterIndicator(R.drawable.apptentive_ic_remove_attachment);

		attachments.setImageIndicatorCallback(fragment);
		//Initialize image attachments band with empty data
		clearImageAttachmentBand();
		attachments.setVisibility(View.GONE);
		attachments.setData(new ArrayList<ImageItem>());

		if (adapter.getListener() != null) {
			adapter.getListener().onComposingViewCreated(this, message, attachments);
		}
	}

	/**
	 * Workaround for this issue: https://code.google.com/p/android/issues/detail?id=208169
	 */
	public void onViewAttachedToWindow() {
		message.setEnabled(false);
		message.setEnabled(true);
	}

	/**
	 * Remove all images from attachment band.
	 */
	public void clearImageAttachmentBand() {
		ApptentiveLog.e("CLEARING ATTACHMENTS");
		attachments.setVisibility(View.GONE);
		images.clear();
		attachments.setData(null);
	}

	/**
	 * Add new images to attachment band.
	 *
	 * @param imagesToAttach an array of new images to add
	 */
	public void addImagesToImageAttachmentBand(final List<ImageItem> imagesToAttach) {
		if (imagesToAttach == null || imagesToAttach.size() == 0) {
			return;
		}
		ApptentiveLog.e("ADDING IMAGES");

		attachments.setupLayoutListener();
		ApptentiveLog.e("SHOWING");
		attachments.setVisibility(View.VISIBLE);

		images.addAll(imagesToAttach);
		addAdditionalAttachItem();
		attachments.notifyDataSetChanged();
	}

	/**
	 * Remove an image from attachment band.
	 *
	 * @param position the postion index of the image to be removed
	 */
	public void removeImageFromImageAttachmentBand(final int position) {
		ApptentiveLog.e("REMOVING IMAGE");
		images.remove(position);
		attachments.setupLayoutListener();
		if (images.size() == 0) {
			// Hide attachment band after last attachment is removed
			ApptentiveLog.e("HIDING");
			attachments.setVisibility(View.GONE);
			return;
		}
		addAdditionalAttachItem();
	}

	private void addAdditionalAttachItem() {
		ArrayList<ImageItem> imagesToAdd = new ArrayList<ImageItem>(images);
		if (imagesToAdd.size() < itemView.getResources().getInteger(R.integer.apptentive_image_grid_default_attachments_total)) {
			imagesToAdd.add(new ImageItem("", "", "Image/*", 0));
		}
		attachments.setData(imagesToAdd);
	}

	public void setSendButtonState() {
		boolean enabled = !TextUtils.isEmpty(message.getText()) || !images.isEmpty();
		if (sendButton.isEnabled() ^ enabled) { // Only if changing value
			sendButton.setEnabled(enabled);
			if (enabled) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					sendButton.setColorFilter(Util.getThemeColor(itemView.getContext(), R.attr.apptentiveButtonTintColor));
				}
			} else {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					sendButton.setColorFilter(Util.getThemeColor(itemView.getContext(), R.attr.apptentiveButtonTintColorDisabled));
				}
			}
		}
	}
}
