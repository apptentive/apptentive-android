/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.WhoCard;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterRecyclerViewAdapter;
import com.apptentive.android.sdk.util.Util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class WhoCardHolder extends RecyclerView.ViewHolder {

	private MessageCenterRecyclerViewAdapter adapter;

	private TextView title;
	private TextInputLayout nameLayout;
	private EditText nameEditText;
	private TextInputLayout emailLayout;
	private EditText emailEditText;
	private TextView emailExplanation;
	private Button skipButton;
	private Button saveButton;

	public WhoCardHolder(MessageCenterRecyclerViewAdapter adapter, View itemView) {
		super(itemView);

		this.adapter = adapter;

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

		if (TextUtils.isEmpty(whoCard.getTitle())) {
			title.setVisibility(GONE);
		} else {
			title.setVisibility(VISIBLE);
			title.setText(whoCard.getTitle());
			itemView.setContentDescription(whoCard.getTitle());
		}

		if (TextUtils.isEmpty(whoCard.getNameHint())) {
			nameLayout.setVisibility(GONE);
		} else {
			nameLayout.setVisibility(VISIBLE);
			nameLayout.setHint(whoCard.getNameHint());
		}
		nameEditText.setText(Apptentive.getPersonName());
		nameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				updateSaveButton(whoCard);
			}
		});

		emailLayout.setHint(whoCard.getEmailHint());
		emailEditText.setText(Apptentive.getPersonEmail());
		if (Util.isEmailValid(emailEditText.getText().toString().trim())) {
			saveButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
		}

		TextWatcher emailTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence existingContent, int i, int i2, int i3) {
				// Disable send button when the content hasn't change yet
				if (Util.isEmailValid(existingContent.toString().trim())) {
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
				updateSaveButton(whoCard);
			}
		};
		emailEditText.addTextChangedListener(emailTextWatcher);

		if (TextUtils.isEmpty(whoCard.getEmailExplanation())) {
			emailExplanation.setVisibility(GONE);
		} else {
			emailExplanation.setVisibility(VISIBLE);
			emailExplanation.setText(whoCard.getEmailExplanation());
		}

		if (TextUtils.isEmpty(whoCard.getSkipButton())) {
			skipButton.setVisibility(GONE);
		} else {
			skipButton.setVisibility(VISIBLE);
			skipButton.setText(whoCard.getSkipButton());
			skipButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (adapter.getListener() != null) {
						adapter.getListener().onCloseWhoCard(skipButton.getText().toString());
					}
				}
			});
		}

		if (TextUtils.isEmpty(whoCard.getSaveButton())) {
			saveButton.setVisibility(GONE);
		} else {
			saveButton.setVisibility(VISIBLE);
			saveButton.setText(whoCard.getSaveButton());
		}

		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (isWhoCardContentValid(whoCard.isRequire())) {
					Apptentive.setPersonEmail(emailEditText.getText().toString().trim());
					Apptentive.setPersonName(nameEditText.getText().toString().trim());
					if (adapter.getListener() != null) {
						adapter.getListener().onSubmitWhoCard(saveButton.getText().toString());
					}
				}
			}
		});
		if (adapter.getListener() != null) {
			adapter.getListener().onWhoCardViewCreated(nameEditText, emailEditText, null);
		}

		// we need to properly announce the profile card: sending an implicit accessibility event
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			itemView.postDelayed(new Runnable() {
				@Override
				public void run() {
						itemView.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
				}
			}, 500);
		}
	}

	private boolean isWhoCardContentValid(boolean required) {
		String emailContent = emailEditText.getText().toString().trim();
		if (Util.isEmailValid(emailContent)) {
			return true;
		}
		// Allow user only change name but leave email blank if profile is only requested, not required
		if (TextUtils.isEmpty(emailContent) && !required) {
			return true;
		}
		return false;
	}

	private void updateSaveButton(WhoCard whoCard) {
		String nameContent = nameEditText.getText().toString().trim();
		String emailContent = emailEditText.getText().toString().trim();

		// if both name and email are empty - disable the button
		if (nameContent.isEmpty() && emailContent.isEmpty()) {
			saveButton.setEnabled(false);
			return;
		}

		// if email is valid - enable the button
		if (Util.isEmailValid(emailContent)) {
			saveButton.setEnabled(true);
			return;
		}

		// Allow user remove email completely when editing profile of that doesn't have "Email Required"
		if (TextUtils.isEmpty(emailContent) && !whoCard.isRequire()) {
			saveButton.setEnabled(true);
			return;
		}

		// otherwise, disable the button
		saveButton.setEnabled(false);
	}
}
