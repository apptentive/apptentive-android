package com.apptentive.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.module.survey.OnSurveyCompletedListener;
import com.apptentive.android.sdk.module.survey.OnSurveyFetchedListener;

/**
 * This is an example of how to integrate Apptentive into your Application if you are not able to inherit from
 * {@link com.apptentive.android.sdk.ApptentiveActivity}. In this case, you must hook Apptentive up to your Activity's
 * onCreate(), onStart(), onStop(), and onDestroy() methods.
 *
 * @author Sky Kelsey
 */
public class AlternateExampleActivity extends Activity {
	private static String LOG_TAG = "Alternate Apptentive Example";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Apptentive.onStart(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Apptentive.getRatingModule().run(this);
		}

	}

	@Override
	protected void onStop() {
		Apptentive.onStop(this);
		super.onStop();
	}

	public void onFeedbackButtonPressed(View view) {
		Apptentive.getFeedbackModule().forceShowFeedbackDialog(this);
	}

	public void onFetchSurveyButtonPressed(View view) {
		Apptentive.getSurveyModule().fetchSurvey(new OnSurveyFetchedListener() {
			public void onSurveyFetched(final boolean success) {
				Log.e(LOG_TAG, "onSurveyFetched(" + success + ")");
				runOnUiThread(new Runnable() {
					public void run() {
						Toast toast = Toast.makeText(AlternateExampleActivity.this, success ? "Survey fetch successful." : "Survey fetch failed.", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						findViewById(R.id.show_survey_button).setEnabled(success);
					}
				});
			}
		});
	}

	public void onShowSurveyButtonPressed(View view) {
		Apptentive.getSurveyModule().show(this, new OnSurveyCompletedListener() {
			public void onSurveyCompletedListener() {
				Log.e(LOG_TAG, "Got a callback from completed survey!");
			}
		});
	}
}
