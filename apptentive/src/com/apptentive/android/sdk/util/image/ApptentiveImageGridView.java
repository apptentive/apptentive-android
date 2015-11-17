/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.Util;

import java.util.List;

public class ApptentiveImageGridView extends GridView implements AdapterView.OnItemClickListener {
	private ImageGridViewAdapter imageBandAdapter;

	private ImageItemClickedListener listener;

	private Context context;

	public ApptentiveImageGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnItemClickListener(this);
		this.context = context;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpec;

		if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {

			// The two leftmost bits in the height measure spec have
			// a special meaning, hence we can't use them to describe height.
			heightSpec = MeasureSpec.makeMeasureSpec(
					Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		} else {
			// Any other height should be respected as is.
			heightSpec = heightMeasureSpec;
		}

		super.onMeasure(widthMeasureSpec, heightSpec);
	}

	public void onItemClick(AdapterView parent, View v, int position, long id) {
		if (!imageBandAdapter.clickOn(position) && listener != null) {
			listener.onClick(position, imageBandAdapter.getItem(position));
		}
	}

	public void setListener(ImageItemClickedListener l) {
		listener = l;
	}

	public void setupUi() {
		imageBandAdapter = new ImageGridViewAdapter(context, false);
		setAdapter(imageBandAdapter);
	}

	public void setupLayoutListener() {
		final int desiredNumCount = getResources().getInteger(R.integer.apptentive_image_grid_default_column_number);
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void onGlobalLayout() {

				final int width = getWidth();
				final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.apptentive_image_grid_space_size);

				int columnWidth = (width - columnSpace * (desiredNumCount - 1)) / desiredNumCount;
				Point point = Util.getScreenSize(getContext().getApplicationContext());
				imageBandAdapter.setItemSize(columnWidth, (int) (((float) point.y / (float) point.x) * columnWidth));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}


			}
		});
	}

	public void setAdapterItemSize(int width, int desiredNumCount) {
		final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.apptentive_image_grid_space_size);

		int columnWidth = (width - columnSpace * (desiredNumCount - 1)) / desiredNumCount;
		Point point = Util.getScreenSize(getContext().getApplicationContext());
		imageBandAdapter.setItemSize(columnWidth, (int) (((float) point.y / (float) point.x) * columnWidth));
	}

	public void setAdapterIndicator(int rid) {
		if (rid != 0) {
			imageBandAdapter.showImageIndicator(true);
			imageBandAdapter.setImageIndicator(rid);
		} else {
			imageBandAdapter.showImageIndicator(false);
		}
	}

	public void setImageIndicatorCallback(ImageGridViewAdapter.Callback callback) {
			imageBandAdapter.setIndicatorCallback(callback);
	}

	public void setData(List<ImageItem> images) {
		imageBandAdapter.setData(images);
	}

	public interface ImageItemClickedListener {
		void onClick(int position, ImageItem image);
	}
}