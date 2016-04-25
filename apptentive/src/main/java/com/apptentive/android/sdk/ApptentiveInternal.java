/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.lifecycle.ApptentiveActivityLifecycleCallbacks;
import com.apptentive.android.sdk.model.AppRelease;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Device;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.model.Sdk;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PayloadSendWorker;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 */
public class ApptentiveInternal {

	static AtomicBoolean isApptentiveInitialized = new AtomicBoolean(false);
	InteractionManager interactionManager;
	MessageManager messageManager;
	PayloadSendWorker payloadWorker;
	ApptentiveDatabase database;
	CodePointStore codePointStore;

	// These variables are initialized in Apptentive.register(), and so they are freely thereafter. If they are unexpectedly null, then if means the host app did not register Apptentive.
	Context appContext;
	boolean appIsInForeground;
	boolean isAppDebuggable;
	SharedPreferences prefs;
	String apiKey;
	String conversationToken;
	String conversationId;
	String personId;
	String androidId;
	String appPackageName;
	Resources.Theme apptentiveTheme;
	Resources.Theme apptentiveToolbarTheme;
	int statusBarColorDefault;
	String defaultAppDisplayName = "this app";
  // booleans to prevent starting multiple fetching asyncTasks simultaneously
	AtomicBoolean isConversationTokenFetchPending = new AtomicBoolean(false);
	AtomicBoolean isConfigurationFetchPending = new AtomicBoolean(false);

	IRatingProvider ratingProvider;
	Map<String, String> ratingProviderArgs;
	WeakReference<OnSurveyFinishedListener> onSurveyFinishedListener;

	final LinkedBlockingQueue interactionUpdateListeners = new LinkedBlockingQueue();

	ExecutorService cachedExecutor;

	private WeakReference<Activity> currentTaskStackTopActivity;

	// Used for temporarily holding customData that needs to be sent on the next message the consumer sends.
	private Map<String, Object> customData;

	public static final String PUSH_ACTION = "action";

	public enum PushAction {
		pmc,       // Present Message Center.
		unknown;   // Anything unknown will not be handled.

		public static PushAction parse(String name) {
			try {
				return PushAction.valueOf(name);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.d("Error parsing unknown PushAction: " + name);
			}
			return unknown;
		}
	}

	private static volatile ApptentiveInternal sApptentiveInternal;

	ApptentiveInternal() {
	}


	public static boolean isApptentiveRegistered() {
		return (sApptentiveInternal != null);
	}

	/**
	 * Create a new or return a existing thread-safe instance of the Apptentive SDK. If this
	 * or any other {@link #getInstance()} has already been called in the application's lifecycle, the
	 * API key will be ignored and the current instance will be returned.
	 * <p/>
	 * This will be called from the application's onCreate(), before any other application objects have been
	 * created. Since the time spent in this function directly impacts the performance of starting the first activity,
	 * service, or receiver in the hosting app's process, the initialization of Apptentive is deferred to the first time
	 * {@link #getInstance()} is called.
	 *
	 * @param context the context of the app that is creating the instance
	 * @return An non-null instance of the Apptentive SDK
	 */
	public static ApptentiveInternal createInstance(Context context, final String apptentiveApiKey) {
		if (sApptentiveInternal == null) {
			synchronized (ApptentiveInternal.class) {
				if (sApptentiveInternal == null && context != null) {
					sApptentiveInternal = new ApptentiveInternal();
					isApptentiveInitialized.set(false);
					sApptentiveInternal.appContext = context.getApplicationContext();
					sApptentiveInternal.prefs = sApptentiveInternal.appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

					MessageManager msgManager = new MessageManager();
					PayloadSendWorker payloadWorker = new PayloadSendWorker();
					InteractionManager interactionMgr = new InteractionManager();
					ApptentiveDatabase db = new ApptentiveDatabase(sApptentiveInternal.appContext);
					CodePointStore store = new CodePointStore();

					sApptentiveInternal.messageManager = msgManager;
					sApptentiveInternal.payloadWorker = payloadWorker;
					sApptentiveInternal.interactionManager = interactionMgr;
					sApptentiveInternal.database = db;
					sApptentiveInternal.codePointStore = store;
					sApptentiveInternal.cachedExecutor = Executors.newCachedThreadPool();
					sApptentiveInternal.apiKey = Util.trim(apptentiveApiKey);
				}
			}
		}
		return sApptentiveInternal;
	}

	/**
	 * Retrieve the existing instance of the Apptentive class. If {@link Apptentive#register(Application)} is
	 * not called prior to this, it will only return null if context is null
	 *
	 * @return the existing instance of the Apptentive SDK fully initialized with API key, or a new instance if context is not null
	 */
	public static ApptentiveInternal getInstance(Context context) {
		return createInstance((context == null) ? null : context, null);
	}

	/**
	 * Retrieve the existing instance of the Apptentive class. If {@link Apptentive#register(Application)} is
	 * not called prior to this, it will return null; Otherwise, it will return the singleton instance initialized.
	 *
	 * @return the existing instance of the Apptentive SDK fully initialized with API key, or null
	 */
	public static ApptentiveInternal getInstance() {
		// Lazy initialization, only once for each application launch when getInstance() is called for the 1st time
		if (sApptentiveInternal != null && !isApptentiveInitialized.get()) {
			synchronized (ApptentiveInternal.class) {
				if (sApptentiveInternal != null && !isApptentiveInitialized.get()) {
					isApptentiveInitialized.set(true);
					sApptentiveInternal.init();
				}
			}
		}
		return sApptentiveInternal;
	}

	/**
	 * Use this method to set or clear the internal state (pass in null)
	 * Note: designed to be used for unit testing only
	 *
	 * @param instance the internal instance to be set to
	 */
	public static void setInstance(ApptentiveInternal instance) {
		sApptentiveInternal = instance;
		isApptentiveInitialized.set(false);
	}

	/**
	 * Use this method to set or clear the internal app context (pass in null)
	 * Note: designed to be used for unit testing only
	 *
	 * @param appContext the new application context to be set to
	 */
	public static void setApplicationContext(Context appContext) {
		synchronized (ApptentiveInternal.class) {
			ApptentiveInternal internal = ApptentiveInternal.getInstance();
			if (internal != null) {
				internal.appContext = appContext;
			}
		}
	}

	/* Called by {@link #Apptentive.register()} to register global lifecycle
	 * callbacks.
	 */
	static void setLifeCycleCallback() {
		if (sApptentiveInternal != null && sApptentiveInternal.appContext instanceof Application) {
			((Application) sApptentiveInternal.appContext).registerActivityLifecycleCallbacks(new ApptentiveActivityLifecycleCallbacks());
		}
	}

	/*
	 * Object getter methods through ApptentiveInternal instance
	 * Usage: ApptentiveInternal.getInstance().getxxxx()
	 */

	public Context getApplicationContext() {
		return appContext;
	}

	/* Get the last running activity from the current application, i.e. at the top of the task
	 * It is tracked through {@link #onActivityStarted(Activity)} and {@link #onActivityDestroyed(Activity)}
	 *
	 * If Apptentive interaction is to be launched from a non-activity context, use the last running activity at
	 * the top of the task.
	 */
	public Activity getCurrentTaskStackTopActivity() {
		if (currentTaskStackTopActivity != null) {
			return currentTaskStackTopActivity.get();
		}
		return null;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	public InteractionManager getInteractionManager() {
		return interactionManager;
	}

	public PayloadSendWorker getPayloadWorker() {
		return payloadWorker;
	}

	public ApptentiveDatabase getApptentiveDatabase() {
		return database;
	}

	public CodePointStore getCodePointStore() {
		return codePointStore;
	}

	public Resources.Theme getApptentiveTheme() {
		return apptentiveTheme;
	}

	public Resources.Theme getApptentiveToolbarTheme() {
		return apptentiveToolbarTheme;
	}

	public int getDefaultStatusbarColor() {
		return statusBarColorDefault;
	}

	public String getApptentiveConversationToken() {
		return conversationToken;
	}

	public String getApptentiveApiKey() {
		return apiKey;
	}

	public String getDefaultAppDisplayName() {
		return defaultAppDisplayName;
	}

	public boolean isApptentiveDebuggable() {
		return isAppDebuggable;
	}

	public String getPersonId() {
		return personId;
	}

	public String getAndroidId() {
		return androidId;
	}

	public SharedPreferences getSharedPrefs() {
		return prefs;
	}

	public void addCustomDeviceData(String key, Object value) {
		if (key == null || key.trim().length() == 0) {
			return;
		}
		key = key.trim();
		CustomData customData = DeviceManager.loadCustomDeviceData();
		if (customData != null) {
			try {
				customData.put(key, value);
				DeviceManager.storeCustomDeviceData(customData);
			} catch (JSONException e) {
				ApptentiveLog.w("Unable to add custom device data.", e);
			}
		}
	}

	public void addCustomPersonData(String key, Object value) {
		if (key == null || key.trim().length() == 0) {
			return;
		}
		CustomData customData = PersonManager.loadCustomPersonData();
		if (customData != null) {
			try {
				customData.put(key, value);
				PersonManager.storeCustomPersonData(customData);
			} catch (JSONException e) {
				ApptentiveLog.w("Unable to add custom person data.", e);
			}
		}
	}

	public void runOnWorkerThread(Runnable r) {
		cachedExecutor.execute(r);
	}

	public void scheduleOnWorkerThread(Runnable r) {
		cachedExecutor.submit(r);
	}

	public void checkAndUpdateApptentiveConfigurations() {
		// Initialize the Conversation Token, or fetch if needed. Fetch config it the token is available.
		if (conversationToken == null || personId == null) {
			asyncFetchConversationToken();
		} else {
			asyncFetchAppConfigurationAndInteractions();
		}
	}

	public void onAppLaunch(final Context appContext) {
		EngagementModule.engageInternal(appContext, Event.EventLabel.app__launch.getLabelName());
	}

	public void onAppExit(final Context appContext) {
		EngagementModule.engageInternal(appContext, Event.EventLabel.app__exit.getLabelName());
	}

	public void onActivityStarted(Activity activity) {
		if (activity != null) {
			currentTaskStackTopActivity = new WeakReference<Activity>(activity);
		}
	}

	public void onActivityResumed(Activity activity) {
		messageManager.setCurrentForgroundActivity(activity);

		checkAndUpdateApptentiveConfigurations();

		syncDevice();
		syncSdk();
		syncPerson();
	}

	public void onActivityDestroyed(Activity activity) {
		if (activity != null && currentTaskStackTopActivity != null) {
			Activity currentBottomActivity = currentTaskStackTopActivity.get();
			if (currentBottomActivity != null && currentBottomActivity == activity) {
				currentTaskStackTopActivity = null;
			}
		}
	}

	public void onAppEnterForeground() {
		appIsInForeground = true;
		payloadWorker.appWentToForeground();
		messageManager.appWentToForeground();
	}

	public void onAppEnterBackground() {
		appIsInForeground = false;
		payloadWorker.appWentToBackground();
		messageManager.appWentToBackground();
	}


	public void init() {

		codePointStore.init();
		messageManager.init();
		conversationToken = prefs.getString(Constants.PREF_KEY_CONVERSATION_TOKEN, null);
		conversationId = prefs.getString(Constants.PREF_KEY_CONVERSATION_ID, null);
		personId = prefs.getString(Constants.PREF_KEY_PERSON_ID, null);

		boolean apptentiveDebug = false;
		String logLevelOverride = null;
		String manifestApiKey = null;
		try {
			appPackageName = appContext.getPackageName();
			PackageManager packageManager = appContext.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			ApplicationInfo ai = packageInfo.applicationInfo;

			Bundle metaData = ai.metaData;
			if (metaData != null) {
				manifestApiKey = Util.trim(metaData.getString(Constants.MANIFEST_KEY_APPTENTIVE_API_KEY));
				logLevelOverride = Util.trim(metaData.getString(Constants.MANIFEST_KEY_APPTENTIVE_LOG_LEVEL));
				apptentiveDebug = metaData.getBoolean(Constants.MANIFEST_KEY_APPTENTIVE_DEBUG);
				isAppDebuggable = (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}

			/*
			 * Construct the theme used by Apptentive Ui in the following order
			 *  1. start with Apptentive default theme : ApptentiveTheme.Base.Versioned
			 *  2. merge with app default theme, if one is specified and it's a AppCompat theme.
			 *  (merge takes value from app default theme if both define the same attribute)
			 *  3. apply Apptentive UI specific frame style
			 *  4. Apply apptentive theme override specified by the app in ApptentiveThemeOverride
			 */
			int appDefaultThemeId = ai.theme;
			boolean appHasTheme = appDefaultThemeId != 0;
			boolean appThemeIsAppCompatTheme = false;
			// Step 1
			apptentiveTheme = appContext.getResources().newTheme();
			apptentiveTheme.applyStyle(R.style.ApptentiveTheme_Base_Versioned, true);

			int transparentColor = ContextCompat.getColor(appContext, android.R.color.transparent);
			statusBarColorDefault = transparentColor;

			// Step 2, check if app specifies a default appcompat theme
			if (appHasTheme) {
				Resources.Theme appDefaultTheme = appContext.getResources().newTheme();
				appDefaultTheme.applyStyle(appDefaultThemeId, true);
				// If the app contains colorPrimaryDark, it is using an AppCompat theme. Therefore, we want to use it.
				// If it's not using an AppCompat theme, we don't want to apply it to our SDK, and use our default theme instead.
				appThemeIsAppCompatTheme = Util.getThemeColor(appDefaultTheme, R.attr.colorPrimaryDark) != 0;
				if (appThemeIsAppCompatTheme) {
					apptentiveTheme.applyStyle(appDefaultThemeId, true);
				}
			}
			// Step 3
			apptentiveTheme.applyStyle(R.style.ApptentiveBaseFrameTheme, true);
			// Step 4
			int themeOverrideResId = appContext.getResources().getIdentifier("ApptentiveThemeOverride", "style", appPackageName);
			if (themeOverrideResId != 0) {
				apptentiveTheme.applyStyle(themeOverrideResId, true);
			}

			TypedArray a = apptentiveTheme.obtainStyledAttributes(new int[]{android.R.attr.statusBarColor});
			try {
				statusBarColorDefault = a.getColor(0, transparentColor);
			} finally {
				a.recycle();
			}

			int toolbarThemeId = Util.getResourceIdFromAttribute(ApptentiveInternal.getInstance().getApptentiveTheme(), R.attr.apptentiveToolbarTheme);
			apptentiveToolbarTheme = appContext.getResources().newTheme();
			apptentiveToolbarTheme.setTo(apptentiveTheme);
			apptentiveToolbarTheme.applyStyle(toolbarThemeId, true);

			Integer currentVersionCode = packageInfo.versionCode;
			String currentVersionName = packageInfo.versionName;
			VersionHistoryStore.VersionHistoryEntry lastVersionEntrySeen = VersionHistoryStore.getLastVersionSeen();
			AppRelease appRelease = new AppRelease();
			appRelease.setVersion(currentVersionName);
			appRelease.setIdentifier(appPackageName);
			appRelease.setBuildNumber(String.valueOf(currentVersionCode));
			appRelease.setTargetSdkVersion(String.valueOf(packageInfo.applicationInfo.targetSdkVersion));
			appRelease.setAppStore(Util.getInstallerPackageName(appContext));
			appRelease.setInheritStyle(appThemeIsAppCompatTheme);
			appRelease.setOverrideStyle(themeOverrideResId != 0);

			if (lastVersionEntrySeen == null) {
				onVersionChanged(null, currentVersionCode, null, currentVersionName, appRelease);
			} else {
				if (!currentVersionCode.equals(lastVersionEntrySeen.versionCode) || !currentVersionName.equals(lastVersionEntrySeen.versionName)) {
					onVersionChanged(lastVersionEntrySeen.versionCode, currentVersionCode, lastVersionEntrySeen.versionName, currentVersionName, appRelease);
				}
			}
			defaultAppDisplayName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageInfo.packageName, 0)).toString();

			// Prevent delayed run-time exception if the app upgrades from pre-2.0 and doesn't remove NetworkStateReceiver from manifest
			ActivityInfo[] registered = packageInfo.receivers;
			if (registered != null) {
				for (ActivityInfo activityInfo : registered) {
					// Throw assertion error when relict class found in manifest.
					if (activityInfo.name.equals("com.apptentive.android.sdk.comm.NetworkStateReceiver")) {
						throw new AssertionError("NetworkStateReceiver has been removed from Apptentive SDK, please make sure it's also removed from manifest file");
					}
				}
			}

		} catch (Exception e) {
			ApptentiveLog.e("Unexpected error while reading application or package info.", e);
		}

		// Set debuggable and appropriate log level.
		if (apptentiveDebug) {
			ApptentiveLog.i("Apptentive debug logging set to VERBOSE.");
			setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		} else if (logLevelOverride != null) {
			ApptentiveLog.i("Overriding log level: %s", logLevelOverride);
			setMinimumLogLevel(ApptentiveLog.Level.parse(logLevelOverride));
		} else {
			if (isAppDebuggable) {
				setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
			}
		}
		ApptentiveLog.i("Debug mode enabled? %b", isAppDebuggable);

		String lastSeenSdkVersion = prefs.getString(Constants.PREF_KEY_LAST_SEEN_SDK_VERSION, "");
		if (!lastSeenSdkVersion.equals(Constants.APPTENTIVE_SDK_VERSION)) {
			onSdkVersionChanged(appContext, lastSeenSdkVersion, Constants.APPTENTIVE_SDK_VERSION);
		}

		// The apiKey can be passed in programmatically, or we can fallback to checking in the manifest.
		if (TextUtils.isEmpty(apiKey) && !TextUtils.isEmpty(manifestApiKey)) {
			apiKey = manifestApiKey;
		}
		if (TextUtils.isEmpty(apiKey) || apiKey.contains(Constants.EXAMPLE_API_KEY_VALUE)) {
			String errorMessage = "The Apptentive API Key is not defined. You may provide your Apptentive API Key in Apptentive.register(), or in as meta-data in your AndroidManifest.xml.\n" +
					"<meta-data android:name=\"apptentive_api_key\"\n" +
					"           android:value=\"@string/your_apptentive_api_key\"/>";
			if (isAppDebuggable) {
				throw new RuntimeException(errorMessage);
			} else {
				ApptentiveLog.e(errorMessage);
			}
		} else {
			ApptentiveLog.d("Using cached Apptentive API Key");
		}
		ApptentiveLog.d("Apptentive API Key: %s", apiKey);

		// Grab app info we need to access later on.
		androidId = Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		ApptentiveLog.d("Android ID: ", androidId);
		ApptentiveLog.d("Default Locale: %s", Locale.getDefault().toString());
		ApptentiveLog.d("Conversation id: %s", prefs.getString(Constants.PREF_KEY_CONVERSATION_ID, "null"));
	}

	private void onVersionChanged(Integer previousVersionCode, Integer currentVersionCode, String previousVersionName, String currentVersionName, AppRelease currentAppRelease) {
		ApptentiveLog.i("Version changed: Name: %s => %s, Code: %d => %d", previousVersionName, currentVersionName, previousVersionCode, currentVersionCode);
		VersionHistoryStore.updateVersionHistory(currentVersionCode, currentVersionName);
		AppRelease appRelease = AppReleaseManager.storeAppReleaseAndReturnDiff(currentAppRelease);
		if (appRelease != null) {
			ApptentiveLog.d("App release was updated.");
			database.addPayload(appRelease);
		}
		invalidateCaches();
	}

	private void onSdkVersionChanged(Context context, String previousSdkVersion, String currentSdkVersion) {
		ApptentiveLog.i("SDK version changed: %s => %s", previousSdkVersion, currentSdkVersion);
		context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().putString(Constants.PREF_KEY_LAST_SEEN_SDK_VERSION, currentSdkVersion).apply();
		invalidateCaches();
	}

	/**
	 * We want to make sure the app is using the latest configuration from the server if the app or sdk version changes.
	 */
	private void invalidateCaches() {
		interactionManager.updateCacheExpiration(0);
		Configuration config = Configuration.load();
		config.setConfigurationCacheExpirationMillis(System.currentTimeMillis());
		config.save();
	}

	private synchronized void asyncFetchConversationToken() {
		if (isConversationTokenFetchPending.compareAndSet(false, true)) {
			AsyncTask<Void, Void, Boolean> fetchConversationTokenTask = new AsyncTask<Void, Void, Boolean>() {
				private Exception e = null;

				@Override
				protected Boolean doInBackground(Void... params) {
					Boolean result = new Boolean(false);
					try {
						result = fetchConversationToken();
					} catch (Exception e) {
						// Hold onto the unhandled exception from fetchConversationToken() for later handling in UI thread
						this.e = e;
					}
					return result;
				}

				@Override
				protected void onPostExecute(Boolean v) {
					if (e == null) {
						// Update pending state on UI thread after finishing the task
						ApptentiveLog.i("Fetching conversation token asyncTask finished. Result:" + v.booleanValue());
						isConversationTokenFetchPending.set(false);
						if (v.booleanValue()) {
							// Once token is fetched successfully, start two asyncTasks to fetch global configuration and interaction simultaneously
							asyncFetchAppConfigurationAndInteractions();
						}
					} else {
						ApptentiveLog.w("Unhandled Exception thrown from fetching conversation token asyncTask", e);
						MetricModule.sendError(e, null, null);
					}
				}

			};

			ApptentiveLog.i("Fetching conversation token asyncTask scheduled.");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				fetchConversationTokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				fetchConversationTokenTask.execute();
			}
		} else {
			ApptentiveLog.v("Fetching Configuration pending");
		}
	}


	private boolean fetchConversationToken() {
		ApptentiveLog.i("Fetching Configuration token task started.");
		// Try to fetch a new one from the server.
		ConversationTokenRequest request = new ConversationTokenRequest();

		// Send the Device and Sdk now, so they are available on the server from the start.
		request.setDevice(DeviceManager.storeDeviceAndReturnIt());
		request.setSdk(SdkManager.storeSdkAndReturnIt());
		request.setPerson(PersonManager.storePersonAndReturnIt());

		ApptentiveHttpResponse response = ApptentiveClient.getConversationToken(request);
		if (response == null) {
			ApptentiveLog.w("Got null response fetching ConversationToken.");
			return false;
		}
		if (response.isSuccessful()) {
			try {
				JSONObject root = new JSONObject(response.getContent());
				String conversationToken = root.getString("token");
				ApptentiveLog.d("ConversationToken: " + conversationToken);
				String conversationId = root.getString("id");
				ApptentiveLog.d("New Conversation id: %s", conversationId);

				if (conversationToken != null && !conversationToken.equals("")) {
					setConversationToken(conversationToken);
					setConversationId(conversationId);
				}
				String personId = root.getString("person_id");
				ApptentiveLog.d("PersonId: " + personId);
				if (personId != null && !personId.equals("")) {
					setPersonId(personId);
				}
				return true;
			} catch (JSONException e) {
				ApptentiveLog.e("Error parsing ConversationToken response json.", e);
			}
		}
		return false;
	}

	/**
	 * Fetches the global app configuration from the server and stores the keys into our SharedPreferences.
	 */
	private void fetchAppConfiguration() {
		ApptentiveLog.i("Fetching new Configuration task started.");
		ApptentiveHttpResponse response = ApptentiveClient.getAppConfiguration();
		try {
			Map<String, String> headers = response.getHeaders();
			if (headers != null) {
				String cacheControl = headers.get("Cache-Control");
				Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
				if (cacheSeconds == null) {
					cacheSeconds = Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS;
				}
				ApptentiveLog.d("Caching configuration for %d seconds.", cacheSeconds);
				Configuration config = new Configuration(response.getContent());
				config.setConfigurationCacheExpirationMillis(System.currentTimeMillis() + cacheSeconds * 1000);
				config.save();
			}
		} catch (JSONException e) {
			ApptentiveLog.e("Error parsing app configuration from server.", e);
		}
	}

	private void asyncFetchAppConfigurationAndInteractions() {
		boolean force = isAppDebuggable;

		// Don't get the app configuration unless no pending fetch AND either forced, or the cache has expired.
		if (isConfigurationFetchPending.compareAndSet(false, true) && (force || Configuration.load().hasConfigurationCacheExpired())) {
			AsyncTask<Void, Void, Void> fetchConfigurationTask = new AsyncTask<Void, Void, Void>() {
				// Hold onto the exception from the AsyncTask instance for later handling in UI thread
				private Exception e = null;

				@Override
				protected Void doInBackground(Void... params) {
					try {
						fetchAppConfiguration();
					} catch (Exception e) {
						this.e = e;
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void v) {
					// Update pending state on UI thread after finishing the task
					ApptentiveLog.i("Fetching new Configuration asyncTask finished.");
					isConfigurationFetchPending.set(false);
					if (e != null) {
						ApptentiveLog.w("Unhandled Exception thrown from fetching configuration asyncTask", e);
						MetricModule.sendError(e, null, null);
					} else {
						// Check if need to start another asyncTask to fetch interaction after successfully fetching configuration
						interactionManager.asyncFetchAndStoreInteractions();
					}
				}
			};

			ApptentiveLog.i("Fetching new Configuration asyncTask scheduled.");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				fetchConfigurationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				fetchConfigurationTask.execute();
			}
		} else {
			ApptentiveLog.v("Using cached Configuration.");
			// If configuration hasn't expire, then check if need to start another asyncTask to fetch interaction
			interactionManager.asyncFetchAndStoreInteractions();
		}
	}

	/**
	 * Sends current Device to the server if it differs from the last time it was sent.
	 */
	void syncDevice() {
		Device deviceInfo = DeviceManager.storeDeviceAndReturnDiff();
		if (deviceInfo != null) {
			ApptentiveLog.d("Device info was updated.");
			ApptentiveLog.v(deviceInfo.toString());
			database.addPayload(deviceInfo);
		} else {
			ApptentiveLog.d("Device info was not updated.");
		}
	}

	/**
	 * Sends current Sdk to the server if it differs from the last time it was sent.
	 */
	private void syncSdk() {
		Sdk sdk = SdkManager.storeSdkAndReturnDiff();
		if (sdk != null) {
			ApptentiveLog.d("Sdk was updated.");
			ApptentiveLog.v(sdk.toString());
			database.addPayload(sdk);
		} else {
			ApptentiveLog.d("Sdk was not updated.");
		}
	}

	/**
	 * Sends current Person to the server if it differs from the last time it was sent.
	 */
	private void syncPerson() {
		Person person = PersonManager.storePersonAndReturnDiff();
		if (person != null) {
			ApptentiveLog.d("Person was updated.");
			ApptentiveLog.v(person.toString());
			database.addPayload(person);
		} else {
			ApptentiveLog.d("Person was not updated.");
		}
	}

	public IRatingProvider getRatingProvider() {
		if (ratingProvider == null) {
			ratingProvider = new GooglePlayRatingProvider();
		}
		return ratingProvider;
	}

	public void setRatingProvider(IRatingProvider ratingProvider) {
		this.ratingProvider = ratingProvider;
	}

	public Map<String, String> getRatingProviderArgs() {
		return ratingProviderArgs;
	}

	public void putRatingProviderArg(String key, String value) {
		if (ratingProviderArgs == null) {
			ratingProviderArgs = new HashMap<String, String>();
		}
		ratingProviderArgs.put(key, value);
	}

	public void setOnSurveyFinishedListener(OnSurveyFinishedListener onSurveyFinishedListener) {
		if (onSurveyFinishedListener != null) {
			this.onSurveyFinishedListener = new WeakReference<OnSurveyFinishedListener>(onSurveyFinishedListener);
		} else {
			this.onSurveyFinishedListener = null;
		}
	}

	public OnSurveyFinishedListener getOnSurveyFinishedListener() {
		return (onSurveyFinishedListener == null) ? null : onSurveyFinishedListener.get();
	}

	public void addInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener) {
		interactionUpdateListeners.add(listener);
	}

	public void removeInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener) {
		interactionUpdateListeners.remove(listener);
	}

	/**
	 * Pass in a log level to override the default, which is {@link ApptentiveLog.Level#INFO}
	 */
	public void setMinimumLogLevel(ApptentiveLog.Level level) {
		ApptentiveLog.overrideLogLevel(level);
	}

	private String pushCallbackActivityName;

	public void setPushCallbackActivity(Class<? extends Activity> activity) {
		pushCallbackActivityName = activity.getName();
		ApptentiveLog.d("Setting push callback activity name to %s", pushCallbackActivityName);
	}

	public String getPushCallbackActivityName() {
		return pushCallbackActivityName;
	}

	/**
	 * The key that is used to store extra data on an Apptentive push notification.
	 */
	static final String APPTENTIVE_PUSH_EXTRA_KEY = "apptentive";

	static final String PARSE_PUSH_EXTRA_KEY = "com.parse.Data";

	static String getApptentivePushNotificationData(Intent intent) {
		String apptentive = null;
		if (intent != null) {
			ApptentiveLog.v("Got an Intent.");
			// Parse
			if (intent.hasExtra(PARSE_PUSH_EXTRA_KEY)) {
				String parseStringExtra = intent.getStringExtra(PARSE_PUSH_EXTRA_KEY);
				ApptentiveLog.v("Got a Parse Push.");
				try {
					JSONObject parseJson = new JSONObject(parseStringExtra);
					apptentive = parseJson.optString(APPTENTIVE_PUSH_EXTRA_KEY, null);
				} catch (JSONException e) {
					ApptentiveLog.e("Corrupt Parse String Extra: %s", parseStringExtra);
				}
			} else {
				// Straight GCM / SNS
				ApptentiveLog.v("Got a non-Parse push.");
				apptentive = intent.getStringExtra(APPTENTIVE_PUSH_EXTRA_KEY);
			}
		}
		return apptentive;
	}

	static String getApptentivePushNotificationData(Bundle pushBundle) {
		if (pushBundle != null) {
			return pushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY);
		}
		return null;
	}

	boolean setPendingPushNotification(String apptentivePushData) {
		if (apptentivePushData != null) {
			ApptentiveLog.d("Saving Apptentive push notification data.");
			prefs.edit().putString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, apptentivePushData).apply();
			messageManager.startMessagePreFetchTask();
			return true;
		}
		return false;
	}

	public void showAboutInternal(Context context, boolean showBrandingBand) {
		Intent intent = new Intent();
		intent.setClass(context, ApptentiveViewActivity.class);
		intent.putExtra(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.ABOUT);
		intent.putExtra(Constants.FragmentConfigKeys.EXTRA, showBrandingBand);
		if (!(context instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		}
		context.startActivity(intent);
	}

	public boolean showMessageCenterInternal(Context context, Map<String, Object> customData) {
		boolean interactionShown = false;
		if (canShowMessageCenterInternal()) {
			if (customData != null) {
				Iterator<String> keysIterator = customData.keySet().iterator();
				while (keysIterator.hasNext()) {
					String key = keysIterator.next();
					Object value = customData.get(key);
					if (value != null) {
						if (!(value instanceof String ||
								value instanceof Boolean ||
								value instanceof Long ||
								value instanceof Double ||
								value instanceof Float ||
								value instanceof Integer ||
								value instanceof Short)) {
							ApptentiveLog.w("Removing invalid customData type: %s", value.getClass().getSimpleName());
							keysIterator.remove();
						}
					}
				}
			}
			this.customData = customData;
			interactionShown = EngagementModule.engageInternal(context, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
			if (!interactionShown) {
				this.customData = null;
			}
		} else {
			showMessageCenterFallback(context);
		}
		return interactionShown;
	}

	public void showMessageCenterFallback(Context context) {
		EngagementModule.launchMessageCenterErrorActivity(context);
	}

	public boolean canShowMessageCenterInternal() {
		return EngagementModule.canShowInteraction("com.apptentive", "app", MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
	}

	public Map<String, Object> getAndClearCustomData() {
		Map<String, Object> customData = this.customData;
		this.customData = null;
		return customData;
	}

	private void setConversationToken(String newConversationToken) {
		conversationToken = newConversationToken;
		prefs.edit().putString(Constants.PREF_KEY_CONVERSATION_TOKEN, conversationToken).apply();
	}

	private void setConversationId(String newConversationId) {
		conversationId = newConversationId;
		prefs.edit().putString(Constants.PREF_KEY_CONVERSATION_ID, conversationId).apply();
	}

	private void setPersonId(String newPersonId) {
		personId = newPersonId;
		prefs.edit().putString(Constants.PREF_KEY_PERSON_ID, personId).apply();
	}

	public void resetSdkState() {
		prefs.edit().clear().apply();
		database.reset(appContext);
	}

	public void notifyInteractionUpdated(boolean successful) {
		Iterator it = interactionUpdateListeners.iterator();

		while (it.hasNext()) {
			InteractionManager.InteractionUpdateListener listener = (InteractionManager.InteractionUpdateListener) it.next();

			if (listener != null) {
				listener.onInteractionUpdated(successful);
			}
		}
	}

	public static PendingIntent prepareMessageCenterPendingIntent(Context context) {
		Intent intent;
		if (Apptentive.canShowMessageCenter()) {
			intent = new Intent();
			intent.setClass(context, ApptentiveViewActivity.class);
			intent.putExtra(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.ENGAGE_INTERNAL_EVENT);
			intent.putExtra(Constants.FragmentConfigKeys.EXTRA, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
		} else {
			intent = MessageCenterInteraction.generateMessageCenterErrorIntent(context);
		}
		return (intent != null) ? PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT) : null;
	}
}
