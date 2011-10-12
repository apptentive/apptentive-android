package com.apptentive.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.apptentive.android.sdk.Apptentive;

public class DemoActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//final Apptentive apptentive = Apptentive.initialize(this, "Demo Activity", "0d7c775a973b30ed6a8ca2cf6469af3168a8c5e38ccd26755d1fdaa3387c6454", 5, 10, 5, 4); // api.apptentive.com
		final Apptentive apptentive = Apptentive.initialize(this, "Demo Activity", "7a9974bf16d7b2c354f2eb2fca612c25fdab62aebeeb3f80767c70e53b35cbd9", 5, 10, 5, 4);   //api.apptentive-beta.com

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
				apptentive.choice();
			}
		});
		Button ratingsButton = (Button) findViewById(R.id.button_ratings);
		ratingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.rating();
			}
		});
		Button feedbackButton = (Button) findViewById(R.id.button_feedback);
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.feedback(true);
			}
		});
		Button surveyButton = (Button) findViewById(R.id.button_survey);
		surveyButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.survey();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Apptentive.getInstance().runIfNeeded();
	}
}
