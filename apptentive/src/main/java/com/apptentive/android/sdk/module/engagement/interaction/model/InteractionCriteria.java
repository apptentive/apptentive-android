/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;

import org.json.JSONException;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;

/**
 * @author Sky Kelsey
 */
public class InteractionCriteria {

	private String json;

	public InteractionCriteria(String json) throws JSONException {
		this.json = json;
	}

	public boolean isMet(FieldManager fieldManager) {
		try {
			Clause rootClause = ClauseParser.parse(json);
			ApptentiveLog.i(INTERACTIONS, "Evaluating Criteria");
			boolean ret = false;
			if (rootClause != null) {
				ret = rootClause.evaluate(fieldManager);
			}
			ApptentiveLog.i(INTERACTIONS, "- => %b", ret);
			return ret;
		} catch (Exception e) {
			ApptentiveLog.e(INTERACTIONS, e, "Exception while evaluating interaction criteria predicate logic.");
		}
		return false;
	}
}
