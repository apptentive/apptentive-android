/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TextModalInteractionTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement/interactions";

	@Test
	public void textModalInteraction() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "/testTextModalInteraction.json");
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
