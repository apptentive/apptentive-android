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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;


/**
 * @author Barry Li
 */
public class MessageCenterWhoCardView extends FrameLayout implements MessageCenterListItemView {

	private MessageAdapter.OnComposingActionListener listener;
	private EditText emailEditText;
	private EditText nameEditText;
	private TextView title;
	private TextView emailTip;
	private Button skipButton;
	private Button sendButton;

	/**
	 * @param context Must be a Context with theme set, such as an Activity
	 */
	public MessageCenterWhoCardView(Context context,
																	final MessageAdapter.OnComposingActionListener listener) {
		super(context);
    this.listener = listener;
		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_who_card, this);
		title = (TextView) parentView.findViewById(R.id.who_title);

		emailEditText = (EditText) parentView.findViewById(R.id.who_email);
		emailEditText.addTextChangedListener(new TextWatcher() {
			private boolean doScroll = false;

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				emailEditText.setTextColor(getResources().getColor(R.color.apptentive_text_message_text));
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		nameEditText = (EditText) parentView.findViewById(R.id.who_name);

		emailTip = (TextView) parentView.findViewById(R.id.email_tip);

		skipButton = (Button) findViewById(R.id.btn_skip);

		sendButton = (Button) findViewById(R.id.btn_send);
	}

	public void updateUi(final MessageCenterComposingItem item) {
		if (item.str_1 != null) {
			title.setText(item.str_1);
		}
		if (item.str_2 != null) {
			nameEditText.setVisibility(VISIBLE);
			nameEditText.setHint(item.str_2);
		} else {
			nameEditText.setVisibility(GONE);
		}
		if (item.str_3 != null) {
			emailEditText.setHint(item.str_3);
		}

		if (item.str_4 != null) {
			emailTip.setVisibility(VISIBLE);
			emailTip.setText(item.str_4);
		} else {
			emailTip.setVisibility(GONE);
		}

		if (item.button_1 == null) {
			skipButton.setVisibility(INVISIBLE);
		} else {
			skipButton.setVisibility(VISIBLE);
			skipButton.setText(item.button_1);
			skipButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					listener.onCloseWhoCard(item.button_1);
				}
			});
		}

		if (item.button_2 != null) {
			sendButton.setText(item.button_2);
			sendButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					if (item.button_1 == null) {
						String email = emailEditText.getText().toString();
						if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
							emailEditText.setTextColor(getResources().getColor(R.color.apptentive_red));
							return;
						}
					}
					Apptentive.setPersonEmail(getContext(), emailEditText.getText().toString());
					Apptentive.setPersonName(getContext(), nameEditText.getText().toString());
					listener.onSubmitWhoCard(item.button_2);
				}
			});
		}
	}

	public EditText getNameField() {
		return nameEditText;
	}

	public EditText getEmailField() {
		return emailEditText;
	}
}