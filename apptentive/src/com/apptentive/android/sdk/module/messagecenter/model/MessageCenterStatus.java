/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageCenterStatus extends JSONObject implements MessageCenterListItem {

	public final String body;

	public MessageCenterStatus(String body) {
		this.body = body;
	}
}
