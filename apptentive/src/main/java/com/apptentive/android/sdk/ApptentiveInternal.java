/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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
import android.os.Bundle;
import android.provider.Settings;
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
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
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
 *
 * @author Sky Kelsey
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
	int statusBarColorDefault = R.color.apptentive_transparency;
	String defaultAppDisplayName = "this app";

	IRatingProvider ratingProvider;
	Map<String, String> ratingProviderArgs;
	WeakReference<OnSurveyFinishedListener> onSurveyFinishedListener;

	final LinkedBlockingQueue configUpdateListeners = new LinkedBlockingQueue();

	ExecutorService cachedExecutor;

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
				Log.d("Error parsing unknown PushAction: " + name);
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
		if (sApptentiveInternal != null && isApptentiveInitialized.compareAndSet(false, true)) {
			sApptentiveInternal.init();
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
			((Application) sApptentiveInternal.appContext).registerActivityLifecycleCallbacks(new ApptentiveActivityLifecycleCallbacks(sApptentiveInternal.appContext));
		}
	}

	/*
	 * Object getter methods through ApptentiveInternal instance
	 * Usage: ApptentiveInternal.getInstance().getxxxx()
	 */

	public Context getApplicationContext() {
		return appContext;
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
				Log.w("Unable to add custom device data.", e);
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
				Log.w("Unable to add custom person data.", e);
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
			fetchSdkState();
		}
	}

	public void onAppLaunch(final Activity activity) {
		EngagementModule.engageInternal(activity, Event.EventLabel.app__launch.getLabelName());
	}

	public void onAppExit(final Activity activity) {
		EngagementModule.engageInternal(activity, Event.EventLabel.app__exit.getLabelName());
	}

	public void onActivityResumed(Activity activity) {
		messageManager.setCurrentForgroundActivity(activity);

		checkAndUpdateApptentiveConfigurations();

		syncDevice();
		syncSdk();
		syncPerson();
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

			Integer currentVersionCode = packageInfo.versionCode;
			String currentVersionName = packageInfo.versionName;
			VersionHistoryStore.VersionHistoryEntry lastVersionEntrySeen = VersionHistoryStore.getLastVersionSeen();
			if (lastVersionEntrySeen == null) {
				onVersionChanged(null, currentVersionCode, null, currentVersionName);
			} else {
				if (!currentVersionCode.equals(lastVersionEntrySeen.versionCode) || !currentVersionName.equals(lastVersionEntrySeen.versionName)) {
					onVersionChanged(lastVersionEntrySeen.versionCode, currentVersionCode, lastVersionEntrySeen.versionName, currentVersionName);
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

			// Set up Apptentive theme by applying default from Apptentive, app, and custom override
			int appDefaultThemeId = ai.theme;
			boolean appHasTheme = appDefaultThemeId != 0;
			apptentiveTheme = appContext.getResources().newTheme();
			apptentiveTheme.applyStyle(R.style.ApptentiveTheme, true);

			if (appHasTheme) {
				Resources.Theme appDefaultTheme = appContext.getResources().newTheme();
				appDefaultTheme.applyStyle(appDefaultThemeId, true);

				TypedArray a = appDefaultTheme.obtainStyledAttributes(new int[]{android.R.attr.statusBarColor});
				try {
					statusBarColorDefault = a.getColor(0, 0);
				} finally {
					a.recycle();
				}

				// If the app contains colorPrimaryDark, it is using an AppCompat theme. Therefore, we want to use it.
				// If it's not using an AppCompat theme, we don't want to apply it to our SDK, and use our default theme instead.
				boolean appThemeIsAppCompatTheme = Util.getThemeColor(appDefaultTheme, R.attr.colorPrimaryDark) != 0;
				if (appThemeIsAppCompatTheme) {
					apptentiveTheme.applyStyle(appDefaultThemeId, true);
				}
			}

			apptentiveTheme.applyStyle(R.style.ApptentiveBaseVersionBaseFrameStyle, true);
			apptentiveTheme.applyStyle(R.style.ApptentiveThemeOverride, true);

		} catch (Exception e) {
			Log.e("Unexpected error while reading application or package info.", e);
		}

		// Set debuggable and appropriate log level.
		if (apptentiveDebug) {
			Log.i("Apptentive debug logging set to VERBOSE.");
			setMinimumLogLevel(Log.Level.VERBOSE);
		} else if (logLevelOverride != null) {
			Log.i("Overriding log level: %s", logLevelOverride);
			setMinimumLogLevel(Log.Level.parse(logLevelOverride));
		} else {
			if (isAppDebuggable) {
				setMinimumLogLevel(Log.Level.VERBOSE);
			}
		}
		Log.i("Debug mode enabled? %b", isAppDebuggable);

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
				Log.a(errorMessage);
			}
		} else {
			Log.d("Using cached Apptentive API Key");
		}
		Log.d("Apptentive API Key: %s", apiKey);

		// Grab app info we need to access later on.
		androidId = Settings.Secure.getString(appContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		Log.d("Android ID: ", androidId);

		Log.d("Default Locale: %s", Locale.getDefault().toString());
		Log.d("Conversation id: %s", prefs.getString(Constants.PREF_KEY_CONVERSATION_ID, "null"));
	}

	private void onVersionChanged(Integer previousVersionCode, Integer currentVersionCode, String previousVersionName, String currentVersionName) {
		Log.i("Version changed: Name: %s => %s, Code: %d => %d", previousVersionName, currentVersionName, previousVersionCode, currentVersionCode);
		VersionHistoryStore.updateVersionHistory(currentVersionCode, currentVersionName);
		AppRelease appRelease = AppReleaseManager.storeAppReleaseAndReturnDiff();
		if (appRelease != null) {
			Log.d("App release was updated.");
			database.addPayload(appRelease);
		}
		invalidateCaches();
	}

	private void onSdkVersionChanged(Context context, String previousSdkVersion, String currentSdkVersion) {
		Log.i("SDK version changed: %s => %s", previousSdkVersion, currentSdkVersion);
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
		Thread thread = new Thread() {
			@Override
			public void run() {
				fetchConversationToken();
			}
		};
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Log.w("Caught UncaughtException in thread \"%s\"", throwable, thread.getName());
				MetricModule.sendError(throwable, null, null);
			}
		};
		thread.setUncaughtExceptionHandler(handler);
		thread.setName("Apptentive-FetchConversationToken");
		thread.start();
	}

	private void fetchConversationToken() {
		// Try to fetch a new one from the server.
		ConversationTokenRequest request = new ConversationTokenRequest();

		// Send the Device and Sdk now, so they are available on the server from the start.
		request.setDevice(DeviceManager.storeDeviceAndReturnIt());
		request.setSdk(SdkManager.storeSdkAndReturnIt());
		request.setPerson(PersonManager.storePersonAndReturnIt());

		ApptentiveHttpResponse response = ApptentiveClient.getConversationToken(request);
		if (response == null) {
			Log.w("Got null response fetching ConversationToken.");
			return;
		}
		if (response.isSuccessful()) {
			try {
				JSONObject root = new JSONObject(response.getContent());
				String conversationToken = root.getString("token");
				Log.d("ConversationToken: " + conversationToken);
				String conversationId = root.getString("id");
				Log.d("New Conversation id: %s", conversationId);

				if (conversationToken != null && !conversationToken.equals("")) {
					setConversationToken(conversationToken);
					setConversationId(conversationId);
				}
				String personId = root.getString("person_id");
				Log.d("PersonId: " + personId);
				if (personId != null && !personId.equals("")) {
					setPersonId(personId);
				}
				fetchSdkState();
			} catch (JSONException e) {
				Log.e("Error parsing ConversationToken response json.", e);
			}
		}
	}

	/**
	 * Fetches the global app configuration from the server and stores the keys into our SharedPreferences.
	 */
	private void fetchAppConfiguration() {
		boolean force = isAppDebuggable;

		// Don't get the app configuration unless forced, or the cache has expired.
		if (force || Configuration.load().hasConfigurationCacheExpired()) {
			Log.i("Fetching new Configuration.");
			ApptentiveHttpResponse response = ApptentiveClient.getAppConfiguration();
			try {
				Map<String, String> headers = response.getHeaders();
				if (headers != null) {
					String cacheControl = headers.get("Cache-Control");
					Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
					if (cacheSeconds == null) {
						cacheSeconds = Constants.CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS;
					}
					Log.d("Caching configuration for %d seconds.", cacheSeconds);
					Configuration config = new Configuration(response.getContent());
					config.setConfigurationCacheExpirationMillis(System.currentTimeMillis() + cacheSeconds * 1000);
					config.save();
				}
			} catch (JSONException e) {
				Log.e("Error parsing app configuration from server.", e);
			}
		} else {
			Log.v("Using cached Configuration.");
		}
	}

	private void asyncFetchAppConfiguration() {
		Thread thread = new Thread() {
			public void run() {
				fetchAppConfiguration();
			}
		};
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Log.e("Caught UncaughtException in thread \"%s\"", throwable, thread.getName());
				MetricModule.sendError(throwable, null, null);
			}
		};
		thread.setUncaughtExceptionHandler(handler);
		thread.setName("Apptentive-FetchAppConfiguration");
		thread.start();
	}

	/**
	 * Sends current Device to the server if it differs from the last time it was sent.
	 */
	void syncDevice() {
		Device deviceInfo = DeviceManager.storeDeviceAndReturnDiff();
		if (deviceInfo != null) {
			Log.d("Device info was updated.");
			Log.v(deviceInfo.toString());
			database.addPayload(deviceInfo);
		} else {
			Log.d("Device info was not updated.");
		}
	}

	/**
	 * Sends current Sdk to the server if it differs from the last time it was sent.
	 */
	private void syncSdk() {
		Sdk sdk = SdkManager.storeSdkAndReturnDiff();
		if (sdk != null) {
			Log.d("Sdk was updated.");
			Log.v(sdk.toString());
			database.addPayload(sdk);
		} else {
			Log.d("Sdk was not updated.");
		}
	}

	/**
	 * Sends current Person to the server if it differs from the last time it was sent.
	 */
	private void syncPerson() {
		Person person = PersonManager.storePersonAndReturnDiff();
		if (person != null) {
			Log.d("Person was updated.");
			Log.v(person.toString());
			database.addPayload(person);
		} else {
			Log.d("Person was not updated.");
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

	public void addConfigUpdateListener(ApptentiveBaseFragment.ConfigUpdateListener listener) {
		configUpdateListeners.add(listener);
	}

	public void removeConfigUpdateListener(ApptentiveBaseFragment.ConfigUpdateListener listener) {
		configUpdateListeners.remove(listener);
	}

	/**
	 * Pass in a log level to override the default, which is {@link Log.Level#INFO}
	 */
	public void setMinimumLogLevel(Log.Level level) {
		Log.overrideLogLevel(level);
	}

	private String pushCallbackActivityName;

	public void setPushCallbackActivity(Class<? extends Activity> activity) {
		pushCallbackActivityName = activity.getName();
		Log.d("Setting push callback activity name to %s", pushCallbackActivityName);
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
			Log.v("Got an Intent.");
			// Parse
			if (intent.hasExtra(PARSE_PUSH_EXTRA_KEY)) {
				String parseStringExtra = intent.getStringExtra(PARSE_PUSH_EXTRA_KEY);
				Log.v("Got a Parse Push.");
				try {
					JSONObject parseJson = new JSONObject(parseStringExtra);
					apptentive = parseJson.optString(APPTENTIVE_PUSH_EXTRA_KEY, null);
				} catch (JSONException e) {
					Log.e("Corrupt Parse String Extra: %s", parseStringExtra);
				}
			} else {
				// Straight GCM / SNS
				Log.v("Got a non-Parse push.");
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
			Log.d("Saving Apptentive push notification data.");
			prefs.edit().putString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, apptentivePushData).apply();
			messageManager.startMessagePreFetchTask();
			return true;
		}
		return false;
	}

	public void showAboutInternal(Activity activity, boolean showBrandingBand) {
		Intent intent = new Intent();
		intent.setClass(activity, ApptentiveViewActivity.class);
		intent.putExtra(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.ABOUT);
		intent.putExtra(Constants.FragmentConfigKeys.EXTRA, showBrandingBand);
		activity.startActivity(intent);
	}

	public boolean showMessageCenterInternal(Activity activity, Map<String, Object> customData) {
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
							Log.w("Removing invalid customData type: %s", value.getClass().getSimpleName());
							keysIterator.remove();
						}
					}
				}
			}
			this.customData = customData;
			interactionShown = EngagementModule.engageInternal(activity, MessageCenterInteraction.DEFAULT_INTERNAL_EVENT_NAME);
			if (!interactionShown) {
				this.customData = null;
			}
		} else {
			showMessageCenterFallback(activity);
		}
		return interactionShown;
	}

	public void showMessageCenterFallback(Activity activity) {
		EngagementModule.launchMessageCenterErrorActivity(activity);
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

	private void fetchSdkState() {
		asyncFetchAppConfiguration();
		interactionManager.asyncFetchAndStoreInteractions();
	}

	public void resetSdkState() {
		// Use commit() instead of apply(), otherwise it doesn't finish before the app is killed.
		prefs.edit().clear().commit();
		database.reset(appContext);
	}

	public void notifyConfigurationUpdated(boolean successful) {
		Iterator it = configUpdateListeners.iterator();

		while (it.hasNext()) {
			ApptentiveBaseFragment.ConfigUpdateListener listener = (ApptentiveBaseFragment.ConfigUpdateListener) it.next();

			if (listener != null) {
				listener.onConfigurationUpdated(successful);
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
