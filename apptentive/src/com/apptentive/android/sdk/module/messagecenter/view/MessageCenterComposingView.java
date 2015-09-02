/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.text.Editable;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;


/**
 * @author Barry Li
 */
public class MessageCenterComposingView extends FrameLayout implements MessageCenterListItemView {

	private EditText et;

	public MessageCenterComposingView(Context activityContext, final MessageCenterComposingItem item, final MessageAdapter.OnComposingActionListener listener) {
		super(activityContext);

		LayoutInflater inflater = LayoutInflater.from(activityContext);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing_area, this);
		et = (EditText) parentView.findViewById(R.id.composing_et);
		if (item.str_2 != null) {
			et.setHint(item.str_2);
		}
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				listener.beforeComposingTextChanged(charSequence);
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				listener.onComposingTextChanged(charSequence);
			}

			@Override
			public void afterTextChanged(Editable editable) {
				listener.afterComposingTextChanged(editable.toString());
			}
		});

	}

	public EditText getEditText() {
		return et;
	}
}