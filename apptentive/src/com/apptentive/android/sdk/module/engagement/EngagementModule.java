/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.EventManager;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.metric.MetricModule;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class EngagementModule {

	public static synchronized boolean engageInternal(Activity activity, String eventName) {
		return engage(activity, "com.apptentive", "app", null, eventName,  null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Activity activity, Interaction interaction, String eventName) {
		return engage(activity, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Activity activity, Interaction interaction, String eventName, String data) {
		return engage(activity, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, data, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engage(Activity activity, String vendor, String interaction, String interactionId, String eventName, String data, Map<String, Object> customData, ExtendedData... extendedData) {
		try {
			String eventLabel = generateEventLabel(vendor, interaction, eventName);
			Log.d("engage(%s)", eventLabel);

			CodePointStore.storeCodePointForCurrentAppVersion(activity.getApplicationContext(), eventLabel);
			EventManager.sendEvent(activity.getApplicationContext(), new Event(eventLabel, interactionId, data, customData, extendedData));
			return doEngage(activity, eventLabel);
		} catch (Exception e) {
			MetricModule.sendError(activity.getApplicationContext(), e, null, null);
		}
		return false;
	}

	public static boolean doEngage(Activity activity, String eventLabel) {
		Interaction interaction = InteractionManager.getApplicableInteraction(activity.getApplicationContext(), eventLabel);
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
			Log.i("Launching interaction: %s", interaction.getType().toString());
			Intent intent = new Intent();
			intent.setClass(activity, ViewActivity.class);
			intent.putExtra(ActivityContent.KEY, ActivityContent.Type.INTERACTION.toString());
			intent.putExtra(Interaction.KEY_NAME, interaction.toString());
			activity.startActivity(intent);
			activity.overridePendingTransition(R.anim.slide_up_in, 0);
		}
	}

	public static boolean willShowInteraction(Context context, String vendor, String interaction, String eventName) {
		String eventLabel = generateEventLabel(vendor, interaction, eventName);
		return willShowInteraction(context, eventLabel);
	}

	private static boolean willShowInteraction(Context context, String eventLabel) {
		Interaction interaction = InteractionManager.getApplicableInteraction(context, eventLabel);
		return interaction != null;
	}


	public static String generateEventLabel(String vendor, String interaction, String eventName) {
		return String.format("%s#%s#%s", encodeEventLabelPart(vendor), encodeEventLabelPart(interaction), encodeEventLabelPart(eventName));
	}

	/**
	 * Used only for encoding event names. DO NOT modify this method.
	 */
	private static String encodeEventLabelPart(String input) {
		return input.replace("%", "%25").replace("/", "%2F").replace("#", "%23");
	}
}
