/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.text.TextUtils;

import com.apptentive.android.sdk.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class SessionDataTest {

	@Before
	public void setup() {
		PowerMockito.mockStatic(TextUtils.class);
		PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				return !(a != null && a.length() > 0);
			}
		});
	}

	@Test
	public void testSerialization() {
		SessionData expected = new SessionData();
		expected.setConversationId("jvnuveanesndndnadldbj");
		expected.setConversationToken("watgsiovncsagjmcneiusdolnfcs");
		expected.setPersonId("sijngmkmvewsnblkfmsd");
		expected.setPersonEmail("nvuewnfaslvbgflkanbx");
		expected.setPersonName("fslkgkdsnbnvwasdibncksd");
		expected.setLastSeenSdkVersion("mdvnjfuoivsknbjgfaoskdl");
		expected.setMessageCenterFeatureUsed(true);
		expected.setMessageCenterWhoCardPreviouslyDisplayed(false);
		expected.setMessageCenterPendingMessage("`~!@#$%^&*(_+{}:\"'<>?!@#$%^&*()_+{}|:<>?");
		expected.setMessageCenterPendingAttachments("NVBUOIVKNBGFANWKSLBJK");
		expected.setTargets("MNCIUFIENVBFKDV");
		expected.setInteractions("nkjvdfikjbffasldnbnfldfmfd");
		expected.setInteractionExpiration(1234567894567890345L);

		/*
		 // TODO: Test these
		 private Device device;
		 private Device lastSentDevice;
		 private Person person;
		 private Person lastSentPerson;
		 private Sdk sdk;
		 private AppRelease appRelease;
		 private EventData eventData;
		 private VersionHistory versionHistory;
		 */


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
			assertEquals(expected.getPersonId(), result.getPersonId());
			assertEquals(expected.getPersonName(), result.getPersonName());
			assertEquals(expected.getPersonEmail(), result.getPersonEmail());
			assertEquals(expected.getLastSeenSdkVersion(), result.getLastSeenSdkVersion());
			assertEquals(expected.isMessageCenterFeatureUsed(), result.isMessageCenterFeatureUsed());
			assertEquals(expected.isMessageCenterWhoCardPreviouslyDisplayed(), result.isMessageCenterWhoCardPreviouslyDisplayed());
			assertEquals(expected.getMessageCenterPendingMessage(), result.getMessageCenterPendingMessage());
			assertEquals(expected.getMessageCenterPendingAttachments(), result.getMessageCenterPendingAttachments());
			assertEquals(expected.getTargets(), result.getTargets());
			assertEquals(expected.getInteractions(), result.getInteractions());
			assertEquals(expected.getInteractionExpiration(), result.getInteractionExpiration());

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