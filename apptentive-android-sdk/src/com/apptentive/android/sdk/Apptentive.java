/*
 * Apptentive.java
 *
 * Created by Sky Kelsey on 2011-05-30.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.activity.ApptentiveActivity;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.module.choice.ChoiceController;
import com.apptentive.android.sdk.module.feedback.FeedbackController;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.module.rating.RatingController;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

	private Log log;

	private static Apptentive instance = null;

	private Apptentive() {}

	public static Apptentive getInstance() {
		if (instance == null) {
			throw new ExceptionInInitializerError("Apptentive not initialized.");
		}
		return instance;
	}

	/**
	 * Initializes the Apptentive SDK. You must call this before performing any other interaction with the SDK.
	 *
	 * @param activity       The activity from which this method is called.
	 * @param appDisplayName The displayable name of your app.
	 * @param apiKey         The Apptentive API key for this app.
	 * @param ratingFlowDefaultDaysBeforePrompt
	 *                       The number of days to wait before initially starting the rating flow. Leave null for default of 30.
	 * @param ratingFlowDefaultDaysBeforeReprompt
	 *                       The number of days to wait before asking the user to rate the app if they have already chosen "Remind me later". Leave null for default of 5.
	 * @param ratingFlowDefaultSignificantEventsBeforePrompt
	 *                       The number of significant events to wait for before initially starting the rating flow. Leave null for default of 10.
	 * @param ratingFlowDefaultUsesBeforePrompt
	 *                       The number of app uses to wait for before initially starting the rating flow. Leave null for default of 5.
	 * @param enableMetrics  Set to true if you want to include usage data.
	 *
	 * @return Apptentive - The initialized SDK instance, who's public methods can be called during user interaction.
	 */
	public static Apptentive initialize(Activity activity, String appDisplayName, String apiKey, Integer ratingFlowDefaultDaysBeforePrompt, Integer ratingFlowDefaultDaysBeforeReprompt, Integer ratingFlowDefaultSignificantEventsBeforePrompt, Integer ratingFlowDefaultUsesBeforePrompt, boolean enableMetrics) {
		if (instance == null) {
			instance = new Apptentive();
			ApptentiveModel.setDefaults(ratingFlowDefaultDaysBeforePrompt, ratingFlowDefaultDaysBeforeReprompt, ratingFlowDefaultSignificantEventsBeforePrompt, ratingFlowDefaultUsesBeforePrompt);

			GlobalInfo.manufacturer = Build.MANUFACTURER;
			GlobalInfo.model = Build.MODEL;
			GlobalInfo.version = String.format("%s.%s", Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL);
			GlobalInfo.carrier = ((TelephonyManager) (activity.getSystemService(activity.TELEPHONY_SERVICE))).getNetworkOperatorName();

			GlobalInfo.androidId = android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			GlobalInfo.appDisplayName = appDisplayName;
			GlobalInfo.appPackage = activity.getApplicationContext().getPackageName();
			GlobalInfo.apiKey = apiKey;

			GlobalInfo.userEmail = getUserEmail(activity);

			ApptentiveModel model = ApptentiveModel.getInstance();
			model.setPrefs(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));
			model.setEnableMetrics(enableMetrics);

			instance.getSurvey();
			instance.uploadPendingPayloads(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));
		}
		return instance;
	}

	public void cleanUp() {
		Apptentive.instance = null;
	}

	/**
	 * Asynchronously download surveys and put them in the model.
	 */
	private void getSurvey() {
		// Upload any payloads that were created while the device was offline.
		new Thread() {
			public void run() {
				ApptentiveModel model = ApptentiveModel.getInstance();
				ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
				model.setSurvey(client.getSurvey());
			}
		}.start();
	}

	private void uploadPendingPayloads(SharedPreferences prefs) {
		// Upload any payloads that were created while the device was offline.
		PayloadManager payloadManager = new PayloadManager(prefs);
		payloadManager.run();
	}

	/**
	 * Call this in your activity's onResume() method. If the rating flow is able to run, it will.
	 * @param activity The activity you are calling this method from.
	 */
	public void runIfNeeded(Activity activity) {

		// TODO: Check to see if a data connection exists first. We don't want to prompt to rate unless one exists.
		ApptentiveModel model = ApptentiveModel.getInstance();
		ApptentiveState state = model.getState();

		switch (state) {
			case START:
				if (ratingPeriodElapsed() || eventThresholdReached() || usesThresholdReached()) {
					choice(activity);
				}
				break;
			case REMIND:
				if (ratingPeriodElapsed()) {
					rating(activity);
				}
				break;
			case DONE:
				break;
			default:
				break;
		}
	}

	/**
	 * This method allows you to pass in a map of key/value pairs, which can then be sent along with subsequently submitted feedback.
	 * @param pairs A Map of keys to values.
	 */
	public void addFeedbackDataFields(Map<String, String> pairs){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setCustomDataFields(pairs);
	}

	/**
	 * Short circuit the rating flow to the "Feedback" screen.
	 * @param activity The activity from which to launch the dialog.
	 * @param forced True if the feedback dialog was launched from a click handler. False if launched after elapsed time or events.
	 */
	public void feedback(Activity activity, boolean forced) {
		new FeedbackController(activity, forced);
	}

	/**
	 * Show about dialog
	 * @param context The context from which to launch the dialog.
	 */
	public void about(Context context) {
		Intent intent = new Intent();
		intent.putExtra("module", ApptentiveActivity.Module.ABOUT.toString());
		//intent.addFlags(Intent.FLAG_ACTIVITY_);
		intent.setClass(context, ApptentiveActivity.class);
		context.startActivity(intent);
	}

	/**
	 * Show a survey, if available.
	 * @param context The context from which to launch the dialog.
	 */
	public void survey(Context context) {
		Intent intent = new Intent();
		intent.putExtra("module", ApptentiveActivity.Module.SURVEY.toString());
		intent.setClass(context, ApptentiveActivity.class);
		context.startActivity(intent);
	}

	/**
	 * Short circuit the rating flow to the "Would you like to rate?" dialog.
	 * @param context The context from which to launch the dialog.
	 */
	public void rating(Context context) {
		RatingController controller = new RatingController(context);
		controller.show();
	}

	/**
	 * Short circuit the rating flow to the "Do you like this app?" dialog.
	 * @param activity The context from which to launch the dialog.
	 */
	public void choice(Activity activity) {
		ChoiceController controller = new ChoiceController(activity);
		controller.show();
	}

	/**
	 * Call this when the user accomplishes a significant event.
	 */
	public void event() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.incrementEvents();
	}

	/**
	 * This method is only for testing. It will move the "first run" date one day into the past.
	 */
	public void day() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setStartOfRatingPeriod(Util.addDaysToDate(model.getStartOfRatingPeriod(), -1));
	}

	/**
	 * This method should be called when a new version of the app is installed, or you want to start the rating/feedback flow over again.
	 */
	public void reset() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.START);
		model.setStartOfRatingPeriod(new Date());
		model.resetEvents();
		model.resetUses();
	}

	/**
	 * Internal use only.
	 */
	public void ratingRemind() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.REMIND);
		model.setStartOfRatingPeriod(new Date());
		model.useRatingDaysBeforeReprompt();
	}

	/**
	 * Internal use only.
	 */
	public void ratingYes() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.DONE);
	}

	/**
	 * Internal use only.
	 */
	public void ratingNo() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.DONE);
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

	private boolean ratingPeriodElapsed() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		return Util.timeHasElapsed(model.getStartOfRatingPeriod(), model.getDaysUntilRate());
	}

	private boolean eventThresholdReached() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		return model.getEvents() >= model.getRatingSignificantEventsBeforePrompt();
	}

	private boolean usesThresholdReached() {
		ApptentiveModel model = ApptentiveModel.getInstance();
		return model.getUses() >= model.getRatingUsesBeforePrompt();
	}

	private void printDebugInfo() {
		Log.w("Build.BRAND:               %s", Build.BRAND);
		Log.w("Build.DEVICE:              %s", Build.DEVICE);
		Log.w("Build.MANUFACTURER:        %s", Build.MANUFACTURER);
		Log.w("Build.MODEL:               %s", Build.MODEL);
		Log.w("Build.PRODUCT:             %s", Build.PRODUCT);
		Log.w("Build.TYPE:                %s", Build.TYPE);
		Log.w("Build.USER:                %s", Build.USER);
		Log.w("Build.VERSION.SDK:         %s", Build.VERSION.SDK);
		Log.w("Build.VERSION.SDK_INT:     %s", Build.VERSION.SDK_INT);
		Log.w("Build.VERSION.CODENAME:    %s", Build.VERSION.CODENAME);
		Log.w("Build.VERSION.INCREMENTAL: %s", Build.VERSION.INCREMENTAL);
		Log.w("Build.VERSION.RELEASE:     %s", Build.VERSION.RELEASE);
		Log.w("Build.BOARD:               %s", Build.BOARD);
		Log.w("Build.CPU_AIB:             %s", Build.CPU_ABI);
		Log.w("Build.DISPLAY:             %s", Build.DISPLAY);
		Log.w("Build.FINGERPRINT:         %s", Build.FINGERPRINT);
		Log.w("Build.HOST:                %s", Build.HOST);
		Log.w("Build.ID:                  %s", Build.ID);
		Log.w("Build.TAGS:                %s", Build.TAGS);
		Log.w("Build.TIME:                %s", Build.TIME);
	}
}
