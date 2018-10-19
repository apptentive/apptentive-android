/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.encryption;

import android.support.annotation.Nullable;
import android.support.v4.util.AtomicFile;

import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class Encryptor {
	/**
	 * Initialization vector size
	 */
	private static final int IV_SIZE = 16;

	//region Encryption

	public static @Nullable byte[] encrypt(EncryptionKey encryptionKey, @Nullable String value) throws NoSuchPaddingException,
	                                                                                                   InvalidKeyException,
	                                                                                                   NoSuchAlgorithmException,
	                                                                                                   IllegalBlockSizeException,
	                                                                                                   BadPaddingException,
	                                                                                                   InvalidAlgorithmParameterException,
	                                                                                                   EncryptionException {
		return value != null ? encrypt(encryptionKey, value.getBytes()) : null;
	}

	public static @Nullable byte[] encrypt(EncryptionKey key, @Nullable byte[] plainText) throws NoSuchPaddingException,
	                                                                                             NoSuchAlgorithmException,
	                                                                                             IllegalBlockSizeException,
	                                                                                             BadPaddingException,
	                                                                                             InvalidAlgorithmParameterException,
	                                                                                             InvalidKeyException,
	                                                                                             EncryptionException {
		if (key == null) {
			throw new IllegalArgumentException("Encryption key is null");
		}

		if (key.isCorrupted()) {
			throw new EncryptionException("Can't encrypt data: key is corrupted");
		}

		if (plainText == null || key.isNull()) {
			return plainText;
		}

		byte[] iv = new byte[IV_SIZE];
		new SecureRandom().nextBytes(iv);
		byte[] cipherText = encrypt(key, plainText, iv);
		byte[] ret = new byte[iv.length + cipherText.length];
		System.arraycopy(iv, 0, ret, 0, iv.length);
		System.arraycopy(cipherText, 0, ret, iv.length, cipherText.length);
		return ret;
	}

	private static byte[] encrypt(EncryptionKey key, byte[] plainText, byte[] iv) throws NoSuchAlgorithmException,
	                                                                                     NoSuchPaddingException,
	                                                                                     InvalidAlgorithmParameterException,
	                                                                                     InvalidKeyException,
	                                                                                     BadPaddingException,
	                                                                                     IllegalBlockSizeException {

		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance(key.getTransformation());
		cipher.init(Cipher.ENCRYPT_MODE, key.getSecretKey(), ivParameterSpec);
		return cipher.doFinal(plainText);
	}

	//endregion

	//region Decrypt

	public static @Nullable String decryptString(EncryptionKey encryptionKey, @Nullable byte[] encryptedBytes) throws NoSuchPaddingException,
	                                                                                                                  InvalidAlgorithmParameterException,
	                                                                                                                  NoSuchAlgorithmException,
	                                                                                                                  IllegalBlockSizeException,
	                                                                                                                  BadPaddingException,
	                                                                                                                  InvalidKeyException,
	                                                                                                                  EncryptionException {
		byte[] decrypted = decrypt(encryptionKey, encryptedBytes);
		return decrypted != null ? new String(decrypted) : null;
	}

	public static @Nullable byte[] decrypt(EncryptionKey key, @Nullable byte[] ivAndCipherText) throws NoSuchPaddingException,
	                                                                                                   InvalidKeyException,
	                                                                                                   NoSuchAlgorithmException,
	                                                                                                   IllegalBlockSizeException,
	                                                                                                   BadPaddingException,
	                                                                                                   InvalidAlgorithmParameterException,
	                                                                                                   EncryptionException {
		if (key == null) {
			throw new IllegalArgumentException("Encryption key is null");
		}

		if (key.isCorrupted()) {
			throw new EncryptionException("Can't decrypt data: key is corrupted");
		}

		if (ivAndCipherText == null || key.isNull()) {
			return ivAndCipherText;
		}


		byte[] iv = new byte[IV_SIZE];
		byte[] cipherText = new byte[ivAndCipherText.length - IV_SIZE];
		System.arraycopy(ivAndCipherText, 0, iv, 0, IV_SIZE);
		System.arraycopy(ivAndCipherText, IV_SIZE, cipherText, 0, ivAndCipherText.length - IV_SIZE);
		return decrypt(key, cipherText, iv);
	}

	private static byte[] decrypt(EncryptionKey key, byte[] cipherText, byte[] iv) throws NoSuchPaddingException,
		                                                                                      NoSuchAlgorithmException,
		                                                                                      BadPaddingException,
		                                                                                      IllegalBlockSizeException,
		                                                                                      InvalidAlgorithmParameterException,
		                                                                                      InvalidKeyException {
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance(key.getTransformation());
		cipher.init(Cipher.DECRYPT_MODE, key.getSecretKey(), ivParameterSpec);
		return cipher.doFinal(cipherText);
	}

	//endregion

	//region File IO

	public static void writeToEncryptedFile(EncryptionKey encryptionKey, File file, byte[] data) throws IOException,
	                                                                                                    NoSuchPaddingException,
	                                                                                                    InvalidAlgorithmParameterException,
	                                                                                                    NoSuchAlgorithmException,
	                                                                                                    IllegalBlockSizeException,
	                                                                                                    BadPaddingException,
	                                                                                                    InvalidKeyException,
	                                                                                                    EncryptionException {
		AtomicFile atomicFile = new AtomicFile(file);
		FileOutputStream stream = null;
		boolean successful = false;
		try {
			stream = atomicFile.startWrite();
			stream.write(encrypt(encryptionKey, data));
			atomicFile.finishWrite(stream);
			successful = true;
		} finally {
			if (!successful) {
				atomicFile.failWrite(stream);
			}
		}
	}

	public static byte[] readFromEncryptedFile(EncryptionKey encryptionKey, File file) throws IOException,
	                                                                                          NoSuchPaddingException,
	                                                                                          InvalidKeyException,
	                                                                                          NoSuchAlgorithmException,
	                                                                                          IllegalBlockSizeException,
	                                                                                          BadPaddingException,
	                                                                                          InvalidAlgorithmParameterException,
	                                                                                          EncryptionException {
		final byte[] bytes = Util.readBytes(file);
		return decrypt(encryptionKey, bytes);
	}

	//endregion
}
