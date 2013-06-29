/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveyManager {

	private static final String KEY_SURVEYS = "surveys";

	public static void asynchFetchAndStoreSurveysIfCacheExpired(final Context context) {
		if (hasCacheExpired(context)) {
			Log.d("Survey cache has expired. Fetching new surveys.");
			new Thread() {
				public void run() {
					fetchAndStoreSurveys(context);
				}
			}.start();
		} else {
			Log.d("Survey cache has not expired. Using existing surveys.");
		}
	}

	public static void fetchAndStoreSurveys(Context context) {
		if (GlobalInfo.conversationToken == null) {
			return;
		}
		ApptentiveHttpResponse response = ApptentiveClient.getSurveys();
		if (response != null && response.isSuccessful()) {
			String surveysString = response.getContent();

			// Store new survey cache expiration.
			String cacheControl = response.getHeaders().get("Cache-Control");
			Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
			if (cacheSeconds == null) {
				cacheSeconds = Constants.CONFIG_DEFAULT_SURVEY_CACHE_EXPIRATION_DURATION_SECONDS;
			}
			updateCacheExpiration(context, cacheSeconds);
			storeSurveys(context, surveysString);
		}
	}

	private static boolean hasCacheExpired(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		long expiration = prefs.getLong(Constants.PREF_KEY_SURVEYS_CACHE_EXPIRATION, 0);
		return expiration < System.currentTimeMillis();
	}

	/**
	 * Set the duration for which the survey cache is valid.
	 *
	 * @param duration The cache duration in seconds.
	 */
	private static void updateCacheExpiration(Context context, long duration) {
		long expiration = System.currentTimeMillis() + (duration * 1000);
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putLong(Constants.PREF_KEY_SURVEYS_CACHE_EXPIRATION, expiration).commit();
	}

	protected static List<SurveyDefinition> parseSurveysString(String surveyString) {
		try {
			JSONObject root = new JSONObject(surveyString);
			if (!root.isNull(KEY_SURVEYS)) {
				List<SurveyDefinition> ret = new ArrayList<SurveyDefinition>();
				JSONArray items = root.getJSONArray(KEY_SURVEYS);
				for (int i = 0; i < items.length(); i++) {
					String json = items.getJSONObject(i).toString();
					ret.add(new SurveyDefinition(json));
				}
				return ret;
			}
		} catch (JSONException e) {
			Log.e("Error parsing surveys JSON.", e);
		}
		return null;
	}

	protected static String marshallSurveys(List<SurveyDefinition> surveys) {
		try {
			JSONObject json = new JSONObject();
			JSONArray array = new JSONArray();
			json.put(KEY_SURVEYS, array);
			for (SurveyDefinition survey : surveys) {
				array.put(survey);
			}
			return json.toString();
		} catch (JSONException e) {
			Log.e("Error storing Surveys", e);
		}
		return null;
	}

	private static List<SurveyDefinition> loadSurveys(Context context) {
		Log.d("Loading surveys.");
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String surveyString = prefs.getString(Constants.PREF_KEY_SURVEYS, null);
		if (surveyString != null) {
			return parseSurveysString(surveyString);
		}
		return null;
	}

	private static void storeSurveys(Context context, List<SurveyDefinition> surveys) {
		String surveysString = marshallSurveys(surveys);
		if (surveysString == null) {
			return;
		}
		storeSurveys(context, surveysString);
	}

	private static void storeSurveys(Context context, String surveysString) {
		Log.v("Storing surveys: " + surveysString);
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_SURVEYS, surveysString).commit();
	}

	public static boolean isSurveyAvailable(Context context, String... tags) {
		SurveyDefinition surveyDefinition = getFirstMatchingSurvey(context, tags);
		return surveyDefinition != null;
	}

	public static boolean showSurvey(Context context, OnSurveyFinishedListener listener, boolean useDialog, String... tags) {
		SurveyDefinition surveyDefinition = getFirstMatchingSurvey(context, tags);
		if (surveyDefinition != null) {
			Log.d("A matching survey was found.");
			if(useDialog) {
				SurveyModule.getInstance().showSurveyAsDialog(context, surveyDefinition, listener);
			} else {
				SurveyModule.getInstance().show(context, surveyDefinition, listener);
			}
			return true;
		}
		Log.d("No matching survey available.");
		return false;
	}

	private static SurveyDefinition getFirstMatchingSurvey(Context context, String... tags) {
		List<SurveyDefinition> surveys = loadSurveys(context);
		if (surveys == null || surveys.size() == 0) {
			return null;
		}

		for (SurveyDefinition survey : surveys) {
			List<String> surveyTags = survey.getTags();
			if (tags.length == 0) { // Case: Need untagged survey.
				if (surveyTags == null || surveyTags.size() == 0) {
					return survey;
				}
			} else { // Case: Need tagged survey.
				if (surveyTags != null) {
					for (String tag : tags) {
						if (surveyTags.contains(tag)) {
							return survey;
						}
					}
				}
			}
		}
		return null;
	}
}
