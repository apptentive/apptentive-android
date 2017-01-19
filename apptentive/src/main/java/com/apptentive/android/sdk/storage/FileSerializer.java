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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileSerializer implements Serializer {

	private File file;

	public FileSerializer(File file) {
		this.file = file;
	}

	@Override
	public void serialize(Object object) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} catch (IOException e) {
			ApptentiveLog.e("Error", e);
		} finally {
			Util.ensureClosed(fos);
			Util.ensureClosed(oos);
		}
	}

	@Override
	public Object deserialize() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return ois.readObject();
		} catch (ClassNotFoundException e) {
			ApptentiveLog.e("Error", e);
		} catch (FileNotFoundException e) {
			ApptentiveLog.e("DataSession file does not yet exist.");
		} catch (IOException e) {
			ApptentiveLog.e("Error", e);
		} finally {
			Util.ensureClosed(fis);
			Util.ensureClosed(ois);
		}
		return null;
	}
}
