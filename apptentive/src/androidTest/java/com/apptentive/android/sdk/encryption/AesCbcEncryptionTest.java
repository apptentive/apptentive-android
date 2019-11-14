package com.apptentive.android.sdk.encryption;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Encryption;

import org.junit.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.apptentive.android.sdk.util.ObjectUtils.isNullOrEmpty;
import static org.junit.Assert.*;

public class AesCbcEncryptionTest {

	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";

	@Test
	public void testEncryptDecryptNullData() throws Exception {
		testData(null);
	}

	@Test
	public void testEncryptDecryptEmptyData() throws Exception {
		testData(0);
	}

	@Test
	public void testEncryptDecryptSmallData() throws Exception {
		testData(10);
	}

	@Test
	public void testEncryptDecryptMediumData1() throws Exception {
		testData(512);
	}

	@Test
	public void testEncryptDecryptMediumData2() throws Exception {
		testData(512 + 100);
	}

	@Test
	public void testEncryptDecryptLargeData1() throws Exception {
		testData(512 * 4);
	}

	@Test
	public void testEncryptDecryptLargeData2() throws Exception {
		testData(512 * 4 + 100);
	}

	@Test
	public void testEncryptDecryptExtraLargeData1() throws Exception {
		testData(512 * 16);
	}


	@Test
	public void testEncryptDecryptExtraLargeData2() throws Exception {
		testData(512 * 16 + 100);
	}

	@Test
	public void testEncryptDecryptExtraExtraLargeData() throws Exception {
		testData(512 * 160 + 100);
	}

	private void testData(int dataSize) throws EncryptionException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {

		byte[] testData = new byte[dataSize];
		new Random().nextBytes(testData);

		testData(testData);
	}

	private void testData(byte[] testData) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, EncryptionException {
		Key key = generateEncryptionKey();
		Encryption encryption = new AesCBCEncryption(key, CIPHER_TRANSFORMATION);

		byte[] encrypted = encryption.encrypt(testData);
		if (!isNullOrEmpty(testData)) {
			assertFalse(Arrays.equals(testData, encrypted));
		}

		byte[] decrypted = encryption.decrypt(encrypted);
		assertArrayEquals(testData, decrypted);
	}

	private SecretKey generateEncryptionKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
		keyGenerator.init(new KeyGenParameterSpec.Builder("my-key", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
			                  .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
			                  .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
			                  .setRandomizedEncryptionRequired(false) // we need that to make our custom IV work
			                  .build());

		return keyGenerator.generateKey();
	}
}