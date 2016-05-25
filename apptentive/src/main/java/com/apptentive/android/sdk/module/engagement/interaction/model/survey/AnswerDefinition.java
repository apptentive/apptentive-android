/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.survey;

import org.json.JSONException;
import org.json.JSONObject;

public class AnswerDefinition extends JSONObject {

	private static final String KEY_ID = "id";
	private static final String KEY_VALUE = "value";
	private static final String KEY_TYPE = "type";
	private static final String KEY_HINT = "hint";

	public static final String TYPE_NONE = "none";
	public static final String TYPE_OPTION = "select_option";
	public static final String TYPE_OTHER = "select_other";

	public AnswerDefinition(String json) throws JSONException {
		super(json);
	}

	public String getId() {
		return optString(KEY_ID, null);
	}

	public String getValue() {
		return optString(KEY_VALUE, null);
	}

	public String getType() {
		return optString(KEY_TYPE, TYPE_NONE);
	}

	public String getHint() {
		return optString(KEY_HINT, null);
	}
}
