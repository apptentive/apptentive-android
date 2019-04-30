package com.apptentive.android.sdk.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.JsonPayload;
import com.apptentive.android.sdk.model.PayloadType;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.DatabaseColumn;
import com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.PayloadEntry;
import com.apptentive.android.sdk.storage.legacy.LegacyPayloadFactory;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;
import static com.apptentive.android.sdk.ApptentiveLogTag.DATABASE;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.debug.Assert.notNull;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.storage.ApptentiveDatabaseHelper.SQL_CREATE_PAYLOAD_TABLE;

class DatabaseMigratorV2 extends DatabaseMigrator {


	private static final class LegacyPayloadEntry {
		static final String TABLE_NAME = "legacy_payload";
		static final DatabaseColumn PAYLOAD_KEY_DB_ID = new DatabaseColumn(0, "_id");
		static final DatabaseColumn PAYLOAD_KEY_BASE_TYPE = new DatabaseColumn(1, "base_type");
		static final DatabaseColumn PAYLOAD_KEY_JSON = new DatabaseColumn(2, "json");
	}

	private static final String SQL_SELECT_LEGACY_PAYLOADS = "SELECT * FROM " + LegacyPayloadEntry.TABLE_NAME +
	                                                         " ORDER BY " + LegacyPayloadEntry.PAYLOAD_KEY_DB_ID;
	private static final String SQL_SELECT_MESSAGES_IN_ORDER = "SELECT * FROM message ORDER BY COALESCE(id, 'z') ASC";
	private static final String SQL_BACKUP_LEGACY_PAYLOAD_TABLE = String.format("ALTER TABLE %s RENAME TO %s;", PayloadEntry.TABLE_NAME, LegacyPayloadEntry.TABLE_NAME);
	private static final String SQL_DELETE_LEGACY_PAYLOAD_TABLE = String.format("DROP TABLE %s;", LegacyPayloadEntry.TABLE_NAME);

	public DatabaseMigratorV2(Encryption encryption, File payloadDataDir) {
		super(encryption, payloadDataDir);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		 * 1. Rename payload table to legacy_payload
		 * 2. Create new payload table with new columns
		 * 2. select all payloads in temp_payload
		 * 3.   load each into a the new payload object format
		 * 4.   Save each into the new payload table
		 * 5. Drop temp_payload
		 */
		Cursor cursor = null;
		try {
			db.beginTransaction();

			// 1. Rename existing "payload" table to "legacy_payload"
			ApptentiveLog.v(DATABASE, "\t1. Backing up \"payloads\" database to \"legacy_payloads\"");
			db.execSQL(SQL_BACKUP_LEGACY_PAYLOAD_TABLE);

			// 2. Create new Payload table as "payload"
			ApptentiveLog.v(DATABASE, "\t2. Creating new \"payloads\" database.");
			db.execSQL(SQL_CREATE_PAYLOAD_TABLE);

			// 3. Load legacy payloads
			ApptentiveLog.v(DATABASE, "\t3. Loading legacy payloads.");
			cursor = db.rawQuery(SQL_SELECT_LEGACY_PAYLOADS, null);

			ApptentiveLog.v(DATABASE, "4. Save payloads into new table.");
			JsonPayload payload;
			while (cursor.moveToNext()) {
				PayloadType payloadType = PayloadType.parse(cursor.getString(1));
				String json = cursor.getString(LegacyPayloadEntry.PAYLOAD_KEY_JSON.index);

				payload = LegacyPayloadFactory.createPayload(payloadType, json);
				if (payload == null) {
					ApptentiveLog.d(DATABASE, "Unable to construct payload of type %s. Continuing.", payloadType.name());
					continue;
				}

				// the legacy payload format didn't store 'nonce' in the database so we need to extract if from json
				String nonce = payload.optString("nonce", null);
				if (nonce == null) {
					nonce = UUID.randomUUID().toString(); // if 'nonce' is missing - generate a new one
				}
				payload.setNonce(nonce);

				// 4. Save each payload in the new table.
				ApptentiveLog.v(DATABASE, "Payload of type %s:, %s", payload.getPayloadType().name(), payload);
				ContentValues values = new ContentValues();
				values.put(PayloadEntry.COLUMN_IDENTIFIER.name, notNull(payload.getNonce()));
				values.put(PayloadEntry.COLUMN_PAYLOAD_TYPE.name, notNull(payload.getPayloadType().name()));
				values.put(PayloadEntry.COLUMN_CONTENT_TYPE.name, notNull(payload.getHttpRequestContentType()));
				// The token is encrypted inside the payload body for Logged In Conversations. In that case, don't store it here.
				if (!payload.isAuthenticated()) {
					values.put(PayloadEntry.COLUMN_AUTH_TOKEN.name, encrypt(payload.getConversationToken())); // might be null
				}
				values.put(PayloadEntry.COLUMN_CONVERSATION_ID.name, payload.getConversationId()); // might be null
				values.put(PayloadEntry.COLUMN_REQUEST_METHOD.name, payload.getHttpRequestMethod().name());
				values.put(PayloadEntry.COLUMN_PATH.name, payload.getHttpEndPoint(
					StringUtils.isNullOrEmpty(payload.getConversationId()) ? "${conversationId}" : payload.getConversationId()) // if conversation id is missing we replace it with a place holder and update it later
				);

				File dest = getPayloadBodyFile(payload.getNonce());
				ApptentiveLog.v(DATABASE, "Saving payload body to: %s", hideIfSanitized(dest));
				writeToFile(dest, payload.renderData(), !payload.isAuthenticated());

				values.put(PayloadEntry.COLUMN_AUTHENTICATED.name, payload.isAuthenticated() ? TRUE : FALSE);

				db.insert(PayloadEntry.TABLE_NAME, null, values);
			}

			// 5. Migrate messages
			ApptentiveLog.v(DATABASE, "\t6. Migrating messages.");
			migrateMessages(db);

			// 6. Finally, delete the temporary legacy table
			ApptentiveLog.v(DATABASE, "\t6. Delete temporary \"legacy_payloads\" database.");
			db.execSQL(SQL_DELETE_LEGACY_PAYLOAD_TABLE);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			ApptentiveLog.e(DATABASE, e, "Error in upgradeVersion2to3()");
			logException(e);
		} finally {
			ensureClosed(cursor);
			if (db != null) {
				db.endTransaction();
			}
		}
	}

	private void migrateMessages(SQLiteDatabase db) {
		try {
			final List<ApptentiveMessage> messages = getAllMessages(db);
			dispatchConversationTask(new ConversationDispatchTask() {
				@Override
				protected boolean execute(Conversation conversation) {
					conversation.getMessageManager().addMessages(messages.toArray(new ApptentiveMessage[messages.size()]));
					return true;
				}
			}, "migrate messages");
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while trying to migrate messages");
			logException(e);
		}
	}

	private List<ApptentiveMessage> getAllMessages(SQLiteDatabase db) {
		List<ApptentiveMessage> messages = new ArrayList<>();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(SQL_SELECT_MESSAGES_IN_ORDER, null);
			while (cursor.moveToNext()) {
				String json = cursor.getString(6);
				ApptentiveMessage message = MessageFactory.fromJson(json);
				if (message == null) {
					ApptentiveLog.e(MESSAGES, "Error parsing Record json from database: %s", json);
					continue;
				}
				message.setId(cursor.getString(1));
				message.setCreatedAt(cursor.getDouble(2));
				message.setNonce(cursor.getString(3));
				message.setState(ApptentiveMessage.State.parse(cursor.getString(4)));
				message.setRead(cursor.getInt(5) == TRUE);
				messages.add(message);
			}
		} finally {
			ensureClosed(cursor);
		}
		return messages;
	}
}
