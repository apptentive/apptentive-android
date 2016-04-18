/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewActivity;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.EventManager;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class EngagementModule {

	public static synchronized boolean engageInternal(Context context, String eventName) {
		return engage(context, "com.apptentive", "app", null, eventName, null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, String eventName, String data) {
		return engage(context, "com.apptentive", "app", null, eventName, data, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, Interaction interaction, String eventName) {
		return engage(context, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, Interaction interaction, String eventName, String data) {
		return engage(context, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, data, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engage(Context context, String vendor, String interaction, String interactionId, String eventName, String data, Map<String, Object> customData, ExtendedData... extendedData) {
		if (!ApptentiveInternal.isApptentiveRegistered() || context == null) {
			return false;
		}
		try {
			String eventLabel = generateEventLabel(vendor, interaction, eventName);
			ApptentiveLog.d("engage(%s)", eventLabel);

			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion(eventLabel);
			EventManager.sendEvent(new Event(eventLabel, interactionId, data, customData, extendedData));
			return doEngage(context, eventLabel);
		} catch (Exception e) {
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	public static boolean doEngage(Context context, String eventLabel) {
		Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction(eventLabel);
		if (interaction != null) {
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());
			launchInteraction(context, interaction);
			return true;
		}
		ApptentiveLog.d("No interaction to show.");
		return false;
	}

	public static void launchInteraction(Context context, Interaction interaction) {
		if (interaction != null) {
			ApptentiveLog.i("Launching interaction: %s", interaction.getType().toString());
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
				// check if any activity from the hosting app is running
				Activity activity = ApptentiveInternal.getInstance().getCurrentTaskStackBottomActivity();
				if (activity != null) {
					context = activity;
				} else {
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				}
			}
			context.startActivity(intent);
		}
	}

	public static void launchMessageCenterErrorActivity(Context context) {
		Intent intent = MessageCenterInteraction.generateMessageCenterErrorIntent(context);
		if (!(context instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		}
		context.startActivity(intent);
	}

	public static boolean canShowInteraction(String vendor, String interaction, String eventName) {
		String eventLabel = generateEventLabel(vendor, interaction, eventName);
		return canShowInteraction(eventLabel);
	}

	private static boolean canShowInteraction(String eventLabel) {
		Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction(eventLabel);
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
