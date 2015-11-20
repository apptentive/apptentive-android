/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.view.View;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author Barry Li
 */
public class CompoundMessageView extends PersonalMessageView<CompoundMessage> {

	private WeakReference<MessageAdapter.OnListviewItemActionListener> listenerRef;
	private boolean isOutGoingView = false;

	public CompoundMessageView(Context context, CompoundMessage message, final MessageAdapter.OnListviewItemActionListener listener) {
		super(context, message);
		this.listenerRef = new WeakReference<MessageAdapter.OnListviewItemActionListener>(listener);
		isOutGoingView = message.isOutgoingMessage();
	}

	protected void init(Context context, CompoundMessage message) {
		super.init(context, message);
		ApptentiveImageGridView imageBandView = (ApptentiveImageGridView) findViewById(R.id.grid);
		imageBandView.setupUi();

		imageBandView.setListener(new ApptentiveImageGridView.ImageItemClickedListener() {
			@Override
			public void onClick(int position, ImageItem image) {
				MessageAdapter.OnListviewItemActionListener listener = listenerRef.get();
				if (listener != null) {
					listener.onClickAttachment(position, image);
				}
			}
		});
		imageBandView.setVisibility(View.GONE);
		imageBandView.setAdapterIndicator(0);
		imageBandView.setData(new ArrayList<ImageItem>());
	}

	public boolean isViewShowingOutgoingMessage() {
		return isOutGoingView;
	}

	public MessageAdapter.OnListviewItemActionListener getListener() {
		return listenerRef.get();
	}

}