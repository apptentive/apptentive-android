/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.util.Util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SessionDataTest {

	@Test
	public void testSerialization() {
		String conversationId = "jvnuveanesndndnadldbj";
		String conversationToken = "watgsiovncsagjmcneiusdolnfcs";

		SessionData expected = new SessionData();
		expected.setConversationId(conversationId);
		expected.setConversationToken(conversationToken);

		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(expected);

			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			SessionData result = (SessionData) ois.readObject();
			assertEquals(expected.getConversationId(), result.getConversationId());
			assertEquals(expected.getConversationToken(), result.getConversationToken());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			Util.ensureClosed(baos);
			Util.ensureClosed(oos);
			Util.ensureClosed(bais);
			Util.ensureClosed(ois);
		}


	}
}