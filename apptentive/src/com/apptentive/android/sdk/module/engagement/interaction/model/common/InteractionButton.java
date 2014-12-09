/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.common;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionButton<T extends InteractionButton> extends JSONObject {
	private static final String KEY_ACTION = "action";
	private static final String KEY_LABEL = "label";

	public InteractionButton(String json) throws JSONException {
		super(json);
	}

	public Action getAction() {
		return Action.parse(optString(KEY_ACTION, "unknown"));
	}

	public String getLabel() {
		return optString(KEY_LABEL, null);
	}

	public static enum Action {
		Dismiss,
		Interaction,
		unknown;

		public static Action parse(String actionName) {
			try {
				return Action.valueOf(actionName);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown InteractionButton.Action: " + actionName);
			}
			return unknown;
		}
	}

	public static class Factory {
		public static InteractionButton parseInteractionButton(String interactionButtonString) {
			try {
				InteractionButton.Action action = Action.unknown;
				JSONObject interactionButton = new JSONObject(interactionButtonString);
				if (interactionButton.has(KEY_ACTION)) {
					action = Action.parse(interactionButton.getString(KEY_ACTION));
				}
				switch (action) {
					case Dismiss:
						return new DismissInteractionButton(interactionButtonString);
					case Interaction:
						return new LaunchInteractionInteractionButton(interactionButtonString);
					case unknown:
						break;
				}
			} catch (JSONException e) {
				// Ignore
			}
			return null;
		}
	}

}
