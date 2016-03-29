/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;

import android.support.v7.view.ContextThemeWrapper;
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
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterWhoCardView extends FrameLayout implements MessageCenterListItemView {

	private MessageAdapter.OnListviewItemActionListener listener;
	private EditText emailEditText;
	private EditText nameEditText;
	private TextView title;
	private TextView emailExplanation;
	private Button skipButton;
	private Button sendButton;

	public MessageCenterWhoCardView(final Context activityContext, final MessageAdapter.OnListviewItemActionListener listener) {
		super(activityContext);
		this.listener = listener;
		final Context contextThemeWrapper = new ContextThemeWrapper(activityContext, ApptentiveInternal.getInstance().getApptentiveTheme());
		// clone the inflater using the ContextThemeWrapper

		LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
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
					if (isWhoCardContentValid(item.getType())) {
						Apptentive.setPersonEmail(emailEditText.getText().toString().trim());
						Apptentive.setPersonName(nameEditText.getText().toString().trim());
						listener.onSubmitWhoCard(item.button_2);
					}
				}
			});
		}


		if (!TextUtils.isEmpty(name)) {
			nameEditText.setText(name);
		}

		TextWatcher emailTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence existingContent, int i, int i2, int i3) {
				// Disable send button when the content hasn't change yet
				if (Util.isEmailValid(existingContent.toString())) {
					sendButton.setEnabled(true);
				} else {
					sendButton.setEnabled(false);
				}
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
				} else
					// Allow user remove email completely when editing prifle of "Email Requested"
					if (TextUtils.isEmpty(emailContent) && item.getType() >= MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD_REQUESTED_INIT) {
						sendButton.setEnabled(true);
					} else {
						// email not valid after change, so disable the send button
						sendButton.setEnabled(false);
					}
			}
		};

		emailEditText.addTextChangedListener(emailTextWatcher);

		if (!TextUtils.isEmpty(email)) {
			emailEditText.setText(email);
		}
	}

	public EditText getNameField() {
		return nameEditText;
	}

	public EditText getEmailField() {
		return emailEditText;
	}

	private boolean isWhoCardContentValid(int type) {
		String emailContent = emailEditText.getText().toString();
		if (Util.isEmailValid(emailContent)) {
			return true;
		}
		// Allow user only change name but leave email blank if profile is only requested, not required
		if (TextUtils.isEmpty(emailContent) && type >= MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD_REQUESTED_INIT) {
			return true;
		}
		return false;
	}

}