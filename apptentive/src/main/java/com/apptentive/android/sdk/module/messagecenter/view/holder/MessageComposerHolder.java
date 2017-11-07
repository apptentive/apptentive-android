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

	private int maxAllowedAttachments;

	public MessageComposerHolder(View itemView) {
		super(itemView);
		images = new ArrayList<ImageItem>();
		closeButton = (ImageButton) itemView.findViewById(R.id.close_button);
		title = (TextView) itemView.findViewById(R.id.title);
		attachButton = (ImageButton) itemView.findViewById(R.id.attach_button);
		sendButton = (ImageButton) itemView.findViewById(R.id.send_button);
		message = (EditText) itemView.findViewById(R.id.message);
		attachments = (ApptentiveImageGridView) itemView.findViewById(R.id.attachments);

		maxAllowedAttachments = itemView.getResources().getInteger(R.integer.apptentive_image_grid_default_attachments_total);

		ColorStateList colors = ContextCompat.getColorStateList(itemView.getContext(), Util.getResourceIdFromAttribute(itemView.getContext().getTheme(), R.attr.apptentiveButtonTintColorStateList));
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable closeButtonDrawable = DrawableCompat.wrap(closeButton.getDrawable());
		DrawableCompat.setTintList(closeButtonDrawable, colors);
		closeButton.setImageDrawable(closeButtonDrawable);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable sendButtonDrawable = DrawableCompat.wrap(sendButton.getDrawable());
		DrawableCompat.setTintList(sendButtonDrawable, colors);
		sendButton.setImageDrawable(sendButtonDrawable);
		// Use a color state list for button tint state on Lollipop. On prior platforms, need to apply state color manually.
		Drawable attachButtonDrawable = DrawableCompat.wrap(attachButton.getDrawable());
		DrawableCompat.setTintList(attachButtonDrawable, colors);
		attachButton.setImageDrawable(attachButtonDrawable);
	}

	public void bindView(final MessageCenterFragment fragment, final MessageCenterRecyclerViewAdapter adapter, final Composer composer) {
		title.setText(composer.title);
		title.setContentDescription(composer.title);


		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!TextUtils.isEmpty(message.getText().toString().trim()) || !images.isEmpty()) {
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
		attachments.setAdapterIndicator(R.drawable.apptentive_remove_button);

		attachments.setImageIndicatorCallback(fragment);
		//Initialize image attachments band with empty data
		clearImageAttachmentBand();
		attachments.setVisibility(View.GONE);
		attachments.setData(new ArrayList<ImageItem>());
		setAttachButtonState();

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
		attachments.setupLayoutListener();
		attachments.setVisibility(View.VISIBLE);
		images.addAll(imagesToAttach);
		setAttachButtonState();
		addAdditionalAttachItem();
		attachments.notifyDataSetChanged();
	}

	/**
	 * Remove an image from attachment band.
	 *
	 * @param position the postion index of the image to be removed
	 */
	public void removeImageFromImageAttachmentBand(final int position) {
		images.remove(position);
		attachments.setupLayoutListener();
		setAttachButtonState();
		if (images.size() == 0) {
			// Hide attachment band after last attachment is removed
			attachments.setVisibility(View.GONE);
			return;
		}
		addAdditionalAttachItem();
	}

	private void addAdditionalAttachItem() {
		ArrayList<ImageItem> imagesToAdd = new ArrayList<ImageItem>(images);
		if (imagesToAdd.size() < maxAllowedAttachments) {
			imagesToAdd.add(new ImageItem("", "", "Image/*", 0));
		}
		attachments.setData(imagesToAdd);
	}

	public void setAttachButtonState() {
		boolean enabled = images.size() < maxAllowedAttachments;
		setButtonState(attachButton, enabled);
	}

	public void setSendButtonState() {
		boolean enabled = !TextUtils.isEmpty(message.getText().toString().trim()) || !images.isEmpty();
		setButtonState(sendButton, enabled);
	}

	public void setButtonState(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		if (enabled) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				button.setColorFilter(Util.getThemeColor(itemView.getContext(), R.attr.apptentiveButtonTintColor));
			}
		} else {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				button.setColorFilter(Util.getThemeColor(itemView.getContext(), R.attr.apptentiveButtonTintColorDisabled));
			}
		}
	}
}
