/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Actions;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class TextModalInteraction extends Interaction {

	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_ACTIONS = "actions";

	public static final String EVENT_NAME_CANCEL = "cancel";
	public static final String EVENT_NAME_DISMISS = "dismiss";
	public static final String EVENT_NAME_INTERACTION = "interaction";

	public static final String EVENT_KEY_ACTION_ID = "action_id";
	public static final String EVENT_KEY_ACTION_POSITION = "position";
	public static final String EVENT_KEY_INVOKED_INTERACTION_ID = "invoked_interaction_id";


	public TextModalInteraction(String json) throws JSONException {
		super(json);
	}

	public String getTitle() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_TITLE)) {
				return configuration.getString(KEY_TITLE);
			}
		} catch (JSONException e) {
			// Ignore
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
			// Ignore
		}
		return null;
	}

	public Actions getActions() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_ACTIONS)) {
				return new Actions(configuration.getString(KEY_ACTIONS));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}
}
