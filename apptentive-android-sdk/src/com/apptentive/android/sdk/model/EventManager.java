/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.storage.EventStore;
import com.apptentive.android.sdk.storage.PayloadSendWorker;

/**
 * @author Sky Kelsey
 */
public class EventManager {

	private static EventStore getEventStore() {
		return Apptentive.getDatabase();
	}

	public static void sendEvent(Event event) {
		getEventStore().addPayload(event);
		PayloadSendWorker.start();
	}
}
