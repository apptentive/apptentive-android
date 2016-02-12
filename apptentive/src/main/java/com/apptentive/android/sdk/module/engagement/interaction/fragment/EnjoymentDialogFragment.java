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
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.EnjoymentDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.ApptentiveDialogButton;

public class EnjoymentDialogFragment extends ApptentiveBaseFragment {

	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_YES = "yes";
	private static final String CODE_POINT_NO = "no";

	private EnjoymentDialogInteraction interaction;

	public static EnjoymentDialogFragment newInstance(Bundle bundle) {
		EnjoymentDialogFragment enjoymentFragment = new EnjoymentDialogFragment();
		enjoymentFragment.setArguments(bundle);
		return enjoymentFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		// create ContextThemeWrapper from the original Activity Context with the apptentive theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), ApptentiveInternal.apptentiveTheme);
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater themedInflater = inflater.cloneInContext(contextThemeWrapper);
		View v = themedInflater.inflate(R.layout.apptentive_enjoyment_dialog_interaction, container, false);
		TextView bodyView = (TextView) v.findViewById(R.id.title);
		String body = interaction.getTitle(getContext());
		bodyView.setText(body);

		// No
		String noText = interaction.getNoText();
		ApptentiveDialogButton noButton = (ApptentiveDialogButton) v.findViewById(R.id.no);
		if (noText != null) {
			noButton.setText(noText);
		}
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_NO);
			}
		});

		// Yes
		String yesText = interaction.getYesText();
		ApptentiveDialogButton yesButton = (ApptentiveDialogButton) v.findViewById(R.id.yes);
		if (yesText != null) {
			yesButton.setText(yesText);
		}
		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_YES);
			}
		});
		return v;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();

		if (bundle != null) {
			String interactionString = bundle.getString("interaction");
			interaction = (EnjoymentDialogInteraction) Interaction.Factory.parseInteraction(interactionString);
			sectionTitle = null;
		}
	}

	@Override
	public boolean onBackPressed() {
		EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_CANCEL);
		return false;
	}
}