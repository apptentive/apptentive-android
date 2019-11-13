/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.test.InstrumentationRegistry;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.encryption.EncryptionFactory;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;
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
import java.util.UUID;

import static com.apptentive.android.sdk.model.ApptentiveMessage.State;
import static junit.framework.Assert.assertEquals;

public class FileMessageStoreTest extends TestCaseBase {
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String ENCRYPTION_KEY = "5C5361D08DA7AD6CD70ACEB572D387BB713A312DE8CE6128B8A42F62A7B381DB";

	private static final boolean READ = true;
	private static final boolean UNREAD = false;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	private Encryption encryption;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		ApptentiveInternal.setInstance(new ApptentiveInternal(InstrumentationRegistry.getTargetContext()));
		encryption = EncryptionFactory.createEncryption(ENCRYPTION_KEY, CIPHER_TRANSFORMATION);
	}

	@After
	public void tearDown() {
		super.tearDown();
		ApptentiveInternal.setInstance(null);
	}

	@Test
	public void testAddingAndLoadingMessages() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));

		// reload store and check saved messages
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'2','client_created_at':'20','state':'sent','read':'false'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");

		// reload the store again and add another message
		store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("4", State.sent, UNREAD, 40.0));
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'2','client_created_at':'20','state':'sent','read':'false'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}",
			"{'nonce':'4','client_created_at':'40','state':'sent','read':'false'}");
	}

	@Test
	public void updateMessage() throws Exception {

		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));

		// reload the store and change a single message
		store = new FileMessageStore(file, encryption);
		store.updateMessage(createMessage("2", State.saved, READ, 40.0));
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'2','client_created_at':'40','state':'saved','read':'true'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");


		// reload the store and check the stored messages
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'2','client_created_at':'40','state':'saved','read':'true'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");
	}

	@Test
	public void getLastReceivedMessageId() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.saved, READ, 10.0, "111"));
		store.addOrUpdateMessages(createMessage("2", State.saved, UNREAD, 20.0, "222"));
		store.addOrUpdateMessages(createMessage("3", State.sending, READ, 30.0, "333"));
		store.addOrUpdateMessages(createMessage("4", State.sent, UNREAD, 40.0, "444"));

		assertEquals("222", store.getLastReceivedMessageId());

		// reload the store and check again
		store = new FileMessageStore(file, encryption);
		assertEquals("222", store.getLastReceivedMessageId());
	}

	@Test
	public void getUnreadMessageCount() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));
		store.addOrUpdateMessages(createMessage("4", State.sending, UNREAD, 40.0));

		assertEquals(2, store.getUnreadMessageCount());

		// reload store and check saved messages
		store = new FileMessageStore(file, encryption);
		assertEquals(2, store.getUnreadMessageCount());
	}

	@Test
	public void deleteAllMessages() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));
		store.addOrUpdateMessages(createMessage("4", State.sending, UNREAD, 40.0));

		// delete all messages
		store.deleteAllMessages();

		// check stored messages
		addResult(store.getAllMessages());
		assertResult();

		// reload the store and check for messages
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());
		assertResult();
	}

	@Test
	public void deleteAllMessagesAfterReload() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));
		store.addOrUpdateMessages(createMessage("4", State.sending, UNREAD, 40.0));

		// delete all messages
		store.deleteAllMessages();

		// reload the store and check for messages
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());
		assertResult();
	}

	@Test
	public void deleteMessage() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));
		store.addOrUpdateMessages(createMessage("4", State.sending, UNREAD, 40.0));

		store.deleteMessage("2");
		store.deleteMessage("4");

		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");
	}

	@Test
	public void deleteMessageAndReload() throws Exception {
		File file = getTempFile();

		// create a few messages and add them to the store
		FileMessageStore store = new FileMessageStore(file, encryption);
		store.addOrUpdateMessages(createMessage("1", State.sending, READ, 10.0));
		store.addOrUpdateMessages(createMessage("2", State.sent, UNREAD, 20.0));
		store.addOrUpdateMessages(createMessage("3", State.saved, READ, 30.0));
		store.addOrUpdateMessages(createMessage("4", State.sending, UNREAD, 40.0));

		store.deleteMessage("2");
		store.deleteMessage("4");

		// reload store
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());

		assertResult(
			"{'nonce':'1','client_created_at':'10','state':'sending','read':'true'}",
			"{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");

		// delete more
		store.deleteMessage("1");
		addResult(store.getAllMessages());

		assertResult("{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");

		// reload store
		store = new FileMessageStore(file, encryption);
		addResult(store.getAllMessages());

		assertResult("{'nonce':'3','client_created_at':'30','state':'saved','read':'true'}");
	}

	private ApptentiveMessage createMessage(String nonce, State state, boolean read, double clientCreatedAt) throws JSONException {
		return createMessage(nonce, state, read, clientCreatedAt, UUID.randomUUID().toString());
	}

	private ApptentiveMessage createMessage(String nonce, State state, boolean read, double clientCreatedAt, String id) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("nonce", nonce);
		object.put("client_created_at", clientCreatedAt);
		CompoundMessage message = new CompoundMessage(object.toString());
		message.setId(id);
		message.setState(state);
		message.setRead(read);
		message.setNonce(nonce);
		return message;
	}

	private File getTempFile() throws IOException {
		final File file = tempFolder.newFile();
		file.delete(); // this file might exist
		return file;
	}

	private void addResult(List<ApptentiveMessage> messages) throws JSONException {
		for (ApptentiveMessage message : messages) {
			addResult(toString(message));
		}
	}

	private String toString(ApptentiveMessage message) throws JSONException {
		String result = "{";
		final Iterator<String> keys = message.getJsonObject().keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equals("id")) { // 'id' is randomly generated each time (so don't test it)
				continue;
			}
			if (key.equals("type")) { // it's always 'CompoundMessage'
				continue;
			}
			result += StringUtils.format("'%s':'%s',", key, message.getJsonObject().get(key));
		}

		result += StringUtils.format("'state':'%s',", message.getState().name());
		result += StringUtils.format("'read':'%s'", message.isRead());
		result += "}";
		return result;
	}
}