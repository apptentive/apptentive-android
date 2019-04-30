package com.apptentive.android.sdk.storage;

import android.database.sqlite.SQLiteDatabase;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionKey;

import java.io.File;

import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.SQL_CREATE_PAYLOAD_TABLE;
import static com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.SQL_DELETE_PAYLOAD_TABLE;

class DatabaseMigratorV1 extends DatabaseMigrator {
	public DatabaseMigratorV1(Encryption encryption, File payloadDataDir) {
		super(encryption, payloadDataDir);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 1. Delete old table
		ApptentiveLog.v(DATABASE, "\t1. Dropping legacy table...");
		db.execSQL(SQL_DELETE_PAYLOAD_TABLE);

		// 2. Create new table
		ApptentiveLog.v(DATABASE, "\t2. Creating new table...");
		db.execSQL(SQL_CREATE_PAYLOAD_TABLE);
	}
}
