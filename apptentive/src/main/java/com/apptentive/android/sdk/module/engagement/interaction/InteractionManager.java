/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionsPayload;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sky Kelsey
 */
public class InteractionManager {

	private Interactions interactions;
	private Targets targets;
	private Boolean pollForInteractions;
	// boolean to prevent multiple fetching threads
	private AtomicBoolean isFetchPending = new AtomicBoolean(false);

	public interface InteractionUpdateListener {
		void onInteractionUpdated(boolean successful);
	}

	public Interactions getInteractions() {
		if (interactions == null) {
			interactions = loadInteractions();
		}
		return interactions;
	}

	public Targets getTargets() {
		if (targets == null) {
			targets = loadTargets();
		}
		return targets;
	}

	public Interaction getApplicableInteraction(String eventLabel) {

		Targets targets = getTargets();

		if (targets != null) {
			String interactionId = targets.getApplicableInteraction(eventLabel);
			if (interactionId != null) {
				Interactions interactions = getInteractions();
				return interactions.getInteraction(interactionId);
			}
		}
		return null;
	}

	public void asyncFetchAndStoreInteractions() {

		if (!isPollForInteractions()) {
			ApptentiveLog.v("Interaction polling is disabled.");
			return;
		}

		boolean force = ApptentiveInternal.getInstance().isApptentiveDebuggable();
		// Check isFetchPending to only allow one asyncTask at a time when fetching interaction
		if (isFetchPending.compareAndSet(false, true) && (force || hasCacheExpired())) {
			AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
				// Hold onto the exception from the AsyncTask instance for later handling in UI thread
				private Exception e = null;

				@Override
				protected Boolean doInBackground(Void... params) {
					Boolean result = new Boolean(false);
					try {
						result = fetchAndStoreInteractions();
					} catch (Exception e) {
						this.e = e;
					}
					return result;
				}

				@Override
				protected void onPostExecute(Boolean v) {
					isFetchPending.set(false);
					if (e == null) {
						ApptentiveLog.i("Fetching new Interactions asyncTask finished. Result:" + v.booleanValue());
						// Update pending state on UI thread after finishing the task
						ApptentiveInternal.getInstance().notifyInteractionUpdated(v.booleanValue());
					} else {
						ApptentiveLog.w("Unhandled Exception thrown from fetching new Interactions asyncTask", e);
						MetricModule.sendError(e, null, null);
					}
				}
			};

			ApptentiveLog.i("Fetching new Interactions asyncTask scheduled");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		} else {
			ApptentiveLog.v("Using cached Interactions.");
		}
	}

	// This method will be run from a worker thread created by asyncTask
	private boolean fetchAndStoreInteractions() {
		ApptentiveLog.i("Fetching new Interactions asyncTask started");
		ApptentiveHttpResponse response = ApptentiveClient.getInteractions();

		// We weren't able to connect to the internet.
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		boolean updateSuccessful = true;
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
			updateCacheExpiration(cacheSeconds);
			storeInteractionsPayloadString(interactionsPayloadString);
		}

		return updateSuccessful;
	}

	/**
	 * Made public for testing. There is no other reason to use this method directly.
	 */
	public void storeInteractionsPayloadString(String interactionsPayloadString) {
		try {
			InteractionsPayload payload = new InteractionsPayload(interactionsPayloadString);
			Interactions interactions = payload.getInteractions();
			Targets targets = payload.getTargets();
			if (interactions != null && targets != null) {
				this.interactions = interactions;
				this.targets = targets;
				saveInteractions();
				saveTargets();
			} else {
				ApptentiveLog.e("Unable to save payloads.");
			}
		} catch (JSONException e) {
			ApptentiveLog.w("Invalid InteractionsPayload received.");
		}
	}

	public void clear() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().remove(Constants.PREF_KEY_INTERACTIONS).apply();
		prefs.edit().remove(Constants.PREF_KEY_TARGETS).apply();
		interactions = null;
		targets = null;
	}

	private void saveInteractions() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_INTERACTIONS, interactions.toString()).apply();
	}

	private Interactions loadInteractions() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String interactionsString = prefs.getString(Constants.PREF_KEY_INTERACTIONS, null);
		if (interactionsString != null) {
			try {
				return new Interactions(interactionsString);
			} catch (JSONException e) {
				ApptentiveLog.w("Exception creating Interactions object.", e);
			}
		}
		return null;
	}

	private void saveTargets() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putString(Constants.PREF_KEY_TARGETS, targets.toString()).apply();
	}

	private Targets loadTargets() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		String targetsString = prefs.getString(Constants.PREF_KEY_TARGETS, null);
		if (targetsString != null) {
			try {
				return new Targets(targetsString);
			} catch (JSONException e) {
				ApptentiveLog.w("Exception creating Targets object.", e);
			}
		}
		return null;
	}

	private boolean hasCacheExpired() {
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		long expiration = prefs.getLong(Constants.PREF_KEY_INTERACTIONS_PAYLOAD_CACHE_EXPIRATION, 0);
		return expiration < System.currentTimeMillis();
	}

	public void updateCacheExpiration(long duration) {
		long expiration = System.currentTimeMillis() + (duration * 1000);
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putLong(Constants.PREF_KEY_INTERACTIONS_PAYLOAD_CACHE_EXPIRATION, expiration).apply();
	}

	public boolean isPollForInteractions() {
		if (pollForInteractions == null) {
			SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
			pollForInteractions = prefs.getBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, true);
		}
		return pollForInteractions;
	}

	public void setPollForInteractions(boolean pollForInteractions) {
		this.pollForInteractions = pollForInteractions;
		SharedPreferences prefs = ApptentiveInternal.getInstance().getSharedPrefs();
		prefs.edit().putBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, pollForInteractions).apply();
	}
}
