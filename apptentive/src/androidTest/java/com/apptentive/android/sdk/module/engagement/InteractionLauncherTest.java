/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.InstrumentationTestCaseBase;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction.DisplayType;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.util.RuntimeUtils;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class InteractionLauncherTest extends InstrumentationTestCaseBase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		RuntimeUtils.overrideStaticFinalField(EngagementModule.class, "LAUNCHER_FACTORY", new DefaultInteractionLauncherFactory() {
			@NonNull
			@Override
			protected InteractionLauncher createActivityInteractionLauncher() {
				return new MockInteractionLauncher("Activity");
			}

			@NonNull
			@Override
			protected InteractionLauncher createNotificationInteractionLauncher() {
				return new MockInteractionLauncher("Notification");
			}
		});

		// Everything should run immediately
		overrideMainQueue(true);
	}

	@Test
	public void testInteractionDefaultDisplayType() throws JSONException {
		Interaction interaction = new TextModalInteraction("{\"type\":\"TextModal\"}");
		assertEquals(interaction.getDisplayType(), DisplayType.unknown);
		EngagementModule.launchInteraction(getContext(), interaction);
		assertResult("Activity");
	}

	@Test
	public void testInteractionNotificationDisplayType() throws JSONException {
		Interaction interaction = new TextModalInteraction("{\"type\":\"TextModal\",\"display_type\":\"notification\"}");
		assertEquals(interaction.getDisplayType(), DisplayType.notification);
		EngagementModule.launchInteraction(getContext(), interaction);
		assertResult("Notification");
	}

	@Test
	public void testInteractionUnknownDisplayType() throws JSONException {
		Interaction interaction = new TextModalInteraction("{\"type\":\"TextModal\",\"display_Type\":\"unknown\"}");
		assertEquals(interaction.getDisplayType(), DisplayType.unknown);
		EngagementModule.launchInteraction(getContext(), interaction);
		assertResult("Activity");
	}

	class MockInteractionLauncher implements InteractionLauncher {
		private final String name;

		MockInteractionLauncher(String name) {
			this.name = name;
		}

		@Override
		public boolean launch(Context context, Interaction interaction) {
			addResult(name);
			return true;
		}
	}
}
