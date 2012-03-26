package com.apptentive.android.sdk.module.survey;

/**
 * This interface is provided so you can get a callback when a survey has been fetched.
 *
 * @author Sky Kelsey.
 */
public interface OnSurveyFetchedListener {

	/**
	 * Callback called when a survey has been fetched.
	 *
	 * @param success True only if the survey was fetched successfully.
	 */
	public void onSurveyFetched(boolean success);
}
