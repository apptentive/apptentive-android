/*
 * JSONPayload.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONPayload {

	protected JSONObject root = new JSONObject();

	public String getAsJSON(){
		return root.toString();
	}

	public void addString(String value, String... keys) throws JSONException {
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
