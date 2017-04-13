/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.image.ImageItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CompoundMessage extends ApptentiveMessage implements MessageCenterUtil.CompoundMessageCommonInterface {

	private static final String KEY_BODY = "body";

	public static final String KEY_TEXT_ONLY = "text_only";

	private static final String KEY_TITLE = "title";

	private static final String KEY_ATTACHMENTS = "attachments";

	private boolean isLast;

	private boolean hasNoAttachments = true;

	private boolean isOutgoing = true;

	/* For incoming message, this array stores attachment Urls
	 * StoredFile::apptentiveUri is set by the "url" of the remote attachment file
	 * StoredFile:localFilePath is set by the "thumbnail_url" of the remote attachment (maybe empty)
	 */
	private ArrayList<StoredFile> remoteAttachmentStoredFiles;

	// Default constructor will only be called when the message is created from local, a.k.a outgoing
	public CompoundMessage() {
		super();
		isOutgoing = true;
	}

	/* Constructing compound message when JSON is received from incoming, or repopulated from database
	*
	* @param json The JSON string of the message
	* @param bOutgoing true if the message is originated from local
	 */
	public CompoundMessage(String json, boolean bOutgoing) throws JSONException {
		super(json);
		parseAttachmentsArray(json);
		hasNoAttachments = getTextOnly();
		isOutgoing = bOutgoing;
	}

	//region Http-request

	@Override
	public String getHttpEndPoint() {
		return StringUtils.format("/conversations/%s/messages", getConversationId());
	}

	@Override
	public HttpRequestMethod getHttpRequestMethod() {
		return HttpRequestMethod.POST;
	}

	@Override
	public String getHttpRequestContentType() {
		return "multipart/mixed;boundary=xxx";
	}

	//endregion

	@Override
	protected void initType() {
		setType(Type.CompoundMessage);
	}

	@Override
	public String marshallForSending() {
		return toString();
	}

	// Get text message body, maybe empty
	@Override
	public String getBody() {
		try {
			if (!isNull(KEY_BODY)) {
				return getString(KEY_BODY);
			}
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	// Set text message body, maybe empty
	@Override
	public void setBody(String body) {
		try {
			put(KEY_BODY, body);
		} catch (JSONException e) {
			ApptentiveLog.e("Unable to set message body.");
		}
	}

	public String getTitle() {
		try {
			return getString(KEY_TITLE);
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setTitle(String title) {
		try {
			put(KEY_TITLE, title);
		} catch (JSONException e) {
			ApptentiveLog.e("Unable to set title.");
		}
	}

	public boolean getTextOnly() {
		try {
			return getBoolean(KEY_TEXT_ONLY);
		} catch (JSONException e) {
			// Ignore
		}
		return true;
	}

	public void setTextOnly(boolean bVal) {
		try {
			put(KEY_TEXT_ONLY, bVal);
		} catch (JSONException e) {
			ApptentiveLog.e("Unable to set file filePath.");
		}
	}


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
		boolean bRet = false;
		try {
			Future<Boolean> future = ApptentiveInternal.getInstance().getApptentiveTaskManager().addCompoundMessageFiles(attachmentStoredFiles);
			bRet = future.get();
		} catch (Exception e) {
			ApptentiveLog.e("Unable to set associated images in worker thread");
		} finally {
			return bRet;
		}
	}

	public boolean setAssociatedFiles(List<StoredFile> attachedFiles) {

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
			ApptentiveLog.e("Unable to set associated files in worker thread");
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
		} catch (InterruptedException | ExecutionException e) {
			ApptentiveLog.e("Unable to get associated files in worker thread");
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
			ApptentiveLog.e("Unable to delete associated files in worker thread");
		}
	}


	@Override
	public boolean isLastSent() {
		return (isOutgoingMessage()) ? isLast : false;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}

	@Override
	public boolean isOutgoingMessage() {
		return isOutgoing;
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
		} else if (isOutgoing) {
			return MESSAGE_OUTGOING;
		} else {
			return MESSAGE_INCOMING;
		}
	}
}
