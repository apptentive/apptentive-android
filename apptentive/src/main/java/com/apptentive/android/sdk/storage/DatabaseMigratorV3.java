package com.apptentive.android.sdk.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionException;
import com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.DatabaseColumn;
import com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.PayloadEntry;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.IOException;

import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.SQL_CREATE_PAYLOAD_TABLE;

class DatabaseMigratorV3 extends DatabaseMigrator {
	private static final String TABLE_NAME_PAYLOADS = "payload";
	private static final String TABLE_NAME_PAYLOADS_LEGACY = "legacy_payload";

	private static final class PayloadEntryLegacy {
		static final DatabaseColumn COLUMN_PRIMARY_KEY = new DatabaseColumn(0, "_id");
		static final DatabaseColumn COLUMN_PAYLOAD_TYPE = new DatabaseColumn(1, "payloadType");
		static final DatabaseColumn COLUMN_IDENTIFIER = new DatabaseColumn(2, "identifier");
		static final DatabaseColumn COLUMN_CONTENT_TYPE = new DatabaseColumn(3, "contentType");
		static final DatabaseColumn COLUMN_AUTH_TOKEN = new DatabaseColumn(4, "authToken");
		static final DatabaseColumn COLUMN_CONVERSATION_ID = new DatabaseColumn(5, "conversationId");
		static final DatabaseColumn COLUMN_REQUEST_METHOD = new DatabaseColumn(6, "requestMethod");
		static final DatabaseColumn COLUMN_PATH = new DatabaseColumn(7, "path");
		static final DatabaseColumn COLUMN_ENCRYPTED = new DatabaseColumn(8, "encrypted");
		static final DatabaseColumn COLUMN_LOCAL_CONVERSATION_ID = new DatabaseColumn(9, "localConversationId");
	}

	private static final String SQL_BACKUP_LEGACY_PAYLOAD_TABLE = String.format("ALTER TABLE %s RENAME TO %s;", TABLE_NAME_PAYLOADS, TABLE_NAME_PAYLOADS_LEGACY);
	private static final String SQL_DELETE_LEGACY_PAYLOAD_TABLE = String.format("DROP TABLE %s;", TABLE_NAME_PAYLOADS_LEGACY);

	private static final String SQL_QUERY_SELECT_LEGACY_PAYLOADS = "SELECT * FROM " + TABLE_NAME_PAYLOADS_LEGACY +
	                                                               " ORDER BY " + PayloadEntryLegacy.COLUMN_PRIMARY_KEY +
	                                                               " ASC";

	public DatabaseMigratorV3(Encryption encryption, File payloadDataDir) {
		super(encryption, payloadDataDir);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws IOException,
	                                                                                EncryptionException {
		Cursor cursor = null;
		try {
			db.beginTransaction();

			// 1. Rename existing "payload" table to "legacy_payload"
			ApptentiveLog.v(DATABASE, "\t1. Backing up '%s' table to '%s'...", TABLE_NAME_PAYLOADS, TABLE_NAME_PAYLOADS_LEGACY);
			db.execSQL(SQL_BACKUP_LEGACY_PAYLOAD_TABLE);

			// 2. Create new Payload table as "payload"
			ApptentiveLog.v(DATABASE, "\t2. Creating new '%s' table...", TABLE_NAME_PAYLOADS);
			db.execSQL(SQL_CREATE_PAYLOAD_TABLE);

			// 3. Load legacy payloads
			cursor = db.rawQuery(SQL_QUERY_SELECT_LEGACY_PAYLOADS, null);
			ApptentiveLog.v(DATABASE, "\t3. Migrating legacy payloads (%d)...", cursor.getCount());

			while (cursor.moveToNext()) {
				// read legacy payload data
				final String nonce = cursor.getString(PayloadEntryLegacy.COLUMN_IDENTIFIER.index);
				final String conversationId = cursor.getString(PayloadEntryLegacy.COLUMN_CONVERSATION_ID.index);
				final String localConversationId = cursor.getString(PayloadEntryLegacy.COLUMN_LOCAL_CONVERSATION_ID.index);
				final String conversationToken = cursor.getString(PayloadEntryLegacy.COLUMN_AUTH_TOKEN.index);
				final String httpRequestPath = cursor.getString(PayloadEntryLegacy.COLUMN_PATH.index);
				final String contentType = cursor.getString(PayloadEntryLegacy.COLUMN_CONTENT_TYPE.index);
				final String httpRequestMethod = cursor.getString(PayloadEntryLegacy.COLUMN_REQUEST_METHOD.index);
				final int authenticated = cursor.getInt(PayloadEntryLegacy.COLUMN_ENCRYPTED.index);
				final String payloadType = cursor.getString(PayloadEntryLegacy.COLUMN_PAYLOAD_TYPE.index);

				// check for inconsistency
				File file = getPayloadBodyFile(nonce);
				if (!file.exists()) {
					ApptentiveLog.w(PAYLOADS, "\t\tLegacy payload missing its data file. Skipping...");
					continue;
				}

				// encrypt payload body
				File payloadFile = getPayloadBodyFile(nonce);
				final boolean shouldWriteEncrypted = authenticated == FALSE; // anonymous payloads should we stored encrypted
				writeToFile(payloadFile, Util.readBytes(payloadFile), shouldWriteEncrypted);

				// FIXME: remove code duplication

				// insert payload data into the new table
				ContentValues values = new ContentValues();
				values.put(PayloadEntry.COLUMN_IDENTIFIER.name, nonce);
				values.put(PayloadEntry.COLUMN_CONVERSATION_ID.name, conversationId);
				values.put(PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID.name, localConversationId);
				values.put(PayloadEntry.COLUMN_PAYLOAD_TYPE.name, payloadType);
				values.put(PayloadEntry.COLUMN_CONTENT_TYPE.name, contentType);
				values.put(PayloadEntry.COLUMN_AUTH_TOKEN.name, encrypt(conversationToken)); // token gets encrypted
				values.put(PayloadEntry.COLUMN_REQUEST_METHOD.name, httpRequestMethod);
				values.put(PayloadEntry.COLUMN_PATH.name, httpRequestPath);
				values.put(PayloadEntry.COLUMN_AUTHENTICATED.name, authenticated);

				db.insert(TABLE_NAME_PAYLOADS, null, values);
			}

			// 5. Finally, delete the temporary legacy table
			ApptentiveLog.v(DATABASE, "\t6. Dropping temporary '%s' table...", TABLE_NAME_PAYLOADS_LEGACY);
			db.execSQL(SQL_DELETE_LEGACY_PAYLOAD_TABLE);

			db.setTransactionSuccessful();
		} finally {
			ensureClosed(cursor);
			if (db != null) {
				db.endTransaction();
			}
		}
	}
}