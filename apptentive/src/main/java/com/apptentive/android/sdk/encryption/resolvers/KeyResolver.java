package com.apptentive.android.sdk.encryption.resolvers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.encryption.EncryptionKey;

public interface KeyResolver {
	@NonNull EncryptionKey resolveKey(Context context, String keyAlias) throws Exception;
}
