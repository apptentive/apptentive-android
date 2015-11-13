/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Sky Kelsey
 */
public class InteractionCriteria extends JSONObject {

	public InteractionCriteria(String json) throws JSONException {
		super(json);
	}

	public boolean isMet(Context context) {
		try {
			Clause criteria = ClauseParser.parse(null, this);
			Log.i("Evaluating Criteria");
			boolean ret = criteria.evaluate(context);
			Log.i("- => %b", ret);
			return ret;
		} catch (JSONException e) {
			Log.w("Error parsing and running InteractionCriteria predicate logic.", e);
		} catch (Exception e) {
			Log.w("Error parsing and running InteractionCriteria predicate logic.", e);
		}
		return false;
	}
}
