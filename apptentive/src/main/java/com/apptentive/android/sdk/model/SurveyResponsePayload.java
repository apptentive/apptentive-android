/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class SurveyResponsePayload extends ConversationItem {

	private static final String KEY_RESPONSE = "response";

	private static final String KEY_SURVEY_ID = "id";

	private static final String KEY_SURVEY_ANSWERS = "answers";

	public SurveyResponsePayload(SurveyInteraction definition, Map<String, Object> answers) {
		super(PayloadType.survey);
		try {
			put(KEY_SURVEY_ID, definition.getId());
			JSONObject answersJson = new JSONObject();
			for (String key : answers.keySet()) {
				answersJson.put(key, answers.get(key));
			}

			put(KEY_SURVEY_ANSWERS, answersJson);
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Unable to construct survey payload.");
			logException(e);
		}
	}

	public SurveyResponsePayload(String json) throws JSONException {
		super(PayloadType.survey, json);
	}

	@Override
	protected String getJsonContainer() {
		return KEY_RESPONSE;
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/surveys/%s/responses", conversationId, getId());
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.POST;
	}

	//endregion

	public String getId() {
		return optString(KEY_SURVEY_ID, null);
	}

}
