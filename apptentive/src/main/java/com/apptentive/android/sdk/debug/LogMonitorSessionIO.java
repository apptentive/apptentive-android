/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Jwt;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

class LogMonitorSessionIO {
	private static final String PREFS_NAME = "com.apptentive.debug";
	private static final String PREFS_KEY_EMAIL_RECIPIENTS = "com.apptentive.debug.EmailRecipients";
	private static final String PREFS_KEY_FILTER_PID = "com.apptentive.debug.FilterPID";

	/**
	 * Attempts to read an existing session from the persistent storage.
	 * Returns <code>null</code> i
	 */
	static @Nullable LogMonitorSession readCurrentSession(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		SharedPreferences prefs = getPrefs(context);
		if (!prefs.contains(PREFS_KEY_EMAIL_RECIPIENTS)) {
			return null;
		}

		LogMonitorSession session = new LogMonitorSession();
		session.restored = true;

		String emailRecipients = prefs.getString(PREFS_KEY_EMAIL_RECIPIENTS, null);
		if (!StringUtils.isNullOrEmpty(emailRecipients)) {
			session.emailRecipients = emailRecipients.split(",");
		}

		return session;
	}

	/**
	 * Saves current session to the persistent storage
	 */
	static void saveCurrentSession(Context context, LogMonitorSession session) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (session == null) {
			throw new IllegalArgumentException("Session is null");
		}

		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_KEY_EMAIL_RECIPIENTS, StringUtils.join(session.emailRecipients));
		editor.apply();
	}

	/**
	 * Deletes current session from the persistent storage
	 */
	static void deleteCurrentSession(Context context) {
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.remove(PREFS_KEY_EMAIL_RECIPIENTS);
		editor.remove(PREFS_KEY_FILTER_PID);
		editor.apply();
	}

	/**
	 * Reads session from JWT-token.
	 * Returns <code>null</code> if fails.
	 */
	static @Nullable LogMonitorSession readSessionFromJWT(String token) {
		try {
			final Jwt jwt = Jwt.decode(token);
			JSONObject payload = jwt.getPayload();

			LogMonitorSession config = new LogMonitorSession();

			// recipients
			JSONArray recipientsJson = payload.optJSONArray("recipients");
			if (recipientsJson != null) {
				String[] recipients = new String[recipientsJson.length()];
				for (int i = 0; i < recipientsJson.length(); ++i) {
					recipients[i] = recipientsJson.optString(i);
				}

				config.emailRecipients = recipients;
			}

			return config;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while parsing access token: '%s'", token);
			logException(e);
			return null;
		}
	}

	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
}
