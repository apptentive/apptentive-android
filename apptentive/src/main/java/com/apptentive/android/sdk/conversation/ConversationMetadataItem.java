package com.apptentive.android.sdk.conversation;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.encryption.SecurityManager;
import com.apptentive.android.sdk.serialization.SerializableObject;
import com.apptentive.android.sdk.encryption.EncryptionKey;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import static com.apptentive.android.sdk.ApptentiveLog.hideIfSanitized;
import static com.apptentive.android.sdk.util.Util.getEncryptedFilename;
import static com.apptentive.android.sdk.util.Util.readNullableUTF;
import static com.apptentive.android.sdk.util.Util.writeNullableUTF;

/**
 * A light weight representation of the conversation object stored on the disk.
 */
public class ConversationMetadataItem implements SerializableObject {

	/**
	 * The state of the target conversation
	 */
	private ConversationState conversationState = ConversationState.UNDEFINED;

	/**
	 * Local conversation ID
	 */
	private final String localConversationId;

	/**
	 * Storage filename for conversation serialized data
	 */
	private final File dataFile;

	/**
	 * Storage filename for conversation serialized messages
	 */
	private final File messagesFile;

	/**
	 * Conversation ID which was received from the backend
	 */
	private @Nullable String conversationId;

	/**
	 * The token for active conversations
	 */
	private @Nullable String conversationToken;

	/**
	 * Key for encrypting logged-in conversations. We receive it from the server. Anonymous conversations
	 * would not have this key.
	 */
	private @Nullable String conversationEncryptionKey;

	/**
	 * An optional user ID for logged in conversations
	 */
	private @Nullable String userId;

	public ConversationMetadataItem(String localConversationId, String conversationId, File dataFile, File messagesFile) {
		if (localConversationId == null) {
			throw new IllegalArgumentException("Local conversation id is null");
		}

		if (dataFile == null) {
			throw new IllegalArgumentException("Data file is null");
		}

		if (messagesFile == null) {
			throw new IllegalArgumentException("Messages file is null");
		}

		this.localConversationId = localConversationId;
		this.conversationId = conversationId;
		this.dataFile = dataFile;
		this.messagesFile = messagesFile;
	}

	public ConversationMetadataItem(DataInput in) throws IOException {
		localConversationId = in.readUTF();
		conversationId = readNullableUTF(in);
		conversationToken = readNullableUTF(in);
		dataFile = getEncryptedFilename(new File(in.readUTF()));
		messagesFile = getEncryptedFilename(new File(in.readUTF()));
		conversationState = ConversationState.valueOf(in.readByte());
		conversationEncryptionKey = readNullableUTF(in);
		userId = readNullableUTF(in);
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeUTF(localConversationId);
		writeNullableUTF(out, conversationId);
		writeNullableUTF(out, conversationToken);
		out.writeUTF(dataFile.getAbsolutePath());
		out.writeUTF(messagesFile.getAbsolutePath());
		out.writeByte(conversationState.ordinal());
		writeNullableUTF(out, conversationEncryptionKey);
		writeNullableUTF(out, userId);
	}

	public @Nullable String getConversationId() {
		return conversationId;
	}

	public String getLocalConversationId() {
		return localConversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public ConversationState getConversationState() {
		return conversationState;
	}

	public void setConversationState(ConversationState conversationState) {
		this.conversationState = conversationState;
	}

	public @Nullable String getConversationEncryptionKey() {
		return conversationEncryptionKey;
	}

	public void setConversationEncryptionKey(@Nullable String conversationEncryptionKey) {
		this.conversationEncryptionKey = conversationEncryptionKey;
	}

	public @Nullable String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public @Nullable String getConversationToken() {
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		this.conversationToken = conversationToken;
	}

	public File getDataFile() {
		return dataFile;
	}

	public File getMessagesFile() {
		return messagesFile;
	}

	@Override
	public String toString() {
		return "ConversationMetadataItem{" +
			       "conversationState=" + conversationState +
			       ", localConversationId='" + localConversationId + '\'' +
			       ", conversationId='" + conversationId + '\'' +
			       ", conversationToken='" + hideIfSanitized(conversationToken) + '\'' +
			       ", dataFile=" + dataFile +
			       ", messagesFile=" + messagesFile +
			       ", conversationEncryptionKey='" + hideIfSanitized(conversationEncryptionKey) + '\'' +
			       ", userId='" + userId + '\'' +
			       '}';
	}
}
