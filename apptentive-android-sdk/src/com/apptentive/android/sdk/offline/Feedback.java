/*
 * Feedback.java
 *
 * Created by skelsey on 2011-09-25.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.util.Util;

import java.util.*;

public class Feedback extends Payload{

	public Feedback(){}

	public Feedback(String uuid,
	                String model,
	                String osVersion,
	                String carrier,
	                String apptentiveApiVersion,
	                String name,
	                String email,
	                String feedback,
	                String feedbackType,
	                Date feedbackDate) {
		super();
		params.put("record[device][uuid]", uuid);
		params.put("record[device][model]", model);
		params.put("record[device][os_version]", osVersion);
		params.put("record[device][carrier]", carrier);
		params.put("record[client][version]", apptentiveApiVersion);
		params.put("record[client][os]", "Android");
		params.put("record[user][name]", name);
		params.put("record[user][email]", email);
		params.put("record[feedback][feedback]", feedback);
		params.put("record[feedback][type]", feedbackType);
		params.put("record[date]", Util.dateToString(feedbackDate, Util.STRINGSAFE_DATE_FORMAT));
	}
}
