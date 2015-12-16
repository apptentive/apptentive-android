/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * There can be only one. SQLiteOpenHelper per database name that is. All new Apptentive tables must be defined here.
 *
 * @author Sky Kelsey
 */
public class ApptentiveDatabase extends SQLiteOpenHelper implements PayloadStore, EventStore, MessageStore {

	// COMMON
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "apptentive";
	private static final int TRUE = 1;
	private static final int FALSE = 0;

	// PAYLOAD
	private static final String TABLE_PAYLOAD = "payload";
	private static final String PAYLOAD_KEY_DB_ID = "_id";           // 0
	private static final String PAYLOAD_KEY_BASE_TYPE = "base_type"; // 1
	private static final String PAYLOAD_KEY_JSON = "json";           // 2

	private static final String TABLE_CREATE_PAYLOAD =
			"CREATE TABLE " + TABLE_PAYLOAD +
					" (" +
					PAYLOAD_KEY_DB_ID + " INTEGER PRIMARY KEY, " +
					PAYLOAD_KEY_BASE_TYPE + " TEXT, " +
					PAYLOAD_KEY_JSON + " TEXT" +
					");";

	private static final String QUERY_PAYLOAD_GET_NEXT_TO_SEND = "SELECT * FROM " + TABLE_PAYLOAD + " ORDER BY " + PAYLOAD_KEY_DB_ID + " ASC LIMIT 1";

	private static final String QUERY_PAYLOAD_GET_ALL_MESSAGE_IN_ORDER = "SELECT * FROM " + TABLE_PAYLOAD + " WHERE " + PAYLOAD_KEY_BASE_TYPE + " = ?" + " ORDER BY " + PAYLOAD_KEY_DB_ID + " ASC";


	// MESSAGE
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

	// FileStore
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

	/* Compund Message FileStore:
	 * For compound message stored in TABLE_MESSAGE, each associated file will add a row to this table
	 * uing the message's "nonce" key
	 */
	private static final String TABLE_COMPOUND_MESSSAGE_FILESTORE = "compound_message_file_store"; // table filePath
	private static final String COMPOUND_FILESTORE_KEY_DB_ID = "_id";                         // 0
	private static final String COMPOUND_FILESTORE_KEY_MESSAGE_NONCE = "nonce"; // message nonce of the compound message
	private static final String COMPOUND_FILESTORE_KEY_MIME_TYPE = "mime_type"; // mine type of the file
	private static final String COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI = "local_uri"; // original uriString or file path of source file (empty for received file)
	private static final String COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH = "local_path"; // path to the local cached version
	private static final String COMPOUND_FILESTORE_KEY_REMOTE_URL = "apptentive_url";  // original server url of received file (empty for sent file)
	private static final String COMPOUND_FILESTORE_KEY_CREATION_TIME = "creation_time"; // creation time of the original file
	// Create the initial table. Use nonce and local cache path as primary key because both sent/received files will have a local cached copy
	private static final String TABLE_CREATE_COMPOUND_FILESTORE =
			"CREATE TABLE " + TABLE_COMPOUND_MESSSAGE_FILESTORE +
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
	private static final String QUERY_MESSAGE_FILES_GET_BY_NONCE = "SELECT * FROM " + TABLE_COMPOUND_MESSSAGE_FILESTORE + " WHERE " + COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?";

	private static ApptentiveDatabase instance;

	private File fileDir; // data dir of the application

	public static synchronized ApptentiveDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new ApptentiveDatabase(context.getApplicationContext());
		}
		return instance;
	}

	public static void ensureClosed(SQLiteDatabase db) {
		try {
			if (db != null) {
				db.close();
			}
		} catch (Exception e) {
			Log.w("Error closing SQLite database.", e);
		}
	}

	public static void ensureClosed(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			Log.w("Error closing SQLite cursor.", e);
		}
	}

	private ApptentiveDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		fileDir = context.getFilesDir();
	}

	/**
	 * This function is called only for new installs, and onUpgrade is not called in that case. Therefore, you must include the
	 * latest complete set of DDL here.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("ApptentiveDatabase.onCreate(db)");
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
		Log.d("ApptentiveDatabase.onUpgrade(db, %d, %d)", oldVersion, newVersion);
		switch (oldVersion) {
			case 1:
				if (newVersion == 2) {
					db.execSQL(TABLE_CREATE_COMPOUND_FILESTORE);
					migrateToCompoundMessage(db);
				}
		}
	}

	// PAYLOAD: This table is used to store all the Payloads we want to send to the server.

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	public synchronized void addPayload(Payload... payloads) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.beginTransaction();
			for (Payload payload : payloads) {
				ContentValues values = new ContentValues();
				values.put(PAYLOAD_KEY_BASE_TYPE, payload.getBaseType().name());
				values.put(PAYLOAD_KEY_JSON, payload.toString());
				db.insert(TABLE_PAYLOAD, null, values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLException sqe) {
			Log.e("addPayload EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized void deletePayload(Payload payload) {
		if (payload != null) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				db.delete(TABLE_PAYLOAD, PAYLOAD_KEY_DB_ID + " = ?", new String[]{Long.toString(payload.getDatabaseId())});
			} catch (SQLException sqe) {
				Log.e("deletePayload EXCEPTION: " + sqe.getMessage());
			} finally {
				ensureClosed(db);
			}
		}
	}

	public synchronized void deleteAllPayloads() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.delete(TABLE_PAYLOAD, "", null);
		} catch (SQLException sqe) {
			Log.e("deleteAllPayloads EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized Payload getOldestUnsentPayload(Context appContext) {

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
				payload = PayloadFactory.fromJson(appContext, json, baseType);
				if (payload != null) {
					payload.setDatabaseId(databaseId);
				}
			}
			return payload;
		} catch (SQLException sqe) {
			Log.e("getOldestUnsentPayload EXCEPTION: " + sqe.getMessage());
			return null;
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
	}


	// MessageStore

	/**
	 * Saves the message into the message table, and also into the payload table so it can be sent to the server.
	 */
	public synchronized void addOrUpdateMessages(ApptentiveMessage... apptentiveMessages) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			for (ApptentiveMessage apptentiveMessage : apptentiveMessages) {
				Cursor cursor = null;
				try {
					cursor = db.rawQuery(QUERY_MESSAGE_GET_BY_NONCE, new String[]{apptentiveMessage.getNonce()});
					if (cursor.moveToFirst()) {
						// Update
						String databaseId = cursor.getString(0);
						ContentValues messageValues = new ContentValues();
						messageValues.put(MESSAGE_KEY_ID, apptentiveMessage.getId());
						messageValues.put(MESSAGE_KEY_STATE, apptentiveMessage.getState().name());
						if (apptentiveMessage.isRead()) { // A apptentiveMessage can't be unread after being read.
							messageValues.put(MESSAGE_KEY_READ, TRUE);
						}
						messageValues.put(MESSAGE_KEY_JSON, apptentiveMessage.toString());
						db.update(TABLE_MESSAGE, messageValues, MESSAGE_KEY_DB_ID + " = ?", new String[]{databaseId});
					} else {
						// Insert
						db.beginTransaction();
						ContentValues messageValues = new ContentValues();
						messageValues.put(MESSAGE_KEY_ID, apptentiveMessage.getId());
						messageValues.put(MESSAGE_KEY_CLIENT_CREATED_AT, apptentiveMessage.getClientCreatedAt());
						messageValues.put(MESSAGE_KEY_NONCE, apptentiveMessage.getNonce());
						messageValues.put(MESSAGE_KEY_STATE, apptentiveMessage.getState().name());
						messageValues.put(MESSAGE_KEY_READ, apptentiveMessage.isRead() ? TRUE : FALSE);
						messageValues.put(MESSAGE_KEY_JSON, apptentiveMessage.toString());
						db.insert(TABLE_MESSAGE, null, messageValues);
						db.setTransactionSuccessful();
						db.endTransaction();
					}
				} finally {
					ensureClosed(cursor);
				}
			}
		} catch (SQLException sqe) {
			Log.e("addOrUpdateMessages EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized void updateMessage(ApptentiveMessage apptentiveMessage) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.beginTransaction();
			ContentValues values = new ContentValues();
			values.put(MESSAGE_KEY_ID, apptentiveMessage.getId());
			values.put(MESSAGE_KEY_CLIENT_CREATED_AT, apptentiveMessage.getClientCreatedAt());
			values.put(MESSAGE_KEY_NONCE, apptentiveMessage.getNonce());
			values.put(MESSAGE_KEY_STATE, apptentiveMessage.getState().name());
			if (apptentiveMessage.isRead()) { // A apptentiveMessage can't be unread after being read.
				values.put(MESSAGE_KEY_READ, TRUE);
			}
			values.put(MESSAGE_KEY_JSON, apptentiveMessage.toString());
			db.update(TABLE_MESSAGE, values, MESSAGE_KEY_NONCE + " = ?", new String[]{apptentiveMessage.getNonce()});
			db.setTransactionSuccessful();
		} catch (SQLException sqe) {
			Log.e("updateMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			if (db != null) {
				db.endTransaction();
			}
			ensureClosed(db);
		}
	}

	public synchronized List<ApptentiveMessage> getAllMessages(Context appContext) {
		List<ApptentiveMessage> apptentiveMessages = new ArrayList<ApptentiveMessage>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_GET_ALL_IN_ORDER, null);
			if (cursor.moveToFirst()) {
				do {
					String json = cursor.getString(6);
					ApptentiveMessage apptentiveMessage = MessageFactory.fromJson(appContext, json);
					if (apptentiveMessage == null) {
						Log.e("Error parsing Record json from database: %s", json);
						continue;
					}
					apptentiveMessage.setDatabaseId(cursor.getLong(0));
					apptentiveMessage.setState(ApptentiveMessage.State.parse(cursor.getString(4)));
					apptentiveMessage.setRead(cursor.getInt(5) == TRUE);
					apptentiveMessages.add(apptentiveMessage);
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			Log.e("getAllMessages EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
		return apptentiveMessages;
	}

	public synchronized String getLastReceivedMessageId() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String ret = null;
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_GET_LAST_ID, null);
			if (cursor.moveToFirst()) {
				ret = cursor.getString(0);
			}
		} catch (SQLException sqe) {
			Log.e("getLastReceivedMessageId EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
		return ret;
	}

	public synchronized int getUnreadMessageCount() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_UNREAD, null);
			return cursor.getCount();
		} catch (SQLException sqe) {
			Log.e("getUnreadMessageCount EXCEPTION: " + sqe.getMessage());
			return 0;
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
	}

	public synchronized void deleteAllMessages() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.delete(TABLE_MESSAGE, "", null);
		} catch (SQLException sqe) {
			Log.e("deleteAllMessages EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized void deleteMessage(String nonce) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_MESSAGE, MESSAGE_KEY_NONCE + " = ?", new String[]{nonce});
			Log.d("Deleted %d messages.", deleted);
		} catch (SQLException sqe) {
			Log.e("deleteMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}


	//
	// File Store
	//

	public synchronized void migrateToCompoundMessage(SQLiteDatabase db) {
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
					db.insert(TABLE_COMPOUND_MESSSAGE_FILESTORE, null, values);

				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			Log.e("migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
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
						Log.v("Error parsing json as Message: %s", e, json);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			Log.e("migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
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
						Log.v("Error parsing json as Message: %s", e, json);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLException sqe) {
			Log.e("migrateToCompoundMessage EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
		}
	}

	public synchronized void deleteAssociatedFiles(String messageNonce) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_COMPOUND_MESSSAGE_FILESTORE, COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?", new String[]{messageNonce});
			Log.d("Deleted %d stored files.", deleted);
		} catch (SQLException sqe) {
			Log.e("deleteAssociatedFiles EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized List<StoredFile> getAssociatedFiles(String nonce) {
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
			Log.e("getAssociatedFiles EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
		return associatedFiles.size() > 0 ? associatedFiles : null;
	}

	/*
	 * Add a list of associated files to compound message file storage
	 * Caller of this method should ensure all associated files have the same message nonce
	 * @param associatedFiles list of associated files
	 * @return true if succeed
	 */
	public synchronized boolean addCompoundMessageFiles(List<StoredFile> associatedFiles) {
		String messageNonce = associatedFiles.get(0).getId();
		SQLiteDatabase db = null;
		long ret = -1;
		try {

			db = getWritableDatabase();
			db.beginTransaction();
			// Always delete existing rows with the same nonce to ensure add/update both work
			db.delete(TABLE_COMPOUND_MESSSAGE_FILESTORE, COMPOUND_FILESTORE_KEY_MESSAGE_NONCE + " = ?", new String[]{messageNonce});

			for (StoredFile file : associatedFiles) {
				ContentValues values = new ContentValues();
				values.put(COMPOUND_FILESTORE_KEY_MESSAGE_NONCE, file.getId());
				values.put(COMPOUND_FILESTORE_KEY_LOCAL_CACHE_PATH, file.getLocalFilePath());
				values.put(COMPOUND_FILESTORE_KEY_MIME_TYPE, file.getMimeType());
				values.put(COMPOUND_FILESTORE_KEY_LOCAL_ORIGINAL_URI, file.getSourceUriOrPath());
				values.put(COMPOUND_FILESTORE_KEY_REMOTE_URL, file.getApptentiveUri());
				values.put(COMPOUND_FILESTORE_KEY_CREATION_TIME, file.getCreationTime());
				ret = db.insert(TABLE_COMPOUND_MESSSAGE_FILESTORE, null, values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLException sqe) {
			Log.e("addCompoundMessageFiles EXCEPTION: " + sqe.getMessage());
		} finally {
			ensureClosed(db);
			return ret != -1;
		}
	}

	/**
	 * This method should ONLY be used during development and testing. It will delete the database, including all saved
	 * payloads, messages, and files.
	 */
	public static void reset(Context context) {
		synchronized (instance) {
			context.deleteDatabase(DATABASE_NAME);
			instance = null;
		}
	}
}
