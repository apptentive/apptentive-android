/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.encryption;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {

	private static final int IV_SIZE = 16;

	private SecretKeySpec key;

	public Encryptor(byte[] keyBytes) {
		this.key = new SecretKeySpec(keyBytes, "AES");
	}

	public byte[] encrypt(byte[] plainText) throws UnsupportedEncodingException,
	                                               NoSuchPaddingException,
	                                               NoSuchAlgorithmException,
	                                               IllegalBlockSizeException,
	                                               BadPaddingException,
	                                               InvalidAlgorithmParameterException,
	                                               InvalidKeyException {
		byte[] iv = new byte[IV_SIZE];
		new SecureRandom().nextBytes(iv);
		byte[] cipherText = encrypt(iv, plainText);
		byte[] ret = new byte[iv.length + cipherText.length];
		System.arraycopy(iv, 0, ret, 0, iv.length);
		System.arraycopy(cipherText, 0, ret, iv.length, cipherText.length);
		return ret;
	}

	public byte[] encrypt(byte[] iv, byte[] plainText) throws NoSuchAlgorithmException,
	                                                          NoSuchPaddingException,
	                                                          InvalidAlgorithmParameterException,
	                                                          InvalidKeyException,
	                                                          BadPaddingException,
	                                                          IllegalBlockSizeException {

		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
		return cipher.doFinal(plainText);
	}

	public byte[] decrypt(byte[] iv, byte[] cipherText) throws NoSuchPaddingException,
	                                                           NoSuchAlgorithmException,
	                                                           BadPaddingException,
	                                                           IllegalBlockSizeException,
	                                                           InvalidAlgorithmParameterException,
	                                                           InvalidKeyException {
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
		return cipher.doFinal(cipherText);
	}

	public byte[] decrypt(byte[] ivAndCipherText) throws NoSuchPaddingException,
	                                                     InvalidKeyException,
	                                                     NoSuchAlgorithmException,
	                                                     IllegalBlockSizeException,
	                                                     BadPaddingException,
	                                                     InvalidAlgorithmParameterException {
		byte[] iv = new byte[IV_SIZE];
		byte[] cipherText = new byte[ivAndCipherText.length - IV_SIZE];
		System.arraycopy(ivAndCipherText, 0, iv, 0, IV_SIZE);
		System.arraycopy(ivAndCipherText, IV_SIZE, cipherText, 0, ivAndCipherText.length - IV_SIZE);
		return decrypt(iv, cipherText);
	}
}
