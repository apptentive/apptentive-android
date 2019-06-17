/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import org.json.JSONException;
import org.json.JSONObject;

public class WhoCard extends JSONObject implements MessageCenterListItem {

	private static final String KEY_REQUEST = "request";
	private static final String KEY_REQUIRE = "require";
	private static final String KEY_INITIAL = "initial";
	private static final String KEY_EDIT = "edit";
	private static final String KEY_TITLE = "title";
	private static final String KEY_NAME_HINT = "name_hint";
	private static final String KEY_EMAIL_HINT = "email_hint";
	private static final String KEY_EMAIL_EXPLANATION = "email_explanation";
	private static final String KEY_SKIP_BUTTON = "skip_button";
	private static final String KEY_SAVE_BUTTON = "save_button";

	private boolean initial;

	public WhoCard(String json) throws JSONException {
		super(json);
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	@Override
	public int getListItemType() {
		return WHO_CARD;
	}

	public boolean isRequest() {
		return optBoolean(KEY_REQUEST, false);
	}

	public boolean isRequire() {
		return optBoolean(KEY_REQUIRE, false);
	}

	private JSONObject getInitial() {
		return optJSONObject(KEY_INITIAL);
	}

	private JSONObject getEdit() {
		return optJSONObject(KEY_EDIT);
	}

	private JSONObject getApplicableConfig() {
		if (isInitial()) {
			return getInitial();
		} else {
			return getEdit();
		}
	}

	public String getTitle() {
		return getApplicableConfig().optString(KEY_TITLE, null);
	}

	public String getNameHint() {
		return getApplicableConfig().optString(KEY_NAME_HINT, null);
	}

	public String getEmailHint() {
		if (isRequire() && !isInitial()) {
			// Email is required when initial, but also if not initial, if the form itself was ever required.
			return getInitial().optString(KEY_EMAIL_HINT, null);

		}
		return getApplicableConfig().optString(KEY_EMAIL_HINT, null);
	}

	public String getEmailExplanation() {
		return getApplicableConfig().optString(KEY_EMAIL_EXPLANATION, null);
	}

	public String getSkipButton() {
		if (isRequire() && isInitial()) {
			// The Who Card will show up right when MC is opened in this scenario. Don't allow skipping it at this time.
			return null;
		}
		return getApplicableConfig().optString(KEY_SKIP_BUTTON, null);
	}

	public String getSaveButton() {
		return getApplicableConfig().optString(KEY_SAVE_BUTTON, null);
	}
}
