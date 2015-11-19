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

	public void testCriteriaTimeAtInstall() {
		Log.e("Running test: testCriteriaTimeAtInstall()\n\n");

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaTimeAtInstall.json");

		Interaction interaction;

		// 0
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 1
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 5);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 3);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 2
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		// 3
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.1", Util.currentTimeSeconds() - 5);
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);


		Log.e("Finished test.");
	}

	public void testCriteriaApplicationVersion() {
		Log.e("Running test: testCriteriaApplicationVersion()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCriteriaApplicationVersion.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		Interaction interaction;

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
		assertNotNull(interaction);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "app.launch");
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
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "non.existant.code.point");
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "non.existant.code.point");
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
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 600000);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "event_label"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "event_label"));

		// Just right
		Log.e("THREE");
		resetDevice();
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 499500);
		assertNotNull(InteractionManager.getApplicableInteraction(getTargetContext(), "event_label"));

		// Already shown
		Log.e("FOUR");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "event_label");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "event_label"));
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

		// Test Message Center

		{
			resetDevice();
			Log.e("TWELVE");
			InteractionManager.storeInteractionsPayloadString(targetContext, json);
			assertNotNull(InteractionManager.getApplicableInteraction(targetContext, "com.apptentive#EnjoymentDialog#no"));
		}
	}

	public void testCanShowInteraction() {
		Log.e("Running test: testCanShowInteraction()\n\n");
		resetDevice();

		ApptentiveInternal.setMinimumLogLevel(Log.Level.VERBOSE);
		String json = FileUtil.loadTextAssetAsString(getTestContext(), TEST_DATA_DIR + "payloads/testCanShowInteraction.json");
		InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);

		boolean willShow = Apptentive.canShowInteraction(getTargetContext(), "init");
		assertFalse(willShow);

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		willShow = Apptentive.canShowInteraction(getTargetContext(), "init");
		assertTrue(willShow);
	}


}
