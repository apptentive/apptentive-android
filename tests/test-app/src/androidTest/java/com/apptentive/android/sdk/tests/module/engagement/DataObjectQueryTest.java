/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.SdkManager;

/**
 * @author Sky Kelsey
 */
public class DataObjectQueryTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement/payloads/";

	public void testQueriesAgainstPerson() {
		ApptentiveLog.e("Running test: testQueriesAgainstPerson()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstPerson.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);

		PersonManager.storePersonEmail( "example@example.com");
		Apptentive.addCustomPersonData( "foo", "bar");
		PersonManager.storePersonAndReturnIt();

		Interaction interaction;

		// 0
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 1
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNull(interaction);

		// 2
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 3
		Apptentive.addCustomPersonData( "foo", "bar");
		PersonManager.storePersonAndReturnIt();
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 4
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNull(interaction);

		// 4
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);
	}

	public void testQueriesAgainstDevice() {
		ApptentiveLog.e("Running test: testQueriesAgainstDevice()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstDevice.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);

		Interaction interaction;

		Apptentive.addCustomDeviceData( "foo", "bar");
		DeviceManager.storeDeviceAndReturnIt();

		// 0
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 1
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 2
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNull(interaction);

		// 3
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);
	}

	public void testQueriesAgainstSdk() {
		ApptentiveLog.e("Running test: testQueriesAgainstSdk()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testQueriesAgainstSdk.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);

		Interaction interaction;

		SdkManager.storeSdkAndReturnDiff();

		// 0
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 1
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNotNull(interaction);

		// 2
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction( "local#app#init");
		assertNull(interaction);
	}
}
