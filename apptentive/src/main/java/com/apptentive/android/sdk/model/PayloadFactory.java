/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;

import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class PayloadFactory {

	public static JsonPayload fromJson(String json, JsonPayload.BaseType baseType) {
		switch (baseType) {
			case message:
				return MessageFactory.fromJson(json);
			case event:
				return EventFactory.fromJson(json);
			case device:
				return DeviceFactory.fromJson(json);
			case sdk:
				return SdkFactory.fromJson(json);
			case app_release:
				return AppReleaseFactory.fromJson(json);
			case sdk_and_app_release:
				return SdkAndAppReleasePayload.fromJson(json);
			case person:
				return PersonFactory.fromJson(json);
			case logout:
				return LogoutPayloadFactory.fromJson(json);
			case survey:
				try {
					return new SurveyResponsePayload(json);
				} catch (JSONException e) {
					// Ignore
				}
			case unknown:
				ApptentiveLog.v("Ignoring unknown RecordType.");
				break;
			default:
				break;
		}
		return null;
	}
}
