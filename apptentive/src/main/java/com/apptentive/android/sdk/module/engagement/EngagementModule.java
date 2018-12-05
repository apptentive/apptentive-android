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
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.model.EventPayload;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.util.threading.DispatchQueue.isMainQueue;
import static com.apptentive.android.sdk.util.threading.DispatchQueue.mainQueue;

/**
 * @author Sky Kelsey
 */
public class EngagementModule {

	// this field gets overridden in unit tests (if renamed - update the test)
	private static final InteractionLauncherFactory LAUNCHER_FACTORY = new DefaultInteractionLauncherFactory();

	public static synchronized boolean engageInternal(Context context, Conversation conversation, String eventName) {
		return engage(context, conversation, "com.apptentive", "app", null, eventName, null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, Conversation conversation, String eventName, String data) {
		return engage(context, conversation, "com.apptentive", "app", null, eventName, data, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, Conversation conversation, Interaction interaction, String eventName) {
		return engage(context, conversation, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, null, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engageInternal(Context context, Conversation conversation, Interaction interaction, String eventName, String data) {
		return engage(context, conversation, "com.apptentive", interaction.getType().name(), interaction.getId(), eventName, data, null, (ExtendedData[]) null);
	}

	public static synchronized boolean engage(Context context, Conversation conversation, String vendor, String interaction, String interactionId, String eventName, String data, Map<String, Object> customData, ExtendedData... extendedData) {
		checkConversationQueue();

		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (conversation == null) {
			throw new IllegalArgumentException("Conversation is null");
		}

		Assert.assertTrue(ApptentiveInternal.isApptentiveRegistered());
		if (!ApptentiveInternal.isApptentiveRegistered()) {
			return false;
		}

		try {
			String eventLabel = generateEventLabel(vendor, interaction, eventName);
			ApptentiveLog.i(INTERACTIONS, "Engage event: '%s'", eventLabel);

			String versionName = ApptentiveInternal.getInstance().getApplicationVersionName();
			int versionCode = ApptentiveInternal.getInstance().getApplicationVersionCode();
			conversation.getEventData().storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, eventLabel);
			conversation.addPayload(new EventPayload(eventLabel, interactionId, data, customData, extendedData));
			return doEngage(conversation, context, eventLabel);
		} catch (Exception e) {
			ApptentiveLog.e(INTERACTIONS, e, "Exception while engaging event '%s'", eventName);
			logException(e);
		}
		return false;
	}

	private static boolean doEngage(Conversation conversation, Context context, String eventLabel) {
		checkConversationQueue();

		Interaction interaction = conversation.getApplicableInteraction(eventLabel, true);
		if (interaction != null) {
			String versionName = ApptentiveInternal.getInstance().getApplicationVersionName();
			int versionCode = ApptentiveInternal.getInstance().getApplicationVersionCode();
			conversation.getEventData().storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, interaction.getId());
			launchInteraction(context, interaction);
			return true;
		}
		ApptentiveLog.d(INTERACTIONS, "No interaction to show for event: '%s'", eventLabel);
		return false;
	}

	public static void launchInteraction(final Context context, final Interaction interaction) {
		if (context == null) {
			ApptentiveLog.e("Unable to launch interaction: context is null"); // TODO: throw an exception instead?
			return;
		}

		if (interaction == null) {
			ApptentiveLog.e("Unable to launch interaction: interaction instance is null"); // TODO: throw an exception instead?
			return;
		}

		if (!isMainQueue()) {
			mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					launchInteraction(context, interaction);
				}
			});
			return;
		}

		try {
			ApptentiveLog.i(INTERACTIONS, "Launching interaction: '%s'", interaction.getType());
			InteractionLauncher launcher = LAUNCHER_FACTORY.launcherForInteraction(interaction);
			if (launcher != null) {
				boolean launched = launcher.launch(context, interaction);
				ApptentiveLog.d("Interaction %slaunched", launched ? "" : "NOT ");
			} else {
				ApptentiveLog.e("Interaction not launched: can't create launcher for interaction: %s", interaction);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while launching interaction: %s", interaction);
			logException(e);
		}
	}

	public static void launchMessageCenterErrorActivity(final Context context) {
		if (!isMainQueue()) {
			mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					launchMessageCenterErrorActivity(context);
				}
			});
			return;
		}

		if (context != null) {
			Intent intent = MessageCenterInteraction.generateMessageCenterErrorIntent(context);
			if (!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			}
			context.startActivity(intent);
		}
	}

	public static boolean canShowInteraction(Conversation conversation, String interaction, String eventName, String vendor) {
		String eventLabel = generateEventLabel(vendor, interaction, eventName);
		return canShowInteraction(conversation, eventLabel);
	}

	private static boolean canShowInteraction(Conversation conversation, String eventLabel) {
		checkConversationQueue();

		if (conversation == null) {
			throw new IllegalArgumentException("Conversation is null");
		}

		Interaction interaction = conversation.getApplicableInteraction(eventLabel, false);
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

	private static void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: more context data
	}
}
