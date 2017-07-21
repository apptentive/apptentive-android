/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage.legacy;

import com.apptentive.android.sdk.model.AppReleasePayload;
import com.apptentive.android.sdk.model.DevicePayload;
import com.apptentive.android.sdk.model.EventPayload;
import com.apptentive.android.sdk.model.JsonPayload;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadType;
import com.apptentive.android.sdk.model.PersonPayload;
import com.apptentive.android.sdk.model.SdkPayload;
import com.apptentive.android.sdk.model.SurveyResponsePayload;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;

import org.json.JSONException;

/**
 * We only keep this class for legacy database migration purposes
 */
public final class LegacyPayloadFactory {
	public static JsonPayload createPayload(PayloadType type, String json) throws JSONException {
		switch (type) {
			case message:
				return MessageFactory.fromJson(json);
			case event:
				return new EventPayload(json);
			case device:
				return new DevicePayload(json);
			case sdk:
				//return new SdkPayload(json);
				// TODO: FIXME
				return null;
			case app_release:
				//return new AppReleasePayload(json);
				return null;
			case person:
				return new PersonPayload(json);
			case survey:
				return new SurveyResponsePayload(json);
			default:
				throw new IllegalArgumentException("Unexpected payload type: " + type);
		}
	}
}
