/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.module.messagecenter;

import android.support.test.runner.AndroidJUnit4;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.tests.util.FileUtil;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class JsonObjectBindingTest extends ApptentiveTestCaseBase {

	private static final String TEST_DATA_DIR = "model" + File.separator;

	@Test
	public void parsingTextMessageResponse() {
		String exampleResponse = FileUtil.loadTextAssetAsString(TEST_DATA_DIR + "testParsingTextMessageResponse.json");
		List<ApptentiveMessage> apptentiveMessages = null;
		try {
			MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();
			if (mgr != null) {
				apptentiveMessages = mgr.parseMessagesString(exampleResponse);
			}
		} catch (JSONException e) {
		}
		assertNotNull(apptentiveMessages);
		assertEquals(apptentiveMessages.size(), 5);
	}

	@Test
	public void textMessageRoundTrip() {
		String exampleMessage = FileUtil.loadTextAssetAsString(TEST_DATA_DIR + "testTextMessageRoundTrip.json");
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
