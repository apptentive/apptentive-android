/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class NavigateToLinkInteraction extends Interaction {

	private static final String KEY_URL = "url";
	private static final String KEY_NEW_TASK = "new_task";

	public NavigateToLinkInteraction(String json) throws JSONException {
		super(json);
	}

	public String getUrl() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration != null && !configuration.isNull(KEY_URL)) {
			return configuration.optString(KEY_URL, null);
		}
		return null;
	}

	public boolean isNewTask() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration != null && !configuration.isNull(KEY_NEW_TASK)) {
			return configuration.optBoolean(KEY_NEW_TASK, true);
		}
		return true;
	}
}
