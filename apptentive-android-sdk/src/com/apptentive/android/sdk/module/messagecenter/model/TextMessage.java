/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

/**
 * @author Sky Kelsey
 */
public class TextMessage implements Message {
	protected String text;
	protected boolean incoming;

	public TextMessage(String text, boolean incoming) {
		this.text = text;
		this.incoming = incoming;
	}

	public String getText() {
		return text;
	}

	public boolean isIncoming() {
		return incoming;
	}
}
