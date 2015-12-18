/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.app.Activity;
import org.json.JSONException;

import java.util.Locale;

/**
 * @author Sky Kelsey
 */
public class NavigateToLinkInteraction extends Interaction {

	public static final String KEY_URL = "url";
	public static final String KEY_TARGET = "target";

	public static final String EVENT_KEY_SUCCESS = "success";

	public static final String EVENT_NAME_NAVIGATE = "navigate";

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

	public Target getTarget() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration != null && !configuration.isNull(KEY_TARGET)) {
			return Target.parse(configuration.optString(KEY_TARGET, null));
		}
		return Target.New;
	}

	public enum Target {
		New, // Default value
		Self;

		public String lowercaseName() {
			return name().toLowerCase(Locale.US);
		}

		public static Target parse(String value) {
			if (value != null) {
				try {
					for (Target target : Target.values()) {
						// "new" is a reserved keyword, so perform a case-insensitive match.
						if (target.name().equalsIgnoreCase(value)) {
							return target;
						}
					}
				} catch (Exception e) {
					// Happens for values introduced after this version of the SDK.
				}
			}
			return New;
		}
	}

	@Override
	public void sendLaunchEvent(Activity activity) {
		// This Interaction type does not send a launch Event.
	}
}
