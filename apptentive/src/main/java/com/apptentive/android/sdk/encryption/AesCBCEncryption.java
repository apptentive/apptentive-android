package com.apptentive.android.sdk.encryption;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;

import static com.apptentive.android.sdk.util.ObjectUtils.isNullOrEmpty;

/**
 * AES CBC encryption implementation
 */
class AesCBCEncryption implements Encryption {

	private static final int CIPHER_CHUNK = 512;
	private static final int IV_LENGTH = 16;

	private final Key key;
	private final String transformation;
	private final SecureRandom secureRandom = new SecureRandom();

	public AesCBCEncryption(Key key, String transformation) {
		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (StringUtils.isNullOrEmpty(transformation)) {
			throw new IllegalArgumentException("Transformation is null or empty");
		}

		this.key = key;
		this.transformation = transformation;
	}

	@Override
	public @NonNull byte[] encrypt(@NonNull byte[] data) throws EncryptionException {
		try {
			if (isNullOrEmpty(data)) {
				return data;
			}

			final Cipher cipher = Cipher.getInstance(transformation);

			byte[] iv = new byte[IV_LENGTH];
			secureRandom.nextBytes(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			buffer.write(iv);

			CipherOutputStream stream = null;
			try {
				stream = new CipherOutputStream(buffer, cipher);

				int off = 0;
				while (off < data.length) {
					int len = Math.min(CIPHER_CHUNK, data.length - off);
					stream.write(data, off, len);
					off += len;
				}
			} finally {
				if (stream != null) {
					stream.close();
				}
			}

			return buffer.toByteArray();
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}

	@Override
	public @NonNull byte[] decrypt(@NonNull byte[] encryptedData) throws EncryptionException {
		try {
			if (isNullOrEmpty(encryptedData)) {
				return encryptedData;
			}

			ByteArrayInputStream input = new ByteArrayInputStream(encryptedData);

			byte[] iv = new byte[IV_LENGTH];
			if (input.read(iv) != IV_LENGTH) {
				throw new EncryptionException("Unable to read initialization vector");
			}

			final Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

			ByteArrayOutputStream output = new ByteArrayOutputStream();

			CipherInputStream stream = null;
			try {
				stream = new CipherInputStream(input, cipher);

				int bytesRead;
				byte[] temp = new byte[CIPHER_CHUNK];
				while ((bytesRead = stream.read(temp)) != -1) {
					output.write(temp, 0, bytesRead);
				}
			} finally {
				if (stream != null) {
					stream.close();
				}
			}

			return output.toByteArray();
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}
}
