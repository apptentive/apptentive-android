/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import android.os.Build;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Reflection;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public class FeedbackPayload extends Payload {
	private String email;
	private String feedback;

	public FeedbackPayload(String feedbackType){
		try{
			// Add in Android specific static device info
			setString("Android",                  "record", "device", "os_name"); //
			setString(Build.VERSION.RELEASE,      "record", "device", "os_version");
			setString(Build.VERSION.INCREMENTAL,  "record", "device", "os_build");
			setString(Build.MANUFACTURER,         "record", "device", "manufacturer"); //
			setString(Build.MODEL,                "record", "device", "model");
			setString(Build.BOARD,                "record", "device", "board");
			setString(Build.PRODUCT,              "record", "device", "product");
			setString(Build.BRAND,                "record", "device", "brand");
			setString(Build.CPU_ABI,              "record", "device", "cpu");
			setString(Build.DEVICE,               "record", "device", "device");
			setString(GlobalInfo.androidId,       "record", "device", "uuid");
			setString(GlobalInfo.carrier,         "record", "device", "carrier");
			setString(GlobalInfo.currentCarrier,  "record", "device", "current_carrier");
			setString(GlobalInfo.networkType +"", "record", "device", "network_type");
			setString(Build.TYPE,                 "record", "device", "type");
			setString(Build.ID,                   "record", "device", "id");

			// Use reflection to load info from classes not available at API level 7.
			String bootloaderVersion = Reflection.getBootloaderVersion();
			if(bootloaderVersion != null){
				setString(bootloaderVersion,        "record", "device", "bootloader_version");
			}
			String radioVersion = Reflection.getRadioVersion();
			if(radioVersion != null){
				setString(radioVersion,             "record", "device", "radio_version");
			}

			// Add common Apptentive info
			setString(GlobalInfo.APPTENTIVE_API_VERSION, "record", "client", "version");

			// Other feedback fields.
			setString(feedbackType,               "record", "feedback", "type");
			setString(Util.dateToString(new Date()), "record", "date");
		}catch(JSONException e){
			Log.w("Exception creating Feedback Payload.", e);
		}
	}

	@Override
	public String getAsJSON() {
		try {
			setString(email, "record", "user", "email");
			setString(feedback, "record", "feedback", "feedback");
		} catch (JSONException e) {
			Log.w("Exception getting Feedback Payload as JSON.", e);
		}
		return root.toString();
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
}
