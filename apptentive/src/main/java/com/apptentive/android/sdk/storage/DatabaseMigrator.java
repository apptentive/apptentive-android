package com.apptentive.android.sdk.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionException;
import com.apptentive.android.sdk.encryption.EncryptionHelper;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.IOException;

import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.util.Constants.PAYLOAD_DATA_FILE_SUFFIX;

abstract class DatabaseMigrator {
	static final int TRUE = 1;
	static final int FALSE = 0;

	private final Encryption encryption;
	private final File payloadDataDir;

	public DatabaseMigrator(Encryption encryption, File payloadDataDir) {
		this.encryption = encryption;
		this.payloadDataDir = payloadDataDir;
	}

	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws Exception;

	//region Helpers

	protected File getPayloadBodyFile(String nonce) {
		return new File(payloadDataDir, nonce + PAYLOAD_DATA_FILE_SUFFIX);
	}

	protected byte[] encrypt(@Nullable String value) throws EncryptionException {
		return EncryptionHelper.encrypt(encryption, value);
	}

	protected void writeToFile(File file, byte[] data, boolean encrypted) throws IOException,
	                                                                             EncryptionException {
		if (encrypted) {
			EncryptionHelper.writeToEncryptedFile(encryption, file, data);
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
			logException(e);
		}
	}

	//endregion
}
