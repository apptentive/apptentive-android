/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;

public abstract class MessageHolder extends RecyclerView.ViewHolder {
	public TextView datestamp;

	public MessageHolder(View itemView) {
		super(itemView);
		datestamp = (TextView) itemView.findViewById(R.id.datestamp);
	}

	public void bindView(RecyclerView recyclerView, CompoundMessage message) { // final String datestampString, final int statusColor, final String statusString) {
		String datestampString = message.getDatestamp();
		datestamp.setText(datestampString);
		datestamp.setVisibility(!TextUtils.isEmpty(datestampString) ? View.VISIBLE : View.GONE);
	}
}
