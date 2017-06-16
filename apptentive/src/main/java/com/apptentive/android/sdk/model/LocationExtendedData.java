/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationExtendedData extends ExtendedData {

	private static final String KEY_COORDINATES = "coordinates";

	private double longitude;
	private double latitude;

	private static final int VERSION = 1;

	@Override
	protected void init() {
		setType(Type.location);
		setVersion(VERSION);
	}

	public LocationExtendedData(double longitude, double latitude) {
		super();
		setCoordinates(longitude, latitude);
	}

	public LocationExtendedData(String json) throws JSONException {
		super(json);
		JSONObject jsonObject = new JSONObject(json);
		JSONArray coords = jsonObject.optJSONArray(KEY_COORDINATES);
		if (coords != null) {
			setCoordinates(coords.optDouble(0, 0), coords.optDouble(1, 0));
		}
	}

	public void setCoordinates(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	@Override
	public JSONObject toJsonObject() throws JSONException {
		JSONObject ret = super.toJsonObject();
		JSONArray coordinates = new JSONArray();
		ret.put(KEY_COORDINATES, coordinates);
		coordinates.put(longitude);
		coordinates.put(latitude);
		return ret;
	}
}
