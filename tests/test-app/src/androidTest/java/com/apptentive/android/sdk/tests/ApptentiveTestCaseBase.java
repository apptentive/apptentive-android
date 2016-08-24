/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.tests;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.util.Util;

import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Initializes the Apptentive SDK so tests can work.
 */
public abstract class ApptentiveTestCaseBase {
	protected Context targetContext;
	protected ApptentiveInternal apptentiveInternal;
	protected InteractionManager interactionManager;
	protected CodePointStore codePointStore;

	@Before
	public void initializeApptentiveSdk() {
		targetContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
		apptentiveInternal = ApptentiveInternal.getInstance(targetContext);
		apptentiveInternal.setMinimumLogLevel(ApptentiveLog.Level.VERBOSE);
		interactionManager = apptentiveInternal.getInteractionManager();
		codePointStore = apptentiveInternal.getCodePointStore();
	}

	protected void resetDevice() {
		targetContext.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		apptentiveInternal.getCodePointStore().clear();
		apptentiveInternal.getInteractionManager().clear();

	}

	protected static boolean isRunningOnEmulator() {
		return Build.FINGERPRINT.contains("generic");
	}

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
			ApptentiveLog.e("Error reading from raw resource with ID \"%d\"", e, resourceId);
		} finally {
			Util.ensureClosed(reader);
		}
		return null;
	}

	public static String loadTextAssetAsString(String path) {
		return loadTextAssetAsString(InstrumentationRegistry.getContext(), path);
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
			ApptentiveLog.e("Error reading from file \"%s\"", e, path);
		} finally {
			Util.ensureClosed(reader);
		}
		return null;
	}

	public static BufferedReader openBufferedReaderFromFileAsset(String path) {
		return openBufferedReaderFromFileAsset(InstrumentationRegistry.getContext(), path);
	}

	public static BufferedReader openBufferedReaderFromFileAsset(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(assetManager.open(path)));
		} catch (IOException e) {
			ApptentiveLog.e("Error opening Reader from asset path \"%s\"", path);
		}
		return reader;
	}
}
