/*
 * JSONFeedback.java
 *
 * Created by Sky Kelsey on 2011-11-01.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class JSONFeedback extends JSONPayload {


	public JSONFeedback() {
	}

	public JSONFeedback(String uuid,
	                    String model,
	                    String osVersion,
	                    String carrier,
	                    String apptentiveApiVersion,
	                    String name,
	                    String email,
	                    String feedback,
	                    String feedbackType,
	                    Date feedbackDate) throws JSONException {
		super();
		addString(uuid, "record", "device", "uuid");
		addString(model, "record", "device", "model");
		addString(osVersion, "record", "device", "os_version");
		addString(carrier, "record", "device", "carrier");
		addString(apptentiveApiVersion, "record", "client", "version");
		addString("Android", "record", "client", "os");
		addString(name, "record", "user", "name");
		addString(email, "record", "user", "email");
		addString(feedback, "record", "feedback", "feedback");
		addString(feedbackType, "record", "feedback", "type");
		addString(Util.dateToString(feedbackDate), "record", "date");
	}

	@Override
	public String getAsJSON() {
		return root.toString();
	}
}
