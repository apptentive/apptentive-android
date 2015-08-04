/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageCenterInteraction extends Interaction {

	public static final String KEY_TITLE = "title";
	public static final String KEY_BRANDING = "branding";
	public static final String KEY_COMPOSER = "composer";
	public static final String KEY_COMPOSER_TITLE = "title";
	public static final String KEY_COMPOSER_HINT_TEXT = "hint_text";
	public static final String KEY_COMPOSER_SEND_BUTTON = "send_button";
	public static final String KEY_GREETING = "greeting";
	public static final String KEY_GREETING_TITLE = "title";
	public static final String KEY_GREETING_BODY = "body";
	public static final String KEY_GREETING_IMAGE = "image_url";
	public static final String KEY_STATUS = "status";
	public static final String KEY_STATUS_BODY = "body";
	public static final String KEY_AUTOMATED_MESSAGE = "automated_message";
	public static final String KEY_AUTOMATED_MESSAGE_BODY = "body";
	public static final String KEY_ERROR = "error_messages";
	public static final String KEY_ERROR_HTTP_TITLE = "http_error_title";
	public static final String KEY_ERROR_HTTP_BODY = "http_error_body";
	public static final String KEY_ERROR_NETWORK_TITLE = "network_error_title";
	public static final String KEY_ERROR_NETWORK_BODY = "network_error_body";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_PROFILE_REQUEST = "request";
	public static final String KEY_PROFILE_REQUIRE = "require";
	public static final String KEY_PROFILE_INIT = "initial";
	public static final String KEY_PROFILE_INIT_TITLE = "title";
	public static final String KEY_PROFILE_INIT_NAME_HINT = "name_hint";
	public static final String KEY_PROFILE_INIT_EMAIL_HINT = "email_hint";
	public static final String KEY_PROFILE_INIT_SKIP_BUTTON = "skip_button";
	public static final String KEY_PROFILE_INIT_SAVE_BUTTON = "save_button";
	public static final String KEY_PROFILE_EDIT = "edit";
	public static final String KEY_PROFILE_EDIT_TITLE = "title";
	public static final String KEY_PROFILE_EDIT_NAME_HINT = "name_hint";
	public static final String KEY_PROFILE_EDIT_EMAIL_HINT = "email_hint";
	public static final String KEY_PROFILE_EDIT_SKIP_BUTTON = "skip_button";
	public static final String KEY_PROFILE_EDIT_SAVE_BUTTON = "save_button";


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

	public String getBranding() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration != null && !configuration.isNull(KEY_BRANDING)) {
			return configuration.optString(KEY_BRANDING, null);
		}
		return null;
	}

	public MessageCenterComposingItem getComposerArea() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject composer = configuration.optJSONObject(KEY_COMPOSER);
		return new MessageCenterComposingItem(
				MessageCenterComposingItem.COMPOSING_ITEM_AREA,
				composer.optString(KEY_COMPOSER_TITLE, null),
				composer.optString(KEY_COMPOSER_HINT_TEXT, null),
				null,
				null,
				null);
	}

	public MessageCenterComposingItem getComposerBar() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject composer = configuration.optJSONObject(KEY_COMPOSER);
		return new MessageCenterComposingItem(
				MessageCenterComposingItem.COMPOSING_ITEM_ACTIONBAR,
				null,
				null,
				null,
				composer.optString(KEY_COMPOSER_SEND_BUTTON, null),
				null);
	}

	public MessageCenterComposingItem getWhoCardInit() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject profile = configuration.optJSONObject(KEY_PROFILE).optJSONObject(KEY_PROFILE_INIT);
		return new MessageCenterComposingItem(
				MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD,
				profile.optString(KEY_PROFILE_INIT_TITLE, null),
				profile.optString(KEY_PROFILE_INIT_NAME_HINT, null),
				profile.optString(KEY_PROFILE_INIT_EMAIL_HINT, null),
				profile.optString(KEY_PROFILE_INIT_SAVE_BUTTON, null),
				profile.optString(KEY_PROFILE_INIT_SKIP_BUTTON, null));
	}

	public MessageCenterComposingItem getWhoCardEdit() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject profile = configuration.optJSONObject(KEY_PROFILE).optJSONObject(KEY_PROFILE_EDIT);
		return new MessageCenterComposingItem(
				MessageCenterComposingItem.COMPOSING_ITEM_WHOCARD,
				profile.optString(KEY_PROFILE_EDIT_TITLE, null),
				profile.optString(KEY_PROFILE_EDIT_NAME_HINT, null),
				profile.optString(KEY_PROFILE_EDIT_EMAIL_HINT, null),
				profile.optString(KEY_PROFILE_EDIT_SAVE_BUTTON, null),
				profile.optString(KEY_PROFILE_EDIT_SKIP_BUTTON, null));
	}


	public MessageCenterGreeting getGreeting() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject greeting = configuration.optJSONObject(KEY_GREETING);
		if (greeting == null) {
			return null;
		}
		return new MessageCenterGreeting(greeting.optString(KEY_GREETING_TITLE, null),
				greeting.optString(KEY_GREETING_BODY, null), greeting.optString(KEY_GREETING_IMAGE, null));
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

	public MessageCenterStatus getErrorStatusServer(Context context) {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject error_status = configuration.optJSONObject(KEY_ERROR);
		if (error_status == null) {
			return null;
		}
		return new MessageCenterStatus(error_status.optString(KEY_ERROR_HTTP_TITLE,
				context.getResources().getString(R.string.apptentive_message_center_status_error_title)),
				error_status.optString(KEY_ERROR_HTTP_BODY,
						context.getResources().getString(R.string.apptentive_message_center_status_error_body)));
	}

	public MessageCenterStatus getErrorStatusNetwork(Context context) {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject error_status = configuration.optJSONObject(KEY_ERROR);
		if (error_status == null) {
			return null;
		}
		return new MessageCenterStatus(error_status.optString(KEY_ERROR_NETWORK_TITLE,
				context.getResources().getString(R.string.apptentive_message_center_status_error_title)),
				error_status.optString(KEY_ERROR_NETWORK_BODY,
						context.getResources().getString(R.string.apptentive_message_center_status_error_body)));
	}
}
