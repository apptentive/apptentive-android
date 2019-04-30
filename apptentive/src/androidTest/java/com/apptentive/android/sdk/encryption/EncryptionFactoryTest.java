package com.apptentive.android.sdk.encryption;

import com.apptentive.android.sdk.Encryption;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class EncryptionFactoryTest {
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String ENCRYPTION_KEY = "5C5361D08DA7AD6CD70ACEB572D387BB713A312DE8CE6128B8A42F62A7B381DB";

	@Test
	public void testEncryption() throws EncryptionException {
		Encryption encryption = EncryptionFactory.createEncryption(ENCRYPTION_KEY, CIPHER_TRANSFORMATION);
		byte[] data = {1, 2, 3};
		byte[] encrypted = encryption.encrypt(data);

		assertFalse(Arrays.equals(data, encrypted));

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}

	@Test
	public void testNullEncryption() throws EncryptionException {
		EncryptionKey key = EncryptionKey.NULL;
		Encryption encryption = EncryptionFactory.createEncryption(key);
		byte[] data = {1, 2, 3};
		byte[] encrypted = encryption.encrypt(data);
		assertArrayEquals(data, encrypted);

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}
}