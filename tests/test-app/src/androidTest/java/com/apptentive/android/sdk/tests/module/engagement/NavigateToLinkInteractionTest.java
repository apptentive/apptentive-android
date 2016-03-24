/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class NavigateToLinkInteractionTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement/interactions";

	public void testNavigateToLinkInteractionNew() {
		ApptentiveLog.e("Running test: testNavigateToLinkInteractionNew()\n\n");

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "/testNavigateToLinkInteractionNew.json");

		Interaction interaction = Interaction.Factory.parseInteraction(json);

		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));

		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;

		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.New));
	}

	public void testNavigateToLinkInteractionSelf() {
		ApptentiveLog.e("Running test: testNavigateToLinkInteractionSelf()\n\n");

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "/testNavigateToLinkInteractionSelf.json");

		Interaction interaction = Interaction.Factory.parseInteraction(json);

		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));

		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;

		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.Self));
	}

	public void testNavigateToLinkInteractionMissing() {
		ApptentiveLog.e("Running test: testNavigateToLinkInteractionNew()\n\n");

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "/testNavigateToLinkInteractionMissing.json");

		Interaction interaction = Interaction.Factory.parseInteraction(json);

		assertTrue(interaction.getType().equals(Interaction.Type.NavigateToLink));

		NavigateToLinkInteraction link = (NavigateToLinkInteraction) interaction;

		assertEquals("http://pages.ebay.com/link/?nav=item.view&id=221648890812", link.getUrl());
		assertTrue(link.getTarget().equals(NavigateToLinkInteraction.Target.New));
	}
}
