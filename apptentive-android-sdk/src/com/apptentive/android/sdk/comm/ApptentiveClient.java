/*
 * ApptentiveClient.java
 *
 * Created by Sky Kelsey on 2011-05-30.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.survey.SurveyDefinition;
import com.apptentive.android.sdk.survey.SurveyManager;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ApptentiveClient {
	private static final String ENDPOINT_BASE     = "http://api.apptentive-beta.com";
	private static final String ENDPOINT_RECORDS = ENDPOINT_BASE + "/records";
	private static final String ENDPOINT_SURVEYS  = ENDPOINT_BASE + "/surveys";

	private final ALog log = new ALog(ApptentiveClient.class);

	private final String APPTENTIVE_API_KEY;

	public ApptentiveClient(String apiKey) {
		this.APPTENTIVE_API_KEY = apiKey;
	}

	public boolean postJSON(String json){
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(ENDPOINT_RECORDS);

		post.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Accept", "application/json");

		StringBuilder content = new StringBuilder();
		InputStream is = null;
		try{
			post.setEntity(new StringEntity(json, "UTF-8"));
			HttpResponse response = httpClient.execute(post);
			is = response.getEntity().getContent();
			byte[] line = new byte[1024];
			int size;
			while((size = is.read(line)) != -1){
				content.append(new String(line, 0, size));
			}
			log.e(response.getStatusLine().toString());
			//log.e(content.toString());

			return (200 <= response.getStatusLine().getStatusCode()) &&
			       (300 > response.getStatusLine().getStatusCode());
		}catch(IOException e){
			log.w("Error submitting feedback.", e);
		}finally{
			if(is != null){
				try{
					is.close();
				}catch(Exception e){}
			}
		}
		return false;
	}

	public boolean postFeedback(List<? extends NameValuePair> params){
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(ENDPOINT_RECORDS);

		post.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setHeader("Accept", "application/json");

		StringBuilder content = new StringBuilder();
		InputStream is = null;
		try{
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = httpClient.execute(post);
			is = response.getEntity().getContent();
			byte[] line = new byte[1024];
			int size;
			while((size = is.read(line)) != -1){
				content.append(new String(line, 0, size));
			}
			log.e(response.getStatusLine().toString());
			//log.e(content.toString());

			return (200 <= response.getStatusLine().getStatusCode()) &&
			       (300 > response.getStatusLine().getStatusCode());
		}catch(IOException e){
			log.w("Error submitting feedback.", e);
		}finally{
			if(is != null){
				try{
					is.close();
				}catch(Exception e){}
			}
		}
		return false;
	}

	public List<SurveyDefinition> getSurveys(){
		InputStream is = null;
		try{
			String uri = ENDPOINT_SURVEYS;
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet get = new HttpGet();

			get.setURI(new URI(uri));
			get.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
			get.setHeader("Accept", "application/json");

			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			log.i("HTTP response status line: " + response.getStatusLine().toString());

			if(code >= 200 && code < 300){
				String content = EntityUtils.toString(response.getEntity(), "UTF-8");
				log.e("Response: " + content);
				return SurveyManager.parseSurveys(content);
			}
		}catch(URISyntaxException e){
			log.e("Error fetching contact information.", e);
		}catch(IOException e){
			log.e("Error fetching contact information.", e);
		}catch(JSONException e){
			log.e("Error parsing retrieved surveys.", e);
		}finally{
			if(is != null){
				try{
					is.close();
				}catch(Exception e){}
			}
		}
		return null;
	}
}
