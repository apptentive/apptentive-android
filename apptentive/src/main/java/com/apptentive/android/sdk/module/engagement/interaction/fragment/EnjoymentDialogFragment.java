/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.EnjoymentDialogInteraction;

public class EnjoymentDialogFragment extends ApptentiveBaseFragment<EnjoymentDialogInteraction> implements View.OnClickListener {

	private static final String CODE_POINT_DISMISS = "dismiss";
	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_YES = "yes";
	private static final String CODE_POINT_NO = "no";

	public static EnjoymentDialogFragment newInstance(Bundle bundle) {
		EnjoymentDialogFragment enjoymentFragment = new EnjoymentDialogFragment();
		enjoymentFragment.setArguments(bundle);
		return enjoymentFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.apptentive_enjoyment_dialog_interaction, container, false);

		TextView bodyView = (TextView) v.findViewById(R.id.title);
		String body = interaction.getTitle();
		bodyView.setText(body);

		int buttonsTotalLength = 0;
		// No
		String noText = interaction.getNoText();
		Button noButton = (Button) v.findViewById(R.id.no);
		if (noText != null) {
			noButton.setText(noText);
			buttonsTotalLength += noText.length();
		}
		noButton.setOnClickListener(this);

		// Yes
		String yesText = interaction.getYesText();
		Button yesButton = (Button) v.findViewById(R.id.yes);
		yesButton.setActivated(true);
		if (yesText != null) {
			yesButton.setText(yesText);
			buttonsTotalLength += yesText.length();
		}
		yesButton.setOnClickListener(this);

		// Change orientation of button area to vertical if buttons won't both fit on one line.
		LinearLayout buttonContainer = (LinearLayout) v.findViewById(R.id.button_container);
		boolean vertical = buttonsTotalLength > 16;
		if (vertical) {
			buttonContainer.setOrientation(LinearLayout.VERTICAL);
		} else {
			buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
		}

		// Dismiss "X" Button
		boolean showDismissButton = interaction.showDismissButton();
		String dismissText = interaction.getDismissText();
		ImageButton dismissButton = (ImageButton) v.findViewById(R.id.dismiss);
		if (showDismissButton) {
			if (dismissText != null) {
				dismissButton.setContentDescription(dismissText);
			}
			dismissButton.setVisibility(View.VISIBLE);
		}
		dismissButton.setOnClickListener(this);
		return v;
	}

	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		engageInternal(CODE_POINT_CANCEL, exitTypeToDataJson(exitType));
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.yes) {
			engageInternal(CODE_POINT_YES);
		} else if (id == R.id.no) {
			engageInternal(CODE_POINT_NO);
		} else if (id == R.id.dismiss) {
			engageInternal(CODE_POINT_DISMISS);
		}
		transit();
	}
}