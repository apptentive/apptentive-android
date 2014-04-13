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
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.EventManager;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.metric.MetricModule;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class EngagementModule {

	public static synchronized boolean engageInternal(Activity activity, String interaction, String codePoint) {
		return engage(activity, "com.apptentive", interaction, codePoint);
	}

	public static synchronized boolean engageInternal(Activity activity, String codePoint) {
		return engage(activity, "com.apptentive", "app", codePoint);
	}

	public static synchronized boolean engage(Activity activity, String vendor, String interaction, String codePointName) {
		try {
			String fullCodePointName = generateEventName(vendor, interaction, codePointName);
			Log.d("engage(%s)", fullCodePointName);

			CodePointStore.storeCodePointForCurrentAppVersion(activity.getApplicationContext(), fullCodePointName);
			EventManager.sendEvent(activity.getApplicationContext(), new Event(fullCodePointName, (Map<String, String>) null));
			return doEngage(activity, fullCodePointName);
		} catch (Exception e) {
			MetricModule.sendError(activity.getApplicationContext(), e, null, null);
		}
		return false;
	}

	public static boolean doEngage(Activity activity, String fullCodePoint) {
		Interaction interaction = InteractionManager.getApplicableInteraction(activity.getApplicationContext(), fullCodePoint);
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
			activity.overridePendingTransition(R.anim.slide_up_in, 0);
		}
	}

	public static String generateEventName(String vendor, String interaction, String codePointName) {
		return String.format("%s#%s#%s", encodeEventPart(vendor), encodeEventPart(interaction), encodeEventPart(codePointName));
	}

	/**
	 * Used only for encoding event names. DO NOT modify this method.
	 */
	private static String encodeEventPart(String input) {
		return input.replace("%", "%25").replace("/", "%2F").replace("#", "%23");
	}

}
