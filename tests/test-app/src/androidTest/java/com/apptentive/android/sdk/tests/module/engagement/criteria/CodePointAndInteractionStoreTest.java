/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
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
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
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
	public void testCodePointInvokesVersion() {
		ApptentiveLog.e("Running test: testCodePointInvokesVersion()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointInvokesVersion.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $gt
			resetDevice();
			ApptentiveLog.e("Test $gt");
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);
			ApptentiveLog.e("Test :");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);
			ApptentiveLog.e("Test $lte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			ApptentiveInternal.getInstance().getInteractionManager().storeInteractionsPayloadString( json);
			ApptentiveLog.e("Test $lt");
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( false, "test.code.point", "1.1", 3);
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
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
	public void testCodePointLastInvokedAt() {
		ApptentiveLog.e("Running test: testCodePointLastInvokedAt()\n\n");

		String json = loadFileAssetAsString(TEST_DIR + "testCodePointLastInvokedAt.json");

		try {
			InteractionCriteria criteria = new InteractionCriteria(json);

			// 0 - $after
			resetDevice();
			ApptentiveLog.e("Test $after");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 1 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			sleep(300);
			assertTrue(criteria.isMet());
			sleep(300);
			assertTrue(criteria.isMet());

			// 2 - $eq // There's no easy way to test this unless we contrive the times.
			resetDevice();
			ApptentiveLog.e("Test $eq");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 3 - : // Ditto
			resetDevice();
			ApptentiveLog.e("Test :");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
			sleep(300);
			assertFalse(criteria.isMet());
			sleep(300);
			assertFalse(criteria.isMet());

			// 4 - $before
			resetDevice();
			ApptentiveLog.e("Test $before");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "test.code.point");
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
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 1 - $gte
			resetDevice();
			ApptentiveLog.e("Test $gte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 2 - $ne
			resetDevice();
			ApptentiveLog.e("Test $ne");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());

			// 3 - $eq
			resetDevice();
			ApptentiveLog.e("Test $eq");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 4 - :
			resetDevice();
			ApptentiveLog.e("Test :");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 5 - $lte
			resetDevice();
			ApptentiveLog.e("Test $lte");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

			// 6 - $lt
			resetDevice();
			ApptentiveLog.e("Test $lt");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			ApptentiveInternal.getInstance().getCodePointStore().storeCodePointForCurrentAppVersion( "switch.code.point");
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertTrue(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());
			ApptentiveInternal.getInstance().getCodePointStore().storeRecord( true, "test.interaction", appVersionName, appVersionCode);
			assertFalse(criteria.isMet());

		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
		ApptentiveLog.e("Finished test.");
	}
}
