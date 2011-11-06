/*
 * PayloadUploader.java
 *
 * Created by Sky Kelsey on 2011-10-06.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import android.content.SharedPreferences;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.model.GlobalInfo;

import java.util.UUID;

public class PayloadManager{

	private static final String PAYLOAD_INDEX_NAME = "apptentive-payloads-json";

	private SharedPreferences prefs;

	public PayloadManager(SharedPreferences prefs){
		this.prefs = prefs;
	}

	public void save(Payload payload){
		String uuid = UUID.randomUUID().toString();
		storePayload(uuid, payload);
		addToPayloadList(uuid);
	}

	private void storePayload(String name, Payload payload){
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
				Log.i("JSON: " + json);
				ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
				boolean success = client.postJSON(json);
				if(success){
					payloadManager.deleteFirstPayloadInPayloadList();
				}else{
					Log.e("Unable to upload Payload");
					break;
				}
				json = payloadManager.getFirstPayloadInPayloadList();
			}
		}
	}
}
