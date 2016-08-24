/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class DataObjectQueryTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement/payloads/";

	@Test
	public void queriesAgainstPerson() {
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstPerson.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		PersonManager.storePersonEmail("example@example.com");
		Apptentive.addCustomPersonData("foo", "bar");
		PersonManager.storePersonAndReturnIt();

		Interaction interaction;

		// 0
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 1
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNull(interaction);

		// 2
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 3
		Apptentive.addCustomPersonData("foo", "bar");
		PersonManager.storePersonAndReturnIt();
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 4
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNull(interaction);

		// 4
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);
	}

	@Test
	public void queriesAgainstDevice() {
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstDevice.json");
		json = json.replace("\"OS_API_LEVEL\"", String.valueOf(Build.VERSION.SDK_INT));
		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction;

		Apptentive.addCustomDeviceData("foo", "bar");
		DeviceManager.storeDeviceAndReturnIt();

		// 0
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 1
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 2
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNull(interaction);

		// 3
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 4
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);
	}

	@Test
	public void queriesAgainstSdk() {
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(TEST_DATA_DIR + "testQueriesAgainstSdk.json");
		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction;

		SdkManager.storeSdkAndReturnDiff();

		// 0
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 1
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNotNull(interaction);

		// 2
		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		interaction = interactionManager.getApplicableInteraction("local#app#init");
		assertNull(interaction);
	}
}
