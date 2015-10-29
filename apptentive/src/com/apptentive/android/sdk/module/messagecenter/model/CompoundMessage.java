/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.content.Context;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.util.image.ImageItem;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Barry Li
 */
public class CompoundMessage extends ApptentiveMessage implements MessageCenterUtil.CompoundMessageCommonInterface {

	private static final String KEY_BODY = "body";

	public static final String KEY_TEXT_ONLY = "text_only";

	private boolean isLast;

	private boolean hasNoAttachments = true;

	public CompoundMessage() {
		super();
	}

	public CompoundMessage(String json) throws JSONException {
		super(json);
		hasNoAttachments = getTextOnly();
	}

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
			Log.e("Unable to set message body.");
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
			Log.e("Unable to set file filePath.");
		}
	}

	/**
	 * This method stores an image, and compresses it in the process so it doesn't fill up the disk. Therefore, do not use
	 * it to store an exact copy of the file in question.
	 */
	public boolean setAssociatedFiles(Context context, List<ImageItem> attachedImages) {

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
			storedFile.setOriginalUriOrPath(image.originalPath);
			// ToDo: look for local cache
			storedFile.setLocalFilePath(image.localCachePath);
			storedFile.setMimeType("image/jpeg");
			storedFile.setCreationTime(image.time);
			attachmentStoredFiles.add(storedFile);
		}
		ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
		return db.addCompoundMessageFiles(attachmentStoredFiles);
	}



	public List<StoredFile> getAssociatedFiles(Context context) {
		if (hasNoAttachments) {
			return null;
		}
		ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
		return db.getAssociatedFiles(getNonce());
	}

	public void deleteAssociatedFiles(Context context) {
		ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
		db.deleteAssociatedFiles(getNonce());
	}


	@Override
	public boolean isLastSent() {
		return (isOutgoingMessage()) ? isLast : false;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}
}
