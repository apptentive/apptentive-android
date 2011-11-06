/*
 * ApptentiveSDK.java
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
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.feedback.FeedbackController;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;

import java.util.Date;

public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

	private ALog log;

	private static Apptentive instance = null;

	private Apptentive() {
		log = new ALog(Apptentive.class);
		log.e("INITIALIZE");
		printDebugInfo();
	}

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
	 * @return Apptentive - The initialized SDK instance, who's public methods can be called during user interaction.
	 */
	public static Apptentive initialize(Activity activity, String appDisplayName, String apiKey, Integer ratingFlowDefaultDaysBeforePrompt, Integer ratingFlowDefaultDaysBeforeReprompt, Integer ratingFlowDefaultSignificantEventsBeforePrompt, Integer ratingFlowDefaultUsesBeforePrompt) {
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
			@Override
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
	 */
	public void runIfNeeded(Activity activity) {

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
	 * Short circuit the rating flow to the "Feedback" screen.
	 */
	public void feedback(Activity activity, boolean forced) {
		new FeedbackController(activity, forced);
	}

	/**
	 * Show about dialog
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
	 */
	public void survey(Context context) {
		Intent intent = new Intent();
		intent.putExtra("module", ApptentiveActivity.Module.SURVEY.toString());
		intent.setClass(context, ApptentiveActivity.class);
		context.startActivity(intent);
	}

	/**
	 * Short circuit the rating flow to the "Would you like to rate?" dialog.
	 */
	public void rating(Context context) {
		RatingController controller = new RatingController(context);
		controller.show();
	}

	/**
	 * Short circuit the rating flow to the "Do you like this app?" dialog.
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

/*
	public void showSoftKeyboard(View target) {
		if (activity.getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(target, 0);
		}
	}
*/

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
		log.w("Build.BRAND:               %s", Build.BRAND);
		log.w("Build.DEVICE:              %s", Build.DEVICE);
		log.w("Build.MANUFACTURER:        %s", Build.MANUFACTURER);
		log.w("Build.MODEL:               %s", Build.MODEL);
		log.w("Build.PRODUCT:             %s", Build.PRODUCT);
		log.w("Build.TYPE:                %s", Build.TYPE);
		log.w("Build.USER:                %s", Build.USER);
		log.w("Build.VERSION.SDK:         %s", Build.VERSION.SDK);
		log.w("Build.VERSION.SDK_INT:     %s", Build.VERSION.SDK_INT);
		log.w("Build.VERSION.CODENAME:    %s", Build.VERSION.CODENAME);
		log.w("Build.VERSION.INCREMENTAL: %s", Build.VERSION.INCREMENTAL);
		log.w("Build.VERSION.RELEASE:     %s", Build.VERSION.RELEASE);
		log.w("Build.BOARD:               %s", Build.BOARD);
		log.w("Build.CPU_AIB:             %s", Build.CPU_ABI);
		log.w("Build.DISPLAY:             %s", Build.DISPLAY);
		log.w("Build.FINGERPRINT:         %s", Build.FINGERPRINT);
		log.w("Build.HOST:                %s", Build.HOST);
		log.w("Build.ID:                  %s", Build.ID);
		log.w("Build.TAGS:                %s", Build.TAGS);
		log.w("Build.TIME:                %s", Build.TIME);
	}
}
