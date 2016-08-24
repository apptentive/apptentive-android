/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import android.content.pm.PackageInfo;
import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.Util;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 */
@RunWith(AndroidJUnit4.class)
public class InteractionTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	@Test
	public void interactionInvocationStorage() {
		resetDevice();
		final String testInteraction = "test.interaction";
		codePointStore.storeRecord(true, testInteraction, "1.0", 1);
		codePointStore.storeRecord(true, testInteraction, "1.1", 2);
		codePointStore.storeRecord(true, testInteraction, "1.1", 3);
		codePointStore.storeRecord(true, testInteraction, "1.1", 3);
		codePointStore.storeRecord(true, testInteraction, "1.1", 3);
		codePointStore.storeRecord(true, testInteraction, "2.0", 4);
		codePointStore.storeRecord(true, testInteraction, "2.0", 4);
		codePointStore.storeRecord(true, testInteraction, "2.0", 4);
		codePointStore.storeRecord(true, testInteraction, "2.0", 4);
		codePointStore.storeRecord(true, testInteraction, "2.0", 5);
		codePointStore.storeRecord(true, testInteraction, "2.0", 5);
		codePointStore.storeRecord(true, testInteraction, "2.1", 6);
		codePointStore.storeRecord(true, testInteraction, "2.1", 6);
		codePointStore.storeRecord(true, testInteraction, "2.1", 6);

		long value = 0;

		value = codePointStore.getTotalInvokes(true, testInteraction);
		assertEquals(value, 14);

		value = codePointStore.getVersionInvokes(true, testInteraction, "1.0");
		assertEquals(value, 1);
		value = codePointStore.getVersionInvokes(true, testInteraction, "1.1");
		assertEquals(value, 4);
		value = codePointStore.getVersionInvokes(true, testInteraction, "2.0");
		assertEquals(value, 6);
		value = codePointStore.getVersionInvokes(true, testInteraction, "2.1");
		assertEquals(value, 3);

		value = codePointStore.getBuildInvokes(true, testInteraction, "1");
		assertEquals(value, 1);
		value = codePointStore.getBuildInvokes(true, testInteraction, "2");
		assertEquals(value, 1);
		value = codePointStore.getBuildInvokes(true, testInteraction, "3");
		assertEquals(value, 3);
		value = codePointStore.getBuildInvokes(true, testInteraction, "4");
		assertEquals(value, 4);
		value = codePointStore.getBuildInvokes(true, testInteraction, "5");
		assertEquals(value, 2);
		value = codePointStore.getBuildInvokes(true, testInteraction, "6");
		assertEquals(value, 3);

		Double lastInvoke = codePointStore.getLastInvoke(true, testInteraction);
		assertFalse(lastInvoke.equals(0d));
	}

	@Test
	public void criteriaTimeAtInstall() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testCriteriaTimeAtInstall.json");
		Interaction interaction;

		// 0
		resetDevice();

		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 1
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 5);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 3);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 3);
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 1);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 2
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(4, "2.0", Util.currentTimeSeconds() - 5);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		// 3
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		codePointStore.storeCodePointForCurrentAppVersion("switch");
		VersionHistoryStore.updateVersionHistory(3, "1.1", Util.currentTimeSeconds() - 5);
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNotNull(interaction);
	}

	@Test
	public void criteriaApplicationVersion() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testCriteriaApplicationVersion.json");
		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction;

		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);

		codePointStore.storeCodePointForCurrentAppVersion("switch");
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		codePointStore.storeCodePointForCurrentAppVersion("switch");
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNotNull(interaction);

		codePointStore.storeCodePointForCurrentAppVersion("switch");
		interaction = interactionManager.getApplicableInteraction("app.launch");
		assertNull(interaction);
	}

	@Test
	public void criteriaApplicationVersionCode() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionCode.json");

		try {
			PackageInfo packageInfo = targetContext.getPackageManager().getPackageInfo(targetContext.getPackageName(), 0);
			json = json.replace("\"APPLICATION_VERSION_CODE\"", String.valueOf(packageInfo.versionCode));

			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (Exception e) {
			ApptentiveLog.e("Error running test.", e);
			assertNull(e);
		}
	}

	@Test
	public void criteriaApplicationVersionName() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionName.json");

		try {
			PackageInfo packageInfo = targetContext.getPackageManager().getPackageInfo(targetContext.getPackageName(), 0);
			json = json.replace("APPLICATION_VERSION_NAME", packageInfo.versionName);

			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (Exception e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}

	@Test
	public void criteriaProcessingPerformance() {
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		interactionManager.storeInteractionsPayloadString(json);

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext));
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = interactionManager.getApplicableInteraction("complex_criteria");
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

	@Test
	public void interactionSelectionPerformance() {
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 100;

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		interactionManager.storeInteractionsPayloadString(json);

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext));
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = interactionManager.getApplicableInteraction("complex_criteria");
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

	@Test
	public void interactionStorageAndSelectionPerformance() {
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		resetDevice();
		final int iterations = 20;

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");

		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext));
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("app.launch");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");
		codePointStore.storeCodePointForCurrentAppVersion("big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext));
			interactionManager.storeInteractionsPayloadString(json);
			Interaction interaction = interactionManager.getApplicableInteraction("complex_criteria");
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

	@Test
	public void savingCodePointAndCheckingForApplicableInteraction() {
		resetDevice();
		final int iterations = 100;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			codePointStore.storeCodePointForCurrentAppVersion("non.existant.code.point");
			Interaction interaction = interactionManager.getApplicableInteraction("non.existant.code.point");
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

	@Test
	public void selectionWithInteractionIdUsedInCriteria() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testSelectionWithInteractionIdUsedInCriteria.json");
		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction = interactionManager.getApplicableInteraction("code.point.2");
		assertNull(interaction);

		interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		codePointStore.storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = interactionManager.getApplicableInteraction("code.point.2");
		assertNotNull(interaction);
	}

	@Test
	public void interactionPriority() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testInteractionPriority.json");

		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000a");
		codePointStore.storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000b");
		codePointStore.storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNotNull(interaction);
		assertEquals(interaction.getId(), "526fe2836dd8bf546a00000c");
		codePointStore.storeInteractionForCurrentAppVersion(interaction.getId());

		interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNull(interaction);
	}

	@Test
	public void missingNullEmptyCriteria() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testMissingNullEmptyCriteria.json");

		interactionManager.storeInteractionsPayloadString(json);

		Interaction interaction = interactionManager.getApplicableInteraction("code.point.1");
		assertNull(interaction);

		interaction = interactionManager.getApplicableInteraction("code.point.2");
		assertNull(interaction);

		interaction = interactionManager.getApplicableInteraction("code.point.3");
		assertNotNull(interaction);
	}

	@Test
	public void corruptedJson() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testCorruptedJson.json");

		interactionManager.storeInteractionsPayloadString(json);
		Interactions interactions = interactionManager.getInteractions();
		assertNull(interactions);
	}

	@Test
	public void actualUpgradeMessage() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testActualUpgradeMessage.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		ApptentiveLog.e("ONE");
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext), Util.currentTimeSeconds() - 600000);
		assertNull(interactionManager.getApplicableInteraction("event_label"));

		// Haven't upgraded
		ApptentiveLog.e("TWO");
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext), Util.currentTimeSeconds() - 499500);
		assertNull(interactionManager.getApplicableInteraction("event_label"));

		// Just right
		ApptentiveLog.e("THREE");
		resetDevice();
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(Util.getAppVersionCode(targetContext), Util.getAppVersionName(targetContext), Util.currentTimeSeconds() - 499500);
		assertNotNull(interactionManager.getApplicableInteraction("event_label"));

		// Already shown
		ApptentiveLog.e("FOUR");
		interaction = interactionManager.getApplicableInteraction("event_label");
		codePointStore.storeInteractionForCurrentAppVersion(interaction.getId());
		assertNull(interactionManager.getApplicableInteraction("event_label"));
	}

	/**
	 * Update this when the Rating Flow group of interactions changes, or with different permutations of that flow.
	 */
	@Test
	public void testRealRatingInteractions() {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testRealRatingInteractions.json");

		// Test version targeted UpgradeMessage

		// Conditions not yet met.
		resetDevice();
		ApptentiveLog.e("ONE");
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 100000);
		assertNull(interactionManager.getApplicableInteraction("by.build"));

		// Conditions partially met.
		resetDevice();
		ApptentiveLog.e("TWO");
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 500000);
		assertNull(interactionManager.getApplicableInteraction("local#app#init"));

		// Conditions partially met the other way.
		resetDevice();
		ApptentiveLog.e("THREE");
		interactionManager.storeInteractionsPayloadString(json);
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		assertNull(interactionManager.getApplicableInteraction("local#app#init"));

		// Conditions almost met.
		resetDevice();
		ApptentiveLog.e("FOUR");
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 430000);
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		assertNull(interactionManager.getApplicableInteraction("local#app#init"));

		// Conditions met barely.
		resetDevice();
		ApptentiveLog.e("FIVE");
		interactionManager.storeInteractionsPayloadString(json);
		VersionHistoryStore.updateVersionHistory(3, "1.0", Util.currentTimeSeconds() - 432000);
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		codePointStore.storeCodePointForCurrentAppVersion("local#app#init");
		assertNotNull(interactionManager.getApplicableInteraction("local#app#init"));


		//// Test Rating Dialog.

		// Conditions are always met.
		resetDevice();
		ApptentiveLog.e("SIX");
		interactionManager.storeInteractionsPayloadString(json);
		assertNotNull(interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes"));

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			ApptentiveLog.e("SEVEN");
			interactionManager.storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			codePointStore.storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			codePointStore.storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#remind");
			assertNull(interactionManager.getApplicableInteraction("local#app#init"));
		}

		// Re-prompt isn't ready yet.
		{
			resetDevice();
			ApptentiveLog.e("EIGHT");
			interactionManager.storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			codePointStore.storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			codePointStore.storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 420000);
			assertNull(interactionManager.getApplicableInteraction("local#app#init"));
		}

		// Re-prompt is ready.
		{
			resetDevice();
			ApptentiveLog.e("NINE");
			interactionManager.storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			codePointStore.storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			codePointStore.storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			assertNotNull(interactionManager.getApplicableInteraction("local#app#init"));
		}

		// Don't re-prompt, since we've already rated.
		{
			resetDevice();
			ApptentiveLog.e("TEN");
			interactionManager.storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			codePointStore.storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			codePointStore.storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			codePointStore.storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#rate");
			assertNull(interactionManager.getApplicableInteraction("local#app#init"));
		}

		// Don't re-prompt, since we've declined to rate.
		{
			resetDevice();
			ApptentiveLog.e("ELEVEN");
			interactionManager.storeInteractionsPayloadString(json);
			Interaction ratingDialogInteraction = interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#yes");
			assertNotNull(ratingDialogInteraction);
			codePointStore.storeInteractionForCurrentAppVersion(ratingDialogInteraction.getId());
			codePointStore.storeRecord(false, "com.apptentive#RatingDialog#remind", Util.getAppVersionName(targetContext), Util.getAppVersionCode(targetContext), Util.currentTimeSeconds() - 432000);
			codePointStore.storeCodePointForCurrentAppVersion("com.apptentive#RatingDialog#decline");
			assertNull(interactionManager.getApplicableInteraction("local#app#init"));
		}

		// Test Message Center

		{
			resetDevice();
			ApptentiveLog.e("TWELVE");
			interactionManager.storeInteractionsPayloadString(json);
			assertNotNull(interactionManager.getApplicableInteraction("com.apptentive#EnjoymentDialog#no"));
		}
	}

	@Test
	public void canShowInteraction() {
		resetDevice();

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testCanShowInteraction.json");
		interactionManager.storeInteractionsPayloadString(json);

		boolean willShow = Apptentive.canShowInteraction("init");
		assertFalse(willShow);

		codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
		willShow = Apptentive.canShowInteraction("init");
		assertTrue(willShow);
	}
}
