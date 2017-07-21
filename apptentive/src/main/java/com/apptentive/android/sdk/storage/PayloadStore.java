package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.Payload;

interface PayloadStore {

	void addPayload(Payload payloads);

	void deletePayload(String payloadIdentifier);

	void deleteAllPayloads();
}
