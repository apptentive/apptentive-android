/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.LogicTestCaseBase;
import com.apptentive.android.sdk.TestUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.MockDispatchQueue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveLog.Level.DEBUG;
import static com.apptentive.android.sdk.ApptentiveLog.Level.ERROR;
import static com.apptentive.android.sdk.ApptentiveLog.Level.INFO;
import static junit.framework.Assert.assertEquals;

public class AsyncLogWriterTest extends LogicTestCaseBase {
	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testWritingLogs() {
		File destDir = tempDir.getRoot();

		MockAsyncLogWriter writer;

		// create the first writer and output some Unicode text (Hiragana characters)
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "あ");
		writer.onLogMessage(DEBUG, "い");
		writer.onLogMessage(INFO, "う");

		assertFiles(listLogFiles(destDir), "あ\nい\nう\n");

		// create the second writer and output more text
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "1");
		writer.onLogMessage(DEBUG, "2");
		writer.onLogMessage(INFO, "3");

		assertFiles(listLogFiles(destDir), "あ\nい\nう\n", "1\n2\n3\n");

		// create the third writer and output more text
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "4");
		writer.onLogMessage(DEBUG, "5");
		writer.onLogMessage(INFO, "6");

		assertFiles(listLogFiles(destDir), "あ\nい\nう\n", "1\n2\n3\n", "4\n5\n6\n");

		// create the fourth writer and output more text
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "7");
		writer.onLogMessage(DEBUG, "8");
		writer.onLogMessage(INFO, "9");

		// truncation should appear
		assertFiles(listLogFiles(destDir), "1\n2\n3\n", "4\n5\n6\n", "7\n8\n9\n");

		// create the fifth writer and output more text
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "10");
		writer.onLogMessage(DEBUG, "11");
		writer.onLogMessage(INFO, "12");

		// truncation should appear
		assertFiles(listLogFiles(destDir), "4\n5\n6\n", "7\n8\n9\n", "10\n11\n12\n");

		// create the sixth writer and output more text
		writer = new MockAsyncLogWriter(destDir, 3);
		writer.onLogMessage(ERROR, "13");
		writer.onLogMessage(DEBUG, "14");
		writer.onLogMessage(INFO, "15");

		// truncation should appear
		assertFiles(listLogFiles(destDir), "7\n8\n9\n", "10\n11\n12\n", "13\n14\n15\n");
	}

	private File[] listLogFiles(File destDir) {
		File[] files = destDir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		return files;
	}

	private void assertFiles(File[] files, String... expected) {
		List<String> actual = TestUtils.map(files, new TestUtils.Map<File, String>() {
			@Override
			public String map(File value) {
				return readLogFile(value);
			}
		});

		String message = String.format("Expected: [%s], Actual: [%s]", StringUtils.join(expected).replace("\n", "\\n"), StringUtils.join(actual).replace("\n", "\\n"));
		assertEquals(message, expected.length, files.length);
		for (int i = 0; i < files.length; ++i) {
			assertEquals(message, expected[i], readLogFile(files[i]));
		}
	}

	@NonNull
	private String readLogFile(File value) {
		String text = TestUtils.readText(value);
		// remove log level prefixes since they are not relevant
		text = text.replaceAll("[VDIWEA]: ", "");
		return text;
	}

	static class MockAsyncLogWriter extends AsyncLogWriter {
		private static int nextId;

		MockAsyncLogWriter(File destDir, int logHistorySize) {
			super(destDir, new MockDispatchQueue(true), logHistorySize);
		}

		@NonNull
		@Override
		// if we don't add the prefix - all the files would have the same name since the test completes
		// in less than a second
		protected String createLogFilename() {
			return ++nextId + "-" + super.createLogFilename();
		}
	}
}