/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.app.Activity;
import android.content.Intent;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

/**
 * @author Sky Kelsey
 */
public class EngagementModule {

	public static boolean engage(Activity activity, String codePoint) {
		Interaction interaction = InteractionManager.getApplicableInteraction(activity.getApplicationContext(), codePoint);
		if (interaction != null) {
			CodePointStore.storeInteractionForCurrentAppVersion(activity, interaction.getId());
			launchInteraction(activity, interaction);
			return true;
		}
		Log.d("No interaction to show.");
		return false;
	}

	public static void launchInteraction(Activity activity, Interaction interaction) {
		if (interaction != null) {
			Log.e("Launching interaction: %s", interaction.getType().toString());
			Intent intent = new Intent();
			intent.setClass(activity, ViewActivity.class);
			intent.putExtra(ActivityContent.KEY, ActivityContent.Type.INTERACTION.toString());
			intent.putExtra(Interaction.KEY_NAME, interaction.toString());
			activity.startActivity(intent);
			activity.overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
		}
	}
}
