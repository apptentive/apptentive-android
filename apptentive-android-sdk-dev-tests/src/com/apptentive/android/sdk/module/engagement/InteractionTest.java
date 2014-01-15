package com.apptentive.android.sdk.module.engagement;

import android.content.Context;
import android.test.AndroidTestCase;
import android.text.format.DateUtils;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.model.Interactions;
import com.apptentive.android.sdk.storage.InteractionManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.List;

/**
 * Note: Right now, these tests need versionName and versionCode in the manifest to be "2.0" and 4", respectively.
 *
 * @author Sky Kelsey
 */
public class InteractionTest extends AndroidTestCase {


	private void resetDevice() {
		getContext().getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(getContext());
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
		CodePointStore.storeRecord(getContext(), true, testInteraction, "1.0", 1);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "1.1", 2);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "1.1", 3);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 4);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 5);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.0", 5);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.1", 6);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.1", 6);
		CodePointStore.storeRecord(getContext(), true, testInteraction, "2.1", 6);

		long value = 0;

		value = CodePointStore.getTotalInvokes(getContext(), true, testInteraction);
		assertEquals(value, 14);

		value = CodePointStore.getVersionInvokes(getContext(), true, testInteraction, "1.0");
		assertEquals(value, 1);
		value = CodePointStore.getVersionInvokes(getContext(), true, testInteraction, "1.1");
		assertEquals(value, 4);
		value = CodePointStore.getVersionInvokes(getContext(), true, testInteraction, "2.0");
		assertEquals(value, 6);
		value = CodePointStore.getVersionInvokes(getContext(), true, testInteraction, "2.1");
		assertEquals(value, 3);

		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "1");
		assertEquals(value, 1);
		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "2");
		assertEquals(value, 1);
		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "3");
		assertEquals(value, 3);
		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "4");
		assertEquals(value, 4);
		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "5");
		assertEquals(value, 2);
		value = CodePointStore.getBuildInvokes(getContext(), true, testInteraction, "6");
		assertEquals(value, 3);

		Double lastInvoke = CodePointStore.getLastInvoke(getContext(), true, testInteraction);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			resetDevice();
			VersionHistoryStore.updateVersionHistory(getContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 7));
			VersionHistoryStore.updateVersionHistory(getContext(), 1l, "1.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 5));
			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
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
					"                    \"application_version\": \"2.0\"\n" +
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
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
			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			Log.e("Test $gt");
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 4 - :
			resetDevice();
			Log.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

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
			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			Log.e("Test $gt");
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 4 - :
			resetDevice();
			Log.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

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
			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			Log.e("Test $gt");
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 4 - :
			resetDevice();
			Log.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

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
			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			Log.e("Test $gt");
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 3 - $eq // There's no easy way to test this unless we contrive the times.
			resetDevice();
			Log.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 4 - : // Ditto
			resetDevice();
			Log.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "test.code.point");
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			sleep(300);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

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
		String appVersionName = Util.getAppVersionName(getContext());
		int appVersionCode = Util.getAppVersionCode(getContext());

		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(TEST_CRITERIA__INTERACTION_INVOKES_TOTAL);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractionList("app.launch");
			assertNotNull("Failed to parse Interactions.", interactionsList);

			Interaction interaction = interactionsForCodePoint.get(0);

			// 0 - $gt
			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			Log.e("Test $gt");
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 4 - :
			resetDevice();
			Log.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "switch.code.point");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);
			CodePointStore.storeRecord(getContext(), true, "test.interaction", appVersionName, appVersionCode);
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

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

			boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

			// Allow conditions to be met.
			VersionHistoryStore.updateVersionHistory(getContext(), 0l, "1.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 10)); // 10 days ago
			VersionHistoryStore.updateVersionHistory(getContext(), 1l, "1.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 8));  //  8 days ago
			VersionHistoryStore.updateVersionHistory(getContext(), 2l, "1.2", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 6));  //  6 days ago
			VersionHistoryStore.updateVersionHistory(getContext(), 3l, "2.0", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 4));  //  4 days ago
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertFalse(shouldRun);

			VersionHistoryStore.updateVersionHistory(getContext(), 4l, "2.1", System.currentTimeMillis() - (DateUtils.DAY_IN_MILLIS * 2));  //  2 days ago
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
			shouldRun = interaction.getCriteria().shouldRun(getContext());
			assertTrue(shouldRun);

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

			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");

			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				boolean shouldRun = interaction.getCriteria().shouldRun(getContext());
				assertTrue(shouldRun);
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

		InteractionManager.storeInteractions(getContext(), LIST_OF_VARIOUS_INTERACTIONS);
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			Interaction interaction = InteractionManager.getApplicableInteraction(getContext(), "complex_criteria");
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

		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "app.launch");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");
		CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "big.win");

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			resetDevice();
			InteractionManager.storeInteractions(getContext(), LIST_OF_VARIOUS_INTERACTIONS);
			Interaction interaction = InteractionManager.getApplicableInteraction(getContext(), "complex_criteria");
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
			CodePointStore.storeCodePointForCurrentAppVersion(getContext(), "non.existant.code.point");
			Interaction interaction = InteractionManager.getApplicableInteraction(getContext(), "non.existant.code.point");
			assertNull(interaction);
		}
		long end = System.currentTimeMillis();

		long duration = end - start;
		double average = (double) duration / iterations;
		Log.e("Finished %d iterations in %,dms, average of %.2fms per run.", iterations, duration, average);
		assertTrue(average < 20d);
	}
}
