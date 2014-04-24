/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.test.InstrumentationTestCase;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class DataObjectQueryTest extends InstrumentationTestCase {

	private Context targetContext;

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	private Context getTargetContext() {
		if (targetContext == null) {
			targetContext = getInstrumentation().getTargetContext();
		}
		return targetContext;
	}

	private void resetDevice() {
		getTargetContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getTargetContext());
	}


	public void testPersonQueries() {
		Log.e("Running test: testPersonQueries()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testPersonQueries.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0
		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3
		Apptentive.addCustomPersonData(getTargetContext(), "foo", "bar");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

	}
}
