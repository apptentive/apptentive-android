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
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.survey.SurveyDefinition;
import com.apptentive.android.sdk.survey.SurveyManager;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

	private ALog log;

	private Activity activity = null;

	private static Apptentive instance = null;
	private Apptentive(Activity activity) {
		this.activity = activity;
		log = new ALog(Apptentive.class);
		printDebugInfo();
	}

	public static Apptentive getInstance(){
		if(instance == null){
			throw new ExceptionInInitializerError("Apptentive not initialized.");
		}
		return instance;
	}

	/**
	 * Initializes the Apptentive SDK. You must call this before performing any other interaction with the SDK.
	 * @param activity The activity from which this method is called.
	 * @param appDisplayName The displayable name of your app.
	 * @param apiKey The Apptentive API key for this app.
	 * @param ratingFlowDefaultDaysBeforePrompt The number of days to wait before initially starting the rating flow. Leave null for default of 30.
	 * @param ratingFlowDefaultDaysBeforeReprompt The number of days to wait before asking the user to rate the app if they have already chosen "Remind me later". Leave null for default of 5.
	 * @param ratingFlowDefaultSignificantEventsBeforePrompt The number of significant events to wait for before initially starting the rating flow. Leave null for default of 10.
	 * @param ratingFlowDefaultUsesBeforePrompt The number of app uses to wait for before initially starting the rating flow. Leave null for default of 5.
	 * @return Apptentive - The initialized SDK instance, who's public methods can be called during user interaction.
	 */
	public static Apptentive initialize(Activity activity, String appDisplayName, String apiKey, Integer ratingFlowDefaultDaysBeforePrompt, Integer ratingFlowDefaultDaysBeforeReprompt, Integer ratingFlowDefaultSignificantEventsBeforePrompt, Integer ratingFlowDefaultUsesBeforePrompt){
		instance = new Apptentive(activity);
		ApptentiveModel.setDefaults(ratingFlowDefaultDaysBeforePrompt, ratingFlowDefaultDaysBeforeReprompt, ratingFlowDefaultSignificantEventsBeforePrompt, ratingFlowDefaultUsesBeforePrompt);
		ApptentiveModel model = ApptentiveModel.getInstance();
		TelephonyManager manager = (TelephonyManager) (activity.getSystemService(activity.TELEPHONY_SERVICE));
		model.setModel(Build.MODEL);
		model.setVersion(String.format("Android %s.%s", Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL));
		model.setCarrier(manager.getNetworkOperatorName());
		String androidId = android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		model.setUuid(androidId);
		model.setFeedbackType("feedback");
		model.setPrefs(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));
		model.setAppDisplayName(appDisplayName);
		model.setAppPackage(activity.getApplicationContext().getPackageName());
		model.setApiKey(apiKey);
		model.setAskForExtraInfo(false);
		model.incrementUses();
		if(model.getEmail().equals("")){
			model.setEmail(getUserEmail(activity));
		}

		instance.getSurveys();
		instance.uploadPendingPayloads();
		return instance;
	}

	/**
	 * Asynchronously download surveys and put them in the model.
	 */
	private void getSurveys(){
		// Upload any payloads that were created while the device was offline.
		new Thread(){
			@Override
			public void run() {
				ApptentiveModel model = ApptentiveModel.getInstance();
				ApptentiveClient client = new ApptentiveClient(model.getApiKey());
				model.setSurveys(client.getSurveys());
			}
		}.start();
	}

	private void uploadPendingPayloads(){
		// Upload any payloads that were created while the device was offline.
		PayloadManager payloadManager = new PayloadManager(activity);
		payloadManager.run();
	}

	/**
	 * Call this in your activity's onResume() method. If the rating flow is able to run, it will.
	 */
	public void runIfNeeded(){

		ApptentiveModel model = ApptentiveModel.getInstance();
		ApptentiveState state = model.getState();

		switch(state){
			case START :
				if(ratingPeriodElapsed() || eventThresholdReached() || usesThresholdReached()){
					choice();
				}
				break;
			case REMIND :
				if(ratingPeriodElapsed()){
					rating();
				}
				break;
			case DONE :
				break;
			default :
				break;
		}
	}

	/**
	 * Short circuit the rating flow to the "Feedback" screen.
	 */
	public void feedback(boolean forced){
		Intent intent = new Intent();
		intent.putExtra("forced", forced);
		intent.putExtra("module", ApptentiveActivity.Module.FEEDBACK.toString());
		intent.setClass(activity, ApptentiveActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * Show a survey, if available.
	 */
	public void survey(){
		Intent intent = new Intent();
		intent.putExtra("module", ApptentiveActivity.Module.SURVEY.toString());
		intent.setClass(activity, ApptentiveActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * Short circuit the rating flow to the "Would you like to rate?" dialog.
	 */
	public void rating(){
		RatingController controller = new RatingController(activity);
		controller.show();
	}

	/**
	 * Short circuit the rating flow to the "Do you like this app?" dialog.
	 */
	public void choice(){
		ChoiceController controller = new ChoiceController(activity);
		controller.show();
	}

	/**
	 * Call this when the user accomplishes a significant event.
	 */
	public void event(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.incrementEvents();
	}

	/**
	 * This method is only for testing. It will move the "first run" date one day into the past.
	 */
	public void day(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setStartOfRatingPeriod(Util.addDaysToDate(model.getStartOfRatingPeriod(), -1));
	}

	/**
	 * This method should be called when a new version of the app is installed, or you want to start the rating/feedback flow over again.
	 */
	public void reset(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.START);
		model.setStartOfRatingPeriod(new Date());
		model.resetEvents();
		model.resetUses();
	}

	/**
	 * Internal use only.
	 */
	public void ratingRemind(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.REMIND);
		model.setStartOfRatingPeriod(new Date());
		model.useRatingDaysBeforeReprompt();
	}

	/**
	 * Internal use only.
	 */
	public void ratingYes(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.DONE);
	}

	/**
	 * Internal use only.
	 */
	public void ratingNo(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.setState(ApptentiveState.DONE);
	}

	/**
	 * Internal use only.
	 */
	public void hideSoftKeyboard(View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

/*
	public void showSoftKeyboard(View target) {
		if (activity.getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(target, 0);
		}
	}
*/

	private static String getUserEmail(Activity activity){
		if(Util.packageHasPermission(activity, "android.permission.GET_ACCOUNTS")){
			String email = EmailUtil.getEmail(activity);
			if(email != null){
				return email;
			}
		}
		return "";
	}

	private boolean ratingPeriodElapsed(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		return Util.timeHasElapsed(model.getStartOfRatingPeriod(), model.getDaysUntilRate());
	}
	private boolean eventThresholdReached(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		return model.getEvents() >= model.getRatingSignificantEventsBeforePrompt();
	}
	private boolean usesThresholdReached(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		return model.getUses() >= model.getRatingUsesBeforePrompt();
	}

	private void printDebugInfo(){
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
