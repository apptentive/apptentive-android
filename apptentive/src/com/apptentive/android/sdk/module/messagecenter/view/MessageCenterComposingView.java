/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
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
	private GridView imageBandView;
	private ImageGridViewAdapter.Callback imageBandCallback;
	private ImageGridViewAdapter imageBandAdapter;
	private int imageBandViewWidth, imageBandViewHeight;
	List<ImageItem> images = new ArrayList<ImageItem>();
	private int numberofAttachments;

	public MessageCenterComposingView(Context activityContext, final MessageCenterComposingItem item, final MessageAdapter.OnComposingActionListener listener) {
		super(activityContext);

		LayoutInflater inflater = LayoutInflater.from(activityContext);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing_area, this);
		et = (EditText) parentView.findViewById(R.id.composing_et);
		if (item.str_2 != null) {
			et.setHint(item.str_2);
		}
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
			}
		});

		imageBandAdapter = new ImageGridViewAdapter(activityContext, false);
		imageBandAdapter.showImageIndicator(true);
		imageBandAdapter.setImageIndicator(R.drawable.apptentive_ic_close);

		imageBandView = (GridView) parentView.findViewById(R.id.grid);
		imageBandView.setAdapter(imageBandAdapter);
		final int desiredNumCount = getResources().getInteger(R.integer.apptentive_image_grid_default_item_number);
		imageBandView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void onGlobalLayout() {

				final int width = imageBandView.getWidth();
				final int height = imageBandView.getHeight();

				imageBandViewWidth = width;
				imageBandViewHeight = height;


				final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.apptentive_image_grid_space_size);

				int columnWidth = (width - columnSpace * (desiredNumCount - 1)) / desiredNumCount;
				imageBandAdapter.setItemSize(columnWidth);

				/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					imageBandView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					imageBandView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}*/
			}
		});
		imageBandView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				ImageItem image = (ImageItem) adapterView.getAdapter().getItem(i);
				listener.onShowImagePreView(i, image.uri);
			}
		});
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
		final int desiredNumCount = getResources().getInteger(R.integer.apptentive_image_grid_default_item_number);

		imageBandView.setVisibility(View.GONE);
		images.clear();
		numberofAttachments = 0;
		for (int i = 0; i < desiredNumCount; ++i) {
			images.add(new ImageItem(null, "", 0));
		}
		imageBandAdapter.setData(images);
	}

	/**
	 * Add new images to attchment band.
	 * @param position the postion index of the first image to add
	 * @param imagesToAttach an array of new images to add
	 */
	public void addImagesToImageAttachmentBand(final int position, final List<Uri> imagesToAttach) {
		final int desiredNumCount = getResources().getInteger(R.integer.apptentive_image_grid_default_item_number);

		if (imagesToAttach == null || imagesToAttach.size() == 0) {
			return;
		}

		final int numOfImagesToAdd = Math.min(desiredNumCount - position, imagesToAttach.size());

		if (numOfImagesToAdd > 0 && numberofAttachments == 0) {
			// About to add image to an empty attachment band
			imageBandView.setVisibility(View.VISIBLE);
		}

		for (int i = 0; i < numOfImagesToAdd; ++i) {
			images.set(position + i, new ImageItem(imagesToAttach.get(i), "", 0));
			numberofAttachments++;
		}

		if (numOfImagesToAdd > 0) {
			imageBandAdapter.setData(images);
		}
	}

	/**
	 * Remove an image from attchment band.
	 * @param position the postion index of the image to be removed
	 */
	public void removeImageFromImageAttachmentBand(final int position) {
		images.remove(position);
		images.add(new ImageItem(null, "", 0));
		numberofAttachments--;
		if (numberofAttachments == 0) {
			// Hide attachment band after last attachment is removed
			imageBandView.setVisibility(View.GONE);
			return;
		}
		imageBandAdapter.setData(images);
	}
}