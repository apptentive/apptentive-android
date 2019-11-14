package com.apptentive.android.sdk.serialization;

import androidx.annotation.NonNull;
import androidx.core.util.AtomicFile;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Helper class for a binary file-based object serialization.
 */
public class ObjectSerialization {
	/**
	 * Writes an object ot a file
	 */
	public static void serialize(File file, SerializableObject object) throws IOException {
		AtomicFile atomicFile = new AtomicFile(file);
		FileOutputStream stream = null;
		try {
			stream = atomicFile.startWrite();
			DataOutputStream out = new DataOutputStream(stream);
			object.writeExternal(out);
			atomicFile.finishWrite(stream); // serialization was successful
		} catch (Exception e) {
			atomicFile.failWrite(stream); // serialization failed
			throw new IOException(e); // throw exception up the chain
		}
	}

	/**
	 * Writes an object ot an encrypted file
	 */
	public static void serialize(File file, SerializableObject object, @NonNull Encryption encryption) throws IOException {
		ByteArrayOutputStream bos = null;
		DataOutputStream dos = null;
		try {
			bos = new ByteArrayOutputStream();
			dos = new DataOutputStream(bos);
			object.writeExternal(dos);
			final byte[] unencryptedBytes = bos.toByteArray();
			final byte[] encryptedBytes = encryption.encrypt(unencryptedBytes);
			Util.writeAtomically(file, encryptedBytes);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			Util.ensureClosed(bos);
			Util.ensureClosed(dos);
		}
	}

	/**
	 * Reads an object from a file
	 */
	public static <T extends SerializableObject> T deserialize(File file, Class<T> cls) throws IOException {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(stream);

			try {
				Constructor<T> constructor = cls.getDeclaredConstructor(DataInput.class);
				constructor.setAccessible(true);
				return constructor.newInstance(in);
			} catch (Exception e) {
				throw new IOException("Unable to instantiate class: " + cls, e);
			}
		} finally {
			Util.ensureClosed(stream);
		}
	}

	public static <T extends SerializableObject> T deserialize(File file, Class<T> cls, Encryption encryption) throws IOException {
		try {
			final byte[] encryptedBytes = Util.readBytes(file);
			final byte[] unencryptedBytes = encryption.decrypt(encryptedBytes);

			ByteArrayInputStream stream = null;
			try {
				stream = new ByteArrayInputStream(unencryptedBytes);
				DataInputStream in = new DataInputStream(stream);
				Constructor<T> constructor = cls.getDeclaredConstructor(DataInput.class);
				constructor.setAccessible(true);
				return constructor.newInstance(in);
			} finally {
				Util.ensureClosed(stream);
			}
		} catch (Exception e) {
			throw new IOException("Unable to instantiate class: " + cls, e);
		}
	}
}
