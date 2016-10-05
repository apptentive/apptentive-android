/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.File;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 */
public class InteractionTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	public void testInteractionInvocationStorage() {
		ApptentiveLog.e("Running test: testInteractionInvocationStorage()\n\n");
		resetDevice();
		final String testInteraction = "test.interaction";
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "1.0", 1);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "1.1", 2);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "1.1", 3);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "1.1", 3);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "1.1", 3);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 4);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 4);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 4);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 4);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 5);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.0", 5);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.1", 6);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.1", 6);
		ApptentiveInternal.getInstance().getCodePointStore().storeRecord(true, testInteraction, "2.1", 6);

		long value = 0;

		value = ApptentiveInternal.getInstance().getCodePointStore().getTotalInvokes(true, testInteraction);
		assertEquals(value, 14);

		value = ApptentiveInternal.getInstance().getCodePointStore().getVersionInvokes(true, testInteraction, "1.0");
		assertEquals(value, 1);
		value = ApptentiveInternal.getInstance().getCodePointStore().getVersionInvokes(true, testInteraction, "1.1");
		assertEquals(value, 4);
		value = ApptentiveInternal.getInstance().getCodePointStore().getVersionInvokes(true, testInteraction, "2.0");
		assertEquals(value, 6);
		value = ApptentiveInternal.getInstance().getCodePointStore().getVersionInvokes(true, testInteraction, "2.1");
		assertEquals(value, 3);

		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "1");
		assertEquals(value, 1);
		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "2");
		assertEquals(value, 1);
		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "3");
		assertEquals(value, 3);
		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "4");
		assertEquals(value, 4);
		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "5");
		assertEquals(value, 2);
		value = ApptentiveInternal.getInstance().getCodePointStore().getBuildInvokes(true, testInteraction, "6");
		assertEquals(value, 3);

		Double lastInvoke = ApptentiveInternal.getInstance().getCodePointStore().getLastInvoke(true, testInteraction);
		assertFalse(lastInvoke.equals(0d));
		ApptentiveLog.e("Finished test.");
	}

	public void testCriteriaTimeAtInstall() {
		ApptentiveLog.e("Running test: testCriteriaTimeAtInstall()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaTimeAtInstall.json");

		Interaction interaction;

		// 0
		resetDevice();

		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 1
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 5);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 3);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 2
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 3
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNotNull(interaction);


		ApptentiveLog.e("Finished test.");
	}

	public void testCriteriaApplicationVersion() {
		ApptentiveLog.e("Running test: testCriteriaApplicationVersion()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaApplicationVersion.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		Interaction interaction;

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("app.launch");
		assertNull(interaction);

		ApptentiveLog.e("Finished test.");
	}

	public void testCriteriaApplicationVersionCode() {
		ApptentiveLog.e("Running test: testCriteriaApplicationVersionCode()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionCode.json");

		try {
			PackageInfo packageInfo = getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(), 0);
			json = json.replace("\"APPLICATION_VERSION_CODE\"", String.valueOf(packageInfo.versionCode));

			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (Exception e) {
			ApptentiveLog.e("Error running test.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	public void testCriteriaApplicationVersionName() {
		ApptentiveLog.e("Running test: testCriteriaApplicationVersionName()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionName.json");

		try {
			PackageInfo packageInfo = getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(), 0);
			json = json.replace("APPLICATION_VERSION_NAME", packageInfo.versionName);

			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (Exception e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	public void testCriteriaProcessingPerformance() {
		ApptentiveLog.e("Running test: testCriteriaProcessingPerformance()");
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 7d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		ApptentiveLog.e(message);
		assertTrue(message, average < limit);
	}

	public void testInteractionSelectionPerformance() {
		ApptentiveLog.e("Running test: testInteractionSelectionPerformance()");
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 7d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		ApptentiveLog.e(message);
		assertTrue(message, average < limit);
	}

	public void testInteractionStorageAndSelectionPerformance() {
		ApptentiveLog.e("Running test: testInteractionStorageAndSelectionPerformance()");
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 20;

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("app.launch");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 50d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		ApptentiveLog.e(message);
		assertTrue(message, average < limit);
	}

	public void testSavingCodePointAndCheckingForApplicableInteraction() {
		ApptentiveLog.e("Running test: testSavingCodePointAndCheckingForApplicableInteraction()");
		resetDevice();
		final int iterations = 100;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("non.existant.code.point");
			Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("non.existant.code.point");
			assertNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 20d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		ApptentiveLog.e(message);
		assertTrue(message, average < limit);
	}

	public void testSelectionWithInteractionIdUsedInCriteria() {
		ApptentiveLog.e("Running test: testSelectionWithInteractionIdUsedInCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testSelectionWithInteractionIdUsedInCriteria.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.2");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.2");
		assertNotNull(interaction);

	}

	public void testInteractionPriority() {
		ApptentiveLog.e("Running test: testInteractionPriority()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testInteractionPriority.json");

		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000a");
		ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000b");
		ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000c");
		ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNull(interaction);
	}

	public void testMissingNullEmptyCriteria() {
		ApptentiveLog.e("Running test: testMissingNullEmptyCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testMissingNullEmptyCriteria.json");

		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		Interaction interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.1");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.2");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("code.point.3");
		assertNotNull(interaction);
	}

	public void testCorruptedJson() {
		ApptentiveLog.e("Running test: testCorruptedJson()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCorruptedJson.json");

		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		Interactions interactions = ApptentiveInternal.getInstance().getInteractionManager().getInteractions();
		assertNull(interactions);
	}

	public void testActualUpgradeMessage() {
		ApptentiveLog.e("Running test: testActualUpgradeMessage()");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testActualUpgradeMessage.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		ApptentiveLog.e("ONE");
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 600000);
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("event_label"));

		// Haven't upgraded
		ApptentiveLog.e("TWO");
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("event_label"));

		// Just right
		ApptentiveLog.e("THREE");
		resetDevice();
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNotNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("event_label"));

		// Already shown
		ApptentiveLog.e("FOUR");
		interaction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("event_label");
		ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(interaction.getId());
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("event_label"));
	}

	/**
	 * Update this when the Rating Flow group of interactions changes, or with different permutations of that flow.
	 */
	public void testRealRatingInteractions() {
		ApptentiveLog.e("Running test: testRealRatingInteractions()");

		Context targetContext = getTargetContext();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testRealRatingInteractions.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Conditions not yet met.
		resetDevice();
		ApptentiveLog.e("ONE");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 100000);
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("by.build"));

		// Conditions partially met.
		resetDevice();
		ApptentiveLog.e("TWO");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 500000);
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));

		// Conditions partially met the other way.
		resetDevice();
		ApptentiveLog.e("THREE");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));

		// Conditions almost met.
		resetDevice();
		ApptentiveLog.e("FOUR");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 430000);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));

		// Conditions met barely.
		resetDevice();
		ApptentiveLog.e("FIVE");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 432000);
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("local#app#init");
		assertNotNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));


		//// Test Rating Dialog.

		// Conditions are always met.
		resetDevice();
		ApptentiveLog.e("SIX");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
		assertNotNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes"));

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			ApptentiveLog.e("SEVEN");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#remind");
			assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));
		}

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			ApptentiveLog.e("EIGHT");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 420000);
			assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));
		}

		// Re-prompt is ready.
		{
			resetDevice();
			ApptentiveLog.e("NINE");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			assertNotNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));
		}

		// Don't re-prompt, since we've already rated.
		{
			resetDevice();
			ApptentiveLog.e("TEN");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#rate");
			assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));
		}

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			ApptentiveLog.e("ELEVEN");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getInstance().getCodePointStore().storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#decline");
			assertNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("local#app#init"));
		}

		// Test Message Center

		{
			resetDevice();
			ApptentiveLog.e("TWELVE");
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);
			assertNotNull(ApptentiveInternal.getInstance().getInteractionManager().getApplicableInteraction("com.apptentive#EnjoymentDialog#no"));
		}
	}

	public void testCanShowInteraction() {
		ApptentiveLog.e("Running test: testCanShowInteraction()\n\n");
		resetDevice();

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCanShowInteraction.json");
		ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString(json);

		boolean willShow = Apptentive.canShowInteraction("init");
		assertFalse(willShow);

		ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion("switch.code.point");
		willShow = Apptentive.canShowInteraction("init");
		assertTrue(willShow);
	}


}
