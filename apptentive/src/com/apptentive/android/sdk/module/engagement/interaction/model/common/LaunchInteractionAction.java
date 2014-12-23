/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.common;

import com.apptentive.android.sdk.module.engagement.interaction.model.Invocation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class LaunchInteractionAction extends Action {

	private static final String KEY_INVOKES = "invokes";

	public LaunchInteractionAction(String json) throws JSONException {
		super(json);
	}

	public List<Invocation> getInvocations() {
		List<Invocation> invocations = new ArrayList<Invocation>();
		try {
			if (!isNull(KEY_INVOKES)) {
				JSONArray invocationsArray= getJSONArray(KEY_INVOKES);
				if (invocationsArray != null) {
					for (int i = 0; i < invocationsArray.length(); i++) {
						JSONObject invocationObject = invocationsArray.getJSONObject(i);
						Invocation invocation = new Invocation(invocationObject.toString());
						invocations.add(invocation);
					}
				}
			}
		} catch (JSONException e) {
			// Ignore
		}
		return invocations;
	}


}
