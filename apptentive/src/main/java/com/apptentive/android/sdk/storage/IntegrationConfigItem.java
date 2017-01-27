/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Set;

public class IntegrationConfigItem extends HashMap<String, Object> {

	public com.apptentive.android.sdk.model.CustomData toJson() {
		try {
			com.apptentive.android.sdk.model.CustomData ret = new com.apptentive.android.sdk.model.CustomData();
			Set<String> keys = keySet();
			for (String key : keys) {
				ret.put(key, get(key));
			}
		} catch (JSONException e) {
			// This can't happen.
		}
		return null;
	}
}
