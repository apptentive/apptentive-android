/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;

public class PayloadData {
	private final PayloadType type;
	private final String nonce;
	private final String conversationId;
	private final byte[] data;
	private final String authToken;
	private final String contentType;
	private final String httpRequestPath;
	private final HttpRequestMethod httpRequestMethod;
	private final boolean authenticated;


	public PayloadData(PayloadType type, String nonce, String conversationId, byte[] data, String authToken, String contentType, String httpRequestPath, HttpRequestMethod httpRequestMethod, boolean authenticated) {
		if (type == null) {
			throw new IllegalArgumentException("Payload type is null");
		}

		if (nonce == null) {
			throw new IllegalArgumentException("Nonce is null");
		}

		if (conversationId == null) {
			throw new IllegalArgumentException("Conversation ID is null");
		}

		if (data == null) {
			throw new IllegalArgumentException("Data is null");
		}

		if (contentType == null) {
			throw new IllegalArgumentException("Content type is null");
		}

		if (httpRequestPath == null) {
			throw new IllegalArgumentException("Path is null");
		}

		if (httpRequestMethod == null) {
			throw new IllegalArgumentException("Http request method is null");
		}

		this.type = type;
		this.nonce = nonce;
		this.conversationId = conversationId;
		this.data = data;
		this.authToken = authToken;
		this.contentType = contentType;
		this.httpRequestPath = httpRequestPath;
		this.httpRequestMethod = httpRequestMethod;
		this.authenticated = authenticated;
	}

	//region String representation

	@Override
	public String toString() {
		return StringUtils.format("type=%s nonce=%s conversationId=%s authToken=%s httpRequestPath=%s", type, nonce, conversationId, hideIfSanitized(authToken), httpRequestPath);
	}

	//endregion

	//region Getters

	public PayloadType getType() {
		return type;
	}

	public String getNonce() {
		return nonce;
	}

	public String getConversationId() {
		return conversationId;
	}

	public byte[] getData() {
		return data;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getContentType() {
		return contentType;
	}

	public String getHttpRequestPath() {
		return httpRequestPath;
	}

	public HttpRequestMethod getHttpRequestMethod() {
		return httpRequestMethod;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	//endregion
}
