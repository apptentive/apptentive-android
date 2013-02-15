package com.apptentive.android.sdk.model;

/**
 * @author Sky Kelsey
 */
public interface RecordStore {

	public void addOrUpdateItems(ActivityFeedItem... activityFeedItems);

	public void updateRecord(ActivityFeedItem records);

	public void deleteAllRecords();

	public ActivityFeedItem getRecordByNonce(String nonce);

	public ActivityFeedItem getOldestUnsentRecord();

	public void deleteRecord(ActivityFeedItem activityFeedItem);

}
