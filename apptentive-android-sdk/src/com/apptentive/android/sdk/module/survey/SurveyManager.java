/*
 * SurveyManager.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.survey;

import org.json.JSONException;
import org.json.JSONObject;

public class SurveyManager {

	public static SurveyDefinition parseSurvey(String json) throws JSONException {
		JSONObject survey = new JSONObject(json);
		return new SurveyDefinition(survey);
	}
}
