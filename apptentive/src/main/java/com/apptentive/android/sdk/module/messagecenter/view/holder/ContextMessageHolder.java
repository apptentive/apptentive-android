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
import com.apptentive.android.sdk.module.messagecenter.model.ContextMessage;

public class ContextMessageHolder extends RecyclerView.ViewHolder {

	private TextView bodyTextView;

	public ContextMessageHolder(View itemView) {
		super(itemView);
		bodyTextView = (TextView) itemView.findViewById(R.id.body);
	}

	public void bindView(ContextMessage contextMessage) {
		bodyTextView.setText(contextMessage.getBody());
	}
}
