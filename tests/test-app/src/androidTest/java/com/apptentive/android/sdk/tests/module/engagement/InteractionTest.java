/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.content.Context;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
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
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "1.0", 1);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "1.1", 2);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "1.1", 3);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 4);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 5);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.0", 5);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, testInteraction, "2.1", 6);

		long value = 0;

		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getTotalInvokes(true, testInteraction);
		assertEquals(value, 14);

		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getVersionInvokes(true, testInteraction, "1.0");
		assertEquals(value, 1);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getVersionInvokes(true, testInteraction, "1.1");
		assertEquals(value, 4);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getVersionInvokes(true, testInteraction, "2.0");
		assertEquals(value, 6);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getVersionInvokes(true, testInteraction, "2.1");
		assertEquals(value, 3);

		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "1");
		assertEquals(value, 1);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "2");
		assertEquals(value, 1);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "3");
		assertEquals(value, 3);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "4");
		assertEquals(value, 4);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "5");
		assertEquals(value, 2);
		value = ApptentiveInternal.getCodePointStore(getTargetContext()).getBuildInvokes(true, testInteraction, "6");
		assertEquals(value, 3);

		Double lastInvoke = ApptentiveInternal.getCodePointStore(getTargetContext()).getLastInvoke(true, testInteraction);
		assertFalse(lastInvoke.equals(0d));
		Log.e("Finished test.");
	}

	public void testCriteriaTimeAtInstall() {
		Log.e("Running test: testCriteriaTimeAtInstall()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaTimeAtInstall.json");

		Interaction interaction;

		// 0
		resetDevice();

		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 1
		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 5);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 3);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 2
		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 3
		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 5);
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);


		Log.e("Finished test.");
	}

	public void testCriteriaApplicationVersion() {
		Log.e("Running test: testCriteriaApplicationVersion()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaApplicationVersion.json");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

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
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 7d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		Log.e(message);
		assertTrue(message, average < limit);
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
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 7d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		Log.e(message);
		assertTrue(message, average < limit);
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
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
			Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 50d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		Log.e(message);
		assertTrue(message, average < limit);
	}

	public void testSavingCodePointAndCheckingForApplicableInteraction() {
		Log.e("Running test: testSavingCodePointAndCheckingForApplicableInteraction()");
		resetDevice();
		final int iterations = 100;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "non.existant.code.point");
			Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "non.existant.code.point");
			assertNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 20d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		Log.e(message);
		assertTrue(message, average < limit);
	}

	public void testSelectionWithInteractionIdUsedInCriteria() {
		Log.e("Running test: testSelectionWithInteractionIdUsedInCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testSelectionWithInteractionIdUsedInCriteria.json");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNotNull(interaction);

	}

	public void testInteractionPriority() {
		Log.e("Running test: testInteractionPriority()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testInteractionPriority.json");

		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000a");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000b");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000c");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNull(interaction);
	}

	public void testMissingNullEmptyCriteria() {
		Log.e("Running test: testMissingNullEmptyCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testMissingNullEmptyCriteria.json");

		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNull(interaction);

		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "code.point.3");
		assertNotNull(interaction);
	}

	public void testCorruptedJson() {
		Log.e("Running test: testCorruptedJson()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCorruptedJson.json");

		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		Interactions interactions = ApptentiveInternal.getInteractionManager(getTargetContext()).getInteractions(getTargetContext());
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
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 600000);
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "event_label"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "event_label"));

		// Just right
		Log.e("THREE");
		resetDevice();
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNotNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "event_label"));

		// Already shown
		Log.e("FOUR");
		interaction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "event_label");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(getTargetContext(), "event_label"));
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
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 100000);
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "by.build"));

		// Conditions partially met.
		resetDevice();
		Log.e("TWO");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 500000);
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions partially met the other way.
		resetDevice();
		Log.e("THREE");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions almost met.
		resetDevice();
		Log.e("FOUR");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 430000);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));

		// Conditions met barely.
		resetDevice();
		Log.e("FIVE");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		VersionHistoryStore.updateVersionHistory(targetContext, 3, "1.0", Util.currentTimeSeconds() - 432000);
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "local#app#init");
		assertNotNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));


		//// Test Rating Dialog.

		// Conditions are always met.
		resetDevice();
		Log.e("SIX");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
		assertNotNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes"));

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			Log.e("SEVEN");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#remind");
			assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			Log.e("EIGHT");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 420000);
			assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Re-prompt is ready.
		{
			resetDevice();
			Log.e("NINE");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			assertNotNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Don't re-prompt, since we've already rated.
		{
			resetDevice();
			Log.e("TEN");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#rate");
			assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			Log.e("ELEVEN");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			Interaction ratingDialogInteraction = ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeInteractionForCurrentAppVersion(targetContext, ratingDialogInteraction.getId());
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(targetContext, false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(targetContext, "com.apptentive#RatingDialog#decline");
			assertNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "local#app#init"));
		}

		// Test Message Center

		{
			resetDevice();
			Log.e("TWELVE");
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(targetContext, json);
			assertNotNull(ApptentiveInternal.getInteractionManager(getTargetContext()).getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#no"));
		}
	}

	public void testCanShowInteraction() {
		Log.e("Running test: testCanShowInteraction()\n\n");
		resetDevice();

		ApptentiveInternal.getInstance(getTargetContext()).setMinimumLogLevel(Log.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCanShowInteraction.json");
		ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);

		boolean willShow = Apptentive.canShowInteraction(getTargetContext(), "init");
		assertFalse(willShow);

		ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		willShow = Apptentive.canShowInteraction(getTargetContext(), "init");
		assertTrue(willShow);
	}


}
