/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.support.annotation.NonNull;

import com.apptentive.android.sdk.util.StringUtils;

public class ApptentiveConfiguration {
	private final String apptentiveKey;
	private final String apptentiveSignature;
	private String baseURL;
	private ApptentiveLog.Level logLevel;
	private boolean shouldSanitizeLogMessages;

	public ApptentiveConfiguration(@NonNull String apptentiveKey, @NonNull String apptentiveSignature) {
		if (StringUtils.isNullOrEmpty(apptentiveKey)) {
			throw new IllegalArgumentException("Apptentive key is null or empty");
		}

		if (StringUtils.isNullOrEmpty(apptentiveSignature)) {
			throw new IllegalArgumentException("Apptentive signature is null or empty");
		}

		this.apptentiveKey = apptentiveKey.trim();
		this.apptentiveSignature = apptentiveSignature.trim();
		this.logLevel = ApptentiveLog.Level.INFO;
		this.shouldSanitizeLogMessages = true;
	}

	public String getApptentiveKey() {
		return apptentiveKey;
	}

	public String getApptentiveSignature() {
		return apptentiveSignature;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public ApptentiveConfiguration setBaseURL(String baseURL) {
		this.baseURL = baseURL;
		return this;
	}

	public ApptentiveLog.Level getLogLevel() {
		return logLevel;
	}

	public ApptentiveConfiguration setLogLevel(ApptentiveLog.Level logLevel) {
		this.logLevel = logLevel;
		return this;
	}

	/**
	 * Returns <code>true</code> is SDK should hide user sensitive information (user name, email,
	 * custom data, etc). Useful for debugging.
	 */
	public boolean shouldSanitizeLogMessages() {
		return shouldSanitizeLogMessages;
	}

	/**
	 * Overrides if SDK should hide sensitive information logging (user name, email,
	 * custom data, etc). Useful for debugging.
	 */
	public void setShouldSanitizeLogMessages(boolean shouldSanitizeLogMessages) {
		this.shouldSanitizeLogMessages = shouldSanitizeLogMessages;
	}
}
