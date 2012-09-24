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
public class BaseMessage implements Message {
	String guid;
	Date created;
	protected boolean incoming;

	public BaseMessage(String guid, Date created, boolean incoming) {
		this.guid = guid;
		this.created = created;
		this.incoming = incoming;
	}

	public String getGuid() {
		return guid;
	}

	public Date getCreated() {
		return created;
	}

	public boolean isIncoming() {
		return incoming;
	}
}
