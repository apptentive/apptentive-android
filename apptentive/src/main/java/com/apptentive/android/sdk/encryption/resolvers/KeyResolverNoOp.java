package com.apptentive.android.sdk.encryption.resolvers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.apptentive.android.sdk.encryption.EncryptionKey;

class KeyResolverNoOp implements KeyResolver {
	@NonNull @Override
	public EncryptionKey resolveKey(Context context, String keyAlias) {
		return EncryptionKey.NULL; // FIXME: descriptive log message
	}
}
