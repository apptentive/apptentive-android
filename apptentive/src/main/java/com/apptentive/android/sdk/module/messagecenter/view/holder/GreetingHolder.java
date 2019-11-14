/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ImageUtil;

import static com.apptentive.android.sdk.util.Util.guarded;


public class GreetingHolder extends RecyclerView.ViewHolder {

	public TextView title;
	public TextView body;
	public ApptentiveAvatarView avatar;
	public ImageButton infoButton;

	public GreetingHolder(View itemView) {
		super(itemView);

		title = (TextView) itemView.findViewById(R.id.greeting_title);
		body = (TextView) itemView.findViewById(R.id.greeting_body);
		avatar = (ApptentiveAvatarView) itemView.findViewById(R.id.avatar);
		infoButton = (ImageButton) itemView.findViewById(R.id.btn_info);
	}

	public void bindView(MessageCenterGreeting greeting) {
		title.setText(greeting.title);
		title.setContentDescription(greeting.title);
		body.setText(greeting.body);
		body.setContentDescription(greeting.body);
		ImageUtil.startDownloadAvatarTask(avatar, greeting.avatar);
		infoButton.setOnClickListener(guarded(new View.OnClickListener() {
			public void onClick(final View view) {
				// Don't let the info button open multiple copies of the About page.
				view.setClickable(false);
				view.postDelayed(new Runnable() { // TODO: replace with DispatchQueue
					@Override
					public void run() {
						view.setClickable(true);
					}
				}, 300);
				ApptentiveInternal.getInstance().showAboutInternal(Util.castContextToActivity(itemView.getContext()), false);
			}
		}));
		infoButton.setClickable(true);
	}
}
