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
public class EnjoymentDialogInteraction extends Interaction {

	private static final String KEY_BODY = "body";
	private static final String KEY_YES_TEXT = "yes_text";
	private static final String KEY_NO_TEXT = "no_text";

	public EnjoymentDialogInteraction(String json) throws JSONException {
		super(json);
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

	public String getYesText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_YES_TEXT)) {
				return configuration.getString(KEY_YES_TEXT);
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
