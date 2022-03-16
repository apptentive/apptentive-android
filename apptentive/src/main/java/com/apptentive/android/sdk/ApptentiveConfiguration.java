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

import java.util.concurrent.TimeUnit;

public class ApptentiveConfiguration {
	private final String apptentiveKey;
	private final String apptentiveSignature;
	private String baseURL;
	private ApptentiveLog.Level logLevel;
	private boolean shouldEncryptStorage;
	private boolean shouldSanitizeLogMessages;
	private boolean troubleshootingModeEnabled;
	private Encryption encryption;
	private TermsAndConditions surveyTermsAndConditions;
	private Long interactionThrottle;

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
	 * AndroidID is no longer collected
	 *
	 * @since Apptentive Android SDK version 5.8.3
	 */
	@Deprecated
	public void setShouldCollectAndroidIdOnPreOreoTargets(boolean shouldCollectAndroidIdOnPreOreoTargets) {
	}

	/**
	 * AndroidID is no longer collected
	 *
	 * @since Apptentive Android SDK version 5.8.3
	 */
	@Deprecated
	public boolean shouldCollectAndroidIdOnPreOreoTargets() {
		return false;
	}

	public TermsAndConditions getSurveyTermsAndConditions() {
		return surveyTermsAndConditions;
	}

	public void setSurveyTermsAndConditions(TermsAndConditions surveyTermsAndConditions) {
		this.surveyTermsAndConditions = surveyTermsAndConditions;
	}

	public Long getInteractionThrottle() {
		return interactionThrottle != null ? interactionThrottle : TimeUnit.DAYS.toMillis(7);
	}

	/**
	 * Sets a time limit throttle which determines when a rating interaction can be shown again.
	 * Default is 7 days.
	 *
	 * @see TimeUnit for conversion utils
	 * e.g. TimeUnit.MINUTES.toMillis(10); or TimeUnit.DAYS.toMillis(30);
	 *
	 * @param interactionThrottle The length of time (in milliseconds) to wait before showing
	 *                            the same interaction again.
	 */
	public void setRatingInteractionThrottle(Long interactionThrottle) {
		this.interactionThrottle = interactionThrottle;
	}
}
