/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteraction extends Interaction {

	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_RATE_TEXT = "rate_text";
	private static final String KEY_REMIND_TEXT = "remind_text";
	private static final String KEY_NO_TEXT = "no_text";

	public RatingDialogInteraction(String json) throws JSONException {
		super(json);
	}

	public String getTitle() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_TITLE)) {
				return configuration.getString(KEY_TITLE);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getBody() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_BODY)) {
				return configuration.getString(KEY_BODY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getRateText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_RATE_TEXT)) {
				return configuration.getString(KEY_RATE_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getRemindText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_REMIND_TEXT)) {
				return configuration.getString(KEY_REMIND_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getNoText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_NO_TEXT)) {
				return configuration.getString(KEY_NO_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}


}
