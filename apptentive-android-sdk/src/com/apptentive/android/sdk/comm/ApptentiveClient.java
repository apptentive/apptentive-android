/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.survey.SurveyDefinition;
import com.apptentive.android.sdk.module.survey.SurveyManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * TODO: Make a generic get, post method, etc.
 * TODO: Catch HttpHostConnectException, which occurs when data connection is not there
 * TODO: Don't communicate when there is no connection present.
 *
 * @author Sky Kelsey
 */
public class ApptentiveClient {
	private static final String ENDPOINT_BASE = "http://api.apptentive-beta.com";
	private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	private static final String ENDPOINT_SURVEYS = ENDPOINT_BASE + "/surveys";
	private static final String ENDPOINT_SURVEYS_ACTIVE = ENDPOINT_SURVEYS + "/active";
	private static final String ENDPOINT_CONFIGURATION = ENDPOINT_BASE + "/devices/%s/configuration";

	private final String APPTENTIVE_API_KEY;

	public ApptentiveClient(String apiKey) {
		this.APPTENTIVE_API_KEY = apiKey;
	}

	public boolean postJSON(String json) {
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpConnectionParams.setSoTimeout(httpParams, 30000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost post = new HttpPost(ENDPOINT_RECORDS);

		post.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Accept", "application/json");

		StringBuilder content = new StringBuilder();
		InputStream is = null;
		try {
			Log.d("Posting JSON: " + json);
			post.setEntity(new StringEntity(json, "UTF-8"));
			HttpResponse response = httpClient.execute(post);
			is = response.getEntity().getContent();
			byte[] line = new byte[1024];
			int size;
			while ((size = is.read(line)) != -1) {
				content.append(new String(line, 0, size));
			}
			Log.d(response.getStatusLine().toString());
			Log.v(content.toString());

			return (200 <= response.getStatusLine().getStatusCode()) &&
					(300 > response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			Log.w("Error submitting feedback.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	public SurveyDefinition getSurvey() {
		InputStream is = null;
		try {
			String uri = ENDPOINT_SURVEYS_ACTIVE;
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet();

			get.setURI(new URI(uri));
			get.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
			get.setHeader("Accept", "application/json");

			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			Log.d("Survey: HTTP response status line: " + response.getStatusLine().toString());

			if (code >= 200 && code < 300) {
				String content = EntityUtils.toString(response.getEntity(), "UTF-8");
				Log.v("Survey: " + content);
				return SurveyManager.parseSurvey(content);
			}
		} catch (URISyntaxException e) {
			Log.e("Error fetching contact information.", e);
		} catch (IOException e) {
			Log.e("Error fetching contact information.", e);
		} catch (JSONException e) {
			Log.e("Error parsing retrieved surveys.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	public HashMap<String, Object> getAppConfiguration(String deviceId) {
		HashMap<String, Object> config = new HashMap<String, Object>();
		InputStream is = null;
		try {
			String uri = String.format(ENDPOINT_CONFIGURATION, deviceId);
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet();

			get.setURI(new URI(uri));
			get.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
			get.setHeader("Accept", "application/json");

			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			Log.d("Configuration: HTTP response status line: " + response.getStatusLine().toString());

			if (code >= 200 && code < 300) {
				String content = EntityUtils.toString(response.getEntity(), "UTF-8");
				JSONObject root = new JSONObject(content);
				Iterator it = root.keys();
				while (it.hasNext()) {
					String key =  (String)it.next();
					Object value = root.get(key);
					if(value instanceof JSONObject) {
						config.put(key, value.toString());
					} else {
						config.put(key, value);
					}
				}
				Log.v("Configuration: " + content);
			}
		} catch (JSONException e) {
			Log.e("Error fetching configuration.", e);
		} catch (URISyntaxException e) {
			Log.e("Error fetching configuration.", e);
		} catch (IOException e) {
			Log.e("Error fetching configuration.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return config;
	}
}
