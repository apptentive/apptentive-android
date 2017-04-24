/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

public class PayloadData {
	private final PayloadType type;
	private String contentType;
	private String authToken;
	private byte[] data;
	private String path;
	private HttpRequestMethod httpRequestMethod;
	private String nonce;

	public PayloadData(PayloadType type) {
		this.type = type;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setHttpRequestPath(String path) {
		this.path = path;
	}

	public String getHttpEndPoint() {
		return path;
	}

	public HttpRequestMethod getHttpRequestMethod() {
		return httpRequestMethod;
	}

	public PayloadType getType() {
		return type;
	}

	public void setHttpRequestMethod(HttpRequestMethod httpRequestMethod) {
		this.httpRequestMethod = httpRequestMethod;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
