/*
 * Created by Sky Kelsey on 2011-10-06.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.offline;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.model.GlobalInfo;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Sky Kelsey.
 */
public class PayloadManager implements Runnable {

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Static
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String PAYLOAD_KEY_PREFIX = "payload-";

	private static PayloadManager instance;
	private static boolean running;

	public static void initialize(Context context){
		if(instance == null){
			instance = new PayloadManager(context);
			running = false;
		}

		// When the app starts, clear out the queue and fill it back up. This ensures all payloads get sent
		// upon startup if the network is available.
		instance.queue.clear();
		instance.initQueue();
		instance.ensureRunning();
	}

	public static PayloadManager getInstance(){
		return instance;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	private LinkedBlockingQueue<String> queue;
	private SharedPreferences prefs;

	private PayloadManager(Context context){
		this.queue = new LinkedBlockingQueue<String>();
		this.prefs = 	context.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE);
	}

	public synchronized void ensureRunning(){
		if(!running){
			running = true;
			new Thread(this).start();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// LinkedBlockingQueue
	//////////////////////////////////////////////////////////////////////////////////////////////////////


	public void putPayload(Payload payload) {
		String name = PAYLOAD_KEY_PREFIX + UUID.randomUUID().toString();
		prefs.edit().putString(name, payload.getAsJSON()).commit();
		queue.offer(name);
		ensureRunning();
	}

	private synchronized void initQueue(){
		Set<String> keys = prefs.getAll().keySet();
		for(String key : keys){
			if(key.startsWith(PAYLOAD_KEY_PREFIX)){
				queue.offer(key);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Runnable
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Blocks on the payload queue until a payload becomes available.
	 * If any error occurs, exit thread.
	 * If the json is bad, delete it.
	 * If the json was sent successfully, delete it.
	 */
	public void run() {
		try{
			while(true){
				String name;
				try{
					name = queue.take();
				}catch(InterruptedException e){
					break;
				}
				if(name == null){
					// Can this even happen?
					break;
				}
				Log.d("Got a payload to send: " + name);
				String json = prefs.getString(name, null);
				if(json == null){
					prefs.edit().remove(name).commit();
					continue;
				}
				ApptentiveClient client = new ApptentiveClient(GlobalInfo.apiKey);
				boolean success = client.postJSON(json);
				if(success){
					prefs.edit().remove(name).commit();
				}else{
					Log.d("Unable to send JSON. Stopping upload thread.");
					break;
				}
			}
		}finally{
			running = false;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////

}
