/*
 * ApptentiveModel.java
 * 
 * Created by Sky Kelsey on 2011-05-23.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.model;

import android.content.SharedPreferences;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import com.apptentive.android.sdk.util.Util;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Observable;

public class ApptentiveModel extends Observable {

	private static ApptentiveModel instance = null;

	// Default configuration variables
	private static int RATING_DEFAULT_DAYS_BEFORE_PROMPT = 30;
	private static int RATING_DEFAULT_USES_BEFORE_PROMPT = 5;
	private static int RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = 10;
	private static int RATING_DEFAULT_DAYS_BEFORE_REPROMPTING = 5;

	// Configuration variables
	private SharedPreferences prefs;

	private int ratingDaysBeforePrompt;
	private int ratingUsesBeforePrompt;
	private int ratingSignificantEventsBeforePrompt;
	private int ratingDaysBeforeReprompt;

	// State variables
	private ApptentiveState state;
	private int events;
	private int uses;
	private int daysUntilRate;
	private Date startOfRatingPeriod;

	// Survey module
	SurveyDefinition survey;

	// Feedback module
	Map<String, String> customDataFields;

	// Metrics Module
	private boolean enableMetrics;


	private ApptentiveModel() {
	}

	public static void setDefaults(Integer ratingFlowDaysBeforePrompt, Integer ratingFlowDaysBeforeReprompt, Integer ratingFlowSignificantEventsBeforePrompt, Integer ratingFlowUsesBeforePrompt) {
		if (ratingFlowDaysBeforePrompt != null) {
			ApptentiveModel.RATING_DEFAULT_DAYS_BEFORE_PROMPT = ratingFlowDaysBeforePrompt;
		}
		if (ratingFlowDaysBeforeReprompt != null) {
			ApptentiveModel.RATING_DEFAULT_DAYS_BEFORE_REPROMPTING = ratingFlowDaysBeforeReprompt;
		}
		if (ratingFlowSignificantEventsBeforePrompt != null) {
			ApptentiveModel.RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = ratingFlowSignificantEventsBeforePrompt;
		}
		if (ratingFlowUsesBeforePrompt != null) {
			ApptentiveModel.RATING_DEFAULT_USES_BEFORE_PROMPT = ratingFlowUsesBeforePrompt;
		}

	}

	public static ApptentiveModel getInstance() {
		if (instance == null) {
			instance = new ApptentiveModel();
		}
		return instance;
	}

	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
		retrieve();
	}

	public int getRatingDaysBeforePrompt() {
		return ratingDaysBeforePrompt;
	}

	public void setRatingDaysBeforePrompt(int ratingDaysBeforePrompt) {
		this.ratingDaysBeforePrompt = ratingDaysBeforePrompt;
		save();
	}

	public int getRatingUsesBeforePrompt() {
		return ratingUsesBeforePrompt;
	}

	public void setRatingUsesBeforePrompt(int ratingUsesBeforePrompt) {
		this.ratingUsesBeforePrompt = ratingUsesBeforePrompt;
		save();
	}

	public int getRatingSignificantEventsBeforePrompt() {
		return ratingSignificantEventsBeforePrompt;
	}

	public void setRatingSignificantEventsBeforePrompt(int ratingSignificantEventsBeforePrompt) {
		this.ratingSignificantEventsBeforePrompt = ratingSignificantEventsBeforePrompt;
		save();
	}

	public int getRatingDaysBeforeReprompt() {
		return ratingDaysBeforeReprompt;
	}

	public void setRatingDaysBeforeReprompt(int ratingDaysBeforeReprompt) {
		this.ratingDaysBeforeReprompt = ratingDaysBeforeReprompt;
		save();
	}

	public void useRatingDaysBeforeReprompt() {
		this.daysUntilRate = ratingDaysBeforeReprompt;
		save();
	}

	public ApptentiveState getState() {
		return state;
	}

	public void setState(ApptentiveState state) {
		this.state = state;
		save();
	}

	public int getEvents() {
		return events;
	}

	public int incrementEvents() {
		++this.events;
		save();
		return this.events;
	}

	public void resetEvents() {
		this.events = 0;
		save();
	}

	public int getUses() {
		return uses;
	}

	public int incrementUses() {
		++this.uses;
		save();
		return this.uses;
	}

	public void resetUses() {
		this.uses = 0;
		save();
	}

	public int getDaysUntilRate() {
		return daysUntilRate;
	}

	public void setDaysUntilRate(int daysUntilRate) {
		this.daysUntilRate = daysUntilRate;
		save();
	}

	public Date getStartOfRatingPeriod() {
		return startOfRatingPeriod;
	}

	public void setStartOfRatingPeriod(Date startOfRatingPeriod) {
		this.startOfRatingPeriod = startOfRatingPeriod;
		save();
	}

	public SurveyDefinition getSurvey() {
		return survey;
	}

	public void setSurvey(SurveyDefinition survey) {
		this.survey = survey;
	}

	public Map<String, String> getCustomDataFields() {
		return customDataFields;
	}

	public void setCustomDataFields(Map<String, String> customDataFields) {
		this.customDataFields = customDataFields;
	}

	public void setEnableMetrics(boolean enableMetrics){
		this.enableMetrics = enableMetrics;
	}

	public boolean isEnableMetrics(){
		return enableMetrics;
	}


	private void save() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("ratingDaysBeforePrompt", ratingDaysBeforePrompt);
		editor.putInt("ratingUsesBeforePrompt", ratingUsesBeforePrompt);
		editor.putInt("ratingSignificantEventsBeforePrompt", ratingSignificantEventsBeforePrompt);
		editor.putInt("ratingDaysBeforeReprompt", ratingDaysBeforeReprompt);

		editor.putString("state", state.name());
		editor.putInt("uses", uses);
		editor.putInt("events", events);
		editor.putInt("daysUntilRate", daysUntilRate);
		editor.putString("startOfRatingPeriod", Util.dateToString(startOfRatingPeriod));
		editor.commit();
	}

	private void retrieve() {
		ratingDaysBeforePrompt = prefs.getInt("ratingDaysBeforePrompt", RATING_DEFAULT_DAYS_BEFORE_PROMPT);
		ratingUsesBeforePrompt = prefs.getInt("ratingUsesBeforePrompt", RATING_DEFAULT_USES_BEFORE_PROMPT);
		ratingSignificantEventsBeforePrompt = prefs.getInt("ratingSignificantEventsBeforePrompt", RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT);
		ratingDaysBeforeReprompt = prefs.getInt("ratingDaysBeforeReprompt", RATING_DEFAULT_DAYS_BEFORE_REPROMPTING);

		state = ApptentiveState.valueOf(prefs.getString("state", "START"));
		uses = prefs.getInt("uses", 0);
		events = prefs.getInt("events", 0);
		daysUntilRate = prefs.getInt("daysUntilRate", ratingDaysBeforePrompt);
		try {
			startOfRatingPeriod = Util.stringToDate(prefs.getString("startOfRatingPeriod", ""));
		} catch (ParseException e) {
			startOfRatingPeriod = new Date();
		}
	}
}
