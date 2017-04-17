/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.JsonPayload;
import com.apptentive.android.sdk.network.HttpRequest;

/**
 * Class responsible for creating and sending an {@link HttpRequest} with a given payload
 * FIXME: this is a legacy workaround and would be removed soon
 */
public interface PayloadRequestSender {
	/**
	 * Creates and sends an {@link HttpRequest} for a given payload
	 *
	 * @param payload  to be sent
	 * @param listener Http-request listener for the payload request
	 */
	HttpRequest sendPayload(JsonPayload payload, HttpRequest.Listener<HttpRequest> listener);
}
