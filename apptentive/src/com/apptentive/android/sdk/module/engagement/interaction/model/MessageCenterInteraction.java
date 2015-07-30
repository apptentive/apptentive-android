/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.ActivityContent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageCenterInteraction extends Interaction {

	public static final String KEY_TITLE = "title";
	public static final String KEY_COMPOSER = "composer";
	public static final String KEY_COMPOSER_TITLE = "title";
	public static final String KEY_GREETING = "greeting";
	public static final String KEY_GREETING_TITLE = "title";
	public static final String KEY_GREETING_BODY = "body";
	public static final String KEY_STATUS = "status";
	public static final String KEY_STATUS_BODY = "body";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_PROFILE_REQUEST = "request";
	public static final String KEY_PROFILE_REQUIRE = "require";
	public static final String KEY_PROFILE_INIT_TITLE = "init_title";
	public static final String KEY_PROFILE_EDIT_TITLE = "edit_title";
	public static final String KEY_PROFILE_NAME_HINT = "name_hint";
	public static final String KEY_PROFILE_EMAIL_HINT = "email_hint";
	public static final String KEY_PROFILE_INIT_SKIP_BUTTON = "init_skip_button";
	public static final String KEY_PROFILE_EDIT_SKIP_BUTTON = "edit_skip_button";
	public static final String KEY_PROFILE_INIT_SAVE_BUTTON = "init_save_button";
	public static final String KEY_PROFILE_EDIT_SAVE_BUTTON = "edit_save_button";
	public static final String KEY_AUTOMATED_MESSAGE = "automated_message";
	public static final String KEY_AUTOMATED_MESSAGE_BODY = "body";

	// The server guarantees that an instance of this Interaction will be targetted to the following internal event name.
	public static final String DEFAULT_INTERNAL_EVENT_NAME = "show_message_center";

	public MessageCenterInteraction(String json) throws JSONException {
		super(json);
	}

	public String getTitle() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration != null && !configuration.isNull(KEY_TITLE)) {
			return configuration.optString(KEY_TITLE, null);
		}
		return null;
	}

	public JSONObject getComposer() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_COMPOSER);
	}

	public String getComposerTitle() {
		JSONObject composer = getComposer();
		if (composer == null) {
			return null;
		}
		return composer.optString(KEY_COMPOSER_TITLE, null);
	}

	public JSONObject getGreeting() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_GREETING);
	}

	public String getGreetingTitle() {
		JSONObject greeting = getGreeting();
		if (greeting == null) {
			return null;
		}
		return greeting.optString(KEY_GREETING_TITLE, null);
	}

	public String getGreetingBody() {
		JSONObject greeting = getGreeting();
		if (greeting == null) {
			return null;
		}
		return greeting.optString(KEY_GREETING_BODY, null);
	}

	public JSONObject getStatus() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_STATUS);
	}

	public String getStatusBody() {
		JSONObject status = getStatus();
		if (status == null) {
			return null;
		}
		return status.optString(KEY_STATUS_BODY, null);
	}

	public JSONObject getProfile() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_PROFILE);
	}

	public boolean isProfileRequested() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return false;
		}
		return profile.optBoolean(KEY_PROFILE_REQUEST);
	}

	public boolean isProfileRequired() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return false;
		}
		return profile.optBoolean(KEY_PROFILE_REQUIRE);
	}

	public String getProfileInitTitle() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_INIT_TITLE, null);
	}

	public String getProfileEditTitle() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_EDIT_TITLE, null);
	}

	public String getProfileNameHint() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_NAME_HINT, null);
	}

	public String getProfileEmailHint() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_EMAIL_HINT, null);
	}

	public String getProfileInitSkipButton() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_INIT_SKIP_BUTTON, null);
	}

	public String getProfileEditSkipButton() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_EDIT_SKIP_BUTTON, null);
	}

	public String getProfileInitSaveButton() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_INIT_SAVE_BUTTON, null);
	}

	public String getProfileEditSaveButton() {
		JSONObject profile = getProfile();
		if (profile == null) {
			return null;
		}
		return profile.optString(KEY_PROFILE_EDIT_SAVE_BUTTON, null);
	}


	public JSONObject getContextualMessage() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_AUTOMATED_MESSAGE);
	}

	public String getContextualMessageBody() {
		JSONObject auto_msg = getContextualMessage();
		if (auto_msg == null) {
			return null;
		}
		return auto_msg.optString(KEY_AUTOMATED_MESSAGE_BODY, null);
	}

	public void clearContextualMessage() {
		JSONObject auto_msg = getContextualMessage();
		if (auto_msg == null) {
			return;
		}
		try {
			auto_msg.put(KEY_AUTOMATED_MESSAGE_BODY, null);
			InteractionConfiguration configuration = getConfiguration();
			configuration.put(KEY_AUTOMATED_MESSAGE, auto_msg);
			put(Interaction.KEY_CONFIGURATION, configuration);
		} catch (JSONException e) {
			// catch and do nothing
		}
	}

	public static Intent generateMessageCenterErrorIntent(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, ViewActivity.class);
		intent.putExtra(ActivityContent.KEY, ActivityContent.Type.MESSAGE_CENTER_ERROR.name());
		return intent;
	}
}
