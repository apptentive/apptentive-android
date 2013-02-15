/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.os.Build;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Device extends JSONObject {

	public final static String KEY = "device";

	private final static String KEY_ID = "id";
	private final static String KEY_OS_NAME = "os_name";
	private final static String KEY_OS_VERSION = "os_version";
	private final static String KEY_OS_BUILD = "os_build";
	private final static String KEY_MANUFACTURER = "manufacturer";
	private final static String KEY_MODEL = "model";
	private final static String KEY_BOARD = "board";
	private final static String KEY_PRODUCT = "product";
	private final static String KEY_BRAND = "brand";
	private final static String KEY_CPU = "cpu";
	private final static String KEY_DEVICE = "device";
	private final static String KEY_CARRIER = "carrier";
	private final static String KEY_CURRENT_CARRIER = "current_carrier";
	private final static String KEY_NETWORK_TYPE = "network_type";
	private final static String KEY_BUILD_TYPE = "build_type";
	private final static String KEY_BUILD_ID = "build_id";
	private final static String KEY_BOOTLOADER_VERSION = "bootloader_version";
	private final static String KEY_RADIO_VERSION = "radio_version";

	public Device(String json) throws JSONException {
		super(json);
	}

	public Device() {
		try {
			put(KEY_ID, GlobalInfo.androidId);
			put(KEY_OS_NAME, "Android");
			put(KEY_OS_VERSION, Build.VERSION.RELEASE);
			put(KEY_OS_BUILD, Build.VERSION.INCREMENTAL);
			put(KEY_MANUFACTURER, Build.MANUFACTURER);
			put(KEY_MODEL, Build.MODEL);
			put(KEY_BOARD, Build.BOARD);
			put(KEY_PRODUCT, Build.PRODUCT);
			put(KEY_BRAND, Build.BRAND);
			put(KEY_CPU, Build.CPU_ABI);
			put(KEY_DEVICE, Build.DEVICE);
			put(KEY_CARRIER, GlobalInfo.carrier);
			put(KEY_CURRENT_CARRIER, GlobalInfo.currentCarrier);
			put(KEY_NETWORK_TYPE, Constants.networkTypeAsString(GlobalInfo.networkType));
			put(KEY_BUILD_TYPE, Build.TYPE);
			put(KEY_BUILD_ID, Build.ID);
		} catch (JSONException e) {
			Log.d("Error creating Device object: %s", e.getMessage());
		}
	}

	public String getId() {
		try {
			return getString(KEY_ID);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setId(String id) {
		try {
			put(KEY_ID, id);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_ID + " of " + KEY);
		}
	}

	public String getOsName() {
		try {
			return getString(KEY_OS_NAME);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setOsName(String osName) {
		try {
			put(KEY_OS_NAME, osName);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_OS_NAME + " of " + KEY);
		}
	}

	public String getOsVersion() {
		try {
			return getString(KEY_OS_VERSION);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setOsVersion(String osVersion) {
		try {
			put(KEY_OS_VERSION, osVersion);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_OS_VERSION + " of " + KEY);
		}
	}

	public String getOsBuild() {
		try {
			return getString(KEY_OS_BUILD);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setOsBuild(String osBuild) {
		try {
			put(KEY_OS_BUILD, osBuild);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_OS_BUILD + " of " + KEY);
		}
	}

	public String getManufacturer() {
		try {
			return getString(KEY_MANUFACTURER);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setManufacturer(String manufacturer) {
		try {
			put(KEY_MANUFACTURER, manufacturer);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_MANUFACTURER + " of " + KEY);
		}
	}

	public String getModel() {
		try {
			return getString(KEY_MODEL);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setModel(String model) {
		try {
			put(KEY_MODEL, model);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_MODEL + " of " + KEY);
		}
	}

	public String getBoard() {
		try {
			return getString(KEY_BOARD);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBoard(String board) {
		try {
			put(KEY_BOARD, board);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BOARD + " of " + KEY);
		}
	}

	public String getProduct() {
		try {
			return getString(KEY_PRODUCT);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setProduct(String product) {
		try {
			put(KEY_PRODUCT, product);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_PRODUCT + " of " + KEY);
		}
	}

	public String getBrand() {
		try {
			return getString(KEY_BRAND);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBrand(String brand) {
		try {
			put(KEY_BRAND, brand);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BRAND + " of " + KEY);
		}
	}

	public String getCpu() {
		try {
			return getString(KEY_CPU);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setCpu(String cpu) {
		try {
			put(KEY_CPU, cpu);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_CPU + " of " + KEY);
		}
	}

	public String getDevice() {
		try {
			return getString(KEY_DEVICE);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setDevice(String device) {
		try {
			put(KEY_DEVICE, device);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_DEVICE + " of " + KEY);
		}
	}

	public String getCarrier() {
		try {
			return getString(KEY_CARRIER);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setCarrier(String carrier) {
		try {
			put(KEY_CARRIER, carrier);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_CARRIER + " of " + KEY);
		}
	}

	public String getCurrentCarrier() {
		try {
			return getString(KEY_CURRENT_CARRIER);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setCurrentCarrier(String currentCarrier) {
		try {
			put(KEY_CURRENT_CARRIER, currentCarrier);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_CURRENT_CARRIER + " of " + KEY);
		}
	}

	public String getNetworkType() {
		try {
			return getString(KEY_NETWORK_TYPE);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setNetworkType(String networkType) {
		try {
			put(KEY_NETWORK_TYPE, networkType);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_NETWORK_TYPE + " of " + KEY);
		}
	}

	public String getBuildType() {
		try {
			return getString(KEY_BUILD_TYPE);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBuildType(String buildType) {
		try {
			put(KEY_BUILD_TYPE, buildType);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BUILD_TYPE + " of " + KEY);
		}
	}

	public String getBuildId() {
		try {
			return getString(KEY_BUILD_ID);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBuildId(String buildId) {
		try {
			put(KEY_BUILD_ID, buildId);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BUILD_ID + " of " + KEY);
		}
	}

	public String getBootloaderVersion() {
		try {
			return getString(KEY_BOOTLOADER_VERSION);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		try {
			put(KEY_BOOTLOADER_VERSION, bootloaderVersion);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_BOOTLOADER_VERSION + " of " + KEY);
		}
	}

	public String getRadioVersion() {
		try {
			return getString(KEY_RADIO_VERSION);
		} catch (JSONException e) {
			return null;
		}
	}

	public void setRadioVersion(String radioVersion) {
		try {
			put(KEY_RADIO_VERSION, radioVersion);
		} catch (JSONException e) {
			Log.e("Unable to set field " + KEY_RADIO_VERSION + " of " + KEY);
		}
	}
}
