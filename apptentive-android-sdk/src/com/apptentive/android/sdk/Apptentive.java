/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.NetworkStateListener;
import com.apptentive.android.sdk.comm.NetworkStateReceiver;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;

import java.util.HashMap;

/**
 * The Apptentive class is responsible for general initialization, and access to each Apptentive Module.
 *
 * @author Sky Kelsey
 */
public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

	private static Apptentive instance = null;
	private Application application = null;

	private Apptentive() {
	}

	/**
	 * Gets the Apptentive singleton instance.
	 *
	 * @return The Apptentive singleton instance.
	 */
	public static Apptentive getInstance() {
		if (instance == null) {
			instance = new Apptentive();
		}
		return instance;
	}

	/**
	 * Initializes the Apptentive Passes your application's Activity to Apptentive so we can initialize.
	 *
	 * @param app    The top level application instance.
	 * @param apiKey The API key. This will be a long base64 token like:<br/>
	 *               <strong>0d7c775a973b30ed6a8cb2cf6469af3168a8c5e38ccd26755d1fdaa3397c6575</strong>
	 */
	public void initialize(Application app, String apiKey) {
		this.application = app;
		GlobalInfo.apiKey = apiKey;

		final Context appContext = application.getApplicationContext();

		NetworkStateReceiver.clearListeners();

		// Retrieve device/app configuration.
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... objects) {
				getAppConfiguration(appContext, GlobalInfo.apiKey);
				return null;
			}
		}.execute();

		GlobalInfo.carrier = ((TelephonyManager) (application.getSystemService(Context.TELEPHONY_SERVICE))).getSimOperatorName();
		GlobalInfo.currentCarrier = ((TelephonyManager) (application.getSystemService(Context.TELEPHONY_SERVICE))).getNetworkOperatorName();
		GlobalInfo.networkType = ((TelephonyManager) (application.getSystemService(Context.TELEPHONY_SERVICE))).getNetworkType();
		GlobalInfo.appPackage = appContext.getPackageName();
		GlobalInfo.androidId = Settings.Secure.getString(application.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		GlobalInfo.userEmail = getUserEmail(appContext);

		PayloadManager.getInstance().setContext(appContext);
		PayloadManager.getInstance().start();

		// Initialize modules.
		RatingModule.getInstance().setContext(appContext);
		FeedbackModule.getInstance().setContext(appContext);

		MetricModule.setContext(appContext);
		MetricModule.sendMetric(MetricModule.Event.app__launch);

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
	}

	/**
	 * Grabs the app configuration from the server and stores the keys into our SharedPreferences.
	 * @param context
	 * @param apiKey
	 */
	private void getAppConfiguration(Context context, String apiKey) {
		ApptentiveClient client = new ApptentiveClient(apiKey);
		HashMap<String, Object> config = client.getAppConfiguration(GlobalInfo.androidId);
		SharedPreferences prefs = context.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE);
		for (String key : config.keySet()) {
			Object value = config.get(key);
			if (value instanceof Integer) {
				prefs.edit().putInt("appConfiguration." + key, (Integer) value).commit();
			} else if (value instanceof String) {
				prefs.edit().putString("appConfiguration." + key, (String) value).commit();
			} else if (value instanceof Boolean) {
				prefs.edit().putBoolean("appConfiguration." + key, (Boolean) value).commit();
			} else if (value instanceof Long) {
				prefs.edit().putLong("appConfiguration." + key, (Long) value).commit();
			} else if (value instanceof Float) {
				prefs.edit().putFloat("appConfiguration." + key, (Float) value).commit();
			}
		}
	}

	/**
	 * Call this from your main Activity's onDestroy() method, so we can clean up.
	 */
	public void onDestroy() {
		MetricModule.sendMetric(MetricModule.Event.app__exit);
	}

	/**
	 * Sets your app's display name.<p/>
	 * Should be something like "My App Name".
	 *
	 * @param name The display name of your app.
	 */
	public void setAppDisplayName(String name) {
		GlobalInfo.appDisplayName = name;
	}

	/**
	 * Sets the user email address. This address will be used in the feedback module, or elsewhere where needed.
	 * This method will override the email address that Apptentive looks for programmatically, but will not override
	 * an email address that the user has previously entered in an Apptentive dialog.
	 *
	 * @param email The user's email address.
	 */
	public void setUserEmail(String email) {
		GlobalInfo.userEmail = email;
	}


	/**
	 * Gets the Apptentive Rating Module.
	 *
	 * @return The Apptentive Rating Module.
	 */
	public RatingModule getRatingModule() {
		return RatingModule.getInstance();
	}

	/**
	 * Gets the Apptentive Feedback Module.
	 *
	 * @return The Apptentive Feedback Module.
	 */
	public FeedbackModule getFeedbackModule() {
		return FeedbackModule.getInstance();
	}

	/**
	 * Gets the Apptentive Survey Module.
	 *
	 * @return The Apptentive Survey Module.
	 */
	public SurveyModule getSurveyModule() {
		return SurveyModule.getInstance();
	}

	private static String getUserEmail(Context context) {
		if (Util.packageHasPermission(context, "android.permission.GET_ACCOUNTS")) {
			String email = EmailUtil.getEmail(context);
			if (email != null) {
				return email;
			}
		}
		return "";
	}
}