/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.encryption.Encryptor;
import com.apptentive.android.sdk.model.EventPayload;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EncryptedPayloadSenderTest extends TestCaseBase {

	private static final String AUTH_TOKEN = "auth_token";
	private static final String ENCRYPTION_KEY = "5C5361D08DA7AD6CD70ACEB572D387BB713A312DE8CE6128B8A42F62A7B381DB";
	private static final String EVENT_LABEL = "com.apptentive#app#launch";

	@Test
	public void testEncryptedPayload() throws Exception {

		final EventPayload original = new EventPayload(EVENT_LABEL, "trigger");
		original.setToken(AUTH_TOKEN);
		original.setEncryptionKey(ENCRYPTION_KEY);

		byte[] cipherText = original.getData();

		Encryptor encryptor = new Encryptor(ENCRYPTION_KEY);

		try {
			byte[] plainText = encryptor.decrypt(cipherText);
			JSONObject resultJson = new JSONObject(new String(plainText));
			EventPayload result = new EventPayload(resultJson.getJSONObject("payload").toString());
			assertEquals(result.getEventLabel(), EVENT_LABEL);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}