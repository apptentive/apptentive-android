/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import com.apptentive.android.sdk.TestCaseBase;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileMessageStoreTest extends TestCaseBase {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void addOrUpdateMessages() throws Exception {
		File file = getTempFile();
		FileMessageStore store = new FileMessageStore(file);
		store.addOrUpdateMessages(createMessage());
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

	private ApptentiveMessage createMessage() throws JSONException {
			return new CompoundMessage("{}", true);
	}

	private File getTempFile() throws IOException {
		return tempFolder.newFile("data.bin");
	}
}