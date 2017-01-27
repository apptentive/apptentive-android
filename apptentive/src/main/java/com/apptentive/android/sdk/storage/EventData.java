/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveInternal;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a record of when events and interactions were triggered, as well as the number of times per versionName or versionCode.
 */
public class EventData implements Saveable {

	private static final long serialVersionUID = 1L;

	private Map<String, EventRecord> events;
	private Map<String, EventRecord> interactions;

	public EventData() {
		events = new HashMap<String, EventRecord>();
		interactions = new HashMap<String, EventRecord>();
	}

	//region Listeners
	private transient DataChangedListener listener;

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	//endregion


	// FIXME: Find all usage of this and ensure they use the same timestamp for saving events and runnign interaction queries.
	public synchronized void storeEventForCurrentAppVersion(double timestamp, String eventLabel) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord == null) {
			eventRecord = new EventRecord();
			events.put(eventLabel, eventRecord);
		}
		String versionName = ApptentiveInternal.getInstance().getApplicationVersionName();
		int versionCode = ApptentiveInternal.getInstance().getApplicationVersionCode();
		eventRecord.update(timestamp, versionName, versionCode);
		notifyDataChanged();
	}

	// FIXME: Find all usage of this and ensure they use the same timestamp for saving events and runnign interaction queries.
	public synchronized void storeInteractionForCurrentAppVersion(double timestamp, String interactionId) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord == null) {
			eventRecord = new EventRecord();
			interactions.put(interactionId, eventRecord);
		}
		String versionName = ApptentiveInternal.getInstance().getApplicationVersionName();
		int versionCode = ApptentiveInternal.getInstance().getApplicationVersionCode();
		eventRecord.update(timestamp, versionName, versionCode);
		notifyDataChanged();
	}

	public Long getEventCountTotal(String eventLabel) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord == null) {
			return 0L;
		}
		return eventRecord.getTotal();
	}

	public Long getInteractionCountTotal(String interactionId) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getTotal();
		}
		return 0L;
	}

	public Double getTimeOfLastEventInvocation(String eventLabel) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getLast();
		}
		return null;
	}

	public Double getTimeOfLastInteractionInvocation(String interactionId) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getLast();
		}
		return null;
	}

	public Long getEventCountForVersionCode(String eventLabel, Integer versionCode) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionCode(versionCode);
		}
		return 0L;
	}

	public Long getInteractionCountForVersionCode(String interactionId, Integer versionCode) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionCode(versionCode);
		}
		return 0L;
	}

	public Long getEventCountForVersionName(String eventLabel, String versionName) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionName(versionName);
		}
		return 0L;
	}

	public Long getInteractionCountForVersionName(String interactionId, String versionName) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionName(versionName);
		}
		return 0L;
	}


	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Events: ");
		for (String key : events.keySet()) {
			builder.append("\n\t").append(key).append(": ").append(events.get(key).toString());
		}
		builder.append("\nInteractions: ");
		for (String key : interactions.keySet()) {
			builder.append("\n\t").append(key).append(": ").append(interactions.get(key).toString());
		}
		return builder.toString();
	}
}
