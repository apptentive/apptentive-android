/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.messagecenter;

import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.tests.ApptentiveInstrumentationTestCase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class JsonObjectBindingTest extends ApptentiveInstrumentationTestCase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	public JsonObjectBindingTest() {
		super();
	}

	public void testParsingTextMessageResponse() {
		String exampleResponse = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testParsingTextMessageResponse.json");
		List<ApptentiveMessage> apptentiveMessages = null;
		try {
			apptentiveMessages = MessageManager.parseMessagesString(getTargetContext(), exampleResponse);
		} catch (JSONException e) {
		}
		assertNotNull(apptentiveMessages);
		assertEquals(apptentiveMessages.size(), 5);
	}

	public void testTextMessageRoundTrip() {
		String exampleMessage = FileUtil.loadTextAssetAsString(getInstrumentation().getContext(), TEST_DATA_DIR + "testTextMessageRoundTrip.json");
		CompoundMessage message = null;
		try {
			message = new CompoundMessage(exampleMessage, true);
		} catch (JSONException e) {

		}
		assertNotNull(message);
		String recoveredMessage = message.toString();
		assertEquals(true, recoveredMessage.contains("\"id\":\"520a84fe4712c71b65000005\""));
		assertEquals(true, recoveredMessage.contains("\"nonce\":\"33b042735704283c0407f22e6d6f8c6fcb0d2f073bf964f76d36ef176ec26472\""));
		assertEquals(true, recoveredMessage.contains("\"created_at\":1.376421118499E9"));
		assertEquals(true, recoveredMessage.contains("\"client_created_at\":1376421112"));
		assertEquals(true, recoveredMessage.contains("\"client_created_at_utc_offset\":-25200"));
		assertEquals(true, recoveredMessage.contains("\"type\":\"CompoundMessage\""));
		assertEquals(true, recoveredMessage.contains("\"name\":\"Sky Kelsey\""));
		assertEquals(true, recoveredMessage.contains("\"id\":\"4de48b826688000001000007\""));
		assertEquals(true, recoveredMessage.contains("\"body\":\"Test reply via email.\""));
	}
}
