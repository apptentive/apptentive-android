/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import java.util.Date;

/**
 * @author Sky Kelsey
 */
public class TextMessage extends BaseMessage {
	protected String text;

	public TextMessage(String guid, Date created, String text, boolean incoming) {
		super(guid, created, incoming);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
