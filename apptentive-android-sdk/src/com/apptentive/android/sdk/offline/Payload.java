/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * @author Sky Kelsey
 */
public class Payload extends JSONObject {

	private long id;
	private String payloadId;

	public static enum PayloadType {
		RECORD,
		MESSAGE
	}

	public Payload() {
		payloadId = UUID.randomUUID().toString();
	}

	public Payload(String json) throws JSONException {
		super(json);
	}

	/**
	 * Guaranteed to be there if retreived from DB.
	 * @return
	 */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * This is the database ID. Not stored in JSON.
	 * @return The database ID, or null.
	 */
	public String getPayloadId() {
		return payloadId;
	}

	public void setPayloadId(String payloadId) {
		this.payloadId = payloadId;
	}

	/**
	 * Override this method.
	 * @return
	 */
	public PayloadType getPayloadType() {
		return null;
	}
}

