/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public abstract class Payload {

	public Payload() {
		try {
			setString(Util.dateToString(new Date()), "record", "date");
		} catch (JSONException e) {
			Log.e("Exception setting date.", e);
		}
	}

	protected JSONObject root = new JSONObject();

	public String getAsJSON(){
		return root.toString();
	}

	public void setString(String value, String... keys) throws JSONException {
		JSONObject parent = root;
		for(int i = 0; i < keys.length; i++){
			String key = keys[i];
			if(i == keys.length - 1){ // Last key, must be a String
				parent.put(key, value);
			}else{ // Must be an Object
				if(parent.has(key)){
					parent = parent.getJSONObject(key);
				}else{
					JSONObject current = new JSONObject();
					parent.put(key, current);
					parent = current;
				}
			}
		}
	}
}
