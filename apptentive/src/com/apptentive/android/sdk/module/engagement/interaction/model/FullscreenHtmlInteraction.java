/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class FullscreenHtmlInteraction extends Interaction {

	private static String KEY_TITLE = "title";
	private static String KEY_BODY = "body";
	private static String KEY_ACTION = "action";

	public static final String EVENT_NAME_LAUNCH = "launch";
	public static final String EVENT_NAME_CANCEL = "cancel";
	public static final String EVENT_NAME_DISMISS = "dismiss";

	public FullscreenHtmlInteraction(String json) throws JSONException {
		super(json);
	}

	public String getTitle() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && !configuration.isNull(KEY_TITLE)) {
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
			if (configuration != null && !configuration.isNull(KEY_BODY)) {
				return configuration.getString(KEY_BODY);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public Action getAction() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && !configuration.isNull(KEY_ACTION)) {
				return Action.Factory.parseAction(configuration.getString(KEY_ACTION));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}
}
