/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;

import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.util.Util;


/**
 * @author Barry Li
 */
public class MessageCenterComposingView extends FrameLayout implements MessageCenterListItemView {

	private int position;
	private EditText et;

	public MessageCenterComposingView(Context context, int position) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_composing, this);
		et = (EditText) parentView.findViewById(R.id.composing_et);

		/*et.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(final View v, boolean hasFocus) {
				v.post(new Runnable() {
					@Override
					public void run() {
						v.requestFocus();
						v.requestFocusFromTouch();
					}
				});
			}
		});*/

		et.addTextChangedListener(new TextWatcher() {
			private boolean doDelete = true;
			private boolean doScroll = false;
			private int lineCount = et.getLineCount();
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				doScroll = false;
				if (doDelete) {
					if (count == 0 && before == 1) {
						//a backspace was entered
						Editable buffer = et.getText();
						// If the cursor is at the end of a RecipientSpan then remove the whole span
						int selStart = Selection.getSelectionStart(buffer);
						int selEnd = Selection.getSelectionEnd(buffer);
						if (selStart == selEnd) {
							ImageSpan[] link = buffer.getSpans(selStart, selEnd, ImageSpan.class);
							if (link.length > 0) {
								buffer.replace(
										buffer.getSpanStart(link[0]),
										buffer.getSpanEnd(link[0]),
										""
								);
								buffer.removeSpan(link[0]);
							}
						}
					} else {
						int newLineCount = et.getLineCount();
						if (newLineCount > lineCount) {
							lineCount = newLineCount;
							doScroll = true;
						}
					}
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				MessageManager.onComposing(editable.toString(), doScroll);
				doScroll = false;
			}
		});

		View closeButton = parentView.findViewById(R.id.cancel_composing);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MessageManager.onCancelComposing();
			}
		});

		View attachButton = findViewById(R.id.attach);
		// Android devices can't take screenshots until Android OS version 4+
		boolean canTakeScreenshot = Util.getMajorOsVersion() >= 4;
		if (canTakeScreenshot) {
			attachButton.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					MessageManager.onAttachImage();
				}
			});
		} else {
			attachButton.setVisibility(GONE);
		}

		View sendButton = findViewById(R.id.send);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				MessageManager.onFinishComposing();
			}
		});
	}


	public EditText getEditText() {
		return et;
	}
}