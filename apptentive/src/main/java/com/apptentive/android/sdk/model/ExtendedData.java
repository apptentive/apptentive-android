/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public abstract class ExtendedData implements Serializable {
	private static final long serialVersionUID = 4070116080651157128L;
	private static final String KEY_VERSION = "version";

	private Type type = Type.unknown;

	private int version;

	protected ExtendedData() {
		init();
	}

	protected ExtendedData(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		setVersion(jsonObject.optInt(KEY_VERSION, -1));
		init();
	}

	public String getTypeName() {
		return type.name();
	}

	protected void setType(Type type) {
		this.type = type;
	}

	protected void setVersion(int version) {
		this.version = version;
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put(KEY_VERSION, version);
		return ret;
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
				ApptentiveLog.v(PAYLOADS, "Error parsing unknown ExtendedData.PayloadType: " + type);
				logException(e);
			}
			return unknown;
		}
	}
}
