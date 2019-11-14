package com.apptentive.android.sdk.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.util.StringUtils;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class EncryptionKey {
	/**
	 * A no-op encryption key for API versions without key chain access (17 and below)
	 */
	public static final EncryptionKey NULL = new EncryptionKey();

	private final Key key;
	private final String transformation;

	private EncryptionKey() {
		this.key = null;
		this.transformation = null;
	}

	public EncryptionKey(@NonNull Key key, @NonNull String transformation) {
		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (StringUtils.isNullOrEmpty(transformation)) {
			throw new IllegalArgumentException("Cipher transformation is null or empty");
		}

		this.key = key;
		this.transformation = transformation;
	}

	public EncryptionKey(@NonNull String hexKey, @NonNull String transformation) {
		if (StringUtils.isNullOrEmpty(hexKey)) {
			throw new IllegalArgumentException("Hex key is null or empty");
		}
		String algorithm = Transformation.parse(transformation).algorithm;
		this.key = new SecretKeySpec(StringUtils.hexToBytes(hexKey), algorithm);
		this.transformation = transformation;
		;
	}

	boolean isNull() {
		return key == null;
	}

	@Nullable Key getSecretKey() {
		return key;
	}

	@NonNull String getTransformation() {
		return transformation;
	}

	//region Helpers

	public static final class Transformation {
		public final String algorithm;
		public final String mode;
		public final String padding;

		public Transformation(String algorithm, String mode, String padding) {
			this.algorithm = algorithm;
			this.mode = mode;
			this.padding = padding;
		}

		public static @NonNull Transformation parse(@NonNull String transformation) {
			if (StringUtils.isNullOrEmpty(transformation)) {
				throw new IllegalArgumentException("Transformation is null or empty");
			}

			String[] tokens = transformation.split("/");
			if (tokens.length != 3) {
				throw new IllegalStateException("Invalid transformation: '" + transformation + "'");
			}

			String algorithm = tokens[0].toUpperCase();
			String mode = tokens[1].toUpperCase();
			String padding = tokens[2].toUpperCase();
			return new Transformation(algorithm, mode, padding);
		}
	}

	//endregion
}
