package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.Payload;

import java.util.concurrent.Future;

public interface PayloadStore {

	void addPayload(Payload... payloads);

	void deletePayload(String payloadIdentifier);

	void deleteAllPayloads();
}
