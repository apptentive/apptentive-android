/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

import java.io.IOException;

public class RawHttpRequest extends HttpRequest {

	private final byte[] data;

	public RawHttpRequest(String urlString, byte[] data) {
		super(urlString);

		if (data == null) {
			throw new IllegalArgumentException("data is null");
		}
		this.data = data;
	}

	@Override
	protected byte[] createRequestData() throws IOException {
		return data;
	}
}
