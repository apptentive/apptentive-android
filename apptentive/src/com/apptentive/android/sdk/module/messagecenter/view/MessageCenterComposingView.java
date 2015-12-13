/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.text.Editable;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageGridViewAdapter;
import com.apptentive.android.sdk.util.image.ImageItem;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Barry Li
 */
public class MessageCenterComposingView extends FrameLayout implements MessageCenterListItemView {

	private EditText et;
	// Image Band
	private ApptentiveImageGridView imageBandView;
	List<ImageItem> images = new ArrayList<ImageItem>();

	public MessageCenterComposingView(Context activityContext, final MessageCenterComposingItem item, final MessageAdapter.OnListviewItemActionListener listener) {
		super(activityContext);

		LayoutInflater inflater = LayoutInflater.from(activityContext);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing_area, this);
		et = (EditText) parentView.findViewById(R.id.composing_et);
		if (item.str_2 != null) {
			et.setHint(item.str_2);
		}
		et.setLinksClickable(true);
		et.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.MAP_ADDRESSES);
		/*
		 * LinkMovementMethod would enable clickable links in EditView, but disables copy/paste through Long Press.
		 * Use a custom MovementMethod instead
		 *
		 */
		et.setMovementMethod(ApptentiveMovementMethod.getInstance());
		//If the edit text contains previous text with potential links
		Linkify.addLinks(et, Linkify.WEB_URLS);

		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				listener.beforeComposingTextChanged(charSequence);
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				listener.onComposingTextChanged(charSequence);
			}

			@Override
			public void afterTextChanged(Editable editable) {
				listener.afterComposingTextChanged(editable.toString());
				Linkify.addLinks(editable, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.MAP_ADDRESSES);
			}
		});


		imageBandView = (ApptentiveImageGridView) parentView.findViewById(R.id.grid);
		imageBandView.setupUi();
		imageBandView.setupLayoutListener();
		imageBandView.setListener(new ApptentiveImageGridView.ImageItemClickedListener() {
			@Override
			public void onClick(int position, ImageItem image) {
				listener.onClickAttachment(position, image);
			}
		});
		imageBandView.setAdapterIndicator(R.drawable.apptentive_ic_remove_attachment);

		imageBandView.setImageIndicatorCallback((ImageGridViewAdapter.Callback) listener);
		// Initialize image attachments band with empty data
		clearImageAttachmentBand();
	}

	public EditText getEditText() {
		return et;
	}

	/**
	 * Remove all images from attchment band.
	 */
	public void clearImageAttachmentBand() {
		imageBandView.setVisibility(View.GONE);
		images.clear();

		imageBandView.setData(images);
	}

	/**
	 * Add new images to attchment band.
	 *
	 * @param imagesToAttach an array of new images to add
	 */
	public void addImagesToImageAttachmentBand(final List<ImageItem> imagesToAttach) {

		if (imagesToAttach == null || imagesToAttach.size() == 0) {
			return;
		}

		imageBandView.setupLayoutListener();
		imageBandView.setVisibility(View.VISIBLE);

		images.addAll(imagesToAttach);
		addAdditionalAttchItem();
	}

	/**
	 * Remove an image from attchment band.
	 *
	 * @param position the postion index of the image to be removed
	 */
	public void removeImageFromImageAttachmentBand(final int position) {
		images.remove(position);
		imageBandView.setupLayoutListener();
		if (images.size() == 0) {
			// Hide attachment band after last attachment is removed
			imageBandView.setVisibility(View.GONE);
			return;
		}
		addAdditionalAttchItem();
	}

	private void addAdditionalAttchItem() {
		ArrayList<ImageItem> imagesToAdd = new ArrayList<ImageItem>(images);
		if (imagesToAdd.size() < getResources().getInteger(R.integer.apptentive_image_grid_default_attachments_total)) {
			imagesToAdd.add(new ImageItem("", "", "Image/*", 0));
		}
		imageBandView.setData(imagesToAdd);
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
}