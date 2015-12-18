/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class LocationExtendedData extends ExtendedData {

	private static final String KEY_COORDINATES = "coordinates";

	private static final int VERSION = 1;

	@Override
	protected void init() {
		setType(Type.location);
		setVersion(VERSION);
	}

	public LocationExtendedData(String json) throws JSONException {
		super(json);
	}

	public LocationExtendedData(double longitude, double latitude) {
		super();
		setCoordinates(longitude, latitude);
	}

	public void setCoordinates(double longitude, double latitude) {
		try {
			JSONArray coordinates = new JSONArray();
			put(KEY_COORDINATES, coordinates);
			coordinates.put(0, longitude);
			coordinates.put(1, latitude);
		} catch (JSONException e) {
			Log.w("Error adding %s to LocationExtendedData.", KEY_COORDINATES, e);
		}
	}
}
