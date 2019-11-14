/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.engagement.criteria;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionCriteria;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OperatorTests extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "engagement" + File.separator + "criteria" + File.separator;

	@Test
	public void exists() throws JSONException {
		doTest("testOperatorExists.json");
	}

	@Test
	public void not() throws JSONException {
		doTest("testOperatorNot.json");
	}

	@Test
	public void lessThan() throws JSONException {
		doTest("testOperatorLessThan.json");
	}

	@Test
	public void lessThanOrEqual() throws JSONException {
		doTest("testOperatorLessThanOrEqual.json");
	}

	@Test
	public void greaterThanOrEqual() throws JSONException {
		doTest("testOperatorGreaterThanOrEqual.json");
	}

	@Test
	public void greaterThan() throws JSONException {
		doTest("testOperatorGreaterThan.json");
	}

	@Test
	public void stringEquals() throws JSONException {
		doTest("testOperatorStringEquals.json");
	}

	@Test
	public void stringNotEquals() throws JSONException {
		doTest("testOperatorStringNotEquals.json");
	}

	@Test
	public void contains() throws JSONException {
		doTest("testOperatorContains.json");
	}

	@Test
	public void startsWith() throws JSONException {
		doTest("testOperatorStartsWith.json");
	}

	@Test
	public void endsWith() throws JSONException {
		doTest("testOperatorEndsWith.json");
	}

	@Test
	public void before() throws JSONException {
		doTest("testOperatorBefore.json");
	}

	@Test
	public void after() throws JSONException {
		doTest("testOperatorAfter.json");
	}

	private void doTest(String testFile) throws JSONException {
		String json = loadTextAssetAsString(TEST_DATA_DIR + testFile);

		InteractionCriteria criteria = new InteractionCriteria(json);

		Device device = new Device();
		FieldManager fieldManager = new FieldManager(targetContext, new VersionHistory(), new EventData(), new Person(), device, new AppRelease());

		Apptentive.DateTime dateTime = new Apptentive.DateTime(1000d);
		Apptentive.Version version = new Apptentive.Version();
		version.setVersion("1.2.3");

		device.getCustomData().put("number_5", 5);
		device.getCustomData().put("string_qwerty", "qwerty");
		device.getCustomData().put("boolean_true", true);
		device.getCustomData().put("key_with_null_value", (String) null);
		device.getCustomData().put("datetime_1000", dateTime);
		device.getCustomData().put("version_1.2.3", version);

		assertTrue(criteria.isMet(fieldManager));
	}
}
