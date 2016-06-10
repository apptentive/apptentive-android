package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.Payload;

import java.util.concurrent.Future;

/**
 * @author Sky Kelsey
 */
public interface PayloadStore {

	public void addPayload(Payload... payloads);

	public void deletePayload(Payload payload);

	public void deleteAllPayloads();

	/* Asynchronous call to retrieve the oldest unsent payload from the data storage.
	 * Calling get() method on the returned Future object will block the caller until the Future has completed,
	 */
	public Future<Payload> getOldestUnsentPayload() throws Exception;

}
