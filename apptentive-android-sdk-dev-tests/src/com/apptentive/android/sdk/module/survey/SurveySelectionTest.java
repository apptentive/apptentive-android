package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.test.AndroidTestCase;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveySelectionTest extends AndroidTestCase {

	static final String SURVEYS_JSON =
			"{\n" +
			"    \"surveys\": [\n" +
			"        {\n" +
			"            \"id\": \"51c251124712c792dd000009\",\n" +
			"            \"tags\": [\n" +
			"                \"test_tag_2\"\n" +
			"            ],\n" +
			"            \"description\": \"Tagged with \\\"test_tag_2\\\"\",\n" +
			"            \"name\": \"Tagged Survey 2\",\n" +
			"            \"questions\": [\n" +
			"                {\n" +
			"                    \"id\": \"51c251124712c792dd00000a\",\n" +
			"                    \"answer_choices\": [\n" +
			"                        {\n" +
			"                            \"id\": \"51c251124712c792dd00000b\",\n" +
			"                            \"value\": \"Away from the wall\"\n" +
			"                        },\n" +
			"                        {\n" +
			"                            \"id\": \"51c251124712c792dd00000c\",\n" +
			"                            \"value\": \"Against the wall\"\n" +
			"                        }\n" +
			"                    ],\n" +
			"                    \"value\": \"Toilet paper should roll out... \",\n" +
			"                    \"instructions\": \"select one\",\n" +
			"                    \"type\": \"multichoice\",\n" +
			"                    \"required\": true\n" +
			"                }\n" +
			"            ],\n" +
			"            \"active\": true,\n" +
			"            \"show_success_message\": false,\n" +
			"            \"app_id\": \"517884df584ef064fc00000e\",\n" +
			"            \"date\": \"2013-06-20T00:47:14Z\",\n" +
			"            \"required\": false,\n" +
			"            \"multiple_responses\": true\n" +
			"        },\n" +
			"        {\n" +
			"            \"id\": \"521e9d464712c7bb14000036\",\n" +
			"            \"tags\": [\n" +
			"                \"test_tag_3\"\n" +
			"            ],\n" +
			"            \"description\": \"A very short survey\",\n" +
			"            \"name\": \"Shorty (Tagged 3)\",\n" +
			"            \"questions\": [\n" +
			"                {\n" +
			"                    \"id\": \"521e9d464712c7bb14000037\",\n" +
			"                    \"value\": \"Hi.\",\n" +
			"                    \"type\": \"singleline\",\n" +
			"                    \"required\": true,\n" +
			"                    \"multiline\": false\n" +
			"                }\n" +
			"            ],\n" +
			"            \"start_time\": \"2013-08-29T00:58:12Z\",\n" +
			"            \"active\": true,\n" +
			"            \"show_success_message\": false,\n" +
			"            \"app_id\": \"517884df584ef064fc00000e\",\n" +
			"            \"date\": \"2013-08-29T01:00:54Z\",\n" +
			"            \"required\": false,\n" +
			"            \"multiple_responses\": false\n" +
			"        },\n" +
			"        {\n" +
			"            \"tags\": [\n" +
			"                \"test_tag_1\"\n" +
			"            ],\n" +
			"            \"view_period\": 3,\n" +
			"            \"questions\": [\n" +
			"                {\n" +
			"                    \"id\": \"522125eb4712c7601d00002f\",\n" +
			"                    \"answer_choices\": [\n" +
			"                        {\n" +
			"                            \"id\": \"522125eb4712c7601d000030\",\n" +
			"                            \"value\": \"Heads\"\n" +
			"                        },\n" +
			"                        {\n" +
			"                            \"id\": \"522125eb4712c7601d000031\",\n" +
			"                            \"value\": \"Tails\"\n" +
			"                        }\n" +
			"                    ],\n" +
			"                    \"value\": \"Flip a coin!\",\n" +
			"                    \"instructions\": \"select one\",\n" +
			"                    \"type\": \"multichoice\",\n" +
			"                    \"required\": true\n" +
			"                }\n" +
			"            ],\n" +
			"            \"date\": \"2013-08-30T23:08:27Z\",\n" +
			"            \"id\": \"522125eb4712c7601d00002e\",\n" +
			"            \"description\": \"Tagged with \\\"test_tag_1\\\"\",\n" +
			"            \"name\": \"Tagged Survey 1\",\n" +
			"            \"start_time\": \"2013-08-30T20:33:32Z\",\n" +
			"            \"active\": true,\n" +
			"            \"show_success_message\": false,\n" +
			"            \"app_id\": \"517884df584ef064fc00000e\",\n" +
			"            \"required\": false,\n" +
			"            \"multiple_responses\": true,\n" +
			"            \"view_count\": 3\n" +
			"        }\n" +
			"    ]\n" +
			"}";

	public void testSurveyRateLimiting() {
		Log.i("testSurveyRateLimiting()");
		resetDevice();

		Context context = getContext();
		List<SurveyDefinition> surveyList = SurveyManager.parseSurveysString(SURVEYS_JSON);
		SurveyManager.storeSurveys(context, surveyList);
		SurveyDefinition surveyDefinition = surveyList.get(2);

		Log.i("TEST: "+System.currentTimeMillis());
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		Log.i("SHOW: " + System.currentTimeMillis());
		SurveyManager.recordShow(context, surveyDefinition);
		pause(750);

		Log.i("TEST: "+System.currentTimeMillis());
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		Log.i("SHOW: " + System.currentTimeMillis());
		SurveyManager.recordShow(context, surveyDefinition);
		pause(750);

		Log.i("TEST: "+System.currentTimeMillis());
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		Log.i("SHOW: " + System.currentTimeMillis());
		SurveyManager.recordShow(context, surveyDefinition);
		pause(500);

		assertFalse(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		pause(1000);

		Log.i("TEST: "+System.currentTimeMillis());
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		Log.i("SHOW: " + System.currentTimeMillis());
		SurveyManager.recordShow(context, surveyDefinition);
		pause(250);

		Log.i("TEST: "+System.currentTimeMillis());
		assertFalse(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
		pause(1000);

		Log.i("TEST: "+System.currentTimeMillis());
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_1"));
	}

	public void testSurveyMultipleResponses() {
		Log.i("testSurveyMultipleResponses()");
		resetDevice();

		Context context = getContext();
		List<SurveyDefinition> surveyList = SurveyManager.parseSurveysString(SURVEYS_JSON);
		SurveyManager.storeSurveys(context, surveyList);

		SurveyDefinition surveyDefinition = surveyList.get(0);
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_2"));
		SurveyManager.recordShow(context, surveyDefinition);
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_2"));
		SurveyManager.recordShow(context, surveyDefinition);
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_2"));

		surveyDefinition = surveyList.get(1);
		assertTrue(SurveyManager.isSurveyAvailable(context, "test_tag_3"));
		SurveyManager.recordShow(context, surveyDefinition);
		assertFalse(SurveyManager.isSurveyAvailable(context, "test_tag_3"));
		SurveyManager.recordShow(context, surveyDefinition);
		assertFalse(SurveyManager.isSurveyAvailable(context, "test_tag_3"));

	}

	private void resetDevice() {
		getContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().commit();
	}

	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
