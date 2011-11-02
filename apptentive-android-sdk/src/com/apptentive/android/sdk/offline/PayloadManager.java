/*
 * PayloadUploader.java
 *
 * Created by Sky Kelsey on 2011-10-06.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.model.ApptentiveModel;

import java.util.UUID;

public class PayloadManager{

	private static final String PAYLOAD_INDEX_NAME = "apptentive-payloads-json";

	private SharedPreferences prefs;

	public PayloadManager(SharedPreferences prefs){
		this.prefs = prefs;
	}

	public void save(JSONPayload payload){
		String uuid = UUID.randomUUID().toString();
		storePayload(uuid, payload);
		addToPayloadList(uuid);
	}

	private void storePayload(String name, JSONPayload payload){
		prefs.edit().putString(name, payload.getAsJSON()).commit();
	}

	private void addToPayloadList(String name){
		String payloadNames = prefs.getString(PAYLOAD_INDEX_NAME, "");
		payloadNames = (payloadNames.length() == 0 ? name : payloadNames + ";" + name);
		prefs.edit().putString(PAYLOAD_INDEX_NAME, payloadNames).commit();
	}

	public String getFirstPayloadInPayloadList(){
		String[] payloadNames = prefs.getString(PAYLOAD_INDEX_NAME, "").split(";");
		if(payloadNames.length > 0){
			return prefs.getString(payloadNames[0], "");
		}
		return null;
	}

	private void deletePayload(String name){
		prefs.edit().remove(name).commit();
	}

	public void deleteFirstPayloadInPayloadList(){
		String[] payloadNames = prefs.getString(PAYLOAD_INDEX_NAME, "").split(";");
		String newPayloadList = "";
		for (int i = 0; i < payloadNames.length; i++) {
			String payloadName = payloadNames[i];
			if(i == 0){
				deletePayload(payloadName);
			}else{
				newPayloadList = (newPayloadList.equals("") ? payloadName : newPayloadList + ";" + payloadName);
			}
		}
		prefs.edit().putString(PAYLOAD_INDEX_NAME, newPayloadList).commit();
	}

	public void run(){
		PayloadUploader uploader = new PayloadUploader(prefs);
		uploader.start();
	}


	private class PayloadUploader extends Thread{
		private ALog log = new ALog(PayloadUploader.class);
		private SharedPreferences prefs;

		public PayloadUploader(SharedPreferences prefs){
			this.prefs = prefs;
		}

		@Override
		public void run() {

			PayloadManager payloadManager = new PayloadManager(this.prefs);
			String json;
			json  = payloadManager.getFirstPayloadInPayloadList();
			while(json != null && !json.equals("")){
				log.i("JSON: " + json);
				ApptentiveClient client = new ApptentiveClient(ApptentiveModel.getInstance().getApiKey());
				boolean success = client.postJSON(json);
				if(success){
					payloadManager.deleteFirstPayloadInPayloadList();
				}else{
					log.e("Unable to uploader JSONPayload");
					break;
				}
				json = payloadManager.getFirstPayloadInPayloadList();
			}
		}
	}
}
