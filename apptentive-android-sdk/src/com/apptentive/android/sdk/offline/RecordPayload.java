/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import org.json.JSONException;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public class RecordPayload extends Payload {
	long date;

	public RecordPayload() {
		date = new Date().getTime();
	}

	public RecordPayload(String json) throws JSONException {
		super(json);
	}

	public PayloadType getPayloadType() {
		return PayloadType.RECORD;
	}
}
