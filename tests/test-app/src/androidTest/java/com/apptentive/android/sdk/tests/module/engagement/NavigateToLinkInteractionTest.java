/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class NavigateToLinkInteractionTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement/interactions";

	@Test
	public void navigateToLinkInteractionNew() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "/testNavigateToLinkInteractionNew.json");
		Interaction interaction = Interaction.Factory.parseInteraction(json);
		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));
		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;
		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.New));
	}

	@Test
	public void navigateToLinkInteractionSelf() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "/testNavigateToLinkInteractionSelf.json");
		Interaction interaction = Interaction.Factory.parseInteraction(json);
		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));
		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;
		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.Self));
	}

	@Test
	public void navigateToLinkInteractionMissing() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "/testNavigateToLinkInteractionMissing.json");
		Interaction interaction = Interaction.Factory.parseInteraction(json);
		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));
		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;
		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.New));
	}
}
