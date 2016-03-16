/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.EnjoymentDialogInteraction;

public class EnjoymentDialogFragment extends ApptentiveBaseFragment<EnjoymentDialogInteraction> {

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
		// create ContextThemeWrapper from the original Activity Context with the apptentive theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), ApptentiveInternal.getInstance().getApptentiveTheme());
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater themedInflater = inflater.cloneInContext(contextThemeWrapper);
		View v = themedInflater.inflate(R.layout.apptentive_enjoyment_dialog_interaction, container, false);
		TextView bodyView = (TextView) v.findViewById(R.id.title);
		String body = interaction.getTitle();
		bodyView.setText(body);

		// No
		String noText = interaction.getNoText();
		Button noButton = (Button) v.findViewById(R.id.no);
		if (noText != null) {
			noButton.setText(noText);
		}
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_NO);
				transit();
			}
		});

		// Yes
		String yesText = interaction.getYesText();
		Button yesButton = (Button) v.findViewById(R.id.yes);
		if (yesText != null) {
			yesButton.setText(yesText);
		}
		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_YES);
				transit();
			}
		});
		return v;
	}

	@Override
	public boolean onBackPressed() {
		EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_CANCEL);
		return false;
	}
}