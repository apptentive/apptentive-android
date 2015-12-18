/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Invocation extends JSONObject {

	private static final String KEY_INTERACTION_ID = "interaction_id";
	private static final String KEY_CRITERIA = "criteria";

	public Invocation(String json) throws JSONException {
		super(json);
	}

	public String getInteractionId() {
		try {
			if (!isNull(KEY_INTERACTION_ID)) {
				return getString(KEY_INTERACTION_ID);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public boolean isCriteriaMet(Context context) {
		try {
			if (!isNull(KEY_CRITERIA)) {
				JSONObject criteriaObject = getJSONObject(KEY_CRITERIA);
				InteractionCriteria criteria = new InteractionCriteria(criteriaObject.toString());
				return criteria.isMet(context);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return false;
	}
}
