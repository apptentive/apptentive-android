/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

public class ContextMessage implements MessageCenterListItem {

	private String body;

	public ContextMessage(String body) {
		this.body = body;
	}

	@Override
	public int getListItemType() {
		return MESSAGE_CONTEXT;
	}

	public String getBody() {
		return body;
	}
}
