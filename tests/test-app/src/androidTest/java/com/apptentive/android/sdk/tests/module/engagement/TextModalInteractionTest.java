/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class TextModalInteractionTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement/interactions";

	public void testTextModalInteraction() {
		Log.e("Running test: testTextModalInteraction()\n\n");

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(Log.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "/testTextModalInteraction.json");

		Interaction interaction = Interaction.Factory.parseInteraction(json);

		assertTrue(interaction.getType().equals(Interaction.Type.TextModal));

		TextModalInteraction textModalInteraction = (TextModalInteraction) interaction;
		assertEquals("548cc5dd49f63bb5c2000001", textModalInteraction.getId());
		assertEquals("External Deep Links", textModalInteraction.getTitle());
		assertEquals("This example allow testing how external deep links are opened.", textModalInteraction.getBody());
		List<Action> actions = textModalInteraction.getActions().getAsList();
		{
			Action action = actions.get(0);
			assertEquals("eBay - Same Task", action.getLabel());
			assertTrue(action.getType().equals(Action.Type.interaction));
		}
	}
}
