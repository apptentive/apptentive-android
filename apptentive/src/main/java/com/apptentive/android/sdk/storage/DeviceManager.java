/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.model.DevicePayload;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A helper class with static methods for and diffing information about the current device.
 */
public class DeviceManager {

	public static Device generateNewDevice(Context context) {
		Device device = new Device();

		// First, get all the information we can load from static resources.
		device.setOsName("Android");
		device.setOsVersion(Build.VERSION.RELEASE);
		device.setOsBuild(Build.VERSION.INCREMENTAL);
		device.setOsApiLevel(Build.VERSION.SDK_INT);
		device.setManufacturer(Build.MANUFACTURER);
		device.setModel(Build.MODEL);
		device.setBoard(Build.BOARD);
		device.setProduct(Build.PRODUCT);
		device.setBrand(Build.BRAND);
		device.setCpu(Build.CPU_ABI);
		device.setDevice(Build.DEVICE);
		device.setUuid(Util.getAndroidId(context));
		device.setBuildType(Build.TYPE);
		device.setBuildId(Build.ID);

		// Second, set the stuff that requires querying system services.
		TelephonyManager tm = ((TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE)));
		device.setCarrier(tm.getSimOperatorName());
		device.setCurrentCarrier(tm.getNetworkOperatorName());
		device.setNetworkType(Constants.networkTypeAsString(tm.getNetworkType()));


		try {
			device.setBootloaderVersion((String) Build.class.getField("BOOTLOADER").get(null));
		} catch (Exception e) {
			//
		}
		device.setRadioVersion(Build.getRadioVersion());


		device.setLocaleCountryCode(Locale.getDefault().getCountry());
		device.setLocaleLanguageCode(Locale.getDefault().getLanguage());
		device.setLocaleRaw(Locale.getDefault().toString());
		device.setUtcOffset(String.valueOf((TimeZone.getDefault().getRawOffset() / 1000)));
		return device;
	}

	public static DevicePayload getDiffPayload(com.apptentive.android.sdk.storage.Device oldDevice, com.apptentive.android.sdk.storage.Device newDevice) {
		if (newDevice == null) {
			return null;
		}

		DevicePayload ret = new DevicePayload();
		boolean changed = false;

		if (oldDevice == null || !equal(oldDevice.getUuid(), newDevice.getUuid())) {
			ret.setUuid(newDevice.getUuid());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsName(), newDevice.getOsName())) {
			ret.setOsName(newDevice.getOsName());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsVersion(), newDevice.getOsVersion())) {
			ret.setOsVersion(newDevice.getOsVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsBuild(), newDevice.getOsBuild())) {
			ret.setOsBuild(newDevice.getOsBuild());
			changed = true;
		}

		if (oldDevice == null || oldDevice.getOsApiLevel() != newDevice.getOsApiLevel()) {
			ret.setOsApiLevel(String.valueOf(newDevice.getOsApiLevel()));
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getManufacturer(), newDevice.getManufacturer())) {
			ret.setManufacturer(newDevice.getManufacturer());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getModel(), newDevice.getModel())) {
			ret.setModel(newDevice.getModel());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBoard(), newDevice.getBoard())) {
			ret.setBoard(newDevice.getBoard());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getProduct(), newDevice.getProduct())) {
			ret.setProduct(newDevice.getProduct());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBrand(), newDevice.getBrand())) {
			ret.setBrand(newDevice.getBrand());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCpu(), newDevice.getCpu())) {
			ret.setCpu(newDevice.getCpu());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getDevice(), newDevice.getDevice())) {
			ret.setDevice(newDevice.getDevice());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCarrier(), newDevice.getCarrier())) {
			ret.setCarrier(newDevice.getCarrier());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCurrentCarrier(), newDevice.getCurrentCarrier())) {
			ret.setCurrentCarrier(newDevice.getCurrentCarrier());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getNetworkType(), newDevice.getNetworkType())) {
			ret.setNetworkType(newDevice.getNetworkType());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBuildType(), newDevice.getBuildType())) {
			ret.setBuildType(newDevice.getBuildType());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBuildId(), newDevice.getBuildId())) {
			ret.setBuildId(newDevice.getBuildId());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBootloaderVersion(), newDevice.getBootloaderVersion())) {
			ret.setBootloaderVersion(newDevice.getBootloaderVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getRadioVersion(), newDevice.getRadioVersion())) {
			ret.setRadioVersion(newDevice.getRadioVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCustomData(), newDevice.getCustomData())) {
			CustomData customData = newDevice.getCustomData();
			ret.setCustomData(customData != null ? customData.toJson() : null);
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleCountryCode(), newDevice.getLocaleCountryCode())) {
			ret.setLocaleCountryCode(newDevice.getLocaleCountryCode());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleLanguageCode(), newDevice.getLocaleLanguageCode())) {
			ret.setLocaleLanguageCode(newDevice.getLocaleLanguageCode());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleRaw(), newDevice.getLocaleRaw())) {
			ret.setLocaleRaw(newDevice.getLocaleRaw());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getUtcOffset(), newDevice.getUtcOffset())) {
			ret.setUtcOffset(newDevice.getUtcOffset());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getIntegrationConfig(), newDevice.getIntegrationConfig())) {
			IntegrationConfig integrationConfig = newDevice.getIntegrationConfig();
			ret.setIntegrationConfig(integrationConfig != null ? integrationConfig.toJson() : null);
			changed = true;
		}
		return changed ? ret : null;
	}

	private static boolean equal(Object a, Object b) {
		return a == null && b == null || a != null && b != null && a.equals(b);
	}
}
