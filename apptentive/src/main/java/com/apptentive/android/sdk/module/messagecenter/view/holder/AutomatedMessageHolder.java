/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.CompoundMessage;

public class AutomatedMessageHolder extends RecyclerView.ViewHolder {
	public TextView body;

	public AutomatedMessageHolder(View itemView) {
		super(itemView);
		body = (TextView) itemView.findViewById(R.id.apptentive_message_auto_body);
	}

	public void bindView(final RecyclerView parent, final CompoundMessage message) {
		body.setText(message.getBody());
	}
}