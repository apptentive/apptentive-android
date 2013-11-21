package com.apptentive.android.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class InteractionConfiguration extends JSONObject {
	public InteractionConfiguration(String json) throws JSONException {
		super(json);
	}
}
