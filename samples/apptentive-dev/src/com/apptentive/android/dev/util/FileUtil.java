/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev.util;

import android.content.Context;
import android.content.res.AssetManager;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class FileUtil {
	private final static int READ_BUF_LEN = 2048;

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

	public static InputStream openFileAsset(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
		try {
			return new BufferedInputStream(assetManager.open(path));
		} catch (IOException e) {
			Log.e("Error open stream from file \"%s\"", e, path);
		}
		return null;
	}

	public static String createFileAssetUriString(String path) {
		return "file:///android_asset/" + path;
	}

	public static List<String> getFileNamesInAssetsDirectory(Context context, String path) {
		List<String> files = null;
		AssetManager assetManager = context.getResources().getAssets();
		try {
			files = new ArrayList<String>();
			Collections.addAll(files, assetManager.list(path));
		} catch (IOException e) {
			Log.e("Error trying to get list of files in \"assets/%s\"", path);
		}
		return files;
	}
}
