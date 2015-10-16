/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.FileStore;
import com.apptentive.android.sdk.util.CountingOutputStream;
import com.apptentive.android.sdk.util.image.ImageUtil;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.io.*;
import java.util.List;

/**
 * @author Barry Li
 */
public class CompoundMessage extends ApptentiveMessage implements MessageCenterUtil.OutgoingItem {

	private static final String KEY_BODY = "body";

	private boolean isLast;

	public CompoundMessage() {
		super();
	}

	public CompoundMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.CompundMessage);
	}

  // Get text message body, maybe empty
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
	public void setBody(String body) {
		try {
			put(KEY_BODY, body);
		} catch (JSONException e) {
			Log.e("Unable to set message body.");
		}
	}


	private String getStoredFileId() {
		return "apptentive-file-" + getNonce();
	}


	public List<StoredFile> getAssociatedFiles(Context context) {
		ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
		return db.getAssociatedFiles(getNonce());
	}

	public void deleteAssociatedFiles(Context context) {
		ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
		db.deleteAssociatedFiles(getNonce());
	}

	@Override
	public boolean isLastSent() {
		return isLast;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}
}
