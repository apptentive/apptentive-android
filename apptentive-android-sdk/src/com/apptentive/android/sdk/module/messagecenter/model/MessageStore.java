package com.apptentive.android.sdk.module.messagecenter.model;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public interface MessageStore {

	public void addMessages(Message... messages);

	public void updateMessageWithPayloadId(String payloadId, Message message);

	public void deleteAllMessages();

	public List<Message> getAllMessages();

	public String getLastMessageId();
}
