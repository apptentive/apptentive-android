/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.lifecycle.ApptentiveActivityLifecycleCallbacks;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.ConversationTokenRequest;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.ApptentiveTaskManager;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.PayloadSendWorker;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.SessionData;
import com.apptentive.android.sdk.storage.legacy.VersionHistoryEntry;
import com.apptentive.android.sdk.storage.legacy.VersionHistoryStore;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
public class ApptentiveInternal implements Handler.Callback {

	static AtomicBoolean isApptentiveInitialized = new AtomicBoolean(false);
	InteractionManager interactionManager;
	MessageManager messageManager;
	PayloadSendWorker payloadWorker;
	ApptentiveTaskManager taskManager;

	ApptentiveActivityLifecycleCallbacks lifecycleCallbacks;

	// These variables are initialized in Apptentive.register(), and so they are freely thereafter. If they are unexpectedly null, then if means the host app did not register Apptentive.
	Context appContext;
	int currentVersionCode;
	String currentVersionName;

	boolean appIsInForeground;
	boolean isAppDebuggable;
	SharedPreferences prefs;
	String apiKey;
	String personId;
	String androidId;
	String appPackageName;
	SessionData sessionData;

	Handler backgroundHandler;

	// toolbar theme specified in R.attr.apptentiveToolbarTheme
	Resources.Theme apptentiveToolbarTheme;

	// app default appcompat theme res id, if specified in app AndroidManifest
	int appDefaultAppCompatThemeId;

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

	// Holds reference to the current foreground activity of the host app
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

	@SuppressLint("StaticFieldLeak")
	private static volatile ApptentiveInternal sApptentiveInternal;


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
					ApptentiveTaskManager worker = new ApptentiveTaskManager(sApptentiveInternal.appContext);

					sApptentiveInternal.messageManager = msgManager;
					sApptentiveInternal.payloadWorker = payloadWorker;
					sApptentiveInternal.interactionManager = interactionMgr;
					sApptentiveInternal.taskManager = worker;
					sApptentiveInternal.cachedExecutor = Executors.newCachedThreadPool();
					sApptentiveInternal.apiKey = Util.trim(apptentiveApiKey);


					HandlerThread handlerThread = new HandlerThread("ApptentiveInternalHandlerThread");
					handlerThread.start();
					sApptentiveInternal.backgroundHandler = new Handler(handlerThread.getLooper(), sApptentiveInternal);

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
					if (!sApptentiveInternal.init()) {
						ApptentiveLog.e("Apptentive init() failed");
					}
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
	 * callbacks, only if the callback hasn't been set yet.
	 */
	static void setLifeCycleCallback() {
		if (sApptentiveInternal != null && sApptentiveInternal.lifecycleCallbacks == null) {
			synchronized (ApptentiveInternal.class) {
				if (sApptentiveInternal != null && sApptentiveInternal.lifecycleCallbacks == null &&
					sApptentiveInternal.appContext instanceof Application) {
					sApptentiveInternal.lifecycleCallbacks = new ApptentiveActivityLifecycleCallbacks();
					((Application) sApptentiveInternal.appContext).registerActivityLifecycleCallbacks(sApptentiveInternal.lifecycleCallbacks);
				}
			}
		}
	}


	/*
	 * Set default theme whom Apptentive UI will inherit theme attributes from. Apptentive will only
	 * inherit from an AppCompat theme
	 * @param themeResId : resource id of the theme style definition, such as R.style.MyAppTheme
	 * @return true if the theme is set for inheritance successfully.
	 */
	public boolean setApplicationDefaultTheme(int themeResId) {
		try {
			if (themeResId != 0) {
				// If passed theme res id does not exist, an exception would be thrown and caught
				appContext.getResources().getResourceName(themeResId);

				// Check if the theme to be inherited from is an AppCompat theme.
				Resources.Theme appDefaultTheme = appContext.getResources().newTheme();
				appDefaultTheme.applyStyle(themeResId, true);

				TypedArray a = appDefaultTheme.obtainStyledAttributes(android.support.v7.appcompat.R.styleable.AppCompatTheme);
				try {
					if (a.hasValue(android.support.v7.appcompat.R.styleable.AppCompatTheme_colorPrimaryDark)) {
						// Only set to use if it's an AppCompat theme. See updateApptentiveInteractionTheme() for theme inheritance chain
						appDefaultAppCompatThemeId = themeResId;
						return true;
					}
				} finally {
					a.recycle();
				}
			}
		} catch (Resources.NotFoundException e) {
			ApptentiveLog.e("Theme Res id not found");
		}
		return false;
	}

	/**
	 * Must be called after {@link ApptentiveInternal#setApplicationDefaultTheme(int)}
	 * @return true it the app is using an AppCompat theme
	 */
	public boolean isAppUsingAppCompatTheme() {
		return appDefaultAppCompatThemeId != 0;
	}

	// Object getter methods reqiure an instance. Get an instance with ApptentiveInternal.getInstance()

	public Context getApplicationContext() {
		return appContext;
	}

	public int getApplicationVersionCode() {
		return currentVersionCode;
	}

	public String getApplicationVersionName() {
		return currentVersionName;
	}

	public ApptentiveActivityLifecycleCallbacks getRegisteredLifecycleCallbacks() {
		return lifecycleCallbacks;
	}


	/* Get the foreground activity from the current application, i.e. at the top of the task
	 * It is tracked through {@link #onActivityStarted(Activity)} and {@link #onActivityStopped(Activity)}
	 *
	 * If Apptentive interaction is to be launched from a non-activity context, use the current activity at
	 * the top of the task stack, i.e. the foreground activity.
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

	public ApptentiveTaskManager getApptentiveTaskManager() {
		return taskManager;
	}

	public Resources.Theme getApptentiveToolbarTheme() {
		return apptentiveToolbarTheme;
	}

	public int getDefaultStatusBarColor() {
		return statusBarColorDefault;
	}

	public SessionData getSessionData() {
		return sessionData;
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

	public void runOnWorkerThread(Runnable r) {
		cachedExecutor.execute(r);
	}

	public void scheduleOnWorkerThread(Runnable r) {
		cachedExecutor.submit(r);
	}

	public void checkAndUpdateApptentiveConfigurations() {
		// Initialize the Conversation Token, or fetch if needed. Fetch config it the token is available.
		if (sessionData == null) {
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
			// Set current foreground activity reference whenever a new activity is started
			currentTaskStackTopActivity = new WeakReference<Activity>(activity);
			messageManager.setCurrentForegroundActivity(activity);
		}

		//FIXME: Kick this off on Application initialization instead.
		checkAndUpdateApptentiveConfigurations();
	}

	public void onActivityResumed(Activity activity) {
		if (activity != null) {
			// Set current foreground activity reference whenever a new activity is started
			currentTaskStackTopActivity = new WeakReference<Activity>(activity);
			messageManager.setCurrentForegroundActivity(activity);
		}

	}

	public void onAppEnterForeground() {
		appIsInForeground = true;
		payloadWorker.appWentToForeground();
		messageManager.appWentToForeground();
	}

	public void onAppEnterBackground() {
		appIsInForeground = false;
		currentTaskStackTopActivity = null;
		messageManager.setCurrentForegroundActivity(null);
		payloadWorker.appWentToBackground();
		messageManager.appWentToBackground();
	}

	/* Apply Apptentive styling layers to the theme to be used by interaction. The layers include
	 * Apptentive defaults, and app/activity theme inheritance and app specific overrides.
	 *
	 * When the Apptentive fragments are hosted by ApptentiveViewActivity(by default), the value of theme attributes
	 * are obtained in the following order: ApptentiveTheme.Base.Versioned specified in Apptentive's AndroidManifest.xml ->
	 * app default theme specified in app AndroidManifest.xml (force) -> ApptentiveThemeOverride (force)
	 *
	 * @param interactionTheme The base theme to apply Apptentive styling layers
	 * @param context The context that will host Apptentive interaction fragment, either ApptentiveViewActivity
	 *                or application context
	 */
	public void updateApptentiveInteractionTheme(Resources.Theme interactionTheme, Context context) {
		/* Step 1: Apply Apptentive default theme layer.
		 * If host activity is an activity, the base theme already has Apptentive defaults applied, so skip Step 1.
		 * If parent activity is NOT an activity, first apply Apptentive defaults.
		 */
		if (!(context instanceof Activity)) {
			// If host context is not an activity, i.e. application context, treat it as initial theme setup
			interactionTheme.applyStyle(R.style.ApptentiveTheme_Base_Versioned, true);
		}

		// Step 2: Inherit app default appcompat theme if there is one specified in app's AndroidManifest
		if (appDefaultAppCompatThemeId != 0) {
			interactionTheme.applyStyle(appDefaultAppCompatThemeId, true);
		}

		// Step 3: Restore Apptentive UI window properties that may have been overridden in Step 2. This theme
		// is to ensure Apptentive interaction has a modal feel-n-look.
		interactionTheme.applyStyle(R.style.ApptentiveBaseFrameTheme, true);

		// Step 4: Apply optional theme override specified in host app's style
		int themeOverrideResId = context.getResources().getIdentifier("ApptentiveThemeOverride",
			"style", getApplicationContext().getPackageName());
		if (themeOverrideResId != 0) {
			interactionTheme.applyStyle(themeOverrideResId, true);
		}

		// Step 5: Update status bar color
		/* Obtain the default status bar color. When an Apptentive Modal interaction is shown,
		*  a translucent overlay would be applied on top of statusBarColorDefault
		*/
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int transparentColor = ContextCompat.getColor(context, android.R.color.transparent);
			TypedArray a = interactionTheme.obtainStyledAttributes(new int[]{android.R.attr.statusBarColor});
			try {
				statusBarColorDefault = a.getColor(0, transparentColor);
			} finally {
				a.recycle();
			}
		}

		// Step 6: Update toolbar overlay theme
		int toolbarThemeId = Util.getResourceIdFromAttribute(interactionTheme, R.attr.apptentiveToolbarTheme);
		apptentiveToolbarTheme.setTo(interactionTheme);
		apptentiveToolbarTheme.applyStyle(toolbarThemeId, true);
	}

	public boolean init() {
		boolean bRet = true;
		/* If Message Center feature has never been used before, don't initialize message polling thread.
		 * Message Center feature will be seen as used, if one of the following conditions has been met:
		 * 1. Message Center has been opened for the first time
		 * 2. The first Push is received which would open Message Center
		 * 3. An unreadMessageCountListener() is set up
		 */

		FileSerializer fileSerializer = new FileSerializer(new File(appContext.getFilesDir(), "apptentive/SessionData.ser"));
		sessionData = (SessionData) fileSerializer.deserialize();
		if (sessionData != null) {
			ApptentiveLog.d("Restored existing SessionData");
			ApptentiveLog.v("Restored EventData: %s", sessionData.getEventData());
			// FIXME: Move this to whereever the sessions first comes online?
			boolean featureEverUsed = sessionData != null && sessionData.isMessageCenterFeatureUsed();
			if (featureEverUsed) {
				messageManager.init();
			}
		}

		apptentiveToolbarTheme = appContext.getResources().newTheme();

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
			}

			// Used for application theme inheritance if the theme is an AppCompat theme.
			setApplicationDefaultTheme(ai.theme);

			AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(appContext);

			isAppDebuggable = appRelease.isDebug();
			currentVersionCode = appRelease.getVersionCode();
			currentVersionName = appRelease.getVersionName();

			VersionHistoryEntry lastVersionEntrySeen = VersionHistoryStore.getLastVersionSeen();

			if (lastVersionEntrySeen == null) {
				onVersionChanged(null, currentVersionCode, null, currentVersionName, appRelease);
			} else {
				int lastSeenVersionCode = lastVersionEntrySeen.getVersionCode();
				Apptentive.Version lastSeenVersionNameVersion = new Apptentive.Version();
				lastSeenVersionNameVersion.setVersion(lastVersionEntrySeen.getVersionName());
				if (!(currentVersionCode == lastSeenVersionCode) || !currentVersionName.equals(lastSeenVersionNameVersion.getVersion())) {
					onVersionChanged(lastVersionEntrySeen.getVersionCode(), currentVersionCode, lastVersionEntrySeen.getVersionName(), currentVersionName, appRelease);
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
			bRet = false;
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

		String lastSeenSdkVersion = sessionData.getLastSeenSdkVersion();
		if (!TextUtils.equals(lastSeenSdkVersion, Constants.APPTENTIVE_SDK_VERSION)) {
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
		return bRet;
	}

	// FIXME: Do this kind of thing when a session becomes active instead of at app initialization? Otherwise there is no active session to send the information to.
	private void onVersionChanged(Integer previousVersionCode, Integer currentVersionCode, String previousVersionName, String currentVersionName, AppRelease currentAppRelease) {
		ApptentiveLog.i("Version changed: Name: %s => %s, Code: %d => %d", previousVersionName, currentVersionName, previousVersionCode, currentVersionCode);
		VersionHistoryStore.updateVersionHistory(currentVersionCode, currentVersionName);
		if (previousVersionCode != null) {
			SessionData sessionData = ApptentiveInternal.getInstance().getSessionData();
			taskManager.addPayload(AppReleaseManager.getPayload(currentAppRelease));
			if (sessionData != null) {
				sessionData.setAppRelease(currentAppRelease);
			}
		}
		invalidateCaches();
	}

	// FIXME: Do this kind of thing when a session becomes active instead of at app initialization? Otherwise there is no active session to send the information to.
	private void onSdkVersionChanged(Context context, String previousSdkVersion, String currentSdkVersion) {
		ApptentiveLog.i("SDK version changed: %s => %s", previousSdkVersion, currentSdkVersion);
		Sdk sdk = SdkManager.generateCurrentSdk();
		taskManager.addPayload(SdkManager.getPayload(sdk));
		if (sessionData != null) {
			sessionData.setLastSeenSdkVersion(currentSdkVersion);
			sessionData.setSdk(sdk);
		}
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
					try {
						return fetchConversationToken();
					} catch (Exception e) {
						// Hold onto the unhandled exception from fetchConversationToken() for later handling in UI thread
						this.e = e;
					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean successful) {
					if (e == null) {
						// Update pending state on UI thread after finishing the task
						ApptentiveLog.d("Fetching conversation token asyncTask finished. Successful? %b", successful);
						isConversationTokenFetchPending.set(false);
						if (successful) {
							// Once token is fetched successfully, start asyncTasks to fetch global configuration, then interaction
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
		Device device = DeviceManager.generateNewDevice();
		Sdk sdk = SdkManager.generateCurrentSdk();
		AppRelease appRelease = AppReleaseManager.generateCurrentAppRelease(appContext);

		request.setDevice(DeviceManager.getDiffPayload(null, device));
		request.setSdk(SdkManager.getPayload(sdk));
		request.setAppRelease(AppReleaseManager.getPayload(appRelease));

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

				sessionData = new SessionData();
				if (conversationToken != null && !conversationToken.equals("")) {
					sessionData.setConversationToken(conversationToken);
					sessionData.setConversationId(conversationId);
					sessionData.setDevice(device);
					sessionData.setSdk(sdk);
					sessionData.setAppRelease(appRelease);
					saveSessionData();
				}
				String personId = root.getString("person_id");
				ApptentiveLog.d("PersonId: " + personId);
				sessionData.setPersonId(personId);
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

	static final String PUSH_EXTRA_KEY_PARSE = "com.parse.Data";
	static final String PUSH_EXTRA_KEY_UA = "com.urbanairship.push.EXTRA_PUSH_MESSAGE_BUNDLE";

	static final String TITLE_DEFAULT = "title";
	static final String BODY_DEFAULT = "body";
	static final String BODY_PARSE = "alert";
	static final String BODY_UA = "com.urbanairship.push.ALERT";


	static String getApptentivePushNotificationData(Intent intent) {
		if (intent != null) {
			ApptentiveLog.v("Got an Intent.");
			return getApptentivePushNotificationData(intent.getExtras());
		}
		return null;
	}

	/**
	 * <p>Internal use only.</p>
	 * This bundle could be any bundle sent to us by a push Intent from any supported platform. For that reason, it needs to be checked in multiple ways.
	 *
	 * @param pushBundle a Bundle, or null.
	 * @return a String, or null.
	 */
	static String getApptentivePushNotificationData(Bundle pushBundle) {
		if (pushBundle != null) {
			if (pushBundle.containsKey(PUSH_EXTRA_KEY_PARSE)) { // Parse
				ApptentiveLog.v("Got a Parse Push.");
				String parseDataString = pushBundle.getString(PUSH_EXTRA_KEY_PARSE);
				if (parseDataString == null) {
					ApptentiveLog.e("com.parse.Data is null.");
					return null;
				}
				try {
					JSONObject parseJson = new JSONObject(parseDataString);
					return parseJson.optString(APPTENTIVE_PUSH_EXTRA_KEY, null);
				} catch (JSONException e) {
					ApptentiveLog.e("com.parse.Data is corrupt: %s", parseDataString);
					return null;
				}
			} else if (pushBundle.containsKey(PUSH_EXTRA_KEY_UA)) { // Urban Airship
				ApptentiveLog.v("Got an Urban Airship push.");
				Bundle uaPushBundle = pushBundle.getBundle(PUSH_EXTRA_KEY_UA);
				if (uaPushBundle == null) {
					ApptentiveLog.e("Urban Airship push extras bundle is null");
					return null;
				}
				return uaPushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY);
			} else if (pushBundle.containsKey(APPTENTIVE_PUSH_EXTRA_KEY)) { // All others
				// Straight FCM / GCM / SNS, or nested
				ApptentiveLog.v("Found apptentive push data.");
				return pushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY);
			} else {
				ApptentiveLog.e("Got an unrecognizable push.");
			}
		}
		ApptentiveLog.e("Push bundle was null.");
		return null;
	}

	static String getApptentivePushNotificationData(Map<String, String> pushData) {
		if (pushData != null) {
			return pushData.get(APPTENTIVE_PUSH_EXTRA_KEY);
		}
		return null;
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

	static PendingIntent generatePendingIntentFromApptentivePushData(String apptentivePushData) {
		ApptentiveLog.d("Generating Apptentive push PendingIntent.");
		if (!TextUtils.isEmpty(apptentivePushData)) {
			try {
				JSONObject pushJson = new JSONObject(apptentivePushData);
				ApptentiveInternal.PushAction action = ApptentiveInternal.PushAction.unknown;
				if (pushJson.has(ApptentiveInternal.PUSH_ACTION)) {
					action = ApptentiveInternal.PushAction.parse(pushJson.getString(ApptentiveInternal.PUSH_ACTION));
				}
				switch (action) {
					case pmc: {
						// Prefetch message when push for message center is received
						MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();
						if (mgr != null) {
							mgr.startMessagePreFetchTask();
						}
						// Construct a pending intent to launch message center
						return ApptentiveInternal.prepareMessageCenterPendingIntent(ApptentiveInternal.getInstance().getApplicationContext());
					}
					default:
						ApptentiveLog.w("Unknown Apptentive push notification action: \"%s\"", action.name());
				}
			} catch (JSONException e) {
				ApptentiveLog.e("Error parsing JSON from push notification.", e);
				MetricModule.sendError(e, "Parsing Apptentive Push", apptentivePushData);
			}
		}
		return null;
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

	public void resetSdkState() {
		prefs.edit().clear().apply();
		taskManager.reset(appContext);
		VersionHistoryStore.clear();
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

	/**
	 * Checks to see if Apptentive was properly registered, and logs a message if not.
	 *
	 * @return true if properly registered, else false.
	 */
	public static boolean checkRegistered() {
		if (!ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveLog.e("Error: You have failed to call Apptentive.register() in your Application.onCreate()");
			return false;
		}
		return true;
	}


	// Multi-tenancy work

	public void saveSessionData() {
		if (!backgroundHandler.hasMessages(MESSAGE_SAVE_SESSION_DATA)) {
			backgroundHandler.sendEmptyMessageDelayed(MESSAGE_SAVE_SESSION_DATA, 100);
		}
	}

	private static final int MESSAGE_SAVE_SESSION_DATA = 0;
	private static final int MESSAGE_CREATE_CONVERSATION = 1;

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_SAVE_SESSION_DATA:
				ApptentiveLog.d("Saving SessionData");
				ApptentiveLog.v("EventData: %s", sessionData.getEventData().toString());

				File internalStorage = appContext.getFilesDir();
				File sessionDataFile = new File(internalStorage, "apptentive/SessionData.ser");
				try {
					sessionDataFile.getParentFile().mkdirs();
				} catch (SecurityException e) {
					ApptentiveLog.e("Security Error creating DataSession file.", e);
				}
				FileSerializer fileSerializer = new FileSerializer(sessionDataFile);
				fileSerializer.serialize(sApptentiveInternal.sessionData);
				ApptentiveLog.d("Session data written to file of length: %s", Util.humanReadableByteCount(sessionDataFile.length(), false));
				break;
			case MESSAGE_CREATE_CONVERSATION:
				// TODO: This.
				break;
		}
		return false;
	}
}
