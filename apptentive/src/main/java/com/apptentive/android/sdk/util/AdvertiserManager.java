/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.content.Context;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;

import static com.apptentive.android.sdk.ApptentiveLogTag.ADVERTISER_ID;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ADVERTISER_ID_DID_RESOLVE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_ADVERTISER_CLIENT_INFO;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_SUCCESSFUL;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class AdvertiserManager {
	private static final String CLASS_ADVERTISING_ID_CLIENT = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
	private static final String METHOD_GET_ADVERTISING_ID_INFO = "getAdvertisingIdInfo";
	private static final String METHOD_GET_ID = "getId";
	private static final String METHOD_IS_LIMIT_AD_TRACKING_ENABLED = "isLimitAdTrackingEnabled";

	private static AdvertisingIdClientInfo cachedClientInfo;

	public static synchronized @Nullable AdvertisingIdClientInfo getAdvertisingIdClientInfo() {
		return cachedClientInfo;
	}

	/**
	 * Returns true if changed
	 */
	public static synchronized boolean updateAdvertisingIdClientInfo(Context context) {
		ApptentiveLog.v(ADVERTISER_ID, "Updating advertiser ID client info...");
		AdvertisingIdClientInfo clientInfo = resolveAdvertisingIdClientInfo(context);
		if (clientInfo != null && clientInfo.equals(cachedClientInfo)) {
			return false; // no changes
		}

		ApptentiveLog.v(ADVERTISER_ID, "Advertiser ID client info changed: %s", clientInfo);
		cachedClientInfo = clientInfo;
		notifyClientInfoChanged(cachedClientInfo);
		return true;
	}

	private static @Nullable AdvertisingIdClientInfo resolveAdvertisingIdClientInfo(Context context) {
		try {
			Invocation advertisingIdClient = Invocation.fromClass(CLASS_ADVERTISING_ID_CLIENT);
			Object infoObject = advertisingIdClient.invokeMethod(METHOD_GET_ADVERTISING_ID_INFO, new Class<?>[] { Context.class }, new Object[] { context });
			if (infoObject == null) {
				ApptentiveLog.w("Unable to resolve advertising ID: '%s' did not return a valid value", METHOD_GET_ADVERTISING_ID_INFO);
				return null;
			}

			Invocation info = Invocation.fromObject(infoObject);
			String id = info.invokeStringMethod(METHOD_GET_ID);
			boolean limitAdTrackingEnabled = info.invokeBooleanMethod(METHOD_IS_LIMIT_AD_TRACKING_ENABLED);
			return new AdvertisingIdClientInfo(id, limitAdTrackingEnabled);
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause != null) {
				if (StringUtils.equal(cause.getClass().getSimpleName(), "GooglePlayServicesNotAvailableException")) {
					ApptentiveLog.e(e, "Unable to resolve advertising ID: Google Play is not installed on this device");
					return null;
				}

				if (StringUtils.equal(cause.getClass().getSimpleName(), "GooglePlayServicesRepairableException")) {
					ApptentiveLog.e(e, "Unable to resolve advertising ID: error connecting to Google Play Services");
					return null;
				}
			}

			ApptentiveLog.e(e, "Unable to resolve advertising ID");
			logException(e);
			return null;
		}
	}

	private static void notifyClientInfoChanged(AdvertisingIdClientInfo clientInfo) {
		ApptentiveNotificationCenter.defaultCenter()
			.postNotification(NOTIFICATION_ADVERTISER_ID_DID_RESOLVE,
				NOTIFICATION_KEY_SUCCESSFUL, clientInfo != null,
				NOTIFICATION_KEY_ADVERTISER_CLIENT_INFO, clientInfo);
	}

	public static class AdvertisingIdClientInfo {
		private final String id;
		private final boolean limitAdTrackingEnabled;

		public AdvertisingIdClientInfo(String id, boolean limitAdTrackingEnabled) {
			this.id = id;
			this.limitAdTrackingEnabled = limitAdTrackingEnabled;
		}

		public String getId() {
			return id;
		}

		public final boolean isLimitAdTrackingEnabled() {
			return limitAdTrackingEnabled;
		}

		@Override
		public String toString() {
			return StringUtils.format("%s: id=%s limited=%b", getClass().getSimpleName(), ApptentiveLog.hideIfSanitized(id), limitAdTrackingEnabled);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AdvertisingIdClientInfo that = (AdvertisingIdClientInfo) o;
			return limitAdTrackingEnabled == that.limitAdTrackingEnabled && StringUtils.equal(id, that.id);
		}
	}
}
