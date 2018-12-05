/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

//NON UI Fragment
public class NavigateToLinkFragment extends ApptentiveBaseFragment<NavigateToLinkInteraction> {

	public static NavigateToLinkFragment newInstance(Bundle bundle) {
		NavigateToLinkFragment fragment = new NavigateToLinkFragment();
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return null;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean success = false;
		try {
			String url = interaction.getUrl();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

			switch (interaction.getTarget()) {
				case New:
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					break;
				case Self:
					// Nothing
					break;
				default:
					break;
			}
			if (Util.canLaunchIntent(getContext(), intent)) {
				getActivity().startActivity(intent);
				success = true;
			}
		} catch (ActivityNotFoundException e) {
			ApptentiveLog.w(e, "NavigateToLink Error: ");
			logException(e);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreate()", getClass().getSimpleName());
			logException(e);
		} finally {
			JSONObject data = new JSONObject();
			try {
				data.put(NavigateToLinkInteraction.KEY_URL, interaction.getUrl());
				data.put(NavigateToLinkInteraction.KEY_TARGET, interaction.getTarget().lowercaseName());
				data.put(NavigateToLinkInteraction.EVENT_KEY_SUCCESS, success);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Error creating Event data object.");
				logException(e);
			}
			engageInternal(NavigateToLinkInteraction.EVENT_NAME_NAVIGATE, data.toString());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		transit();
	}



	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		return false;
	}

	@Override
	protected void sendLaunchEvent(Activity activity) {
		// This Interaction type does not send a launch Event.
	}
}