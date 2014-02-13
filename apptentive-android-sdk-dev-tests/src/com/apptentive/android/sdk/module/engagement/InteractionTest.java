/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.format.DateUtils;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.FileUtil;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.List;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 *
 * @author Sky Kelsey
 */
public class InteractionTest extends InstrumentationTestCase {

	private Context context;

	private Context getTargetContext() {
		if (context == null) {
			context = getInstrumentation().getTargetContext();
		}
		return context;
	}

	private void resetDevice() {
		getTargetContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getTargetContext());
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public void testInteractionSavingAndLoading() {
		Log.e("Running test: testCriteriaDaysSinceInstall()\n\n");
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
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaTimeSinceInstall.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");

		assertNotNull("Failed to parse Interactions.", interactionsList);

		boolean canRun;
		Interaction interaction = interactionsForCodePoint.get(0);

		resetDevice();
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		resetDevice();
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 2.8);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		resetDevice();
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);


		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "1.1", Util.currentTimeSeconds() - 4);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 5, "2.0", Util.currentTimeSeconds() - 2.8);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "1.1", Util.currentTimeSeconds() - 2.8);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 5, "2.0", Util.currentTimeSeconds() - 1);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);


		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 4);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "2.0", Util.currentTimeSeconds() - 4);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 2.8);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		resetDevice();
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch");
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "2.0", Util.currentTimeSeconds() - 2.8);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - 1);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		Log.e("Finished test.");
	}

	/**
	 * Tests to make sure application_version is interpreted as a string. Never runs.
	 */
	private static final String TEST_CRITERIA__APPLICATION_VERSION_1 =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"application_version\": \"2\"\n" +
					"                },\n" +
					"                \"type\": \"UpgradeMessage\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"app_version\": \"2\",\n" +
					"                    \"show_app_icon\": true,\n" +
					"                    \"show_powered_by\": true,\n" +
					"                    \"body\": \"\"\n" +
					"                }\n" +
					"            }\n" +
					"        ]\n" +
					"    }\n" +
					"}";

	public void testCriteriaApplicationVersion1() {
		Log.e("Running test: testCriteriaApplicationVersion1()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__APPLICATION_VERSION_1);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests app application_version. Never runs.
	 */
	private static final String TEST_CRITERIA__APPLICATION_VERSION_2 =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"application_version\": \"2.1\"\n" +
					"                },\n" +
					"                \"type\": \"UpgradeMessage\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"app_version\": \"2\",\n" +
					"                    \"show_app_icon\": true,\n" +
					"                    \"show_powered_by\": true,\n" +
					"                    \"body\": \"\"\n" +
					"                }\n" +
					"            }\n" +
					"        ]\n" +
					"    }\n" +
					"}";

	public void testCriteriaApplicationVersion2() {
		Log.e("Running test: testCriteriaApplicationVersion2()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__APPLICATION_VERSION_2);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	public void testCriteriaApplicationVersion3() {
		Log.e("Running test: testCriteriaApplicationVersion3()\n\n");
		resetDevice();
		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaApplicationBuild2.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		Log.e("Finished test.");
	}

	/**
	 * Tests application_build. Never Runs.
	 */
	private static final String TEST_CRITERIA__APPLICATION_BUILD_1 =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"application_build\": 3\n" +
					"                },\n" +
					"                \"type\": \"UpgradeMessage\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"app_version\": \"2\",\n" +
					"                    \"show_app_icon\": true,\n" +
					"                    \"show_powered_by\": true,\n" +
					"                    \"body\": \"\"\n" +
					"                }\n" +
					"            }\n" +
					"        ]\n" +
					"    }\n" +
					"}";

	public void testCriteriaApplicationBuild1() {
		Log.e("Running test: testCriteriaApplicationBuild1()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__APPLICATION_BUILD_1);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	public void testCriteriaApplicationBuild2() {
		Log.e("Running test: testCriteriaApplicationBuild2()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaApplicationBuild2.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		boolean canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesTotal() {
		Log.e("Running test: testCriteriaCodePointInvokesTotal()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaCodePointInvokesTotal.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0 - $gt
		boolean canRun = interaction.canRun(getTargetContext());
		Log.e("Test $gt");
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1 - $gte
		resetDevice();
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2 - $ne
		resetDevice();
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3 - $eq
		resetDevice();
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4 - :
		resetDevice();
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5 - $lte
		resetDevice();
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 6 - $lt
		resetDevice();
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesVersion() {
		Log.e("Running test: testCriteriaCodePointInvokesVersion()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaCodePointInvokesVersion.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0 - $gt
		boolean canRun = interaction.canRun(getTargetContext());
		Log.e("Test $gt");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1 - $gte
		resetDevice();
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2 - $ne
		resetDevice();
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3 - $eq
		resetDevice();
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4 - :
		resetDevice();
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5 - $lte
		resetDevice();
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 6 - $lt
		resetDevice();
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
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesBuild() {
		Log.e("Running test: testCriteriaCodePointInvokesBuild()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaCodePointInvokesBuild.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0 - $gt
		boolean canRun = interaction.canRun(getTargetContext());
		Log.e("Test $gt");
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1 - $gte
		resetDevice();
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2 - $ne
		resetDevice();
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3 - $eq
		resetDevice();
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4 - :
		resetDevice();
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5 - $lte
		resetDevice();
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 6 - $lt
		resetDevice();
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
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCriteriaCodePointInvokesTimeAgo() {
		Log.e("Running test: testCriteriaCodePointInvokesTimeAgo()\n\n");

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaCodePointInvokesTimeAgo.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0 - $gt
		resetDevice();
		boolean canRun = interaction.canRun(getTargetContext());
		Log.e("Test $gt");
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1 - $gte
		resetDevice();
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2 - $ne
		resetDevice();
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3 - $eq // There's no easy way to test this unless we contrive the times.
		resetDevice();
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4 - : // Ditto
		resetDevice();
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5 - $lte
		resetDevice();
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 6 - $lt
		resetDevice();
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		sleep(300);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

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

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCriteriaInteractionInvokesTotal.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactionsList = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
		assertNotNull("Failed to parse Interactions.", interactionsList);

		Interaction interaction = interactionsForCodePoint.get(0);

		// 0 - $gt
		boolean canRun = interaction.canRun(getTargetContext());
		Log.e("Test $gt");
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 1 - $gte
		resetDevice();
		Log.e("Test $gte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 2 - $ne
		resetDevice();
		Log.e("Test $ne");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		// 3 - $eq
		resetDevice();
		Log.e("Test $eq");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 4 - :
		resetDevice();
		Log.e("Test :");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 5 - $lte
		resetDevice();
		Log.e("Test $lte");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// 6 - $lt
		resetDevice();
		Log.e("Test $lt");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);
		CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
		canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		Log.e("Finished test.");
	}


	public void testVariousInteractionCritera() {
		Log.e("Running test: testVariousInteractionCriteria()\n\n");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testListOfVariousInteractions.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactions = InteractionManager.loadInteractions(getTargetContext());
		List<Interaction> interactionsForCodePoint = interactions.getInteractionList("complex_criteria");
		assertNotNull("Failed to parse interactions.", interactions);
		Interaction interaction = interactionsForCodePoint.get(0);

		// Conditions are not met yet.
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 0, "1.0", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 10)); // 10 days ago
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 1, "1.1", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 8));  //  8 days ago
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 2, "1.2", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 6));  //  6 days ago
		//CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		boolean canRun = interaction.canRun(getTargetContext());
		assertFalse(canRun);

		// Allow it to run.
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 4, "2.0", Util.currentTimeSeconds() - (DateUtils.DAY_IN_MILLIS / 1000 * 2));  //  2 days ago
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		canRun = interaction.canRun(getTargetContext());
		assertTrue(canRun);

		Log.e("Finished test.");
	}

	public void testCriteriaProcessingPerformance() {
		Log.e("Running test: testCriteriaProcessingPerformance()");
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testListOfVariousInteractions.json");
		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactions = InteractionManager.loadInteractions(getTargetContext());
		assertNotNull("Failed to parse interactions.", interactions);
		List<Interaction> interactionsForCodePoint = interactions.getInteractionList("complex_criteria");
		Interaction interaction = interactionsForCodePoint.get(0);

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			boolean canRun = interaction.getCriteria().isMet(getTargetContext());
			assertTrue(canRun);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 4d);
	}

	public void testInteractionSelectionPerformance() {
		Log.e("Running test: testInteractionSelectionPerformance()");
		resetDevice();
		final int iterations = 100;

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testListOfVariousInteractions.json");
		InteractionManager.storeInteractions(getTargetContext(), json);

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
		assertTrue(average < 5d);
	}

	public void testInteractionStorageAndSelectionPerformance() {
		Log.e("Running test: testInteractionStorageAndSelectionPerformance()");
		resetDevice();
		final int iterations = 20;

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testListOfVariousInteractions.json");

		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()));
			InteractionManager.storeInteractions(getTargetContext(), json);
			Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "complex_criteria");
			assertNotNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 50d);
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
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 20d);
	}

	public void testSelectionWithInteractionIdUsedInCriteria() {
		Log.e("Running test: testSelectionWithInteractionIdUsedInCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testSelectionWithInteractionIdUsedInCriteria.json");

		InteractionManager.storeInteractions(getTargetContext(), json);

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

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testInteractionPriority.json");

		InteractionManager.storeInteractions(getTargetContext(), json);

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

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testMissingNullEmptyCriteria.json");

		InteractionManager.storeInteractions(getTargetContext(), json);

		Interaction interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.1");
		assertNull(interaction);

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNull(interaction);

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.3");
		assertNotNull(interaction);
		assertTrue(interaction.canRun(getTargetContext()));
	}

	public void testBadCriteria() {
		Log.e("Running test: testBadCriteria()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testBadCriteria.json");


		Interaction interaction = null;
		try {
			Interactions interactions = new Interactions(json);

			interaction = interactions.getInteractionList("code.point.1").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.2").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.3").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.4").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.5").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.6").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

		} catch (Exception e) {
			assertNull("An exception was thrown from bad criteria.", e);
		}
	}

	public void testCorruptedJson() {
		Log.e("Running test: testCorruptedJson()");
		resetDevice();

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testCorruptedJson.json");

		InteractionManager.storeInteractions(getTargetContext(), json);
		Interactions interactions = InteractionManager.loadInteractions(getTargetContext());
		assertNull(interactions);
	}

	/**
	 * Update this when the UpgradeMessage payload and client changes.
	 */
	public void testActualUpgradeMessage() {
		Log.e("Running test: testActualUpgradeMessage()");

		String json = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), "testActualUpgradeMessage.json");
		Interaction interaction;

		// Test version targeted UpgradeMessage

		// Saw this build too long ago.
		Log.e("ONE");
		resetDevice();
		InteractionManager.storeInteractions(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 604800);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		InteractionManager.storeInteractions(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.build"));

		// Just right
		Log.e("THREE");
		resetDevice();
		InteractionManager.storeInteractions(getTargetContext(), json);
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
		InteractionManager.storeInteractions(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 604800);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Haven't upgraded
		Log.e("TWO");
		resetDevice();
		InteractionManager.storeInteractions(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Just right
		Log.e("THREE");
		resetDevice();
		InteractionManager.storeInteractions(getTargetContext(), json);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), 3, "1.0", Util.currentTimeSeconds() - 1000000);
		VersionHistoryStore.updateVersionHistory(getTargetContext(), Util.getAppVersionCode(getTargetContext()), Util.getAppVersionName(getTargetContext()), Util.currentTimeSeconds() - 500000);
		assertNotNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

		// Already shown
		Log.e("FOUR");
		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "by.version");
		CodePointStore.storeInteractionForCurrentAppVersion(getTargetContext(), interaction.getId());
		assertNull(InteractionManager.getApplicableInteraction(getTargetContext(), "by.version"));

	}
}
