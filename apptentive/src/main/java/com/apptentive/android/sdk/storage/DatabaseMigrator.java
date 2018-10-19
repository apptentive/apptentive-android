package com.apptentive.android.sdk.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.encryption.EncryptionException;
import com.apptentive.android.sdk.encryption.EncryptionKey;
import com.apptentive.android.sdk.encryption.Encryptor;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.util.Constants.PAYLOAD_DATA_FILE_SUFFIX;

abstract class DatabaseMigrator {
	static final int TRUE = 1;
	static final int FALSE = 0;

	private final EncryptionKey encryptionKey;
	private final File payloadDataDir;

	public DatabaseMigrator(EncryptionKey encryptionKey, File payloadDataDir) {
		this.encryptionKey = encryptionKey;
		this.payloadDataDir = payloadDataDir;
	}

	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws Exception;

	//region Helpers

	protected File getPayloadBodyFile(String nonce) {
		return new File(payloadDataDir, nonce + PAYLOAD_DATA_FILE_SUFFIX);
	}

	protected byte[] encrypt(@Nullable String value) throws NoSuchPaddingException,
	                                                        InvalidKeyException,
	                                                        NoSuchAlgorithmException,
	                                                        IllegalBlockSizeException,
	                                                        BadPaddingException,
	                                                        InvalidAlgorithmParameterException,
	                                                        EncryptionException {
		return Encryptor.encrypt(encryptionKey, value);
	}

	protected void writeToFile(File file, byte[] data, boolean encrypted) throws NoSuchPaddingException,
	                                                                             InvalidKeyException,
	                                                                             NoSuchAlgorithmException,
                                                                               IOException,
	                                                                             BadPaddingException,
	                                                                             IllegalBlockSizeException,
	                                                                             InvalidAlgorithmParameterException,
	                                                                             EncryptionException {
		if (encrypted) {
			Encryptor.writeToEncryptedFile(encryptionKey, file, data);
		} else {
			Util.writeAtomically(file, data);
		}
	}

	void ensureClosed(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			ApptentiveLog.w(DATABASE, "Error closing SQLite cursor.", e);
		}
	}

	//endregion
}
