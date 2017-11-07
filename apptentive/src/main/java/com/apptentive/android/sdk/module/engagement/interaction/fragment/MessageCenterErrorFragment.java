/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;


public class MessageCenterErrorFragment extends ApptentiveBaseFragment<Interaction> {

	private static final String EVENT_NAME_NO_INTERACTION_CLOSE = "no_interaction_close";
	private static final String EVENT_NAME_NO_INTERACTION_NO_INTERNET = "no_interaction_no_internet";
	private static final String EVENT_NAME_NO_INTERACTION_ATTEMPTING = "no_interaction_attempting";

	private View progress;
	private View root;

	public static MessageCenterErrorFragment newInstance(Bundle bundle) {
		MessageCenterErrorFragment fragment = new MessageCenterErrorFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void sendLaunchEvent(Activity activity) {
		if (wasLastAttemptServerError(getContext()) || Util.isNetworkConnectionPresent()) {
			engage("com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_ATTEMPTING, null, null, (ExtendedData[]) null);
		} else {
			engage("com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_NO_INTERNET, null, null, (ExtendedData[]) null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.apptentive_message_center_error, container, false);
		progress = root.findViewById(R.id.config_loading_progress);

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateStatus();
	}


	@Override
	public void onInteractionUpdated(boolean successful) {
		if (successful && Apptentive.canShowMessageCenter()) {
			ApptentiveInternal.getInstance().showMessageCenterInternal(getActivity(), null);
			transit();
		} else {
			updateStatus();
		}
	}

	private boolean wasLastAttemptServerError(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, false);
	}


	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		engage("com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_CLOSE, exitTypeToDataJson(exitType), null, (ExtendedData[]) null);
		return false;
	}

	private void updateStatus() {
		if (wasLastAttemptServerError(getContext()) || Util.isNetworkConnectionPresent()) {
			progress.setVisibility(View.VISIBLE);
			((AppCompatImageView) root.findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_ic_error);
			((TextView) root.findViewById(R.id.message)).setText(R.string.apptentive_message_center_server_error);
		} else {
			progress.setVisibility(View.GONE);
			((AppCompatImageView) root.findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_ic_no_connection);
			((TextView) root.findViewById(R.id.message)).setText(R.string.apptentive_message_center_no_connection);
		}
	}
}