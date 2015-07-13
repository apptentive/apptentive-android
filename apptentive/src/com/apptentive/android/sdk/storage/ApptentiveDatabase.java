/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * There can be only one. SQLiteOpenHelper per database name that is. All new Apptentive tables must be defined here.
 *
 * @author Sky Kelsey
 */
public class ApptentiveDatabase extends SQLiteOpenHelper implements PayloadStore, EventStore, MessageStore, FileStore {

	// COMMON
	private static final int DATABASE_VERSION = 1;
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


	private static ApptentiveDatabase instance;

	public static ApptentiveDatabase getInstance(Context context) {
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
			case 2:
		}
	}

	// PAYLOAD: This table is used to store all the Payloads we want to send to the server.

	// Try not to hit the database unless we need to. This value is polled every few seconds.
	private boolean payloadsDirty = true;

	/**
	 * If an item with the same nonce as an item passed in already exists, it is overwritten by the item. Otherwise
	 * a new message is added.
	 */
	public synchronized void addPayload(Payload... payloads) {
		payloadsDirty = true;
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
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized void deletePayload(Payload payload) {
		payloadsDirty = true;
		if (payload != null) {
			SQLiteDatabase db = null;
			try {
				db = getWritableDatabase();
				db.delete(TABLE_PAYLOAD, PAYLOAD_KEY_DB_ID + " = ?", new String[]{Long.toString(payload.getDatabaseId())});
			} finally {
				ensureClosed(db);
			}
		}
	}

	public synchronized void deleteAllPayloads() {
		payloadsDirty = true;
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.delete(TABLE_PAYLOAD, "", null);
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized Payload getOldestUnsentPayload() {

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
				payload = PayloadFactory.fromJson(json, baseType);
				payload.setDatabaseId(databaseId);
			}
			payloadsDirty = false;
			return payload;
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
		} finally {
			ensureClosed(db);
		}
	}

	public synchronized void updateMessage(ApptentiveMessage apptentiveMessage) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
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
		} finally {
			if (db != null) {
				db.endTransaction();
			}
			ensureClosed(db);
		}
	}

	public synchronized List<ApptentiveMessage> getAllMessages() {
		List<ApptentiveMessage> apptentiveMessages = new ArrayList<ApptentiveMessage>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_GET_ALL_IN_ORDER, null);
			if (cursor.moveToFirst()) {
				do {
					String json = cursor.getString(6);
					ApptentiveMessage apptentiveMessage = MessageFactory.fromJson(json);
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
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
		return apptentiveMessages;
	}

	public synchronized String getLastReceivedMessageId() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_GET_LAST_ID, null);
			String ret = null;
			if (cursor.moveToFirst()) {
				ret = cursor.getString(0);
			}
			return ret;
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
	}

	public synchronized int getUnreadMessageCount() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_MESSAGE_UNREAD, null);
			return cursor.getCount();
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
		} finally {
			ensureClosed(db);
		}
	}


	//
	// File Store
	//

	public synchronized StoredFile getStoredFile(String id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FILESTORE + " WHERE " + FILESTORE_KEY_ID + " = ?", new String[]{id});
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery("SELECT * FROM " + TABLE_FILESTORE + " WHERE " + FILESTORE_KEY_ID + " = ?", new String[]{id});
			StoredFile ret = null;
			if (cursor.moveToFirst()) {
				ret = new StoredFile();
				ret.setId(id);
				ret.setMimeType(cursor.getString(1));
				ret.setOriginalUri(cursor.getString(2));
				ret.setLocalFilePath(cursor.getString(3));
				ret.setApptentiveUri(cursor.getString(4));
			}
			return ret;
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
	}

	public synchronized boolean putStoredFile(StoredFile storedFile) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(FILESTORE_KEY_ID, storedFile.getId());
			values.put(FILESTORE_KEY_MIME_TYPE, storedFile.getMimeType());
			values.put(FILESTORE_KEY_ORIGINAL_URL, storedFile.getOriginalUri());
			values.put(FILESTORE_KEY_LOCAL_URL, storedFile.getLocalFilePath());
			values.put(FILESTORE_KEY_APPTENTIVE_URL, storedFile.getApptentiveUri());
			cursor = db.rawQuery("SELECT * FROM " + TABLE_FILESTORE + " WHERE " + FILESTORE_KEY_ID + " = ?", new String[]{storedFile.getId()});
			boolean doUpdate = cursor.moveToFirst();
			long ret;
			if (doUpdate) {
				ret = db.update(TABLE_FILESTORE, values, FILESTORE_KEY_ID + " = ?", new String[]{storedFile.getId()});
			} else {
				ret = db.insert(TABLE_FILESTORE, null, values);
			}
			return ret != -1;
		} finally {
			ensureClosed(cursor);
			ensureClosed(db);
		}
	}

	public synchronized void deleteStoredFile(String id) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			int deleted = db.delete(TABLE_FILESTORE, MESSAGE_KEY_ID + " = ?", new String[]{id});
			Log.d("Deleted %d stored files.", deleted);
		} finally {
			ensureClosed(db);
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
