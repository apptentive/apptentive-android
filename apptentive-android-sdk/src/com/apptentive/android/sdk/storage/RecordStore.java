package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.ConversationItem;

/**
 * @author Sky Kelsey
 */
public interface RecordStore {

	public void addOrUpdateItems(ConversationItem... conversationItems);

	public void updateRecord(ConversationItem records);

	public void deleteAllRecords();

	public ConversationItem getRecordByNonce(String nonce);

	public ConversationItem getOldestUnsentRecord();

	public void deleteRecord(ConversationItem conversationItem);

}
