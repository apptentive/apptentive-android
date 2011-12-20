/*
 * Created by Sky Kelsey on 2011-11-05.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import android.os.Build;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.Date;

public class FeedbackPayload extends Payload {
	private String email;
	private String feedback;

	public FeedbackPayload(String feedbackType){
		try{
			setString(Build.MANUFACTURER, "record", "device", "manufacturer");
			setString(Build.MODEL, "record", "device", "model");
			setString(String.format("%s.%s", Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL), "record", "device", "os_version");
			setString(GlobalInfo.androidId, "record", "device", "uuid");
			setString(GlobalInfo.carrier, "record", "device", "carrier");
			setString(GlobalInfo.APPTENTIVE_API_VERSION, "record", "client", "version");
			setString("Android", "record", "device", "os_name");
			setString(feedbackType, "record", "feedback", "type");
			setString(Util.dateToString(new Date()), "record", "date");
		}catch(JSONException e){
		}
	}

	@Override
	public String getAsJSON() {
		try {
			setString(email, "record", "user", "email");
			setString(feedback, "record", "feedback", "feedback");
		} catch (JSONException e) {
		}
		return root.toString();
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
}
