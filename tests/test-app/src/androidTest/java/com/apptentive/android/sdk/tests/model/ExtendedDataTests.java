/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import androidx.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.CommerceExtendedData;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.model.LocationExtendedData;
import com.apptentive.android.sdk.model.TimeExtendedData;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.JsonDiffer;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExtendedDataTests extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	@Test
	public void commerceExtendedData() throws JSONException {
		ApptentiveLog.e("testCommerceExtendedData()");
		ExtendedData expected = new CommerceExtendedData(loadTextAssetAsString(TEST_DATA_DIR + "testCommerceExtendedData.json"));

		CommerceExtendedData actual = new CommerceExtendedData()
				.setId("commerce_id")
				.setAffiliation("1111111111")
				.setRevenue(100d)
				.setShipping(5L)
				.setTax(4.38f)
				.setCurrency("USD");
		CommerceExtendedData.Item item = new CommerceExtendedData.Item(22222222, "Item Name", "Category", 20, 5.0d, "USD");
		actual.addItem(item);

		assertTrue(JsonDiffer.areObjectsEqual(expected.toJsonObject(), actual.toJsonObject()));
	}

	@Test
	public void locationExtendedData() throws JSONException {
		ApptentiveLog.e("testLocationExtendedData()");
		ExtendedData expected = new LocationExtendedData(loadTextAssetAsString(TEST_DATA_DIR + "testLocationExtendedData.json"));

		LocationExtendedData actual = new LocationExtendedData(-122.34569190000002d, 47.6288591d);

		assertEquals(expected.toJsonObject().toString(), actual.toJsonObject().toString());
	}

	@Test
	public void timeExtendedData() throws JSONException {
		ApptentiveLog.e("testTimeExtendedData()");
		ExtendedData expected = new TimeExtendedData(loadTextAssetAsString(TEST_DATA_DIR + "testTimeExtendedData.json"));

		TimeExtendedData millis = new TimeExtendedData(1406251926165L);
		ApptentiveLog.e("expected: %s\n\n millis: %s", expected.toJsonObject().toString(), millis.toJsonObject().toString());

		assertTrue(JsonDiffer.areObjectsEqual(expected.toJsonObject(), millis.toJsonObject()));

		TimeExtendedData seconds = new TimeExtendedData(1406251926.165);
		assertTrue(JsonDiffer.areObjectsEqual(expected.toJsonObject(), seconds.toJsonObject()));

		TimeExtendedData date = new TimeExtendedData(new Date(1406251926165L));
		assertTrue(JsonDiffer.areObjectsEqual(expected.toJsonObject(), date.toJsonObject()));
	}
}
