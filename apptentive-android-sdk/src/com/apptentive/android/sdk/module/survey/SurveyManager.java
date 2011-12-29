/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class SurveyManager {

	public static SurveyDefinition parseSurvey(String json) throws JSONException {
		JSONObject survey = new JSONObject(json);
		return new SurveyDefinition(survey);
	}
}
