/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

// TODO: remove this class
public class ApptentiveClient {

	private static final String USER_AGENT_STRING = "Apptentive/%s (Android)"; // Format with SDK version string.

	public static String getUserAgentString() {
		return String.format(USER_AGENT_STRING, Constants.getApptentiveSdkVersion());
	}

	/**
	 * Reads error response and returns it as a string. Handles gzipped streams.
	 *
	 * @param connection Current connection
	 * @return Error response as String
	 * @throws IOException
	 */
	public static String getErrorResponse(HttpURLConnection connection, boolean isZipped) throws IOException {
		if (connection != null) {
			InputStream is = null;
			try {
				is = connection.getErrorStream();
				if (is != null) {
					if (isZipped) {
						is = new GZIPInputStream(is);
					}
				}
				return Util.readStringFromInputStream(is, "UTF-8");
			} finally {
				Util.ensureClosed(is);
			}
		}
		return null;
	}
}
