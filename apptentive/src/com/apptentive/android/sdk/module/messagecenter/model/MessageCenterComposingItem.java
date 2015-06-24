/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class MessageCenterComposingItem implements MessageCenterListItem {
	private String composingContent;

	public MessageCenterComposingItem(String content) {
		this.composingContent = content;
	}

	public String getComposingContent() {
		return composingContent;
	}

}