/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.RatingModule;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteractionView extends InteractionView<RatingDialogInteraction> {

	private static final String CODE_POINT_LAUNCH = "launch";
	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_RATE = "rate";
	private static final String CODE_POINT_REMIND = "remind";
	private static final String CODE_POINT_DECLINE = "decline";

	public RatingDialogInteractionView(RatingDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(final Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_rating_dialog_interaction);

		Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_LAUNCH);

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) activity.findViewById(R.id.title);
			titleView.setText(title);
		}

		TextView bodyView = (TextView) activity.findViewById(R.id.body);
		String body = interaction.getBody();
		if (body == null) {
			body = String.format(activity.getResources().getString(R.string.apptentive_rating_message_fs), Configuration.load(activity).getAppDisplayName());
		}
		bodyView.setText(body);

		// Rate
		Button rateButton = (Button) activity.findViewById(R.id.rate);
		String rate = interaction.getRateText();
		if (rate == null) {
			rate = String.format(activity.getResources().getString(R.string.apptentive_rate_this_app), Configuration.load(activity).getAppDisplayName());
		}
		rateButton.setText(rate);
		rateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String errorMessage = activity.getString(R.string.apptentive_rating_error);
				try {
					// TODO: Delete the RatingModule class altogether.
					if (RatingModule.getInstance().selectedRatingProvider == null) {
						// Default to the Android Market provider, if none has been specified
						RatingModule.getInstance().selectedRatingProvider = new GooglePlayRatingProvider();
					}
					errorMessage = RatingModule.getInstance().selectedRatingProvider.activityNotFoundMessage(activity);

					String appDisplayName = Configuration.load(activity).getAppDisplayName();
					Map<String, String> finalRatingProviderArgs = new HashMap<String, String>(RatingModule.getInstance().ratingProviderArgs);
					finalRatingProviderArgs.put("name", appDisplayName);

					// Engage, then start the rating.
					Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_RATE);
					RatingModule.getInstance().selectedRatingProvider.startRating(activity, finalRatingProviderArgs);
				} catch (ActivityNotFoundException e) {
					displayError(activity, errorMessage);
				} catch (InsufficientRatingArgumentsException e) {
					// TODO: Log a message to apptentive to let the developer know that their custom rating provider puked?
					displayError(activity, activity.getString(R.string.apptentive_rating_error));
				} finally {
					activity.finish();
				}
			}
		});

		// Remind
		Button remindButton = (Button) activity.findViewById(R.id.remind);
		String remind = interaction.getRemindText();
		if (remind != null) {
			remindButton.setText(remind);
		}
		remindButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_REMIND);
				activity.finish();
			}
		});

		// Decline
		Button declineButton = (Button) activity.findViewById(R.id.decline);
		String decline = interaction.getDeclineText();
		if (decline != null) {
			declineButton.setText(decline);
		}
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_DECLINE);
				activity.finish();
			}
		});
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onBackPressed(Activity activity) {
		Apptentive.engageInternal(activity, interaction.getType().name(), CODE_POINT_CANCEL);
	}

	private void displayError(Activity activity, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle(activity.getString(R.string.apptentive_oops));
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.apptentive_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
	}

}
