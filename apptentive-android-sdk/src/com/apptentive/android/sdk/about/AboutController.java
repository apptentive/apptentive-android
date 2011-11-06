/*
 * AboutController.java
 *
 * Created by Sky Kelsey on 2011-11-05.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.about;

import android.app.Activity;
import android.view.View;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ViewController;

public class AboutController implements ViewController {

	private Activity activity;

	public AboutController(Activity activity) {
		this.activity = activity;
		activity.setContentView(R.layout.apptentive_about);
		activity.findViewById(R.id.apptentive_button_about_okay).setOnClickListener(clickListener);
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.apptentive_button_about_okay:
					activity.finish();
					break;
				default:
					break;
			}
		}
	};
	@Override
	public void cleanup() {
	}
}
