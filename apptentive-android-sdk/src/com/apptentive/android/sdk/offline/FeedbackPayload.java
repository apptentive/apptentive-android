/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import android.os.Build;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Reflection;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class FeedbackPayload extends RecordPayload {

	private static final String KEY_RECORD = "record";

	private static final String KEY_DEVICE = "device";
	private static final String KEY_DEVICE_OS_NAME = "os_name";
	private static final String KEY_DEVICE_OS_VERSION = "os_version";
	private static final String KEY_DEVICE_OS_BUILD = "os_build";
	private static final String KEY_DEVICE_MANUFACTURER = "manufacturer";
	private static final String KEY_DEVICE_MODEL = "model";
	private static final String KEY_DEVICE_BOARD = "board";
	private static final String KEY_DEVICE_PRODUCT = "product";
	private static final String KEY_DEVICE_BRAND = "brand";
	private static final String KEY_DEVICE_CPU = "cpu";
	private static final String KEY_DEVICE_DEVICE = "device";
	private static final String KEY_DEVICE_UUID = "uuid";
	private static final String KEY_DEVICE_CARRIER = "carrier";
	private static final String KEY_DEVICE_CURRENT_CARRIER = "current_carrier";
	private static final String KEY_DEVICE_NETWORK_TYPE = "network_type";
	private static final String KEY_DEVICE_TYPE = "type";
	private static final String KEY_DEVICE_ID = "id";
	private static final String KEY_DEVICE_BOOTLOADER_VERSION = "bootloader_version";
	private static final String KEY_DEVICE_RADIO_VERSION = "radio_version";

	private static final String KEY_CLIENT = "client";
	private static final String KEY_CLIENT_VERSION = "version";

	private static final String KEY_FEEDBACK = "feedback";
	private static final String KEY_FEEDBACK_TYPE = "type";
	private static final String KEY_FEEDBACK_FEEDBACK = "feedback";

	private static final String KEY_USER = "user";
	private static final String KEY_USER_EMAIL = "email";

	private static final String KEY_DATA = "data";

	public FeedbackPayload(String json) throws JSONException {
		super(json);
	}

	public FeedbackPayload() {
		super();
		try {
			// Add in Android specific static device info
			JSONObject record = new JSONObject();
			put(KEY_RECORD, record);
			JSONObject device = new JSONObject();
			record.put(KEY_DEVICE, device);
			device.put(KEY_DEVICE_OS_NAME, "Android");
			device.put(KEY_DEVICE_OS_VERSION, Build.VERSION.RELEASE);
			device.put(KEY_DEVICE_OS_BUILD, Build.VERSION.INCREMENTAL);
			device.put(KEY_DEVICE_MANUFACTURER, Build.MANUFACTURER);
			device.put(KEY_DEVICE_MODEL, Build.MODEL);
			device.put(KEY_DEVICE_BOARD, Build.BOARD);
			device.put(KEY_DEVICE_PRODUCT, Build.PRODUCT);
			device.put(KEY_DEVICE_BRAND, Build.BRAND);
			device.put(KEY_DEVICE_CPU, Build.CPU_ABI);
			device.put(KEY_DEVICE_DEVICE, Build.DEVICE);
			device.put(KEY_DEVICE_UUID, GlobalInfo.androidId);
			device.put(KEY_DEVICE_CARRIER, GlobalInfo.carrier);
			device.put(KEY_DEVICE_CURRENT_CARRIER, GlobalInfo.currentCarrier);
			device.put(KEY_DEVICE_NETWORK_TYPE, Constants.networkTypeAsString(GlobalInfo.networkType));
			device.put(KEY_DEVICE_TYPE, Build.TYPE);
			device.put(KEY_DEVICE_ID, Build.ID);

			// Use reflection to load info from classes not available at API level 7.
			String bootloaderVersion = Reflection.getBootloaderVersion();
			if (bootloaderVersion != null) {
				device.put(KEY_DEVICE_BOOTLOADER_VERSION, bootloaderVersion);
			}
			String radioVersion = Reflection.getRadioVersion();
			if (radioVersion != null) {
				device.put(KEY_DEVICE_RADIO_VERSION, radioVersion);
			}

			JSONObject client = new JSONObject();
			record.put(KEY_CLIENT, client);
			client.put(KEY_CLIENT_VERSION, GlobalInfo.APPTENTIVE_API_VERSION);

			JSONObject feedback = new JSONObject();
			record.put(KEY_FEEDBACK, feedback);
			feedback.put(KEY_FEEDBACK_TYPE, "feedback");

			JSONObject user = new JSONObject();
			record.put(KEY_USER, user);

		} catch (JSONException e) {
			Log.e("Unable to construct feedback payload.", e);
		}
	}

	public String getEmail() {
		try {
			if(has(KEY_RECORD)) {
				JSONObject record = getJSONObject(KEY_RECORD);
				if (record.has(KEY_USER)) {
					JSONObject user = getJSONObject(KEY_USER);
					if (user.has(KEY_USER_EMAIL)) {
						return user.getString(KEY_USER_EMAIL);
					}
				}
			}
		} catch (JSONException e) {
			Log.w("Error getting email from feedback.");
		}
		return null;
	}

	public void setEmail(String email) {
		try {
			JSONObject record = getJSONObject(KEY_RECORD);
			JSONObject user;
			if (!record.has(KEY_USER)) {
				user = new JSONObject();
				record.put(KEY_USER, user);
			} else {
				user = record.getJSONObject(KEY_USER);
			}
			user.put(KEY_USER_EMAIL, email);
		} catch (JSONException e) {
		}
	}

	public void setFeedback(String feedbackText) {
		try {
			JSONObject record = getJSONObject(KEY_RECORD);
			JSONObject feedback;
			if (!record.has(KEY_FEEDBACK)) {
				feedback = new JSONObject();
				record.put(KEY_FEEDBACK, feedback);
			} else {
				feedback = record.getJSONObject(KEY_FEEDBACK);
			}
			feedback.put(KEY_FEEDBACK_FEEDBACK, feedbackText);
		} catch (JSONException e) {
			Log.e("BLAH", e);
		}
	}

	public void setData(Map<String, String> dataToAdd) {
		try {
			JSONObject record = getJSONObject(KEY_RECORD);
			record.remove(KEY_DATA);
			JSONObject data = new JSONObject();
			record.put(KEY_DATA, data);
			for (String key : dataToAdd.keySet()) {
				data.put(key, dataToAdd.get(key));
			}
		} catch (JSONException e) {
		}
	}

}
