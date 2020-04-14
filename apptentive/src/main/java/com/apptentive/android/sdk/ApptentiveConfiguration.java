/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.module.engagement.interaction.model.TermsAndConditions;
import com.apptentive.android.sdk.util.StringUtils;

public class ApptentiveConfiguration {
	private final String apptentiveKey;
	private final String apptentiveSignature;
	private String baseURL;
	private ApptentiveLog.Level logLevel;
	private boolean shouldEncryptStorage;
	private boolean shouldSanitizeLogMessages;
	private boolean troubleshootingModeEnabled;
	private Encryption encryption;
	private boolean shouldCollectAndroidIdOnPreOreoTargets;
	private TermsAndConditions surveyTermsAndConditions;

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
		this.shouldEncryptStorage = false;
		this.shouldSanitizeLogMessages = true;
		this.troubleshootingModeEnabled = true;
		this.shouldCollectAndroidIdOnPreOreoTargets = true;
		this.surveyTermsAndConditions = null;
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
	 * Enables/disables encrypted on-device storage. Disabled by default.
	 */
	public void setShouldEncryptStorage(boolean shouldEncryptStorage) {
		this.shouldEncryptStorage = shouldEncryptStorage;
	}

	/**
	 * Returns <code>true</code> if SDK should use encrypted on-device storage.
	 */
	public boolean shouldEncryptStorage() {
		return shouldEncryptStorage;
	}

	/**
	 * Returns <code>true</code> if SDK should hide user sensitive information (user name, email,
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

	/**
	 * Overrides custom encryption object.
	 */
	public void setEncryption(@Nullable Encryption encryption) {
		this.encryption = encryption;
	}

	/**
	 * Returns custom encryption object (if any)
	 */
	@Nullable Encryption getEncryption() {
		return encryption;
	}

	/**
	 * Indicates if the SDK troubleshooting mode should be enabled (<code>true</code> by default).
	 */
	public boolean isTroubleshootingModeEnabled() {
		return troubleshootingModeEnabled;
	}

	/**
	 * Overrides the SDK troubleshooting mode.
	 */
	public ApptentiveConfiguration setTroubleshootingModeEnabled(boolean troubleshootingModeEnabled) {
		this.troubleshootingModeEnabled = troubleshootingModeEnabled;
		return this;
	}

	/**
	 * Overrides if the SDK should collect Android ID on pre Android-O targets. If set to <code>false</code>
	 * a random value would be generated on the initial SDK launch and provided on each subsequent launch.
	 */
	public void setShouldCollectAndroidIdOnPreOreoTargets(boolean shouldCollectAndroidIdOnPreOreoTargets) {
		this.shouldCollectAndroidIdOnPreOreoTargets = shouldCollectAndroidIdOnPreOreoTargets;
	}

	/**
	 * Indicates if the SDK should collect Android ID on pre Android-O targets.
	 */
	public boolean shouldCollectAndroidIdOnPreOreoTargets() {
		return shouldCollectAndroidIdOnPreOreoTargets;
	}

	public TermsAndConditions getSurveyTermsAndConditions() {
		return surveyTermsAndConditions;
	}

	public void setSurveyTermsAndConditions(TermsAndConditions surveyTermsAndConditions) {
		this.surveyTermsAndConditions = surveyTermsAndConditions;
	}
}
