/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CodePointAndInteractionStoreTest extends ApptentiveTestCaseBase {

	private static final String TEST_DIR = "engagement/criteria/";

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointInvokesTotal() {
		String json = FileUtil.loadTextAssetAsString(TEST_DIR + "testCodePointInvokesTotal.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			ApptentiveLog.e("Test $gt");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointInvokesVersion() {
		String json = FileUtil.loadTextAssetAsString(TEST_DIR + "testCodePointInvokesVersion.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			ApptentiveLog.e("Test $gt");
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			interactionManager.storeInteractionsPayloadString(json);
			ApptentiveLog.e("Test :");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			interactionManager.storeInteractionsPayloadString(json);
			ApptentiveLog.e("Test $lte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			interactionManager.storeInteractionsPayloadString(json);
			ApptentiveLog.e("Test $lt");
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeRecord(false, "test.code.point", "1.1", 3);
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertFalse(criteria.isMet());

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void codePointLastInvokedAt() {
		String json = FileUtil.loadTextAssetAsString(TEST_DIR + "testCodePointLastInvokedAt.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $after
			resetDevice();
			ApptentiveLog.e("Test $after");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			sleep(300);
			assertTrue(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 1 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			sleep(300);
			assertTrue(criteria.isMet());
			sleep(300);
			assertTrue(criteria.isMet());

			// 2 - $eq // There's no easy way to test this unless we contrive the times.
			resetDevice();
			ApptentiveLog.e("Test $eq");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			sleep(300);
			assertFalse(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 3 - : // Ditto
			resetDevice();
			ApptentiveLog.e("Test :");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			sleep(300);
			assertFalse(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 4 - $before
			resetDevice();
			ApptentiveLog.e("Test $before");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeCodePointForCurrentAppVersion("test.code.point");
			sleep(300);
			assertFalse(criteria.isMet());
			sleep(300);
			assertTrue(criteria.isMet());

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	/**
	 * Tests for a specific code point running. Tests all condition types.
	 */
	@Test
	public void interactionInvokesTotal() {
		String appVersionName = Util.getAppVersionName(targetContext);
		int appVersionCode = Util.getAppVersionCode(targetContext);
		String json = FileUtil.loadTextAssetAsString(TEST_DIR + "testInteractionInvokesTotal.json");
		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			ApptentiveLog.e("Test $gt");
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			codePointStore.storeCodePointForCurrentAppVersion("switch.code.point");
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			codePointStore.storeRecord(true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
