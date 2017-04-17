package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.JsonPayload;

import java.util.concurrent.Future;

/**
 * @author Sky Kelsey
 */
public interface PayloadStore {

	public void addPayload(JsonPayload... payloads);

	public void deletePayload(JsonPayload payload);

	public void deleteAllPayloads();

	/* Asynchronous call to retrieve the oldest unsent payload from the data storage.
	 * Calling get() method on the returned Future object will block the caller until the Future has completed,
	 */
	public Future<JsonPayload> getOldestUnsentPayload() throws Exception;

}
