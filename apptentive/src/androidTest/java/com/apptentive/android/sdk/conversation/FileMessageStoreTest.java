/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveInternalMock;
import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class FileMessageStoreTest extends TestCaseBase {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		super.setUp();
		ApptentiveInternal.setInstance(new ApptentiveInternalMock(), true);
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testAddingAndLoadingMessages() throws Exception {
		File file = getTempFile();
		FileMessageStore store = new FileMessageStore(file);
		store.addOrUpdateMessages(createMessage("1"));
		store.addOrUpdateMessages(createMessage("2"));
		store.addOrUpdateMessages(createMessage("3"));

		store = new FileMessageStore(file);
		assertEquals("1,2,3", toString(store.getAllMessages()));
	}

	@Test
	public void updateMessage() throws Exception {

	}

	@Test
	public void getAllMessages() throws Exception {

	}

	@Test
	public void getLastReceivedMessageId() throws Exception {

	}

	@Test
	public void getUnreadMessageCount() throws Exception {

	}

	@Test
	public void deleteAllMessages() throws Exception {

	}

	@Test
	public void deleteMessage() throws Exception {

	}

	private ApptentiveMessage createMessage(String nonce) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("nonce", nonce);
		object.put("client_created_at", 0.0);
		return new CompoundMessage(object.toString(), true);
	}

	private File getTempFile() throws IOException {
		return tempFolder.newFile("data.bin");
	}

	private String toString(List<ApptentiveMessage> messages) {
		String result = "";
		for (ApptentiveMessage message : messages) {
			if (result.length() > 0) result += ",";
			result += message.getNonce();
		}
		return result;
	}
}