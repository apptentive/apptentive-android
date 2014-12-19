/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.common;

import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class Actions extends JSONArray {
	public Actions(String json) throws JSONException {
		super(json);
	}

	public List<Action> getAsList() {
		List<Action> ret = new ArrayList<Action>();
		try {
			for (int i = 0; i < length(); i++) {
				Action button = Action.Factory.parseAction(getJSONObject(i).toString());
				if (button != null) {
					ret.add(button);
				}
			}
		} catch (JSONException e) {
			Log.w("Exception parsing interactions array.", e);
		}
		return ret;
	}
}
