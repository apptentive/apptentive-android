/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

public class MessageCenterUtil {

	public interface MessageCenterListItem {

	}

	// Combine both incoming and outgoing interfaces into one
	public interface CompoundMessageCommonInterface {

		public void setBody(String body);
		public String getBody();
		public void setLastSent(boolean bVal);
		public boolean isLastSent();

	}

}