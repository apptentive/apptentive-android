/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.*;

/**
 * @author Sky Kelsey
 */
public class ApptentiveClient {
	//private static final String ENDPOINT_BASE = "https://api.apptentive.com";
	private static final String ENDPOINT_BASE = "http://api.apptentive-beta.com";
	private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	private static final String ENDPOINT_SURVEYS = ENDPOINT_BASE + "/surveys";
	private static final String ENDPOINT_SURVEYS_ACTIVE = ENDPOINT_SURVEYS + "/active";
	private static final String ENDPOINT_CONFIGURATION = ENDPOINT_BASE + "/devices/%s/configuration";
	private static final String ENDPOINT_MESSAGES = ENDPOINT_BASE + "/people/%s/messages";
	private static final String ENDPOINT_MESSAGES_SINCE = ENDPOINT_MESSAGES + "?newer_than=%s";
	private static final String ENDPOINT_PEOPLE = ENDPOINT_BASE + "/people";


	/**
	 * Gets all messages since the message specified by guid was specified.
	 *
	 * @param lastMessageId Specifies the last successfully fetched message. If null, all messages will be fetched.
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(String personId, String lastMessageId) {
		Log.d("Fetching messages for person: " + personId + ", and lastGuid: " + lastMessageId);
		String uri = String.format(ENDPOINT_MESSAGES, personId);
		if (lastMessageId != null) {
			uri = String.format(ENDPOINT_MESSAGES_SINCE, personId, lastMessageId);
		}
		return performHttpRequest(uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse createPerson() {
		return performHttpRequest(ENDPOINT_PEOPLE, Method.POST, new Person().toString());
	}

	public static ApptentiveHttpResponse getSurvey() {
		return performHttpRequest(ENDPOINT_SURVEYS_ACTIVE, Method.GET, null);
	}

	public static ApptentiveHttpResponse getAppConfiguration(String deviceId) {
		String uri = String.format(ENDPOINT_CONFIGURATION, deviceId);
		return performHttpRequest(uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse postMessage(String json) {
		String uri = String.format(ENDPOINT_MESSAGES, GlobalInfo.personId);
		return performHttpRequest(uri, Method.POST, json);
	}

	public static ApptentiveHttpResponse postRecord(String json) {
		return performHttpRequest(ENDPOINT_RECORDS, Method.POST, json);
	}

	private static ApptentiveHttpResponse performHttpRequest(String uri, Method method, String postBody) {
		Log.v("Performing request to %s", uri);
		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		try {
			HttpClient httpClient;
			HttpRequestBase request;
			httpClient = new DefaultHttpClient();
			switch (method) {
				case GET:
					request = new HttpGet(uri);
					break;
				case POST:
					request = new HttpPost(uri);
					HttpParams httpParams = request.getParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
					HttpConnectionParams.setSoTimeout(httpParams, 30000);
					request.setHeader("Content-Type", "application/json");
					Log.v("Post body: " + postBody);
					((HttpPost) request).setEntity(new StringEntity(postBody, "UTF-8"));
					break;
				default:
					Log.e("Unrecognized method: " + method.name());
					return null;
			}
			request.setHeader("Authorization", "OAuth " + GlobalInfo.apiKey);
			request.setHeader("Accept", "application/json");

			HttpResponse response = httpClient.execute(request);
			int code = response.getStatusLine().getStatusCode();
			ret.setCode(code);
			ret.setReason(response.getStatusLine().getReasonPhrase());
			Log.v("Response Status Line: " + response.getStatusLine().toString());
			if (code >= 200 && code < 300) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ret.setContent(EntityUtils.toString(entity, "UTF-8"));
					Log.e("Content: " + ret.getContent());
				}
			}
		} catch (IllegalArgumentException e) {
			Log.w("Error communicating with server.", e);
		} catch (IOException e) {
			Log.w("Error communicating with server.", e);
		}
		return ret;
	}

	private enum Method {
		GET,
		POST
	}
}
