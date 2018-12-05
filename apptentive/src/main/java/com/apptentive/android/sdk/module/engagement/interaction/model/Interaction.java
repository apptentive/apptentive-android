/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public abstract class Interaction extends JSONObject {

	public static final String KEY_NAME = "interaction";

	public static final String KEY_ID = "id";
	private static final String KEY_TYPE = "type";
	public static final String KEY_DISPLAY_TYPE = "display_type";
	private static final String KEY_VERSION = "version";
	protected static final String KEY_CONFIGURATION = "configuration";

	public Interaction(String json) throws JSONException {
		super(json);
	}

	/**
	 * Override this method if the subclass has certain restrictions other than Criteria that it needs to evaluate in
	 * order to know if it can run. Example: Interactions that will require an internet connection must override and check
	 * for network availability.
	 *
	 * @param context The Context from which this method is called.
	 * @return true if this interaction can run.
	 */
	protected boolean isInRunnableState(Context context) {
		return true;
	}

	public String getId() {
		try {
			if (!isNull(KEY_ID)) {
				return getString(KEY_ID);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	/**
	 * Interactions that have a title that can be displayed in a toolbar should return it from this method.
	 * @return The title to be set in the toolbar.
	 */
	public String getTitle() {
		return null;
	}

	public Type getType() {
		try {
			if (!isNull(KEY_TYPE)) {
				return Type.parse(getString(KEY_TYPE));
			}
		} catch (JSONException e) {
			logException(e);
		}
		return Type.unknown;
	}

	public DisplayType getDisplayType() {
		try {
			if (isNull(KEY_DISPLAY_TYPE)) {
				return getDefaultDisplayType();
			}
			return DisplayType.parse(getString(KEY_DISPLAY_TYPE));
		} catch (JSONException e) {
			logException(e);
		}
		return DisplayType.unknown;
	}

	protected DisplayType getDefaultDisplayType() {
		return DisplayType.unknown;
	}

	public Integer getVersion() {
		try {
			if (!isNull(KEY_VERSION)) {
				return getInt(KEY_VERSION);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public InteractionConfiguration getConfiguration() {
		try {
			if (!isNull(KEY_CONFIGURATION)) {
				return new InteractionConfiguration(getJSONObject(KEY_CONFIGURATION).toString());
			}
		} catch (JSONException e) {
			logException(e);
		}
		return new InteractionConfiguration();
	}

	public enum Type {
		UpgradeMessage,
		EnjoymentDialog,
		RatingDialog,
		MessageCenter,
		AppStoreRating,
		Survey,
		TextModal,
		NavigateToLink,
		unknown;

		public static Type parse(String type) {
			try {
				return Type.valueOf(type);
			} catch (IllegalArgumentException e) {
				ApptentiveLog.v(INTERACTIONS, "Error parsing unknown Interaction.Type: " + type);
				logException(e);
			}
			return unknown;
		}
	}

	public enum DisplayType {
		notification,
		unknown;

		public static DisplayType parse(String type) {
			try {
				return DisplayType.valueOf(type);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Error parsing interaction display_type: " + type);
				logException(e);
			}
			return unknown;
		}
	}

	public static class Factory {
		public static Interaction parseInteraction(String interactionString) {
			if (interactionString == null) {
				return null;
			}
			try {
				Interaction.Type type = Type.unknown;
				JSONObject interaction = new JSONObject(interactionString);
				if (interaction.has(KEY_TYPE)) {
					type = Type.parse(interaction.getString(KEY_TYPE));
				}
				switch (type) {
					case UpgradeMessage:
						return new UpgradeMessageInteraction(interactionString);
					case EnjoymentDialog:
						return new EnjoymentDialogInteraction(interactionString);
					case RatingDialog:
						return new RatingDialogInteraction(interactionString);
					case MessageCenter:
						return new MessageCenterInteraction(interactionString);
					case AppStoreRating:
						return new AppStoreRatingInteraction(interactionString);
					case Survey:
						return new SurveyInteraction(interactionString);
					case TextModal:
						return new TextModalInteraction(interactionString);
					case NavigateToLink:
						return new NavigateToLinkInteraction(interactionString);
					case unknown:
						break;
				}
			} catch (JSONException e) {
				ApptentiveLog.w(INTERACTIONS, e, "Error parsing Interaction");
				logException(e);
			}
			return null;
		}
	}
}
