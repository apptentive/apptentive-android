/*
 * SurveyManager.java
 *
 * Created by Sky Kelsey on 2011-10-08.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SurveyManager {

	public static SurveyDefinition parseSurvey(String json) throws JSONException {
		JSONObject survey = new JSONObject(json);
		return new SurveyDefinition(survey);
	}
}
