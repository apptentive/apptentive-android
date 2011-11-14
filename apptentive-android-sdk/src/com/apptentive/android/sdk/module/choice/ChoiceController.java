/*
 * ChoiceController.java
 *
 * Created by Sky Kelsey on 2011-09-17.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.choice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.ApptentiveState;
import com.apptentive.android.sdk.model.GlobalInfo;
import com.apptentive.android.sdk.module.rating.RatingController;

public class ChoiceController{

	private Activity activity;
	private Dialog dialog;

	public ChoiceController(Activity activity) {
		this.activity = activity;
	}

	public void show(){
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.apptentive_choice, null, false);
		dialog = new Dialog(activity);

		dialog.setContentView(content);
		setupForm();
		dialog.show();
	}

	private void setupForm(){
		dialog.setTitle("Are you enjoying " + GlobalInfo.appDisplayName + "?");
		Button yes = (Button) dialog.findViewById(R.id.apptentive_choice_yes);
		yes.setOnClickListener(clickListener);
		Button no = (Button) dialog.findViewById(R.id.apptentive_choice_no);
		no.setOnClickListener(clickListener);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			dialog.dismiss();
			int id = view.getId();
			if(id == R.id.apptentive_choice_no){
				ApptentiveModel.getInstance().setState(ApptentiveState.DONE);
				Apptentive.getInstance().feedback(activity, false);
			}else if(id == R.id.apptentive_choice_yes){
				RatingController controller = new RatingController(activity);
				controller.show();
			}
		}
	};
}
