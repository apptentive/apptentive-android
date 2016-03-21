/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class CodePointAndInteractionStoreTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DIR = "engagement/criteria/";

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCodePointInvokesTotal() {
		ApptentiveLog.e("Running test: testCodePointInvokesTotal()\n\n");
		resetDevice();

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointInvokesTotal.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			ApptentiveLog.e("Test $gt");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCodePointInvokesVersion() {
		ApptentiveLog.e("Running test: testCodePointInvokesVersion()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointInvokesVersion.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			ApptentiveLog.e("Test $gt");
			CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			CodePointStore.storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
			ApptentiveLog.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
			ApptentiveLog.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			InteractionManager.storeInteractionsPayloadString(getTargetContext(), json);
			ApptentiveLog.e("Test $lt");
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
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCodePointLastInvokedAt() {
		ApptentiveLog.e("Running test: testCodePointLastInvokedAt()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointLastInvokedAt.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $after
			resetDevice();
			ApptentiveLog.e("Test $after");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 1 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $eq // There's no easy way to test this unless we contrive the times.
			resetDevice();
			ApptentiveLog.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 3 - : // Ditto
			resetDevice();
			ApptentiveLog.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - $before
			resetDevice();
			ApptentiveLog.e("Test $before");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testInteractionInvokesTotal() {
		ApptentiveLog.e("Running test: testInteractionInvokesTotal()\n\n");
		resetDevice();
		String appVersionName = Util.getAppVersionName(getTargetContext());
		int appVersionCode = Util.getAppVersionCode(getTargetContext());

		String json = loadFileAssetAsString(TEST_DIR + "testInteractionInvokesTotal.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			ApptentiveLog.e("Test $gt");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			CodePointStore.storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			CodePointStore.storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}
}
