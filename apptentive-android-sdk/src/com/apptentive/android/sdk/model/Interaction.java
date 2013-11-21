package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class Interaction extends JSONObject {

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
		return null;
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
		return null;
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
		EnjoymentDialog,
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

}
