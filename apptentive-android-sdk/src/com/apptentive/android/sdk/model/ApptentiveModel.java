/*
 * ApptentiveModel.java
 * 
 * Created by Sky Kelsey on 2011-05-23.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.model;

import android.content.SharedPreferences;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.survey.SurveyDefinition;
import com.apptentive.android.sdk.util.Util;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class ApptentiveModel extends Observable {

	private static ApptentiveModel instance = null;

	// Default configuration variables
	private static int RATING_DEFAULT_DAYS_BEFORE_PROMPT = 30;
	private static int RATING_DEFAULT_USES_BEFORE_PROMPT = 5;
	private static int RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = 10;
	private static int RATING_DEFAULT_DAYS_BEFORE_REPROMPTING = 5;

	private ALog log = new ALog(ApptentiveModel.class);

	// Configuration variables
	private SharedPreferences prefs;
	private String appDisplayName;
	private String appPackage;
	private String apiKey;
	private boolean askForExtraInfo;

	private int ratingDaysBeforePrompt;
	private int ratingUsesBeforePrompt;
	private int ratingSignificantEventsBeforePrompt;
	private int ratingDaysBeforeReprompt;

	// Data variables
	private String name;
	private String feedback;
	private String email;
	private String phone;

	private String model;
	private String version;
	private String carrier;
	private String uuid;
	private String feedbackType;

	// State variables
	private ApptentiveState state;

	private int events;
	private int uses;
	private int daysUntilRate;
	private Date startOfRatingPeriod;

	// Survey module
	SurveyDefinition survey;



	private ApptentiveModel() {
		askForExtraInfo = false;
		clearTransientData();
	}

	public static void setDefaults(Integer ratingFlowDaysBeforePrompt, Integer ratingFlowDaysBeforeReprompt, Integer ratingFlowSignificantEventsBeforePrompt, Integer ratingFlowUsesBeforePrompt){
		if(ratingFlowDaysBeforePrompt != null){
			ApptentiveModel.RATING_DEFAULT_DAYS_BEFORE_PROMPT = ratingFlowDaysBeforePrompt;
		}
		if(ratingFlowDaysBeforeReprompt != null){
			ApptentiveModel.RATING_DEFAULT_DAYS_BEFORE_REPROMPTING = ratingFlowDaysBeforeReprompt;
		}
		if(ratingFlowSignificantEventsBeforePrompt != null){
			ApptentiveModel.RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT = ratingFlowSignificantEventsBeforePrompt;
		}
		if(ratingFlowUsesBeforePrompt != null){
			ApptentiveModel.RATING_DEFAULT_USES_BEFORE_PROMPT = ratingFlowUsesBeforePrompt;
		}

	}

	public static ApptentiveModel getInstance(){
		if(instance == null){
			instance = new ApptentiveModel();
		}
		return instance;
	}

	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
		retrieve();
	}

	public String getAppDisplayName() {
		return appDisplayName;
	}

	public void setAppDisplayName(String appDisplayName) {
		this.appDisplayName = appDisplayName;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
		this.appPackage = "org.mozilla.firefox";
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isAskForExtraInfo() {
		return askForExtraInfo;
	}

	public void setAskForExtraInfo(boolean askForExtraInfo) {
		this.askForExtraInfo = askForExtraInfo;
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

	public void useRatingDaysBeforeReprompt(){
		this.daysUntilRate = ratingDaysBeforeReprompt;
		save();
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		save();
		setChanged();
		notifyObservers(instance);
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
		setChanged();
		notifyObservers(instance);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		save();
		setChanged();
		notifyObservers(instance);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
		setChanged();
		notifyObservers(instance);
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

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFeedbackType() {
		return feedbackType;
	}

	public void setFeedbackType(String feedbackType) {
		this.feedbackType = feedbackType;
	}

	public SurveyDefinition getSurvey() {
		return survey;
	}

	public void setSurvey(SurveyDefinition survey) {
		this.survey = survey;
	}



	public void forceNotifyObservers(){
		setChanged();
		notifyObservers(instance);
	}

	private void save(){
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("ratingDaysBeforePrompt", ratingDaysBeforePrompt);
		editor.putInt("ratingUsesBeforePrompt", ratingUsesBeforePrompt);
		editor.putInt("ratingSignificantEventsBeforePrompt", ratingSignificantEventsBeforePrompt);
		editor.putInt("ratingDaysBeforeReprompt", ratingDaysBeforeReprompt);

		editor.putString("name", name);
		editor.putString("email", email);
		editor.putString("state", state.name());
		editor.putInt("uses", uses);
		editor.putInt("events", events);
		editor.putInt("daysUntilRate", daysUntilRate);
		editor.putString("startOfRatingPeriod", Util.dateToString(startOfRatingPeriod));
		editor.commit();
	}

	private void retrieve(){
		ratingDaysBeforePrompt              = prefs.getInt("ratingDaysBeforePrompt",              RATING_DEFAULT_DAYS_BEFORE_PROMPT);
		ratingUsesBeforePrompt              = prefs.getInt("ratingUsesBeforePrompt",              RATING_DEFAULT_USES_BEFORE_PROMPT);
		ratingSignificantEventsBeforePrompt = prefs.getInt("ratingSignificantEventsBeforePrompt", RATING_DEFAULT_SIGNIFICANT_EVENTS_BEFORE_PROMPT);
		ratingDaysBeforeReprompt            = prefs.getInt("ratingDaysBeforeReprompt",            RATING_DEFAULT_DAYS_BEFORE_REPROMPTING);

		name  = prefs.getString("name", "");
		email = prefs.getString("email", "");
		state = ApptentiveState.valueOf(prefs.getString("state", "START"));
		uses = prefs.getInt("uses", 0);
		events = prefs.getInt("events", 0);
		daysUntilRate = prefs.getInt("daysUntilRate", ratingDaysBeforePrompt);
		try{
			startOfRatingPeriod = Util.stringToDate(prefs.getString("startOfRatingPeriod", ""));
		}catch(ParseException e){
			startOfRatingPeriod = new Date();
		}
	}

	public void clearTransientData(){
		feedback = "";
		phone = "";
		model = "";
		version = "";
		carrier = "";
		uuid = "";
		feedbackType = "";
	}

	@Override
	public String toString() {
		return "ApptentiveModel{prefs=" + prefs + ", appDisplayName='" + appDisplayName + '\'' + ", appPackage='" + appPackage + '\'' + ", apiKey='" + apiKey + '\'' + ", askForExtraInfo=" + askForExtraInfo + ", ratingDaysBeforePrompt=" + ratingDaysBeforePrompt + ", ratingUsesBeforePrompt=" + ratingUsesBeforePrompt + ", ratingSignificantEventsBeforePrompt=" + ratingSignificantEventsBeforePrompt + ", ratingDaysBeforeReprompt=" + ratingDaysBeforeReprompt + ", name='" + name + '\'' + ", feedback='" + feedback + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\'' + ", model='" + model + '\'' + ", version='" + version + '\'' + ", carrier='" + carrier + '\'' + ", uuid='" + uuid + '\'' + ", feedbackType='" + feedbackType + '\'' + ", state=" + state + ", events=" + events + ", uses=" + uses + ", daysUntilRate=" + daysUntilRate + ", startOfRatingPeriod=" + startOfRatingPeriod + '}';
	}
}
