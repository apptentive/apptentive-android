package com.apptentive.android.sdk.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.AtomicFile;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class EncryptionHelper {
	public static @Nullable byte[] encrypt(@NonNull Encryption encryption, @Nullable String value) throws EncryptionException {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		return value != null ? encryption.encrypt(value.getBytes()) : null;
	}

	public static @Nullable String decryptString(@NonNull Encryption encryption, @Nullable byte[] encryptedBytes) throws EncryptionException {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		byte[] decrypted = encryption.decrypt(encryptedBytes);
		return decrypted != null ? new String(decrypted) : null;
	}

	public static void writeToEncryptedFile(@NonNull Encryption encryption, @NonNull File file, @NonNull byte[] data) throws IOException, EncryptionException {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}

		AtomicFile atomicFile = new AtomicFile(file);
		FileOutputStream stream = null;
		boolean successful = false;
		try {
			stream = atomicFile.startWrite();
			stream.write(encryption.encrypt(data));
			atomicFile.finishWrite(stream);
			successful = true;
		} finally {
			if (!successful) {
				atomicFile.failWrite(stream);
			}
		}
	}

	public static byte[] readFromEncryptedFile(@NonNull Encryption encryption, @NonNull File file) throws IOException, EncryptionException {
		final byte[] bytes = Util.readBytes(file);
		return encryption.decrypt(bytes);
	}
}
