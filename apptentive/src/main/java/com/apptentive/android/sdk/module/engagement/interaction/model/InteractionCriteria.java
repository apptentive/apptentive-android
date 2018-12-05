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
import com.apptentive.android.sdk.util.IndentBufferedPrinter;
import com.apptentive.android.sdk.util.IndentPrinter;

import org.json.JSONException;

import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

/**
 * @author Sky Kelsey
 */
public class InteractionCriteria {
	private String json;

	public InteractionCriteria(String json) throws JSONException {
		this.json = json;
	}

	public boolean isMet(FieldManager fieldManager) {
		return isMet(fieldManager, true);
	}

	public boolean isMet(FieldManager fieldManager, boolean verbose) {
		try {
			Clause rootClause = ClauseParser.parse(json);
			boolean ret = false;
			if (rootClause != null) {
				IndentPrinter printer = verbose ? new IndentBufferedPrinter() : IndentPrinter.NULL;
				ret = rootClause.evaluate(fieldManager, printer);
				if (verbose) {
					ApptentiveLog.i(INTERACTIONS, "Criteria evaluated => %b", ret);
					ApptentiveLog.d(INTERACTIONS, "Criteria evaluation details:\n%s", printer);
				}
			} else {
				if (verbose) {
					ApptentiveLog.i(INTERACTIONS, "Criteria could not be evaluated: no clause found");
				}
			}
			return ret;
		} catch (Exception e) {
			ApptentiveLog.e(INTERACTIONS, e, "Exception while evaluating interaction criteria");
			logException(e);
		}
		return false;
	}
}
