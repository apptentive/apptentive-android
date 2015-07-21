/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.util.Constants;


/**
 * @author Barry Li
 */
public class MessageCenterWhoCardView extends FrameLayout implements MessageCenterListItemView {

	private EditText et_email;
	private EditText et_name;

	public MessageCenterWhoCardView(Context context, final MessageAdapter.OnComposingActionListener listener) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_who_card, this);
		et_email = (EditText) parentView.findViewById(R.id.who_email);
		et_name = (EditText) parentView.findViewById(R.id.who_name);

		et_email.addTextChangedListener(new TextWatcher() {
			private boolean doScroll = false;

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				et_email.setTextColor(getResources().getColor(R.color.apptentive_text_message_text));
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		View skipButton = findViewById(R.id.btn_skip);
		final boolean reqired = Configuration.load(getContext()).isMessageCenterEmailRequired();
		if (skipButton != null) {
			if (reqired) {
				skipButton.setVisibility(INVISIBLE);
			} else {
				skipButton.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						listener.onSkipWhoCard();
					}
				});
			}
		}

		View sendButton = findViewById(R.id.btn_send);
		if (reqired) {
			((Button)sendButton).setText(getResources().getText(R.string.apptentive_send));
		}
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (reqired) {
					String email = et_email.getText().toString();
					if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
						et_email.setTextColor(getResources().getColor(R.color.apptentive_red));
						return;
					}
				}
				SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_EMAIL, et_email.getText().toString());
				editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_NAME, et_name.getText().toString());
				editor.commit();
				listener.onSendWhoCard();
			}
		});

	}

	public EditText getNameField() {
		return et_name;
	}

	public EditText getEmailField() {
		return et_email;
	}
}