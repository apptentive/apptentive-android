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
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.MotionEvent;
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

	List<ImageItem> images;

	public ImageButton closeButton;
	public TextView title;
	public ImageButton attachButton;
	public ImageButton sendButton;
	public EditText message;
	public ApptentiveImageGridView attachments;

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
		setSendButtonEnabled(false);
		sendButton.setContentDescription(composer.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (adapter.getListener() != null) {
					adapter.getListener().onFinishComposing();
				}
			}
		});

		message.setHint(composer.messageHint);
		message.setLinksClickable(true);
		message.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.MAP_ADDRESSES);
		/*
		* LinkMovementMethod would enable clickable links in EditView, but disables copy/paste through Long Press.
		* Use a custom MovementMethod instead
		*
		*/
		message.setMovementMethod(ApptentiveMovementMethod.getInstance());
		//If the edit text contains previous text with potential links
		Linkify.addLinks(message, Linkify.WEB_URLS);

		message.addTextChangedListener(new TextWatcher() {
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
				setSendButtonEnabled(!TextUtils.isEmpty(message.getText()));
				Linkify.addLinks(editable, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.MAP_ADDRESSES);
			}
		});


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
	}

	/**
	 * Remove all images from attchment band.
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
		addAdditionalAttachItem();
	}

	/**
	 * Remove an image from attchment band.
	 *
	 * @param position the postion index of the image to be removed
	 */
	public void removeImageFromImageAttachmentBand(final int position) {
		images.remove(position);
		attachments.setupLayoutListener();
		if (images.size() == 0) {
			// Hide attachment band after last attachment is removed
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

	/*
	* Extends Android default movement method to enable selecting text and openning the links at the same time
	 */
	private static class ApptentiveMovementMethod extends ArrowKeyMovementMethod {

		private static ApptentiveMovementMethod sInstance;

		public static MovementMethod getInstance() {
			if (sInstance == null) {
				sInstance = new ApptentiveMovementMethod();
			}
			return sInstance;
		}

		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
			int action = event.getAction();
			if (action == MotionEvent.ACTION_UP ||
				action == MotionEvent.ACTION_DOWN) {
				int x = (int) event.getX();
				int y = (int) event.getY();
				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();
				x += widget.getScrollX();
				y += widget.getScrollY();
				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);
				ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
				if (link.length != 0) {
					if (action == MotionEvent.ACTION_UP) {
						link[0].onClick(widget);
					} else if (action == MotionEvent.ACTION_DOWN) {
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
					}
					return true;
				}
			}
			return super.onTouchEvent(widget, buffer, event);
		}
	}

	private void setSendButtonEnabled(boolean enabled) {
		if (sendButton.isEnabled() ^ enabled) { // No change required
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
