/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import com.apptentive.android.sdk.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testOperatorExists() {
		Log.e("Running test: testOperatorExists()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testOperatorExists.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		boolean canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}

	public void testOperatorNot() {
		Log.e("Running test: testOperatorNot()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testOperatorNot.json");
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

	public void testOperatorContains() {
		Log.e("Running test: testOperatorContains()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testOperatorContains.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

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

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 6
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}

	public void testOperatorStartsWith() {
		Log.e("Running test: testOperatorStartsWith()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testOperatorStartsWith.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

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
		assertFalse(canRun);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}

	public void testOperatorEndsWith() {
		Log.e("Running test: testOperatorEndsWith()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testOperatorEndsWith.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

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
		assertFalse(canRun);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}


}
