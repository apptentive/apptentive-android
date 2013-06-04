/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.rating.view;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class EnjoymentDialog extends ApptentiveBaseDialog {

	private OnChoiceMadeListener onChoiceMadeListener;

	public EnjoymentDialog(final Context context) {
		super(context, R.layout.apptentive_enjoyment_dialog);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Button noButton = (Button) findViewById(R.id.no);
		final Button yesButton = (Button) findViewById(R.id.yes);

		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (onChoiceMadeListener != null) {
					onChoiceMadeListener.onNo();
				}
			}
		});

		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (onChoiceMadeListener != null) {
					onChoiceMadeListener.onYes();
				}
			}
		});
	}

	@Override
	public void setTitle(CharSequence title) {
		TextView textView = (TextView) findViewById(R.id.title);
		textView.setText(title);
	}


	public void setOnChoiceMadeListener(OnChoiceMadeListener onChoiceMadeListener) {
		this.onChoiceMadeListener = onChoiceMadeListener;
	}

	public interface OnChoiceMadeListener {
		public void onNo();
		public void onYes();
	}
}
