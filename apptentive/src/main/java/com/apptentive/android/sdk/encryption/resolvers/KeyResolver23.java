package com.apptentive.android.sdk.encryption.resolvers;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.util.ObjectUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
class KeyResolver23 implements KeyResolver {
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";

	@Override
	public @NonNull EncryptionKey resolveKey(Context context, String keyAlias) throws KeyStoreException,
	                                                                                  CertificateException,
	                                                                                  NoSuchAlgorithmException,
	                                                                                  UnrecoverableEntryException,
	                                                                                  IOException,
                                                                                    NoSuchProviderException,
                                                                                    InvalidAlgorithmParameterException {
		SecretKey secretKey = resolveSecretKey(keyAlias);
		return new EncryptionKey(secretKey, CIPHER_TRANSFORMATION);
	}

	private SecretKey resolveSecretKey(String keyAlias) throws CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException {
		SecretKey secretKey = loadExistingKey(keyAlias);
		if (secretKey != null) {
			return secretKey;
		}

		return generateKey(keyAlias);
	}

	private @Nullable SecretKey loadExistingKey(String keyAlias) throws CertificateException,
	                                                                    NoSuchAlgorithmException,
	                                                                    IOException,
	                                                                    UnrecoverableEntryException,
	                                                                    KeyStoreException {
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
		keyStore.load(null);
		KeyStore.SecretKeyEntry secretKeyEntry = ObjectUtils.as(keyStore.getEntry(keyAlias, null), KeyStore.SecretKeyEntry.class);
		return secretKeyEntry != null ? secretKeyEntry.getSecretKey() : null;
	}

	private SecretKey generateKey(String keyAlias) throws NoSuchProviderException,
	                                                      NoSuchAlgorithmException,
	                                                      InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
		keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
			                  .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
			                  .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
			                  .setRandomizedEncryptionRequired(false) // we need that to make our custom IV work
			                  .build());

		return keyGenerator.generateKey();
	}
}
