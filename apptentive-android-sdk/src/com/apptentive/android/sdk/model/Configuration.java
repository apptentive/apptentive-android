/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Configuration extends JSONObject {
	private static final String KEY_METRICS_ENABLED = "metrics_enabled";
	private static final String KEY_APP_DISPLAY_NAME = "app_display_name";
	private static final String KEY_MESSAGE_CENTER = "message_center";
	private static final String KEY_MESSAGE_CENTER_TITLE = "title";
	private static final String KEY_MESSAGE_CENTER_FG_POLL = "fg_poll";
	private static final String KEY_MESSAGE_CENTER_BG_POLL = "bg_poll";
	private static final String KEY_MESSAGE_CENTER_ENABLED = "message_center_enabled";
	private static final String KEY_MESSAGE_CENTER_EMAIL_REQUIRED = "email_required";
	private static final String KEY_HIDE_BRANDING = "hide_apptentive_branding";

	// This one is not sent in JSON, but as a header form the server.
	private static final String KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS = "configuration_cache_expiration_millis";


	public Configuration() {
		super();
	}

	public Configuration(String json) throws JSONException {
		super(json);
	}

	public void save(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_APP_CONFIG_JSON, toString()).commit();
	}

	public static Configuration load(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return Configuration.load(prefs);
	}

	public static Configuration load(SharedPreferences prefs) {
		String json = prefs.getString(Constants.PREF_KEY_APP_CONFIG_JSON, null);
		try {
			if (json != null) {
				return new Configuration(json);
			}
		} catch (JSONException e) {
			Log.e("Error loading Configuration from SharedPreferences.", e);
		}
		return new Configuration();
	}

	public boolean isMetricsEnabled() {
		try {
			if (!isNull(KEY_METRICS_ENABLED)) {
				return getBoolean(KEY_METRICS_ENABLED);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return true;
	}

	public String getAppDisplayName() {
		try {
			if (!isNull(KEY_APP_DISPLAY_NAME)) {
				return getString(KEY_APP_DISPLAY_NAME);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return GlobalInfo.appDisplayName;
	}

	private JSONObject getMessageCenter() {
		try {
			if (!isNull(KEY_MESSAGE_CENTER)) {
				return getJSONObject(KEY_MESSAGE_CENTER);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public String getMessageCenterTitle() {
		try {
			JSONObject messageCenter = getMessageCenter();
			if (messageCenter != null) {
				if (!messageCenter.isNull(KEY_MESSAGE_CENTER_TITLE)) {
					return messageCenter.getString(KEY_MESSAGE_CENTER_TITLE);
				}
			}
		} catch (JSONException e) {
			// Ignore
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
			// Ignore
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
			// Ignore
		}
		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_BG_POLL_SECONDS;
	}

	public boolean isMessageCenterEnabled(Context context) {
		try {
			if (!isNull(KEY_MESSAGE_CENTER_ENABLED)) {
				return getBoolean(KEY_MESSAGE_CENTER_ENABLED);
			}
		} catch (JSONException e) {
			// Move on.
		}

		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = ai.metaData;
			return metaData.getBoolean(Constants.MANIFEST_KEY_MESSAGE_CENTER_ENABLED, Constants.CONFIG_DEFAULT_MESSAGE_CENTER_ENABLED);
		} catch (Exception e) {
			Log.w("Unexpected error while reading default %s manifest setting.", e, Constants.MANIFEST_KEY_MESSAGE_CENTER_ENABLED);
		}

		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_ENABLED;
	}

	public boolean isMessageCenterEmailRequired(Context context) {
		try {
			JSONObject messageCenter = getMessageCenter();
			if (messageCenter != null) {
				if (!messageCenter.isNull(KEY_MESSAGE_CENTER_EMAIL_REQUIRED)) {
					return messageCenter.getBoolean(KEY_MESSAGE_CENTER_EMAIL_REQUIRED);
				}
			}
		} catch (JSONException e) {
			// Move on.
		}

		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = ai.metaData;
			return metaData.getBoolean(Constants.MANIFEST_KEY_EMAIL_REQUIRED, Constants.CONFIG_DEFAULT_MESSAGE_CENTER_EMAIL_REQUIRED);
		} catch (Exception e) {
			Log.w("Unexpected error while reading %s manifest setting.", e, Constants.MANIFEST_KEY_EMAIL_REQUIRED);
		}

		return Constants.CONFIG_DEFAULT_MESSAGE_CENTER_EMAIL_REQUIRED;
	}

	public boolean isHideBranding(Context context) {
		try {
			JSONObject messageCenter = getMessageCenter();
			if (messageCenter != null) {
				if (!messageCenter.isNull(KEY_HIDE_BRANDING)) {
					return messageCenter.getBoolean(KEY_HIDE_BRANDING);
				}
			}
		} catch (JSONException e) {
			// Move on.
		}

		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle metaData = ai.metaData;
			return metaData.getBoolean(Constants.MANIFEST_KEY_HIDE_BRANDING, Constants.CONFIG_DEFAULT_HIDE_BRANDING);
		} catch (Exception e) {
			Log.w("Unexpected error while reading %s manifest setting.", e, Constants.MANIFEST_KEY_HIDE_BRANDING);
		}

		return Constants.CONFIG_DEFAULT_HIDE_BRANDING;
	}

	public long getConfigurationCacheExpirationMillis() {
		try {
			if (!isNull(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS)) {
				return getLong(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_MILLIS;
	}

	public void setConfigurationCacheExpirationMillis(long configurationCacheExpirationMillis) {
		try {
			put(KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS, configurationCacheExpirationMillis);
		} catch (JSONException e) {
			Log.w("Error adding %s to Configuration.", KEY_CONFIGURATION_CACHE_EXPIRATION_MILLIS);
		}
	}
}