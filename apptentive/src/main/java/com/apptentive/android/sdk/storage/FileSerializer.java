/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.core.util.AtomicFile;

import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSerializer implements Serializer {

	private final File file;

	public FileSerializer(File file) {
		if (file == null) {
			throw new IllegalArgumentException("'file' is null");
		}
		this.file = file;
	}

	@Override
	public void serialize(Object object) throws SerializerException {
		file.getParentFile().mkdirs();

		AtomicFile atomicFile = new AtomicFile(file);
		FileOutputStream stream = null;
		try {
			stream = atomicFile.startWrite();
			serialize(stream, object);
			atomicFile.finishWrite(stream);
		} catch (Exception e) {
			atomicFile.failWrite(stream);
			throw new SerializerException(e);
		}

	}

	@Override
	public Object deserialize() throws SerializerException {
		return deserialize(file);
	}

	protected void serialize(FileOutputStream stream, Object object) throws Exception {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			stream.write(bos.toByteArray());
		} finally {
			Util.ensureClosed(oos);
		}
	}

	protected Object deserialize(File file) throws SerializerException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new OverrideSerialVersionUIDObjectInputStream(fis);
			return ois.readObject();
		} catch (Exception e) {
			throw new SerializerException(e);
		} finally {
			Util.ensureClosed(fis);
			Util.ensureClosed(ois);
		}
	}
}
