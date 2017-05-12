/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.util.Util;

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
		serialize(file, object);
	}

	@Override
	public Object deserialize() throws SerializerException {
		return deserialize(file);
	}

	protected void serialize(File file, Object object) throws SerializerException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} catch (Exception e) {
			throw new SerializerException(e);
		} finally {
			Util.ensureClosed(fos);
			Util.ensureClosed(oos);
		}
	}

	protected Object deserialize(File file) throws SerializerException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return ois.readObject();
		} catch (Exception e) {
			throw new SerializerException(e);
		} finally {
			Util.ensureClosed(fis);
			Util.ensureClosed(ois);
		}
	}
}
