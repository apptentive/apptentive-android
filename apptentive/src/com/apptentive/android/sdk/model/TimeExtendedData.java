/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONException;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public class TimeExtendedData extends ExtendedData {

	private static final String KEY_TIMESTAMP = "timestamp";

	private static final int VERSION = 1;

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
		try {
			put(KEY_TIMESTAMP, dateInSeconds);
		} catch (JSONException e) {
			Log.w("Error adding %s to TimeExtendedData.", KEY_TIMESTAMP, e);
		}
	}
}
