/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;

import org.json.JSONException;

public class SdkPayload extends JsonPayload {

	public static final String KEY = "sdk";

	private static final String KEY_VERSION = "version";
	private static final String KEY_PROGRAMMING_LANGUAGE = "programming_language";
	@SensitiveDataKey private static final String KEY_AUTHOR_NAME = "author_name";
	@SensitiveDataKey private static final String KEY_AUTHOR_EMAIL = "author_email";
	private static final String KEY_PLATFORM = "platform";
	private static final String KEY_DISTRIBUTION = "distribution";
	private static final String KEY_DISTRIBUTION_VERSION = "distribution_version";

	static {
		registerSensitiveKeys(SdkPayload.class);
	}

	public SdkPayload() {
		super(PayloadType.sdk);
	}

	public SdkPayload(String json) throws JSONException {
		super(PayloadType.sdk, json);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // TODO: find a better approach
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // TODO: find a better approach
	}

	@Override
	public String getHttpRequestContentType() {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // TODO: find a better approach
	}

	//endregion

	public String getVersion() {
		return optString(KEY_VERSION, null);
	}

	public void setVersion(String version) {
		put(KEY_VERSION, version);
	}

	public String getProgrammingLanguage() {
		return optString(KEY_PROGRAMMING_LANGUAGE, null);
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		put(KEY_PROGRAMMING_LANGUAGE, programmingLanguage);
	}

	public String getAuthorName() {
		return optString(KEY_AUTHOR_NAME, null);
	}

	public void setAuthorName(String authorName) {
		put(KEY_AUTHOR_NAME, authorName);
	}

	public String getAuthorEmail() {
		return optString(KEY_AUTHOR_EMAIL, null);
	}

	public void setAuthorEmail(String authorEmail) {
		put(KEY_AUTHOR_EMAIL, authorEmail);
	}

	public String getPlatform() {
		return optString(KEY_PLATFORM, null);
	}

	public void setPlatform(String platform) {
		put(KEY_PLATFORM, platform);
	}

	public String getDistribution() {
		return optString(KEY_DISTRIBUTION, null);
	}

	public void setDistribution(String distribution) {
		put(KEY_DISTRIBUTION, distribution);
	}

	public String getDistributionVersion() {
		return optString(KEY_DISTRIBUTION_VERSION, null);
	}

	public void setDistributionVersion(String distributionVersion) {
		put(KEY_DISTRIBUTION_VERSION, distributionVersion);
	}
}
