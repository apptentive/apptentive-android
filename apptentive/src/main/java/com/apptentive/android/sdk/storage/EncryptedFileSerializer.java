/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EncryptedFileSerializer extends FileSerializer {
	private final Encryption encryption;

	public EncryptedFileSerializer(File file, Encryption encryption) {
		super(file);

		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null or empty");
		}

		this.encryption = encryption;
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
			final byte[] encryptedBytes = encryption.encrypt(unencryptedBytes);
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
			final byte[] unencryptedBytes = encryption.decrypt(encryptedBytes);

			ByteArrayInputStream bis = null;
			ObjectInputStream ois = null;
			try {
				bis = new ByteArrayInputStream(unencryptedBytes);
				ois = new OverrideSerialVersionUIDObjectInputStream(bis);
				return ois.readObject();
			} finally {
				Util.ensureClosed(bis);
				Util.ensureClosed(ois);
			}
		} catch (Exception e) {
			throw new SerializerException(e);
		}
	}
}
