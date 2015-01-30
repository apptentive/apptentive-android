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
	private static final String KEY_TARGET = "target";

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
			return Target.parse(configuration.optString(KEY_TARGET, ""));
		}
		return Target.New;
	}

	public static enum Target {
		New, // Default value
		Self;

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
}
