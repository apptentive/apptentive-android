package com.apptentive.android.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class InteractionCriteria extends JSONObject {
	private static final String KEY_DAYS_SINCE_INSTALL = "days_since_install";
	private static final String KEY_DAYS_SINCE_UPGRADE = "days_since_upgrade";
	private static final String KEY_APPLICATION_VERSION = "application_version";



	public InteractionCriteria(String json) throws JSONException {
		super(json);
	}
}
