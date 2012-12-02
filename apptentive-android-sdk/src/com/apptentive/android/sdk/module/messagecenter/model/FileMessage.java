/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.model.Message;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class FileMessage extends Message {

	private static final String KEY_FILE_NAME = "file_name";
	private static final String KEY_DATA = "data";
	private static final String KEY_MIME_TYPE = "mime_type";
	private static final String KEY_LOCAL_URI = "local_uri";

	public static final String MIME_TYPE_TEXT = "text/plain";
	public static final String MIME_TYPE_PNG = "image/png";

	public FileMessage() {
		super();
		setType(MessageType.file_message);
	}

	public FileMessage(String json) throws JSONException {
		super(json);
		setType(MessageType.file_message);
	}

	public void setFileName(String fileName) {
		try {
			put(KEY_FILE_NAME, fileName);
		} catch (JSONException e) {
			Log.e("Unable to set file name.");
		}
	}

	public void setData(String data) {
		try {
			put(KEY_DATA, data);
		} catch (JSONException e) {
			Log.e("Unable to set data.");
		}
	}

	public void setLocalUri(String localUri) {
		try {
			put(KEY_LOCAL_URI, localUri);
		}catch (JSONException e) {
			Log.e("Unable to set local Uri.");
		}
	}

	public void setMimeType(String mimeType) {
		try {
			put(KEY_MIME_TYPE, mimeType);
		} catch (JSONException e) {
			Log.e("Unable to set mime type.");
		}
	}

	public static FileMessage createMessage(Context context, Uri uri) {
		FileMessage message = null;
		try {
			ContentResolver resolver = context.getContentResolver();
			String mimeType = resolver.getType(uri);
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String extension = mime.getExtensionFromMimeType(mimeType);

			message = new FileMessage();
			message.setLocalUri(uri.toString());
			message.setFileName(uri.getLastPathSegment() + "." + extension);
			message.setMimeType(mimeType);
			message.setData("DATA GOES HERE"); //TODO
		} catch (Exception e) {
			Log.w("Error creating FileMessage from " + uri.toString());
		}
		return message;
	}
}
