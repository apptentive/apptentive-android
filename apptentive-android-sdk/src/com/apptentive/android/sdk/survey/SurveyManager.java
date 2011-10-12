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

	public static List<SurveyDefinition> parseSurveys(String json) throws JSONException {
		List<SurveyDefinition> surveys = new ArrayList<SurveyDefinition>();
		JSONArray surveyArray = new JSONArray(json);
		for(int i = 0; i < surveyArray.length(); i++){
			surveys.add(new SurveyDefinition((JSONObject)surveyArray.get(i)));
		}
		return surveys;
	}
}
