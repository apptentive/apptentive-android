/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.text.TextUtils;

import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.util.Util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class ConversationTest {

	@Rule
	public TemporaryFolder conversationFolder = new TemporaryFolder();

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
		Conversation expected = new Conversation();
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
		 // TODO: Test nested objects as well
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

			Conversation result = (Conversation) ois.readObject();
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
			assertEquals(expected.getInteractionExpiration(), result.getInteractionExpiration(), 0.000001);

		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			Util.ensureClosed(baos);
			Util.ensureClosed(oos);
			Util.ensureClosed(bais);
			Util.ensureClosed(ois);
		}
	}

	private boolean listenerFired; // TODO: get rid of this field and make it test "local"

	@Test
	public void testDataChangeListeners() throws Exception {
		Conversation conversation = new Conversation();
		testConversationListeners(conversation);
	}

	@Test
	public void testDataChangeListenersWhenDeserialized() throws Exception {
		Conversation conversation = new Conversation();

		File conversationFile = new File(conversationFolder.getRoot(), "conversation.bin");
		new FileSerializer(conversationFile).serialize(conversation);

		conversation = (Conversation) new FileSerializer(conversationFile).deserialize();
		testConversationListeners(conversation);
	}

	private void testConversationListeners(Conversation conversation) {
		conversation.setDataChangedListener(new DataChangedListener() {
			@Override
			public void onDataChanged() {
				listenerFired = true;
			}
		});
		listenerFired = false;

		conversation.setConversationToken("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setConversationId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setPersonId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setPersonEmail("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setPersonName("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setLastSeenSdkVersion("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setMessageCenterFeatureUsed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setMessageCenterWhoCardPreviouslyDisplayed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setMessageCenterPendingMessage("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setMessageCenterPendingAttachments("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setInteractions("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setTargets("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setInteractionExpiration(1000L);
		assertTrue(listenerFired);
		listenerFired = false;


		conversation.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().setIntegrationConfig(new IntegrationConfig());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().setOsApiLevel(5);
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setLastSentDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getLastSentDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		conversation.setPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setEmail("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setName("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setFacebookId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setPhoneNumber("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setStreet("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setCity("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setZip("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setCountry("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setBirthday("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().setCustomData(new CustomData());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getPerson().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setLastSentPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getLastSentPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		conversation.setSdk(new Sdk());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setAppRelease(new AppRelease());
		assertTrue(listenerFired);
		listenerFired = false;


		conversation.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setVersionHistory(new VersionHistory());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.setEventData(new EventData());
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		conversation.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;
	}

	// TODO: Add a test for verifying that setting an existing value doesn't fire listeners.
}