/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.util.AdvertiserManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.Locale;
import java.util.TimeZone;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class DeviceManager {
	private final String androidID;

	public DeviceManager(String androidID) {
		if (StringUtils.isNullOrEmpty(androidID)) {
			throw new IllegalArgumentException("Android ID is null or empty");
		}
		this.androidID = androidID;
	}

	public Device generateNewDevice(Context context) {
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
		device.setUuid(androidID);
		device.setBuildType(Build.TYPE);
		device.setBuildId(Build.ID);

		// advertiser id
		try {
			Configuration configuration = Configuration.load();
			if (configuration.isCollectingAdID()) {
				AdvertiserManager.AdvertisingIdClientInfo info = AdvertiserManager.getAdvertisingIdClientInfo();
				if (info != null && !info.isLimitAdTrackingEnabled()) {
					device.setAdvertiserId(info.getId());
				} else {
					ApptentiveLog.w("Advertising ID tracking is not available or limited");
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while collecting advertising ID");
			logException(e);
		}

		// Second, set the stuff that requires querying system services.
		TelephonyManager tm = ((TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE)));
		device.setCarrier(tm.getSimOperatorName());
		device.setCurrentCarrier(tm.getNetworkOperatorName());
		device.setNetworkType(Constants.networkTypeAsString(tm.getNetworkType()));


		try {
			device.setBootloaderVersion((String) Build.class.getField("BOOTLOADER").get(null));
		} catch (Exception e) {
			logException(e);
		}
		device.setRadioVersion(Build.getRadioVersion());


		device.setLocaleCountryCode(Locale.getDefault().getCountry());
		device.setLocaleLanguageCode(Locale.getDefault().getLanguage());
		device.setLocaleRaw(Locale.getDefault().toString());
		device.setUtcOffset(String.valueOf((TimeZone.getDefault().getRawOffset() / 1000)));
		return device;
	}
}

