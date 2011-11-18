/*
 * ChoiceController.java
 *
 * Created by Sky Kelsey on 2011-09-17.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.enjoyment;

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
import com.apptentive.android.sdk.module.metric.MetricPayload;
import com.apptentive.android.sdk.module.rating.RatingController;
import com.apptentive.android.sdk.offline.PayloadManager;

public class EnjoymentController {

	private Activity activity;
	private Dialog dialog;

	private Trigger reason;
	public enum Trigger{
		days,
		uses,
		events,
		forced
	}

	public EnjoymentController(Activity activity, Trigger reason) {
		this.activity = activity;
		this.reason = reason == null ? Trigger.forced : reason;
	}

	public void show(){
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.apptentive_choice, null, false);
		dialog = new Dialog(activity);

		dialog.setContentView(content);
		setupForm();

		// Instrumentation
		MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__launch);
		if(reason != null){
			metric.putData("trigger", reason.name());
		}
		PayloadManager.getInstance().putPayload(metric);

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
		public void onClick(View view) {
			dialog.dismiss();
			int id = view.getId();
			if(id == R.id.apptentive_choice_no){
				// Instrumentation
				MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__no);
				PayloadManager.getInstance().putPayload(metric);
				ApptentiveModel.getInstance().setState(ApptentiveState.DONE);
				Apptentive.getInstance().feedback(activity, false);
			}else if(id == R.id.apptentive_choice_yes){
				// Instrumentation
				MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__yes);
				PayloadManager.getInstance().putPayload(metric);
				RatingController controller = new RatingController(activity);
				controller.show();
			}
		}
	};
}
