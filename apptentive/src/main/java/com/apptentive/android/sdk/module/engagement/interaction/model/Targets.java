/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * @author Sky Kelsey
 */
public class Targets extends JSONObject {

	public static final String KEY_NAME = "targets";

	public Targets(String json) throws JSONException {
		super(json);
	}

	public String getApplicableInteraction(String eventLabel, boolean verbose) {
		JSONArray invocations = optJSONArray(eventLabel);
		if (invocations != null) {
			for (int i = 0; i < invocations.length(); i++) {
				JSONObject invocationObject = invocations.optJSONObject(i);
				if (invocationObject != null) {
					try {
						Invocation invocation = new Invocation(invocationObject.toString());
						Conversation conversation = ApptentiveInternal.getInstance().getConversation();
						FieldManager fieldManager = new FieldManager(ApptentiveInternal.getInstance().getApplicationContext(), conversation.getVersionHistory(), conversation.getEventData(), conversation.getPerson(), conversation.getDevice(), conversation.getAppRelease());

						if (invocation.isCriteriaMet(fieldManager, verbose)) {
							return invocation.getInteractionId();
						}
					} catch (JSONException e) {
						logException(e);
					}
				}
			}
		}
		ApptentiveLog.v(INTERACTIONS, "No runnable Interactions for EventLabel: %s", eventLabel);
		return null;
	}
}
