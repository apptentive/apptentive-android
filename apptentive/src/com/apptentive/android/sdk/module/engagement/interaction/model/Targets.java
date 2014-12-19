/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
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
	public static final String KEY_INTERACTION_ID = "interaction_id";
	public static final String KEY_CRITERIA = "criteria";

	public Targets(String json) throws JSONException {
		super(json);
	}

	public String getApplicableInteraction(Context context, String eventLabel) {
		JSONArray targetsForEventLabel = optJSONArray(eventLabel);
		if (targetsForEventLabel != null) {
			for (int i = 0; i < targetsForEventLabel.length(); i++) {
				JSONObject target = targetsForEventLabel.optJSONObject(i);
				if (target != null) {
					JSONObject criteriaObject = target.optJSONObject(KEY_CRITERIA);
					// If criteria is null or missing, it is assumed false.
					if (criteriaObject == null) {
						return null;
					}
					String criteriaString = criteriaObject.toString();
					try {
						InteractionCriteria criteria = new InteractionCriteria(criteriaString);
						if (criteria.isMet(context)) {
							return target.optString(KEY_INTERACTION_ID);
						}
					} catch (JSONException e) {
						Log.e("Invalid InteractionCriteria.", e);
					}
				}
			}
		}
		Log.e("No runnable Interactions for EventLabel: %s", eventLabel);
		return null;
	}
}
