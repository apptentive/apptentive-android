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
public abstract class Action<T extends Action> extends JSONObject {
	private static final String KEY_ACTION = "action";
	private static final String KEY_LABEL = "label";

	public Action(String json) throws JSONException {
		super(json);
	}

	public Type getType() {
		return Type.parse(optString(KEY_ACTION, Type.unknown.name()));
	}

	public String getLabel() {
		return optString(KEY_LABEL, null);
	}

	public static enum Type {
		Dismiss,
		Interaction,
		unknown;

		public static Type parse(String name) {
			try {
				return Type.valueOf(name);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown Action.Type: " + name);
			}
			return unknown;
		}
	}

	public static class Factory {
		public static com.apptentive.android.sdk.module.engagement.interaction.model.common.Action parseInteractionAction(String actionString) {
			try {
				Type action = Type.unknown;
				JSONObject actionObject = new JSONObject(actionString);
				if (actionObject.has(KEY_ACTION)) {
					action = Type.parse(actionObject.getString(KEY_ACTION));
				}
				switch (action) {
					case Dismiss:
						return new DismissAction(actionString);
					case Interaction:
						return new LaunchInteractionAction(actionString);
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
