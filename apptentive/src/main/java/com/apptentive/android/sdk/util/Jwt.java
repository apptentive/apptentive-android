/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Utility class for Jwt handling
 */

public class Jwt {
	private final String alg;
	private final String type;
	private final JSONObject payload;

	public Jwt(String alg, String type, JSONObject payload) {
		if (alg == null) {
			throw new IllegalArgumentException("Alg is null");
		}
		if (type == null) {
			throw new IllegalArgumentException("Type is null");
		}
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}
		this.alg = alg;
		this.type = type;
		this.payload = payload;
	}

	public static Jwt decode(String data) {
		if (data == null) {
			throw new IllegalArgumentException("Data string is null");
		}

		final String[] tokens = data.split("\\.");
		if (tokens.length != 3) {
			throw new IllegalArgumentException("Invalid JWT data format: '" + data + "'");
		}

		final JSONObject headerJson = decodeBase64Json(tokens[0]);
		final String alg = headerJson.optString("alg", null);
		final String type = headerJson.optString("typ", null);
		if (alg == null || type == null) {
			throw new IllegalArgumentException("Invalid jwt header: '" + headerJson + "'");
		}

		final JSONObject payloadJson = decodeBase64Json(tokens[1]);
		return new Jwt(alg, type, payloadJson);
	}

	private static JSONObject decodeBase64Json(String data) {
		try {
			final String text = new String(Base64.decode(data, Base64.DEFAULT), "UTF-8");
			return new JSONObject(text);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String getAlg() {
		return alg;
	}

	public String getType() {
		return type;
	}

	public JSONObject getPayload() {
		return payload;
	}
}
