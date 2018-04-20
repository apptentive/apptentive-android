/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;

public class InteractionManifest extends JSONObject {

	public InteractionManifest(String json) throws JSONException {
		super(json);
	}

	/**
	 * In addition to returning the Interactions contained in this payload, this method reformats the Interactions from a
	 * list into a map. The map is then used for further Interaction lookup.
	 * @return
	 */
	public Interactions getInteractions() {
		try {
			if (!isNull(Interactions.KEY_NAME)) {
				Object obj = get(Interactions.KEY_NAME);
				if (obj instanceof JSONArray) {
					Interactions interactions = new Interactions();
					JSONArray interactionsJSONArray = (JSONArray) obj;
					for (int i = 0; i < interactionsJSONArray.length(); i++) {
						Interaction interaction = Interaction.Factory.parseInteraction(interactionsJSONArray.getString(i));
						if (interaction != null) {
							interactions.put(interaction.getId(), interaction);
						} else {
							// This is an unknown Interaction type. Probably for a future SDK version.
						}
					}
					return interactions;
				}
			}
		} catch (JSONException e) {
			ApptentiveLog.w(INTERACTIONS, e, "Unable to load Interactions from InteractionManifest.");
		}
		return null;
	}

	public Targets getTargets() {
		try {
			if (!isNull(Targets.KEY_NAME)) {
				Object targets = get(Targets.KEY_NAME);
				return new Targets(targets.toString());
			}
		} catch (JSONException e) {
			ApptentiveLog.w(INTERACTIONS, e, "Unable to load Targets from InteractionManifest.");
		}
		return null;
	}
}
