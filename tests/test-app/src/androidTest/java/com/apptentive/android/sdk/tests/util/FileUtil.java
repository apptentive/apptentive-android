/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests.util;

import android.content.Context;
import android.content.res.AssetManager;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

import java.io.*;

/**
 * @author Sky Kelsey
 */
public class FileUtil {
	private final static int READ_BUF_LEN = 2048;

	public static String loadRawTextResourceAsString(Context context, int resourceId) {
		BufferedReader reader = null;
		try {
			StringBuilder builder = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)));

			char[] buf = new char[READ_BUF_LEN];
			int count;
			while ((count = reader.read(buf, 0, READ_BUF_LEN)) != -1) {
				builder.append(buf, 0, count);
			}
			return builder.toString();
		} catch (IOException e) {
			Log.e("Error reading from raw resource with ID \"%d\"", e, resourceId);
		} finally {
			Util.ensureClosed(reader);
		}
		return null;
	}

	public static String loadTextAssetAsString(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
		BufferedReader reader = null;
		try {
			StringBuilder builder = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(assetManager.open(path)));

			char[] buf = new char[READ_BUF_LEN];
			int count;
			while ((count = reader.read(buf, 0, READ_BUF_LEN)) != -1) {
				builder.append(buf, 0, count);
			}
			return builder.toString();
		} catch (IOException e) {
			Log.e("Error reading from file \"%s\"", e, path);
		} finally {
			Util.ensureClosed(reader);
		}
		return null;
	}

	public static BufferedReader openBufferedReaderFromFileAsset(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(assetManager.open(path)));
		} catch (IOException e) {
			Log.e("Error opening Reader from asset path \"%s\"", path);
		}
		return reader;
	}
}
