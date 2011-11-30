/*
 * Apptentive.java
 *
 * Created by Sky Kelsey on 2011-05-30.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 * Edited by Dr. Cocktor on 2011-11-29.
 * 		+ Removed incorrect dynamic access on the 'Context' object
 * 		+ Removed dead code
 * 		+ Fixed a typo in the file header
 * 		+ Changes Copyright 2011 MiKandi, LLC. All right reserved.
 * Edited by Dr. Cocktor on 2011-11-29.
 * 		+ Updated to support pluggable ratings
 * 		+ Changes Copyright 2011 MiKandi, LLC. All right reserved.
 */

package com.apptentive.android.sdk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.apptentive.android.sdk.activity.ApptentiveActivity;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.ApptentiveState;
import com.apptentive.android.sdk.model.GlobalInfo;
import com.apptentive.android.sdk.module.enjoyment.EnjoymentController;
import com.apptentive.android.sdk.module.feedback.FeedbackController;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.RatingController;
import com.apptentive.android.sdk.module.rating.impl.GoogleMarketRating;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;

public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

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
	 * @param ratingProvider The rating provider to use when providing ratings.
	 * @param ratingArgs	 Arguments for the rating provider.
	 * @return Apptentive - The initialized SDK instance, who's public methods can be called during user interaction.
	 */
	public static Apptentive initialize(Activity activity, String appDisplayName, String apiKey, Integer ratingFlowDefaultDaysBeforePrompt, Integer ratingFlowDefaultDaysBeforeReprompt, Integer ratingFlowDefaultSignificantEventsBeforePrompt, Integer ratingFlowDefaultUsesBeforePrompt, Class<? extends IRatingProvider> ratingProvider, Map<String,String> ratingArgs) {
		Context appContext = activity.getApplicationContext();
		if (instance == null) {
			instance = new Apptentive();

			ApptentiveModel.setDefaults(ratingFlowDefaultDaysBeforePrompt, ratingFlowDefaultDaysBeforeReprompt, ratingFlowDefaultSignificantEventsBeforePrompt, ratingFlowDefaultUsesBeforePrompt);

			GlobalInfo.manufacturer = Build.MANUFACTURER;
			GlobalInfo.model = Build.MODEL;
			GlobalInfo.version = String.format("%s.%s", Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL);
			GlobalInfo.carrier = ((TelephonyManager) (activity.getSystemService(Context.TELEPHONY_SERVICE))).getNetworkOperatorName();

			GlobalInfo.androidId = android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

			GlobalInfo.appDisplayName = appDisplayName;
			GlobalInfo.appPackage = activity.getApplicationContext().getPackageName();
			GlobalInfo.apiKey = apiKey;

			GlobalInfo.userEmail = getUserEmail(activity);
			
			GlobalInfo.ratingProvider = ratingProvider;
			GlobalInfo.ratingArgs = ratingArgs == null ? new HashMap<String,String>(1) : ratingArgs;

			ApptentiveModel model = ApptentiveModel.getInstance();
			model.setPrefs(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));

			instance.getSurvey();

		}
		PayloadManager.initialize(appContext);
		return instance;
	}
	
	/**
	 * Initializes the Apptentive SDK. You must call this before performing any other interaction with the SDK.
	 * Uses the defult {@link GoogleMarketRating} provider for app ratings.
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
		return initialize(activity,appDisplayName,apiKey,ratingFlowDefaultDaysBeforePrompt,ratingFlowDefaultDaysBeforeReprompt,ratingFlowDefaultSignificantEventsBeforePrompt,ratingFlowDefaultUsesBeforePrompt, GoogleMarketRating.class, null);
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
				if (ratingPeriodElapsed()){
					enjoyment(activity, EnjoymentController.Trigger.days);
				}else if(eventThresholdReached()){
					enjoyment(activity, EnjoymentController.Trigger.events);
				}else if(usesThresholdReached()){
					enjoyment(activity, EnjoymentController.Trigger.uses);
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
	 * This method allows you to pass in key/value pairs, which can then be sent along with subsequently submitted feedback.
	 * @param key A key String
	 * @param value A value String
	 */
	public void addFeedbackDataField(String key, String value){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setCustomDataField(key, value);
	}

	/**
	 * Short circuit the rating flow to the "Feedback" screen.
	 * @param activity The activity from which to launch the dialog.
	 * @param forced True if the feedback dialog was launched from a click handler. False if launched after elapsed time or events.
	 */
	public void feedback(Activity activity, boolean forced) {
		if(forced){
			new FeedbackController(activity, FeedbackController.Trigger.forced);
		}else{
			new FeedbackController(activity, FeedbackController.Trigger.rating);
		}
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
	public void enjoyment(Activity activity) {
		enjoyment(activity, EnjoymentController.Trigger.forced);
	}

	private void enjoyment(Activity activity, EnjoymentController.Trigger reason) {
		EnjoymentController controller = new EnjoymentController(activity, reason);
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
}
