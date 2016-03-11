/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;


public class MessageCenterErrorFragment extends ApptentiveBaseFragment<Interaction> implements ApptentiveBaseFragment.ConfigUpdateListener {

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!hasLaunched) {
			hasLaunched = true;
			if (wasLastAttemptServerError(getContext()) ||
					Util.isNetworkConnectionPresent()) {
				EngagementModule.engage(getActivity(), "com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_ATTEMPTING, null, null, (ExtendedData[]) null);
			} else {
				EngagementModule.engage(getActivity(), "com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_NO_INTERNET, null, null, (ExtendedData[]) null);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// create ContextThemeWrapper from the original Activity Context with the apptentive theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), ApptentiveInternal.getInstance().getApptentiveTheme());
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater themedInflater = inflater.cloneInContext(contextThemeWrapper);
		root = themedInflater.inflate(R.layout.apptentive_message_center_error, container, false);
		progress = root.findViewById(R.id.config_loading_progress);

		return root;
	}

	@Override
	public void onResume() {
		ApptentiveInternal.getInstance().addConfigUpdateListener(this);
		super.onResume();
		updateStatus();
	}

	@Override
	public void onPause() {
		ApptentiveInternal.getInstance().removeConfigUpdateListener(this);
		super.onPause();
	}

	@Override
	public void onConfigurationUpdated(boolean successful) {
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


	public boolean onBackPressed() {
		EngagementModule.engage(getActivity(), "com.apptentive", "MessageCenter", null, EVENT_NAME_NO_INTERACTION_CLOSE, null, null, (ExtendedData[]) null);
		return false;
	}

	private void updateStatus() {
		if (wasLastAttemptServerError(getContext()) ||
				Util.isNetworkConnectionPresent()) {
			progress.setVisibility(View.VISIBLE);
			((ImageView) root.findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_icon_server_error);
			((TextView) root.findViewById(R.id.message)).setText(R.string.apptentive_message_center_server_error);
		} else {
			progress.setVisibility(View.GONE);
			((ImageView) root.findViewById(R.id.icon)).setImageResource(R.drawable.apptentive_icon_no_connection);
			((TextView) root.findViewById(R.id.message)).setText(R.string.apptentive_message_center_no_connection);
		}
	}
}