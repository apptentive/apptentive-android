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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

	private boolean listenerFired;

	@Test
	public void testListeners() {

		DataChangedListener listener = new DataChangedListener() {
			@Override
			public void onDataChanged() {
				listenerFired = true;
			}
		};

		SessionData sessionData = new SessionData();
		sessionData.setDataChangedListener(listener);
		assertFalse(listenerFired);

		sessionData.setConversationToken("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setConversationId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setPersonId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setPersonEmail("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setPersonName("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setLastSeenSdkVersion("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setMessageCenterFeatureUsed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setMessageCenterWhoCardPreviouslyDisplayed(true);
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setMessageCenterPendingMessage("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setMessageCenterPendingAttachments("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setInteractions("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setTargets("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setInteractionExpiration(1000L);
		assertTrue(listenerFired);
		listenerFired = false;


		sessionData.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().setIntegrationConfig(new IntegrationConfig());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().getIntegrationConfig().setAmazonAwsSns(new IntegrationConfigItem());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().setOsApiLevel(5);
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setLastSentDevice(new Device());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getLastSentDevice().setUuid("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		sessionData.setPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setEmail("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setName("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setFacebookId("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setPhoneNumber("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setStreet("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setCity("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setZip("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setCountry("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setBirthday("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().setCustomData(new CustomData());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().getCustomData().put("foo", "bar");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getPerson().getCustomData().remove("foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setLastSentPerson(new Person());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getLastSentPerson().setId("foo");
		assertTrue(listenerFired);
		listenerFired = false;


		sessionData.setSdk(new Sdk());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setAppRelease(new AppRelease());
		assertTrue(listenerFired);
		listenerFired = false;


		sessionData.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setVersionHistory(new VersionHistory());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getVersionHistory().updateVersionHistory(100D, 1, "1");
		assertTrue(listenerFired);
		listenerFired = false;


		sessionData.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.setEventData(new EventData());
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getEventData().storeEventForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;

		sessionData.getEventData().storeInteractionForCurrentAppVersion(100D, 10, "1.0", "foo");
		assertTrue(listenerFired);
		listenerFired = false;
	}

	// TODO: Add a test for verifying that setting an existing value doesn't fire listeners.
}