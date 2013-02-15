/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

/**
 * @author Sky Kelsey
 */
public class ApptentiveHttpResponse {
	private String content;
	private String reason;
	private int code;

	public boolean wasSuccessful() {
		return code >= 200 && code < 300;
	}

	public boolean wasRejectedPermanently() {
		return code >= 400 && code < 500;
	}

	public boolean wasRejectedTemporarily() {
		return !(wasSuccessful() || wasRejectedPermanently());
	}

	public ApptentiveHttpResponse() {
		content = null;
		reason = null;
		code = -1;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
