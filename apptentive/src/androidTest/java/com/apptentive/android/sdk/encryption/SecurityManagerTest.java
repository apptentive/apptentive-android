package com.apptentive.android.sdk.encryption;

import com.apptentive.android.sdk.InstrumentationTestCaseBase;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class SecurityManagerTest extends InstrumentationTestCaseBase {
	private static final int TEST_DATA_SIZE = 8096;

	@Before
	public void setup() {
		SecurityManager.clear(getContext());
		SecurityManager.init(getContext(), true);
	}

	@Test
	public void testDataEncryptionDecryption() throws Exception {
		EncryptionKey key = SecurityManager.getMasterKey();

		// Set up the test data
		byte[] testData = new byte[TEST_DATA_SIZE];
		new Random().nextBytes(testData);

		byte[] cipherText = Encryptor.encrypt(key, testData);
		assertNotNull(cipherText);

		byte[] plainText = Encryptor.decrypt(key, cipherText);
		assertNotNull(plainText);

		assertTrue(Arrays.equals(plainText, testData));
	}

	@Test
	public void testStringEncryptionDecryption() throws Exception {
		EncryptionKey key = SecurityManager.getMasterKey();

		// Set up the test data
		String testData = "Test data";

		byte[] cipherText = Encryptor.encrypt(key, testData);
		assertNotNull(cipherText);

		String plainText = Encryptor.decryptString(key, cipherText);
		assertNotNull(plainText);

		assertEquals(plainText, testData);
	}
}