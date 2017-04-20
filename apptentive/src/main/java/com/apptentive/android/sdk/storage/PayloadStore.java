package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.Payload;

import java.util.concurrent.Future;

public interface PayloadStore {

	void addPayload(Payload... payloads);

	void deletePayload(Payload payload);

	void deleteAllPayloads();

	/* Asynchronous call to retrieve the oldest unsent payload from the data storage.
	 * Calling get() method on the returned Future object will block the caller until the Future has completed,
	 */
	Future<Payload> getOldestUnsentPayload() throws Exception;

}
