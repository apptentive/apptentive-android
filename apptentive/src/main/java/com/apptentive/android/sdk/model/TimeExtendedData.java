/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TimeExtendedData extends ExtendedData {

	private static final String KEY_TIMESTAMP = "timestamp";

	private static final int VERSION = 1;

	private double timestamp;

	@Override
	protected void init() {
		setType(Type.time);
		setVersion(VERSION);
	}

	public TimeExtendedData() {
		super();
		setTimestamp(System.currentTimeMillis());
	}

	public TimeExtendedData(String json) throws JSONException {
		super(json);
		JSONObject jsonObject = new JSONObject(json);
		setTimestamp(jsonObject.optDouble(KEY_TIMESTAMP));
	}

	public TimeExtendedData(Date date) {
		super();
		setTimestamp(date);
	}

	public TimeExtendedData(long millis) {
		super();
		setTimestamp(millis);
	}

	public TimeExtendedData(double seconds) {
		super();
		setTimestamp(seconds);
	}

	protected void setTimestamp(Date date) {
		if (date != null) {
			setTimestamp(date.getTime());
		} else {
			setTimestamp(System.currentTimeMillis());
		}
	}

	protected void setTimestamp(long millis) {
		setTimestamp(((double) millis) / 1000);
	}

	protected void setTimestamp(double dateInSeconds) {
		this.timestamp = dateInSeconds;
	}

	@Override
	public JSONObject toJsonObject() throws JSONException {
		JSONObject ret = super.toJsonObject();
		ret.put(KEY_TIMESTAMP, timestamp);
		return ret;
	}
}
