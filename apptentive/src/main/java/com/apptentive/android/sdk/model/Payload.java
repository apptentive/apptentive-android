/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

import java.util.List;
import java.util.UUID;

public abstract class Payload {
	private final PayloadType payloadType;

	/**
	 * If set, this payload should be encrypted in getData().
	 */
	protected String encryptionKey;

	/**
	 * Encrypted Payloads need to include the Conversation JWT inside them so that the server can
	 * authenticate each payload after it is decrypted.
	 */
	protected String token;

	/**
	 * A value that can be used to correlate a payload with another object
	 * (for example, to update the sent status of a message)
	 */
	private String nonce;

	private List<Object> attachments; // TODO: Figure out attachment handling

	protected Payload(PayloadType type) {
		if (type == null) {
			throw new IllegalArgumentException("Payload type is null");
		}

		this.payloadType = type;
		nonce = UUID.randomUUID().toString();
	}

	//region Data

	/**
	 * Binary data to be stored in database
	 */
	public abstract byte[] getData();

	//region

	//region Http-request

	/**
	 * Http endpoint for sending this payload
	 */
	public abstract String getHttpEndPoint(String conversationId);

	/**
	 * Http request method for sending this payload
	 */
	public abstract HttpRequestMethod getHttpRequestMethod();

	/**
	 * Http content type for sending this payload
	 */
	public abstract String getHttpRequestContentType();

	//endregion

	//region Getters/Setters

	public PayloadType getPayloadType() {
		return payloadType;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public List<Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Object> attachments) {
		this.attachments = attachments;
	}

	//endregion
}
