package com.apptentive.android.sdk.encryption.resolvers;

import android.os.Build;
import androidx.annotation.NonNull;

public class KeyResolverFactory {
	public static @NonNull KeyResolver createKeyResolver(int versionCode) {
		// Android API level 26 has a bug when symmetric key does not work. We use legacy approach instead.
		// see: https://stackoverflow.com/questions/36015194/android-keystoreexception-unknown-error
		if (versionCode == Build.VERSION_CODES.O) {
			return new KeyResolver26();
		}

		if (versionCode >= Build.VERSION_CODES.M) {
			return new KeyResolver23();
		}

		if (versionCode >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return new KeyResolver18();
		}

		return new KeyResolverNoOp();
	}
}
