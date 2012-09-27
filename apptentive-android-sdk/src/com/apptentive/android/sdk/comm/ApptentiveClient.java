/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.Log;
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
 * TODO: Catch HttpHostConnectException, which occurs when data connection is not there
 *
 * @author Sky Kelsey
 */
public class ApptentiveClient {
	private static final String ENDPOINT_BASE = "https://api.apptentive.com";
	private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	private static final String ENDPOINT_SURVEYS = ENDPOINT_BASE + "/surveys";
	private static final String ENDPOINT_SURVEYS_ACTIVE = ENDPOINT_SURVEYS + "/active";
	private static final String ENDPOINT_CONFIGURATION = ENDPOINT_BASE + "/devices/%s/configuration";
	private static final String ENDPOINT_MESSAGES = ENDPOINT_BASE + "/messages";
	private static final String ENDPOINT_MESSAGES_SINCE = ENDPOINT_MESSAGES + "?since=%s";

	public static ApptentiveHttpResponse postJSON(String json) {
		try {
			return performHttpRequest(ENDPOINT_RECORDS, Method.POST, json);
		} catch (IllegalArgumentException e) {
			Log.w("Error posting JSON: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
		} catch (IOException e) {
			Log.w("Error posting JSON: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
		}
		return null;
	}

	public static ApptentiveHttpResponse getSurvey() {
		try {
			return performHttpRequest(ENDPOINT_SURVEYS_ACTIVE, Method.GET, null);
		} catch (IllegalArgumentException e) {
			Log.e("Error fetching survey.", e);
		} catch (IOException e) {
			Log.e("Error fetching survey.", e);
		}
		return null;
	}

	public static ApptentiveHttpResponse getAppConfiguration(String deviceId) {
		String uri = String.format(ENDPOINT_CONFIGURATION, deviceId);
		try {
			return performHttpRequest(uri, Method.GET, null);
		} catch (IllegalArgumentException e) {
			Log.e("Error fetching configuration.", e);
		} catch (IOException e) {
			Log.e("Error fetching configuration: %s", e.getMessage());
		}
		return null;
	}

	/**
	 * Gets all messages since the message specified by guid was specified.
	 *
	 * @param lastGuid Specifies the last successfully fetched message. If null, all messages will be fetched.
	 * @return An ApptentiveHttpResponse object with the HTTP response code, reason, and content.
	 */
	public static ApptentiveHttpResponse getMessages(String lastGuid) {
		Log.e("Fetching messages...");
		String uri;
		if (lastGuid != null) {
			uri = String.format(ENDPOINT_MESSAGES_SINCE, lastGuid);
		} else {
			uri = ENDPOINT_MESSAGES_SINCE;
		}
		try {
			return performHttpRequest(uri, Method.GET, null);
		} catch (IOException e) {
			Log.e("Error fetching survey.", e);
		}
		return null;
	}


	private static ApptentiveHttpResponse performHttpRequest(String uri, Method method, String postBody) throws IOException, IllegalArgumentException {
		Log.e("Performing request...");
		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
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
				((HttpPost)request).setEntity(new StringEntity(postBody, "UTF-8"));
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
		Log.e("Response Status Line: " + response.getStatusLine().toString());
		if (code >= 200 && code < 300) {
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				ret.setContent(EntityUtils.toString(entity, "UTF-8"));
				Log.e("Content: " + ret.getContent());
			}
		}
		return ret;
	}

	private enum Method {
		GET,
		POST
	}
}
