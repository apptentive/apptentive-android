/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.content.Intent;
import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.InstrumentationTestCaseBase;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.notification.ApptentiveNotificationInteractionBroadcastReceiver;
import com.apptentive.android.sdk.module.engagement.notification.DefaultInteractionNotificationBroadcastReceiverHandler;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.RuntimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class InteractionNotificationBroadcastReceiverHandlerTest extends InstrumentationTestCaseBase {

	@Test
	public void testNoteNotification() throws Exception {
		final String interactionDefinition = "{\"type\":\"TextModal\",\"name\":\"TextModal Interaction displayed as Notification\",\"id\":\"textmodal_interaction_notification\",\"displayType\":\"Notification\",\"priority\":1,\"configuration\":{\"title\":\"Note Title\",\"body\":\"Note body\",\"actions\":[{\"id\":\"action_id_1\",\"label\":\"Dismiss\",\"action\":\"dismiss\"},{\"id\":\"action_id_2\",\"label\":\"Dismiss\",\"action\":\"dismiss\"}]}}";
		Interaction interaction = new TextModalInteraction(interactionDefinition);

		RuntimeUtils.overrideStaticFinalField(ApptentiveNotificationInteractionBroadcastReceiver.class, "DEFAULT_HANDLER", new DefaultInteractionNotificationBroadcastReceiverHandler() {
			@Override
			public void handleBroadcast(Context context, Intent intent) {
				assertNotNull(intent);
				assertEquals(Constants.NOTIFICATION_ACTION_DISPLAY, intent.getAction());
				assertEquals(interactionDefinition, intent.getStringExtra(Constants.NOTIFICATION_EXTRA_INTERACTION_DEFINITION));
			}
		});
		EngagementModule.launchInteraction(getContext(), interaction);
	}
}
