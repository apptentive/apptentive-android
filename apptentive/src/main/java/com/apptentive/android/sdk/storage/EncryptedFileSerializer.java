/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.encryption.Encryptor;
import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EncryptedFileSerializer extends FileSerializer {
	private final EncryptionKey encryptionKey;

	public EncryptedFileSerializer(File file, EncryptionKey encryptionKey) {
		super(file);

		if (encryptionKey == null) {
			throw new IllegalArgumentException("'encryptionKey' is null or empty");
		}

		this.encryptionKey = encryptionKey;
	}

	@Override
	protected void serialize(FileOutputStream stream, Object object) throws Exception {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			final byte[] unencryptedBytes = bos.toByteArray();
			final byte[] encryptedBytes = Encryptor.encrypt(encryptionKey, unencryptedBytes);
			stream.write(encryptedBytes); // TODO: should we write using a buffer?
		} finally {
			Util.ensureClosed(bos);
			Util.ensureClosed(oos);
		}
	}

	@Override
	protected Object deserialize(File file) throws SerializerException {
		try {
			final byte[] encryptedBytes = Util.readBytes(file);
			final byte[] unencryptedBytes = Encryptor.decrypt(encryptionKey, encryptedBytes);

			ByteArrayInputStream bis = null;
			ObjectInputStream ois = null;
			try {
				bis = new ByteArrayInputStream(unencryptedBytes);
				ois = new ObjectInputStream(bis);
				return ois.readObject();
			} catch (Exception e) {
				throw new SerializerException(e);
			} finally {
				Util.ensureClosed(bis);
				Util.ensureClosed(ois);
			}
		} catch (Exception e) {
			throw new SerializerException(e);
		}
	}
}
