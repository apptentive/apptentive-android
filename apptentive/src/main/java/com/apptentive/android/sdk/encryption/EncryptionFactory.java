package com.apptentive.android.sdk.encryption;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionKey.Transformation;

import static com.apptentive.android.sdk.util.ObjectUtils.isNullOrEmpty;

public class EncryptionFactory {
	public static final @NonNull Encryption NULL = new NullEncryption();

	public static @NonNull Encryption wrapNullSafe(@NonNull Encryption encryption) {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		return new NullSafeEncryption(encryption);
	}

	public static @NonNull Encryption createEncryption(String key, String transformation) {
		return createEncryption(new EncryptionKey(key, transformation));
	}

	public static @NonNull Encryption createEncryption(EncryptionKey key) {
		if (key.isNull()) {
			return NULL;
		}

		final Transformation transformation = Transformation.parse(key.getTransformation());

		String algorithm = transformation.algorithm;
		String mode = transformation.mode;

		if (algorithm.equals("AES") && mode.equals("CBC")) {
			return new AesCBCEncryption(key.getSecretKey(), key.getTransformation());
		}

		throw new IllegalArgumentException("Unsupported transformation: '" + transformation + "'");
	}

	/**
	 * No-op encryption implementation
	 */
	private static class NullEncryption implements Encryption {

		@Override
		public @NonNull byte[] encrypt(@NonNull byte[] data) {
			return data;
		}

		@Override
		public @NonNull byte[] decrypt(@NonNull byte[] data) {
			return data;
		}
	}

	/**
	 * Wrapper class for null-safe encryption/decryption operations.
	 */
	private static class NullSafeEncryption implements Encryption {
		private final Encryption target;

		private NullSafeEncryption(Encryption target) {
			if (target == null) {
				throw new IllegalArgumentException("Target is null");
			}
			this.target = target;
		}

		@Override
		public @NonNull byte[] encrypt(@NonNull byte[] data) throws EncryptionException {
			return isNullOrEmpty(data) ? data : target.encrypt(data);
		}

		@Override
		public @NonNull byte[] decrypt(@NonNull byte[] data) throws EncryptionException {
			return isNullOrEmpty(data) ? data : target.decrypt(data);
		}
	}
}
