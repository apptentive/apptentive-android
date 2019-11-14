/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.ImageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.ApptentiveLogTag.PAYLOADS;

public class CompoundMessage extends ApptentiveMessage implements MessageCenterUtil.CompoundMessageCommonInterface {

	@SensitiveDataKey private static final String KEY_BODY = "body";
	public static final String KEY_TEXT_ONLY = "text_only";
	@SensitiveDataKey private static final String KEY_TITLE = "title";
	private static final String KEY_ATTACHMENTS = "attachments";

	private boolean isLast;

	private boolean hasNoAttachments = true;

	private final String boundary;

	/* For incoming message, this array stores attachment Urls
	 * StoredFile::apptentiveUri is set by the "url" of the remote attachment file
	 * StoredFile:localFilePath is set by the "thumbnail_url" of the remote attachment (maybe empty)
	 */
	private ArrayList<StoredFile> remoteAttachmentStoredFiles;

	static {
		registerSensitiveKeys(CompoundMessage.class);
	}

	// Default constructor will only be called when the message is created from local, a.k.a outgoing
	public CompoundMessage() {
		super();
		boundary = UUID.randomUUID().toString();
	}

	/**
	 * Construct a CompoundMessage when JSON is fetched from server, or repopulated from database.
	 *
	 * @param json The message JSON
	 */
	public CompoundMessage(String json) throws JSONException {
		super(json);
		boundary = UUID.randomUUID().toString();
		parseAttachmentsArray(json);
		hasNoAttachments = getTextOnly();
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/messages", conversationId);
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.POST;
	}

	@Override
	public String getHttpRequestContentType() {
		return String.format("%s;boundary=%s", isAuthenticated() ? "multipart/encrypted" : "multipart/mixed", boundary);
	}

	//endregion

	@Override
	protected void initType() {
		setType(Type.CompoundMessage);
	}

	// Get text message body, maybe empty
	@Override
	public String getBody() {
		return optString(KEY_BODY, null);
	}

	// Set text message body, maybe empty
	@Override
	public void setBody(String body) {
		put(KEY_BODY, body);
	}

	public String getTitle() {
		return optString(KEY_TITLE, null);
	}

	public void setTitle(String title) {
		put(KEY_TITLE, title);
	}

	public boolean getTextOnly() {
		return getBoolean(KEY_TEXT_ONLY);
	}

	public void setTextOnly(boolean bVal) {
		put(KEY_TEXT_ONLY, bVal);
	}

	private List<StoredFile> attachedFiles;

	public boolean setAssociatedImages(List<ImageItem> attachedImages) {

		if (attachedImages == null || attachedImages.size() == 0) {
			hasNoAttachments = true;
			return false;
		} else {
			hasNoAttachments = false;
		}
		setTextOnly(hasNoAttachments);
		ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
		for (ImageItem image : attachedImages) {
			StoredFile storedFile = new StoredFile();
			storedFile.setId(getNonce());
			storedFile.setApptentiveUri("");
			storedFile.setSourceUriOrPath(image.originalPath);
			// ToDo: look for local cache
			storedFile.setLocalFilePath(image.localCachePath);
			storedFile.setMimeType("image/jpeg");
			storedFile.setCreationTime(image.time);
			attachmentStoredFiles.add(storedFile);
		}

		attachedFiles = attachmentStoredFiles;

		boolean bRet = false;
		try {
			Future<Boolean> future = ApptentiveInternal.getInstance().getApptentiveTaskManager().addCompoundMessageFiles(attachmentStoredFiles);
			bRet = future.get();
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Unable to set associated images in worker thread");
			logException(e);
		} finally {
			return bRet;
		}
	}

	public boolean setAssociatedFiles(List<StoredFile> attachedFiles) {

		this.attachedFiles = attachedFiles;

		if (attachedFiles == null || attachedFiles.size() == 0) {
			hasNoAttachments = true;
			return false;
		} else {
			hasNoAttachments = false;
		}
		setTextOnly(hasNoAttachments);

		boolean bRet = false;
		try {
			Future<Boolean> future = ApptentiveInternal.getInstance().getApptentiveTaskManager().addCompoundMessageFiles(attachedFiles);
			bRet = future.get();
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Unable to set associated files in worker thread");
			logException(e);
		} finally {
			return bRet;
		}
	}

	public List<StoredFile> getAssociatedFiles() {
		if (hasNoAttachments) {
			return null;
		}
		List<StoredFile> associatedFiles = null;
		try {
			Future<List<StoredFile>> future = ApptentiveInternal.getInstance().getApptentiveTaskManager().getAssociatedFiles(getNonce());
			associatedFiles = future.get();
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Unable to get associated files in worker thread");
			logException(e);
		} finally {
			return associatedFiles;
		}
	}

	public void deleteAssociatedFiles() {
		try {
			Future<List<StoredFile>> future = ApptentiveInternal.getInstance().getApptentiveTaskManager().getAssociatedFiles(getNonce());
			List<StoredFile> associatedFiles = future.get();
			// Delete local cached files
			if (associatedFiles == null || associatedFiles.size() == 0) {
				return;
			}

			for (StoredFile file : associatedFiles) {
				File localFile = new File(file.getLocalFilePath());
				localFile.delete();
			}
			// Delete records from db
			ApptentiveInternal.getInstance().getApptentiveTaskManager().deleteAssociatedFiles(getNonce());
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, "Unable to delete associated files in worker thread");
			logException(e);
		}
	}


	@Override
	public boolean isLastSent() {
		return (isOutgoingMessage()) && isLast;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}

	public List<StoredFile> getRemoteAttachments() {
		return remoteAttachmentStoredFiles;
	}

	/* Parse attachment array in json. Only incoming compound message would have "attachments" key set
	 * @param messageString JSON string of the message
	 * @return true if attachment array is found in JSON
	 */
	private boolean parseAttachmentsArray(String messageString) throws JSONException {
		JSONObject root = new JSONObject(messageString);
		if (!root.isNull(KEY_ATTACHMENTS)) {
			JSONArray items = root.getJSONArray(KEY_ATTACHMENTS);
			remoteAttachmentStoredFiles = new ArrayList<StoredFile>();
			for (int i = 0; i < items.length(); i++) {
				String json = items.getJSONObject(i).toString();
				JSONObject attachment = new JSONObject(json);
				String mimeType = attachment.optString("content_type");
				StoredFile storedFile = new StoredFile();
				storedFile.setId(getNonce());
				storedFile.setApptentiveUri(attachment.optString("url"));
				storedFile.setSourceUriOrPath(attachment.optString("thumbnail_url"));
				storedFile.setLocalFilePath(attachment.optString(""));
				storedFile.setMimeType(mimeType);
				storedFile.setCreationTime(0);
				remoteAttachmentStoredFiles.add(storedFile);
			}
			if (remoteAttachmentStoredFiles.size() > 0) {
				setTextOnly(false);
				return true;
			}
		}
		return false;
	}

	@Override
	public int getListItemType() {
		if (isAutomatedMessage()) {
			return MESSAGE_AUTO;
		} else if (isOutgoingMessage()) {
			return MESSAGE_OUTGOING;
		} else {
			return MESSAGE_INCOMING;
		}
	}

	private static final String lineEnd = "\r\n";
	private static final String twoHyphens = "--";

	/**
	 * This is a multipart request. To accomplish this, we will create a data blog that is the entire contents
	 * of the request after the request's headers. Each part of the body includes its own headers,
	 * boundary, and data, but that is all rolled into one byte array to be stored pending sending.
	 * This enables the contents to be stores securely via encryption the moment it is created, and
	 * not read again as plain text while it sits on the device.
	 *
	 * @return a Byte array that can be set on the payload request.
	 * TODO: Refactor this API so that the resulting byte array is streamed to a file for later retrieval.
	 */
	@Override
	public @NonNull byte[] renderData() throws Exception {
		boolean shouldEncrypt = isAuthenticated();
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		// First write the message body out as the first "part".
		StringBuilder header = new StringBuilder();
		header.append(twoHyphens).append(boundary).append(lineEnd);

		StringBuilder part = new StringBuilder();
		part
			.append("Content-Disposition: form-data; name=\"message\"").append(lineEnd)
			.append("Content-Type: application/json;charset=UTF-8").append(lineEnd)
			.append(lineEnd)
			.append(marshallForSending().toString()).append(lineEnd);
		byte[] partBytes = part.toString().getBytes();

		final Encryption encryption = getEncryption();
		if (shouldEncrypt) {
			header
				.append("Content-Disposition: form-data; name=\"message\"").append(lineEnd)
				.append("Content-Type: application/octet-stream").append(lineEnd)
				.append(lineEnd);
			data.write(header.toString().getBytes());
			data.write(encryption.encrypt(partBytes));
			data.write("\r\n".getBytes());
		} else {
			data.write(header.toString().getBytes());
			data.write(partBytes);
		}

		// Then append attachments
		if (attachedFiles != null) {
			for (StoredFile storedFile : attachedFiles) {
				ApptentiveLog.v(PAYLOADS, "Starting to write an attachment part.");
				data.write(("--" + boundary + lineEnd).getBytes());
				StringBuilder attachmentEnvelope = new StringBuilder();
				attachmentEnvelope.append(String.format("Content-Disposition: form-data; name=\"file[]\"; filename=\"%s\"", storedFile.getFileName())).append(lineEnd)
					.append("Content-Type: ").append(storedFile.getMimeType()).append(lineEnd)
					.append(lineEnd);
				ByteArrayOutputStream attachmentBytes = new ByteArrayOutputStream();
				FileInputStream fileInputStream = null;
				ApptentiveLog.v(PAYLOADS, "Writing attachment envelope: %s", attachmentEnvelope.toString());
				attachmentBytes.write(attachmentEnvelope.toString().getBytes());

				try {
					if (Util.isMimeTypeImage(storedFile.getMimeType())) {
						ApptentiveLog.v(PAYLOADS, "Appending image attachment.");
						ImageUtil.appendScaledDownImageToStream(storedFile.getSourceUriOrPath(), attachmentBytes);
					} else {
						ApptentiveLog.v(PAYLOADS, "Appending non-image attachment.");
						Util.appendFileToStream(new File(storedFile.getSourceUriOrPath()), attachmentBytes);
					}
				} catch (Exception e) {
					ApptentiveLog.e(PAYLOADS, "Error reading Message Payload attachment: \"%s\".", e, storedFile.getLocalFilePath());
					logException(e);
					continue;
				} finally {
					Util.ensureClosed(fileInputStream);
				}

				if (shouldEncrypt) {
					// If encrypted, each part must be encrypted, and wrapped in a plain text set of headers.
					StringBuilder encryptionEnvelope = new StringBuilder();
					encryptionEnvelope
						.append("Content-Disposition: form-data; name=\"file[]\"").append(lineEnd)
						.append("Content-Type: application/octet-stream").append(lineEnd)
						.append(lineEnd);
					ApptentiveLog.v(PAYLOADS, "Writing encrypted envelope: %s", encryptionEnvelope.toString());
					data.write(encryptionEnvelope.toString().getBytes());
					ApptentiveLog.v(PAYLOADS, "Encrypting attachment bytes: %d", attachmentBytes.size());
					byte[] encryptedAttachment = encryption.encrypt(attachmentBytes.toByteArray());
					ApptentiveLog.v(PAYLOADS, "Writing encrypted attachment bytes: %d", encryptedAttachment.length);
					data.write(encryptedAttachment);
				} else {
					ApptentiveLog.v(PAYLOADS, "Writing attachment bytes: %d", attachmentBytes.size());
					data.write(attachmentBytes.toByteArray());
				}
				data.write("\r\n".getBytes());
			}
		}
		data.write(("--" + boundary + "--").getBytes());

		ApptentiveLog.d(PAYLOADS, "Total payload body bytes: %d", data.size());
		return data.toByteArray();
	}

	private void logException(Exception e) {
		ErrorMetrics.logException(e);
	}
}
