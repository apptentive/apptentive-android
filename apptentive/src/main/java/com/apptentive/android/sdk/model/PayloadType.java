/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;

import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public enum PayloadType {
	message,
	event,
	device,
	sdk,
	app_release,
	sdk_and_app_release,
	person,
	logout,
	unknown,
	// Legacy
	survey;

	public static PayloadType parse(String type) {
		try {
			return PayloadType.valueOf(type);
		} catch (IllegalArgumentException e) {
			ApptentiveLog.v(PAYLOADS, "Error parsing unknown Payload.PayloadType: " + type);
			logException(e);
		}
		return unknown;
	}
}
