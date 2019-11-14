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
import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerViewAdapter;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACCESSIBILITY_SERVICE;

public class IncomingCompoundMessageHolder extends MessageHolder {

	public ApptentiveAvatarView avatar;
	private View root;
	private View container;
	private TextView messageBodyView;
	private TextView nameView;
	private ApptentiveImageGridView imageBandView;

	public IncomingCompoundMessageHolder(View itemView) {
		super(itemView);
		root = itemView.findViewById(R.id.message_root);
		container = itemView.findViewById(R.id.apptentive_compound_message_body_container);
		avatar = (ApptentiveAvatarView) itemView.findViewById(R.id.avatar);
		nameView = (TextView) itemView.findViewById(R.id.sender_name);
		messageBodyView = (TextView) itemView.findViewById(R.id.apptentive_compound_message_body);
		imageBandView = (ApptentiveImageGridView) itemView.findViewById(R.id.grid);
	}

	public void bindView(MessageCenterFragment fragment, final RecyclerView parent, final MessageCenterRecyclerViewAdapter adapter, final CompoundMessage message) {
		super.bindView(fragment, parent, message);
		imageBandView.setupUi();
		ImageUtil.startDownloadAvatarTask(avatar, message.getSenderProfilePhoto());

		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
		root.measure(widthMeasureSpec, 0);
		int viewWidth = container.getMeasuredWidth();

		messageBodyView.setText(message.getBody());
		// We have to disable text selection, or the Google TalkBack won't read this unless it's selected. It's too tiny to select by itself easily.
		AccessibilityManager accessibilityManager = (AccessibilityManager) fragment.getContext().getSystemService(ACCESSIBILITY_SERVICE);
		if (accessibilityManager != null) {
			boolean talkbackNotEnabled = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN).isEmpty();
			messageBodyView.setTextIsSelectable(talkbackNotEnabled);
		}
		String name = message.getSenderUsername();
		if (name != null && !name.isEmpty()) {
			nameView.setVisibility(View.VISIBLE);
			nameView.setText(name);
		} else {
			nameView.setVisibility(View.GONE);
		}

		final List<StoredFile> files = message.getRemoteAttachments();
		if (imageBandView != null) {
			if (files == null || files.size() == 0) {
				imageBandView.setVisibility(View.GONE);
			} else {
				imageBandView.setVisibility(View.VISIBLE);
				imageBandView.setAdapterItemSize(viewWidth, itemView.getResources().getInteger(R.integer.apptentive_image_grid_default_column_number_incoming));
				List<ImageItem> images = new ArrayList<ImageItem>();
				final File cacheDir = Util.getDiskCacheDir(imageBandView.getContext());
				for (StoredFile file : files) {
					String thumbnailUrl = file.getSourceUriOrPath();
					String remoteUrl = file.getApptentiveUri();
					String thumbnailStorageFilePath;
					if (!TextUtils.isEmpty(thumbnailUrl)) {
						thumbnailStorageFilePath = Util.generateCacheFileFullPath(thumbnailUrl, cacheDir);
						images.add(new ImageItem(thumbnailUrl, thumbnailStorageFilePath, file.getMimeType(), file.getCreationTime()));
					} else {
						thumbnailStorageFilePath = Util.generateCacheFileFullPath(remoteUrl, cacheDir);
						images.add(new ImageItem(remoteUrl, thumbnailStorageFilePath, file.getMimeType(), file.getCreationTime()));
					}
				}
				imageBandView.setData(images);
				imageBandView.setListener(new ApptentiveImageGridView.ImageItemClickedListener() {
					@Override
					public void onClick(int position, ImageItem image) {
						StoredFile file = files.get(position);
						String remoteUrl = file.getApptentiveUri();
						String localFilePath = Util.generateCacheFileFullPath(remoteUrl, cacheDir);
						if (adapter.getListener() != null) {
							adapter.getListener().onClickAttachment(position, new ImageItem(remoteUrl, localFilePath, file.getMimeType(), file.getCreationTime()));
						}
					}
				});
			}
		}
	}
}