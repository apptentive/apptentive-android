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
import com.apptentive.android.sdk.SessionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class SQLitePersistentSessionQueue extends SQLiteOpenHelper implements PersistentSessionQueue {

	private static int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "apptentive";
	private static final String TABLE_SESSION_EVENT = "session_event";
	private static final String KEY_ID = "id";
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_ACTION = "action";
	private static final String KEY_ACTIVITY = "activity";

	private static final String SESSION_QUEUE_TABLE_CREATE =
		"CREATE TABLE " + TABLE_SESSION_EVENT +
			" (" +
					KEY_ID        + " INTEGER PRIMARY KEY, " +
					KEY_TIMESTAMP + " INTEGER, " +
					KEY_ACTION    + " TEXT, " +
					KEY_ACTIVITY  + " TEXT" +
				");";

	public SQLitePersistentSessionQueue(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SESSION_QUEUE_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int i, int i1) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION_EVENT);
		onCreate(db);
	}

	public void addEvents(SessionEvent... events) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		for(SessionEvent event : events) {
			ContentValues values = new ContentValues();
			values.put(KEY_TIMESTAMP, event.getTimestamp());
			values.put(KEY_ACTION, event.getAction().name());
			values.put(KEY_ACTIVITY, event.getActivityName());
			db.insert(TABLE_SESSION_EVENT, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void deleteEvents(SessionEvent... events) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		for(SessionEvent event : events) {
			db.delete(TABLE_SESSION_EVENT, KEY_ID + " = ?", new String[]{String.valueOf(event.getId())});
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public void deleteAllEvents() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SESSION_EVENT, "", null);
		db.close();
	}

	public List<SessionEvent> getAllEvents() {
		List<SessionEvent> events = new ArrayList<SessionEvent>();
		String query = "SELECT * FROM " + TABLE_SESSION_EVENT;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if(cursor.moveToFirst()) {
			do {
				SessionEvent event = new SessionEvent();
				event.setId(Long.parseLong(cursor.getString(0)));
				event.setTimestamp(Long.parseLong(cursor.getString(1)));
				event.setAction(SessionEvent.Action.valueOf(cursor.getString(2)));
				event.setActivityName(cursor.getString(3));
				events.add(event);
			} while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return events;
	}
}
