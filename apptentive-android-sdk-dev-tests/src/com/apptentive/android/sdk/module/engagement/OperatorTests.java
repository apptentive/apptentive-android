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
import com.apptentive.android.sdk.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends InstrumentationTestCase {

	private Context context;

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	private Context getTargetContext() {
		if (context == null) {
			context = getInstrumentation().getTargetContext();
		}
		return context;
	}

	private void resetDevice() {
		getTargetContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getTargetContext());
	}


	public void testExistsOperator() {
		Log.e("Running test: testExistsOperator()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testExistsOperator.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		boolean canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		Apptentive.setInitialUserEmail(getTargetContext(), "sky.kelsey@gmail.com");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}

	public void testContainsOperator() {
		Log.e("Running test: testContainsOperator()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testContainsOperator.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0
		boolean canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
	}

	public void testNotOperator() {
		Log.e("Running test: testNotOperator()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testNotOperator.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0
		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}
}
