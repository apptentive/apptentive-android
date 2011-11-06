/*
 * Feedback.java
 *
 * Created by Sky Kelsey on 2011-11-05.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.model.GlobalInfo;
import com.apptentive.android.sdk.offline.Payload;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.Date;

public class FeedbackPayload extends Payload {
	private String email;
	private String feedback;

	public FeedbackPayload(String feedbackType){
		try{
			setString(GlobalInfo.manufacturer, "record", "device", "manufacturer");
			setString(GlobalInfo.model, "record", "device", "model");
			setString(GlobalInfo.version, "record", "device", "os_version");
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
