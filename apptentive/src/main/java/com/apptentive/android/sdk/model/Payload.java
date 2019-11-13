/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;

import java.util.List;

public abstract class Payload {
	private final PayloadType payloadType;

	/**
	 * Encryption key for encrypting payload.
	 */
	private @NonNull Encryption encryption;

	/**
	 * The Conversation ID of the payload, if known at this time.
	 */
	private String conversationId;

	/**
	 * Encrypted Payloads need to include the Conversation JWT inside them so that the server can
	 * authenticate each payload after it is decrypted.
	 */
	private String token;

	private String localConversationIdentifier;

	private List<Object> attachments; // TODO: Figure out attachment handling

	/**
	 * <code>true</code> if payload belongs to an authenticated (logged-in) conversation
	 */
	private boolean authenticated;

	/**
	 * Session id which this payload belongs to
	 */
	private @Nullable String sessionId;

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
	public abstract @NonNull byte[] renderData() throws Exception;

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

	@NonNull Encryption getEncryption() {
		return encryption;
	}

	public void setEncryption(@NonNull Encryption encryption) {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		this.encryption = encryption;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public @Nullable String getConversationToken() {
		return token;
	}

	public void setToken(@Nullable String token) {
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

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public boolean hasSessionId() {
		return !StringUtils.isNullOrEmpty(sessionId);
	}

	//endregion
}
