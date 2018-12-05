/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * @author Sky Kelsey
 */
// TODO: get rid of JSONObject
public class Configuration extends JSONObject {
	private static final String KEY_METRICS_ENABLED = "metrics_enabled";
	private static final String KEY_COLLECT_AD_ID = "collect_ad_id";
	private static final String KEY_APP_DISPLAY_NAME = "app_display_name";
	private static final String KEY_MESSAGE_CENTER = "message_center";
	private static final String KEY_MESSAGE_CENTER_FG_POLL = "fg_poll";
	private static final String KEY_MESSAGE_CENTER_BG_POLL = "bg_poll";
	private static final String KEY_MESSAGE_CENTER_ENABLED = "message_center_enabled";
	private static final String KEY_MESSAGE_CENTER_NOTIFICATION_POPUP = "notification_popup";
	private static final String KEY_MESSAGE_CENTER_NOTIFICATION_POPUP_ENABLED = "enabled";
	private static final String KEY_COLLECT_APPTIMIZE_DATA = "apptimize_integration";

	private static final String KEY_HIDE_BRANDING = "hide_branding";

	// This one is not sent in JSON, but as a header form the server.
	private static final String KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS = "configuration_cache_expiration_millis";

	// Store the last configuration object to avoid json parsing and disk IO
	private static Configuration cachedConfiguration;

	public Configuration() {
		super();
	}

	public Configuration(String json) throws JSONException {
		super(json);
	}

	public void save() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_APP_CONFIG_JSON, toString()).apply();
		cachedConfiguration = this;
	}

	public static Configuration load() {
		if (cachedConfiguration == null) {
			SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
			cachedConfiguration = Configuration.load(prefs);
		}

		return cachedConfiguration;
	}

	public static Configuration load(SharedPreferences prefs) {
		String json = prefs.getString(Constants.PREF_KEY_APP_CONFIG_JSON, null);
		try {
			if (json != null) {
				return new Configuration(json);
			}
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Error loading Configuration from SharedPreferences.");
			logException(e);
		}
		return new Configuration();
	}

	public boolean isMetricsEnabled() {
		return getBoolean(KEY_METRICS_ENABLED, true);
	}

	public boolean isCollectingAdID() {
		return getBoolean(KEY_COLLECT_AD_ID, false);
	}

	public String getAppDisplayName() {
		try {
			if (!isNull(KEY_APP_DISPLAY_NAME)) {
				return getString(KEY_APP_DISPLAY_NAME);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return ApptentiveInternal.getInstance().getDefaultAppDisplayName();
	}

	private JSONObject getMessageCenter() {
		try {
			if (!isNull(KEY_MESSAGE_CENTER)) {
				return getJSONObject(KEY_MESSAGE_CENTER);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public int getMessageCenterFgPoll() {
		try {
			JSONObject messageCenter = getMessageCenter();
			if (messageCenter != null) {
				if (!messageCenter.isNull(KEY_MESSAGE_CENTER_FG_POLL)) {
					return messageCenter.getInt(KEY_MESSAGE_CENTER_FG_POLL);
				}
			}
		} catch (JSONException e) {
			logException(e);
		}
		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_FG_POLL_SECONDS;
	}

	public int getMessageCenterBgPoll() {
		try {
			JSONObject messageCenter = getMessageCenter();
			if (messageCenter != null) {
				if (!messageCenter.isNull(KEY_MESSAGE_CENTER_BG_POLL)) {
					return messageCenter.getInt(KEY_MESSAGE_CENTER_BG_POLL);
				}
			}
		} catch (JSONException e) {
			logException(e);
		}
		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_BG_POLL_SECONDS;
	}

	public boolean isMessageCenterEnabled() {
		return getBoolean(KEY_MESSAGE_CENTER_ENABLED, Constants.CONFIG_DEFAULT_MESSAGE_CENTER_ENABLED);
	}

	public boolean isMessageCenterNotificationPopupEnabled() {
		JSONObject messageCenter = getMessageCenter();
		if (messageCenter != null) {
			if (!messageCenter.isNull(KEY_MESSAGE_CENTER_NOTIFICATION_POPUP)) {
				JSONObject notificationPopup = messageCenter.optJSONObject(KEY_MESSAGE_CENTER_NOTIFICATION_POPUP);
				if (notificationPopup != null) {
					return notificationPopup.optBoolean(KEY_MESSAGE_CENTER_NOTIFICATION_POPUP_ENABLED, Constants.CONFIG_DEFAULT_MESSAGE_CENTER_NOTIFICATION_POPUP_ENABLED);
				}
			}
		}
		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_NOTIFICATION_POPUP_ENABLED;
	}

	public boolean isCollectingApptimizeData() {
		return optBoolean(KEY_COLLECT_APPTIMIZE_DATA, false);
	}

	public boolean isHideBranding(Context context) {
		try {
			if (!isNull(KEY_HIDE_BRANDING)) {
				return getBoolean(KEY_HIDE_BRANDING);
			}
		} catch (JSONException e) {
			logException(e);
		}

		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = ai.metaData;
			return metaData.getBoolean(Constants.MANIFEST_KEY_INITIALLY_HIDE_BRANDING, Constants.CONFIG_DEFAULT_HIDE_BRANDING);
		} catch (Exception e) {
			ApptentiveLog.w(CONVERSATION, e, "Unexpected error while reading %s manifest setting.", Constants.MANIFEST_KEY_INITIALLY_HIDE_BRANDING);
			logException(e);
		}

		return Constants.CONFIG_DEFAULT_HIDE_BRANDING;
	}

	public long getConfigurationCacheExpirationMillis() {
		try {
			if (!isNull(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS)) {
				return getLong(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_MILLIS;
	}

	public void setConfigurationCacheExpirationMillis(long configurationCacheExpirationMillis) {
		try {
			put(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS, configurationCacheExpirationMillis);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Configuration.", KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS);
			logException(e);
		}
	}

	public boolean hasConfigurationCacheExpired() {
		return getConfigurationCacheExpirationMillis() < System.currentTimeMillis();
	}

	//region Helpers

	private boolean getBoolean(String key, boolean defaultValue) {
		try {
			return optBoolean(key, defaultValue);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while getting boolean key '%s'", key);
			logException(e);
			return defaultValue;
		}
	}

	private static void logException(Exception e) {
		ErrorMetrics.logException(e);
	}

	//endregion
}