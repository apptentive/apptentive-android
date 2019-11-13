/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 */
@RunWith(AndroidJUnit4.class)
public class InteractionTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator;

	@Test
	public void interactionInvocationStorage() {

		EventData eventData = new EventData();

		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 1, "1.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 2, "1.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 3, "1.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 4, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 4, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 4, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 4, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 5, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 5, "2.0", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 6, "2.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 6, "2.1", "test.interaction");
		eventData.storeInteractionForCurrentAppVersion(Util.currentTimeSeconds(), 6, "2.1", "test.interaction");

		long value = 0;

		value = eventData.getInteractionCountTotal("test.interaction");
		assertEquals(value, 14);

		value = eventData.getInteractionCountForVersionName("test.interaction", "1.0");
		assertEquals(value, 1);
		value = eventData.getInteractionCountForVersionName("test.interaction", "1.1");
		assertEquals(value, 4);
		value = eventData.getInteractionCountForVersionName("test.interaction", "2.0");
		assertEquals(value, 6);
		value = eventData.getInteractionCountForVersionName("test.interaction", "2.1");
		assertEquals(value, 3);

		value = eventData.getInteractionCountForVersionCode("test.interaction", 1);
		assertEquals(value, 1);
		value = eventData.getInteractionCountForVersionCode("test.interaction", 2);
		assertEquals(value, 1);
		value = eventData.getInteractionCountForVersionCode("test.interaction", 3);
		assertEquals(value, 3);
		value = eventData.getInteractionCountForVersionCode("test.interaction", 4);
		assertEquals(value, 4);
		value = eventData.getInteractionCountForVersionCode("test.interaction", 5);
		assertEquals(value, 2);
		value = eventData.getInteractionCountForVersionCode("test.interaction", 6);
		assertEquals(value, 3);

		Double lastInvoked = eventData.getTimeOfLastInteractionInvocation("test.interaction");
		assertFalse(lastInvoked.equals(0d));
	}

	@Test
	public void criteriaTimeAtInstall() throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testCriteriaTimeAtInstall.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		EventData eventData = new EventData();
		VersionHistory versionHistory = new VersionHistory();
		FieldManager fieldManager = new FieldManager(targetContext, versionHistory, eventData, new Person(), new Device(), new AppRelease());

		// 0
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 5, 4, "2.0");
		assertFalse(criteria.isMet(fieldManager));

		eventData.clear();
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 3, 4, "2.0");
		assertTrue(criteria.isMet(fieldManager));

		eventData.clear();
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1, 4, "2.0");
		assertFalse(criteria.isMet(fieldManager));

		// 1
		eventData.clear();
		versionHistory.clear();
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 5, 4, "2.0");
		assertFalse(criteria.isMet(fieldManager));

		eventData.clear();
		versionHistory.clear();
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 5, 3, "1.1");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 3, 4, "2.0");
		assertTrue(criteria.isMet(fieldManager));

		eventData.clear();
		versionHistory.clear();
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 3, 3, "1.1");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1, 4, "2.0");
		assertFalse(criteria.isMet(fieldManager));

		// 2
		eventData.clear();
		versionHistory.clear();
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 5, 4, "2.0");
		assertFalse(criteria.isMet(fieldManager));

		// 3
		eventData.clear();
		versionHistory.clear();
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "switch");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 5, 3, "1.1");
		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void criteriaApplicationVersionCode() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionCode.json");
		json = json.replace("\"APPLICATION_VERSION_CODE\"", String.valueOf(appRelease.getVersionCode()));
		InteractionCriteria criteria = new InteractionCriteria(json);
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), new EventData(), new Person(), new Device(), appRelease);

		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void criteriaApplicationVersionName() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DATA_DIR + "criteria/testCriteriaApplicationVersionName.json");
		json = json.replace("APPLICATION_VERSION_NAME", appRelease.getVersionName());
		InteractionCriteria criteria = new InteractionCriteria(json);
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), new EventData(), new Person(), new Device(), appRelease);
		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void criteriaApplicationDebug() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DATA_DIR + "criteria/testCriteriaApplicationDebug.json");
		json = json.replace("APPLICATION_DEBUG", Boolean.toString(appRelease.isDebug()));
		InteractionCriteria criteria = new InteractionCriteria(json);
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), new EventData(), new Person(), new Device(), appRelease);

		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void criteriaProcessingPerformance() throws JSONException {
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		final int iterations = 100;

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		VersionHistory versionHistory = new VersionHistory();
		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, versionHistory, eventData, new Person(), new Device(), new AppRelease());
		versionHistory.updateVersionHistory(Util.currentTimeSeconds(), versionCode, versionName);
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "app.launch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "app.launch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "big.win");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			assertTrue(criteria.isMet(fieldManager));
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
	public void savingCodePointAndCheckingForApplicableInteraction() throws JSONException {
		if (isRunningOnEmulator()) {
			ApptentiveLog.e("Running on emulator. Skipping test.");
			return;
		}
		final int iterations = 100;

		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/testListOfVariousInteractions.json");
		InteractionCriteria criteria = new InteractionCriteria(json);

		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), eventData, new Person(), new Device(), new AppRelease());
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "app.launch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "app.launch");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "big.win");
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			assertTrue(criteria.isMet(fieldManager));
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		double limit = 20d;
		String message = String.format("Finished %d iterations in %,dms, average of %.2fms per run, limit was %.2fms", iterations, duration, average, limit);
		ApptentiveLog.e(message);
		assertTrue(message, average < limit);
	}

/*
	@Test // TODO: Modify Sdk to allow easier unit testing of interaction selection.
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
*/


	@Test
	public void upgradeMessageOnVersionCode() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/upgradeMessageOnVersionCode.json")
				.replace("\"APPLICATION_VERSION_CODE\"", String.valueOf(appRelease.getVersionCode()));
		InteractionCriteria criteria = new InteractionCriteria(json);

		VersionHistory versionHistory = new VersionHistory();
		FieldManager fieldManager = new FieldManager(targetContext, versionHistory, new EventData(), new Person(), new Device(), appRelease);
		versionHistory.updateVersionHistory(Util.currentTimeSeconds(), versionCode, versionName);

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		ApptentiveLog.e("ONE");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1000000, 1, "1.0");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 600000, versionCode, "2.0.0");
		ApptentiveLog.e("Current time: %f", Util.currentTimeSeconds());
		assertFalse(criteria.isMet(fieldManager));

		// Haven't upgraded
		ApptentiveLog.e("TWO");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 499500, versionCode, "2.0.0");
		assertFalse(criteria.isMet(fieldManager));

		// Just right
		ApptentiveLog.e("THREE");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1000000, 1, "1.0");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 499500, versionCode, "2.0.0");
		assertTrue(criteria.isMet(fieldManager));
	}

	@Test
	public void upgradeMessageOnVersionName() throws JSONException {
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(targetContext, null);
		String json = loadTextAssetAsString(TEST_DATA_DIR + "payloads/upgradeMessageOnVersionName.json")
				.replace("APPLICATION_VERSION_NAME", appRelease.getVersionName());
		InteractionCriteria criteria = new InteractionCriteria(json);

		VersionHistory versionHistory = new VersionHistory();
		EventData eventData = new EventData();
		FieldManager fieldManager = new FieldManager(targetContext, versionHistory, eventData, new Person(), new Device(), appRelease);
		versionHistory.updateVersionHistory(Util.currentTimeSeconds(), versionCode, versionName);
		eventData.storeEventForCurrentAppVersion(Util.currentTimeSeconds(), versionCode, versionName, "app.launch");

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		ApptentiveLog.e("ONE");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1000000, 3, "1.0");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 600000, 4, "2.0.0");
		assertFalse(criteria.isMet(fieldManager));

		// Haven't upgraded
		ApptentiveLog.e("TWO");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 499500, 4, "2.0.0");
		assertFalse(criteria.isMet(fieldManager));

		// Just right
		ApptentiveLog.e("THREE");
		versionHistory.clear();
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 1000000, 3, "1.0");
		versionHistory.updateVersionHistory(Util.currentTimeSeconds() - 499500, 4, "2.0.0");
		assertTrue(criteria.isMet(fieldManager));
	}

	/**
	 * Update this when the Rating Flow group of interactions changes, or with different permutations of that flow.
	 */
/* TODO: Re-enable interaction flow unit testing.
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
*/
}
