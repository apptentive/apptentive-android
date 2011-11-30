/*
 * RatingController.java
 *
 * Created by Sky Kelsey on 2011-09-17.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 * Edited by Dr. Cocktor on 2011-11-29.
 * 		+ Updated to support pluggable ratings
 * 		+ Changes Copyright 2011 MiKandi, LLC. All right reserved.
 */

package com.apptentive.android.sdk.module.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

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
				String errorMessage = context.getString(R.string.apptentive_rating_error);
				try{
					// Instrumentation
					MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__rate);
					PayloadManager.getInstance().putPayload(metric);
					// Add some default info to the rating provider page
					if(!GlobalInfo.ratingArgs.containsKey("package")) {
						GlobalInfo.ratingArgs.put("package", GlobalInfo.appPackage);
					}
					if(!GlobalInfo.ratingArgs.containsKey("name")) {
						GlobalInfo.ratingArgs.put("name", GlobalInfo.appDisplayName);
					}
					// Send user to app rating page
					IRatingProvider ratingProvider = GlobalInfo.ratingProvider.newInstance();
					errorMessage = ratingProvider.activityNotFoundMessage(context);
					ratingProvider.startRating(context, GlobalInfo.ratingArgs);
					Apptentive.getInstance().ratingYes();
				}catch(ActivityNotFoundException e) {
					displayError(errorMessage);
				}catch (IllegalAccessException e) {
					displayError(context.getString(R.string.apptentive_rating_error));
				}catch (InstantiationException e) {
					displayError(context.getString(R.string.apptentive_rating_error));
				} catch (InsufficientRatingArgumentsException e) {
					// TODO: Log a message to apptentive to let the
					// developer know that their custom rating provider
					// puked?
					displayError(context.getString(R.string.apptentive_rating_error));
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
	
	private void displayError(final String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle("Oops!");
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
	}
}
