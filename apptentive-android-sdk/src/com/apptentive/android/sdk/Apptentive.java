/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.NetworkStateListener;
import com.apptentive.android.sdk.comm.NetworkStateReceiver;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.ActivityUtil;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.Date;
import java.util.HashMap;

/**
 * The Apptentive class is responsible for general initialization, and access to each Apptentive Module.
 *
 * @author Sky Kelsey
 */
public class Apptentive {

	private static Context appContext = null;

	private Apptentive() {
	}

	/**
	 * Initializes Apptentive.
	 *
	 * @param activity The Activity from which this method is called.
	 * @param savedInstanceState
	 */
	public static void onCreate(Activity activity, Bundle savedInstanceState) {
		appContext = activity.getApplicationContext();
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

		init();

		boolean activityIsActive = isActivityActive(activity);

		// If the activity is marked active, this means the app was stopped improperly last time (crash, low mem, or killed)
		// Pretend it just started, since the last session has already ended and that's the best we can do.
		if(activityIsActive) {
			Log.i("App was not shutdown properly last time due to crash, force stop, or low memory condition.");
			clearActiveActivities();
		}

		if(countActiveActivities() == 0) {
			MetricModule.sendMetric(MetricModule.Event.app__launch);
			prefs.edit().putBoolean(Constants.PREF_KEY_APP_IN_BACKGROUND, true).commit(); // Prime the pipe
		}
		markActivityActive(activity);
	}


	/**
	 * @param activity The Activity from which this method is called.
	 */
	public static void onStart(Activity activity) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		// The first run of this method will be when the main Activity is launched.
		boolean comingFromBackground = prefs.getBoolean(Constants.PREF_KEY_APP_IN_BACKGROUND, true);
		if(comingFromBackground) {
			Apptentive.beginSession();
			prefs.edit().putBoolean(Constants.PREF_KEY_APP_IN_BACKGROUND, false).commit();
			Apptentive.asyncFetchAppConfiguration();
		}
	}

	/**
	 * Reserved for future use.
	 * @param activity The Activity from which this method is called.
	 */
	public static void onResume(Activity activity) {
	}

	/**
	 * Reserved for future use.
	 * @param activity The Activity from which this method is called.
	 * @param hasFocus true if the activity is coming into focus, else false.
	 */
	public static void onWindowFocusChanged(Activity activity, boolean hasFocus) {
	}

	/**
	 * Reserved for future use.
	 * @param activity The Activity this method is called from.
	 */
	public static void onPause(Activity activity) {
	}

	/**
	 * @param activity The Activity from which this method is called.
	 */
	public static void onStop(Activity activity) {
		if(ActivityUtil.isApplicationBroughtToBackground(activity)) {
			SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			prefs.edit().putBoolean(Constants.PREF_KEY_APP_IN_BACKGROUND, true).commit();
			Apptentive.endSession();
		}
	}

	/**
	 * @param activity The Activity from which this method is called.
	 */
	public static void onDestroy(Activity activity) {
		markActivityInactive(activity);
		if(countActiveActivities() == 0) {
			MetricModule.sendMetric(MetricModule.Event.app__exit);
			Apptentive.endSession();
		}
	}

	/**
	 * Multiple calls to beginSession within a short period of time are NOPs.
	 * Calls to beginSession when a session is running result in a new session.
	 */
	private static void beginSession() {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean activeSession = prefs.getBoolean(Constants.PREF_KEY_APP_ACTIVE_SESSION, false);
		if(!activeSession) {
			Log.d("Starting session.");
			MetricModule.sendMetric(MetricModule.Event.app__session_start);
			RatingModule.getInstance().logUse();
		} else {
			// TODO: Log an error. This was either a crash, or a dev missing the onStop call.
			Log.w("Starting session, but a session is already active.");
		}
		prefs.edit().putBoolean(Constants.PREF_KEY_APP_ACTIVE_SESSION, true).commit();
	}

	/**
	 * If a session is active, will end it. If a session is not active, is a NOP.
	 */
	private static void endSession() {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean activeSession = prefs.getBoolean(Constants.PREF_KEY_APP_ACTIVE_SESSION, false);
		if(activeSession) {
			Log.d("Ending Session.");
			MetricModule.sendMetric(MetricModule.Event.app__session_end);
		} else {
			// This should never happen...
			Log.w("Ending session, but no session is active.");
		}
		prefs.edit().putBoolean(Constants.PREF_KEY_APP_ACTIVE_SESSION, false).commit();
	}

	private static int countActiveActivities() {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String active = prefs.getString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, "");
		String[] names = active.split(";");
		int count = 0;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if(!name.equals("")) {
				count++;
			}
		}
		return count;
	}

	private static boolean isActivityActive(Activity activity) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String active = prefs.getString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, "");
		String activityName = activity.getComponentName().getClassName();
		String[] names = active.split(";");
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if(name.equals(activityName)) {
				return true;
			}
		}
		return false;
	}

	private static void markActivityActive(Activity activity) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String active = prefs.getString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, "");
		String activityName = activity.getComponentName().getClassName();
		active = active + activityName + ";";
		prefs.edit().putString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, active).commit();
	}

	private static void markActivityInactive(Activity activity) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String active = prefs.getString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, "");
		String activityName = activity.getComponentName().getClassName();
		String[] names = active.split(";");
		active = "";
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if(name.equals(activityName) || name.equals("")) {
				continue;
			}
			active = active + name + ";";
		}
		prefs.edit().putString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, active).commit();
	}

	private static void clearActiveActivities() {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_APP_ACTIVE_ACTIVITY_NAMES, "").commit();
	}

	private static void init() {
		if(GlobalInfo.initialized) {
			Log.v("Already initialized...");
			return;
		}
		Log.v("Initializing...");

		NetworkStateReceiver.clearListeners();

		GlobalInfo.isAppDebuggable = false;

		// First, Get the api key, and figure out if app is debuggable.
		String apiKey = null;
		try {
			ApplicationInfo ai = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
			if(ai != null && ai.metaData != null && ai.metaData.containsKey(Constants.MANIFEST_KEY_APPTENTIVE_API_KEY)) {
				apiKey = ai.metaData.getString(Constants.MANIFEST_KEY_APPTENTIVE_API_KEY);
			}
			if(ai != null && ((ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)) {
				GlobalInfo.isAppDebuggable = true;
			}
		} catch(Exception e) {
			Log.e("Unexpected error while reading application info.", e);
		}

		// If we are in debug mode, but no api key is found, throw an exception. Otherwise, just assert log. We don't want to crash a production app.
		String errorString = "No Apptentive api key specified. Please make sure you have specified your api key in your AndroidManifest.xml";
		if((apiKey == null || apiKey.equals(""))) {
			if(GlobalInfo.isAppDebuggable) {
				throw new RuntimeException(errorString);
			} else {
				Log.e(errorString);
			}
		}
		GlobalInfo.apiKey = apiKey;

		// Grab device info.
		TelephonyManager tm = ((TelephonyManager) (appContext.getSystemService(Context.TELEPHONY_SERVICE)));
		GlobalInfo.carrier = tm.getSimOperatorName();
		GlobalInfo.currentCarrier = tm.getNetworkOperatorName();
		GlobalInfo.networkType = tm.getNetworkType();
		GlobalInfo.appPackage = appContext.getPackageName();
		GlobalInfo.androidId = Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		GlobalInfo.userEmail = Util.getUserEmail(appContext);

		PayloadManager.getInstance().setContext(appContext);
		PayloadManager.getInstance().start();

		// Initialize modules.
		RatingModule.getInstance().setContext(appContext);
		FeedbackModule.getInstance().setContext(appContext);

		MetricModule.setContext(appContext);

		// Listen for network state changes.
		NetworkStateListener networkStateListener = new NetworkStateListener() {
			public void stateChanged(NetworkInfo networkInfo) {
				if(networkInfo.getState() == NetworkInfo.State.CONNECTED){
					Log.v("Network connected.");
					PayloadManager.getInstance().ensureRunning();
				}
				if(networkInfo.getState() == NetworkInfo.State.DISCONNECTED){
					Log.v("Network disconnected.");
				}
			}
		};
		NetworkStateReceiver.addListener(networkStateListener);

		// Check the host app version, and notify modules if it's changed.
		try {
			PackageManager packageManager = appContext.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(appContext.getPackageName(), 0);
			int currentVersionCode = packageInfo.versionCode;
			SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			if(prefs.contains(Constants.PREF_KEY_APP_VERSION_CODE)) {
				int previousVersionCode = prefs.getInt(Constants.PREF_KEY_APP_VERSION_CODE, 0);
				if(previousVersionCode != currentVersionCode) {
					onVersionChanged(previousVersionCode, currentVersionCode);
				}
			}
			prefs.edit().putInt(Constants.PREF_KEY_APP_VERSION_CODE, currentVersionCode).commit();

			GlobalInfo.appDisplayName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageInfo.packageName, 0)).toString();
		} catch(PackageManager.NameNotFoundException e) {
			// Nothing we can do then.
			GlobalInfo.appDisplayName = "this app";
		}

		GlobalInfo.initialized = true;
		Log.v("Done initializing...");
	}

	private static void onVersionChanged(int previousVersion, int currentVersion) {
		RatingModule.getInstance().onAppVersionChanged();
	}

	/**
	 * Fetches the app configuration from the server and stores the keys into our SharedPreferences.
	 * @param force If true, will always fetch configuration. If false, only fetches configuration if the cached
	 *              configuration has expired.
	 */
	private static void fetchAppConfiguration(boolean force) {
		SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

		// Don't get the app configuration unless forced, or the cache has expired.
		if(!force) {
			String iso8601DateString = prefs.getString(Constants.PREF_KEY_APP_CONFIG_PREFIX + "cache-expiration", null);
			if(iso8601DateString != null) {
				Date expiration = Util.parseIso8601Date(iso8601DateString);
				boolean expired = new Date().getTime() > expiration.getTime();
				if(!expired) {
					Log.v("Using cached configuration.");
					return;
				}
			}
		}

		Log.v("Fetching new configuration.");
		ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
		HashMap<String, Object> config = client.getAppConfiguration(GlobalInfo.androidId);
		Log.v("App configuration: " + config.toString());
		for (String key : config.keySet()) {
			Object value = config.get(key);
			Log.v("- %s = %s", key, value);
			if (value instanceof Integer) {
				prefs.edit().putInt(Constants.PREF_KEY_APP_CONFIG_PREFIX + key, (Integer) value).commit();
			} else if (value instanceof String) {
				prefs.edit().putString(Constants.PREF_KEY_APP_CONFIG_PREFIX + key, (String) value).commit();
			} else if (value instanceof Boolean) {
				prefs.edit().putBoolean(Constants.PREF_KEY_APP_CONFIG_PREFIX + key, (Boolean) value).commit();
			} else if (value instanceof Long) {
				prefs.edit().putLong(Constants.PREF_KEY_APP_CONFIG_PREFIX + key, (Long) value).commit();
			} else if (value instanceof Float) {
				prefs.edit().putFloat(Constants.PREF_KEY_APP_CONFIG_PREFIX + key, (Float) value).commit();
			}
		}
	}

	private static void asyncFetchAppConfiguration() {
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... objects) {
				fetchAppConfiguration(GlobalInfo.isAppDebuggable);
				return null;
			}
		}.execute();
	}

	/**
	 * Sets the user email address. This address will be used in the feedback module, or elsewhere where needed.
	 * This method will override the email address that Apptentive looks for programmatically, but will not override
	 * an email address that the user has previously entered in an Apptentive dialog.
	 *
	 * @param email The user's email address.
	 */
	public static void setUserEmail(String email) {
		GlobalInfo.userEmail = email;
	}


	/**
	 * Gets the Apptentive Rating Module.
	 *
	 * @return The Apptentive Rating Module.
	 */
	public static RatingModule getRatingModule() {
		return RatingModule.getInstance();
	}

	/**
	 * Gets the Apptentive Feedback Module.
	 *
	 * @return The Apptentive Feedback Module.
	 */
	public static FeedbackModule getFeedbackModule() {
		return FeedbackModule.getInstance();
	}

	/**
	 * Gets the Apptentive Survey Module.
	 *
	 * @return The Apptentive Survey Module.
	 */
	public static SurveyModule getSurveyModule() {
		return SurveyModule.getInstance();
	}
}