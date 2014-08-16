/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class DataObjectQueryTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testQueriesAgainstPerson() {
		Log.e("Running test: testQueriesAgainstPerson()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstPerson.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		Apptentive.addCustomPersonData(getTargetContext(), "foo", "bar");
		PersonManager.storePersonAndReturnIt(getTargetContext());

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

	public void testQueriesAgainstDevice() {
		Log.e("Running test: testQueriesAgainstDevice()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstDevice.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		Apptentive.addCustomDeviceData(getTargetContext(), "foo", "bar");
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

		// 0
		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

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
	}

	public void testQueriesAgainstAppRelease() {
		Log.e("Running test: testQueriesAgainstAppRelease()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstAppRelease.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		AppReleaseManager.storeAppReleaseAndReturnDiff(getTargetContext());

		// 0
		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

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

	public void testQueriesAgainstSdk() {
		Log.e("Running test: testQueriesAgainstSdk()\n\n");
		resetDevice();

		String json = FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstSdk.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("local#app#init");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		SdkManager.storeSdkAndReturnDiff(getTargetContext());

		// 0
		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
	}
}
