/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction;

import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionManifest;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.util.Constants;

import org.json.JSONException;

import java.util.concurrent.atomic.AtomicBoolean;

/** Determining wheather interaction should be fetched and performing the fetch */
public class InteractionManager {

	private Boolean pollForInteractions;
	// boolean to prevent multiple fetching threads
	private AtomicBoolean isFetchPending = new AtomicBoolean(false);

	private Conversation conversation;

	public InteractionManager(Conversation conversation) {
		this.conversation = conversation;
	}

	public interface InteractionUpdateListener {
		void onInteractionUpdated(boolean successful);
	}

	/**
	 * Made public for testing. There is no other reason to use this method directly.
	 */
	public void storeInteractionManifest(String interactionManifest) {
		Conversation conversation = ApptentiveInternal.getInstance().getConversation();
		if (conversation == null) {
			return;
		}
		try {
			InteractionManifest payload = new InteractionManifest(interactionManifest);
			Interactions interactions = payload.getInteractions();
			Targets targets = payload.getTargets();
			if (interactions != null && targets != null) {
				conversation.setTargets(targets.toString());
				conversation.setInteractions(interactions.toString());
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
