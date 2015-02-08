/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.app.Activity;
import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public abstract class Interaction extends JSONObject {

	public static final String KEY_NAME = "interaction";

	public  static final String KEY_ID = "id";
	private static final String KEY_TYPE = "type";
	private static final String KEY_VERSION = "version";
	private static final String KEY_CONFIGURATION = "configuration";

	public static final String EVENT_NAME_LAUNCH = "launch";

	public Interaction(String json) throws JSONException {
		super(json);
	}

	public void sendLaunchEvent(Activity activity) {
		EngagementModule.engageInternal(activity, this, Interaction.EVENT_NAME_LAUNCH);
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
			// Ignore
		}
		return null;
	}

	public Type getType() {
		try {
			if (!isNull(KEY_TYPE)) {
				return Type.parse(getString(KEY_TYPE));
			}
		} catch (JSONException e) {
			// Ignore
		}
		return Type.unknown;
	}

	public Integer getVersion() {
		try {
			if (!isNull(KEY_VERSION)) {
				return getInt(KEY_VERSION);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public InteractionConfiguration getConfiguration() {
		try {
			if (!isNull(KEY_CONFIGURATION)) {
				return new InteractionConfiguration(getJSONObject(KEY_CONFIGURATION).toString());
			}
		} catch (JSONException e) {
			// Ignore
		}
		return new InteractionConfiguration();
	}

	public static enum Type {
		UpgradeMessage,
		EnjoymentDialog,
		RatingDialog,
		FeedbackDialog,
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
				Log.v("Error parsing unknown Interaction.Type: " + type);
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
					case FeedbackDialog:
						return new FeedbackDialogInteraction(interactionString);
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
				// Ignore
			}
			return null;
		}
	}
}
