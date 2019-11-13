package com.apptentive.android.sdk;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.encryption.EncryptionException;

/**
 * Represents and object for encrypting/decrypting on-device data storage.
 */
public interface Encryption {
	/**
	 * Encrypts an array of bytes
	 *
	 * @param data - raw data to encrypt
	 * @return an encrypted data
	 */
	@NonNull byte[] encrypt(@NonNull byte[] data) throws EncryptionException;

	/**
	 * Decrypts an array of bytes
	 *
	 * @param data - raw data to decrypt
	 * @return a decrypted data
	 */
	@NonNull byte[] decrypt(@NonNull byte[] data) throws EncryptionException;
}
