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

		final Apptentive apptentive = Apptentive.initialize(this, "Demo Activity", /*TODO: Specify your API Key here: */ "YOUR_API_KEY", 5, 10, 5, 4);

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
		Button choiceButton = (Button) findViewById(R.id.button_choice);
		choiceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				apptentive.choice();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Apptentive.getInstance().runIfNeeded();
	}
}
