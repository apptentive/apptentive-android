/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.accessibilityservice.AccessibilityServiceInfo;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerViewAdapter;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACCESSIBILITY_SERVICE;

public class OutgoingCompoundMessageHolder extends MessageHolder {

	public View root;
	public View container;
	public ApptentiveMaterialIndeterminateProgressBar progressBar;
	public TextView messageBodyView;
	public ApptentiveImageGridView imageBandView;
	public TextView status;

	public OutgoingCompoundMessageHolder(View itemView) {
		super(itemView);
		root = itemView.findViewById(R.id.message_root);
		container = itemView.findViewById(R.id.apptentive_compound_message_body_container);
		progressBar = (ApptentiveMaterialIndeterminateProgressBar) itemView.findViewById(R.id.progressBar);
		messageBodyView = (TextView) itemView.findViewById(R.id.apptentive_compound_message_body);
		imageBandView = (ApptentiveImageGridView) itemView.findViewById(R.id.grid);
		status = (TextView) itemView.findViewById(R.id.status);
	}

	public void bindView(MessageCenterFragment fragment, final RecyclerView recyclerView, final MessageCenterRecyclerViewAdapter adapter, final CompoundMessage message) {
		super.bindView(fragment, recyclerView, message);
		imageBandView.setupUi();

		messageBodyView.setText(message.getBody());
/*
		// We have to disable text selection, or the Google TalkBack won't read this unless it's selected. It's too tiny to select by itself easily.
		AccessibilityManager accessibilityManager = (AccessibilityManager) fragment.getContext().getSystemService(ACCESSIBILITY_SERVICE);
		if (accessibilityManager != null) {
			boolean talkbackNotEnabled = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN).isEmpty();
			messageBodyView.setTextIsSelectable(talkbackNotEnabled);
		}
*/

		boolean showProgress;
		Double createdAt = message.getCreatedAt();
		String statusText;
		if (createdAt == null || createdAt > Double.MIN_VALUE) {
			// show progress bar if: 1. no sent time set, and 2. not paused, and 3. have either text or files to sent
			showProgress = createdAt == null && !fragment.isPaused() && (message.getAssociatedFiles() != null || !TextUtils.isEmpty(message.getBody()));
			statusText = createStatus(createdAt, message.isLastSent(), fragment.isPaused());
		} else {
			showProgress = false;
			statusText = itemView.getResources().getString(R.string.apptentive_failed);
		}


		if (showProgress) {
			progressBar.start();
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.stop();
			progressBar.setVisibility(View.GONE);
		}

		List<StoredFile> files = message.getAssociatedFiles();
		int imagebandWidth = 0;
		if (files != null && files.size() > 0) {
			int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
			root.measure(widthMeasureSpec, 0);
			int viewWidth = container.getMeasuredWidth();
			imagebandWidth = viewWidth - container.getPaddingLeft() - container.getPaddingRight();
		}

		if (files == null || files.size() == 0) {
			imageBandView.setVisibility(View.GONE);
		} else {
			imageBandView.setVisibility(View.VISIBLE);
			imageBandView.setAdapterItemSize(imagebandWidth, itemView.getResources().getInteger(R.integer.apptentive_image_grid_default_column_number));
			List<ImageItem> images = new ArrayList<ImageItem>();
			for (StoredFile file : files) {
				images.add(new ImageItem(file.getSourceUriOrPath(), file.getLocalFilePath(), file.getMimeType(), file.getCreationTime()));
			}
			imageBandView.setData(images);
			imageBandView.setListener(new ApptentiveImageGridView.ImageItemClickedListener() {
				@Override
				public void onClick(int position, ImageItem image) {
					if (adapter.getListener() != null) {
						adapter.getListener().onClickAttachment(position, image);
					}
				}
			});
		}
		status.setText(statusText);
		status.setTextColor(getStatusColor(createdAt, fragment.isPaused()));
		status.setVisibility(!TextUtils.isEmpty(statusText) ? View.VISIBLE : View.GONE);
	}

	protected String createStatus(Double seconds, boolean showSent, boolean isPaused) {
		if (seconds == null) {
			return isPaused ? itemView.getResources().getString(R.string.apptentive_failed) : null;
		}
		return (showSent) ? itemView.getResources().getString(R.string.apptentive_sent) : null;
	}

	private int getStatusColor(Double seconds, boolean isPaused) {
		if (seconds == null) {
			// failed color (red)
			return isPaused ? Util.getThemeColor(itemView.getContext(), R.attr.apptentiveValidationFailedColor) : 0;
		}
		// other status color
		return Util.getThemeColor(itemView.getContext(), android.R.attr.textColorSecondary);
	}
}