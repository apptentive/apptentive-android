/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.FileUtil;
import com.apptentive.android.sdk.util.JsonDiffer;
import org.json.JSONException;
import org.json.JSONObject;

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
			JSONObject expected = new JSONObject(FileUtil.loadFileAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testExtendedDataEvents.json"));

			Map<String, String> data = new HashMap<String, String>();
			data.put("data_key", "data_value");

			Map<String, Object> customData = new HashMap<String, Object>();
			customData.put("string_key", "string_value");
			customData.put("number_key", 12345.6789f);

			CommerceExtendedData commerce = new CommerceExtendedData("commerce_id")
				.setAffiliation(1111111111)
				.setRevenue(100d)
				.setShipping(5l)
				.setTax(4.38f)
				.setCurrency("USD")
				.addItem(22222222, "Item Name", "Category", 20, 5.0d, "USD");

			TimeExtendedData time = new TimeExtendedData(1.406316991957E9);

			LocationExtendedData location = new LocationExtendedData(-122.34569190000002d, 47.6288591d);

			Event event = new Event("event_label", data, customData, commerce, time, location);
			event.setClientCreatedAt(1.406316991967E9);
			event.setNonce("4579c403-e8c5-4e6b-8826-f3d61e6ebb98");

			boolean equal = JsonDiffer.areObjectsEqual(expected, event);
			Log.e("Events are equal: %b", equal);
			assertTrue(equal);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
