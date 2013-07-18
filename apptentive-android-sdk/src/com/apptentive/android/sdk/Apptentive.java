/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.apptentive.android.sdk.lifecycle.ActivityLifecycleManager;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.storage.*;
import com.apptentive.android.sdk.util.ActivityUtil;
import com.apptentive.android.sdk.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * The Apptentive class is responsible for general initialization, and access to each Apptentive Module.
 *
 * @author Sky Kelsey
 */
public class Apptentive {

	private static UnreadMessagesListener unreadMessagesListener;
	private static Map<String, String> customData;

	private Apptentive() {
	}

	// ****************************************************************************************
	// DELEGATE METHODS
	// ****************************************************************************************

	/**
	 * Reserved for future use.
	 */
	public static void onCreate(Activity activity, Bundle savedInstanceState) {
		ActivityUtil.isCurrentActivityMainActivity(activity);
	}


	/**
	 * Call this method from each of your Activity's onStart() methods. Must be called before using other Apptentive APIs.
	 * @param activity The Activity from which this method is called.
	 */
	public static void onStart(Activity activity) {
		init(activity);
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
	 * Call this method from each of your Activity's onStop() methods.
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


	// ****************************************************************************************
	// GLOBAL DATA METHODS
	// ****************************************************************************************

	/**
	 * Sets the user email address. This email address will be sent to the Apptentive server to allow out of app
	 * communication, and to help provide more context about this user. This email will be the definitive email address
	 * for this user.
	 *
	 * @param email The user's email address.
	 */
	public static void setUserEmail(String email) {
		GlobalInfo.userEmail = email;
	}

	/**
	 * Allows you to pass arbitrary string data to the server along with this device's info.
	 * @param customData A Map of key/value pairs to send to the server.
	 */
	public static void setCustomData(Map<String, String> customData) {
		Apptentive.customData = customData;
	}


	// ****************************************************************************************
	// RATINGS
	// ****************************************************************************************

	/**
	 * Increments the number of "significant events" the app's user has achieved. What you consider to be a significant
	 * event is up to you to decide. The number of significant events is used be the Rating Module to determine if it
	 * is time to run the rating flow.
	 *
	 * @param context The context from which this method is called.
	 */
	public static void logSignificantEvent(Context context) {
		RatingModule.getInstance().logSignificantEvent(context);
	}

	/**
	 * Use this to choose where to send the user when they are prompted to rate the app. This should be the same place
	 * that the app was downloaded from.
	 *
	 * @param ratingProvider A {@link IRatingProvider} value.
	 */

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		RatingModule.getInstance().setRatingProvider(ratingProvider);
	}

	/**
	 * If there are any properties that your {@link IRatingProvider} implementation requires, populate them here. This
	 * is not currently needed with the Google Play and Amazon Appstore IRatingProviders.
	 * @param key A String
	 * @param value A String
	 */
	public static void putRatingProviderArg(String key, String value) {
		RatingModule.getInstance().putRatingProviderArg(key, value);
	}

	/**
	 * If you want to launch the ratings flow when conditions are met, call this at an appropriate place in your code.
	 * Calling this method will display the rating flow's first dialog if the conditions you have specified at
	 * apptentive.com for this app have been met. Otherwise it will return immediately and have no side effect.
	 * @param activity The activity from which this set of dialogs is launched.
	 */
	public static void showRatingFlowIfConditionsAreMet(Activity activity) {
		RatingModule.getInstance().run(activity);
	}

	// ****************************************************************************************
	// MESSAGE CENTER
	// ****************************************************************************************


	/**
	 * Opens the Apptentive Message Center UI Activity
	 *
	 * @param activity The Activity from which to launch the Message Center
	 */
	public static void showMessageCenter(Activity activity) {
		showMessageCenter(activity, true);
	}

	/**
	 * Set a listener to be notified when the number of unread messages in the Message Center changes.
	 * @param listener An UnreadMessageListener that you instantiate.
	 */
	public static void setUnreadMessagesListener(UnreadMessagesListener listener) {
		unreadMessagesListener = listener;
	}

	// ****************************************************************************************
	// SURVEYS
	// ****************************************************************************************

	/**
	 * Gets the Apptentive Survey Module.
	 *
	 * @return The Apptentive Survey Module.
	 */
	public static SurveyModule getSurveyModule() {
		return SurveyModule.getInstance();
	}


	// ****************************************************************************************
	// INTERNAL METHODS
	// ****************************************************************************************

	@SuppressLint("NewApi")
	private static void init(final Context context) {

		//
		// First, initialize data relies on synchronous reads from local resources.
		//

		if(!GlobalInfo.initialized) {
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			NetworkStateReceiver.clearListeners();

			// First, Get the api key, and figure out if app is debuggable.
			GlobalInfo.isAppDebuggable = false;
			String apiKey = null;
			try {
				ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
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
			GlobalInfo.appPackage = context.getPackageName();
			GlobalInfo.androidId = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			// Check the host app version, and notify modules if it's changed.
			try {
				PackageManager packageManager = context.getPackageManager();
				PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
				int currentVersionCode = packageInfo.versionCode;
				if(prefs.contains(Constants.PREF_KEY_APP_VERSION_CODE)) {
					int previousVersionCode = prefs.getInt(Constants.PREF_KEY_APP_VERSION_CODE, 0);
					if(previousVersionCode != currentVersionCode) {
						onVersionChanged(context, previousVersionCode, currentVersionCode);
					}
				} else {
					// First start.
					onVersionChanged(context, -1, currentVersionCode);
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
						PayloadSendWorker.start(context);
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
			asyncFetchConversationToken(context);
		} else {
			asyncFetchAppConfiguration(context);
		}

		// TODO: Do this on a dedicated thread if it takes too long. Some HTC devices might take like 30 seconds I think.
		// See if the device info has changed.
		Device deviceInfo = DeviceManager.storeDeviceAndReturnDiff(context, customData);
		if(deviceInfo != null) {
			Log.d("Device info was updated.");
			Log.v(deviceInfo.toString());
			getDatabase(context).addPayload(deviceInfo);
		} else {
			Log.d("Device info was not updated.");
		}

		Sdk sdk = SdkManager.storeSdkAndReturnDiff(context);
		if(sdk != null) {
			Log.d("Sdk was updated.");
			Log.v(sdk.toString());
			getDatabase(context).addPayload(sdk);
		} else {
			Log.d("Sdk was not updated.");
		}

		Person person = PersonManager.storePersonAndReturnDiff(context);
		if(person != null) {
			Log.d("Person was updated.");
			Log.v(person.toString());
			getDatabase(context).addPayload(person);
		} else {
			Log.d("Person was not updated.");
		}

		Log.d("Default Locale: %s", Locale.getDefault().toString());

		// Finally, ensure the send worker is running.
		PayloadSendWorker.start(context);
	}

	private static void onVersionChanged(Context context, int previousVersion, int currentVersion) {
		RatingModule.getInstance().onAppVersionChanged(context);
		AppRelease appRelease = AppReleaseManager.storeAppReleaseAndReturnDiff(context);
		getDatabase(context).addPayload(appRelease);
	}

	private synchronized static void asyncFetchConversationToken(final Context context) {
		new Thread() {
			@Override
			public void run() {
				fetchConversationToken(context);
			}
		}.start();
	}

	/**
	 * First looks to see if we've saved the ConversationToken in memory, then in SharedPreferences, and finally tries to get one
	 * from the server.
	 */
	private static void fetchConversationToken(Context context) {
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
				SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
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
				asyncFetchAppConfiguration(context);
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
	private static void fetchAppConfiguration(Context context, boolean force) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

		// Don't get the app configuration unless forced, or the cache has expired.
		if(!force) {
			Configuration config = Configuration.load(prefs);
			Long expiration = config.getConfigurationCacheExpirationMillis();
			if(System.currentTimeMillis() < expiration){
				Log.v("Using cached configuration.");
				return;
			}
		}

		Log.v("Fetching new configuration.");
		ApptentiveHttpResponse response = ApptentiveClient.getAppConfiguration();
		if(!response.isSuccessful()) {
			return;
		}

		try {
			int cacheSeconds = Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS;
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
			config.setConfigurationCacheExpirationMillis(System.currentTimeMillis() + cacheSeconds * 1000);
			config.save(context);
		} catch (JSONException e) {
			Log.e("Error parsing app configuration from server.", e);
		}
	}

	private static void asyncFetchAppConfiguration(final Context context) {
		new Thread() {
			public void run() {
				fetchAppConfiguration(context, GlobalInfo.isAppDebuggable);
			}
		}.start();
	}

	/**
	 * Internal use only.
	 * @param activity The Activity from which to launch the Message Center
	 * @param forced True if opened manually. False if opened from ratings flow.
	 */
	static void showMessageCenter(Activity activity, boolean forced) {
		MessageManager.createMessageCenterAutoMessage(activity, forced);
		ApptentiveMessageCenter.show(activity, forced);
	}

	/**
	 * Internal use only.
	 */
	public static ApptentiveDatabase getDatabase(Context context) {
		return new ApptentiveDatabase(context);
	}

	/**
	 * Internal use only.
	 */
	public static void notifyUnreadMessagesListener(int unreadMessages) {
		Log.v("Notifying UnreadMessagesListener");
		if(unreadMessagesListener != null) {
			unreadMessagesListener.onUnreadMessageCountChanged(unreadMessages);
		}
	}

	/**
	 * Internal use only.
	 */
	public static void onAppLaunch(final Context context) {
		RatingModule.getInstance().logUse(context);
		MessageManager.asyncFetchAndStoreMessages(context, new MessageManager.MessagesUpdatedListener() {
			public void onMessagesUpdated() {
				notifyUnreadMessagesListener(MessageManager.getUnreadMessageCount(context));
			}
		});
	}

	/**
	 * Internal use only.
	 */
	public static void onAppDidExit() {}
}