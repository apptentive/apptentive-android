/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Targets extends JSONObject {

	public static final String KEY_NAME = "targets";

	public Targets(String json) throws JSONException {
		super(json);
	}

	public String getApplicableInteraction(Context context, String eventLabel) {
		JSONArray invocations = optJSONArray(eventLabel);
		if (invocations != null) {
			for (int i = 0; i < invocations.length(); i++) {
				JSONObject invocationObject = invocations.optJSONObject(i);
				if (invocationObject != null) {
					try {
						Invocation invocation = new Invocation(invocationObject.toString());
						if (invocation.isCriteriaMet(context)) {
							return invocation.getInteractionId();
						}
					} catch (JSONException e) {
						//
					}
				}
			}
		}
		Log.v("No runnable Interactions for EventLabel: %s", eventLabel);
		return null;
	}
}
