/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public abstract class Interaction extends JSONObject {

	public static final String KEY_NAME = "interaction";

	private static final String KEY_ID = "id";
	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION = "version";
	private static final String KEY_PRIORITY = "priority";
	private static final String KEY_ACTIVE = "active";
	private static final String KEY_CONFIGURATION = "configuration";
	private static final String KEY_CRITERIA = "criteria";

	public Interaction(String json) throws JSONException {
		super(json);
	}

	/**
	 * Override this method if the subclass has certain restrictions other than Criteria that it needs to evaluate in
	 * order to know if it can run. Example: Interactions that will require an internet connection must override and check
	 * for network availability.
	 *
	 * @param context The Context from which this method is called.
	 * @return true if this interaction can run.
	 */
	protected boolean isInRunnableState(Context context) {
		return true;
	}

	public boolean canRun(Context context) {
		InteractionCriteria criteria = getCriteria();
		return isInRunnableState(context) && criteria != null && criteria.isMet(context);
	}

	public String getId() {
		try {
			if (!isNull(KEY_ID)) {
				return getString(KEY_ID);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Type getType() {
		try {
			if (!isNull(KEY_TYPE)) {
				return Type.parse(getString(KEY_TYPE));
			}
		} catch (JSONException e) {
		}
		return Type.unknown;
	}

	public Integer getVersion() {
		try {
			if (!isNull(KEY_VERSION)) {
				return getInt(KEY_VERSION);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public Integer getPriority() {
		try {
			if (!isNull(KEY_PRIORITY)) {
				return getInt(KEY_PRIORITY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public boolean getActive() {
		try {
			if (!isNull(KEY_ACTIVE)) {
				return getBoolean(KEY_ACTIVE);
			}
		} catch (JSONException e) {
		}
		return false;
	}

	public InteractionConfiguration getConfiguration() {
		try {
			if (!isNull(KEY_CONFIGURATION)) {
				return new InteractionConfiguration(getJSONObject(KEY_CONFIGURATION).toString());
			}
		} catch (JSONException e) {
		}
		return new InteractionConfiguration();
	}

	public InteractionCriteria getCriteria() {
		try {
			if (!isNull(KEY_CRITERIA)) {
				return new InteractionCriteria(getJSONObject(KEY_CRITERIA).toString());
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public static enum Type {
		UpgradeMessage,
		RatingDialog,
		unknown;

		public static Type parse(String type) {
			try {
				return Type.valueOf(type);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown Interaction.Type: " + type);
			}
			return unknown;
		}
	}

	public static class Factory {
		public static Interaction parseInteraction(String interactionString) {
			try {
				Interaction.Type type = Type.unknown;
				JSONObject interaction = new JSONObject(interactionString);
				if (interaction.has(KEY_TYPE)) {
					type = Type.parse(interaction.getString(KEY_TYPE));
				}
				switch (type) {
					case UpgradeMessage:
						return new UpgradeMessageInteraction(interactionString);
					case RatingDialog:
						return new RatingDialogInteraction(interactionString);
					case unknown:
						break;
				}
			} catch (JSONException e) {
			}
			return null;
		}
	}
}
