/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

public class OutgoingPayload extends Payload {

	private byte[] data;

	public OutgoingPayload(PayloadType payloadType) {
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String getHttpEndPoint() {
		return null;
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return null;
	}

	@Override
	public String getHttpRequestContentType() {
		return null;
	}
}
