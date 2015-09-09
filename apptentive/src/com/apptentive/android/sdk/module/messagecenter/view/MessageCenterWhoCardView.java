/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;

import android.text.Editable;
import android.text.TextUtils;
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
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterWhoCardView extends FrameLayout implements MessageCenterListItemView {

	private MessageAdapter.OnComposingActionListener listener;
	private EditText emailEditText;
	private EditText nameEditText;
	private TextView title;
	private TextView emailExplanation;
	private Button skipButton;
	private Button sendButton;

	private TextWatcher nameTextWatcher;
	private TextWatcher emailTextWatcher;
	private boolean emailIsValid;

	public MessageCenterWhoCardView(final Context activityContext, final MessageAdapter.OnComposingActionListener listener) {
		super(activityContext);
		this.listener = listener;
		LayoutInflater inflater = LayoutInflater.from(activityContext);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_who_card, this);
		title = (TextView) parentView.findViewById(R.id.who_title);

		sendButton = (Button) parentView.findViewById(R.id.btn_send);

		emailEditText = (EditText) parentView.findViewById(R.id.who_email);

		nameEditText = (EditText) parentView.findViewById(R.id.who_name);

		emailExplanation = (TextView) parentView.findViewById(R.id.email_explanation);

		skipButton = (Button) parentView.findViewById(R.id.btn_skip);

	}

	/**
	 * Update Who Card UI under different scenarios, defined in {@link com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem}
	 *
	 *
	 * @param item  The generic object containing the data to update composing view.
	 * @param name  Stored profile name, maybe null
	 * @param email Stored profile email, maybe null
	 * @return
	 */
	public void updateUi(final MessageCenterComposingItem item, String name, String email) {
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
			emailExplanation.setVisibility(VISIBLE);
			emailExplanation.setText(item.str_4);
		} else {
			emailExplanation.setVisibility(GONE);
		}

		skipButton.setEnabled(true);
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
					Apptentive.setPersonEmail(getContext(), emailEditText.getText().toString().trim());
					Apptentive.setPersonName(getContext(), nameEditText.getText().toString().trim());
					listener.onSubmitWhoCard(item.button_2);
				}
			});
			sendButton.setEnabled(false);
		}

		if (nameTextWatcher != null) {
			nameEditText.removeTextChangedListener(nameTextWatcher);
		}

		if (!TextUtils.isEmpty(name)) {
			nameEditText.setText(name);
		}

		nameTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				// Disable send button when the content hasn't change yet
				sendButton.setEnabled(false);
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// If email field content is valid, any change in name field would enable send button
				if (emailIsValid) {
					sendButton.setEnabled(true);
				}
			}
		};

		nameEditText.addTextChangedListener(nameTextWatcher);

		if (emailTextWatcher != null) {
			emailEditText.removeTextChangedListener(emailTextWatcher);
		}

		// email passed into updateUi() is saved profile email, it must have been validated
		if (!TextUtils.isEmpty(email)) {
			emailEditText.setText(email);
			emailIsValid = true;
		} else if (item.getType() >= MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD_REQUESTED_INIT) {
			// Allow user only change name with email being blank if profile is requested
			emailIsValid = true;
		} else {
			skipButton.setEnabled(false);
		}

		emailTextWatcher = new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				// Disable send button when the content hasn't change yet
				sendButton.setEnabled(false);
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				String emailContent = editable.toString().trim();
				if (Util.isEmailValid(emailContent)) {
					// email must be in valid format after the change. If it is, enable send button
					sendButton.setEnabled(true);
					emailIsValid = true;
				} else
					// Allow user remove email completely when editing prifle of "Email Requested"
					if (TextUtils.isEmpty(emailContent) && item.getType() >= MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD_REQUESTED_INIT) {
						sendButton.setEnabled(true);
						emailIsValid = true;
					} else {
						// email not valid after change, so disable the send button
						sendButton.setEnabled(false);
						emailIsValid = false;
					}
			}
		};

		emailEditText.addTextChangedListener(emailTextWatcher);

	}

	public EditText getNameField() {
		return nameEditText;
	}

	public EditText getEmailField() {
		return emailEditText;
	}
}