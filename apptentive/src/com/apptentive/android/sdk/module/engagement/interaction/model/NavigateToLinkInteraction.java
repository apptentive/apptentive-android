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
}
