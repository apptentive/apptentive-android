/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class FeedbackDialogInteraction extends Interaction {

	private static final String KEY_ASK_FOR_EMAIL = "ask_for_email";
	private static final String KEY_EMAIL_REQUIRED = "email_required";
	private static final String KEY_MESSAGE_CENTER_ENABLED = "message_center_enabled";

	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_EMAIL_HINT_TEXT = "email_hint_text";
	private static final String KEY_MESSAGE_HINT_TEXT = "message_hint_text";
	private static final String KEY_DECLINE_TEXT = "decline_text";
	private static final String KEY_SUBMIT_TEXT = "submit_text";

	private static final String KEY_THANK_YOU_TITLE = "thank_you_title";
	private static final String KEY_THANK_YOU_BODY = "thank_you_body";
	private static final String KEY_THANK_YOU_CLOSE_TEXT = "thank_you_close_text";
	private static final String KEY_THANK_YOU_VIEW_MESSAGES_TEXT = "thank_you_view_messages_text";

	public FeedbackDialogInteraction(String json) throws JSONException {
		super(json);
	}

	public boolean isAskForEmail() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_ASK_FOR_EMAIL)) {
				return configuration.getBoolean(KEY_ASK_FOR_EMAIL);
			}
		} catch (JSONException e) {
		}
		return true;
	}

	public boolean isEmailRequired() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_EMAIL_REQUIRED)) {
				return configuration.getBoolean(KEY_EMAIL_REQUIRED);
			}
		} catch (JSONException e) {
		}
		return false;
	}

	public boolean isMessageCenterEnabled() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_MESSAGE_CENTER_ENABLED)) {
				return configuration.getBoolean(KEY_MESSAGE_CENTER_ENABLED);
			}
		} catch (JSONException e) {
		}
		return true;
	}

	public String getTitle() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_TITLE)) {
				return configuration.getString(KEY_TITLE);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getBody() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_BODY)) {
				return configuration.getString(KEY_BODY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getEmailHintText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_EMAIL_HINT_TEXT)) {
				return configuration.getString(KEY_EMAIL_HINT_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getMessageHintText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_MESSAGE_HINT_TEXT)) {
				return configuration.getString(KEY_MESSAGE_HINT_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getDeclineText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_DECLINE_TEXT)) {
				return configuration.getString(KEY_DECLINE_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getSubmitText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_SUBMIT_TEXT)) {
				return configuration.getString(KEY_SUBMIT_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getThankYouTitle() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_THANK_YOU_TITLE)) {
				return configuration.getString(KEY_THANK_YOU_TITLE);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getThankYouBody() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_THANK_YOU_BODY)) {
				return configuration.getString(KEY_THANK_YOU_BODY);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getThankYouCloseText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_THANK_YOU_CLOSE_TEXT)) {
				return configuration.getString(KEY_THANK_YOU_CLOSE_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}

	public String getThankYouViewMessagesText() {
		try {
			InteractionConfiguration configuration = getConfiguration();
			if (configuration != null && configuration.has(KEY_THANK_YOU_VIEW_MESSAGES_TEXT)) {
				return configuration.getString(KEY_THANK_YOU_VIEW_MESSAGES_TEXT);
			}
		} catch (JSONException e) {
		}
		return null;
	}
}
