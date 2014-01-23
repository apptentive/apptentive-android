/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.content.Context;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteraction extends Interaction {

	public RatingDialogInteraction(String json) throws JSONException {
		super(json);
	}

	@Override
	public boolean isInRunnableState(Context context) {
		return Util.isNetworkConnectionPresent(context);
	}
}
