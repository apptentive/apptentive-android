/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.network.HttpRequest;

/**
 * Created by alementuev on 4/4/17.
 */

interface PayloadRequestFactory<T extends Payload> {
	HttpRequest createRequest(T payload);
}
