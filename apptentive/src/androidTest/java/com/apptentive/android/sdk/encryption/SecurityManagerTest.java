package com.apptentive.android.sdk.encryption;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.InstrumentationTestCaseBase;

import org.junit.Before;
import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.Assert.*;

public class SecurityManagerTest extends InstrumentationTestCaseBase {

	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";

	@Before
	public void setup() {
		SecurityManager.clear(getContext());
	}

	@Test
	public void testNoEncryption() throws Exception {
		byte[] data = new byte[4096];
		new Random().nextBytes(data);

		Encryption encryption = SecurityManager.getEncryption(getContext(), null, false);
		byte[] encrypted = encryption.encrypt(data);
		assertArrayEquals(data, encrypted);

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}

	@Test
	public void testEncryption() throws Exception {
		byte[] data = new byte[4096];
		new Random().nextBytes(data);

		Encryption encryption = SecurityManager.getEncryption(getContext(), null, true);
		byte[] encrypted = encryption.encrypt(data);
		assertFalse(Arrays.equals(data, encrypted));
		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}


	@Test
	public void testCustomEncryption() throws Exception {
		byte[] data = new byte[4096];
		new Random().nextBytes(data);

		Encryption customEncryption = new AesCBCEncryption(generateEncryptionKey(), CIPHER_TRANSFORMATION);

		Encryption encryption = SecurityManager.getEncryption(getContext(), customEncryption, true);
		byte[] encrypted = encryption.encrypt(data);

		assertFalse(Arrays.equals(data, encrypted));

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}

	@Test
	public void testCustomEncryptionWithNullData() throws Exception {
		byte[] data = null;

		Encryption encryption = SecurityManager.getEncryption(getContext(), FAILURE_ENCRYPTION, false);
		byte[] encrypted = encryption.encrypt(data);
		assertArrayEquals(data, encrypted);

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}

	@Test
	public void testCustomEncryptionWithEmptyData() throws Exception {
		byte[] data = {};

		Encryption encryption = SecurityManager.getEncryption(getContext(), FAILURE_ENCRYPTION, false);
		byte[] encrypted = encryption.encrypt(data);
		assertArrayEquals(data, encrypted);

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(data, decrypted);
	}

	private SecretKey generateEncryptionKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
		keyGenerator.init(new KeyGenParameterSpec.Builder("custom-key-" + UUID.randomUUID(), KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
			                  .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
			                  .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
			                  .setRandomizedEncryptionRequired(false) // we need that to make our custom IV work
			                  .build());

		return keyGenerator.generateKey();
	}

	private static final Encryption FAILURE_ENCRYPTION = new Encryption() {
		@Override
		public @NonNull byte[] encrypt(@NonNull byte[] data) {
			throw new AssertionError("Should not try to encrypt");
		}

		@Override
		public @NonNull byte[] decrypt(@NonNull byte[] data) {
			throw new AssertionError("Should not try to decrypt");
		}
	};
}