/*
 * Payload.java
 *
 * Created by skelsey on 2011-09-25.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import android.content.SharedPreferences;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.util.Util;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

public class Payload {

	private static final String PAYLOAD_INDEX_NAME = "apptentive-payloads";
	private static final String PAYLOAD_MANIFEST_NAME = "apptentive-payload-manifest_";
	private static final String PAYLOAD_ENTRY_PREFIX = "apptentive-payload-entry_";
	private ALog log = new ALog(Payload.class);

	protected Map<String, String> params = new TreeMap<String, String>();
	protected String payloadName;

	protected Payload(){
		payloadName = Util.dateToString(new Date(), Util.STRINGSAFE_DATE_FORMAT);
	}

	public List<BasicNameValuePair> getParams(){
		List<BasicNameValuePair> retParams = new ArrayList<BasicNameValuePair>();
		for(String key : params.keySet()){
			retParams.add(new BasicNameValuePair(key, params.get(key)));
		}
		return retParams;
	}

	public void store(SharedPreferences prefs){
		SharedPreferences.Editor editor = prefs.edit();
		addToIndex(prefs);
		storeManifest(editor);
		for(String key : params.keySet()){
			storeValue(editor, PAYLOAD_ENTRY_PREFIX+payloadName+"_"+key, params.get(key));
		}
		prefs.edit().commit();
	}

	public void retrieveOldest(SharedPreferences prefs){
		List<String> payloadNames = Arrays.asList(prefs.getString(PAYLOAD_INDEX_NAME, "").split(";"));
		Collections.sort(payloadNames);
		for(String payloadName : payloadNames){
			if(!payloadName.equals("")){
				retrieve(prefs, payloadName);
				return;
			}
		}
	}

	public void retrieve(SharedPreferences prefs, String payloadNameToGet){
		List<String> entryNames = Arrays.asList(prefs.getString(PAYLOAD_MANIFEST_NAME + payloadNameToGet, "").split(";"));
		for(String entryName : entryNames){
			String key = entryName.substring((PAYLOAD_ENTRY_PREFIX+payloadNameToGet).length()+1);
			String value = prefs.getString(entryName, "");
			params.put(key, value);
		}
	}

	private void addToIndex(SharedPreferences prefs){
		String index = prefs.getString(PAYLOAD_INDEX_NAME, "");
		List<String> payloadNames = Arrays.asList(index.split(";"));
		if(!payloadNames.contains(getPayloadName())){
			storeValue(prefs.edit(), PAYLOAD_INDEX_NAME, (index.equals("") ? "" : index + ";") + getPayloadName());
		}
	}

	private void storeManifest(SharedPreferences.Editor editor){
		StringBuilder manifest = new StringBuilder();
		for(String key : params.keySet()){
			manifest.append(getPayloadEntryName(key)).append(";");
		}
		storeValue(editor, PAYLOAD_MANIFEST_NAME+payloadName, manifest.toString());
	}

	private void storeValue(SharedPreferences.Editor editor, String key, String value){
		editor.putString(key, value).commit();
	}

	private String getPayloadName(){
		return payloadName;
	}
	private String getPayloadEntryName(String key){
		return PAYLOAD_ENTRY_PREFIX+payloadName+"_"+key;
	}

}
