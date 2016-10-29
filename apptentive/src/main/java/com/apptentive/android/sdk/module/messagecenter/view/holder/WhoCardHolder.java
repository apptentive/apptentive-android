/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.WhoCard;
import com.apptentive.android.sdk.util.Util;

public class WhoCardHolder extends RecyclerView.ViewHolder {
	private TextView title;
	private TextInputLayout nameLayout;
	private EditText nameEditText;
	private TextInputLayout emailLayout;
	private EditText emailEditText;
	private TextView emailExplanation;
	private Button skipButton;
	private Button saveButton;

	public WhoCardHolder(View itemView) {
		super(itemView);
		title = (TextView) itemView.findViewById(R.id.who_title);
		nameEditText = (EditText) itemView.findViewById(R.id.who_name);
		nameLayout = (TextInputLayout) itemView.findViewById(R.id.input_layout_who_name);
		emailEditText = (EditText) itemView.findViewById(R.id.who_email);
		emailLayout = (TextInputLayout) itemView.findViewById(R.id.input_layout_who_email);
		emailExplanation = (TextView) itemView.findViewById(R.id.email_explanation);
		skipButton = (Button) itemView.findViewById(R.id.btn_skip);
		saveButton = (Button) itemView.findViewById(R.id.btn_send);
	}

	public void bindView(RecyclerView recyclerView, final WhoCard whoCard) {

		title.setText(whoCard.getTitle());
		nameLayout.setHint(whoCard.getNameHint());
		emailExplanation.setText(whoCard.getEmailExplanation());
		emailLayout.setHint(whoCard.getEmailHint());
		skipButton.setText(whoCard.getSkipButton());
		saveButton.setText(whoCard.getSaveButton());

		TextWatcher emailTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence existingContent, int i, int i2, int i3) {
				// Disable send button when the content hasn't change yet
				if (Util.isEmailValid(existingContent.toString())) {
					saveButton.setEnabled(true);
				} else {
					saveButton.setEnabled(false);
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
					saveButton.setEnabled(true);
				} else
					// Allow user remove email completely when editing profile of "Email Requested"
					if (TextUtils.isEmpty(emailContent) && !whoCard.isRequire()) {
						saveButton.setEnabled(true);
					} else {
						// email not valid after change, so disable the send button
						saveButton.setEnabled(false);
					}
			}
		};
		emailEditText.addTextChangedListener(emailTextWatcher);

		// TODO: Prepopulate with name and email if we already have them.
/*
		emailEditText.setText(email);
		nameEditText.setText(name);
*/
		/* // TODO: Hook up listeners
			skipButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					listener.onCloseWhoCard(item.button_1);
				}
			});

			sendButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (isWhoCardContentValid(item.getType())) {
						Apptentive.setPersonEmail(emailEditText.getText().toString().trim());
						Apptentive.setPersonName(nameEditText.getText().toString().trim());
						listener.onSubmitWhoCard(item.button_2);
					}
				}
			});
*/
	}
}
