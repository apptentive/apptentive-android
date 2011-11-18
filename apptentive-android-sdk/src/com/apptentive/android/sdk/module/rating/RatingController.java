/*
 * RatingController.java
 *
 * Created by Sky Kelsey on 2011-09-17.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.GlobalInfo;
import com.apptentive.android.sdk.module.metric.MetricPayload;
import com.apptentive.android.sdk.offline.PayloadManager;

public class RatingController {

	private Context context;
	private Dialog dialog;

	public RatingController(Context context) {
		this.context = context;
	}

	public void show() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.apptentive_rating, null, false);
		dialog = new Dialog(context);

		dialog.setContentView(content);
		dialog.setTitle("Rate " + GlobalInfo.appDisplayName + "?");

		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		int width = new Float(display.getWidth() * 0.8f).intValue();

		TextView message = (TextView) dialog.findViewById(R.id.apptentive_rating_message);
		message.setWidth(width);
		message.setText(String.format(context.getString(R.string.apptentive_rating_message), GlobalInfo.appDisplayName));
		Button rate = (Button) dialog.findViewById(R.id.apptentive_rating_rate);
		rate.setOnClickListener(clickListener);
		rate.setText(String.format(context.getString(R.string.apptentive_rating_rate), GlobalInfo.appDisplayName));
		Button later = (Button) dialog.findViewById(R.id.apptentive_rating_later);
		later.setOnClickListener(clickListener);
		Button no = (Button) dialog.findViewById(R.id.apptentive_rating_no);
		no.setOnClickListener(clickListener);

		// Instrumentation
		MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__launch);
		PayloadManager.getInstance().putPayload(metric);

		dialog.show();
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View view) {
			dialog.dismiss();
			int id = view.getId();
			if(id == R.id.apptentive_rating_rate){
				try{
					// Instrumentation
					MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__rate);
					PayloadManager.getInstance().putPayload(metric);
					// Send user to app rating page
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GlobalInfo.appPackage)));
					Apptentive.getInstance().ratingYes();
				}catch(ActivityNotFoundException e) {
					final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
					alertDialog.setTitle("Oops!");
					alertDialog.setMessage(context.getString(R.string.apptentive_rating_no_market));
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface, int i) {
							alertDialog.dismiss();
						}
					});
					alertDialog.show();
				}finally{
					dialog.dismiss();
				}
			}else if(id == R.id.apptentive_rating_later){
				// Instrumentation
				MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__remind);
				PayloadManager.getInstance().putPayload(metric);
				Apptentive.getInstance().ratingRemind();
			}else if(id == R.id.apptentive_rating_no){
				// Instrumentation
				MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__decline);
				PayloadManager.getInstance().putPayload(metric);
				Apptentive.getInstance().ratingNo();
			}
		}
	};
}
