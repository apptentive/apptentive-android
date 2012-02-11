/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.apptentive.android.sdk.module.metric.MetricPayload;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;
import com.apptentive.android.sdk.module.rating.impl.AndroidMarketRatingProvider;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.Util;

/**
 * This module is responsible for determining when to show, and showing the rating flow of dialogs.
 *
 * @author Sky Kelsey
 */
public class RatingModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************

	private static RatingModule instance = null;

	static RatingModule getInstance() {
		if (instance == null) {
			instance = new RatingModule();
		}
		return instance;
	}

	// Default configuration variables
	public static int DEFAULT_DAYS_BEFORE_PROMPT = 30;
	public static int DEFAULT_USES_BEFORE_PROMPT = 5;
	public static int DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = 10;
	public static int DEFAULT_DAYS_BEFORE_REPROMPTING = 5;


	// *************************************************************************************************
	// ********************************************* Private *******************************************
	// *************************************************************************************************

	private SharedPreferences prefs;
	private IRatingProvider selectedRatingProvider = null;

	private int daysBeforePrompt = DEFAULT_DAYS_BEFORE_PROMPT;
	private int usesBeforePrompt = DEFAULT_USES_BEFORE_PROMPT;
	private int significantEventsBeforePrompt = DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT;
	private int daysBeforeReprompting = DEFAULT_DAYS_BEFORE_REPROMPTING;

	private Map<String, String> ratingProviderArgs;

	private RatingModule() {
		ratingProviderArgs = new HashMap<String, String>();
		ratingProviderArgs.put("name", GlobalInfo.appDisplayName);
		ratingProviderArgs.put("package", GlobalInfo.appPackage);
	}

	private boolean ratingPeriodElapsed() {
		RatingState state = getState();
		switch (state) {
			case REMIND:
				return Util.timeHasElapsed(getStartOfRatingPeriod(), daysBeforeReprompting);
			default:
				return Util.timeHasElapsed(getStartOfRatingPeriod(), daysBeforePrompt);
		}
	}

	private boolean eventThresholdReached() {
		return getEvents() >= significantEventsBeforePrompt;
	}

	private boolean usesThresholdReached() {
		return getUses() >= usesBeforePrompt;
	}

	private Date getStartOfRatingPeriod() {
		try {
			return Util.stringToDate(prefs.getString("startOfRatingPeriod", ""));
		} catch (ParseException e) {
			return new Date();
		}
	}

	private void setStartOfRatingPeriod(Date startOfRatingPeriod) {
		prefs.edit().putString("startOfRatingPeriod", Util.dateToString(startOfRatingPeriod)).commit();
	}

	private RatingState getState() {
		return RatingState.valueOf(prefs.getString("ratingState", "START"));
	}

	private void setState(RatingState state) {
		prefs.edit().putString("ratingState", state.name()).commit();
	}

	private int getEvents() {
		return prefs.getInt("events", 0);
	}

	private void setEvents(int events) {
		prefs.edit().putInt("events", events).commit();
	}

	private int getUses() {
		return prefs.getInt("uses", 0);
	}

	private void setUses(int uses) {
		prefs.edit().putInt("uses", uses).commit();
	}

	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	void setContext(Context context) {
		this.prefs = context.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE);
	}

	/**
	 * Ues this to choose where to send the user when they are prompted to rate the app. This should be the same place
	 * that the app was downloaded from.
	 *
	 * @param ratingProvider A {@link RatingProvider} value.
	 */
	public void setRatingProvider(IRatingProvider ratingProvider) {
		this.selectedRatingProvider = ratingProvider;
	}

	/**
	 * If there are any pieces of data your {@link IRatingProvider} needs, add them here.
	 * Default keys already included are: [name, package].
	 * 
	 * @param key The argument name the the chosen {@link IRatingProvider} needs.
	 * @param value The value of the argument.
 	 */
	public void putRatingProviderArg(String key, String value) {
		this.ratingProviderArgs.put(key, value);
	}

	/**
	 * Sets the number of days after installation to wait before showing the rating flow.
	 *
	 * @param daysBeforePrompt The number of days after installation to wait before showing the rating flow.
	 */
	public void setDaysBeforePrompt(int daysBeforePrompt) {
		this.daysBeforePrompt = daysBeforePrompt;
	}

	/**
	 * Sets the number of uses after installation to occur before showing the rating flow.
	 *
	 * @param usesBeforePrompt The number of uses after installation to occur before showing the rating flow.
	 */
	public void setUsesBeforePrompt(int usesBeforePrompt) {
		this.usesBeforePrompt = usesBeforePrompt;
	}

	/**
	 * Sets the number of significant events after installation to occur before showint the rating flow.
	 *
	 * @param significantEventsBeforePrompt The number of significant events after installation to occur before showint the rating flow.
	 */
	public void setSignificantEventsBeforePrompt(int significantEventsBeforePrompt) {
		this.significantEventsBeforePrompt = significantEventsBeforePrompt;
	}

	/**
	 * Sets the number of days after postponing rating to wait before showing the rating flow again.
	 *
	 * @param daysBeforeReprompting The number of days after postponing rating to wait before showing the rating flow again.
	 */
	public void setDaysBeforeReprompting(int daysBeforeReprompting) {
		this.daysBeforeReprompting = daysBeforeReprompting;
	}

	/**
	 * Shows the initial "Are you enjoying this app?" dialog that starts the rating flow.
	 * It will be called if you call RatingModule.run() and any of the usage conditions have been met.
	 *
	 * @param activity The activityContext from which this method was called.
	 */
	public void forceShowEnjoymentDialog(Activity activity) {
		showEnjoymentDialog(activity, Trigger.forced);
	}

	void showEnjoymentDialog(Activity activity, Trigger reason) {
		this.new EnjoymentDialog(activity).show(reason);
	}

	/**
	 * Shows the "Would you please rate this app?" dialog that is the second dialog in the rating flow.
	 * It will be called automatically if the user shooses "Yes" in the "Are you enjoyin this app?" dialog.
	 *
	 * @param activity The acvitity from which this method was called.
	 */
	public void showRatingDialog(Activity activity) {
		this.new RatingDialog(activity).show();
	}

	/**
	 * Start the rating flow dialogs if any of the usage conditions have been met. Call this method if it is
	 * appropriate to show a popup dialog. Generally, you would want to call this method in your Activity's
	 * <strong>onWindowFocusChanged(boolean hasFocus)</strong> method if hasFocus is true.
	 *
	 * @param activity The activityContext from which this method was called.
	 */
	public void run(Activity activity) {
		// TODO: Check to see if a data connection exists first. We don't want to prompt to rate unless one exists.

		switch (getState()) {
			case START:
				if (ratingPeriodElapsed()) {
					if (eventThresholdReached()) {
						showEnjoymentDialog(activity, Trigger.events);
					} else if (usesThresholdReached()) {
						showEnjoymentDialog(activity, Trigger.uses);
					}
				}
				break;
			case REMIND:
				if (ratingPeriodElapsed()) {
					showRatingDialog(activity);
				}
				break;
			case POSTPONE:
				break;
			case RATED:
				break;
			default:
				break;
		}
	}

	/**
	 * Resets the Rating Module metrics such as significant events, start of rating period, and number of uses.
	 * If you would like each new version of your app to be rated, you can call this method upon app upgrade.
	 */
	public void reset() {
		if (RatingState.RATED != getState()) {
			setState(RatingState.START);
			setStartOfRatingPeriod(new Date());
			setEvents(0);
			setUses(0);
		}
	}

	/**
	 * Increments the number of "significant events" the app's user has achieved. What you condider to be a significant
	 * event is up to you to decide. The number of significant events is used be the Rating Module to determine if it
	 * is time to run the rating flow.
	 */
	public void logEvent() {
		setEvents(getEvents() + 1);
	}

	/**
	 * Increments the number of times this app has been used. This method should be called each time your app
	 * starts. The number of uses is used be the Rating Module to determine if it is time to run the rating flow.
	 */
	public void logUse() {
		setUses(getUses() + 1);
	}

	/**
	 * This method is for debugging purposes only. It will move the rating start date one day into the past.
	 *
	 * @deprecated
	 */
	public void day() {
		setStartOfRatingPeriod(Util.addDaysToDate(getStartOfRatingPeriod(), -1));
	}

	// *************************************************************************************************
	// ***************************************** Inner Classes *****************************************
	// *************************************************************************************************

	enum RatingState {
		/**
		 * Initial state after first install.
		 */
		START,
		/**
		 * The user likes the app, but would like us to remind them to rate it.
		 */
		REMIND,
		/**
		 * The user didn't like this version, or doesn't want to rate it. Ask again only after app upgrade/reset.
		 */
		POSTPONE,
		/**
		 * The user has rated the app. No further action will occur.
		 */
		RATED
	}

	enum Trigger {
		uses,
		events,
		forced
	}


	private final class EnjoymentDialog extends Dialog {

		private Activity activity;

		public EnjoymentDialog(Activity activity) {
			super(activity);
			this.activity = activity;
		}

		public void show(Trigger reason) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View content = inflater.inflate(R.layout.apptentive_choice, null, false);

			setContentView(content);

			String title = String.format(activity.getString(R.string.apptentive_enjoyment_message_fs), GlobalInfo.appDisplayName);
			setTitle(title);
			Button yes = (Button) findViewById(R.id.apptentive_choice_yes);
			yes.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					dismiss();
					// Instrumentation
					MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__yes);
					PayloadManager.getInstance().putPayload(metric);
					Apptentive.getInstance().getRatingModule().showRatingDialog(activity);
					dismiss();
				}
			});
			Button no = (Button) findViewById(R.id.apptentive_choice_no);
			no.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// Instrumentation
					MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__no);
					PayloadManager.getInstance().putPayload(metric);
					setState(RatingState.POSTPONE);
					FeedbackModule.getInstance().showFeedbackDialog(activity, FeedbackModule.Trigger.rating);
					dismiss();
				}
			});

			// Instrumentation
			MetricPayload metric = new MetricPayload(MetricPayload.Event.enjoyment_dialog__launch);
			if (reason != null) {
				metric.putData("trigger", reason.name());
			}
			PayloadManager.getInstance().putPayload(metric);

			setCancelable(false);
			super.show();
		}
	}


	private final class RatingDialog extends Dialog {

		private Context activityContext;

		public RatingDialog(Activity activity) {
			super(activity);
			this.activityContext = activity;
		}

		public void show() {
			LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View content = inflater.inflate(R.layout.apptentive_rating, null, false);

			setContentView(content);
			setTitle(activityContext.getString(R.string.apptentive_rating_title));

			Display display = ((WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			int width = new Float(display.getWidth() * 0.8f).intValue();

			TextView message = (TextView) findViewById(R.id.apptentive_rating_message);
			message.setWidth(width);
			message.setText(String.format(activityContext.getString(R.string.apptentive_rating_message_fs), GlobalInfo.appDisplayName));
			Button rate = (Button) findViewById(R.id.apptentive_rating_rate);
			rate.setText(String.format(activityContext.getString(R.string.apptentive_rating_rate), GlobalInfo.appDisplayName));
			Button later = (Button) findViewById(R.id.apptentive_rating_later);
			Button no = (Button) findViewById(R.id.apptentive_rating_no);

			rate.setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View view) {
							dismiss();
							String errorMessage = activityContext.getString(R.string.apptentive_rating_error);
							try {
								// Instrumentation
								MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__rate);
								PayloadManager.getInstance().putPayload(metric);
								// Send user to app rating page
								if (RatingModule.this.selectedRatingProvider == null) {
									// Default to the Android Market provider, if none has been specified
									RatingModule.this.selectedRatingProvider = new AndroidMarketRatingProvider();
								}
								errorMessage = RatingModule.this.selectedRatingProvider.activityNotFoundMessage(activityContext);
								RatingModule.this.selectedRatingProvider.startRating(activityContext, RatingModule.this.ratingProviderArgs);
								setState(RatingState.RATED);
							} catch (ActivityNotFoundException e) {
								displayError(errorMessage);
							} catch (InsufficientRatingArgumentsException e) {
								// TODO: Log a message to apptentive to let the developer know that their custom rating provider puked?
								displayError(activityContext.getString(R.string.apptentive_rating_error));
							} finally {
								dismiss();
							}
						}

						private void displayError(String message) {
							final AlertDialog alertDialog = new AlertDialog.Builder(activityContext).create();
							alertDialog.setTitle(activityContext.getString(R.string.apptentive_oops));
							alertDialog.setMessage(message);
							alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activityContext.getString(R.string.apptentive_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialogInterface, int i) {
									alertDialog.dismiss();
								}
							});
							alertDialog.show();
						}
					}
			);

			later.setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View view) {
							dismiss();
							// Instrumentation
							MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__remind);
							PayloadManager.getInstance().putPayload(metric);

							setState(RatingState.REMIND);
							setStartOfRatingPeriod(new Date());
						}
					}
			);

			no.setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View view) {
							dismiss();
							// Instrumentation
							MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__decline);
							PayloadManager.getInstance().putPayload(metric);

							setState(RatingState.POSTPONE);
						}
					}
			);

			// Instrumentation
			MetricPayload metric = new MetricPayload(MetricPayload.Event.rating_dialog__launch);
			PayloadManager.getInstance().putPayload(metric);

			setCancelable(false);
			super.show();
		}
	}
}
