/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.conversation.ConversationData;
import com.apptentive.android.sdk.util.Util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class ConversationDataTest {

	@Rule
	public TemporaryFolder conversationFolder = new TemporaryFolder();

	@Test
	public void testSerialization() {
		ConversationData expected = new ConversationData();
		expected.setConversationId("jvnuveanesndndnadldbj");
		expected.setConversationToken("watgsiovncsagjmcneiusdolnfcs");
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
			ois = new OverrideSerialVersionUIDObjectInputStream(bais);

			ConversationData actual = (ConversationData) ois.readObject();
			assertEquals(expected.getConversationId(), actual.getConversationId());
			assertEquals(expected.getConversationToken(), actual.getConversationToken());
			assertEquals(expected.getLastSeenSdkVersion(), actual.getLastSeenSdkVersion());
			assertEquals(expected.isMessageCenterFeatureUsed(), actual.isMessageCenterFeatureUsed());
			assertEquals(expected.isMessageCenterWhoCardPreviouslyDisplayed(), actual.isMessageCenterWhoCardPreviouslyDisplayed());
			assertEquals(expected.getMessageCenterPendingMessage(), actual.getMessageCenterPendingMessage());
			assertEquals(expected.getMessageCenterPendingAttachments(), actual.getMessageCenterPendingAttachments());
			assertEquals(expected.getTargets(), actual.getTargets());
			assertEquals(expected.getInteractions(), actual.getInteractions());
			assertEquals(expected.getInteractionExpiration(), actual.getInteractionExpiration(), 0.000001);

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
		ConversationData data = new ConversationData();
		testConversationListeners(data);
	}

	@Test
	public void testDataChangeListenersWhenDeserialized() throws Exception {
		ConversationData data = new ConversationData();

		File conversationFile = new File(conversationFolder.getRoot(), "conversation.bin");
		new FileSerializer(conversationFile).serialize(data);

		data = (ConversationData) new FileSerializer(conversationFile).deserialize();
		testConversationListeners(data);
	}

	private void testConversationListeners(ConversationData data) {
		data.setDataChangedListener(new DataChangedListener() {
			@Override
			public void onDataChanged() {
				listenerFired = true;
			}
		});
		listenerFired = false;

		data.setConversationToken("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setConversationId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setLastSeenSdkVersion("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMessageCenterFeatureUsed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMessageCenterWhoCardPreviouslyDisplayed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMessageCenterPendingMessage("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMessageCenterPendingAttachments("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setInteractions("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setTargets("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setInteractionExpiration(1000L);
		assertTrue(listenerFired);
		listenerFired = false;


		data.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().setIntegrationConfig(new IntegrationConfig());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().setOsApiLevel(5);
		assertTrue(listenerFired);
		listenerFired = false;

		data.getDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setLastSentDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getLastSentDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		data.setPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setEmail("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setName("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setFacebookId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setPhoneNumber("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setStreet("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setCity("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setZip("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setCountry("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setBirthday("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().setCustomData(new CustomData());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getPerson().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setLastSentPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getLastSentPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		data.setSdk(new Sdk());
		assertTrue(listenerFired);
		listenerFired = false;

		data.setAppRelease(new AppRelease());
		assertTrue(listenerFired);
		listenerFired = false;


		data.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setVersionHistory(new VersionHistory());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setEventData(new EventData());
		assertTrue(listenerFired);
		listenerFired = false;

		data.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMParticleId("1234567890");
		assertTrue(listenerFired);
		listenerFired = false;

		data.setMParticleId("1234567890");
		assertFalse(listenerFired);
		listenerFired = false;
	}

	// TODO: Add a test for verifying that setting an existing value doesn't fire listeners.
}