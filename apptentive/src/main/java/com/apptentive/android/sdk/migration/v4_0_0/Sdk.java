/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration.v4_0_0;

import com.apptentive.android.sdk.ApptentiveLog;
import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class Sdk extends JSONObject {

	private static final String KEY_VERSION = "version";
	private static final String KEY_PROGRAMMING_LANGUAGE = "programming_language";
	private static final String KEY_AUTHOR_NAME = "author_name";
	private static final String KEY_AUTHOR_EMAIL = "author_email";
	private static final String KEY_PLATFORM = "platform";
	private static final String KEY_DISTRIBUTION = "distribution";
	private static final String KEY_DISTRIBUTION_VERSION = "distribution_version";

	public Sdk(String json) throws JSONException {
		super(json);
	}

	public String getVersion() {
		try {
			if(!isNull(KEY_VERSION)) {
				return getString(KEY_VERSION);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setVersion(String version) {
		try {
			put(KEY_VERSION, version);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_VERSION);
			logException(e);
		}
	}

	public String getProgrammingLanguage() {
		try {
			if(!isNull(KEY_PROGRAMMING_LANGUAGE)) {
				return getString(KEY_PROGRAMMING_LANGUAGE);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setProgrammingLanguage(String programmingLanguage) {
		try {
			put(KEY_PROGRAMMING_LANGUAGE, programmingLanguage);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_PROGRAMMING_LANGUAGE);
			logException(e);
		}
	}

	public String getAuthorName() {
		try {
			if(!isNull(KEY_AUTHOR_NAME)) {
				return getString(KEY_AUTHOR_NAME);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setAuthorName(String authorName) {
		try {
			put(KEY_AUTHOR_NAME, authorName);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_AUTHOR_NAME);
			logException(e);
		}
	}

	public String getAuthorEmail() {
		try {
			if(!isNull(KEY_AUTHOR_EMAIL)) {
				return getString(KEY_AUTHOR_EMAIL);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setAuthorEmail(String authorEmail) {
		try {
			put(KEY_AUTHOR_EMAIL, authorEmail);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_AUTHOR_EMAIL);
			logException(e);
		}
	}

	public String getPlatform() {
		try {
			if(!isNull(KEY_PLATFORM)) {
				return getString(KEY_PLATFORM);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setPlatform(String platform) {
		try {
			put(KEY_PLATFORM, platform);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_PLATFORM);
			logException(e);
		}
	}

	public String getDistribution() {
		try {
			if(!isNull(KEY_DISTRIBUTION)) {
				return getString(KEY_DISTRIBUTION);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setDistribution(String distribution) {
		try {
			put(KEY_DISTRIBUTION, distribution);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_DISTRIBUTION);
			logException(e);
		}
	}

	public String getDistributionVersion() {
		try {
			if(!isNull(KEY_DISTRIBUTION_VERSION)) {
				return getString(KEY_DISTRIBUTION_VERSION);
			}
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	public void setDistributionVersion(String distributionVersion) {
		try {
			put(KEY_DISTRIBUTION_VERSION, distributionVersion);
		} catch (JSONException e) {
			ApptentiveLog.w(CONVERSATION, "Error adding %s to Sdk.", KEY_DISTRIBUTION_VERSION);
			logException(e);
		}
	}
}