/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.SdkManager;

/**
 * @author Sky Kelsey
 */
public class DataObjectQueryTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement/payloads/";

	public void testQueriesAgainstPerson() {
		Log.e("Running test: testQueriesAgainstPerson()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstPerson.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		Apptentive.addCustomPersonData(getTargetContext(), "foo", "bar");
		PersonManager.storePersonAndReturnIt(getTargetContext());

		Interaction interaction;

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 3
		Apptentive.addCustomPersonData(getTargetContext(), "foo", "bar");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testQueriesAgainstDevice() {
		Log.e("Running test: testQueriesAgainstDevice()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstDevice.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		Apptentive.addCustomDeviceData(getTargetContext(), "foo", "bar");
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testQueriesAgainstSdk() {
		Log.e("Running test: testQueriesAgainstSdk()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstSdk.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		SdkManager.storeSdkAndReturnDiff(getTargetContext());

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);
	}
}
