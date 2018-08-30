/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.encryption;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.apptentive.android.sdk.encryption.EncryptionKey.DEFAULT_TRANSFORMATION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EncryptorTest {

	private static final int TEST_DATA_SIZE = 8096;

	@Test
	public void testRoundTripEncryption() throws Exception {
		// Generate a key and setup the crypto
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey secretKey = keyGen.generateKey();

		// Set up the test data
		byte[] testData = new byte[TEST_DATA_SIZE];
		new Random().nextBytes(testData);

		long start = System.currentTimeMillis();
		EncryptionKey key = new EncryptionKey(secretKey, DEFAULT_TRANSFORMATION);
		byte[] cipherText = Encryptor.encrypt(key, testData);
		assertNotNull(cipherText);
		byte[] plainText = Encryptor.decrypt(key, cipherText);
		long stop = System.currentTimeMillis();
		System.out.println(String.format("Round trip encryption took: %dms", stop - start));
		assertNotNull(plainText);
		assertTrue(Arrays.equals(plainText, testData));
	}
}