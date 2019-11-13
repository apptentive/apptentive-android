/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;
import com.apptentive.android.sdk.InstrumentationTestCaseBase;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.notification.DefaultInteractionNotificationBroadcastReceiverHandler;
import com.apptentive.android.sdk.module.engagement.notification.NoteInteractionNotificationAdapter;
import com.apptentive.android.sdk.util.RuntimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_CHANNEL_DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class NoteInteractionNotificationAdapterTest extends InstrumentationTestCaseBase {

	@Test
	public void testNoteNotificationDisplay() throws Exception {
		final String interactionDefinition = "{\"type\":\"TextModal\",\"name\":\"TextModal Interaction displayed as Notification\",\"id\":\"textmodal_interaction_notification\",\"display_type\":\"notification\",\"priority\":1,\"configuration\":{\"title\":\"Note Title\",\"body\":\"Note body\",\"actions\":[{\"id\":\"action_id_1\",\"label\":\"Dismiss\",\"action\":\"dismiss\"},{\"id\":\"action_id_2\",\"label\":\"Dismiss\",\"action\":\"dismiss\"}]}}";
		Interaction interaction = new TextModalInteraction(interactionDefinition);

		RuntimeUtils.overrideStaticFinalField(DefaultInteractionNotificationBroadcastReceiverHandler.class, "DEFAULT_ADAPTER_NOTE", new NoteInteractionNotificationAdapter() {
			@Override
			protected void actionDisplayNotification(Context context, String channelId, TextModalInteraction interaction) {
				assertNotNull(context);
				assertEquals(NOTIFICATION_CHANNEL_DEFAULT, channelId);
				assertNotNull(interaction);
				assertEquals(Interaction.Type.TextModal, interaction.getType());
				assertEquals("Note Title", interaction.getTitle());
				assertEquals("Note body", interaction.getBody());
			}
		});
		EngagementModule.launchInteraction(getContext(), interaction);
	}
}