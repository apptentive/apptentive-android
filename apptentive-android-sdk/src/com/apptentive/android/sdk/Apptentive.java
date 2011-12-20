/*
 * Created by Sky Kelsey on 2011-05-30.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.EmailUtil;
import com.apptentive.android.sdk.util.Util;


public class Apptentive {

	public static final String APPTENTIVE_API_VERSION = "0.1";

	private static Apptentive instance = null;
	private Application application;


	private Apptentive() {
	}

	public static Apptentive getInstance() {
		if (instance == null) {
			instance = new Apptentive();
		}
		return instance;
	}

	public void setActivity(Activity activity) {
		this.application = activity.getApplication();
		Context appContext = activity.getApplicationContext();

		GlobalInfo.carrier = ((TelephonyManager) (application.getSystemService(Application.TELEPHONY_SERVICE))).getNetworkOperatorName();
		GlobalInfo.appPackage = activity.getApplicationContext().getPackageName();
		GlobalInfo.androidId = Settings.Secure.getString(application.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		GlobalInfo.userEmail = getUserEmail(application.getApplicationContext());

		PayloadManager.getInstance().setContext(appContext);
		PayloadManager.getInstance().start();

		// Initialize modules.
		RatingModule.getInstance().setContext(application.getApplicationContext());
	}

	public void setApiKey(String apiKey) {
		GlobalInfo.apiKey = apiKey;
	}

	public void setAppDisplayName(String name) {
		GlobalInfo.appDisplayName = name;
	}


	public RatingModule getRatingModule() {
		return RatingModule.getInstance();
	}

	public FeedbackModule getFeedbackModule() {
		return FeedbackModule.getInstance();
	}

	public SurveyModule getSurveyModule() {
		return SurveyModule.getInstance();
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
}
