/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.content.Context;
import android.text.format.DateUtils;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Util;

import java.io.File;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 *
 * @author Sky Kelsey
 */
public class InteractionTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testInteractionInvocationStorage() {
		Log.e("Running test: testInteractionInvocationStorage()\n\n");
		resetDevice();
		final String testInteraction = "test.interaction";
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "1.0", 1);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "1.1", 2);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 5);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.0", 5);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);
		CodePointStore.storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);

		long value = 0;

		value = CodePointStore.getTotalInvokes(getTargetContext(), true, testInteraction);
		assertEquals(value, 14);

		value = CodePointStore.getVersionInvokes(getTargetContext(), true, testInteraction, "1.0");
		assertEquals(value, 1);
		value = CodePointStore.getVersionInvokes(getTargetContext(), true, testInteraction, "1.1");
		assertEquals(value, 4);
		value = CodePointStore.getVersionInvokes(getTargetContext(), true, testInteraction, "2.0");
		assertEquals(value, 6);
		value = CodePointStore.getVersionInvokes(getTargetContext(), true, testInteraction, "2.1");
		assertEquals(value, 3);

		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "1");
		assertEquals(value, 1);
		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "2");
		assertEquals(value, 1);
		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "3");
		assertEquals(value, 3);
		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "4");
		assertEquals(value, 4);
		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "5");
		assertEquals(value, 2);
		value = CodePointStore.getBuildInvokes(getTargetContext(), true, testInteraction, "6");
		assertEquals(value, 3);

		Double lastInvoke = CodePointStore.getLastInvoke(getTargetContext(), true, testInteraction);
		assertFalse(lastInvoke.equals(0d));
		Log.e("Finished test.");
	}

	public void testCriteriaTimeSinceInstall() {
		Log.e("Running test: testCriteriaTimeSinceInstall()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaTimeSinceInstall.json");

		Interaction interaction;

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 2.8);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "1.1", Util.currentTimeSeconds() - 4);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 5, "2.0", Util.currentTimeSeconds() - 2.8);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "1.1", Util.currentTimeSeconds() - 2.8);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 5, "2.0", Util.currentTimeSeconds() - 1);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "2.0", Util.currentTimeSeconds() - 4);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 2.8);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "2.0", Util.currentTimeSeconds() - 2.8);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		Log.e("Finished test.");
	}

	public void testCriteriaApplicationVersionAndBuild() {
		Log.e("Running test: testCriteriaApplicationVersionAndBuild()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaApplicationVersionAndBuild.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesTotal() {
		Log.e("Running test: testCriteriaCodePointInvokesTotal()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaCodePointInvokesTotal.json");

		Interaction interaction;

		// 0 - $gt
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gt");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 1 - $gte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 2 - $ne
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 3 - $eq
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 4 - :
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 5 - $lte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 6 - $lt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesVersion() {
		Log.e("Running test: testCriteriaCodePointInvokesVersion()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaCodePointInvokesVersion.json");

		Interaction interaction;

		// 0 - $gt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		Log.e("Test $gt");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 1 - $gte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 2 - $ne
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 3 - $eq
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 4 - :
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 5 - $lte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 6 - $lt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lt");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesBuild() {
		Log.e("Running test: testCriteriaCodePointInvokesBuild()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaCodePointInvokesBuild.json");

		Interaction interaction;

		// 0 - $gt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gt");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 1 - $gte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 2 - $ne
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 3 - $eq
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 4 - :
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 5 - $lte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 6 - $lt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lt");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesTimeAgo() {
		Log.e("Running test: testCriteriaCodePointInvokesTimeAgo()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaCodePointInvokesTimeAgo.json");

		Interaction interaction;

		// 0 - $gt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gt");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 1 - $gte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 2 - $ne
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 3 - $eq // There's no easy way to test this unless we contrive the times.
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 4 - : // Ditto
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 5 - $lte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 6 - $lt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		sleep(300);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaInteractionInvokesTotal() {
		Log.e("Running test: testCriteriaInteractionInvokesTotal()\n\n");
		resetDevice();
		String appVersionName = Util.getAppVersionName(getTargetContext());
		int appVersionCode = Util.getAppVersionCode(getTargetContext());

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaInteractionInvokesTotal.json");

		Interaction interaction;

		// 0 - $gt
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gt");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 1 - $gte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 2 - $ne
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		// 3 - $eq
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 4 - :
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 5 - $lte
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 6 - $lt
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		Log.e("Finished test.");
	}


	public void testListOfVariousInteractions() {
		Log.e("Running test: testListOfVariousInteractions()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Interaction interaction;


		// Conditions are not met yet.
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 0, "1.0", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 10)); // 10 days ago
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 1, "1.1", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 8));  //  8 days ago
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 2, "1.2", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 6));  //  6 days ago
		//CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
		assertNull(interaction);

		// Allow it to run.
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 2));  //  2 days ago
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
		assertNotNull(interaction);

		Log.e("Finished test.");
	}

	public void testCriteriaProcessingPerformance() {
		Log.e("Running test: testCriteriaProcessingPerformance()");
		if (isRunningOnEmulator()) {
			Log.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 7d);
	}

	public void testInteractionSelectionPerformance() {
		Log.e("Running test: testInteractionSelectionPerformance()");
		if (isRunningOnEmulator()) {
			Log.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 7d);
	}

	public void testInteractionStorageAndSelectionPerformance() {
		Log.e("Running test: testInteractionStorageAndSelectionPerformance()");
		if (isRunningOnEmulator()) {
			Log.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 20;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
			InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 50d);
	}

	/* 
	 * Removed to stop intermittent Jenkins failures.
	 *
	public void testSavingCodePointAndCheckingForApplicableInteraction() {
		Log.e("Running test: testSavingCodePointAndCheckingForApplicableInteraction()");
		resetDevice();
		final int iterations = 100;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "non.existant.code.point");
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "non.existant.code.point");
			assertNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 20d);
	}*/

	public void testSelectionWithInteractionIdUsedInCriteria() {
		Log.e("Running test: testSelectionWithInteractionIdUsedInCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testSelectionWithInteractionIdUsedInCriteria.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNull(interaction);

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNotNull(interaction);

	}

	public void testInteractionPriority() {
		Log.e("Running test: testInteractionPriority()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testInteractionPriority.json");

		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000a");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000b");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000c");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNull(interaction);
	}

	public void testMissingNullEmptyCriteria() {
		Log.e("Running test: testMissingNullEmptyCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testMissingNullEmptyCriteria.json");

		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNull(interaction);

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNull(interaction);

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.3");
		assertNotNull(interaction);
	}

	public void testBadCriteria() {
		Log.e("Running test: testBadCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testBadCriteria.json");

		Interaction interaction;
		try {
			InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
			assertNull(interaction);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
			assertNull(interaction);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.3");
			assertNull(interaction);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.4");
			assertNull(interaction);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.5");
			assertNull(interaction);

			interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.6");
			assertNull(interaction);
		} catch (Exception e) {
			assertNull("An exception was thrown from bad criteria.", e);
		}
	}

	public void testCorruptedJson() {
		Log.e("Running test: testCorruptedJson()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCorruptedJson.json");

		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		Interactions interactions = InteractionManager.getInteractions(getTargetContext());
		assertNull(interactions);
	}

	public void testActualUpgradeMessage() {
		Log.e("Running test: testActualUpgradeMessage()");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testActualUpgradeMessage.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		Log.e("ONE");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 604800);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Just right
		Log.e("THREE");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNotNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Already shown
		Log.e("FOUR");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "by.build");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Test build targeted UpgradeMessage

		// Saw this version too long ago.
		Log.e("ONE");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 604800);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Just right
		Log.e("THREE");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNotNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Already shown
		Log.e("FOUR");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "by.version");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

	}

	/**
	 * Update this when the Rating Flow group of interactions changes, or with different permutations of that flow.
	 */
	public void testRealRatingInteractions() {
		Log.e("Running test: testRealRatingInteractions()");

		Context targetContext = getTargetContext();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testRealRatingInteractions.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Conditions not yet met.
		resetDevice();
		Log.e("ONE");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 100000);
		assertNull(InteractionManager.getApplicableInteraction(targetContext, "by.build"));

		// Conditions partially met.
		resetDevice();
		Log.e("TWO");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 500000);
		assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions partially met the other way.
		resetDevice();
		Log.e("THREE");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions almost met.
		resetDevice();
		Log.e("FOUR");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 430000);
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions met barely.
		resetDevice();
		Log.e("FIVE");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 432000);
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));


		//// Test Rating Dialog.

		// Conditions are always met.
		resetDevice();
		Log.e("SIX");
		InteractionManager.storeInteractionsPayloadString(targetContext, json);
		assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes"));

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			Log.e("SEVEN");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			CodePointStore.storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#remind");
			assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			Log.e("EIGHT");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			CodePointStore.storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			CodePointStore.storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 420000);
			assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Re-prompt is ready.
		{
			resetDevice();
			Log.e("NINE");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			CodePointStore.storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			CodePointStore.storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Don't re-prompt, since we've already rated.
		{
			resetDevice();
			Log.e("TEN");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			CodePointStore.storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			CodePointStore.storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#rate");
			assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			Log.e("ELEVEN");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			CodePointStore.storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			CodePointStore.storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			CodePointStore.storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#decline");
			assertNull(InteractionManager.getApplicableInteraction(targetContext, "local#app#init"));
		}

		//// Test Feedback Dialog

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			Log.e("TWELVE");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#no"));
		}

		//// Test Message Center

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			Log.e("THIRTEEN");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#FeedbackDialog#view_messages"));
		}
	}

	public void testWillShowInteraction() {
		Log.e("Running test: testWillShowInteraction()\n\n");
		resetDevice();

		ApptentiveInternal.setMinimumLogLevel(Log.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testWillShowInteraction.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		boolean willShow = Apptentive.willShowInteraction(getTargetContext(), "init");
		assertFalse(willShow);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		willShow = Apptentive.willShowInteraction(getTargetContext(), "init");
		assertTrue(willShow);
	}


}
