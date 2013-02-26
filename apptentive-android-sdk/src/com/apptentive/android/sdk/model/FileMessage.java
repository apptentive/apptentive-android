/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.storage.FileStore;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author Sky Kelsey
 */
public class FileMessage extends Message {

	private static final String KEY_FILE_NAME = "file_name";
	private static final String KEY_MIME_TYPE = "mime_type";

	public FileMessage() {
		super();
	}

	public FileMessage(String json) throws JSONException {
		super(json);
	}

	@Override
	protected void initType() {
		setType(Type.FileMessage);
	}

	/**
	 * FileMessages are sent using a multipart form encoded request, so they are handled differently here.
	 * @return
	 */
	@Override
	public String marshallForSending() {
		return toString();
	}

	public String getFileName() {
		try {
			return getString(KEY_FILE_NAME);
		}catch (JSONException e) {
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

	public boolean createStoredFile(String uriString) {
		Context appContext = Apptentive.getAppContext();
		Uri uri = Uri.parse(uriString);

		ContentResolver resolver = appContext.getContentResolver();
		String mimeType = resolver.getType(uri);
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String extension = mime.getExtensionFromMimeType(mimeType);
		setFileName(uri.getLastPathSegment() + "." + extension);
		setMimeType(mimeType);

		// Create a file to save locally.
		String localFileName = getStoredFileId();
		File localFile = new File(localFileName);

		// Copy the file contents over.
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = Apptentive.getContentResolver().openInputStream(uri);
			fos = appContext.openFileOutput(localFile.getPath(), Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int read;
			int total = 0;
			while ((read = is.read(buffer, 0, 1024)) > 0) {
				total++;
				fos.write(buffer, 0, read);
			}
			Log.d("Saved file, size = " + total + "k");
		} catch (FileNotFoundException e) {
			Log.e("File not found while storing file.", e);
			return false;
		} catch (Exception e) {
			Log.a("Error storing file.", e);
			return false;
		} finally {
			Util.ensureClosed(is);
			Util.ensureClosed(fos);
		}

		// Create a StoredFile database entry for this locally saved file.
		StoredFile storedFile = new StoredFile();
		storedFile.setId(getStoredFileId());
		storedFile.setOriginalUri(uri.toString());
		storedFile.setLocalFilePath(localFile.getPath());
		storedFile.setMimeType(mimeType);
		FileStore db = Apptentive.getDatabase();
		return db.putStoredFile(storedFile);
	}

	public StoredFile getStoredFile() {
		FileStore fileStore = Apptentive.getDatabase();
		StoredFile storedFile = fileStore.getStoredFile(getStoredFileId());
		return storedFile;
	}
}
