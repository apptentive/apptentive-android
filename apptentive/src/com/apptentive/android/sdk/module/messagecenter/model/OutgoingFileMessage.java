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

/**
 * @author Sky Kelsey
 */
public class OutgoingFileMessage extends ApptentiveMessage implements MessageCenterUtil.CompoundMessageCommonInterface {

	private static final int MAX_STORED_IMAGE_EDGE = 1024;

	private static final String KEY_FILE_NAME = "file_name";
	private static final String KEY_MIME_TYPE = "mime_type";

	private boolean isLast;

	public OutgoingFileMessage() {
		super();
	}

	public OutgoingFileMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.FileMessage);
	}

	/**
	 * FileMessages are sent using a multi-part form encoded request, so they are handled differently here.
	 *
	 * @return A String containing just the meta data about the OutgoingFileMessage.
	 */
	@Override
	public String marshallForSending() {
		return toString();
	}

	public String getFileName() {
		try {
			return getString(KEY_FILE_NAME);
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setFileName(String fileName) {
		try {
			put(KEY_FILE_NAME, fileName);
		} catch (JSONException e) {
			Log.e("Unable to set file name.");
		}
	}

	public String getMimeType() {
		try {
			return getString(KEY_MIME_TYPE);
		} catch (JSONException e) {
			// Ignore
		}
		return null;
	}

	public void setMimeType(String mimeType) {
		try {
			put(KEY_MIME_TYPE, mimeType);
		} catch (JSONException e) {
			Log.e("Unable to set mime type.");
		}
	}

	private String getStoredFileId() {
		return "apptentive-file-" + getNonce();
	}

	public boolean createStoredFile(Context context, String uriString) {
		Uri uri = Uri.parse(uriString);

		ContentResolver resolver = context.getContentResolver();
		String mimeType = resolver.getType(uri);
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String extension = mime.getExtensionFromMimeType(mimeType);

		// If we can't get the mime type from the uri, try getting it from the extension.
		if (extension == null) {
			extension = MimeTypeMap.getFileExtensionFromUrl(uriString);
		}
		if (mimeType == null && extension != null) {
			mimeType = mime.getMimeTypeFromExtension(extension);
		}

		setFileName(uri.getLastPathSegment() + "." + extension);
		setMimeType(mimeType);

		InputStream is = null;
		try {
			is = new BufferedInputStream(context.getContentResolver().openInputStream(uri));
			return createStoredFile(context, is, mimeType);
		} catch (FileNotFoundException e) {
			Log.e("File not found while storing file.", e);
		} catch (IOException e) {
			Log.a("Error storing image.", e);
		} finally {
			Util.ensureClosed(is);
		}
		return false;
	}

	public boolean createStoredFile(Context context, byte[] content, String mimeType) {
		ByteArrayInputStream is = null;
		try {
			is = new ByteArrayInputStream(content);
			return createStoredFile(context, is, mimeType);
		} catch (FileNotFoundException e) {
			Log.e("File not found while storing file.", e);
		} catch (IOException e) {
			Log.a("Error storing file.", e);
		} finally {
			Util.ensureClosed(is);
		}
		return false;
	}

	public boolean createStoredFile(Context context, InputStream is, String mimeType) throws IOException {
		setMimeType(mimeType);

		// Create a file to save locally.
		String localFileName = getStoredFileId();
		File localFile = new File(localFileName);

		// Copy the file contents over.
		CountingOutputStream os = null;
		try {
			os = new CountingOutputStream(new BufferedOutputStream(context.openFileOutput(localFile.getPath(), Context.MODE_PRIVATE)));
			byte[] buf = new byte[2048];
			int count;
			while ((count = is.read(buf, 0, 2048)) != -1) {
				os.write(buf, 0, count);
			}
			Log.d("File saved, size = " + (os.getBytesWritten() / 1024) + "k");
		} finally {
			Util.ensureClosed(os);
		}

		// Create a StoredFile database entry for this locally saved file.
		StoredFile storedFile = new StoredFile();
		storedFile.setId(getStoredFileId());
		storedFile.setLocalFilePath(localFile.getPath());
		storedFile.setMimeType(mimeType);
		FileStore db = ApptentiveDatabase.getInstance(context);
		return db.putStoredFile(storedFile);
	}

	public StoredFile getStoredFile(Context context) {
		FileStore fileStore = ApptentiveDatabase.getInstance(context);
		return fileStore.getStoredFile(getStoredFileId());
	}

	public void deleteStoredFile(Context context) {
		FileStore fileStore = ApptentiveDatabase.getInstance(context);
		fileStore.deleteStoredFile(getStoredFileId());
	}

	@Override
	public boolean isLastSent() {
		return isLast;
	}

	@Override
	public void setLastSent(boolean bVal) {
		isLast = bVal;
	}

	@Override
	public void setBody(String body) {

	}

	@Override
	public String getBody() {
		return null;
	}
}
