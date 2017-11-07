/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Destroyable;
import com.apptentive.android.sdk.util.Jwt;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_LOG_MONITOR_STARTED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_LOG_MONITOR_STOPPED;
import static com.apptentive.android.sdk.comm.ApptentiveHttpClient.USER_AGENT_STRING;

public class LogMonitor implements Destroyable {

	// TODO: Replace with a better unique number
	public static final int NOTIFICATION_ID = 1;

	private static final String TAG = "LogMonitor";

	private static final String PREFS_NAME = "com.apptentive.debug";
	private static final String PREFS_KEY_EMAIL_RECIPIENTS = "com.apptentive.debug.EmailRecipients";
	private static final String PREFS_KEY_LOG_LEVEL = "com.apptentive.debug.LogLevel";
	private static final String PREFS_KEY_FILTER_PID = "com.apptentive.debug.FilterPID";

	private static final String DEBUG_TEXT_HEADER = "com.apptentive.debug:";

	private static LogMonitor instance;

	private final WeakReference<Context> contextRef;
	private final ApptentiveLog.Level logLevel;
	private final String[] emailRecipients;

	private final LogWriter logWriter;

	private ApptentiveLog.Level oldLogLevel;

	private LogMonitor(Context context, Configuration configuration) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		if (configuration == null) {
			throw new IllegalArgumentException("Configuration is null");
		}

		this.contextRef = new WeakReference<>(context);
		this.logLevel = configuration.logLevel;
		this.emailRecipients = configuration.emailRecipients;
		this.logWriter = new LogWriter(getLogFile(context), configuration.restored, configuration.filterByPID);
	}

	//region Initialization

	/**
	 * Attempts to initialize an instance. Returns <code>true</code> if succeed.
	 */
	public static boolean tryInitialize(Context context, String apptentiveApiKey, String apptentiveApiSignature) {
		if (instance != null) {
			ApptentiveLog.i("Log Monitor already initialized");
			return false;
		}

		try {
			Configuration configuration = readConfigurationFromPersistentStorage(context);
			if (configuration != null) {
				ApptentiveLog.i("Read log monitor configuration from persistent storage: " + configuration);
			} else {
				String accessToken = readAccessTokenFromClipboard(context);

				// No access token was supplied
				if (accessToken == null) {
					return false;
				}

				// The access token was invalid, or expired, or the server could be reached to verify it
				if (!syncVerifyAccessToken(apptentiveApiKey, apptentiveApiSignature, accessToken)) {
					ApptentiveLog.i("Can't start log monitor: access token verification failed");
					return false;
				}

				configuration = readConfigurationFromToken(accessToken);
				if (configuration != null) {
					ApptentiveLog.i("Read log monitor configuration from clipboard: " + configuration);
					Util.setClipboardText(context, ""); // clear the clipboard contents after the data is parsed

					// store the configuration to make sure we can resume the current session
					saveConfigurationFromPersistentStorage(context, configuration);
				}
			}

			if (configuration != null) {
				ApptentiveLog.i("Entering Apptentive Troubleshooting mode.");
				instance = new LogMonitor(context, configuration);
				instance.start(context);
				return true;
			}

		} catch (Exception e) {
			ApptentiveLog.i("Exception while initializing Apptentive Log Monitor", e);
		}

		return false;
	}

	private static Configuration readConfigurationFromToken(String accessToken) {
		try {
			final Jwt jwt = Jwt.decode(accessToken);
			JSONObject payload = jwt.getPayload();

			Configuration config = new Configuration();

			// log level
			String logLevelStr = payload.optString("level");
			if (!StringUtils.isNullOrEmpty(logLevelStr)) {
				config.logLevel = ApptentiveLog.Level.parse(logLevelStr);
			}

			// recipients
			JSONArray recipientsJson = payload.optJSONArray("recipients");
			if (recipientsJson != null) {
				String[] recipients = new String[recipientsJson.length()];
				for (int i = 0; i < recipientsJson.length(); ++i) {
					recipients[i] = recipientsJson.optString(i);
				}

				config.emailRecipients = recipients;
			}

			// should we filter by PID
			config.filterByPID = payload.optBoolean("filter_app_process", config.filterByPID);

			return config;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while parsing access token: '%s'", accessToken);
			return null;
		}
	}

	/**
	 * Attempts to read a log monitor configuration stored in the last session. Returns <code>null</code> if failed
	 */
	private static Configuration readConfigurationFromPersistentStorage(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		SharedPreferences prefs = getPrefs(context);
		if (!prefs.contains(PREFS_KEY_EMAIL_RECIPIENTS)) {
			return null;
		}

		Configuration configuration = new Configuration();
		configuration.restored = true;

		String emailRecipients = prefs.getString(PREFS_KEY_EMAIL_RECIPIENTS, null);
		if (!StringUtils.isNullOrEmpty(emailRecipients)) {
			configuration.emailRecipients = emailRecipients.split(",");
		}

		String logLevel = prefs.getString(PREFS_KEY_LOG_LEVEL, null);
		if (!StringUtils.isNullOrEmpty(logLevel)) {
			configuration.logLevel = ApptentiveLog.Level.parse(logLevel);
		}

		configuration.filterByPID = prefs.getBoolean(PREFS_KEY_FILTER_PID, configuration.filterByPID);

		return configuration;
	}

	/** Saves the configuration into SharedPreferences */
	private static void saveConfigurationFromPersistentStorage(Context context, Configuration configuration) {
		SharedPreferences prefs = getPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_KEY_EMAIL_RECIPIENTS, StringUtils.join(configuration.emailRecipients));
		editor.putString(PREFS_KEY_LOG_LEVEL, configuration.logLevel.toString());
		editor.putBoolean(PREFS_KEY_FILTER_PID, configuration.filterByPID);
		editor.apply();
	}

	private static void deleteConfigurationFromPersistentStorage(Context context) {
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.remove(PREFS_KEY_EMAIL_RECIPIENTS);
		editor.remove(PREFS_KEY_LOG_LEVEL);
		editor.remove(PREFS_KEY_FILTER_PID);
		editor.apply();
	}

	//endregion

	//region Access Token

	/**
	 * Attempts to read access token from the clipboard
	 */
	private static String readAccessTokenFromClipboard(Context context) {
		String text = Util.getClipboardText(context);

		if (StringUtils.isNullOrEmpty(text)) {
			return null;
		}

		//Since the token string should never contain spaces, attempt to repair line breaks introduced in the copying process.
		text = text.replaceAll("\\s+", "");

		if (!text.startsWith(DEBUG_TEXT_HEADER)) {
			return null;
		}

		// Remove the header
		text = text.substring(DEBUG_TEXT_HEADER.length());
		return text;
	}

	/**
	 * Send a sync URL request to the backend to verify the access token. This method would block
	 */
	private static boolean syncVerifyAccessToken(String apptentiveApiKey, String apptentiveApiSignature, String accessToken) {
		return verifyToken(apptentiveApiKey, apptentiveApiSignature, accessToken);
	}

	//endregion

	//region Lifecycle

	private void start(Context context) {
		Log.i(TAG, "Overriding log level: " + logLevel);
		oldLogLevel = ApptentiveLog.getLogLevel();
		ApptentiveLog.overrideLogLevel(logLevel);

		showDebugNotification(context);

		logWriter.start();

		// post a notification
		ApptentiveNotificationCenter.defaultCenter()
				.postNotification(NOTIFICATION_LOG_MONITOR_STARTED);
	}

	public void stopWritingLogs() {
		try {
			logWriter.stopAndWait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// post a notification
		ApptentiveNotificationCenter.defaultCenter()
				.postNotification(NOTIFICATION_LOG_MONITOR_STOPPED);
	}

	private void showDebugNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new TroubleshootingNotification().buildNotification(context, getSubject(context), getSystemInfo(context), getLogFile(context), getManifestFile(context), emailRecipients);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private String getSubject(Context context) {
		String subject = String.format("%s (Android)", context.getPackageName());
		try {
			ApplicationInfo ai = context.getApplicationInfo();
			subject = String.format("%s (Android)", ai.loadLabel(context.getPackageManager()).toString());
		} catch (Exception e) {
			ApptentiveLog.e(e, "Unable to load troubleshooting email status line");
		}
		return subject;
	}

	private String getSystemInfo(Context context) {
		String versionName = "";
		int versionCode = -1;
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			// TODO: list activities, permissions, etc
			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		Object[] info = {
				"App Package Name", context.getPackageName(),
				"App Version Name", versionName,
				"App Version Code", versionCode,
				"Apptentive SDK", com.apptentive.android.sdk.util.Constants.APPTENTIVE_SDK_VERSION,
				"Device Model", Build.MODEL,
				"Android OS Version", Build.VERSION.RELEASE,
				"Android OS API Level", Build.VERSION.SDK_INT,
				"Locale", Locale.getDefault().getDisplayName()
		};

		StringBuilder result = new StringBuilder();
		result.append("This email may contain sensitive content. Please review before sending.\n\n");
		for (int i = 0; i < info.length; i += 2) {
			if (result.length() > 0) {
				result.append("\n");
			}
			result.append(info[i]);
			result.append(": ");
			result.append(info[i + 1]);
		}
		return result.toString();
	}

	//endregion

	//region Destroyable

	@Override
	public void destroy() {
		// restoring old log level
		if (oldLogLevel != null) {
			ApptentiveLog.overrideLogLevel(oldLogLevel);
		}

		Context context = getContext();
		if (context != null) {
			// deleting saved session
			deleteConfigurationFromPersistentStorage(context);
			instance = null;
		} else {
			Log.e(TAG, "Unable to destroy session: context is lost");
		}
	}

	//endregion

	//region Helpers

	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	private static File getLogFile(Context context) {
		return new File(context.getCacheDir(), Constants.FILE_APPTENTIVE_LOG_FILE);
	}

	private static File getManifestFile(Context context) {
		return new File(context.getCacheDir(), Constants.FILE_APPTENTIVE_ENGAGEMENT_MANIFEST);
	}

	//endregion

	//region Getters/Setters

	public static LogMonitor sharedInstance() {
		return instance;
	}

	private Context getContext() {
		return contextRef.get();
	}

	//endregion

	//region Configuration

	private static class Configuration {
		/** Email recipients for the log email */
		String[] emailRecipients = { "support@apptentive.com" };

		/** New log level */
		ApptentiveLog.Level logLevel = ApptentiveLog.Level.VERY_VERBOSE;

		/** True if logcat output should be filtered by the process id */
		boolean filterByPID = true;

		/** True if configuration was restored from the persistent storage */
		boolean restored;

		@Override
		public String toString() {
			return String.format("logLevel=%s recipients=%s filterPID=%s restored=%s",
					logLevel, Arrays.toString(emailRecipients), Boolean.toString(filterByPID),
					Boolean.toString(restored));
		}
	}

	//endregion

	//region Token Verification

	private static boolean verifyToken(String apptentiveAppKey, String apptentiveAppSignature, String token) {

		final Map<String, String> headers = new HashMap<>();
		headers.put("X-API-Version", String.valueOf(Constants.API_VERSION));
		headers.put("APPTENTIVE-KEY", apptentiveAppKey);
		headers.put("APPTENTIVE-SIGNATURE", apptentiveAppSignature);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("User-Agent", String.format(USER_AGENT_STRING, Constants.APPTENTIVE_SDK_VERSION));

		JSONObject postBodyJson;
		try {
			postBodyJson = new JSONObject();
			postBodyJson.put("debug_token", token);
		} catch (JSONException e) {
			// Can't happen
			throw new RuntimeException(e);
		}

		String response = loadFromURL(Constants.CONFIG_DEFAULT_SERVER_URL + "/debug_token/verify", headers, postBodyJson.toString());
		if (!StringUtils.isNullOrEmpty(response)) {
			try {
				JSONObject debugTokenResponse = new JSONObject(response);
				if (!debugTokenResponse.isNull("valid")) {
					return debugTokenResponse.optBoolean("valid", false);
				}
				ApptentiveLog.e("Debug token response was missing \"valid\" field.");
			} catch (Exception e) {
				ApptentiveLog.e("Error parsing debug token validation response.");
				return false;
			}
			return true;
		}
		return false;
	}

	private static String loadFromURL(final String urlString, final Map<String, String> headers, final String body) {
		final StringBuilder responseString = new StringBuilder();
		Thread networkThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					loadFromURL(urlString, headers, responseString);
				} catch (Exception e) {
					ApptentiveLog.e("Error performing debug token validation request: %s", e.getMessage());
				}
			}

			private void loadFromURL(String urlString, Map<String, String> headers, StringBuilder responseString) throws IOException {
				ApptentiveLog.i("Performing debug token verification request: \"%s\"", body);
				URL url = new URL(urlString);
				BufferedReader reader = null;
				try {
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setConnectTimeout(ApptentiveHttpClient.DEFAULT_HTTP_CONNECT_TIMEOUT);
					connection.setReadTimeout(ApptentiveHttpClient.DEFAULT_HTTP_SOCKET_TIMEOUT);

					for (String key : headers.keySet()) {
						connection.setRequestProperty(key, headers.get(key));
					}

					OutputStream outputStream = null;
					try {
						outputStream = connection.getOutputStream();
						outputStream.write(body.getBytes());
					} finally {
						Util.ensureClosed(outputStream);
					}

					int responseCode = connection.getResponseCode();
					ApptentiveLog.vv("Response code: %d", responseCode);

					InputStream is;
					StringBuilder buffer;
					boolean successful;
					// If successful, read the message into the response String. If not, read it into a buffer and throw it in an exception.
					if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
						successful = true;
						is = connection.getInputStream();
						buffer = responseString;
					} else {
						successful = false;
						is = connection.getErrorStream();
						buffer = new StringBuilder();
					}

					reader = new BufferedReader(new InputStreamReader(is));
					String line;
					while ((line = reader.readLine()) != null) {
						if (buffer.length() > 0) {
							buffer.append('\n');
						}
						buffer.append(line);
					}
					ApptentiveLog.v("Debug Token verification response: %s", buffer);
					if (!successful) {
						throw new IOException(buffer.toString());
					}
				} finally {
					Util.ensureClosed(reader);
				}
			}
		});
		networkThread.start();
		try {
			networkThread.join();
		} catch (InterruptedException e) {
			ApptentiveLog.e("Debug token validation thread interrupted.");
			return null;
		}
		return responseString.toString();
	}

	//endregion

}
