/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.module.messagecenter.view.CompoundMessageView;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Barry Li
 */
public class OutgoingCompoundMessageHolder extends MessageHolder {
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public TextView messageBodyView;
	private ApptentiveImageGridView imageBandView;

	public OutgoingCompoundMessageHolder(CompoundMessageView view) {
		super(view);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) view.findViewById(R.id.progressBar);
		messageBodyView = (TextView) view.findViewById(R.id.apptentive_compound_message_body);
		imageBandView =(ApptentiveImageGridView) view.findViewById(R.id.grid);
	}

	public void updateMessage(String datestamp, String status, int statusColor,
														boolean progressBarVisible, final String body, final int viewWidth, final int desiredColumn, final List<StoredFile> imagesToAttach) {
		super.updateMessage(datestamp, statusColor, status);
		if (progressBar != null) {
			if (progressBarVisible) {
				progressBar.start();
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.stop();
				progressBar.setVisibility(View.GONE);
			}
		}
		if (messageBodyView != null) {
			messageBodyView.setText(body);
		}
		// Set up attahments view
		if (imageBandView != null) {
			if (imagesToAttach == null || imagesToAttach.size() == 0) {
				imageBandView.setVisibility(View.GONE);
			} else {
				imageBandView.setVisibility(View.VISIBLE);
				imageBandView.setAdapterItemSize(viewWidth, desiredColumn);
				List<ImageItem> images = new ArrayList<ImageItem>();
				for (StoredFile file: imagesToAttach) {
					images.add(new ImageItem(file.getSourceUriOrPath(), file.getLocalFilePath(), file.getMimeType(), file.getCreationTime()));
				}
				imageBandView.setData(images);
			}
		}
	}
}