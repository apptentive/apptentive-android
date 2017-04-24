/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Class representing HTTP multipart request with Json body
 */
public class HttpJsonMultipartRequest extends HttpRequest {
	private static final String lineEnd = "\r\n";
	private static final String twoHyphens = "--";

	private final byte[] requestData;
	private final List<StoredFile> files;
	private final String boundary;

	public HttpJsonMultipartRequest(String urlString, byte[] requestData, List<StoredFile> files) {
		super(urlString);

		if (files == null) {
			throw new IllegalArgumentException("Files reference is null");
		}
		this.files = files;
		this.requestData = requestData;

		boundary = UUID.randomUUID().toString();
		setRequestProperty("Content-Type", "multipart/mixed;boundary=" + boundary);
	}

	@Override
	protected byte[] createRequestData() throws IOException {
		ByteArrayOutputStream stream = null;
		try {
			// get payload bytes
			final byte[] jsonData = super.createRequestData();

			// write data
			stream = new ByteArrayOutputStream();
			writeRequestData(new DataOutputStream(stream), jsonData);
			return stream.toByteArray();
		} finally {
			Util.ensureClosed(stream);
		}
	}

	private void writeRequestData(DataOutputStream out, byte[] jsonData) throws IOException {
		out.writeBytes(twoHyphens + boundary + lineEnd);

		// Write text message
		out.writeBytes("Content-Disposition: form-data; name=\"message\"" + lineEnd);
		// Indicate the character encoding is UTF-8
		out.writeBytes("Content-Type: text/plain;charset=UTF-8" + lineEnd);

		out.writeBytes(lineEnd);

		// Explicitly encode message json
		out.write(jsonData);
		out.writeBytes(lineEnd);

		// Send associated files
		if (files != null) {
			writeFiles(out, files);
		}
		out.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	}

	private void writeFiles(DataOutputStream out, List<StoredFile> files) throws IOException {
		for (StoredFile file : files) {
			writeFile(out, file);
		}
	}

	private void writeFile(DataOutputStream out, StoredFile file) throws IOException {
		String cachedImagePathString = file.getLocalFilePath();
		String originalFilePath = file.getSourceUriOrPath();
		File cachedImageFile = new File(cachedImagePathString);

		// No local cache found
		if (!cachedImageFile.exists()) {
			boolean cachedCreated = false;
			if (Util.isMimeTypeImage(file.getMimeType())) {
				// Create a scaled down version of original image
				cachedCreated = ImageUtil.createScaledDownImageCacheFile(originalFilePath, cachedImagePathString);
			} else {
				// For non-image file, just copy to a cache file
				if (Util.createLocalStoredFile(originalFilePath, cachedImagePathString, null) != null) {
					cachedCreated = true;
				}
			}

			if (!cachedCreated) {
				return;
			}
		}
		out.writeBytes(twoHyphens + boundary + lineEnd);
		StringBuilder requestText = new StringBuilder();
		String fileFullPathName = originalFilePath;
		if (StringUtils.isNullOrEmpty(fileFullPathName)) {
			fileFullPathName = cachedImagePathString;
		}
		requestText.append(String.format("Content-Disposition: form-data; name=\"file[]\"; filename=\"%s\"", fileFullPathName)).append(lineEnd);
		requestText.append("Content-Type: ").append(file.getMimeType()).append(lineEnd);

		// Write file attributes
		out.writeBytes(requestText.toString());
		out.writeBytes(lineEnd);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cachedImageFile);

			int bytesAvailable = fis.available();
			int maxBufferSize = 512 * 512;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			// read image data 0.5MB at a time and write it into buffer
			int bytesRead = fis.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				out.write(buffer, 0, bufferSize);
				bytesAvailable = fis.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fis.read(buffer, 0, bufferSize);
			}
		} finally {
			Util.ensureClosed(fis);
		}
		out.writeBytes(lineEnd);
	}
}
