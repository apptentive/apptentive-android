/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
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
		Log.e("Running test: testCodePointInvokesTotal()\n\n");
		resetDevice();

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointInvokesTotal.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			Log.e("Test $gt");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			Log.e("Test :");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCodePointInvokesVersion() {
		Log.e("Running test: testCodePointInvokesVersion()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointInvokesVersion.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			Log.e("Test $gt");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
			Log.e("Test :");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
			Log.e("Test $lte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			ApptentiveInternal.getInteractionManager(getTargetContext()).storeInteractionsPayloadString(getTargetContext(), json);
			Log.e("Test $lt");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertFalse(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testCodePointLastInvokedAt() {
		Log.e("Running test: testCodePointLastInvokedAt()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointLastInvokedAt.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $after
			resetDevice();
			Log.e("Test $after");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 1 - $ne
			resetDevice();
			Log.e("Test $ne");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $eq // There's no easy way to test this unless we contrive the times.
			resetDevice();
			Log.e("Test $eq");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 3 - : // Ditto
			resetDevice();
			Log.e("Test :");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - $before
			resetDevice();
			Log.e("Test $before");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet(getTargetContext()));
			sleep(300);
			assertTrue(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	public void testInteractionInvokesTotal() {
		Log.e("Running test: testInteractionInvokesTotal()\n\n");
		resetDevice();
		String appVersionName = Util.getAppVersionName(getTargetContext());
		int appVersionCode = Util.getAppVersionCode(getTargetContext());

		String json = loadFileAssetAsString(TEST_DIR + "testInteractionInvokesTotal.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			Log.e("Test $gt");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 1 - $gte
			resetDevice();
			Log.e("Test $gte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 2 - $ne
			resetDevice();
			Log.e("Test $ne");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));

			// 3 - $eq
			resetDevice();
			Log.e("Test $eq");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 4 - :
			resetDevice();
			Log.e("Test :");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 5 - $lte
			resetDevice();
			Log.e("Test $lte");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

			// 6 - $lt
			resetDevice();
			Log.e("Test $lt");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeCodePointForCurrentAppVersion(getTargetContext(), "switch.code.point");
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));
			ApptentiveInternal.getCodePointStore(getTargetContext()).storeRecord(getTargetContext(), true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet(getTargetContext()));

		} catch (JSONException e) {
			Log.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		Log.e("Finished test.");
	}
}
