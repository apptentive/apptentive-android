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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.lifecycle.ApptentiveActivityLifecycleCallbacks;
import com.apptentive.android.sdk.listeners.OnUserLogOutListener;
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
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.AppReleaseManager;
import com.apptentive.android.sdk.storage.ApptentiveTaskManager;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.PayloadSendWorker;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SdkManager;
import com.apptentive.android.sdk.storage.VersionHistoryItem;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.registry.ApptentiveComponentRegistry;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchQueueType;
import com.apptentive.android.sdk.util.threading.DispatchTask;

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

import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.debug.Tester.*;
import static com.apptentive.android.sdk.debug.TesterEvent.*;
import static com.apptentive.android.sdk.util.registry.ApptentiveComponentRegistry.ComponentNotifier;

/**
 * This class contains only internal methods. These methods should not be access directly by the host app.
 */
public class ApptentiveInternal implements DataChangedListener {

	static AtomicBoolean isApptentiveInitialized = new AtomicBoolean(false);
	private InteractionManager interactionManager;
	private final MessageManager messageManager;
	private final PayloadSendWorker payloadWorker;
	private final ApptentiveTaskManager taskManager;

	ApptentiveActivityLifecycleCallbacks lifecycleCallbacks;
	private final ApptentiveComponentRegistry componentRegistry;
	private final ApptentiveHttpClient apptentiveHttpClient;

	// These variables are initialized in Apptentive.register(), and so they are freely thereafter. If they are unexpectedly null, then if means the host app did not register Apptentive.
	private final Context appContext;

	// We keep a readonly reference to AppRelease object since it won't change at runtime
	private final AppRelease appRelease;

	boolean appIsInForeground;
	private final SharedPreferences globalSharedPrefs;
	private final String apiKey;
	String personId;
	String androidId;
	String appPackageName;
	Conversation conversation;
	private FileSerializer fileSerializer;

	// private background serial dispatch queue for internal SDK tasks
	private final DispatchQueue backgroundQueue;

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

	private final ExecutorService cachedExecutor;

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

	private ApptentiveInternal(Context context, String apiKey) {
		this.apiKey = apiKey;

		appContext = context.getApplicationContext();

		globalSharedPrefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		backgroundQueue = DispatchQueue.createBackgroundQueue("Apptentive Serial Queue", DispatchQueueType.Serial);
		componentRegistry = new ApptentiveComponentRegistry();
		apptentiveHttpClient = new ApptentiveHttpClient(apiKey, getEndpointBase(globalSharedPrefs));

		appRelease = AppReleaseManager.generateCurrentAppRelease(context, this);
		messageManager = new MessageManager();
		payloadWorker = new PayloadSendWorker();
		taskManager = new ApptentiveTaskManager(appContext);
		cachedExecutor = Executors.newCachedThreadPool();
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
	public static ApptentiveInternal createInstance(Context context, String apptentiveApiKey) {
		if (sApptentiveInternal == null) {
			synchronized (ApptentiveInternal.class) {
				if (sApptentiveInternal == null && context != null) {

					// trim spaces
					apptentiveApiKey = Util.trim(apptentiveApiKey);

					// if API key is not defined - try loading from AndroidManifest.xml
					if (TextUtils.isEmpty(apptentiveApiKey)) {
						apptentiveApiKey = resolveManifestApiKey(context);
					}

					sApptentiveInternal = new ApptentiveInternal(context, apptentiveApiKey);
					isApptentiveInitialized.set(false);
/*
<<<<<<< HEAD
=======
					sApptentiveInternal.appContext = context.getApplicationContext();
					sApptentiveInternal.globalSharedPrefs = sApptentiveInternal.appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

					MessageManager msgManager = new MessageManager();
					PayloadSendWorker payloadWorker = new PayloadSendWorker();
					ApptentiveTaskManager worker = new ApptentiveTaskManager(sApptentiveInternal.appContext);
					ApptentiveComponentRegistry componentRegistry = new ApptentiveComponentRegistry();

					sApptentiveInternal.messageManager = msgManager;
					sApptentiveInternal.payloadWorker = payloadWorker;
					sApptentiveInternal.taskManager = worker;
					sApptentiveInternal.cachedExecutor = Executors.newCachedThreadPool();
					sApptentiveInternal.componentRegistry = componentRegistry;
					sApptentiveInternal.apiKey = Util.trim(apptentiveApiKey);
>>>>>>> feature/fetch_interactions
*/
				}
			}
		}
		return sApptentiveInternal;
	}

	/**
	 * Helper method for resolving API key from AndroidManifest.xml
	 *
	 * @return null if API key is missing or exception is thrown
	 */
	private static String resolveManifestApiKey(Context context) {
		try {
			String appPackageName = context.getPackageName();
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			Bundle metaData = packageInfo.applicationInfo.metaData;
			if (metaData != null) {
				return Util.trim(metaData.getString(Constants.MANIFEST_KEY_APPTENTIVE_API_KEY));
			}
		} catch (Exception e) {
			ApptentiveLog.e("Unexpected error while reading application or package info.", e);
		}

		return null;
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

					final boolean successful = sApptentiveInternal.init();
					dispatchDebugEvent(EVT_INSTANCE_CREATED, successful);

					if (!successful) {
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

	public ApptentiveComponentRegistry getComponentRegistry() {
		return componentRegistry;
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

	public Conversation getConversation() {
		return conversation;
	}

	public String getApptentiveApiKey() {
		return apiKey;
	}

	public String getDefaultAppDisplayName() {
		return defaultAppDisplayName;
	}

	public boolean isApptentiveDebuggable() {
		return appRelease.isDebug();
	}

	public String getPersonId() {
		return personId;
	}

	public SharedPreferences getGlobalSharedPrefs() {
		return globalSharedPrefs;
	}

	public void runOnWorkerThread(Runnable r) {
		cachedExecutor.execute(r);
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

		File internalStorage = appContext.getFilesDir();
		File sessionDataFile = new File(internalStorage, "apptentive/Conversation.ser");
		sessionDataFile.getParentFile().mkdirs();
		fileSerializer = new FileSerializer(sessionDataFile);
		conversation = (Conversation) fileSerializer.deserialize();
		if (conversation != null) {
			conversation.setDataChangedListener(this);
			ApptentiveLog.d("Restored existing Conversation");
			ApptentiveLog.v("Restored EventData: %s", conversation.getEventData());
			// FIXME: Move this to wherever the sessions first comes online?
			boolean featureEverUsed = conversation != null && conversation.isMessageCenterFeatureUsed();
			if (featureEverUsed) {
				messageManager.init();
			}
			conversation.setInteractionManager(new InteractionManager(conversation));
			// TODO: Make a callback like conversationBecameCurrent(), and call this there
			scheduleInteractionFetch();
		} else {
			fetchConversationToken();
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

			checkSendVersionChanges();

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
			if (appRelease.isDebug()) {
				setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
			}
		}
		ApptentiveLog.i("Debug mode enabled? %b", appRelease.isDebug());

		// The apiKey can be passed in programmatically, or we can fallback to checking in the manifest.
		if (TextUtils.isEmpty(apiKey) || apiKey.contains(Constants.EXAMPLE_API_KEY_VALUE)) {
			String errorMessage = "The Apptentive API Key is not defined. You may provide your Apptentive API Key in Apptentive.register(), or in as meta-data in your AndroidManifest.xml.\n" +
				"<meta-data android:name=\"apptentive_api_key\"\n" +
				"           android:value=\"@string/your_apptentive_api_key\"/>";
			if (appRelease.isDebug()) {
				throw new RuntimeException(errorMessage);
			} else {
				ApptentiveLog.e(errorMessage);
			}
		} else {
			ApptentiveLog.d("Using cached Apptentive API Key");
		}
		ApptentiveLog.d("Apptentive API Key: %s", apiKey);

		// Grab app info we need to access later on.
		ApptentiveLog.d("Default Locale: %s", Locale.getDefault().toString());
		return bRet;
	}

	private void checkSendVersionChanges() {
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
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getVersionHistory().updateVersionHistory(Util.currentTimeSeconds(), currentVersionCode, currentVersionName);
			}
		}

		Sdk sdk = SdkManager.generateCurrentSdk();
		if (sdkChanged) {
			ApptentiveLog.i("SDK version changed: %s => %s", lastSeenSdkVersion, currentSdkVersion);
			conversation.setLastSeenSdkVersion(currentSdkVersion);
			conversation.setSdk(sdk);
		}

		if (appReleaseChanged || sdkChanged) {
			taskManager.addPayload(AppReleaseManager.getPayload(sdk, appRelease));
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

	private void fetchConversationToken() {
		if (isConversationTokenFetchPending.compareAndSet(false, true)) {
			ApptentiveLog.i(CONVERSATION, "Fetching Configuration token task started.");
			dispatchDebugEvent(EVT_FETCH_CONVERSATION_TOKEN);

			// Try to fetch a new one from the server.
			ConversationTokenRequest request = new ConversationTokenRequest();

			// Send the Device and Sdk now, so they are available on the server from the start.
			final Device device = DeviceManager.generateNewDevice(appContext);
			final Sdk sdk = SdkManager.generateCurrentSdk();

			request.setDevice(DeviceManager.getDiffPayload(null, device));
			request.setSdk(SdkManager.getPayload(sdk));
			request.setAppRelease(AppReleaseManager.getPayload(appRelease));

			apptentiveHttpClient.getConversationToken(request, new HttpRequest.Listener<HttpJsonRequest>() {
				@Override
				public void onFinish(HttpJsonRequest request) {
					try {
						JSONObject root = request.getResponseObject();
						String conversationToken = root.getString("token");
						ApptentiveLog.d(CONVERSATION, "ConversationToken: " + conversationToken);
						String conversationId = root.getString("id");
						ApptentiveLog.d(CONVERSATION, "New Conversation id: %s", conversationId);

						conversation = new Conversation();
						conversation.setDataChangedListener(ApptentiveInternal.this);
						if (conversationToken != null && !conversationToken.equals("")) {
							conversation.setConversationToken(conversationToken);
							conversation.setConversationId(conversationId);
							conversation.setDevice(device);
							conversation.setSdk(sdk);
							conversation.setAppRelease(appRelease);
							conversation.setInteractionManager(new InteractionManager(conversation));
						}
						String personId = root.getString("person_id");
						ApptentiveLog.d(CONVERSATION, "PersonId: " + personId);
						conversation.setPersonId(personId);

						// TODO: Make a callback like sessionBecameCurrent(), and call this there
						scheduleInteractionFetch();
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling conversation token");
					} finally {
						isConversationTokenFetchPending.set(false);
					}
				}

				@Override
				public void onCancel(HttpJsonRequest request) {
					isConversationTokenFetchPending.set(false);
				}

				@Override
				public void onFail(HttpJsonRequest request, String reason) {
					ApptentiveLog.w("Failed to fetch conversation token: %s", reason);
					isConversationTokenFetchPending.set(false);
				}
			});
		}
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
		globalSharedPrefs.edit().clear().apply();
		taskManager.reset(appContext);
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

	private synchronized void scheduleSessionDataSave() {
		boolean scheduled = backgroundQueue.dispatchAsyncOnce(saveSessionTask, 100L);
		if (scheduled) {
			ApptentiveLog.d("Scheduling Conversation save.");
		} else {
			ApptentiveLog.d("Conversation save already scheduled.");
		}
	}

	private synchronized void scheduleInteractionFetch() {
		if (conversation != null) {
			InteractionManager interactionManager = conversation.getInteractionManager();
			if (interactionManager != null) {
				if (interactionManager.isPollForInteractions()) {
					boolean cacheExpired = conversation.getInteractionExpiration() > Util.currentTimeSeconds();
					boolean force = appRelease != null && appRelease.isDebug();
					if (cacheExpired || force) {
						backgroundQueue.dispatchAsyncOnce(fetchInteractionsTask);
					}
				} else {
					ApptentiveLog.v("Interaction polling is disabled.");
				}
			}
		}
	}

	private final DispatchTask saveSessionTask = new DispatchTask() {
		@Override
		protected void execute() {
			ApptentiveLog.d("Saving Conversation");
			ApptentiveLog.v("EventData: %s", conversation.getEventData().toString());
			if (fileSerializer != null) {
				fileSerializer.serialize(conversation);
			}
		}
	};

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

	private final DispatchTask fetchInteractionsTask = new DispatchTask() {
		@Override
		protected void execute() {
			Conversation conversation = getConversation();
			if (conversation != null) {
				conversation.getInteractionManager().fetchInteractions();
			}
		}
	};

	//region Listeners

	@Override
	public void onDataChanged() {
		scheduleSessionDataSave();
	}
	//endregion

	/**
	 * Ends current user session
	 */
	public static void logout() {
		getInstance().getComponentRegistry()
			.notifyComponents(new ComponentNotifier<OnUserLogOutListener>(OnUserLogOutListener.class) {
				@Override
				public void onComponentNotify(OnUserLogOutListener component) {
					component.onUserLogOut();
				}
			});
	}
}
