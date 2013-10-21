package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.test.AndroidTestCase;
import com.apptentive.android.sdk.Log;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SurveySelectionTest extends AndroidTestCase {

	static final String SURVEYS_JSON = "{\"surveys\":[{\"id\":\"51c251124712c792dd000009\",\"tags\":[\"test_tag_2\"],\"description\":\"Tagged with \\\"test_tag_2\\\"\",\"name\":\"Tagged Survey 2\",\"questions\":[{\"id\":\"51c251124712c792dd00000a\",\"answer_choices\":[{\"id\":\"51c251124712c792dd00000b\",\"value\":\"Away from the wall\"},{\"id\":\"51c251124712c792dd00000c\",\"value\":\"Against the wall\"}],\"value\":\"Toilet paper should roll out... \",\"instructions\":\"select one\",\"type\":\"multichoice\",\"required\":true}],\"active\":true,\"show_success_message\":false,\"app_id\":\"517884df584ef064fc00000e\",\"date\":\"2013-06-20T00:47:14Z\",\"required\":false,\"multiple_responses\":false},{\"id\":\"521e9d464712c7bb14000036\",\"tags\":[\"test_tag_3\"],\"description\":\"A very short survey\",\"name\":\"Shorty (Tagged 3)\",\"questions\":[{\"id\":\"521e9d464712c7bb14000037\",\"value\":\"Hi.\",\"type\":\"singleline\",\"required\":true,\"multiline\":false}],\"start_time\":\"2013-08-29T00:58:12Z\",\"active\":true,\"show_success_message\":false,\"app_id\":\"517884df584ef064fc00000e\",\"date\":\"2013-08-29T01:00:54Z\",\"required\":false,\"multiple_responses\":false},{\"tags\":[\"test_tag_1\"],\"view_period\":3,\"questions\":[{\"id\":\"522125eb4712c7601d00002f\",\"answer_choices\":[{\"id\":\"522125eb4712c7601d000030\",\"value\":\"Heads\"},{\"id\":\"522125eb4712c7601d000031\",\"value\":\"Tails\"}],\"value\":\"Flip a coin!\",\"instructions\":\"select one\",\"type\":\"multichoice\",\"required\":true}],\"date\":\"2013-08-30T23:08:27Z\",\"id\":\"522125eb4712c7601d00002e\",\"description\":\"Tagged with \\\"test_tag_1\\\"\",\"name\":\"Tagged Survey 1\",\"start_time\":\"2013-08-30T20:33:32Z\",\"active\":true,\"show_success_message\":false,\"app_id\":\"517884df584ef064fc00000e\",\"required\":false,\"multiple_responses\":false,\"view_count\":3}]}";

	public void testSurveyRateLimiting() {
		Log.i("testSurveyRateLimiting()");
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

	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
