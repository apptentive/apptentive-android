/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.ApptentiveViewActivity;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.Composer;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.WhoCard;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageCenterInteraction extends Interaction {

	public static final String KEY_TITLE = "title";
	public static final String KEY_BRANDING = "branding";
	public static final String KEY_COMPOSER = "composer";
	public static final String KEY_COMPOSER_TITLE = "title";
	public static final String KEY_COMPOSER_HINT_TEXT = "hint_text";
	public static final String KEY_COMPOSER_SEND_BUTTON = "send_button";
	public static final String KEY_COMPOSER_CLOSE_BODY = "close_confirm_body";
	public static final String KEY_COMPOSER_CLOSE_DISCARD = "close_discard_button";
	public static final String KEY_COMPOSER_CLOSE_CANCEL = "close_cancel_button";
	public static final String KEY_GREETING = "greeting";
	public static final String KEY_GREETING_TITLE = "title";
	public static final String KEY_GREETING_BODY = "body";
	public static final String KEY_GREETING_IMAGE = "image_url";
	public static final String KEY_STATUS = "status";
	public static final String KEY_STATUS_BODY = "body";
	public static final String KEY_AUTOMATED_MESSAGE = "automated_message";
	public static final String KEY_AUTOMATED_MESSAGE_BODY = "body";
	public static final String KEY_ERROR = "error_messages";
	public static final String KEY_ERROR_HTTP_BODY = "http_error_body";
	public static final String KEY_ERROR_NETWORK_BODY = "network_error_body";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_PROFILE_REQUEST = "request";
	public static final String KEY_PROFILE_REQUIRE = "require";
	public static final String KEY_PROFILE_INIT = "initial";
	public static final String KEY_PROFILE_INIT_TITLE = "title";
	public static final String KEY_PROFILE_INIT_NAME_HINT = "name_hint";
	public static final String KEY_PROFILE_INIT_EMAIL_HINT = "email_hint";
	public static final String KEY_PROFILE_INIT_EMAIL_EXPLANATION = "email_explanation";
	public static final String KEY_PROFILE_INIT_SKIP_BUTTON = "skip_button";
	public static final String KEY_PROFILE_INIT_SAVE_BUTTON = "save_button";
	public static final String KEY_PROFILE_EDIT = "edit";
	public static final String KEY_PROFILE_EDIT_TITLE = "title";
	public static final String KEY_PROFILE_EDIT_NAME_HINT = "name_hint";
	public static final String KEY_PROFILE_EDIT_EMAIL_HINT = "email_hint";
	public static final String KEY_PROFILE_EDIT_EMAIL_EXPLANATION = "email_explanation";
	public static final String KEY_PROFILE_EDIT_SKIP_BUTTON = "skip_button";
	public static final String KEY_PROFILE_EDIT_SAVE_BUTTON = "save_button";


	// The server guarantees that an instance of this Interaction will be targeted to the following internal event name.
	public static final String DEFAULT_INTERNAL_EVENT_NAME = "show_message_center";

	// Events
	public static final String EVENT_NAME_CLOSE = "close";
	public static final String EVENT_NAME_CANCEL = "cancel";
	public static final String EVENT_NAME_READ = "read";
	public static final String EVENT_NAME_COMPOSE_OPEN = "compose_open";
	public static final String EVENT_NAME_COMPOSE_CLOSE = "compose_close";
	public static final String EVENT_NAME_STATUS = "status";
	public static final String EVENT_NAME_MESSAGE_HTTP_ERROR = "message_http_error";
	public static final String EVENT_NAME_MESSAGE_NETWORK_ERROR = "message_network_error";
	public static final String EVENT_NAME_PROFILE_OPEN = "profile_open";
	public static final String EVENT_NAME_PROFILE_CLOSE = "profile_close";
	public static final String EVENT_NAME_PROFILE_SUBMIT = "profile_submit";
	public static final String EVENT_NAME_ATTACH = "attach";
	public static final String EVENT_NAME_ATTACHMENT_DELETE = "attachment_delete";
	public static final String EVENT_NAME_ATTACHMENT_CANCEL = "attachment_cancel";
/*
	// Not implemented on Android
	public static final String EVENT_NAME_GREETING_MESSAGE = "greeting_message";
	public static final String EVENT_NAME_KEYBOARD_OPEN = "keyboard_open";
	public static final String EVENT_NAME_KEYBOARD_CLOSE = "keyboard_close";
	public static final String EVENT_NAME_PROFILE_NAME = "profile_name";
	public static final String EVENT_NAME_PROFILE_EMAIL = "profile_email";
*/

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

	public Composer getComposer() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject composer = configuration.optJSONObject(KEY_COMPOSER);
		return new Composer(
			composer.optString(KEY_COMPOSER_TITLE, null),
			composer.optString(KEY_COMPOSER_CLOSE_BODY, null),
			composer.optString(KEY_COMPOSER_CLOSE_DISCARD, null),
			composer.optString(KEY_COMPOSER_CLOSE_CANCEL, null),
			composer.optString(KEY_COMPOSER_SEND_BUTTON, null),
			composer.optString(KEY_COMPOSER_HINT_TEXT, null)
		);
	}

	//When enabled, display Who Card to request profile info
	public boolean getWhoCardRequestEnabled() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return false;
		}
		JSONObject profile = configuration.optJSONObject(KEY_PROFILE);
		return profile.optBoolean(KEY_PROFILE_REQUEST, true);
	}

	public boolean getWhoCardRequired() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return false;
		}
		JSONObject profile = configuration.optJSONObject(KEY_PROFILE);
		return profile.optBoolean(KEY_PROFILE_REQUIRE, false);
	}

	public JSONObject getProfile() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		return configuration.optJSONObject(KEY_PROFILE);
	}

	public WhoCard getWhoCard() {
		try {
			return new WhoCard(getProfile().toString());
		} catch (JSONException e) {
			// Never happens.
		}
		return null;
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
		intent.setClass(context, ApptentiveViewActivity.class);
		intent.putExtra(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.MESSAGE_CENTER_ERROR);
		return intent;
	}

	// Regular status shows customer's hours, expected time until response
	public MessageCenterStatus getRegularStatus() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject status = configuration.optJSONObject(KEY_STATUS);
		if (status == null) {
			return null;
		}
		String statusBody = status.optString(KEY_STATUS_BODY);
		if (statusBody == null || statusBody.isEmpty()) {
			return null;
		}
		return new MessageCenterStatus(statusBody, null);
	}

	public MessageCenterStatus getErrorStatusServer() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject errorStatus = configuration.optJSONObject(KEY_ERROR);
		if (errorStatus == null) {
			return null;
		}
		return new MessageCenterStatus(errorStatus.optString(KEY_ERROR_HTTP_BODY), R.drawable.apptentive_ic_error);
	}

	public MessageCenterStatus getErrorStatusNetwork() {
		InteractionConfiguration configuration = getConfiguration();
		if (configuration == null) {
			return null;
		}
		JSONObject errorStatus = configuration.optJSONObject(KEY_ERROR);
		if (errorStatus == null) {
			return null;
		}
		return new MessageCenterStatus(errorStatus.optString(KEY_ERROR_NETWORK_BODY), R.drawable.apptentive_ic_no_connection);
	}
}
