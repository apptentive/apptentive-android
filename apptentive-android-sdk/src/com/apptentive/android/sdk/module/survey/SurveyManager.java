/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.SurveyModule;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @author Sky Kelsey
 */
public class SurveyManager {

	private static final String KEY_SURVEYS = "surveys";

	public static void asyncFetchAndStoreSurveysIfCacheExpired(final Context context) {
		if (hasCacheExpired(context)) {
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			boolean deviceDataSent = prefs.getBoolean(Constants.PREF_KEY_DEVICE_DATA_SENT, false);
			Log.d("Survey cache has expired. Fetching new surveys.");
			if (deviceDataSent) { // Don't allow survey fetches until Device info has been sent at least once.
				Thread thread = new Thread() {
					public void run() {
						fetchAndStoreSurveys(context);
					}
				};
				Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread thread, Throwable throwable) {
						Log.w("UncaughtException in SurveyManager.", throwable);
						MetricModule.sendError(context.getApplicationContext(), throwable, null, null);
					}
				};
				thread.setUncaughtExceptionHandler(handler);
				thread.setName("Apptentive-FetchSurveys");
				thread.start();
			} else {
				Log.d("Can't fetch surveys because Device info has not been sent.");
			}
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

			// Prune out surveys that have met serving requirements already.
			List<SurveyDefinition> surveyList = parseSurveysString(surveysString);
			// If a null survey list is returned, notify the server. This shouldn't ever happen.
			if (surveyList == null) {
				MetricModule.sendError(context, null, "Survey list returned null because of possible parsing error.", surveysString);
				return;
			}
			Iterator<SurveyDefinition> surveyIterator = surveyList.iterator();
			while (surveyIterator.hasNext()) {
				SurveyDefinition next = surveyIterator.next();
				// Filter out surveys that have met of exceeded the number of allowed displays per time period.
				if (SurveyHistory.isSurveyLimitMet(context, next)) {
					Log.d("Removing survey: " + next.getName());
					surveyIterator.remove();
				}
			}
			storeSurveys(context, surveyList);
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

	public static List<SurveyDefinition> parseSurveysString(String surveyString) {
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

	public static void storeSurveys(Context context, List<SurveyDefinition> surveys) {
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

	public static boolean showSurvey(Activity activity, OnSurveyFinishedListener listener, String... tags) {
		SurveyDefinition surveyDefinition = getFirstMatchingSurvey(activity, tags);
		if (surveyDefinition != null) {
			Log.d("A matching survey was found.");
			recordShow(activity, surveyDefinition);
			SurveyModule.getInstance().show(activity, surveyDefinition, listener);
			return true;
		}
		Log.d("No matching survey available.");
		return false;
	}

	public static void recordShow(Context context, SurveyDefinition surveyDefinition) {
		SurveyHistory.recordSurveyDisplay(context, surveyDefinition.getId(), System.currentTimeMillis());
	}

	private static SurveyDefinition getFirstMatchingSurvey(Context context, String... tags) {
		Set<String> tagsSet = new HashSet<String>();
		// Remove duplicates
		for (String tag : tags) {
			tag = tag.trim();
			// Remove empty strings.
			if (tag.length() > 0) {
				tagsSet.add(tag);
			}
		}

		List<SurveyDefinition> surveys = loadSurveys(context);
		if (surveys == null || surveys.size() == 0) {
			return null;
		}

		for (SurveyDefinition survey : surveys) {
			List<String> surveyTags = survey.getTags();
			if (tagsSet.size() == 0) { // Case: Need untagged survey.
				if (surveyTags == null || surveyTags.size() == 0) {
					if (isSurveyValid(context, survey)) {
						return survey;
					}
				}
			} else { // Case: Need tagged survey.
				if (surveyTags != null) {
					for (String tag : tagsSet.toArray(new String[tagsSet.size()])) {
						if (surveyTags.contains(tag)) {
							if (isSurveyValid(context, survey)) {
								return survey;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static boolean isSurveyValid(Context context, SurveyDefinition survey) {
		boolean expired = false;
		String endTimeString = survey.getEndTime();
		if (endTimeString != null) {
			Time endTime = new Time();
			try {
				long now = System.currentTimeMillis();
				Time currentTime = new Time();
				currentTime.set(now);
				endTime.parse3339(endTimeString);
				long expirationTime = endTime.normalize(false);
				if (expirationTime < System.currentTimeMillis()) {
					expired = true;
				}
			} catch (NumberFormatException e) {
				Log.w("Error parsing end time for survey: %s", e, survey.getId());
			}
		}
		boolean ret = !expired && !SurveyHistory.isSurveyLimitMet(context, survey);
		Log.d("Survey is valid: " + ret);
		return ret;
	}
}
