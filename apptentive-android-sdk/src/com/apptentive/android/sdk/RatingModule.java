/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.model.Event;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;
import com.apptentive.android.sdk.module.rating.impl.GooglePlayRatingProvider;
import com.apptentive.android.sdk.module.rating.view.EnjoymentDialog;
import com.apptentive.android.sdk.module.rating.view.RatingDialog;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This module is responsible for determining when to show, and showing the rating flow of dialogs.
 *
 * @author Sky Kelsey
 */
public class RatingModule {

	// *************************************************************************************************
	// ********************************************* Static ********************************************
	// *************************************************************************************************

	private static boolean ratingFlowVisible;

	private static RatingModule instance;

	static RatingModule getInstance() {
		if (instance == null) {
			instance = new RatingModule();
		}
		return instance;
	}


	// *************************************************************************************************
	// ********************************************* Private *******************************************
	// *************************************************************************************************

	private IRatingProvider selectedRatingProvider = null;

	private Map<String, String> ratingProviderArgs;

	private RatingModule() {
		ratingProviderArgs = new HashMap<String, String>();
		ratingProviderArgs.put("package", GlobalInfo.appPackage);
	}

	private static long getStartOfRatingPeriod(SharedPreferences prefs) {
		if (!prefs.contains(Constants.PREF_KEY_START_OF_RATING_PERIOD)) {
			setStartOfRatingPeriod(prefs, new Date().getTime());
		}
		return prefs.getLong(Constants.PREF_KEY_START_OF_RATING_PERIOD, new Date().getTime());
	}

	private static void setStartOfRatingPeriod(SharedPreferences prefs, long startOfRatingPeriod) {
		prefs.edit().putLong(Constants.PREF_KEY_START_OF_RATING_PERIOD, startOfRatingPeriod).commit();
	}

	private static RatingState getState(SharedPreferences prefs) {
		return RatingState.valueOf(prefs.getString(Constants.PREF_KEY_RATING_STATE, RatingState.START.toString()));
	}

	private static void setState(SharedPreferences prefs, RatingState state) {
		prefs.edit().putString(Constants.PREF_KEY_RATING_STATE, state.name()).commit();
	}

	private static int getEvents(SharedPreferences prefs) {
		return prefs.getInt(Constants.PREF_KEY_RATING_EVENTS, 0);
	}

	private static void setEvents(SharedPreferences prefs, int events) {
		prefs.edit().putInt(Constants.PREF_KEY_RATING_EVENTS, events).commit();
	}

	private static int getUses(SharedPreferences prefs) {
		return prefs.getInt(Constants.PREF_KEY_RATING_USES, 0);
	}

	private static void setUses(SharedPreferences prefs, int uses) {
		prefs.edit().putInt(Constants.PREF_KEY_RATING_USES, uses).commit();
	}

	// *************************************************************************************************
	// ******************************************* Not Private *****************************************
	// *************************************************************************************************

	void onAppVersionChanged(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		Configuration config = Configuration.load(prefs);
		if (config.isRatingsClearOnUpgrade()) {
			setState(prefs, RatingState.START);
			setStartOfRatingPeriod(prefs, new Date().getTime());
			setEvents(prefs, 0);
			setUses(prefs, 1);
		}
	}

	/**
	 * Internal use.
	 */
	void setRatingProvider(IRatingProvider ratingProvider) {
		this.selectedRatingProvider = ratingProvider;
	}

	/**
	 * If there are any pieces of data your {@link IRatingProvider} needs, add them here.
	 * Default keys already included are: [name, package].
	 *
	 * @param key   The argument name the the chosen {@link IRatingProvider} needs.
	 * @param value The value of the argument.
	 */
	void putRatingProviderArg(String key, String value) {
		this.ratingProviderArgs.put(key, value);
	}

	/**
	 * Internal use only.
	 * Shows the initial "Are you enjoying this app?" dialog that starts the rating flow.
	 * It will be called if you call RatingModule.run() and any of the usage conditions have been met.
	 *
	 * @param activity The activityContext from which this method was called.
	 */
	void forceShowEnjoymentDialog(Activity activity) {
		showEnjoymentDialog(activity, Trigger.forced);
	}

	void showEnjoymentDialog(final Activity activity, Trigger reason) {
		ratingFlowVisible = true;
		final SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		final EnjoymentDialog dialog = new EnjoymentDialog(activity);
		String appDisplayName = Configuration.load(activity).getAppDisplayName();
		String title = String.format(activity.getString(R.string.apptentive_do_you_love_this_app), appDisplayName);
		dialog.setTitle(title);

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				setState(prefs, RatingState.POSTPONE);
				MetricModule.sendMetric(activity, Event.EventLabel.enjoyment_dialog__cancel);
				ratingFlowVisible = false;
			}
		});

		dialog.setOnChoiceMadeListener(new EnjoymentDialog.OnChoiceMadeListener() {
			@Override
			public void onNo() {
				setState(prefs, RatingState.POSTPONE);
				MetricModule.sendMetric(activity, Event.EventLabel.enjoyment_dialog__no);
				dialog.dismiss();
				Apptentive.showMessageCenter(activity, false, null);
			}

			@Override
			public void onYes() {
				MetricModule.sendMetric(activity, Event.EventLabel.enjoyment_dialog__yes);
				dialog.dismiss();
				forceShowRatingDialog(activity);
			}
		});
		MetricModule.sendMetric(activity, Event.EventLabel.enjoyment_dialog__launch, reason.name());
		dialog.show();
	}

	/**
	 * Internal use only.
	 * Shows the "Would you please rate this app?" dialog that is the second dialog in the rating flow.
	 * It will be called automatically if the user chooses "Yes" in the "Are you enjoying this app?" dialog.
	 *
	 * @param activity The activity from which this method was called.
	 */
	void forceShowRatingDialog(final Activity activity) {
		ratingFlowVisible = true;
		final SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		final RatingDialog dialog = new RatingDialog(activity);

		String appDisplayName = Configuration.load(activity).getAppDisplayName();
		dialog.setBody(activity.getString(R.string.apptentive_rating_message_fs, appDisplayName));
		dialog.setRateButtonText(activity.getString(R.string.apptentive_rate_this_app, appDisplayName));

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				ratingFlowVisible = false;
				MetricModule.sendMetric(activity, Event.EventLabel.rating_dialog__cancel);
				setState(prefs, RatingState.POSTPONE);
			}
		});

		// Called no matter how this dialog is exited.
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				ratingFlowVisible = false;
			}
		});

		dialog.setOnChoiceMadeListener(new RatingDialog.OnChoiceMadeListener() {
			@Override
			public void onRate() {
				dialog.dismiss();
				String errorMessage = activity.getString(R.string.apptentive_rating_error);
				try {
					MetricModule.sendMetric(activity, Event.EventLabel.rating_dialog__rate);
					// Send user to app rating page
					if (RatingModule.this.selectedRatingProvider == null) {
						// Default to the Android Market provider, if none has been specified
						RatingModule.this.selectedRatingProvider = new GooglePlayRatingProvider();
					}
					errorMessage = RatingModule.this.selectedRatingProvider.activityNotFoundMessage(activity);

					String appDisplayName = Configuration.load(activity).getAppDisplayName();
					Map<String, String> finalRatingProviderArgs = new HashMap<String, String>(RatingModule.this.ratingProviderArgs);
					finalRatingProviderArgs.put("name", appDisplayName);

					RatingModule.this.selectedRatingProvider.startRating(activity, finalRatingProviderArgs);
					setState(prefs, RatingState.RATED);
				} catch (ActivityNotFoundException e) {
					displayError(errorMessage);
				} catch (InsufficientRatingArgumentsException e) {
					// TODO: Log a message to apptentive to let the developer know that their custom rating provider puked?
					displayError(activity.getString(R.string.apptentive_rating_error));
				} finally {
					dialog.dismiss();
				}
			}

			private void displayError(String message) {
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

			@Override
			public void onRemind() {
				dialog.dismiss();
				MetricModule.sendMetric(activity, Event.EventLabel.rating_dialog__remind);
				setState(prefs, RatingState.REMIND);
				setStartOfRatingPeriod(prefs, new Date().getTime());
			}

			@Override
			public void onNo() {
				dialog.dismiss();
				MetricModule.sendMetric(activity, Event.EventLabel.rating_dialog__decline);
				setState(prefs, RatingState.POSTPONE);
			}
		});
		MetricModule.sendMetric(activity, Event.EventLabel.rating_dialog__launch);
		dialog.show();
	}

	/**
	 * Start the rating flow dialogs if any of the usage conditions have been met. Call this method if it is
	 * appropriate to show a popup dialog. Generally, you would want to call this method in your Activity's
	 * <strong>onWindowFocusChanged(boolean hasFocus)</strong> method if hasFocus is true.
	 *
	 * @param activity The activityContext from which this method was called.
	 */
	boolean run(Activity activity) {
		// If the dialog is already being shown, return true so we don't make the host app think otherwise.
		if (ratingFlowVisible) {
			Log.e("Duplicate call to Apptentive.showRatingFlowIfConditionsAreMet().");
			return true;
		}
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		Configuration config = Configuration.load(prefs);
		if (!config.isRatingsEnabled()) {
			Log.d("Skipped showing ratings because they are disabled.");
			return false;
		}
		if (!Util.isNetworkConnectionPresent(activity)) {
			Log.d("Ratings can't be shown because the network is not available. Try again later.");
			return false;
		}
		Logic logic = new Logic();
		logic.load(prefs);

		RatingState state = getState(prefs);
		boolean canShow = logic.evaluate();
		if (canShow) {
			switch (state) {
				case START:
					// TODO: Trigger no longer makes sense with boolean logic expressions. Axe it.
					showEnjoymentDialog(activity, Trigger.events);
					return true;
				case REMIND:
					forceShowRatingDialog(activity);
					return true;
				case POSTPONE:
					break;
				case RATED:
					break;
				default:
					break;
			}
		}
		return false;
	}

	/**
	 * Resets the Rating Module metrics such as significant events, start of rating period, and number of uses.
	 * If you would like each new version of your app to be rated, you can call this method upon app upgrade.
	 */
	void reset(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		if (RatingState.RATED != getState(prefs)) {
			setState(prefs, RatingState.START);
			setStartOfRatingPeriod(prefs, new Date().getTime());
			setEvents(prefs, 0);
			setUses(prefs, 0);
		}
	}

	void logSignificantEvent(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		setEvents(prefs, getEvents(prefs) + 1);
	}

	/**
	 * Increments the number of times this app has been used. This method should be called each time your app
	 * regains focus. The number of uses is used be the Rating Module to determine if it is time to run the rating flow.
	 * <p/>A Use is defined as each time the App comes back to the foreground. It will happen with each launch, and each
	 * time the app is switched back to, after another app has been brought to the foreground.
	 * <p>Internal use only. We handle calls to this method.</p>
	 */
	void logUse(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		setUses(prefs, getUses(prefs) + 1);
	}

	/**
	 * This method is for debugging purposes only. It will move the rating start date one day into the past.
	 */
	void logDay(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		setStartOfRatingPeriod(prefs, getStartOfRatingPeriod(prefs) - DateUtils.DAY_IN_MILLIS);
	}

	// *************************************************************************************************
	// ***************************************** Inner Classes *****************************************
	// *************************************************************************************************

	public static class Logic {
		private RatingState state;
		private String logic;
		private int daysBeforePrompt;
		private long startOfRatingPeriod;
		private int usesBeforePrompt;
		private int usesElapsed;
		private int significantEventsBeforePrompt;
		private int significantEventsElapsed;
		private int daysBetweenPrompts;

		private Long currentTime; // For Testing only.

		public void load(SharedPreferences prefs) {
			Configuration config = Configuration.load(prefs);
			state = getState(prefs);
			logic = config.getRatingsPromptLogic();
			daysBeforePrompt = config.getRatingsDaysBeforePrompt();
			startOfRatingPeriod = getStartOfRatingPeriod(prefs);
			usesBeforePrompt = config.getRatingsUsesBeforePrompt();
			usesElapsed = getUses(prefs);
			significantEventsBeforePrompt = config.getRatingsEventsBeforePrompt();
			significantEventsElapsed = getEvents(prefs);
			daysBetweenPrompts = config.getRatingsDaysBetweenPrompts();
		}

		public boolean evaluate() {
			switch (state) {
				case START:
					try {
						return logic(new JSONObject(logic));
					} catch (JSONException e) {
						// Fallback logic.
						Boolean ratingPeriodElapsed = ratingPeriodElapsed();
						if (ratingPeriodElapsed == null) {
							ratingPeriodElapsed = true;
						}
						Boolean usesThresholdReached = usesThresholdReached();
						if (usesThresholdReached == null) {
							usesThresholdReached = false;
						}
						Boolean eventThresholdReached = eventThresholdReached();
						if (eventThresholdReached == null) {
							eventThresholdReached = false;
						}
						return ratingPeriodElapsed && (eventThresholdReached || usesThresholdReached);
					}
				case REMIND:
					Boolean ratingPeriodElapsed = ratingPeriodElapsed();
					if(ratingPeriodElapsed == null) {
						ratingPeriodElapsed = false;
					}
					return ratingPeriodElapsed;
				case POSTPONE:
					break;
				case RATED:
					break;
				default:
					break;
			}
			return false;
		}

		private Boolean logic(Object obj) throws JSONException {
			boolean ret = false;
			if (obj instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject) obj;
				String key = (String) jsonObject.keys().next(); // Should be one array per logic statement.
				if ("and".equals(key)) {
					JSONArray and = jsonObject.getJSONArray("and");
					ret = true;
					for (int i = 0; i < and.length(); i++) {
						Boolean prev = logic(and.get(i));
						if (prev == null) {
							prev = true;
						}
						ret = ret && prev;
					}
				} else if ("or".equals(key)) {
					JSONArray or = jsonObject.getJSONArray("or");
					for (int i = 0; i < or.length(); i++) {
						Boolean arg = logic(or.get(i));
						if (arg == null) {
							arg = false;
						}
						ret = ret || arg;
					}
				} else {
					return logic(key);
				}
			} else if (obj instanceof String) {
				if ("uses".equals(obj)) {
					return usesThresholdReached();
				} else if ("days".equals(obj)) {
					return ratingPeriodElapsed();
				} else if ("events".equals(obj)) {
					return eventThresholdReached();
				}
			} else {
				Log.w("Unknown logic token: " + obj);
			}
			return ret;
		}

		/**
		 * @return True if elapsed, false if not, null if disabled by setting daysBeforePrompt to 0.
		 */
		private Boolean ratingPeriodElapsed() {
			long days;
			switch (state) {
				case REMIND:
					days = daysBetweenPrompts;
					break;
				default:
					days = daysBeforePrompt;
					break;
			}
			if (days == 0) {
				return null;
			}
			long now = getCurrentTime();
			long periodEnd = startOfRatingPeriod + (DateUtils.DAY_IN_MILLIS * days);
			return now > periodEnd;
		}

		/**
		 * @return True if elapsed, false if not, null if disabled by setting usesBeforePrompt to 0.
		 */
		private Boolean usesThresholdReached() {
			if (usesBeforePrompt == 0) {
				return null;
			} else {
				return usesElapsed >= usesBeforePrompt;
			}
		}

		/**
		 * @return True if elapsed, false if not, null if disabled by setting significantEventsBeforePrompt to 0.
		 */
		private Boolean eventThresholdReached() {
			if (significantEventsBeforePrompt == 0) {
				return null;
			} else {
				return significantEventsElapsed >= significantEventsBeforePrompt;
			}
		}

		public void setRatingState(RatingState state) {
			this.state = state;
		}

		public void setLogicString(String logic) {
			this.logic = logic;
		}

		public void setDaysBeforePrompt(int daysBeforePrompt) {
			this.daysBeforePrompt = daysBeforePrompt;
		}

		public void setStartOfRatingPeriod(long startOfRatingPeriod) {
			this.startOfRatingPeriod = startOfRatingPeriod;
		}

		public void setUsesBeforePrompt(int usesBeforePrompt) {
			this.usesBeforePrompt = usesBeforePrompt;
		}

		public void setUsesElapsed(int usesElapsed) {
			this.usesElapsed = usesElapsed;
		}

		public void setSignificantEventsBeforePrompt(int significantEventsBeforePrompt) {
			this.significantEventsBeforePrompt = significantEventsBeforePrompt;
		}

		public void setSignificantEventsElapsed(int significantEventsElapsed) {
			this.significantEventsElapsed = significantEventsElapsed;
		}

		public void setDaysBetweenPrompts(int daysBetweenPrompts) {
			this.daysBetweenPrompts = daysBetweenPrompts;
		}

		public long getCurrentTime() {
			if (currentTime != null) {
				return currentTime;
			}
			return System.currentTimeMillis();
		}

		public void setCurrentTime(long currentTime) {
			this.currentTime = currentTime;
		}

		@SuppressWarnings({"unused", "EmptyCatchBlock"})
		public void logRatingFlowState() {
			try {
				Log.e(String.format("Ratings Prompt\nLogic: %s\nState: %s, Days met: %b, Uses: %d/%d, Events: %d/%d, Returned: %b", logic, state.name(), ratingPeriodElapsed(), usesElapsed, usesBeforePrompt, significantEventsElapsed, significantEventsBeforePrompt, logic(new JSONObject(logic))));
			} catch (JSONException e) {
			}
		}

	}

	public enum RatingState {
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
}
