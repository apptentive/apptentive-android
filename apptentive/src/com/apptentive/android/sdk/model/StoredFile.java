/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.webkit.MimeTypeMap;

/**
 * @author Sky Kelsey
 */
public class StoredFile {
	private String id;
	private String mimeType;
	/* For outgoing attachment, this field is the source image uri or source image full path
	 * if READ_EXTERNAL_STORAGE permission is granted
	 * For incoming attachment, this field is the full path to the on-device cache file where it is downloaded to
	 */
	private String sourceUriOrPath;
	/* For outgoing attachment, this field is the full path to the on-device cache file where the source image is copied to.
	 * For incoming attachment, this field is the full path to the on-device cache file where the thumbnail is downloaded to.
	 */
	private String localFilePath;
	/* For outgoing attachment, this field is empty.
	 * For incoming attachment, this field is the full remote url to the attachment
	 */
	private String apptentiveUri;
	//creation time of original file; set to 0 if failed to retrieve creation time from original uri
	private long creationTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getSourceUriOrPath() {
		return sourceUriOrPath;
	}

	public void setSourceUriOrPath(String sourceUriOrPath) {
		this.sourceUriOrPath = sourceUriOrPath;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public String getApptentiveUri() {
		return apptentiveUri;
	}

	public void setApptentiveUri(String apptentiveUri) {
		this.apptentiveUri = apptentiveUri;
	}

	public String getFileName() {
		return String.format("file.%s", MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType));
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long time) {
		creationTime = time;
	}
}
