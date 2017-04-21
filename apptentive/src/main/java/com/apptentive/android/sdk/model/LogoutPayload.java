/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;

public class LogoutPayload extends JsonPayload {
	public LogoutPayload() {
	}

	public LogoutPayload(String json) throws JSONException {
		super(json);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint() {
		return StringUtils.format("/conversations/%s/logout", getConversationId());
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.POST;
	}

	@Override
	public String getHttpRequestContentType() {
		return "application/json"; // TODO: application/octet-stream
	}

	//endregion
}
