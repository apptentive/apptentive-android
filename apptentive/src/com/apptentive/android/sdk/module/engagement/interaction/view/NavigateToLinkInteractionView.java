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
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;

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
	public void show(Activity activity) {
		super.show(activity);

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
			String data = String.format("{\"url\":\"%s\",\"target\":\"%s\",\"success\":%b}", interaction.getUrl(), interaction.getTarget().lowercaseName(), success);
			EngagementModule.engageInternal(activity, interaction, NavigateToLinkInteraction.EVENT_NAME_NAVIGATE, data);
			// Always finish this Activity.
			activity.finish();
		}
	}

	@Override
	public void onStop() {
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		return true;
	}
}
