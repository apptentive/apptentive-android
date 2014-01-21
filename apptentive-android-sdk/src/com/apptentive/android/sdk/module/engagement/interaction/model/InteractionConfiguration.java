package com.apptentive.android.sdk.module.engagement.interaction.model;

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
