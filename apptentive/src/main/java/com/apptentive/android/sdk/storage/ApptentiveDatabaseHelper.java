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
import android.text.TextUtils;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.Payload;
import com.apptentive.android.sdk.model.PayloadFactory;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;

/**
 * There can be only one. SQLiteOpenHelper per database name that is. All new Apptentive tables must be defined here.
 */
public class ApptentiveDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "apptentive";
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private File fileDir; // data dir of the application

	//region Payload SQL

	public static final String TABLE_PAYLOAD = "payload";
	public static final String PAYLOAD_KEY_DB_ID = "_id";           // 0
	public static final String PAYLOAD_KEY_BASE_TYPE = "base_type"; // 1
	public static final String PAYLOAD_KEY_JSON = "json";           // 2
	private static final String PAYLOAD_KEY_CONVERSATION_ID = "conversation_id"; // 3
	private static final String PAYLOAD_KEY_TOKEN = "token"; // 4

	private static final String TABLE_CREATE_PAYLOAD =
		"CREATE TABLE " + TABLE_PAYLOAD +
			" (" +
			PAYLOAD_KEY_DB_ID + " INTEGER PRIMARY KEY, " +
			PAYLOAD_KEY_BASE_TYPE + " TEXT, " +
			PAYLOAD_KEY_JSON + " TEXT," +
			PAYLOAD_KEY_CONVERSATION_ID + " TEXT," +
			PAYLOAD_KEY_TOKEN + " TEXT" +
			");";

	public static final String QUERY_PAYLOAD_GET_NEXT_TO_SEND = "SELECT * FROM " + TABLE_PAYLOAD + " ORDER BY " + PAYLOAD_KEY_DB_ID + " ASC LIMIT 1";
	private static final String QUERY_UPDATE_INCOMPLETE_PAYLOADS = StringUtils.format("UPDATE %s SET %s = ?, %s = ? WHERE %s IS NULL OR %s IS NULL", TABLE_PAYLOAD, PAYLOAD_KEY_CONVERSATION_ID, PAYLOAD_KEY_TOKEN, PAYLOAD_KEY_CONVERSATION_ID, PAYLOAD_KEY_TOKEN);

	private static final String QUERY_PAYLOAD_GET_ALL_MESSAGE_IN_ORDER = "SELECT * FROM " + TABLE_PAYLOAD + " WHERE " + PAYLOAD_KEY_BASE_TYPE + " = ?" + " ORDER BY " + PAYLOAD_KEY_DB_ID + " ASC";
	private static final String UPGRADE_V2_to_v3_ALTER_PAYLOAD_ADD_CONVERSATION_ID = "ALTER TABLE " + TABLE_PAYLOAD + " ADD COLUMN " + PAYLOAD_KEY_CONVERSATION_ID + " TEXT";
	private static final String UPGRADE_V2_to_v3_ALTER_PAYLOAD_ADD_TOKEN = "ALTER TABLE " + TABLE_PAYLOAD + " ADD COLUMN " + PAYLOAD_KEY_TOKEN + " TEXT";

	//endregion

	//region Message SQL

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

	private static final String QUERY_MESSAGE_GET_BY_NONCE = "SELECT * FROM " + TABLE_MESSAGE + " WHERE " + MESSAGE_KEY_NONCE + " = ?";
	// Coalesce returns the second arg if the first is null. This forces the entries with null IDs to be ordered last in the list until they do have IDs because they were sent and retrieved from the server.
	private static final String QUERY_MESSAGE_GET_ALL_IN_ORDER = "SELECT * FROM " + TABLE_MESSAGE + " ORDER BY COALESCE(" + MESSAGE_KEY_ID + ", 'z') ASC";
	private static final String QUERY_MESSAGE_GET_LAST_ID = "SELECT " + MESSAGE_KEY_ID + " FROM " + TABLE_MESSAGE + " WHERE " + MESSAGE_KEY_STATE + " = '" + ApptentiveMessage.State.saved + "' AND " + MESSAGE_KEY_ID + " NOTNULL ORDER BY " + MESSAGE_KEY_ID + " DESC LIMIT 1";
	private static final String QUERY_MESSAGE_UNREAD = "SELECT " + MESSAGE_KEY_ID + " FROM " + TABLE_MESSAGE + " WHERE " + MESSAGE_KEY_READ + " = " + FALSE + " AND " + MESSAGE_KEY_ID + " NOTNULL";

	//endregion

	//region File SQL

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

	//region Compound Message FileStore SQL
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


	public ApptentiveDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		fileDir = context.getFilesDir();
	}

	//region Create & Upgrade

	/**
	 * This function is called only for new installs, and onUpgrade is not called in that case. Therefore, you must include the
	 * latest complete set of DDL here.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		ApptentiveLog.d(DATABASE, "ApptentiveDatabase.onCreate(db)");
		db.execSQL(TABLE_CREATE_PAYLOAD);
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
		ApptentiveLog.d(DATABASE, "ApptentiveDatabase.onUpgrade(db, %d, %d)", oldVersion, newVersion);
		switch (oldVersion) {
			case 1:
				upgradeVersion1to2(db);
			case 2:
				upgradeVersion2to3(db);
		}
	}

	private void upgradeVersion1to2(SQLiteDatabase db) {
		ApptentiveLog.i(DATABASE, "Upgrading Database from v1 to v2");
		db.execSQL(TABLE_CREATE_COMPOUND_FILESTORE);
		Cursor cursor = null;
		// Migrate legacy stored files to compound message associated files
		try {
			cursor = db.rawQuery("SELECT * FROM " + TABLE_FILESTORE, null);
			if (cursor.moveToFirst()) {
				do {
					String file_nonce = cursor.getString(0);
					// Stored File id was in the format of "apptentive-file-nonce"
					String patten = "apptentive-file-";
					String nonce = file_nonce.substring(file_nonce.indexOf(patten) + patten.length());
					ContentValues values = new ContentValues();
					values.put(COMPOUND_FILESTORE_KEY_MESSAGE_NONCE, nonce);
					// Legacy file was stored in db by name only. Need to get the full path when migrated
					String localFileName = cursor.getString(3);
					values.put(COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH, (new File(fileDir, localFileName).getAbsolutePath()));
					values.put(COMPOUND_FILESTORE_KEY_MIME_TYPE, cursor.getString(1));
					// Original file name might not be stored, i.e. sent by API, in which case, local stored file name will be used.
					String originalFileName = cursor.getString(2);
					if (TextUtils.isEmpty(originalFileName)) {
						originalFileName = localFileName;
					}
					values.put(COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI, originalFileName);
					values.put(COMPOUND_FILESTORE_KEY_REMOTE_URL, cursor.getString(4));
					values.put(COMPOUND_FILESTORE_KEY_CREATION_TIME, 0); // we didn't store creation time of legacy file message
					db.insert(TABLE_COMPOUND_MESSAGE_FILESTORE, null, values);

				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
		}
		// Migrate legacy message types to CompoundMessage Type
		try {
			cursor = db.rawQuery(QUERY_MESSAGE_GET_ALL_IN_ORDER, null);
			if (cursor.moveToFirst()) {
				do {
					String json = cursor.getString(6);
					JSONObject root = null;
					boolean bUpdateRecord = false;
					try {
						root = new JSONObject(json);
						ApptentiveMessage.Type type = ApptentiveMessage.Type.valueOf(root.getString(ApptentiveMessage.KEY_TYPE));
						switch (type) {
							case TextMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, true);
								bUpdateRecord = true;
								break;
							case FileMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, false);
								bUpdateRecord = true;
								break;
							case AutomatedMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, true);
								root.put(ApptentiveMessage.KEY_AUTOMATED, true);
								bUpdateRecord = true;
								break;
							default:
								break;
						}
						if (bUpdateRecord) {
							String databaseId = cursor.getString(0);
							ContentValues messageValues = new ContentValues();
							messageValues.put(MESSAGE_KEY_JSON, root.toString());
							db.update(TABLE_MESSAGE, messageValues, MESSAGE_KEY_DB_ID + " = ?", new String[]{databaseId});
						}
					} catch (JSONException e) {
						ApptentiveLog.v(DATABASE, "Error parsing json as Message: %s", e, json);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
		}

		// Migrate all pending payload messages
		// Migrate legacy message types to CompoundMessage Type
		try {
			cursor = db.rawQuery(QUERY_PAYLOAD_GET_ALL_MESSAGE_IN_ORDER, new String[]{Payload.BaseType.message.name()});
			if (cursor.moveToFirst()) {
				do {
					String json = cursor.getString(2);
					JSONObject root;
					boolean bUpdateRecord = false;
					try {
						root = new JSONObject(json);
						ApptentiveMessage.Type type = ApptentiveMessage.Type.valueOf(root.getString(ApptentiveMessage.KEY_TYPE));
						switch (type) {
							case TextMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, true);
								bUpdateRecord = true;
								break;
							case FileMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, false);
								bUpdateRecord = true;
								break;
							case AutomatedMessage:
								root.put(ApptentiveMessage.KEY_TYPE, ApptentiveMessage.Type.CompoundMessage.name());
								root.put(CompoundMessage.KEY_TEXT_ONLY, true);
								root.put(ApptentiveMessage.KEY_AUTOMATED, true);
								bUpdateRecord = true;
								break;
							default:
								break;
						}
						if (bUpdateRecord) {
							String databaseId = cursor.getString(0);
							ContentValues messageValues = new ContentValues();
							messageValues.put(PAYLOAD_KEY_JSON, root.toString());
							db.update(TABLE_PAYLOAD, messageValues, PAYLOAD_KEY_DB_ID + " = ?", new String[]{databaseId});
						}
					} catch (JSONException e) {
						ApptentiveLog.v(DATABASE, "Error parsing json as Message: %s", e, json);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
		}
	}

	private void upgradeVersion2to3(SQLiteDatabase db) {
		ApptentiveLog.i(DATABASE, "Upgrading Database from v2 to v3");
		db.execSQL(UPGRADE_V2_to_v3_ALTER_PAYLOAD_ADD_CONVERSATION_ID);
		db.execSQL(UPGRADE_V2_to_v3_ALTER_PAYLOAD_ADD_TOKEN);
	}

	//endregion

	//region Payloads

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	public void addPayload(Payload... payloads) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.beginTransaction();
			for (Payload payload : payloads) {
				ContentValues values = new ContentValues();
				values.put(PAYLOAD_KEY_BASE_TYPE, payload.getBaseType().name());
				values.put(PAYLOAD_KEY_JSON, payload.toString());
				values.put(PAYLOAD_KEY_CONVERSATION_ID, payload.getConversationId());
				values.put(PAYLOAD_KEY_TOKEN, payload.getToken());
				db.insert(TABLE_PAYLOAD, null, values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "addPayload EXCEPTION: " + sqe.getMessage());
		}
	}

	public void deletePayload(Payload payload) {
		if (payload != null) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				db.delete(TABLE_PAYLOAD, PAYLOAD_KEY_DB_ID + " = ?", new String[]{Long.toString(payload.getDatabaseId())});
			} catch (SQLException sqe) {
				ApptentiveLog.e(DATABASE, "deletePayload EXCEPTION: " + sqe.getMessage());
			}
		}
	}

	public void deleteAllPayloads() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.delete(TABLE_PAYLOAD, "", null);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteAllPayloads EXCEPTION: " + sqe.getMessage());
		}
	}

	public Payload getOldestUnsentPayload() {

		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.rawQuery(QUERY_PAYLOAD_GET_NEXT_TO_SEND, null);
			Payload payload = null;
			if (cursor.moveToFirst()) {
				long databaseId = Long.parseLong(cursor.getString(0));
				Payload.BaseType baseType = Payload.BaseType.parse(cursor.getString(1));
				String json = cursor.getString(2);
				String conversationId = cursor.getString(3);
				String token = cursor.getString(4);
				payload = PayloadFactory.fromJson(json, baseType);
				if (payload != null) {
					payload.setDatabaseId(databaseId);
					payload.setConversationId(conversationId);
					payload.setToken(token);
				}
			}
			return payload;
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "getOldestUnsentPayload EXCEPTION: " + sqe.getMessage());
			return null;
		} finally {
			ensureClosed(cursor);
		}
	}

	public void updateIncompletePayloads(String conversationId, String token) {
		if (StringUtils.isNullOrEmpty(conversationId)) {
			throw new IllegalArgumentException("Conversation id is null or empty");
		}
		if (StringUtils.isNullOrEmpty(token)) {
			throw new IllegalArgumentException("Token is null or empty");
		}
		Cursor cursor = null;
		try {
			SQLiteDatabase db = getWritableDatabase();
			cursor = db.rawQuery(QUERY_UPDATE_INCOMPLETE_PAYLOADS, new String[] { conversationId , token});
			cursor.moveToFirst(); // we need to move a cursor in order to update database
			ApptentiveLog.v(DATABASE, "Updated missing conversation ids");
		} catch (SQLException e) {
			ApptentiveLog.e(e, "Exception while updating missing conversation ids");
		} finally {
			ensureClosed(cursor);
		}
	}

	//endregion

	//region Messages

	public synchronized int getUnreadMessageCount() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_UNREAD, null);
			return cursor.getCount();
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "getUnreadMessageCount EXCEPTION: " + sqe.getMessage());
			return 0;
		} finally {
			ensureClosed(cursor);
		}
	}

	public synchronized void deleteAllMessages() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.delete(TABLE_MESSAGE, "", null);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteAllMessages EXCEPTION: " + sqe.getMessage());
		}
	}

	public synchronized void deleteMessage(String nonce) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_MESSAGE, MESSAGE_KEY_NONCE + " = ?", new String[]{nonce});
			ApptentiveLog.d(DATABASE, "Deleted %d messages.", deleted);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteMessage EXCEPTION: " + sqe.getMessage());
		}
	}

	//endregion

	//region Files

	public void deleteAssociatedFiles(String messageNonce) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_COMPOUND_MESSAGE_FILESTORE, COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?", new String[]{messageNonce});
			ApptentiveLog.d(DATABASE, "Deleted %d stored files.", deleted);
		} catch (SQLException sqe) {
			ApptentiveLog.e(DATABASE, "deleteAssociatedFiles EXCEPTION: " + sqe.getMessage());
		}
	}

	public List<StoredFile> getAssociatedFiles(String nonce) {
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
	public boolean addCompoundMessageFiles(List<StoredFile> associatedFiles) {
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
		} finally {
			return ret != -1;
		}
	}

	//endregion

	// region Helpers

	private void ensureClosed(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			ApptentiveLog.w(DATABASE, "Error closing SQLite cursor.", e);
		}
	}

	public void reset(Context context) {
		/**
		 * The following ONLY be used during development and testing. It will delete the database, including all saved
		 * payloads, messages, and files.
		 */
		context.deleteDatabase(DATABASE_NAME);
	}

	//endregion
}