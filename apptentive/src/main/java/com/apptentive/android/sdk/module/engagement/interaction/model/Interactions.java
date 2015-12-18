/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A map of "interaction_id" => {Interaction}
 * @author Sky Kelsey
 */
public class Interactions extends JSONObject {
	public static final String KEY_NAME = "interactions";

	public Interactions() throws JSONException {
		super();
	}

	public Interactions(String json) throws JSONException {
		super(json);
	}

	public Interaction getInteraction(String id) {
		try {
			if (!isNull(id)) {
				return Interaction.Factory.parseInteraction(getJSONObject(id).toString());
			}
		} catch (JSONException e) {
			Log.w("Exception parsing interactions array.", e);
		}
		return null;
	}

	public List<Interaction> getInteractionList() {
		List<Interaction> ret = new ArrayList<Interaction>();
		Iterator<String> keys = (Iterator<String>) keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject interactionObject = optJSONObject(key);
			if (interactionObject != null) {
				Interaction interaction = Interaction.Factory.parseInteraction(interactionObject.toString());
				if (interaction != null) {
					ret.add(interaction);
				}
			}
		}
		return ret;
	}

	/**
	 * Returns true if the data that was loaded into this object is from an old style interactions payload.
	 */
	private boolean isLegacyInteractions() {
		return !isNull(KEY_NAME);
	}
}
