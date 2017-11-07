/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationManager;
import com.apptentive.android.sdk.debug.LogMonitor;
import com.apptentive.android.sdk.lifecycle.ApptentiveActivityLifecycleCallbacks;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.EventPayload;
import com.apptentive.android.sdk.model.LogoutPayload;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationCenter;
import com.apptentive.android.sdk.notifications.ApptentiveNotificationObserver;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.ApptentiveTaskManager;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.VersionHistoryItem;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ACTIVITY_RESUMED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_ACTIVITY_STARTED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_BACKGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_APP_ENTERED_FOREGROUND;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_AUTHENTICATION_FAILED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_WILL_LOGOUT;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_INTERACTIONS_FETCHED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_INTERACTIONS_SHOULD_DISMISS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_INTERACTION_MANIFEST_FETCHED;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_ACTIVITY;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_AUTHENTICATION_FAILED_REASON;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION_ID;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_MANIFEST;
import static com.apptentive.android.sdk.debug.Assert.assertNotNull;
import static com.apptentive.android.sdk.debug.Assert.assertTrue;
import static com.apptentive.android.sdk.util.Constants.CONVERSATIONS_DIR;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 */
public class ApptentiveInternal implements ApptentiveNotificationObserver {

	private final ApptentiveTaskManager taskManager;

	private final ApptentiveActivityLifecycleCallbacks lifecycleCallbacks;
	private final ApptentiveHttpClient apptentiveHttpClient;
	private final ConversationManager conversationManager;

	// These variables are initialized in Apptentive.register(), and so they are freely thereafter. If they are unexpectedly null, then if means the host app did not register Apptentive.
	private final Context appContext;

	// We keep a readonly reference to AppRelease object since it won't change at runtime
	private final AppRelease appRelease;

	private final SharedPreferences globalSharedPrefs;
	private final String apptentiveKey;
	private final String apptentiveSignature;
	private String serverUrl;
	private String appPackageName;

	// toolbar theme specified in R.attr.apptentiveToolbarTheme
	private Resources.Theme apptentiveToolbarTheme;

	// app default appcompat theme res id, if specified in app AndroidManifest
	private int appDefaultAppCompatThemeId;

	private int statusBarColorDefault;
	private String defaultAppDisplayName = "this app";
	// booleans to prevent starting multiple fetching asyncTasks simultaneously

	private IRatingProvider ratingProvider;
	private Map<String, String> ratingProviderArgs;
	private WeakReference<OnSurveyFinishedListener> onSurveyFinishedListener;

	private final LinkedBlockingQueue interactionUpdateListeners = new LinkedBlockingQueue();

	private WeakReference<Apptentive.AuthenticationFailedListener> authenticationFailedListenerRef = null;

	// Holds reference to the current foreground activity of the host app
	private WeakReference<Activity> currentTaskStackTopActivity;

	// Used for temporarily holding customData that needs to be sent on the next message the consumer sends.
	private Map<String, Object> customData;

	private static final String PUSH_ACTION = "action";
	private static final String PUSH_CONVERSATION_ID = "conversation_id";

	private enum PushAction {
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

	// for unit testing
	public ApptentiveInternal(Context appContext) {
		taskManager = null;
		globalSharedPrefs = null;
		apptentiveKey = null;
		apptentiveSignature = null;
		apptentiveHttpClient = null;
		conversationManager = null;
		this.appContext = appContext;
		appRelease = null;
		lifecycleCallbacks = null;
	}

	private ApptentiveInternal(Application application, String apptentiveKey, String apptentiveSignature, String serverUrl) {
		if (StringUtils.isNullOrEmpty(apptentiveKey)) {
			throw new IllegalArgumentException("Apptentive Key is null or empty");
		}

		if (StringUtils.isNullOrEmpty(apptentiveSignature)) {
			throw new IllegalArgumentException("Apptentive Signature is null or empty");
		}

		this.apptentiveKey = apptentiveKey;
		this.apptentiveSignature = apptentiveSignature;
		this.serverUrl = serverUrl;

		appContext = application.getApplicationContext();

		globalSharedPrefs = application.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		apptentiveHttpClient = new ApptentiveHttpClient(apptentiveKey, apptentiveSignature, getEndpointBase(globalSharedPrefs));
		conversationManager = new ConversationManager(appContext, Util.getInternalDir(appContext, CONVERSATIONS_DIR, true));

		appRelease = AppReleaseManager.generateCurrentAppRelease(application, this);
		taskManager = new ApptentiveTaskManager(appContext, apptentiveHttpClient);

		lifecycleCallbacks = new ApptentiveActivityLifecycleCallbacks();
		ApptentiveNotificationCenter.defaultCenter()
			.addObserver(NOTIFICATION_CONVERSATION_WILL_LOGOUT, this)
			.addObserver(NOTIFICATION_AUTHENTICATION_FAILED, this)
			.addObserver(NOTIFICATION_INTERACTION_MANIFEST_FETCHED, this);
	}

	public static boolean isApptentiveRegistered() {
		return sApptentiveInternal != null;
	}

	public static boolean isConversationActive() {
		return sApptentiveInternal != null && sApptentiveInternal.getConversation() != null;
	}

	/**
	 * Create a new or return a existing thread-safe instance of the Apptentive SDK. If this
	 * or any other {@link #getInstance()} has already been called in the application's lifecycle, the
	 * App key will be ignored and the current instance will be returned.
	 * <p/>
	 * This will be called from the application's onCreate(), before any other application objects have been
	 * created. Since the time spent in this function directly impacts the performance of starting the first activity,
	 * service, or receiver in the hosting app's process, the initialization of Apptentive is deferred to the first time
	 * {@link #getInstance()} is called.
	 *
	 * @param application the context of the app that is creating the instance
	 */
	static void createInstance(Application application, String apptentiveKey, String apptentiveSignature, final String serverUrl) {
		if (application == null) {
			throw new IllegalArgumentException("Application is null");
		}

		// try initializing log monitor
		LogMonitor.tryInitialize(application.getApplicationContext(), apptentiveKey, apptentiveSignature);

		synchronized (ApptentiveInternal.class) {
			if (sApptentiveInternal == null) {

				// trim spaces
				apptentiveKey = Util.trim(apptentiveKey);
				apptentiveSignature = Util.trim(apptentiveSignature);

				// if App key is not defined - try loading from AndroidManifest.xml
				if (StringUtils.isNullOrEmpty(apptentiveKey)) {
					apptentiveKey = Util.getManifestMetadataString(application, Constants.MANIFEST_KEY_APPTENTIVE_KEY);
					// TODO: check if Apptentive Key is still empty
				}

				// if App signature is not defined - try loading from AndroidManifest.xml
				if (StringUtils.isNullOrEmpty(apptentiveSignature)) {
					apptentiveSignature = Util.getManifestMetadataString(application, Constants.MANIFEST_KEY_APPTENTIVE_SIGNATURE);
					// TODO: check if Apptentive Signature is still empty
				}

				try {
					ApptentiveLog.v("Initializing Apptentive instance: apptentiveKey=%s apptentiveSignature=%s", apptentiveKey, apptentiveSignature);
					sApptentiveInternal = new ApptentiveInternal(application, apptentiveKey, apptentiveSignature, serverUrl);
					sApptentiveInternal.start(); // TODO: check the result of this call
					application.registerActivityLifecycleCallbacks(sApptentiveInternal.lifecycleCallbacks);
				} catch (Exception e) {
					ApptentiveLog.e(e, "Exception while initializing ApptentiveInternal instance");
				}
			} else {
				ApptentiveLog.w("Apptentive instance is already initialized");
			}
		}
	}

	/**
	 * Retrieve the existing instance of the Apptentive class. If {@link Apptentive#register(Application)} is
	 * not called prior to this, it will return null; Otherwise, it will return the singleton instance initialized.
	 *
	 * @return the existing instance of the Apptentive SDK fully initialized with API key, or null
	 */
	public static ApptentiveInternal getInstance() {
		synchronized (ApptentiveInternal.class) {
			return sApptentiveInternal;
		}
	}

	/**
	 * Use this method to set or clear the internal state (pass in null)
	 * Note: designed to be used for unit testing only
	 *
	 * @param instance the internal instance to be set to
	 */
	public static void setInstance(ApptentiveInternal instance) {
		sApptentiveInternal = instance;
	}

	/*
	 * Set default theme whom Apptentive UI will inherit theme attributes from. Apptentive will only
	 * inherit from an AppCompat theme
	 * @param themeResId : resource id of the theme style definition, such as R.style.MyAppTheme
	 * @return true if the theme is set for inheritance successfully.
	 */
	private boolean setApplicationDefaultTheme(int themeResId) {
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
	 *
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
		return appRelease.getVersionCode();
	}

	public String getApplicationVersionName() {
		return appRelease.getVersionName();
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
		final Conversation conversation = getConversation();
		return conversation != null ? conversation.getMessageManager() : null;
	}

	public ApptentiveTaskManager getApptentiveTaskManager() {
		return taskManager;
	}

	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	public Resources.Theme getApptentiveToolbarTheme() {
		return apptentiveToolbarTheme;
	}

	int getDefaultStatusBarColor() {
		return statusBarColorDefault;
	}

	public Conversation getConversation() {
		return conversationManager.getActiveConversation();
	}

	public String getApptentiveKey() {
		return apptentiveKey;
	}

	public String getApptentiveSignature() {
		return apptentiveSignature;
	}

	public String getServerUrl() {
		if (serverUrl == null) {
			return Constants.CONFIG_DEFAULT_SERVER_URL;
		}
		return serverUrl;
	}

	public String getDefaultAppDisplayName() {
		return defaultAppDisplayName;
	}

	public boolean isApptentiveDebuggable() {
		return appRelease.isDebug();
	}

	public SharedPreferences getGlobalSharedPrefs() {
		return globalSharedPrefs;
	}

	// TODO: remove app release from this class
	public AppRelease getAppRelease() {
		return appRelease;
	}

	public ApptentiveHttpClient getApptentiveHttpClient() {
		return apptentiveHttpClient;
	}

	public void onAppLaunch(final Context appContext) {
		if (isConversationActive()) {
			engageInternal(appContext, EventPayload.EventLabel.app__launch.getLabelName());
		}
	}

	public void onAppExit(final Context appContext) {
		if (isConversationActive()) {
			engageInternal(appContext, EventPayload.EventLabel.app__exit.getLabelName());
		}
	}

	public void onActivityStarted(Activity activity) {
		if (activity != null) {
			// Set current foreground activity reference whenever a new activity is started
			currentTaskStackTopActivity = new WeakReference<>(activity);

			// Post a notification
			ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_ACTIVITY_STARTED,
				NOTIFICATION_KEY_ACTIVITY, activity);
		}
	}

	public void onActivityResumed(Activity activity) {
		if (activity != null) {
			// Set current foreground activity reference whenever a new activity is started
			currentTaskStackTopActivity = new WeakReference<>(activity);

			// Post a notification
			ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_ACTIVITY_RESUMED,
				NOTIFICATION_KEY_ACTIVITY, activity);
		}
	}

	public void onAppEnterForeground() {

		// Try to initialize log monitor
		LogMonitor.tryInitialize(appContext, apptentiveKey, apptentiveSignature);

		// Post a notification
		ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_APP_ENTERED_FOREGROUND);
	}

	public void onAppEnterBackground() {
		currentTaskStackTopActivity = null;

		// Post a notification
		ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_APP_ENTERED_BACKGROUND);
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

	private boolean start() {
		boolean bRet = true;
		/* If Message Center feature has never been used before, don't initialize message polling thread.
		 * Message Center feature will be seen as used, if one of the following conditions has been met:
		 * 1. Message Center has been opened for the first time
		 * 2. The first Push is received which would open Message Center
		 * 3. An unreadMessageCountListener() is set up
		 */

		long start = System.currentTimeMillis();
		boolean conversationLoaded = conversationManager.loadActiveConversation(getApplicationContext());
		ApptentiveLog.i(CONVERSATION, "Active conversation is%s loaded. Took %d ms", conversationLoaded ? "" : " not", System.currentTimeMillis() - start);

		if (conversationLoaded) {
			Conversation activeConversation = conversationManager.getActiveConversation();
			// TODO: figure out if this is still necessary
			// boolean featureEverUsed = activeConversation.isMessageCenterFeatureUsed();
			// if (featureEverUsed) {
			// 	messageManager.init();
			// }
		}

		apptentiveToolbarTheme = appContext.getResources().newTheme();

		boolean apptentiveDebug = false;
		String logLevelOverride = null;
		try {
			appPackageName = appContext.getPackageName();
			PackageManager packageManager = appContext.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			ApplicationInfo ai = packageInfo.applicationInfo;

			Bundle metaData = ai.metaData;
			if (metaData != null) {
				logLevelOverride = Util.trim(metaData.getString(Constants.MANIFEST_KEY_APPTENTIVE_LOG_LEVEL));
				apptentiveDebug = metaData.getBoolean(Constants.MANIFEST_KEY_APPTENTIVE_DEBUG);
			}

			// Used for application theme inheritance if the theme is an AppCompat theme.
			setApplicationDefaultTheme(ai.theme);

			Conversation conversation = getConversation();
			if (conversation != null) {
				checkSendVersionChanges(conversation);
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
			ApptentiveLog.e(e, "Unexpected error while reading application or package info.");
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
			if (appRelease.isDebug()) {
				setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
			}
		}
		ApptentiveLog.i("Debug mode enabled? %b", appRelease.isDebug());

		// The app key can be passed in programmatically, or we can fallback to checking in the manifest.
		if (TextUtils.isEmpty(apptentiveKey) || apptentiveKey.contains(Constants.EXAMPLE_APPTENTIVE_KEY_VALUE)) {
			String errorMessage = "The Apptentive Key is not defined. You may provide your Apptentive Key in Apptentive.register(), or in as meta-data in your AndroidManifest.xml.\n" +
				                      "<meta-data android:name=\"apptentive_key\"\n" +
				                      "           android:value=\"@string/your_apptentive_key\"/>";
			if (appRelease.isDebug()) {
				throw new RuntimeException(errorMessage);
			} else {
				ApptentiveLog.e(errorMessage);
			}
		} else {
			ApptentiveLog.d("Using cached Apptentive App Key");
		}
		ApptentiveLog.d("Apptentive App Key: %s", apptentiveKey);

		// The app signature can be passed in programmatically, or we can fallback to checking in the manifest.
		if (TextUtils.isEmpty(apptentiveSignature) || apptentiveSignature.contains(Constants.EXAMPLE_APPTENTIVE_SIGNATURE_VALUE)) {
			String errorMessage = "The Apptentive Signature is not defined. You may provide your Apptentive Signature in Apptentive.register(), or in as meta-data in your AndroidManifest.xml.\n" +
				                      "<meta-data android:name=\"apptentive_signature\"\n" +
				                      "           android:value=\"@string/your_apptentive_signature\"/>";
			if (appRelease.isDebug()) {
				throw new RuntimeException(errorMessage);
			} else {
				ApptentiveLog.e(errorMessage);
			}
		} else {
			ApptentiveLog.d("Using cached Apptentive App Signature");
		}
		ApptentiveLog.d("Apptentive App Signature: %s", apptentiveSignature);

		// Grab app info we need to access later on.
		ApptentiveLog.d("Default Locale: %s", Locale.getDefault().toString());
		return bRet;
	}

	private void checkSendVersionChanges(Conversation conversation) {
		if (conversation == null) {
			ApptentiveLog.e("Can't check session data changes: session data is not initialized");
			return;
		}

		boolean appReleaseChanged = false;
		boolean sdkChanged = false;

		final VersionHistoryItem lastVersionItemSeen = conversation.getVersionHistory().getLastVersionSeen();
		final int currentVersionCode = appRelease.getVersionCode();
		final String currentVersionName = appRelease.getVersionName();

		Integer previousVersionCode = null;
		String previousVersionName = null;

		if (lastVersionItemSeen == null) {
			appReleaseChanged = true;
		} else {
			previousVersionCode = lastVersionItemSeen.getVersionCode();
			Apptentive.Version lastSeenVersionNameVersion = new Apptentive.Version();

			previousVersionName = lastVersionItemSeen.getVersionName();
			lastSeenVersionNameVersion.setVersion(previousVersionName);
			if (!(currentVersionCode == previousVersionCode) || !currentVersionName.equals(lastSeenVersionNameVersion.getVersion())) {
				appReleaseChanged = true;
			}
		}

		// TODO: Move this into a session became active handler.
		final String lastSeenSdkVersion = conversation.getLastSeenSdkVersion();
		final String currentSdkVersion = Constants.APPTENTIVE_SDK_VERSION;
		if (!TextUtils.equals(lastSeenSdkVersion, currentSdkVersion)) {
			sdkChanged = true;
		}

		if (appReleaseChanged) {
			ApptentiveLog.i("Version changed: Name: %s => %s, Code: %d => %d", previousVersionName, currentVersionName, previousVersionCode, currentVersionCode);
			conversation.getVersionHistory().updateVersionHistory(Util.currentTimeSeconds(), currentVersionCode, currentVersionName);
		}

		Sdk sdk = SdkManager.generateCurrentSdk(appContext);
		if (sdkChanged) {
			ApptentiveLog.i("SDK version changed: %s => %s", lastSeenSdkVersion, currentSdkVersion);
			conversation.setLastSeenSdkVersion(currentSdkVersion);
			conversation.setSdk(sdk);
		}

		if (appReleaseChanged || sdkChanged) {
			conversation.addPayload(AppReleaseManager.getPayload(sdk, appRelease));
			invalidateCaches();
		}
	}

	/**
	 * We want to make sure the app is using the latest configuration from the server if the app or sdk version changes.
	 */
	private void invalidateCaches() {
		Conversation conversation = getConversation();
		if (conversation != null) {
			conversation.setInteractionExpiration(0L);
		}
		Configuration config = Configuration.load();
		config.setConfigurationCacheExpirationMillis(System.currentTimeMillis());
		config.save();
	}

	public IRatingProvider getRatingProvider() {
		if (ratingProvider == null) {
			ratingProvider = new GooglePlayRatingProvider();
		}
		return ratingProvider;
	}

	void setRatingProvider(IRatingProvider ratingProvider) {
		this.ratingProvider = ratingProvider;
	}

	public Map<String, String> getRatingProviderArgs() {
		return ratingProviderArgs;
	}

	void putRatingProviderArg(String key, String value) {
		if (ratingProviderArgs == null) {
			ratingProviderArgs = new HashMap<>();
		}
		ratingProviderArgs.put(key, value);
	}

	void setOnSurveyFinishedListener(OnSurveyFinishedListener onSurveyFinishedListener) {
		if (onSurveyFinishedListener != null) {
			this.onSurveyFinishedListener = new WeakReference<>(onSurveyFinishedListener);
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

	public void setAuthenticationFailedListener(Apptentive.AuthenticationFailedListener listener) {
		authenticationFailedListenerRef = new WeakReference<>(listener);
	}

	public void notifyAuthenticationFailedListener(Apptentive.AuthenticationFailedReason reason, String conversationIdOfFailedRequest) {
		if (isConversationActive()) {
			String activeConversationId = getConversation().getConversationId();
			if (StringUtils.equal(activeConversationId, conversationIdOfFailedRequest)) {
				Apptentive.AuthenticationFailedListener listener = authenticationFailedListenerRef != null ? authenticationFailedListenerRef.get() : null;
				if (listener != null) {
					listener.onAuthenticationFailed(reason);
				}
			}
		}
	}

	/**
	 * Pass in a log level to override the default, which is {@link ApptentiveLog.Level#INFO}
	 */
	private void setMinimumLogLevel(ApptentiveLog.Level level) {
		ApptentiveLog.overrideLogLevel(level);
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

	/**
	 * TODO: Decouple this from Conversation and Message Manager so it can be unit tested.
	 */
	static PendingIntent generatePendingIntentFromApptentivePushData(String apptentivePushData) {
		ApptentiveLog.d("Generating Apptentive push PendingIntent.");
		if (!TextUtils.isEmpty(apptentivePushData)) {
			try {
				JSONObject pushJson = new JSONObject(apptentivePushData);

				// we need to check if current user is actually the receiver of this notification
				final String conversationId = pushJson.optString(PUSH_CONVERSATION_ID, null);
				if (conversationId != null) {
					final Conversation conversation = ApptentiveInternal.getInstance().getConversation();

					// do we have a conversation right now?
					if (conversation == null) {
						ApptentiveLog.w("Can't generate pending intent from Apptentive push data: no active conversation");
						return null;
					}

					// is it an actual receiver?
					if (!StringUtils.equal(conversation.getConversationId(), conversationId)) {
						ApptentiveLog.i("Can't generate pending intent from Apptentive push data: push conversation id doesn't match active conversation");
						return null;
					}
				}

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
				ApptentiveLog.e(e, "Error parsing JSON from push notification.");
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
			interactionShown = engageInternal(context, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
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

	// TODO: remove this method
	public boolean canShowMessageCenterInternal() {
		Conversation conversation = getConversation();
		return conversation != null && canShowMessageCenterInternal(conversation);
	}

	public boolean canShowMessageCenterInternal(Conversation conversation) {
		return EngagementModule.canShowInteraction(conversation, "app", MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME, "com.apptentive");
	}

	public Map<String, Object> getAndClearCustomData() {
		Map<String, Object> customData = this.customData;
		this.customData = null;
		return customData;
	}

	public void resetSdkState() {
		globalSharedPrefs.edit().clear().apply();
		taskManager.reset(appContext);
	}

	public void notifyInteractionUpdated(boolean successful) {
		ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_INTERACTIONS_FETCHED);
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

	//region Helpers

	private String getEndpointBase(SharedPreferences prefs) {
		String url = prefs.getString(Constants.PREF_KEY_SERVER_URL, null);
		if (url == null) {
			url = Constants.CONFIG_DEFAULT_SERVER_URL;
			prefs.edit().putString(Constants.PREF_KEY_SERVER_URL, url).apply();
		}
		return url;
	}

	//endregion

	//region Login/Logout

	/**
	 * Flag indicating if login request is currently active (used to avoid multiple competing
	 * requests
	 */
	private boolean loginInProgress;

	/**
	 * Mutex object for synchronizing login request flag
	 */
	private final Object loginMutex = new Object();

	void login(String token, final LoginCallback callback) {
		synchronized (loginMutex) {
			if (loginInProgress) {
				if (callback != null) {
					callback.onLoginFail("Another login request is currently in progress");
				}
				return;
			}

			loginInProgress = true;

			LoginCallback wrapperCallback = new LoginCallback() {
				@Override
				public void onLoginFinish() {
					synchronized (loginMutex) {
						assertTrue(loginInProgress);
						try {
							engageInternal(getApplicationContext(), "login");
							if (callback != null) {
								callback.onLoginFinish();
							}
						} finally {
							loginInProgress = false;
						}
					}
				}

				@Override
				public void onLoginFail(String errorMessage) {
					synchronized (loginMutex) {
						assertTrue(loginInProgress);
						try {
							if (callback != null) {
								callback.onLoginFail(errorMessage);
							}
						} finally {
							loginInProgress = false;
						}
					}
				}
			};

			conversationManager.login(token, wrapperCallback);
		}
	}

	void logout() {
		conversationManager.logout();
	}

	//endregion

	/**
	 * Dismisses any currently-visible interactions. This method is for internal use and is subject to change.
	 */
	public static void dismissAllInteractions() {
		ApptentiveNotificationCenter.defaultCenter().postNotification(NOTIFICATION_INTERACTIONS_SHOULD_DISMISS);
	}

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		if (notification.hasName(NOTIFICATION_CONVERSATION_WILL_LOGOUT)) {
			Conversation conversation = notification.getRequiredUserInfo(NOTIFICATION_KEY_CONVERSATION, Conversation.class);
			conversation.addPayload(new LogoutPayload());
		} else if (notification.hasName(NOTIFICATION_AUTHENTICATION_FAILED)) {
			String conversationIdOfFailedRequest = notification.getUserInfo(NOTIFICATION_KEY_CONVERSATION_ID, String.class);
			Apptentive.AuthenticationFailedReason authenticationFailedReason = notification.getUserInfo(NOTIFICATION_KEY_AUTHENTICATION_FAILED_REASON, Apptentive.AuthenticationFailedReason.class);
			notifyAuthenticationFailedListener(authenticationFailedReason, conversationIdOfFailedRequest);
		} else if (notification.hasName(NOTIFICATION_INTERACTION_MANIFEST_FETCHED)) {
			String manifest = notification.getRequiredUserInfo(NOTIFICATION_KEY_MANIFEST, String.class);
			storeManifestResponse(appContext, manifest);
		}
	}

	//region Engagement

	private boolean engageInternal(Context context, String eventName) {
		Conversation conversation = getConversation();
		assertNotNull(conversation, "Attempted to engage '%s' internal event without an active conversation", eventName);
		return conversation != null && EngagementModule.engageInternal(context, conversation, eventName);
	}

	//endregion

	//region Engagement Manifest Data

	private void storeManifestResponse(Context context, String manifest) {
		try {
			File file = new File(context.getCacheDir(), Constants.FILE_APPTENTIVE_ENGAGEMENT_MANIFEST);
			Util.writeText(file, manifest);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while trying to save engagement manifest data");
		}
	}

	//endregion
}
