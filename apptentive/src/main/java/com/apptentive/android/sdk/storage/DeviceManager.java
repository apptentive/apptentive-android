/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

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
		try {
			setupTelephony(context, device);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while collecting telephony");
			logException(e);
		}

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

	@SuppressLint("MissingPermission")
	private static void setupTelephony(Context context, Device device) {
		TelephonyManager tm = ((TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE)));
		device.setCarrier(tm.getSimOperatorName());
		device.setCurrentCarrier(tm.getNetworkOperatorName());

		// SDK < 30 can access networkType without any special permissions, but for SDK >= 30
		// READ_PHONE_STATE must be granted by the user as it is a dangerous permission. The library
		// should continue to function if the app doesn't want to request this permission or the user
		// does not want to grant the permission.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			device.setNetworkType(Constants.networkTypeAsString(tm.getNetworkType()));
		} else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			device.setNetworkType(Constants.networkTypeAsString(tm.getDataNetworkType()));
		}
	}
}

