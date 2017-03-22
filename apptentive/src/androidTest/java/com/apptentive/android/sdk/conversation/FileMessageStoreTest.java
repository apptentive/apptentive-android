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
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage.State;
import static junit.framework.Assert.assertEquals;

public class FileMessageStoreTest extends TestCaseBase {
	private static final boolean READ = true;
	private static final boolean UNREAD = false;

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
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));

		store = new FileMessageStore(file);
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'2','client_created_at':'20','state':'sent','read':'false'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");
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

	private ApptentiveMessage createMessage(String nonce, State state, boolean read, double clientCreatedAt) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("nonce", nonce);
		object.put("client_created_at", clientCreatedAt);
		CompoundMessage message = new CompoundMessage(object.toString(), true);
		message.setState(state);
		message.setRead(read);
		return message;
	}

	private File getTempFile() throws IOException {
		return tempFolder.newFile("data.bin");
	}

	private void addResult(List<ApptentiveMessage> messages) throws JSONException {
		for (ApptentiveMessage message : messages) {
			addResult(toString(message));
		}
	}

	private String toString(ApptentiveMessage message) throws JSONException {
		String result = "{";
		final Iterator<String> keys = message.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			result += StringUtils.format("'%s':'%s',", key, message.get(key));
		}

		result += StringUtils.format("'state':'%s',", message.getState().name());
		result += StringUtils.format("'read':'%s'", message.isRead());
		result += "}";

		return result;
	}
}