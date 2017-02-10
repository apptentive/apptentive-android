/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionManifest;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.storage.SessionData;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

import java.util.concurrent.atomic.AtomicBoolean;

public class InteractionManager {

	private Boolean pollForInteractions;
	// boolean to prevent multiple fetching threads
	private AtomicBoolean isFetchPending = new AtomicBoolean(false);

	private SessionData sessionData;

	public InteractionManager(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	public interface InteractionUpdateListener {
		void onInteractionUpdated(boolean successful);
	}

	public Interaction getApplicableInteraction(String eventLabel) {

		SessionData sessionData = ApptentiveInternal.getInstance().getSessionData();
		if (sessionData == null) {
			return null;
		}
		String targetsString = sessionData.getTargets();
		if (targetsString != null) {
			try {
				Targets targets = new Targets(sessionData.getTargets());
				String interactionId = targets.getApplicableInteraction(eventLabel);
				if (interactionId != null) {
					String interactionsString = sessionData.getInteractions();
					if (interactionsString != null) {
						Interactions interactions = new Interactions(interactionsString);
						return interactions.getInteraction(interactionId);
					}
				}
			} catch (JSONException e) {
				ApptentiveLog.e("");
			}
		}
		return null;
	}

	// TODO: Refactor this class to dispatch to its own queue.
	public void fetchInteractions() {
		ApptentiveLog.v("Fetching Interactions");
		if (sessionData != null) {
			InteractionManager interactionManager = sessionData.getInteractionManager();
			if (interactionManager != null) {
				ApptentiveHttpResponse response = ApptentiveClient.getInteractions();

				// TODO: Move this to global config
				SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
				boolean updateSuccessful = true;

				// We weren't able to connect to the internet.
				if (response.isException()) {
					prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, false).apply();
					updateSuccessful = false;
				}
				// We got a server error.
				else if (!response.isSuccessful()) {
					prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, true).apply();
					updateSuccessful = false;
				}

				if (updateSuccessful) {
					String interactionsPayloadString = response.getContent();

					// Store new integration cache expiration.
					String cacheControl = response.getHeaders().get("Cache-Control");
					Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
					if (cacheSeconds == null) {
						cacheSeconds = Constants.CONFIG_DEFAULT_INTERACTION_CACHE_EXPIRATION_DURATION_SECONDS;
					}
					sessionData.setInteractionExpiration(Util.currentTimeSeconds() + cacheSeconds);
					try {
						InteractionManifest payload = new InteractionManifest(interactionsPayloadString);
						Interactions interactions = payload.getInteractions();
						Targets targets = payload.getTargets();
						if (interactions != null && targets != null) {
							sessionData.setTargets(targets.toString());
							sessionData.setInteractions(interactions.toString());
						} else {
							ApptentiveLog.e("Unable to save interactionManifest.");
						}
					} catch (JSONException e) {
						ApptentiveLog.w("Invalid InteractionManifest received.");
					}					}
				ApptentiveLog.d("Fetching new Interactions asyncTask finished. Successful? %b", updateSuccessful);
				// Update pending state on UI thread after finishing the task
				ApptentiveInternal.getInstance().notifyInteractionUpdated(updateSuccessful);
			}
		} else {
			ApptentiveLog.v("Cancelled Interaction fetch due to null SessionData.");
		}
	}

	/**
	 * Made public for testing. There is no other reason to use this method directly.
	 */
	public void storeInteractionManifest(String interactionManifest) {
		SessionData sessionData = ApptentiveInternal.getInstance().getSessionData();
		if (sessionData == null) {
			return;
		}
		try {
			InteractionManifest payload = new InteractionManifest(interactionManifest);
			Interactions interactions = payload.getInteractions();
			Targets targets = payload.getTargets();
			if (interactions != null && targets != null) {
				sessionData.setTargets(targets.toString());
				sessionData.setInteractions(interactions.toString());
			} else {
				ApptentiveLog.e("Unable to save interactionManifest.");
			}
		} catch (JSONException e) {
			ApptentiveLog.w("Invalid InteractionManifest received.");
		}

	}

	public boolean isPollForInteractions() {
		if (pollForInteractions == null) {
			SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
			pollForInteractions = prefs.getBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, true);
		}
		return pollForInteractions;
	}

	public void setPollForInteractions(boolean pollForInteractions) {
		this.pollForInteractions = pollForInteractions;
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		prefs.edit().putBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, pollForInteractions).apply();
	}
}
