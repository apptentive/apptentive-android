package com.apptentive.android.sdk.encryption;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.apptentive.android.sdk.util.StringUtils;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class EncryptionKey {
	public static final EncryptionKey NULL = new EncryptionKey();

	static final String DEFAULT_TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM = "AES";

	private final Key key;
	private final String hexKey;
	private final String transformation;

	public EncryptionKey(@NonNull Key key, @NonNull String transformation) {
		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}
		if (StringUtils.isNullOrEmpty(transformation)) {
			throw new IllegalArgumentException("Cipher transformation is null or empty");
		}

		this.key = key;
		this.transformation = transformation;
		this.hexKey = null;
	}

	public EncryptionKey(@NonNull String hexKey) {
		if (StringUtils.isNullOrEmpty(hexKey)) {
			throw new IllegalArgumentException("Hex key is null or empty");
		}
		this.key = new SecretKeySpec(StringUtils.hexToBytes(hexKey), ALGORITHM);
		this.transformation = DEFAULT_TRANSFORMATION;
		this.hexKey = hexKey;
	}

	private EncryptionKey() {
		this.key = null;
		this.hexKey = null;
		this.transformation = "";
	}

	public boolean isNull() {
		return key == null;
	}

	@Nullable Key getSecretKey() {
		return key;
	}

	public @Nullable String getHexKey() {
		return hexKey;
	}

	public @NonNull String getTransformation() {
		return transformation;
	}
}
