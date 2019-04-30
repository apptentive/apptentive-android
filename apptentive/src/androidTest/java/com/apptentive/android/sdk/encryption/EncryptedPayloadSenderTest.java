/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.encryption;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.model.EventPayload;

import org.json.JSONObject;
import org.junit.Test;

import static com.apptentive.android.sdk.util.Constants.PAYLOAD_ENCRYPTION_KEY_TRANSFORMATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EncryptedPayloadSenderTest extends TestCaseBase {

	private static final String AUTH_TOKEN = "auth_token";
	private static final String ENCRYPTION_KEY = "5C5361D08DA7AD6CD70ACEB572D387BB713A312DE8CE6128B8A42F62A7B381DB";
	private static final String EVENT_LABEL = "com.apptentive#app#launch";

	@Test
	public void testEncryptedPayload() throws Exception {

		Encryption encryption = EncryptionFactory.createEncryption(ENCRYPTION_KEY, PAYLOAD_ENCRYPTION_KEY_TRANSFORMATION);

		final EventPayload original = new EventPayload(EVENT_LABEL, "trigger");
		original.setToken(AUTH_TOKEN);
		original.setEncryption(encryption);
		original.setAuthenticated(true);

		byte[] cipherText = original.renderData();
		byte[] plainText = encryption.decrypt(cipherText);
		JSONObject result = new JSONObject(new String(plainText));
		String label = result.getJSONObject("event").getString("label");
		assertEquals(label, EVENT_LABEL);
	}
}