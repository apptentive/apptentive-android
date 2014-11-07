/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.model;

import com.apptentive.android.sdk.model.CommerceExtendedData;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.LocationExtendedData;
import com.apptentive.android.sdk.model.TimeExtendedData;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.JsonDiffer;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class EventTests extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	public void testExtendedDataEvents() {
		Log.e("testExtendedDataEvents()");
		try {
			Event expected = new Event(FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testExtendedDataEvents.json"));
			// Change the expected output to use the same timezone as the test machine.
			expected.setClientCreatedAtUtcOffset(Util.getUtcOffset());

			Map<String, String> data = new HashMap<String, String>();
			data.put("data_key", "data_value");

			Map<String, Object> customData = new HashMap<String, Object>();
			customData.put("string_key", "string_value");
			customData.put("number_key", 12345.6789f);

			CommerceExtendedData commerce = null;
			try {
				commerce = new CommerceExtendedData()
					.setId("commerce_id")
					.setAffiliation(1111111111)
					.setRevenue(100d)
					.setShipping(5l)
					.setTax(4.38f)
					.setCurrency("USD");
				CommerceExtendedData.Item item = new CommerceExtendedData.Item(22222222, "Item Name", "Category", 20, 5.0d, "USD");
				commerce.addItem(item);
			} catch (JSONException e) {
				Log.e("Error: ", e);
			}
			assertNotNull(commerce);

			TimeExtendedData time = new TimeExtendedData(1.406316991957E9);

			LocationExtendedData location = new LocationExtendedData(-122.34569190000002d, 47.6288591d);


			Event actual = new Event("event_label", data, customData, commerce, time, location);
			actual.setClientCreatedAt(1.406316991967E9);
			actual.setNonce("4579c403-e8c5-4e6b-8826-f3d61e6ebb98");

			boolean equal = JsonDiffer.areObjectsEqual(expected, actual);
			Log.e("Events are equal: %b", equal);
			assertTrue(equal);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
