/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

public interface MessageCenterListItem {
	int GREETING = 1;
	int STATUS = 2;
	int MESSAGE_CONTEXT = 3;
	int MESSAGE_AUTO = 4;
	int MESSAGE_OUTGOING = 5;
	int MESSAGE_INCOMING = 6;
	int MESSAGE_COMPOSER = 7;
	int WHO_CARD = 8;

	int getListItemType();
}
