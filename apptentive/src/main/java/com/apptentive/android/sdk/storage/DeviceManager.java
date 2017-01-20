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
import com.apptentive.android.sdk.util.Constants;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A helper class with static methods for and diffing information about the current device.
 */
public class DeviceManager {

	public static Device generateNewDevice() {
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
		device.setUuid(ApptentiveInternal.getInstance().getAndroidId());
		device.setBuildType(Build.TYPE);
		device.setBuildId(Build.ID);

		// Second, set the stuff that requires querying system services.
		TelephonyManager tm = ((TelephonyManager) (ApptentiveInternal.getInstance().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)));
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

	public static void onSentDeviceInfo() {
	}

	public static com.apptentive.android.sdk.model.Device getDiffPayload(com.apptentive.android.sdk.storage.Device oldDevice, com.apptentive.android.sdk.storage.Device newDevice) {
		if (newDevice == null) {
			return null;
		}

		com.apptentive.android.sdk.model.Device ret = new com.apptentive.android.sdk.model.Device();

		if (oldDevice == null || !oldDevice.getUuid().equals(newDevice.getUuid())) {
			ret.setUuid(newDevice.getUuid());
		}

		if (oldDevice == null || !oldDevice.getOsName().equals(newDevice.getOsName())) {
			ret.setOsName(newDevice.getOsName());
		}

		if (oldDevice == null || !oldDevice.getOsVersion().equals(newDevice.getOsVersion())) {
			ret.setOsVersion(newDevice.getOsVersion());
		}

		if (oldDevice == null || !oldDevice.getOsBuild().equals(newDevice.getOsBuild())) {
			ret.setOsBuild(newDevice.getOsBuild());
		}

		if (oldDevice == null || oldDevice.getOsApiLevel() != newDevice.getOsApiLevel()) {
			ret.setOsApiLevel(String.valueOf(newDevice.getOsApiLevel()));
		}

		if (oldDevice == null || !oldDevice.getManufacturer().equals(newDevice.getManufacturer())) {
			ret.setManufacturer(newDevice.getManufacturer());
		}

		if (oldDevice == null || !oldDevice.getModel().equals(newDevice.getModel())) {
			ret.setModel(newDevice.getModel());
		}

		if (oldDevice == null || !oldDevice.getBoard().equals(newDevice.getBoard())) {
			ret.setBoard(newDevice.getBoard());
		}

		if (oldDevice == null || !oldDevice.getProduct().equals(newDevice.getProduct())) {
			ret.setProduct(newDevice.getProduct());
		}

		if (oldDevice == null || !oldDevice.getBrand().equals(newDevice.getBrand())) {
			ret.setBrand(newDevice.getBrand());
		}

		if (oldDevice == null || !oldDevice.getCpu().equals(newDevice.getCpu())) {
			ret.setCpu(newDevice.getCpu());
		}

		if (oldDevice == null || !oldDevice.getDevice().equals(newDevice.getDevice())) {
			ret.setDevice(newDevice.getDevice());
		}

		if (oldDevice == null || !oldDevice.getCarrier().equals(newDevice.getCarrier())) {
			ret.setCarrier(newDevice.getCarrier());
		}

		if (oldDevice == null || !oldDevice.getCurrentCarrier().equals(newDevice.getCurrentCarrier())) {
			ret.setCurrentCarrier(newDevice.getCurrentCarrier());
		}

		if (oldDevice == null || !oldDevice.getNetworkType().equals(newDevice.getNetworkType())) {
			ret.setNetworkType(newDevice.getNetworkType());
		}

		if (oldDevice == null || !oldDevice.getBuildType().equals(newDevice.getBuildType())) {
			ret.setBuildType(newDevice.getBuildType());
		}

		if (oldDevice == null || !oldDevice.getBuildId().equals(newDevice.getBuildId())) {
			ret.setBuildId(newDevice.getBuildId());
		}

		if (oldDevice == null || !oldDevice.getBootloaderVersion().equals(newDevice.getBootloaderVersion())) {
			ret.setBootloaderVersion(newDevice.getBootloaderVersion());
		}

		if (oldDevice == null || !oldDevice.getRadioVersion().equals(newDevice.getRadioVersion())) {
			ret.setRadioVersion(newDevice.getRadioVersion());
		}

		if (oldDevice == null || !oldDevice.getCustomData().equals(newDevice.getCustomData())) {
			ret.setCustomData(newDevice.getCustomData().toJson());
		}

		if (oldDevice == null || !oldDevice.getLocaleCountryCode().equals(newDevice.getLocaleCountryCode())) {
			ret.setLocaleCountryCode(newDevice.getLocaleCountryCode());
		}

		if (oldDevice == null || !oldDevice.getLocaleLanguageCode().equals(newDevice.getLocaleLanguageCode())) {
			ret.setLocaleLanguageCode(newDevice.getLocaleLanguageCode());
		}

		if (oldDevice == null || !oldDevice.getLocaleRaw().equals(newDevice.getLocaleRaw())) {
			ret.setLocaleRaw(newDevice.getLocaleRaw());
		}

		if (oldDevice == null || !oldDevice.getUtcOffset().equals(newDevice.getUtcOffset())) {
			ret.setUtcOffset(newDevice.getUtcOffset());
		}

		if (oldDevice == null || !oldDevice.getIntegrationConfig().equals(newDevice.getIntegrationConfig())) {
			ret.setIntegrationConfig(newDevice.getIntegrationConfig().toJson());
		}
		return ret;
	}
}
