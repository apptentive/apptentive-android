/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;

import java.io.File;

/**
 * @author Sky Kelsey
 */
public class OperatorTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testOperatorExists() {
		Log.e("Running test: testOperatorExists()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "payloads/testOperatorExists.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testOperatorNot() {
		Log.e("Running test: testOperatorNot()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "payloads/testOperatorNot.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

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
		assertNull(interaction);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testOperatorContains() {
		Log.e("Running test: testOperatorContains()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "payloads/testOperatorContains.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 1
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 2
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 3
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 6
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testOperatorStartsWith() {
		Log.e("Running test: testOperatorStartsWith()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "payloads/testOperatorStartsWith.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

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

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}

	public void testOperatorEndsWith() {
		Log.e("Running test: testOperatorEndsWith()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "payloads/testOperatorEndsWith.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		PersonManager.storePersonEmail(getTargetContext(), "example@example.com");
		PersonManager.storePersonAndReturnIt(getTargetContext());
		DeviceManager.storeDeviceAndReturnIt(getTargetContext());

		// 0
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

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

		// 4
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNull(interaction);

		// 5
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "local#app#init");
		assertNotNull(interaction);
	}
}
