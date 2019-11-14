package com.apptentive.android.sdk.encryption.resolvers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Base64;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import static com.apptentive.android.sdk.ApptentiveLogTag.SECURITY;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2) class KeyResolver18 implements KeyResolver {
	private static final String DEFAULT_KEY_ALGORITHM = "AES";
	private static final String WRAPPER_KEY_ALGORITHM = "RSA";

	private static final String DEFAULT_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String WRAPPER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

	private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";

	private static final String PREFS_NAME_SYMMETRIC_KEY = "com.apptentive.sdk.security.keys";
	private static final String PREFS_KEY_SYMMETRIC_KEY = "key";

	@Override
	public @NonNull EncryptionKey resolveKey(Context context, String keyAlias) throws UnrecoverableKeyException,
	                                                                                  CertificateException,
	                                                                                  NoSuchAlgorithmException,
	                                                                                  KeyStoreException,
	                                                                                  IOException,
	                                                                                  NoSuchProviderException,
	                                                                                  InvalidAlgorithmParameterException,
	                                                                                  NoSuchPaddingException,
	                                                                                  InvalidKeyException,
	                                                                                  IllegalBlockSizeException {
		SecretKey secretKey = resolveSymmetricKey(context, keyAlias);
		return new EncryptionKey(secretKey, DEFAULT_TRANSFORMATION);
	}

	//region Keys

	private SecretKey resolveSymmetricKey(Context context, String keyAlias) throws UnrecoverableKeyException,
	                                                                               CertificateException,
	                                                                               NoSuchAlgorithmException,
	                                                                               KeyStoreException,
	                                                                               IOException,
	                                                                               NoSuchProviderException,
	                                                                               InvalidAlgorithmParameterException,
	                                                                               NoSuchPaddingException,
	                                                                               InvalidKeyException,
	                                                                               IllegalBlockSizeException {
		// 1. try to resolve the wrapper key (load an existing one from the key store or generate a new one)
		KeyPair wrapperKey = resolveWrapperKey(context, keyAlias);

		// 2. try to load and existing symmetric key from un-secure device storage
		SecretKey secretKey = loadSymmetricKey(context, wrapperKey);
		if (secretKey != null) {
			return secretKey;
		}

		// 3. generate and store a new symmetric key in the un-secure device storage.
		return generateSymmetricKey(context, wrapperKey);
	}

	private SecretKey generateSymmetricKey(Context context, KeyPair wrapperKey) throws NoSuchAlgorithmException,
	                                                                                   NoSuchPaddingException,
	                                                                                   InvalidKeyException,
	                                                                                   IllegalBlockSizeException {
		SecretKey secretKey = generateSymmetricKey();
		storeSymmetricKey(context, secretKey, wrapperKey);
		return secretKey;
	}

	private static SecretKey generateSymmetricKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(DEFAULT_KEY_ALGORITHM);
		return keyGenerator.generateKey();
	}

	private static void storeSymmetricKey(Context context, SecretKey symmetricKey, KeyPair wrapperKey) throws IllegalBlockSizeException,
	                                                                                                          InvalidKeyException,
	                                                                                                          NoSuchAlgorithmException,
	                                                                                                          NoSuchPaddingException {
		String encryptedSymmetricKey = wrapSymmetricKey(wrapperKey, symmetricKey);
		getKeyPrefs(context).edit()
			.putString(PREFS_KEY_SYMMETRIC_KEY, encryptedSymmetricKey)
			.apply();
	}

	/**
	 * Attempts to load an existing symmetric key or return <code>null</code> if failed.
	 * Multistep process:
	 * 1. Load and encrypted symmetric key data from the shared preferences.
	 * 2. Unwraps the key using <code>wrapperKey</code>
	 */
	private static @Nullable SecretKey loadSymmetricKey(Context context, KeyPair wrapperKey) throws NoSuchPaddingException,
	                                                                                                NoSuchAlgorithmException,
	                                                                                                InvalidKeyException {
		String encryptedSymmetricKey = getKeyPrefs(context).getString(PREFS_KEY_SYMMETRIC_KEY, null);
		if (StringUtils.isNullOrEmpty(encryptedSymmetricKey)) {
			return null;
		}

		return unwrapSymmetricKey(wrapperKey, encryptedSymmetricKey);
	}

	private static KeyPair resolveWrapperKey(Context context, String keyAlias) throws UnrecoverableKeyException,
	                                                                                  CertificateException,
	                                                                                  NoSuchAlgorithmException,
	                                                                                  KeyStoreException,
	                                                                                  IOException,
	                                                                                  NoSuchProviderException,
	                                                                                  InvalidAlgorithmParameterException {
		KeyPair existingWrapperKey = loadExistingWrapperKey(keyAlias);
		if (existingWrapperKey != null) {
			ApptentiveLog.v(SECURITY, "Loaded existing asymmetric wrapper key (alias: %s)", keyAlias);
			return existingWrapperKey;
		}

		KeyPair wrapperKey = generateWrapperKey(context, keyAlias);
		ApptentiveLog.v(SECURITY, "Generated new asymmetric wrapper key (alias: %s)", keyAlias);
		return wrapperKey;
	}


	private static @Nullable KeyPair loadExistingWrapperKey(String keyAlias) throws KeyStoreException,
	                                                                                CertificateException,
	                                                                                NoSuchAlgorithmException,
	                                                                                IOException,
	                                                                                UnrecoverableKeyException {
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
		keyStore.load(null);
		PrivateKey privateKey = ObjectUtils.as(keyStore.getKey(keyAlias, null), PrivateKey.class);
		if (privateKey == null) {
			return null;
		}

		Certificate certificate = keyStore.getCertificate(keyAlias);
		if (certificate == null) {
			return null;
		}

		PublicKey publicKey = certificate.getPublicKey();
		if (publicKey == null) {
			return null;
		}

		return new KeyPair(publicKey, privateKey);
	}

	private static KeyPair generateWrapperKey(Context context, String alias) throws NoSuchProviderException,
	                                                                                NoSuchAlgorithmException,
	                                                                                InvalidAlgorithmParameterException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(WRAPPER_KEY_ALGORITHM, KEYSTORE_PROVIDER);
		Calendar startDate = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		endDate.add(Calendar.YEAR, 25);

		KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(context)
			                                       .setAlias(alias)
			                                       .setSerialNumber(BigInteger.ONE)
			                                       .setSubject(new X500Principal("CN=${alias} CA Certificate"))
			                                       .setStartDate(startDate.getTime())
			                                       .setEndDate(endDate.getTime());

		generator.initialize(builder.build());
		return generator.generateKeyPair();
	}

	//endregion

	//region Key Wrapping

	private static String wrapSymmetricKey(KeyPair wrapperKey, SecretKey symmetricKey) throws NoSuchPaddingException,
	                                                                                          NoSuchAlgorithmException,
	                                                                                          InvalidKeyException,
	                                                                                          IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance(WRAPPER_TRANSFORMATION);
		cipher.init(Cipher.WRAP_MODE, wrapperKey.getPublic());
		byte[] decodedData = cipher.wrap(symmetricKey);
		return Base64.encodeToString(decodedData, Base64.DEFAULT);
	}

	private static SecretKey unwrapSymmetricKey(KeyPair wrapperKey, String wrappedKeyData) throws NoSuchPaddingException,
	                                                                                              NoSuchAlgorithmException,
	                                                                                              InvalidKeyException {
		byte[] encryptedKeyData = Base64.decode(wrappedKeyData, Base64.DEFAULT);
		Cipher cipher = Cipher.getInstance(WRAPPER_TRANSFORMATION);
		cipher.init(Cipher.UNWRAP_MODE, wrapperKey.getPrivate());
		return (SecretKey) cipher.unwrap(encryptedKeyData, DEFAULT_KEY_ALGORITHM, Cipher.SECRET_KEY);
	}

	//endregion

	//region Helpers

	private static SharedPreferences getKeyPrefs(Context context) {
		return context.getSharedPreferences(PREFS_NAME_SYMMETRIC_KEY, Context.MODE_PRIVATE);
	}

	//endregion
}
