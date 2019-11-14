/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.encryption.EncryptionException;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.encryption.EncryptionHelper;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadData;
import com.apptentive.android.sdk.model.PayloadType;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;
import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;
import static com.apptentive.android.sdk.debug.Assert.assertFail;
import static com.apptentive.android.sdk.debug.Assert.assertFalse;
import static com.apptentive.android.sdk.debug.Assert.notNull;
import static com.apptentive.android.sdk.util.Constants.PAYLOAD_DATA_FILE_SUFFIX;

/**
 * There can be only one. SQLiteOpenHelper per database name that is. All new Apptentive tables must be defined here.
 */
public class ApptentiveDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "apptentive";
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private final File fileDir; // data dir of the application

	private final File payloadDataDir;
	private final Encryption encryption;

	//region Payload SQL

	static final class PayloadEntry {
		static final String TABLE_NAME = "payload";
		static final DatabaseColumn COLUMN_PRIMARY_KEY = new DatabaseColumn(0, "_id");
		static final DatabaseColumn COLUMN_PAYLOAD_TYPE = new DatabaseColumn(1, "payloadType");
		static final DatabaseColumn COLUMN_IDENTIFIER = new DatabaseColumn(2, "identifier");
		static final DatabaseColumn COLUMN_CONTENT_TYPE = new DatabaseColumn(3, "contentType");
		static final DatabaseColumn COLUMN_AUTH_TOKEN = new DatabaseColumn(4, "authToken");
		static final DatabaseColumn COLUMN_CONVERSATION_ID = new DatabaseColumn(5, "conversationId");
		static final DatabaseColumn COLUMN_REQUEST_METHOD = new DatabaseColumn(6, "requestMethod");
		static final DatabaseColumn COLUMN_PATH = new DatabaseColumn(7, "path");
		static final DatabaseColumn COLUMN_AUTHENTICATED = new DatabaseColumn(8, "authenticated");
		static final DatabaseColumn COLUMN_LOCAL_CONVERSATION_ID = new DatabaseColumn(9, "localConversationId");
	}

	static final String SQL_CREATE_PAYLOAD_TABLE =
		"CREATE TABLE " + PayloadEntry.TABLE_NAME +
			" (" +
			PayloadEntry.COLUMN_PRIMARY_KEY + " INTEGER PRIMARY KEY, " +
			PayloadEntry.COLUMN_PAYLOAD_TYPE + " TEXT, " +
			PayloadEntry.COLUMN_IDENTIFIER + " TEXT, " +
			PayloadEntry.COLUMN_CONTENT_TYPE + " TEXT," +
			PayloadEntry.COLUMN_AUTH_TOKEN + " BLOB," +
			PayloadEntry.COLUMN_CONVERSATION_ID + " TEXT," +
			PayloadEntry.COLUMN_REQUEST_METHOD + " TEXT," +
			PayloadEntry.COLUMN_PATH + " TEXT," +
			PayloadEntry.COLUMN_AUTHENTICATED + " INTEGER," +
			PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID + " TEXT" +
			");";

	static final String SQL_DELETE_PAYLOAD_TABLE = "DROP TABLE " + PayloadEntry.TABLE_NAME + ";";

	private static final String SQL_SELECT_PAYLOADS_IN_SEND_ORDER =
		"SELECT * FROM " + PayloadEntry.TABLE_NAME +
			" ORDER BY " + PayloadEntry.COLUMN_PRIMARY_KEY +
			" ASC";

	private static final String SQL_UPDATE_INCOMPLETE_PAYLOADS =
		"UPDATE " + PayloadEntry.TABLE_NAME + " SET " +
			PayloadEntry.COLUMN_AUTH_TOKEN + " = ?, " +
			PayloadEntry.COLUMN_CONVERSATION_ID + " = ? " +
			"WHERE " +
			PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID + " = ? AND " +
			PayloadEntry.COLUMN_AUTH_TOKEN + " IS NULL AND " +
			PayloadEntry.COLUMN_CONVERSATION_ID + " IS NULL";

	private static final String SQL_UPDATE_LEGACY_PAYLOADS =
			"UPDATE " + PayloadEntry.TABLE_NAME + " SET " +
					PayloadEntry.COLUMN_AUTH_TOKEN + " = ?, " +
					PayloadEntry.COLUMN_CONVERSATION_ID + " = ?, " +
					PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID + " = ? " +
					"WHERE " +
					PayloadEntry.COLUMN_AUTH_TOKEN + " IS NULL AND " +
					PayloadEntry.COLUMN_CONVERSATION_ID + " IS NULL";

	private static final String SQL_REMOVE_INCOMPLETE_PAYLOADS =
		"DELETE FROM " + PayloadEntry.TABLE_NAME + " " +
			"WHERE " +
			PayloadEntry.COLUMN_AUTH_TOKEN + " IS NULL OR " +
			PayloadEntry.COLUMN_CONVERSATION_ID + " IS NULL";

	//endregion

	//region Message SQL (Deprecated: Used for migration only)

	private static final String TABLE_MESSAGE = "message";
	private static final String MESSAGE_KEY_DB_ID = "_id";                           // 0
	private static final String MESSAGE_KEY_ID = "id";                               // 1
	private static final String MESSAGE_KEY_CLIENT_CREATED_AT = "client_created_at"; // 2
	private static final String MESSAGE_KEY_NONCE = "nonce";                         // 3
	private static final String MESSAGE_KEY_STATE = "state";                         // 4
	private static final String MESSAGE_KEY_READ = "read";                           // 5
	private static final String MESSAGE_KEY_JSON = "json";                           // 6

	private static final String TABLE_CREATE_MESSAGE =
		"CREATE TABLE " + TABLE_MESSAGE +
			" (" +
			MESSAGE_KEY_DB_ID + " INTEGER PRIMARY KEY, " +
			MESSAGE_KEY_ID + " TEXT, " +
			MESSAGE_KEY_CLIENT_CREATED_AT + " DOUBLE, " +
			MESSAGE_KEY_NONCE + " TEXT, " +
			MESSAGE_KEY_STATE + " TEXT, " +
			MESSAGE_KEY_READ + " INTEGER, " +
			MESSAGE_KEY_JSON + " TEXT" +
			");";

	//endregion

	//region File SQL  (Deprecated: Used for migration only)

	private static final String TABLE_FILESTORE = "file_store";
	private static final String FILESTORE_KEY_ID = "id";                         // 0
	private static final String FILESTORE_KEY_MIME_TYPE = "mime_type";           // 1
	private static final String FILESTORE_KEY_ORIGINAL_URL = "original_uri";     // 2
	private static final String FILESTORE_KEY_LOCAL_URL = "local_uri";           // 3
	private static final String FILESTORE_KEY_APPTENTIVE_URL = "apptentive_uri"; // 4
	private static final String TABLE_CREATE_FILESTORE =
		"CREATE TABLE " + TABLE_FILESTORE +
			" (" +
			FILESTORE_KEY_ID + " TEXT PRIMARY KEY, " +
			FILESTORE_KEY_MIME_TYPE + " TEXT, " +
			FILESTORE_KEY_ORIGINAL_URL + " TEXT, " +
			FILESTORE_KEY_LOCAL_URL + " TEXT, " +
			FILESTORE_KEY_APPTENTIVE_URL + " TEXT" +
			");";

	//endregion

	//region Compound Message FileStore SQL (legacy)

	/* Compound Message FileStore:
	 * For Compound Messages stored in TABLE_MESSAGE, each associated file will add a row to this table
	 * using the message's "nonce" key
	 */
	private static final String TABLE_COMPOUND_MESSAGE_FILESTORE = "compound_message_file_store"; // table filePath
	private static final String COMPOUND_FILESTORE_KEY_DB_ID = "_id";                         // 0
	private static final String COMPOUND_FILESTORE_KEY_MESSAGE_NONCE = "nonce"; // message nonce of the compound message
	private static final String COMPOUND_FILESTORE_KEY_MIME_TYPE = "mime_type"; // mine type of the file
	private static final String COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI = "local_uri"; // original uriString or file path of source file (empty for received file)
	private static final String COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH = "local_path"; // path to the local cached version
	private static final String COMPOUND_FILESTORE_KEY_REMOTE_URL = "apptentive_url";  // original server url of received file (empty for sent file)
	private static final String COMPOUND_FILESTORE_KEY_CREATION_TIME = "creation_time"; // creation time of the original file
	// Create the initial table. Use nonce and local cache path as primary key because both sent/received files will have a local cached copy
	private static final String TABLE_CREATE_COMPOUND_FILESTORE =
		"CREATE TABLE " + TABLE_COMPOUND_MESSAGE_FILESTORE +
			" (" +
			COMPOUND_FILESTORE_KEY_DB_ID + " INTEGER PRIMARY KEY, " +
			COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " TEXT, " +
			COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH + " TEXT, " +
			COMPOUND_FILESTORE_KEY_MIME_TYPE + " TEXT, " +
			COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI + " TEXT, " +
			COMPOUND_FILESTORE_KEY_REMOTE_URL + " TEXT, " +
			COMPOUND_FILESTORE_KEY_CREATION_TIME + " LONG" +
			");";

	// Query all files associated with a given compound message nonce id
	private static final String QUERY_MESSAGE_FILES_GET_BY_NONCE = "SELECT * FROM " + TABLE_COMPOUND_MESSAGE_FILESTORE + " WHERE " + COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?";

	// endregion

	ApptentiveDatabaseHelper(Context context, Encryption encryption) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption key is null");
		}

		this.fileDir = context.getFilesDir();
		this.payloadDataDir = new File(fileDir, Constants.PAYLOAD_DATA_DIR);
		this.encryption = encryption;
	}

	//region Create & Upgrade

	/**
	 * This function is called only for new installs, and onUpgrade is not called in that case. Therefore, you must include the
	 * latest complete set of DDL here.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		ApptentiveLog.d(DATABASE, "ApptentiveDatabase.onCreate(db)");
		db.execSQL(SQL_CREATE_PAYLOAD_TABLE);

		// Leave legacy tables in place for now.
		db.execSQL(TABLE_CREATE_MESSAGE);
		db.execSQL(TABLE_CREATE_FILESTORE);
		db.execSQL(TABLE_CREATE_COMPOUND_FILESTORE);
	}

	/**
	 * This method is called when an app is upgraded. Add alter table statements here for each version in a non-breaking
	 * switch, so that all the necessary upgrades occur for each older version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ApptentiveLog.d(DATABASE, "Upgrade database from %d to %d", oldVersion, newVersion);
		try {
			DatabaseMigrator migrator = createDatabaseMigrator(oldVersion, newVersion);
			if (migrator != null) {
				migrator.onUpgrade(db, oldVersion, newVersion);
			}
		} catch (Exception e) {
			ApptentiveLog.e(DATABASE, e, "Exception while trying to migrate database from %d to %d", oldVersion, newVersion);
			logException(e);

			// if migration failed - create new table
			db.execSQL(SQL_DELETE_PAYLOAD_TABLE);
			onCreate(db);
		}
	}

	private @Nullable DatabaseMigrator createDatabaseMigrator(int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				return new DatabaseMigratorV1(encryption, payloadDataDir);
			case 2:
				return new DatabaseMigratorV2(encryption, payloadDataDir);
			case 3:
				return new DatabaseMigratorV3(encryption, payloadDataDir);
		}

		assertFail("Missing database migrator version: %d", oldVersion);
		return null;
	}

	//endregion

	//region Payloads

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	void addPayload(Payload payload) throws Exception {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.beginTransaction();

			ContentValues values = new ContentValues();
			values.put(PayloadEntry.COLUMN_IDENTIFIER.name, notNull(payload.getNonce()));
			values.put(PayloadEntry.COLUMN_PAYLOAD_TYPE.name, notNull(payload.getPayloadType().name()));
			values.put(PayloadEntry.COLUMN_CONTENT_TYPE.name, notNull(payload.getHttpRequestContentType()));
			// The token is encrypted inside the payload body for authenticated conversations. In that case, don't store it here.
			if (!payload.isAuthenticated()) {
				values.put(PayloadEntry.COLUMN_AUTH_TOKEN.name, encrypt(payload.getConversationToken())); // might be null
			}
			values.put(PayloadEntry.COLUMN_CONVERSATION_ID.name, payload.getConversationId()); // might be null
			values.put(PayloadEntry.COLUMN_REQUEST_METHOD.name, payload.getHttpRequestMethod().name());
			values.put(PayloadEntry.COLUMN_PATH.name, payload.getHttpEndPoint(
				StringUtils.isNullOrEmpty(payload.getConversationId()) ? "${conversationId}" : payload.getConversationId()) // if conversation id is missing we replace it with a place holder and update it later
			);

			File dest = getPayloadBodyFile(payload.getNonce());
			ApptentiveLog.v(DATABASE, "Saving payload body to: %s", dest);
			writeToFile(dest, payload.renderData(), !payload.isAuthenticated());  // only anonymous payloads get encrypted upon write (authenticated payloads get encrypted on serialization)

			values.put(PayloadEntry.COLUMN_AUTHENTICATED.name, payload.isAuthenticated() ? TRUE : FALSE);
			values.put(PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID.name, notNull(payload.getLocalConversationIdentifier()));

			db.insert(PayloadEntry.TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
			}
		}

		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			printPayloadTable("Added payload");
		}
	}

	void deletePayload(String payloadIdentifier) {
		if (payloadIdentifier == null) {
			throw new IllegalArgumentException("Payload identifier is null");
		}
		// First delete the row
		SQLiteDatabase db;
		try {
			db = getWritableDatabase();
			db.delete(
				PayloadEntry.TABLE_NAME,
				PayloadEntry.COLUMN_IDENTIFIER + " = ?",
				new String[]{payloadIdentifier}
			);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deletePayload EXCEPTION: " + sqe.getMessage());
			logException(sqe);
		}

		// Then delete the data file
		File dest = getPayloadBodyFile(payloadIdentifier);
		ApptentiveLog.v(DATABASE, "Deleted payload \"%s\" data file successfully? %b", payloadIdentifier, dest.delete());

		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			printPayloadTable("Deleted payload");
		}
	}

	void deleteAllPayloads() {
		// TODO: Delete files too.
		SQLiteDatabase db;
		try {
			db = getWritableDatabase();
			db.delete(PayloadEntry.TABLE_NAME, "", null);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteAllPayloads EXCEPTION: " + sqe.getMessage());
			logException(sqe);
		}
	}

	PayloadData getOldestUnsentPayload() {
		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			printPayloadTable("getOldestUnsentPayload");
		}

		SQLiteDatabase db;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.rawQuery(SQL_SELECT_PAYLOADS_IN_SEND_ORDER, null);
			int count = cursor.getCount();
			ApptentiveLog.v(PAYLOADS, "Unsent payloads count: %d", count);

			while(cursor.moveToNext()) {
				final String conversationId = cursor.getString(PayloadEntry.COLUMN_CONVERSATION_ID.index);
				if (conversationId == null) {
					ApptentiveLog.d(PAYLOADS, "Oldest unsent payload is missing a conversation id");
					return null;
				}

				final String nonce = notNull(cursor.getString(PayloadEntry.COLUMN_IDENTIFIER.index));

				// if we failed to decrypt auth token - delete it
				final String authToken = tryDecryptString(cursor.getBlob(PayloadEntry.COLUMN_AUTH_TOKEN.index), "");
				if (authToken != null && authToken.length() == 0) {
					ApptentiveLog.w(PAYLOADS, "Oldest unsent payload auth token can't be decrypted. Deleting...");
					deletePayload(nonce);
					continue;
				}

				final PayloadType payloadType = PayloadType.parse(cursor.getString(PayloadEntry.COLUMN_PAYLOAD_TYPE.index));
				assertFalse(PayloadType.unknown.equals(payloadType), "Oldest unsent payload has unknown type");

				if (PayloadType.unknown.equals(payloadType)) {
					ApptentiveLog.w(PAYLOADS, "Oldest unsent payload type is undefined. Deleting...");
					deletePayload(nonce);
					continue;
				}

				final String httpRequestPath = updatePayloadRequestPath(cursor.getString(PayloadEntry.COLUMN_PATH.index), conversationId);

				File file = getPayloadBodyFile(nonce);
				if (!file.exists()) {
					ApptentiveLog.w(PAYLOADS, "Oldest unsent payload had no data file. Deleting...");
					deletePayload(nonce);
					continue;
				}

				final String contentType = notNull(cursor.getString(PayloadEntry.COLUMN_CONTENT_TYPE.index));
				final HttpRequestMethod httpRequestMethod = HttpRequestMethod.valueOf(notNull(cursor.getString(PayloadEntry.COLUMN_REQUEST_METHOD.index)));
				final boolean authenticated = cursor.getInt(PayloadEntry.COLUMN_AUTHENTICATED.index) == TRUE;

				byte[] data = tryReadFromFile(file, !authenticated); // only anonymous payloads get encrypted upon write (authenticated payloads get encrypted on serialization)
				if (data == null) {
					ApptentiveLog.w(PAYLOADS, "Oldest unsent payload file can't be read. Deleting...");
					deletePayload(nonce);
					continue;
				}

				return new PayloadData(payloadType, nonce, conversationId, data, authToken, contentType, httpRequestPath, httpRequestMethod, authenticated);
			}
			return null;
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error getting oldest unsent payload.");
			// TODO: delete all payloads???
			logException(e);
			return null;
		} finally {
			ensureClosed(cursor);
		}
	}

	private String updatePayloadRequestPath(String path, String conversationId) {
		return path.replace("${conversationId}", conversationId);
	}

	void updateIncompletePayloads(String conversationId, String authToken, String localConversationId, boolean legacyPayloads) {
		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			printPayloadTable("updateIncompletePayloads BEFORE");
		}

		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}
		if (StringUtils.isNullOrEmpty(authToken)) {
			throw new IllegalArgumentException("Token is null or empty");
		}
		try {
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL(legacyPayloads ? SQL_UPDATE_LEGACY_PAYLOADS : SQL_UPDATE_INCOMPLETE_PAYLOADS, new Object[] {
				encrypt(authToken), conversationId, localConversationId
			});
			ApptentiveLog.v(DATABASE, "Updated missing conversation ids");
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while updating missing conversation ids");
			logException(e);
		}

		// remove incomplete payloads which don't belong to an active conversation
		removeCorruptedPayloads();

		if (ApptentiveLog.canLog(ApptentiveLog.Level.VERBOSE)) {
			printPayloadTable("updateIncompletePayloads AFTER");
		}
	}

	private void removeCorruptedPayloads() {
		Cursor cursor = null;
		try {
			SQLiteDatabase db = getWritableDatabase();
			cursor = db.rawQuery(SQL_REMOVE_INCOMPLETE_PAYLOADS, null);
			cursor.moveToFirst(); // we need to move a cursor in order to update database
			ApptentiveLog.v(DATABASE, "Removed incomplete payloads");
		} catch (SQLException e) {
			ApptentiveLog.e(e, "Exception while removing incomplete payloads");
			logException(e);
		} finally {
			ensureClosed(cursor);
		}
	}

	//endregion

	//region Files

	void deleteAssociatedFiles(String messageNonce) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_COMPOUND_MESSAGE_FILESTORE, COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?", new String[]{messageNonce});
			ApptentiveLog.d(DATABASE, "Deleted %d stored files.", deleted);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteAssociatedFiles EXCEPTION: " + sqe.getMessage());
			logException(sqe);
		}
	}

	List<StoredFile> getAssociatedFiles(String nonce) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<StoredFile> associatedFiles = new ArrayList<StoredFile>();
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_FILES_GET_BY_NONCE, new String[]{nonce});
			StoredFile ret;
			if (cursor.moveToFirst()) {
				do {
					ret = new StoredFile();
					ret.setId(nonce);
					ret.setLocalFilePath(cursor.getString(2));
					ret.setMimeType(cursor.getString(3));
					ret.setSourceUriOrPath(cursor.getString(4));
					ret.setApptentiveUri(cursor.getString(5));
					ret.setCreationTime(cursor.getLong(6));
					associatedFiles.add(ret);
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "getAssociatedFiles EXCEPTION: " + sqe.getMessage());
			logException(sqe);
		} finally {
			ensureClosed(cursor);
		}
		return associatedFiles.size() > 0 ? associatedFiles : null;
	}

	/*
	 * Add a list of associated files to compound message file storage
	 * Caller of this method should ensure all associated files have the same message nonce
	 * @param associatedFiles list of associated files
	 * @return true if succeed
	 */
	boolean addCompoundMessageFiles(List<StoredFile> associatedFiles) {
		String messageNonce = associatedFiles.get(0).getId();
		SQLiteDatabase db = null;
		long ret = -1;
		try {

			db = getWritableDatabase();
			db.beginTransaction();
			// Always delete existing rows with the same nonce to ensure add/update both work
			db.delete(TABLE_COMPOUND_MESSAGE_FILESTORE, COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?", new String[]{messageNonce});

			for (StoredFile file : associatedFiles) {
				ContentValues values = new ContentValues();
				values.put(COMPOUND_FILESTORE_KEY_MESSAGE_NONCE, file.getId());
				values.put(COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH, file.getLocalFilePath());
				values.put(COMPOUND_FILESTORE_KEY_MIME_TYPE, file.getMimeType());
				values.put(COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI, file.getSourceUriOrPath());
				values.put(COMPOUND_FILESTORE_KEY_REMOTE_URL, file.getApptentiveUri());
				values.put(COMPOUND_FILESTORE_KEY_CREATION_TIME, file.getCreationTime());
				ret = db.insert(TABLE_COMPOUND_MESSAGE_FILESTORE, null, values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "addCompoundMessageFiles EXCEPTION: " + sqe.getMessage());
			logException(sqe);
		}
		return ret != -1;
	}

	//endregion

	// region Helpers

	private File getPayloadBodyFile(String nonce) {
		return new File(payloadDataDir, nonce + PAYLOAD_DATA_FILE_SUFFIX);
	}

	private void ensureClosed(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			ApptentiveLog.w(DATABASE, "Error closing SQLite cursor.", e);
			logException(e);
		}
	}

	/**
	 * The following shall ONLY be used during development and testing. It will delete the database,
	 * including all saved payloads, messages, and files.
	 */
	void reset(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

	private @Nullable byte[] encrypt(@Nullable String value) throws EncryptionException {
		return EncryptionHelper.encrypt(encryption, value);
	}

	private @Nullable String tryDecryptString(@Nullable byte[] bytes, String defaultValue) {
		return tryDecryptString(bytes, defaultValue, true);
	}

	private @Nullable String tryDecryptString(@Nullable byte[] bytes, String defaultValue, boolean printError) {
		try {
			return decryptString(bytes);
		} catch (Exception e) {
			if (printError) {
				ApptentiveLog.e(e, "Failed to decrypt string");
			}
			return defaultValue;
		}
	}

	private @Nullable String decryptString(@Nullable byte[] bytes) throws EncryptionException {
		return EncryptionHelper.decryptString(encryption, bytes);
	}

	private void writeToFile(File file, byte[] data, boolean encrypted) throws IOException,
	                                                                           EncryptionException {
		if (encrypted) {
			EncryptionHelper.writeToEncryptedFile(encryption, file, data);
		} else {
			Util.writeAtomically(file, data);
		}
	}

	private @Nullable byte[] tryReadFromFile(File file, boolean encrypted) {
		try {
			return readFromFile(file, encrypted);
		} catch (Exception e) {
			ApptentiveLog.e(PAYLOADS, e, "Unable to read% file: %s", encrypted ? " encrypted" : "", file);
			logException(e);
			return null;
		}
	}

	private byte[] readFromFile(File file, boolean encrypted) throws IOException,
	                                                                 EncryptionException {
		return encrypted ? EncryptionHelper.readFromEncryptedFile(encryption, file) : Util.readBytes(file);
	}

	//endregion

	//region Helper classes

	static final class DatabaseColumn {
		public final String name;
		final int index;

		DatabaseColumn(int index, String name) {
			this.index = index;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	//endregion

	//region Debug

	private void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add additional context information
	}

	private void printPayloadTable(String title) {
		SQLiteDatabase db;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.rawQuery(SQL_SELECT_PAYLOADS_IN_SEND_ORDER, null);
			int payloadCount = cursor.getCount();
			if (payloadCount == 0) {
				ApptentiveLog.v(PAYLOADS, "%s (%d payload(s))", title, payloadCount);
				return;
			}

			Object[][] rows = new Object[1 + payloadCount][];
			rows[0] = new Object[] {
					PayloadEntry.COLUMN_PRIMARY_KEY,
					PayloadEntry.COLUMN_PAYLOAD_TYPE,
					PayloadEntry.COLUMN_IDENTIFIER,
					PayloadEntry.COLUMN_CONTENT_TYPE,
					PayloadEntry.COLUMN_CONVERSATION_ID,
					PayloadEntry.COLUMN_REQUEST_METHOD,
					PayloadEntry.COLUMN_PATH,
					PayloadEntry.COLUMN_AUTHENTICATED,
					PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID,
					PayloadEntry.COLUMN_AUTH_TOKEN
			};

			int index = 1;
			while(cursor.moveToNext()) {
				rows[index++] = new Object[] {
						cursor.getInt(PayloadEntry.COLUMN_PRIMARY_KEY.index),
						cursor.getString(PayloadEntry.COLUMN_PAYLOAD_TYPE.index),
						cursor.getString(PayloadEntry.COLUMN_IDENTIFIER.index),
						cursor.getString(PayloadEntry.COLUMN_CONTENT_TYPE.index),
						cursor.getString(PayloadEntry.COLUMN_CONVERSATION_ID.index),
						cursor.getString(PayloadEntry.COLUMN_REQUEST_METHOD.index),
						hideIfSanitized(cursor.getString(PayloadEntry.COLUMN_PATH.index)),
						cursor.getInt(PayloadEntry.COLUMN_AUTHENTICATED.index),
						cursor.getString(PayloadEntry.COLUMN_LOCAL_CONVERSATION_ID.index),
						hideIfSanitized(tryDecryptString(cursor.getBlob(PayloadEntry.COLUMN_AUTH_TOKEN.index), "<CORRUPTED>", false))
				};
			}
			ApptentiveLog.v(PAYLOADS, "%s (%d payload(s)):\n%s", title, payloadCount, StringUtils.table(rows));
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while printing metadata");
			logException(e);
		} finally {
			ensureClosed(cursor);
		}
	}

	//endregion
}
