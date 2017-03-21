/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSerializer implements Serializer {

	private File file;

	public FileSerializer(File file) {
		this.file = file;
	}

	@Override
	public void serialize(Object object) throws SerializerException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			file.getParentFile().mkdirs();
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			ApptentiveLog.v("Session data written to file of length: %s", Util.humanReadableByteCount(file.length(), false));
		} catch (Exception e) {
			throw new SerializerException(e);
		} finally {
			Util.ensureClosed(fos);
			Util.ensureClosed(oos);
		}
	}

	@Override
	public Object deserialize() throws SerializerException {
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
