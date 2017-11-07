/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.logic.Clause;
import com.apptentive.android.sdk.module.engagement.logic.ClauseParser;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;

import org.json.JSONException;

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
			ApptentiveLog.i("Evaluating Criteria");
			boolean ret = false;
			if (rootClause != null) {
				ret = rootClause.evaluate(fieldManager);
			}
			ApptentiveLog.i("- => %b", ret);
			return ret;
		} catch (JSONException e) {
			ApptentiveLog.w(e, "Error parsing and running InteractionCriteria predicate logic.");
		} catch (Exception e) {
			ApptentiveLog.w(e, "Error parsing and running InteractionCriteria predicate logic.");
		}
		return false;
	}
}
