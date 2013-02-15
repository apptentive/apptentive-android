/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.ActivityFeedTokenRequest;
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
import java.net.URL;

/**
 * @author Sky Kelsey
 */
public class ApptentiveClient {

	// TODO: Break out a version for each endpoint if we start to version endpoints separately.
	private static final String API_VERSION = "1";

	private static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 30000;
	private static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 30000;

	// New API
	private static final String ENDPOINT_BASE = "http://api.apptentive-beta.com";
	private static final String ENDPOINT_ACTIVITY_FEED_CREATE = ENDPOINT_BASE + "/activity_feed";
	private static final String ENDPOINT_ACTIVITY_FEED_FETCH = ENDPOINT_BASE + "/activity_feed?count=%s&after_id=%s&before_id=%s";
	private static final String ENDPOINT_MESSAGES = ENDPOINT_BASE + "/messages";
	private static final String ENDPOINT_EVENTS = ENDPOINT_BASE + "/events";

	// Old API
	private static final String ENDPOINT_CONFIGURATION = ENDPOINT_BASE + "/devices/%s/configuration";
	private static final String ENDPOINT_SURVEYS = ENDPOINT_BASE + "/surveys";
	private static final String ENDPOINT_SURVEYS_ACTIVE = ENDPOINT_SURVEYS + "/active";
	private static final String ENDPOINT_MESSAGES_SINCE = ENDPOINT_MESSAGES + "?since_id=%s";
	private static final String ENDPOINT_PEOPLE = ENDPOINT_BASE + "/people";

	// Deprecated API
	private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";


	public static ApptentiveHttpResponse getActivityFeedToken(ActivityFeedTokenRequest activityFeedTokenRequest) {
		return performHttpRequest(GlobalInfo.apiKey, ENDPOINT_ACTIVITY_FEED_CREATE, Method.POST, activityFeedTokenRequest.toString());
	}

	public static ApptentiveHttpResponse getAppConfiguration(String deviceId) {
		String uri = String.format(ENDPOINT_CONFIGURATION, deviceId);
		return performHttpRequest(GlobalInfo.apiKey, uri, Method.GET, null);
	}

	/**
	 * Gets all messages since the message specified by guid was specified.
	 *
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(Integer count, String afterId, String beforeId) {
		String uri = String.format(ENDPOINT_ACTIVITY_FEED_FETCH, count == null ? "" : count.toString(), afterId == null ? "" : afterId, beforeId == null ? "" : beforeId);
		return performHttpRequest(GlobalInfo.activityFeedToken, uri, Method.GET, null);
	}

	public static ApptentiveHttpResponse postMessage(String json) {
		return performHttpRequest(GlobalInfo.activityFeedToken, ENDPOINT_MESSAGES, Method.POST, json);
	}

	public static ApptentiveHttpResponse postEvent(String json) {
		return performHttpRequest(GlobalInfo.activityFeedToken, ENDPOINT_EVENTS, Method.POST, json);
	}

	public static ApptentiveHttpResponse postRecord(String json) {
		return performHttpRequest(GlobalInfo.apiKey, ENDPOINT_RECORDS, Method.POST, json);
	}

	public static ApptentiveHttpResponse getSurvey() {
		return performHttpRequest(GlobalInfo.apiKey, ENDPOINT_SURVEYS_ACTIVE, Method.GET, null);
	}

	private static ApptentiveHttpResponse performHttpRequest(String oauthToken, String uri, Method method, String postBody) {
		Log.d("Performing request to %s", uri);
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
					HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_HTTP_CONNECT_TIMEOUT);
					HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_HTTP_SOCKET_TIMEOUT);
					request.setHeader("Content-Type", "application/json");
					Log.d("Post body: " + postBody);
					((HttpPost) request).setEntity(new StringEntity(postBody, "UTF-8"));
					break;
				default:
					Log.e("Unrecognized method: " + method.name());
					return null;
			}
			request.setHeader("Authorization", "OAuth " + oauthToken);
			request.setHeader("Accept", "application/json");
			request.setHeader("X-API-Version", API_VERSION);

			HttpResponse response = httpClient.execute(request);
			int code = response.getStatusLine().getStatusCode();
			ret.setCode(code);
			ret.setReason(response.getStatusLine().getReasonPhrase());
			Log.d("Response Status Line: " + response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				ret.setContent(EntityUtils.toString(entity, "UTF-8"));
				if (code >= 200 && code < 300) {
					Log.d("Response: " + ret.getContent());
				} else {
					Log.w("Response: " + ret.getContent());
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
