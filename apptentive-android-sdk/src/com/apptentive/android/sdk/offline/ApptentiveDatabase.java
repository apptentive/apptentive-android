/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import com.apptentive.android.sdk.module.messagecenter.model.MessageStore;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * There can be only one. SQLiteOpenHelper per database name that is. All new Apptentive tables must be defined here.
 *
 * @author Sky Kelsey
 */
public class ApptentiveDatabase extends SQLiteOpenHelper implements PayloadStore, MessageStore {

	// COMMON

	private static int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "apptentive";

	// PAYLOAD
	private static final String TABLE_PAYLOAD = "payload";
	private static final String PAYLOAD_KEY_ID         = "id";         // 0
	private static final String PAYLOAD_KEY_PAYLOAD_ID = "payload_id"; // 1
	private static final String PAYLOAD_KEY_TYPE       = "type";       // 2
	private static final String PAYLOAD_KEY_JSON       = "json";       // 3

	private static final String TABLE_CREATE_DATABASE_PAYLOAD =
			"CREATE TABLE " + TABLE_PAYLOAD +
					" (" +
					PAYLOAD_KEY_ID         + " INTEGER PRIMARY KEY, " +
					PAYLOAD_KEY_PAYLOAD_ID + " TEXT, " +
					PAYLOAD_KEY_TYPE       + " TEXT, " +
					PAYLOAD_KEY_JSON       + " TEXT" +
					");";

	private static final String QUERY_PAYLOAD_LAST_PAYLOAD = "SELECT * FROM " + TABLE_PAYLOAD + " ORDER BY " + PAYLOAD_KEY_ID + " ASC LIMIT 1";


	// MESSAGE
	private static final String TABLE_MESSAGE = "message";

	private static final String MESSAGE_KEY_ID = "id";                 // 0
	private static final String MESSAGE_KEY_MESSAGE_ID = "message_id"; // 1
	private static final String MESSAGE_KEY_PAYLOAD_ID = "payload_id"; // 2
	private static final String MESSAGE_KEY_CREATED_AT = "created_at"; // 3
	private static final String MESSAGE_KEY_PRIORITY   = "priority";   // 4
	private static final String MESSAGE_KEY_TYPE       = "type";       // 5
	private static final String MESSAGE_KEY_DISPLAY    = "display";    // 6
	private static final String MESSAGE_KEY_JSON       = "json";       // 7

	private static final String TABLE_CREATE_MESSAGE =
			"CREATE TABLE " + TABLE_MESSAGE +
					" (" +
					MESSAGE_KEY_ID         + " INTEGER PRIMARY KEY, " +
					MESSAGE_KEY_MESSAGE_ID + " TEXT, " +
					MESSAGE_KEY_PAYLOAD_ID + " LONG, " +
					MESSAGE_KEY_CREATED_AT + " DOUBLE, " +
					MESSAGE_KEY_PRIORITY   + " INTEGER, " +
					MESSAGE_KEY_TYPE       + " TEXT, " +
					MESSAGE_KEY_DISPLAY    + " TEXT, " +
					MESSAGE_KEY_JSON       + " TEXT" +
					");";

	private static final String QUERY_MESSAGE_GET_LAST_MESSAGE_ID =
			"SELECT * FROM " + TABLE_MESSAGE + " WHERE " + MESSAGE_KEY_MESSAGE_ID + " NOTNULL ORDER BY " + MESSAGE_KEY_ID + " DESC LIMIT 1";


	// COMMON

	public ApptentiveDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE_DATABASE_PAYLOAD);
		db.execSQL(TABLE_CREATE_MESSAGE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int i, int i1) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYLOAD);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
		onCreate(db);
	}


	// PAYLOAD

	public synchronized void addPayload(Payload payload) {
		SQLiteDatabase db = this.getWritableDatabase();
		String json;
		json = payload.toString();
		ContentValues values = new ContentValues();
		values.put(PAYLOAD_KEY_PAYLOAD_ID, payload.getPayloadId());
		values.put(PAYLOAD_KEY_TYPE, payload.getPayloadType().name());
		values.put(PAYLOAD_KEY_JSON, json);
		db.insert(TABLE_PAYLOAD, null, values);
		db.close();
	}

	public synchronized Payload getNextPayload() {
		Payload payload = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery(QUERY_PAYLOAD_LAST_PAYLOAD, null);
			if (cursor.moveToFirst()) {
				long id = cursor.getLong(0);
				String payloadId = cursor.getString(1);
				String type = cursor.getString(2);
				String json = cursor.getString(3);
				try {
					Payload.PayloadType payloadType = Payload.PayloadType.valueOf(cursor.getString(2));
					switch (payloadType) {
						case RECORD:
							payload = new RecordPayload(json);
							break;
						case MESSAGE:
							payload = new Message(json);
							break;
					}
					payload.setId(id);
					payload.setPayloadId(payloadId);
					return payload;
				} catch (JSONException e) {
					Log.e("Unable to construct Message.", e);
				} catch (IllegalArgumentException e) {
					Log.e("Unknown type: " + type, e);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}

	public synchronized void deletePayload(Payload payload) {
		SQLiteDatabase db = this.getWritableDatabase();
		int ret = db.delete(TABLE_PAYLOAD, PAYLOAD_KEY_ID + " = ?", new String[]{String.valueOf(payload.getId())});
		Log.d("Deleted " + ret + " sent paylaod.");
		db.close();
	}


	// MESSAGE

	public synchronized void addMessages(Message... messages) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		for (Message message : messages) {
			String json;
			json = message.toString();
			ContentValues values = new ContentValues();
			values.put(MESSAGE_KEY_MESSAGE_ID, message.getMessageId());
			values.put(MESSAGE_KEY_PAYLOAD_ID, message.getPayloadId());
			values.put(MESSAGE_KEY_CREATED_AT, message.getCreatedAt());
			values.put(MESSAGE_KEY_PRIORITY, message.getPriority());
			values.put(MESSAGE_KEY_TYPE, message.getType());
			values.put(MESSAGE_KEY_DISPLAY, message.getDisplay());
			values.put(MESSAGE_KEY_JSON, json);
			db.insert(TABLE_MESSAGE, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public synchronized void updateMessageWithPayloadId(String payloadId, Message message) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE_KEY_MESSAGE_ID, message.getMessageId());
		values.put(MESSAGE_KEY_PAYLOAD_ID, (String) null);
		values.put(MESSAGE_KEY_CREATED_AT, message.getCreatedAt());
		values.put(MESSAGE_KEY_PRIORITY, message.getPriority());
		values.put(MESSAGE_KEY_TYPE, message.getType());
		values.put(MESSAGE_KEY_DISPLAY, message.getDisplay());
		values.put(MESSAGE_KEY_JSON, message.toString());
		db.update(TABLE_MESSAGE, values, "payload_id = ?", new String[]{payloadId});
		Log.d("Updated message " + message.getMessageId());
		db.close();
		return;
	}

	public synchronized void deleteAllMessages() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MESSAGE, "", null);
		db.close();
	}

	public synchronized List<Message> getAllMessages() {
		List<Message> messages = new ArrayList<Message>();
		String query = "SELECT * FROM " + TABLE_MESSAGE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			do {
				String json = cursor.getString(7);
				String type = cursor.getString(5);
				try {
					Message message = MessageManager.constructTypedMessage(json, type);
					message.setId(cursor.getLong(0));
					messages.add(message);
				} catch (JSONException e) {
					Log.e("Error parsing Message json from database: %s", e, json);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return messages;
	}

	public synchronized String getLastMessageId() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(QUERY_MESSAGE_GET_LAST_MESSAGE_ID, null);
		String ret = null;
		if (cursor.moveToFirst()) {
			ret = cursor.getString(1);
		}
		cursor.close();
		db.close();
		return ret;
	}
}
