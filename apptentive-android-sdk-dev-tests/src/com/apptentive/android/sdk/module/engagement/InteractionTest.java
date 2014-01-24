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

	/**
	 * Tests days_since_install,
	 */
	private static final String TEST_CRITERIA__DAYS_SINCE_INSTALL =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"days_since_install\": 5\n" +
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

	public void testCriteriaDaysSinceInstall() {
		Log.e("Running test: testCriteriaDaysSinceInstall()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__DAYS_SINCE_INSTALL);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests days_since_upgrade.
	 */
	private static final String TEST_CRITERIA__DAYS_SINCE_UPGRADE =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"days_since_upgrade\": 5\n" +
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

	public void testCriteriaDaysSinceUpgrade() {
		Log.e("Running test: testCriteriaDaysSinceUpgrade()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__DAYS_SINCE_UPGRADE);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 7));
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 1l, "1.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
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

	/**
	 * Tests app application_version. Runs.
	 */
	private static final String TEST_CRITERIA__APPLICATION_VERSION_3 =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"application_version\": \"4.0\"\n" +
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

	public void testCriteriaApplicationVersion3() {
		Log.e("Running test: testCriteriaApplicationVersion3()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__APPLICATION_VERSION_3);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
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

	/**
	 * Tests application_build. Runs.
	 */
	private static final String TEST_CRITERIA__APPLICATION_BUILD_2 =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"application_build\": 4\n" +
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

	public void testCriteriaApplicationBuild2() {
		Log.e("Running test: testCriteriaApplicationBuild2()\n\n");
		resetDevice();
		try {
			Interactions interactionsList = new Interactions(TEST_CRITERIA__APPLICATION_BUILD_2);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			boolean canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	private static final String TEST_CRITERIA__CODE_POINT_INVOKES_TOTAL =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 0,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$gt\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 1,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$gte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 2,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$ne\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 3,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$eq\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 4,\n" +
					"                            \"code_point/test.code.point/invokes/total\": 2\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 5,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$lte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 6,\n" +
					"                            \"code_point/test.code.point/invokes/total\": {\n" +
					"                                \"$lt\": 2\n" +
					"                            }\n" +
					"                        }\n" +
					"                    ]\n" +
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

	public void testCriteriaCodePointInvokesTotal() {
		Log.e("Running test: testCriteriaCodePointInvokesTotal()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__CODE_POINT_INVOKES_TOTAL);
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

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	private static final String TEST_CRITERIA__CODE_POINT_INVOKES_VERSION =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 0,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$gt\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 1,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$gte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 2,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$ne\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 3,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$eq\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 4,\n" +
					"                            \"code_point/test.code.point/invokes/version\": 2\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 5,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$lte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 6,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$lt\": 2\n" +
					"                            }\n" +
					"                        }\n" +
					"                    ]\n" +
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

	public void testCriteriaCodePointInvokesVersion() {
		Log.e("Running test: testCriteriaCodePointInvokesVersion()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__CODE_POINT_INVOKES_VERSION);
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

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	private static final String TEST_CRITERIA__CODE_POINT_INVOKES_BUILD =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 0,\n" +
					"                            \"code_point/test.code.point/invokes/build\": {\n" +
					"                                \"$gt\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 1,\n" +
					"                            \"code_point/test.code.point/invokes/build\": {\n" +
					"                                \"$gte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 2,\n" +
					"                            \"code_point/test.code.point/invokes/build\": {\n" +
					"                                \"$ne\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 3,\n" +
					"                            \"code_point/test.code.point/invokes/build\": {\n" +
					"                                \"$eq\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 4,\n" +
					"                            \"code_point/test.code.point/invokes/build\": 2\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 5,\n" +
					"                            \"code_point/test.code.point/invokes/build\": {\n" +
					"                                \"$lte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 6,\n" +
					"                            \"code_point/test.code.point/invokes/version\": {\n" +
					"                                \"$lt\": 2\n" +
					"                            }\n" +
					"                        }\n" +
					"                    ]\n" +
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

	public void testCriteriaCodePointInvokesBuild() {
		Log.e("Running test: testCriteriaCodePointInvokesBuild()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__CODE_POINT_INVOKES_BUILD);
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

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	private static final String TEST_CRITERIA__CODE_POINT_INVOKES_TIME_AGO =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 0,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$gt\": 0.600\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 1,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$gte\": 0.600\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 2,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$ne\": 0.600\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 3,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$eq\": 0.600\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 4,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": 1.0\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 5,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$lte\": 0.600\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 6,\n" +
					"                            \"code_point/test.code.point/invokes/time_ago\": {\n" +
					"                                \"$lt\": 0.600\n" +
					"                            }\n" +
					"                        }\n" +
					"                    ]\n" +
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

	public void testCriteriaCodePointInvokesTimeAgo() {
		Log.e("Running test: testCriteriaCodePointInvokesTimeAgo()\n\n");
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__CODE_POINT_INVOKES_TIME_AGO);
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

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	private static final String TEST_CRITERIA__INTERACTION_INVOKES_TOTAL =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 0,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$gt\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 1,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$gte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 2,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$ne\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 3,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$eq\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 4,\n" +
					"                            \"interactions/test.interaction/invokes/total\": 2\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 5,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$lte\": 2\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/switch.code.point/invokes/total\": 6,\n" +
					"                            \"interactions/test.interaction/invokes/total\": {\n" +
					"                                \"$lt\": 2\n" +
					"                            }\n" +
					"                        }\n" +
					"                    ]\n" +
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

	public void testCriteriaInteractionInvokesTotal() {
		Log.e("Running test: testCriteriaInteractionInvokesTotal()\n\n");
		resetDevice();
		String appVersionName = Util.getAppVersionName(getTargetContext());
		int appVersionCode = Util.getAppVersionCode(getTargetContext());

		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__INTERACTION_INVOKES_TOTAL);
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

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}


	private static final String LIST_OF_VARIOUS_INTERACTIONS =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"526fe2836dd8bf546a00000c\",\n" +
					"                \"priority\": 2,\n" +
					"                \"criteria\": {\n" +
					"                    \"days_since_upgrade\": {\n" +
					"                        \"$lt\": 3\n" +
					"                    },\n" +
					"                    \"code_point/app.launch/invokes/total\": 2,\n" +
					"                    \"interactions/526fe2836dd8bf546a00000b/invokes/version\": 0\n" +
					"                },\n" +
					"                \"type\": \"RatingDialog\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"question_text\": \"Do you love Jelly Bean GO SMS Pro?\"\n" +
					"                }\n" +
					"            }\n" +
					"        ],\n" +
					"        \"big.win\": [\n" +
					"            {\n" +
					"                \"id\": \"526fe2836dd8bf546a00000d\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {},\n" +
					"                \"type\": \"RatingDialog\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"question_text\": \"Do you love Jelly Bean GO SMS Pro?\"\n" +
					"                }\n" +
					"            }\n" +
					"        ],\n" +
					"        \"or_clause\": [\n" +
					"            {\n" +
					"                \"id\": \"526fe2836dd8bf546a00000e\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"days_since_upgrade\": {\n" +
					"                                \"$lt\": 3\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"code_point/app.launch/invokes/total\": 2\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"interactions/526fe2836dd8bf546a00000b/invokes/version\": 0\n" +
					"                        }\n" +
					"                    ]\n" +
					"                },\n" +
					"                \"type\": \"RatingDialog\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"question_text\": \"Do you love Jelly Bean GO SMS Pro?\"\n" +
					"                }\n" +
					"            }\n" +
					"        ],\n" +
					"        \"complex_criteria\": [\n" +
					"            {\n" +
					"                \"id\": \"526fe2836dd8bf546a00000f\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"$or\": [\n" +
					"                        {\n" +
					"                            \"days_since_upgrade\": {\n" +
					"                                \"$lt\": 3\n" +
					"                            }\n" +
					"                        },\n" +
					"                        {\n" +
					"                            \"$and\": [\n" +
					"                                {\n" +
					"                                    \"code_point/app.launch/invokes/total\": 2\n" +
					"                                },\n" +
					"                                {\n" +
					"                                    \"interactions/526fe2836dd8bf546a00000b/invokes/version\": 0\n" +
					"                                },\n" +
					"                                {\n" +
					"                                    \"$or\": [\n" +
					"                                        {\n" +
					"                                            \"code_point/small.win/invokes/total\": 2\n" +
					"                                        },\n" +
					"                                        {\n" +
					"                                            \"code_point/big.win/invokes/total\": 2\n" +
					"                                        }\n" +
					"                                    ]\n" +
					"                                }\n" +
					"                            ]\n" +
					"                        }\n" +
					"                    ]\n" +
					"                },\n" +
					"                \"type\": \"RatingDialog\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"question_text\": \"Do you love Jelly Bean GO SMS Pro?\"\n" +
					"                }\n" +
					"            }\n" +
					"        ]\n" +
					"    }\n" +
					"}";

	public void testVariousInteractionCritera() {
		Log.e("Running test: testVariousInteractionCriteria()\n\n");
		resetDevice();
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(LIST_OF_VARIOUS_INTERACTIONS);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("complex_criteria");
			assertNotNull("Failed to parse interactions.", interactionsList);
			Interaction interaction = interactionsForCodePoint.get(0);

			// TODO: Use the actual Apptentive activity lifecycle to simulate this better.

			boolean canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);

			// Allow conditions to be met.
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 10)); // 10 days ago
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 1l, "1.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 8));  //  8 days ago
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 2l, "1.2", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));  //  6 days ago
			VersionHistoryStore.updateVersionHistory(getTargetContext(), 3l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));  //  4 days ago
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
			canRun = interaction.canRun(getTargetContext());
			assertFalse(canRun);

			VersionHistoryStore.updateVersionHistory(getTargetContext(), 4l, "2.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 2));  //  2 days ago
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
			canRun = interaction.canRun(getTargetContext());
			assertTrue(canRun);

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		Log.e("Finished test.");
	}

	public void testCriteriaProcessingPerformance() {
		Log.e("Running test: testCriteriaProcessingPerformance()");
		resetDevice();
		final int iterations = 100;

		try {
			Interactions interactions = new Interactions(LIST_OF_VARIOUS_INTERACTIONS);
			List<Interaction> interactionsForCodePoint = interactions.getInteractionList("complex_criteria");
			assertNotNull("Failed to parse interactions.", interactions);
			Interaction interaction = interactionsForCodePoint.get(0);

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
			assertTrue(average < 2d);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void testInteractionSelectionPerformance() {
		Log.e("Running test: testInteractionSelectionPerformance()");
		resetDevice();
		final int iterations = 100;

		InteractionManager.storeInteractions(getTargetContext(), LIST_OF_VARIOUS_INTERACTIONS);
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

		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			InteractionManager.storeInteractions(getTargetContext(), LIST_OF_VARIOUS_INTERACTIONS);
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
		assertNotNull(interaction);
		assertTrue(interaction.canRun(getTargetContext()));

		interaction = InteractionManager.getApplicableInteraction(getTargetContext(), "code.point.2");
		assertNotNull(interaction);
		assertTrue(interaction.canRun(getTargetContext()));

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

			interaction = interactions.getInteractionList("code.point.7").get(0);
			assertNotNull(interaction);
			assertTrue(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.8").get(0);
			assertNotNull(interaction);
			assertFalse(interaction.canRun(getTargetContext()));

			interaction = interactions.getInteractionList("code.point.9").get(0);
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

	private static final String UPGRADE_MESSAGE_REAL_INTERACTION = "{\"interactions\":{\"app.launch\":[{\"id\":\"528d14854712c7bfd7000002\",\"priority\":1,\"criteria\":{\"code_point/app.launch/invokes/version\":{\"$gte\":0},\"application_version\":\"4.0\"},\"type\":\"UpgradeMessage\",\"version\":null,\"active\":true,\"configuration\":{\"active\":true,\"app_version\":\"2.0\",\"show_app_icon\":true,\"show_powered_by\":true,\"body\":\"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Testing upgrade messaging.</p></body></html>\"}}]}}";
	private static final String UPGRADE_MESSAGE_REAL_INTERACTION_EXPANDED =
			"{\n" +
					"    \"interactions\": {\n" +
					"        \"app.launch\": [\n" +
					"            {\n" +
					"                \"id\": \"528d14854712c7bfd7000002\",\n" +
					"                \"priority\": 1,\n" +
					"                \"criteria\": {\n" +
					"                    \"code_point/app.launch/invokes/version\": {\"$gte\":0},\n" +
					"                    \"application_version\": \"4.0\"\n" +
					"                },\n" +
					"                \"type\": \"UpgradeMessage\",\n" +
					"                \"version\": null,\n" +
					"                \"active\": true,\n" +
					"                \"configuration\": {\n" +
					"                    \"active\": true,\n" +
					"                    \"app_version\": \"2.0\",\n" +
					"                    \"show_app_icon\": true,\n" +
					"                    \"show_powered_by\": true,\n" +
					"                    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Testing upgrade messaging.</p></body></html>\"\n" +
					"                }\n" +
					"            }\n" +
					"        ]\n" +
					"    }\n" +
					"}";
}
