/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveViewActivity;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;

class ActivityInteractionLauncher implements InteractionLauncher {
	@Override
	public boolean launch(Context context, Interaction interaction) {
		Intent intent = new Intent();
		intent.setClass(context.getApplicationContext(), ApptentiveViewActivity.class);
		intent.putExtra(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.INTERACTION);
		intent.putExtra(Interaction.KEY_NAME, interaction.toString());
			/* non-activity context start an Activity, but it requires that a new task be created.
			 * This may fit specific use cases, but can create non-standard back stack behaviors in
			 * hosting application. non-activity context include application context, context from Service
			 * ContentProvider, and BroadcastReceiver
			 */
		if (!(context instanceof Activity)) {
			// check if any activity from the hosting app is running at foreground
			Activity activity = ApptentiveInternal.getInstance().getCurrentTaskStackTopActivity();
			if (activity != null) {
				context = activity;
			} else {
				// If no foreground activity from the host app, launch Apptentive interaction as a new task
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			}
		}
		context.startActivity(intent);

		return true;
	}
}
