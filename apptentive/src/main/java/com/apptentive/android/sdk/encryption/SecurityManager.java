package com.apptentive.android.sdk.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.encryption.resolvers.KeyResolver;
import com.apptentive.android.sdk.encryption.resolvers.KeyResolverFactory;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.UUID;

import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;
import static com.apptentive.android.sdk.ApptentiveLogTag.SECURITY;

/**
 * Class responsible for managing the master encryption key (generation, storage and retrieval).
 */
public final class SecurityManager {
	private static final String PREFS_KEY_ALIAS = "alias";
	private static final String PREFS_SDK_VERSION_CODE = "version_code";

	/**
	 * The highest API version which would resolve in no encryption operations
	 */
	private static final int LEGACY_KEY_STORE_API_NO_OP = 17;

	/** If we don't extract it into a constant - R8 would crash! */
	private static final int SDK_INT = Build.VERSION.SDK_INT;

	//region Initialization

	/**
	 * Resolves the "master" encryption object (the one used for secure on device storage)
	 *
	 * @param encryption           - user-specified encryption object (comes from the SDK configuration). Has precedence over anything else.
	 * @param shouldEncryptStorage - indicates if encrypted storage should be used on device. Once this flag is set - it won't be changed again.
	 * @throws EncryptionInitializationException - if encryption key could not be resolved.
	 */
	public static @NonNull Encryption getEncryption(Context context, @Nullable Encryption encryption, boolean shouldEncryptStorage) throws EncryptionInitializationException {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		// if the developer passes an encryption object - it would have precedence over anything else
		if (encryption != null) {
			if (!hasEncryptionInfo(context)) {
				ApptentiveLog.i(SECURITY, "Using an external encryption for secure storage");
				return EncryptionFactory.wrapNullSafe(encryption);
			}
			ApptentiveLog.w(SECURITY, "The client already has its storage encrypted and can't transit to a custom encryption implementation.");
		}

		// get the name of the alias
		KeyInfo keyInfo = resolveKeyInfo(context, shouldEncryptStorage);
		ApptentiveLog.v(SECURITY, "Secret key info: %s", keyInfo);

		// load or generate the key
		EncryptionKey masterKey = resolveMasterKey(context, keyInfo);

		// create an encryption for the given key
		return EncryptionFactory.createEncryption(masterKey);
	}

	public static void clear(Context context) {
		SharedPreferences prefs = getPrefs(context);
		prefs.edit().clear().apply();
	}

	private static boolean hasEncryptionInfo(Context context) {
		SharedPreferences prefs = getPrefs(context);
		String keyAlias = prefs.getString(PREFS_KEY_ALIAS, null);
		int versionCode = prefs.getInt(PREFS_SDK_VERSION_CODE, 0);
		return !StringUtils.isNullOrEmpty(keyAlias) && versionCode > 0;
	}

	private static KeyInfo resolveKeyInfo(Context context, boolean shouldEncryptStorage) {
		// in order to avoid potential naming collisions we would generate a unique name for the alias and
		// store it in the SharedPreferences
		SharedPreferences prefs = getPrefs(context);

		String keyAlias = prefs.getString(PREFS_KEY_ALIAS, null);
		int versionCode = prefs.getInt(PREFS_SDK_VERSION_CODE, 0);
		if (StringUtils.isNullOrEmpty(keyAlias) || versionCode == 0) {
			keyAlias = generateUniqueKeyAlias();
			if (shouldEncryptStorage) {
				versionCode = SDK_INT;
			} else {
				versionCode = LEGACY_KEY_STORE_API_NO_OP; // if user opts out of encryption - use no-op API
			}
			prefs.edit()
				.putString(PREFS_KEY_ALIAS, keyAlias)
				.putInt(PREFS_SDK_VERSION_CODE, versionCode)
				.apply();
			ApptentiveLog.v(SECURITY, "Generated new key info");
		}

		return new KeyInfo(keyAlias, versionCode);
	}

	private static @NonNull EncryptionKey resolveMasterKey(Context context, KeyInfo keyInfo) throws EncryptionInitializationException {
		try {
			KeyResolver keyResolver = KeyResolverFactory.createKeyResolver(keyInfo.versionCode);
			return keyResolver.resolveKey(context, keyInfo.alias);
		} catch (Exception e) {
			throw new EncryptionInitializationException(StringUtils.format("Exception while resolving secret key for alias '%s'. Encryption might not work correctly!", hideIfSanitized(keyInfo.alias)), e);
		}
	}

	//endregion

	//region Helpers

	private static String generateUniqueKeyAlias() {
		return "apptentive-key-" + UUID.randomUUID().toString();
	}

	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences("com.apptentive.sdk.security", Context.MODE_PRIVATE);
	}

	//endregion

	//region Helper classes

	static class KeyInfo {
		/**
		 * Alias name for KeyStore.
		 */
		final String alias;

		/**
		 * Android SDK version code at the time the target key was generated.
		 */
		final int versionCode;

		KeyInfo(String alias, int versionCode) {
			if (StringUtils.isNullOrEmpty(alias)) {
				throw new IllegalArgumentException("Key alias name is null or empty");
			}
			if (versionCode < 1) {
				throw new IllegalArgumentException("Invalid SDK version code");
			}

			this.alias = alias;
			this.versionCode = versionCode;
		}

		@Override
		public String toString() {
			return StringUtils.format("KeyInfo: alias=%s versionCode=%d", hideIfSanitized(alias), versionCode);
		}
	}

	//endregion
}
