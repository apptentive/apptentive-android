package com.apptentive.android.sdk.encryption;

/**
 * Thrown if encryption key was not properly loaded from the Keystore.
 */
public class EncryptionInitializationException extends RuntimeException {
	public EncryptionInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
