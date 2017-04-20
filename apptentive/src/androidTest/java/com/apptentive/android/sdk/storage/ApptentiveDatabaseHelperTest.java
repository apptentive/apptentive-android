/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(AndroidJUnit4.class)
public class ApptentiveDatabaseHelperTest {

	@After
	public void tearDown() throws Exception {
		deleteDbFile(InstrumentationRegistry.getContext());
	}

	@Test
	public void testFoo() throws Exception {
		final Context context = InstrumentationRegistry.getContext();
		replaceDbFile(context, "apptentive-v2");

		ApptentiveDatabaseHelper db = new ApptentiveDatabaseHelper(context);
		db.getWritableDatabase();
		Thread.sleep(10000);

	}

	private static void replaceDbFile(Context context, String filename) throws IOException {
		InputStream input = context.getAssets().open(filename);
		try {
			OutputStream output = new FileOutputStream(getDatabaseFile(context));
			try {
				byte[] buffer = new byte[1024];
				int read;
				while ((read = input.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
			} finally {
				output.close();
			}
		} finally {
			input.close();
		}
	}

	private static void deleteDbFile(Context context) throws IOException {
		getDatabaseFile(context).delete();
	}

	private static File getDatabaseFile(Context context) {
		return context.getDatabasePath("apptentive");
	}
}