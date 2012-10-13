/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class ShareRequest extends Message {
	public ShareRequest(String json) throws JSONException {
		super(json);
		setType(MessageType.share_request);
	}
}
