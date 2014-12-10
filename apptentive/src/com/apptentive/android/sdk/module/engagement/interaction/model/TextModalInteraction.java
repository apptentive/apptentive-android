/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.InteractionActions;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class TextModalInteraction extends Interaction {

	private static String KEY_TEMPLATE = "template";
	private static String KEY_TITLE = "title";
	private static String KEY_BODY = "body";
	private static String KEY_ACTIONS = "actions";

	public TextModalInteraction(String json) throws JSONException {
		super(json);
	}

	public static enum Template {
		ModalDialog,
		Toast,
		Fullscreen,
		unknown;

		public static Template parse(String templateName) {
			try {
				return Template.valueOf(templateName);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown TextModalInteraction.Template: " + templateName);
			}
			return unknown;
		}
	}

	public Template getTemplate() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_TEMPLATE)) {
				return Template.parse(configuration.getString(KEY_TEMPLATE));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
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

	public InteractionActions getInteractionButtons() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_ACTIONS)) {
				return new InteractionActions(configuration.getString(KEY_ACTIONS));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}


}
