/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.comm.NetworkStateListener;
import com.apptentive.android.sdk.comm.NetworkStateReceiver;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.ApptentiveMessageCenter;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.lifecycle.ActivityLifecycleManager;
import com.apptentive.android.sdk.storage.*;
import com.apptentive.android.sdk.util.ActivityUtil;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * The Apptentive class is responsible for general initialization, and access to each Apptentive Module.
 *
 * @author Sky Kelsey
 */
public class Apptentive {

	private static Context appContext = null;
	private static ApptentiveDatabase db;
	private static UnreadMessagesListener unreadMessagesListener;

	private Apptentive() {
	}

	/**
	 * Reserved for future use.
	 */
	public static void onCreate(Activity activity, Bundle savedInstanceState) {
		ActivityUtil.isCurrentActivityMainActivity(activity);
	}


	/**
	 * @param activity The Activity from which this method is called.
	 */
	public static void onStart(Activity activity) {
		appContext = activity.getApplicationContext();
		init();
		ActivityLifecycleManager.activityStarted(activity);
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
	 * @param activity The Activity from which this method is called.
	 */
	public static void onPause(Activity activity) {
	}

	/**
	 * @param activity The Activity from which this method is called.
	 */
	public static void onStop(Activity activity) {
		ActivityLifecycleManager.activityStopped(activity);
	}

	/**
	 * Reserved for future use.
	 * @param activity The Activity from which this method is called.
	 */
	public static void onDestroy(Activity activity) {
	}

	public static Context getAppContext() {
		return appContext;
	}

	public static ApptentiveDatabase getDatabase() {
		return db;
	}

	public static ContentResolver getContentResolver() {
		return appContext.getContentResolver();
	}

	private static void init() {

		//
		// First, initialize data relies on synchronous reads from local resources.
		//

		if(!GlobalInfo.initialized) {
			SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			db = new ApptentiveDatabase(appContext);
			NetworkStateReceiver.clearListeners();

			// First, Get the api key, and figure out if app is debuggable.
			GlobalInfo.isAppDebuggable = false;
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

			// Grab app info we need to access later on.
			GlobalInfo.appPackage = appContext.getPackageName();
			GlobalInfo.androidId = Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			GlobalInfo.userEmail = Util.getUserEmail(appContext);

			// Initialize modules.
			RatingModule.getInstance().setContext(appContext);
			MetricModule.setContext(appContext);

			// Check the host app version, and notify modules if it's changed.
			try {
				PackageManager packageManager = appContext.getPackageManager();
				PackageInfo packageInfo = packageManager.getPackageInfo(appContext.getPackageName(), 0);
				int currentVersionCode = packageInfo.versionCode;
				if(prefs.contains(Constants.PREF_KEY_APP_VERSION_CODE)) {
					int previousVersionCode = prefs.getInt(Constants.PREF_KEY_APP_VERSION_CODE, 0);
					if(previousVersionCode != currentVersionCode) {
						onVersionChanged(previousVersionCode, currentVersionCode);
					}
				} else {
					// First start.
					onVersionChanged(-1, currentVersionCode);
				}
				prefs.edit().putInt(Constants.PREF_KEY_APP_VERSION_CODE, currentVersionCode).commit();

				GlobalInfo.appDisplayName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageInfo.packageName, 0)).toString();
			} catch(PackageManager.NameNotFoundException e) {
				// Nothing we can do then.
				GlobalInfo.appDisplayName = "this app";
			}

			// Listen for network state changes.
			NetworkStateListener networkStateListener = new NetworkStateListener() {
				public void stateChanged(NetworkInfo networkInfo) {
					if(networkInfo.getState() == NetworkInfo.State.CONNECTED){
						Log.v("Network connected.");
						PayloadSendWorker.start();
					}
					if(networkInfo.getState() == NetworkInfo.State.DISCONNECTED){
						Log.v("Network disconnected.");
					}
				}
			};
			NetworkStateReceiver.addListener(networkStateListener);

			// Grab the conversation token from shared preferences.
			if(prefs.contains(Constants.PREF_KEY_CONVERSATION_TOKEN) && prefs.contains(Constants.PREF_KEY_PERSON_ID)) {
				GlobalInfo.conversationToken = prefs.getString(Constants.PREF_KEY_CONVERSATION_TOKEN, null);
				GlobalInfo.personId = prefs.getString(Constants.PREF_KEY_PERSON_ID, null);
			}

			GlobalInfo.initialized = true;
			Log.v("Done initializing...");
		} else {
			Log.v("Already initialized...");
		}

		// Initialize the Conversation Token, or fetch if needed. Fetch config it the token is available.
		if(GlobalInfo.conversationToken == null || GlobalInfo.personId == null) {
			asyncFetchConversationToken();
		} else {
			asyncFetchAppConfiguration();
		}

		// TODO: Do this on a dedicated thread if it takes too long. Some HTC devices might take like 30 seconds I think.
		// See if the device info has changed.
		Device deviceInfo = DeviceManager.storeDeviceAndReturnDiff(appContext);
		if(deviceInfo != null) {
			Log.d("Device info was updated.");
			Log.v(deviceInfo.toString());
			Apptentive.getDatabase().addPayload(deviceInfo);
		} else {
			Log.d("Device info was not updated.");
		}

		Sdk sdk = SdkManager.storeSdkAndReturnDiff(appContext);
		if(sdk != null) {
			Log.d("Sdk was updated.");
			Log.v(sdk.toString());
			Apptentive.getDatabase().addPayload(sdk);
		} else {
			Log.d("Sdk was not updated.");
		}

		// TODO: Check out locale...
		Log.e("Default Locale: %s", Locale.getDefault().toString());

		// Finally, ensure the send worker is running.
		PayloadSendWorker.start();
	}

	private static void onVersionChanged(int previousVersion, int currentVersion) {
		RatingModule.getInstance().onAppVersionChanged();
		AppRelease appRelease = AppReleaseManager.storeAppReleaseAndReturnDiff(Apptentive.getAppContext());
		getDatabase().addPayload(appRelease);
	}

	private synchronized static void asyncFetchConversationToken() {
		new Thread() {
			@Override
			public void run() {
				fetchConversationToken();
			}
		}.start();
	}

	/**
	 * First looks to see if we've saved the ConversationToken in memory, then in SharedPreferences, and finally tries to get one
	 * from the server.
	 */
	private static void fetchConversationToken() {
		// Try to fetch a new one from the server.
		ConversationTokenRequest request = new ConversationTokenRequest();
		// TODO: Allow host app to send a user id, if available.
		ApptentiveHttpResponse response = ApptentiveClient.getConversationToken(request);
		if (response == null) {
			Log.w("Got null response fetching ConversationToken.");
			return;
		}
		if(response.isSuccessful()) {
			try {
				JSONObject root = new JSONObject(response.getContent());
				String conversationToken = root.getString("token");
				Log.d("ConversationToken: " + conversationToken);
				SharedPreferences prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
				if (conversationToken != null && !conversationToken.equals("")) {
					GlobalInfo.conversationToken = conversationToken;
					prefs.edit().putString(Constants.PREF_KEY_CONVERSATION_TOKEN, conversationToken).commit();
				}
				String personId = root.getString("person_id");
				Log.d("PersonId: " + personId);
				if (personId != null && !personId.equals("")) {
					GlobalInfo.personId = personId;
					prefs.edit().putString(Constants.PREF_KEY_PERSON_ID, personId).commit();
				}
				// Try to fetch app configuration, since it depends on the conversation token.
				asyncFetchAppConfiguration();
			} catch (JSONException e) {
				Log.e("Error parsing ConversationToken response json.", e);
			}
		}
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
			String iso8601DateString = prefs.getString(Constants.PREF_KEY_APP_CONFIG_EXPIRATION, null);
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
		ApptentiveHttpResponse response = ApptentiveClient.getAppConfiguration();
		if(!response.isSuccessful()) {
			return;
		}

		try {
			int cacheSeconds = Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION;
			if(response.getHeaders() != null && response.getHeaders().get("Cache-Control") != null) {
				String cacheControl = response.getHeaders().get("Cache-Control");
				String[] parts = cacheControl.split("max-age=");
				if(parts.length == 2) {
					try {
						cacheSeconds = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						Log.e("Error parsing cache expiration as number: %d", e, parts[1]);
					}
				}
			}
			Configuration config = new Configuration(response.getContent());
			config.save(appContext, cacheSeconds);
		} catch (JSONException e) {
			Log.e("Error parsing app configuration from server.", e);
		}
	}

	private static void asyncFetchAppConfiguration() {
		new Thread() {
			public void run() {
				fetchAppConfiguration(GlobalInfo.isAppDebuggable);
			}
		}.start();
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
	 * Gets the Apptentive Survey Module.
	 *
	 * @return The Apptentive Survey Module.
	 */
	public static SurveyModule getSurveyModule() {
		return SurveyModule.getInstance();
	}

	/**
	 * Opens the Apptentive Message Center UI Activity
	 *
	 * @param context The Context from which to launch the Message Center
	 */
	public static void showMessageCenter(Context context) {
		showMessageCenter(context, true);
	}

	/**
	 * Internal use only.
	 * @param context The Context from which to launch the Message Center
	 * @param forced True if opened manually. False if opened from ratings flow.
	 */
	static void showMessageCenter(Context context, boolean forced) {
		MessageManager.createMessageCenterAutoMessage(forced);
		ApptentiveMessageCenter.show(context);
	}

	public static void setUnreadMessagesListener(UnreadMessagesListener listener) {
		unreadMessagesListener = listener;
	}

	/**
	 * Internal use only.
	 */
	public static void notifyUnreadMessagesListener(int unreadMessages) {
		Log.v("Notifying UnreadMessagesListener");
		if(unreadMessagesListener != null) {
			unreadMessagesListener.onUnreadMessagesAvailable(unreadMessages);
		}
	}

	/**
	 * Internal use only.
	 */
	public static void onAppLaunch() {
		Apptentive.getRatingModule().logUse();
		MessageManager.asyncFetchAndStoreMessages(new MessageManager.MessagesUpdatedListener() {
			public void onMessagesUpdated() {
				notifyUnreadMessagesListener(MessageManager.getUnreadMessageCount());
			}
		});
	}

	/**
	 * Internal use only.
	 */
	public static void onAppDidExit() {
	}
}