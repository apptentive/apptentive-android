/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public abstract class ExtendedData extends JSONObject {

	private static final String KEY_VERSION = "version";

	private Type type = Type.unknown;

	protected ExtendedData() {
		init();
	}

	protected ExtendedData(String json) throws JSONException {
		super(json);
		init();
	}

	public String getTypeName() {
		return type.name();
	}

	protected void setType(Type type) {
		this.type = type;
	}

	protected void setVersion(int version) {
		try {
			put(KEY_VERSION, version);
		} catch (JSONException e) {
			Log.w("Error adding %s to ExtendedData.", KEY_VERSION, e);
		}
		return;
	}

	protected abstract void init();

	public enum Type {
		time,
		location,
		commerce,
		unknown;

		public static Type parse(String type) {
			try {
				return Type.valueOf(type);
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown ExtendedData.BaseType: " + type);
			}
			return unknown;
		}
	}
}
