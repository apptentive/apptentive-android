/*
 * ApptentiveClient.java
 *
 * Created by Sky Kelsey on 2011-05-30.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApptentiveClient {
	private static final String ENDPOINT_BASE = "https://api.apptentive.com";
	private static final String ENDPOINT_FEEDBACK = ENDPOINT_BASE + "/records";

	private final ALog log = new ALog(ApptentiveClient.class);

	private final String APPTENTIVE_API_KEY;

	public ApptentiveClient(String apiKey) {
		this.APPTENTIVE_API_KEY = apiKey;
	}

	public String submitFeedback(String uuid, String name, String email, String model, String osVersion, String carrier, String feedback, String feedbackType, Date feedbackDate){
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(ENDPOINT_FEEDBACK);

		post.setHeader("Authorization", "OAuth " + APPTENTIVE_API_KEY);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setHeader("Accept", "application/json");

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("record[device][uuid]",       uuid));
		params.add(new BasicNameValuePair("record[device][model]",      model));
		params.add(new BasicNameValuePair("record[device][os_version]", osVersion));
		params.add(new BasicNameValuePair("record[device][carrier]",    carrier));
		params.add(new BasicNameValuePair("record[client][version]",    Apptentive.APPTENTIVE_API_VERSION));
		params.add(new BasicNameValuePair("record[client][os]",         "Android"));
		params.add(new BasicNameValuePair("record[user][name]",         name));
		params.add(new BasicNameValuePair("record[user][email]",        email));
		params.add(new BasicNameValuePair("record[feedback][feedback]", feedback));
		params.add(new BasicNameValuePair("record[feedback][type]",     feedbackType));
		params.add(new BasicNameValuePair("record[date]",               Util.dateToString(feedbackDate)));

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
		}catch(UnsupportedEncodingException e){
			log.e("Error submitting feedback.", e);
		}catch(IOException e){
			log.e("Error submitting feedback.", e);
		}finally{
			if(is != null){
				try{
					is.close();
				}catch(Exception e){}
			}
		}
		return content.toString();
	}
}
