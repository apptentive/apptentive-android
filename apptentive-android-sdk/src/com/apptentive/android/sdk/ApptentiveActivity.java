/*
 * ApptentiveActivity.java
 *
 * Created by Sky Kelsey on 2011-09-18.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import com.apptentive.android.sdk.activity.BaseActivity;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.FeedbackController;
import com.apptentive.android.sdk.survey.SurveyController;
import com.apptentive.android.sdk.survey.SurveyManager;

public class ApptentiveActivity  extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		String module = getIntent().getStringExtra("module");

		// Inflate the wrapper view, and then inflate the content view into it
/*
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = inflater.inflate(R.layout.apptentive_activity, null);
		LinearLayout contentParent = (LinearLayout) root.findViewById(R.id.apptentive_activity_content_view);
*/

		//setContentView(R.layout.apptentive_activity);
		//contentParent.removeAllViews();

		switch(Module.valueOf(module)){
			case FEEDBACK:
				//inflater.inflate(R.layout.apptentive_feedback, contentParent);
				setContentView(R.layout.apptentive_feedback);
				new FeedbackController(this, getIntent().getBooleanExtra("forced", false));
				break;
			case SURVEY:
				ApptentiveModel model = ApptentiveModel.getInstance();
				if(model.getSurveys() == null || model.getSurveys().size() == 0){
					finish();
					return;
				}
				//inflater.inflate(R.layout.apptentive_feedback, contentParent);
				setContentView(R.layout.apptentive_survey);
				new SurveyController(this);
				break;
			default:
				//inflater.inflate(R.layout.apptentive_feedback, contentParent);
				//setContentView(root);
				new FeedbackController(this, getIntent().getBooleanExtra("forced", false));
				break;
		}
	}

	public static enum Module{
		FEEDBACK,
		SURVEY
	}
}
