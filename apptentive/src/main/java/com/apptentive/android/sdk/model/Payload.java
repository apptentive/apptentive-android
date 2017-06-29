/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.List;

public abstract class Payload {
	private final PayloadType payloadType;

	/**
	 * If set, this payload should be encrypted in renderData().
	 */
	protected String encryptionKey;

	/**
	 * The Conversation ID of the payload, if known at this time.
	 */
	protected String conversationId;

	/**
	 * Encrypted Payloads need to include the Conversation JWT inside them so that the server can
	 * authenticate each payload after it is decrypted.
	 */
	protected String token;

	private String localConversationIdentifier;

	private List<Object> attachments; // TODO: Figure out attachment handling

	protected Payload(PayloadType type) {
		if (type == null) {
			throw new IllegalArgumentException("Payload type is null");
		}

		this.payloadType = type;
	}

	//region Data

	/**
	 * Binary data to be stored in database
	 */
	public abstract byte[] renderData();

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

	public boolean hasEncryptionKey() {
		return !StringUtils.isNullOrEmpty(encryptionKey);
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public abstract String getNonce();

	public abstract void setNonce(String nonce);

	public List<Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Object> attachments) {
		this.attachments = attachments;
	}

	public String getLocalConversationIdentifier() {
		return localConversationIdentifier;
	}

	public void setLocalConversationIdentifier(String localConversationIdentifier) {
		this.localConversationIdentifier = localConversationIdentifier;
	}

	//endregion
}
