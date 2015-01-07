/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.Log;
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

/**
 * @author Sky Kelsey
 */
public class InteractionManager {

	private static Interactions interactions;
	private static Targets targets;
	private static Boolean pollForInteractions;

	public static Interactions getInteractions(Context context) {
		if (interactions == null) {
			loadInteractions(context);
		}
		return interactions;
	}

	public static Targets getTargets(Context context) {
		if (targets == null) {
			loadTargets(context);
		}
		return targets;
	}

	public static Interaction getApplicableInteraction(Context context, String eventLabel) {

		Targets targets = getTargets(context);

		// TODO: Check with Interaction to see if it can run. Otherwise, put that check in the criteria?
		if (targets != null) {
			String interactionId = targets.getApplicableInteraction(context, eventLabel);
			if (interactionId != null) {
				Interactions interactions = getInteractions(context);
				return interactions.getInteraction(interactionId);
			}
		}
		return null;
	}

	public static void asyncFetchAndStoreInteractions(final Context context) {

		if (!isPollForInteractions(context)) {
			Log.v("Interaction polling is disabled.");
			return;
		}

		if (hasCacheExpired(context)) {
			Log.d("Interaction cache has expired. Fetching new interactions.");
			Thread thread = new Thread() {
				public void run() {
					fetchAndStoreInteractions(context);
				}
			};
			Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					Log.w("UncaughtException in InteractionManager.", throwable);
					MetricModule.sendError(context.getApplicationContext(), throwable, null, null);
				}
			};
			thread.setUncaughtExceptionHandler(handler);
			thread.setName("Apptentive-FetchInteractions");
			thread.start();
		} else {
			Log.d("Interaction cache has not expired. Using existing interactions.");
		}
	}

	private static void fetchAndStoreInteractions(Context context) {
		ApptentiveHttpResponse response = ApptentiveClient.getInteractions();

		if (response != null && response.isSuccessful()) {
			String interactionsPayloadString = response.getContent();

			// Store new integration cache expiration.
			String cacheControl = response.getHeaders().get("Cache-Control");
			Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
			if (cacheSeconds == null) {
				cacheSeconds = Constants.CONFIG_DEFAULT_INTERACTION_CACHE_EXPIRATION_DURATION_SECONDS;
			}
			updateCacheExpiration(context, cacheSeconds);
			storeInteractionsPayloadString(context, interactionsPayloadString);
		}
	}

	/**
	 * Made public for testing. There is no other reason to use this method directly.
	 */
	public static void storeInteractionsPayloadString(Context context, String interactionsPayloadString) {
		try {
			InteractionsPayload payload = new InteractionsPayload(interactionsPayloadString);
			Interactions interactions = payload.getInteractions();
			Targets targets = payload.getTargets();
			if (interactions != null && targets != null) {
				InteractionManager.interactions = interactions;
				InteractionManager.targets = targets;
				saveInteractions(context);
				saveTargets(context);
			} else {
				Log.e("Unable to save payloads.");
			}
		} catch (JSONException e) {
			Log.w("Invalid InteractionsPayload received.");
		}
	}

	public static void clear(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().remove(Constants.PREF_KEY_INTERACTIONS).commit();
		prefs.edit().remove(Constants.PREF_KEY_TARGETS).commit();
		interactions = null;
		targets = null;
	}

	private static void saveInteractions(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_INTERACTIONS, interactions.toString()).commit();
	}

	private static Interactions loadInteractions(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String interactionsString = prefs.getString(Constants.PREF_KEY_INTERACTIONS, null);
		if (interactionsString != null) {
			try {
				return new Interactions(interactionsString);
			} catch (JSONException e) {
				Log.w("Exception creating Interactions object.", e);
			}
		}
		return null;
	}

	private static void saveTargets(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_TARGETS, targets.toString()).commit();
	}

	private static Targets loadTargets(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String targetsString = prefs.getString(Constants.PREF_KEY_TARGETS, null);
		if (targetsString != null) {
			try {
				return new Targets(targetsString);
			} catch (JSONException e) {
				Log.w("Exception creating Targets object.", e);
			}
		}
		return null;
	}

	private static boolean hasCacheExpired(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		long expiration = prefs.getLong(Constants.PREF_KEY_INTERACTIONS_PAYLOAD_CACHE_EXPIRATION, 0);
		return expiration < System.currentTimeMillis();
	}

	private static void updateCacheExpiration(Context context, long duration) {
		long expiration = System.currentTimeMillis() + (duration * 1000);
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putLong(Constants.PREF_KEY_INTERACTIONS_PAYLOAD_CACHE_EXPIRATION, expiration).commit();
	}

	public static boolean isPollForInteractions(Context context) {
		if (pollForInteractions == null) {
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
			pollForInteractions = prefs.getBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, true);
		}
		return pollForInteractions;
	}

	public static void setPollForInteractions(Context context, boolean pollForInteractions) {
		InteractionManager.pollForInteractions = pollForInteractions;
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Constants.PREF_KEY_POLL_FOR_INTERACTIONS, pollForInteractions).commit();
	}
}
