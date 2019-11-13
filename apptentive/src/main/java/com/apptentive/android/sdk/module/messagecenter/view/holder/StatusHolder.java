/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.R;

public class StatusHolder extends RecyclerView.ViewHolder {
	public TextView body;
	public ImageView icon;

	public StatusHolder(View itemView) {
		super(itemView);
		body = (TextView) itemView.findViewById(R.id.status_body);
		icon = (ImageView) itemView.findViewById(R.id.icon);
	}
}
