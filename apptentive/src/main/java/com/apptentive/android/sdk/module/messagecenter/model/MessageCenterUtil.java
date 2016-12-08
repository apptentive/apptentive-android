/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

public class MessageCenterUtil {

	// Combine both incoming and outgoing interfaces into one
	public interface CompoundMessageCommonInterface {
		void setBody(String body);

		String getBody();

		void setLastSent(boolean bVal);

		boolean isLastSent();
	}
}