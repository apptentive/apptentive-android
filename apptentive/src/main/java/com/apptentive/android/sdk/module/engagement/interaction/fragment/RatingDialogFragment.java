/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;

public class RatingDialogFragment extends ApptentiveBaseFragment<RatingDialogInteraction> {

	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_RATE = "rate";
	private static final String CODE_POINT_REMIND = "remind";
	private static final String CODE_POINT_DECLINE = "decline";

	public static RatingDialogFragment newInstance(Bundle bundle) {
		RatingDialogFragment ratingFragment = new RatingDialogFragment();
		ratingFragment.setArguments(bundle);
		return ratingFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.apptentive_rating_dialog_interaction, container, false);

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) v.findViewById(R.id.title);
			titleView.setText(title);
		}

		TextView bodyView = (TextView) v.findViewById(R.id.body);
		String body = interaction.getBody(getContext());
		bodyView.setText(body);

		// Rate
		Button rateButton = (Button) v.findViewById(R.id.rate);
		String rate = interaction.getRateText(getContext());
		rateButton.setText(rate);
		// Make Rate button the default activated button with possible highlight color if specified in theme
		rateButton.setActivated(true);
		rateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_RATE);
				transit();
			}
		});

		// Remind
		Button remindButton = (Button) v.findViewById(R.id.remind);
		String remind = interaction.getRemindText();
		if (remind != null) {
			remindButton.setText(remind);
		}
		remindButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_REMIND);
				transit();
			}
		});

		// Decline
		Button declineButton = (Button) v.findViewById(R.id.decline);
		String decline = interaction.getDeclineText();
		if (decline != null) {
			declineButton.setText(decline);
		}
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_DECLINE);
				transit();
			}
		});
		return v;
	}

	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		EngagementModule.engageInternal(getActivity(), interaction, CODE_POINT_CANCEL, exitTypeToDataJson(exitType));
		return false;
	}
}