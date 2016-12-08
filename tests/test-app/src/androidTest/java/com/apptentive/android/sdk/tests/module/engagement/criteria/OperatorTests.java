/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OperatorTests extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void exists() {
		doTest("testOperatorExists.json");
	}

	@Test
	public void not() {
		doTest("testOperatorNot.json");
	}

	@Test
	public void lessThan() {
		doTest("testOperatorLessThan.json");
	}

	@Test
	public void lessThanOrEqual() {
		doTest("testOperatorLessThanOrEqual.json");
	}

	@Test
	public void greaterThanOrEqual() {
		doTest("testOperatorGreaterThanOrEqual.json");
	}

	@Test
	public void greaterThan() {
		doTest("testOperatorGreaterThan.json");
	}

	@Test
	public void stringEquals() {
		doTest("testOperatorStringEquals.json");
	}

	@Test
	public void stringNotEquals() {
		doTest("testOperatorStringNotEquals.json");
	}

	@Test
	public void contains() {
		doTest("testOperatorContains.json");
	}

	@Test
	public void startsWith() {
		doTest("testOperatorStartsWith.json");
	}

	@Test
	public void endsWith() {
		doTest("testOperatorEndsWith.json");
	}

	@Test
	public void before() {
		doTest("testOperatorBefore.json");
	}

	@Test
	public void after() {
		doTest("testOperatorAfter.json");
	}

	private void doTest(String testFile) {
		String json = loadTextAssetAsString(TEST_DATA_DIR + testFile);

		Apptentive.DateTime dateTime = new Apptentive.DateTime(1000d);
		Apptentive.Version version = new Apptentive.Version();
		version.setVersion("1.2.3");

		try {
			Apptentive.addCustomDeviceData("number_5", 5);
			Apptentive.addCustomDeviceData("string_qwerty", "qwerty");
			Apptentive.addCustomDeviceData("boolean_true", true);
			Apptentive.addCustomDeviceData("key_with_null_value", (String) null);

			// Need to use ApptentiveInternal because we don't expose complex types in our public API yet.
			ApptentiveInternal.getInstance(targetContext).addCustomDeviceData("datetime_1000", dateTime);
			ApptentiveInternal.getInstance().addCustomDeviceData("version_1.2.3", version);

			DeviceManager.storeDeviceAndReturnIt();
			InteractionCriteria criteria = new InteractionCriteria(json);
			assertTrue(criteria.isMet());
		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing test JSON.", e);
			assertNull(e);
		}
	}
}
