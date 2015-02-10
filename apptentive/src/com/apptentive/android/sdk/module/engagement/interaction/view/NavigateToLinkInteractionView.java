/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This interaction doesn't display its own UI. It just launches a URL.
 *
 * @author Sky Kelsey
 */
public class NavigateToLinkInteractionView extends InteractionView<NavigateToLinkInteraction> {

	public NavigateToLinkInteractionView(NavigateToLinkInteraction interaction) {
		super(interaction);
	}

	@Override
	public void doOnCreate(Activity activity, Bundle savedInteraction) {
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

			activity.startActivity(intent);
			success = true;
		} catch (ActivityNotFoundException e) {
			Log.w("NavigateToLink Error: ", e);
		} finally {
			JSONObject data = new JSONObject();
			try {
				data.put(NavigateToLinkInteraction.KEY_URL, interaction.getUrl());
				data.put(NavigateToLinkInteraction.KEY_TARGET, interaction.getTarget().lowercaseName());
				data.put(NavigateToLinkInteraction.EVENT_KEY_SUCCESS, success);
			} catch (JSONException e) {
				Log.e("Error creating Event data object.", e);
			}
			EngagementModule.engageInternal(activity, interaction, NavigateToLinkInteraction.EVENT_NAME_NAVIGATE, data.toString());
			// Always finish this Activity.
			activity.finish();
		}
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		return true;
	}
}
