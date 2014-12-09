/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.InteractionButtons;
import org.json.JSONException;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class AnnouncementInteraction extends Interaction {

	private static String KEY_TEMPLATE = "template";
	private static String KEY_TITLE = "title";
	private static String KEY_BODY = "body";
	private static String KEY_BUTTONS = "buttons";

	public AnnouncementInteraction(String json) throws JSONException {
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
				Log.v("Error parsing unknown AnnouncementInteraction.Template: " + templateName);
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

	public InteractionButtons getInteractionButtons() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_BUTTONS)) {
				return new InteractionButtons(configuration.getString(KEY_BUTTONS));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}


}
