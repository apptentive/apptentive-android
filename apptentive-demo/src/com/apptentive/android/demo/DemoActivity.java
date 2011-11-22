package com.apptentive.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.apptentive.android.sdk.Apptentive;

public class DemoActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("DEMO", "onCreate()");
		setContentView(R.layout.main);

		final Apptentive apptentive = Apptentive.initialize(this, "Demo Activity", "<YOUR_API_KEY>", 5, 10, 5, 4);

		// Add custom data fields to feedback this way:
		//apptentive.addFeedbackDataField("boo", "far");

		Button resetButton = (Button) findViewById(R.id.button_reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.reset();
			}
		});
		Button eventButton = (Button) findViewById(R.id.button_event);
		eventButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.event();
			}
		});
		Button dayButton = (Button) findViewById(R.id.button_day);
		dayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.day();
			}
		});
		Button choiceButton = (Button) findViewById(R.id.button_choice);
		choiceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.enjoyment(DemoActivity.this);
			}
		});
		Button ratingsButton = (Button) findViewById(R.id.button_ratings);
		ratingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.rating(DemoActivity.this);
			}
		});
		Button feedbackButton = (Button) findViewById(R.id.button_feedback);
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.feedback(DemoActivity.this, true);
			}
		});
		Button surveyButton = (Button) findViewById(R.id.button_survey);
		surveyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.survey(DemoActivity.this);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("DEMO", "onResume()");
		Apptentive.getInstance().runIfNeeded(DemoActivity.this);
	}

	@Override
	protected void onDestroy() {
		Log.e("DEMO", "onDestroy()");
		super.onDestroy();
	}
}
