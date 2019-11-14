/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;


public class MessageCenterRecyclerView extends RecyclerView {

	public MessageCenterRecyclerView(Context context) {
		super(context);
	}

	public MessageCenterRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public MessageCenterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}





	public int getFirstVisiblePosition() {
		return 0; // TODO
	}

	public int getLastVisiblePosition() {
		return 0; // TODO
	}

	public void setSelection(int selection) {
		return; // TODO
	}

	public void setSelectionFromTop(int selection, int top) {
		return; // TODO
	}
}
