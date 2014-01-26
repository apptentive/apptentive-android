/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Device;
import com.apptentive.android.sdk.module.survey.SurveyManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Reflection;
import org.json.JSONException;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A helper class with static methods for getting, storing, retrieving, and diffing information about the current device.
 *
 * @author Sky Kelsey
 */
public class DeviceManager {

	/**
	 * If any device setting has changed, return only the changed fields in a new Device object. If a field's value was
	 * cleared, set that value to null in the Device. The first time this is called, all Device will be returned.
	 *
	 * @return A Device containing diff data which, when added to the last sent Device, yields the new Device.
	 */
	public static Device storeDeviceAndReturnDiff(Context context) {

		Device stored = loadOldDevice(context);

		Device current = generateNewDevice(context);
		CustomData customData = loadCustomDeviceData(context);
		current.setCustomData(customData);
		CustomData integrationConfig = loadIntegrationConfig(context);
		current.setIntegrationConfig(integrationConfig);

		Device diff = diffDevice(stored, current);
		if (diff != null) {
			storeDevice(context, current);
			return diff;
		}
		return null;
	}

	public static CustomData loadCustomDeviceData(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String deviceDataString = prefs.getString(Constants.PREF_KEY_DEVICE_DATA, null);
		try {
			return new CustomData(deviceDataString);
		} catch (Exception e) {
		}
		try {
			return new CustomData();
		} catch (JSONException e) {
		}
		return null; // This should never happen.
	}

	public static void storeCustomDeviceData(Context context, CustomData deviceData) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String deviceDataString = deviceData.toString();
		prefs.edit().putString(Constants.PREF_KEY_DEVICE_DATA, deviceDataString).commit();
	}

	public static CustomData loadIntegrationConfig(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String integrationConfigString = prefs.getString(Constants.PREF_KEY_DEVICE_INTEGRATION_CONFIG, null);
		try {
			return new CustomData(integrationConfigString);
		} catch (Exception e) {
		}
		try {
			return new CustomData();
		} catch (JSONException e) {
		}
		return null; // This should never happen.
	}

	public static void storeIntegrationConfig(Context context, CustomData integrationConfig) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String integrationConfigString = integrationConfig.toString();
		prefs.edit().putString(Constants.PREF_KEY_DEVICE_INTEGRATION_CONFIG, integrationConfigString).commit();
	}

	private static Device generateNewDevice(Context context) {
		Device device = new Device();

		// First, get all the information we can load from static resources.
		device.setOsName("Android");
		device.setOsVersion(Build.VERSION.RELEASE);
		device.setOsBuild(Build.VERSION.INCREMENTAL);
		device.setOsApiLevel("" + Build.VERSION.SDK_INT);
		device.setManufacturer(Build.MANUFACTURER);
		device.setModel(Build.MODEL);
		device.setBoard(Build.BOARD);
		device.setProduct(Build.PRODUCT);
		device.setBrand(Build.BRAND);
		device.setCpu(Build.CPU_ABI);
		device.setDevice(Build.DEVICE);
		device.setUuid(GlobalInfo.androidId);
		device.setBuildType(Build.TYPE);
		device.setBuildId(Build.ID);

		// Second, set the stuff that requires querying system services.
		TelephonyManager tm = ((TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE)));
		device.setCarrier(tm.getSimOperatorName());
		device.setCurrentCarrier(tm.getNetworkOperatorName());
		device.setNetworkType(Constants.networkTypeAsString(tm.getNetworkType()));

		// Finally, use reflection to try loading from APIs that are not available on all Android versions.
		device.setBootloaderVersion(Reflection.getBootloaderVersion());
		device.setRadioVersion(Reflection.getRadioVersion());

		device.setLocaleCountryCode(Locale.getDefault().getCountry());
		device.setLocaleLanguageCode(Locale.getDefault().getLanguage());
		device.setLocaleRaw(Locale.getDefault().toString());
		device.setUtcOffset("" + (TimeZone.getDefault().getRawOffset() / 1000));
		return device;
	}

	private static Device loadOldDevice(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String deviceString = prefs.getString(Constants.PREF_KEY_DEVICE, null);
		try {
			return new Device(deviceString);
		} catch (Exception e) {
		}
		return null;
	}

	private static void storeDevice(Context context, Device device) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_DEVICE, device.toString()).commit();
	}

	/**
	 * Creates a new Device object with the values from newer where they are different from older. If a value exists
	 * in older but not newer, an empty string is set for that key, which tells the server to clear the value. A null
	 * values for a key will not be written so that this method only returns a strict diff of older and newer.
	 *
	 * @return A new Device object if there were any differences, else null.
	 */
	private static Device diffDevice(Device old, Device newer) {
		if (old == null) {
			return newer;
		}

		Device ret = new Device();
		int baseEntries = ret.length();

		String uuid = chooseLatest(old.getUuid(), newer.getUuid());
		if (uuid != null) {
			ret.setUuid(uuid);
		}

		String osName = chooseLatest(old.getOsName(), newer.getOsName());
		if (osName != null) {
			ret.setOsName(osName);
		}

		String osVersion = chooseLatest(old.getOsVersion(), newer.getOsVersion());
		if (osVersion != null) {
			ret.setOsVersion(osVersion);
		}

		String osBuild = chooseLatest(old.getOsBuild(), newer.getOsBuild());
		if (osBuild != null) {
			ret.setOsBuild(osBuild);
		}

		String osApiLevel = chooseLatest(old.getOsApiLevel(), newer.getOsApiLevel());
		if (osApiLevel != null) {
			ret.setOsApiLevel(osApiLevel);
		}

		String manufacturer = chooseLatest(old.getManufacturer(), newer.getManufacturer());
		if (manufacturer != null) {
			ret.setManufacturer(manufacturer);
		}

		String model = chooseLatest(old.getModel(), newer.getModel());
		if (model != null) {
			ret.setModel(model);
		}

		String board = chooseLatest(old.getBoard(), newer.getBoard());
		if (board != null) {
			ret.setBoard(board);
		}

		String product = chooseLatest(old.getProduct(), newer.getProduct());
		if (product != null) {
			ret.setProduct(product);
		}

		String brand = chooseLatest(old.getBrand(), newer.getBrand());
		if (brand != null) {
			ret.setBrand(brand);
		}

		String cpu = chooseLatest(old.getCpu(), newer.getCpu());
		if (cpu != null) {
			ret.setCpu(cpu);
		}

		String device = chooseLatest(old.getDevice(), newer.getDevice());
		if (device != null) {
			ret.setDevice(device);
		}

		String carrier = chooseLatest(old.getCarrier(), newer.getCarrier());
		if (carrier != null) {
			ret.setCarrier(carrier);
		}

		String currentCarrier = chooseLatest(old.getCurrentCarrier(), newer.getCurrentCarrier());
		if (currentCarrier != null) {
			ret.setCurrentCarrier(currentCarrier);
		}

		String networkType = chooseLatest(old.getNetworkType(), newer.getNetworkType());
		if (networkType != null) {
			ret.setNetworkType(networkType);
		}

		String buildType = chooseLatest(old.getBuildType(), newer.getBuildType());
		if (buildType != null) {
			ret.setBuildType(buildType);
		}

		String buildId = chooseLatest(old.getBuildId(), newer.getBuildId());
		if (buildId != null) {
			ret.setBuildId(buildId);
		}

		String bootloaderVersion = chooseLatest(old.getBootloaderVersion(), newer.getBootloaderVersion());
		if (bootloaderVersion != null) {
			ret.setBootloaderVersion(bootloaderVersion);
		}

		String radioVersion = chooseLatest(old.getRadioVersion(), newer.getRadioVersion());
		if (radioVersion != null) {
			ret.setRadioVersion(radioVersion);
		}

		CustomData customData = chooseLatest(old.getCustomData(), newer.getCustomData());
		if (customData != null) {
			ret.setCustomData(customData);
		}

		CustomData integrationConfig = chooseLatest(old.getIntegrationConfig(), newer.getIntegrationConfig());
		if (integrationConfig != null) {
			ret.setIntegrationConfig(integrationConfig);
		}

		String localeCountryCode = chooseLatest(old.getLocaleCountryCode(), newer.getLocaleCountryCode());
		if (localeCountryCode != null) {
			ret.setLocaleCountryCode(localeCountryCode);
		}

		String localeLanguageCode = chooseLatest(old.getLocaleLanguageCode(), newer.getLocaleLanguageCode());
		if (localeLanguageCode != null) {
			ret.setLocaleLanguageCode(localeLanguageCode);
		}

		String localeRaw = chooseLatest(old.getLocaleRaw(), newer.getLocaleRaw());
		if (localeRaw != null) {
			ret.setLocaleRaw(localeRaw);
		}

		String utcOffset = chooseLatest(old.getUtcOffset(), newer.getUtcOffset());
		if (utcOffset != null) {
			ret.setUtcOffset(utcOffset);
		}


		// If there were no differences, return null.
		if (ret.length() <= baseEntries) {
			return null;
		}
		return ret;
	}

	/**
	 * A convenience method.
	 *
	 * @return newer - if it is different from old. <p/>empty string - if there was an old value, but not a newer value. This clears the old value.<p/> null - if there is no difference.
	 */
	private static String chooseLatest(String old, String newer) {
		if (old == null || old.equals("")) {
			old = null;
		}
		if (newer == null || newer.equals("")) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			return "";
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}

	private static CustomData chooseLatest(CustomData old, CustomData newer) {
		if (old == null || old.length() == 0) {
			old = null;
		}
		if (newer == null || newer.length() == 0) {
			newer = null;
		}

		// New value.
		if (old != null && newer != null && !old.equals(newer)) {
			return newer;
		}

		// Clear existing value.
		if (old != null && newer == null) {
			try {
				return new CustomData();
			} catch (JSONException e) {
				return null;
			}
		}

		if (old == null && newer != null) {
			return newer;
		}

		// Do nothing.
		return null;
	}

	public static void onSentDeviceInfo(Context appContext) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Constants.PREF_KEY_DEVICE_DATA_SENT, true).commit();
		SurveyManager.asyncFetchAndStoreSurveysIfCacheExpired(appContext);
	}
}
