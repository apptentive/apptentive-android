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
	private static final String KEY_AUTHOR_NAME = "author_name";
	private static final String KEY_AUTHOR_EMAIL = "author_email";
	private static final String KEY_PLATFORM = "platform";
	private static final String KEY_DISTRIBUTION = "distribution";
	private static final String KEY_DISTRIBUTION_VERSION = "distribution_version";

	public SdkPayload() {
	}

	public SdkPayload(String json) throws JSONException {
		super(json);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // FIXME: find a better approach
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // FIXME: find a better approach
	}

	@Override
	public String getHttpRequestContentType() {
		throw new RuntimeException(getClass().getName() + " is deprecated"); // FIXME: find a better approach
	}

	//endregion

	public String getVersion() {
		if (!isNull(KEY_VERSION)) {
			return getString(KEY_VERSION);
		}
		return null;
	}

	public void setVersion(String version) {
		put(KEY_VERSION, version);
	}

	public String getProgrammingLanguage() {
		return getString(KEY_PROGRAMMING_LANGUAGE);
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		put(KEY_PROGRAMMING_LANGUAGE, programmingLanguage);
	}

	public String getAuthorName() {
		if (!isNull(KEY_AUTHOR_NAME)) {
			return getString(KEY_AUTHOR_NAME);
		}
		return null;
	}

	public void setAuthorName(String authorName) {
		put(KEY_AUTHOR_NAME, authorName);
	}

	public String getAuthorEmail() {
		if (!isNull(KEY_AUTHOR_EMAIL)) {
			return getString(KEY_AUTHOR_EMAIL);
		}
		return null;
	}

	public void setAuthorEmail(String authorEmail) {
		put(KEY_AUTHOR_EMAIL, authorEmail);
	}

	public String getPlatform() {
		if (!isNull(KEY_PLATFORM)) {
			return getString(KEY_PLATFORM);
		}
		return null;
	}

	public void setPlatform(String platform) {
		put(KEY_PLATFORM, platform);
	}

	public String getDistribution() {
		if (!isNull(KEY_DISTRIBUTION)) {
			return getString(KEY_DISTRIBUTION);
		}
		return null;
	}

	public void setDistribution(String distribution) {
		put(KEY_DISTRIBUTION, distribution);
	}

	public String getDistributionVersion() {
		if (!isNull(KEY_DISTRIBUTION_VERSION)) {
			return getString(KEY_DISTRIBUTION_VERSION);
		}
		return null;
	}

	public void setDistributionVersion(String distributionVersion) {
		put(KEY_DISTRIBUTION_VERSION, distributionVersion);
	}
}
