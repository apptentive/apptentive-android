/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

public class LogWriter {
	private static final String TAG = LogWriter.class.getSimpleName();
	private final File file;
	private final boolean append;
	private final boolean filterByPID;

	private Thread thread;
	private Process process;

	public LogWriter(File file, boolean append, boolean filterByPID) {
		if (file == null) {
			throw new IllegalArgumentException("File is null");
		}
		this.file = file;
		this.append = append;
		this.filterByPID = filterByPID;
	}

	//region Lifecycle

	public void start() {
		if (thread != null) {
			throw new IllegalStateException("Already started");
		}

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				writeLogs();
			}
		}, "Apptentive Logcat Writer");
		thread.start();
	}

	public void stopAndWait() throws InterruptedException {
		if (thread != null) {
			process.destroy();
			thread.interrupt();
			thread.join();
			thread = null;
		}
	}

	//endregion

	//region Logs

	private void writeLogs() {
		try {
			writeLogsGuarded();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeLogsGuarded() throws IOException {
		BufferedReader reader = null;
		FileWriter writer = null;

		try {
			List<String> cmd = new ArrayList<>();
			cmd.add("logcat");
			cmd.add("-v");
			cmd.add("tag");
			if (filterByPID && logcatCanFilterByProcess()) {
				cmd.add("--pid=" + android.os.Process.myPid());
			}

			process = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 8192);
			writer = new FileWriter(file, append);

			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write('\n');
			}
		} catch (InterruptedIOException e) {
			Log.i(TAG, "Apptentive log writing interrupted");
		} finally {
			if (reader != null) {
				reader.close();
			}

			if (writer != null) {
				writer.close();
			}

			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Returns true if <code>--pid=<pid></code> option is supported
	 */
	private boolean logcatCanFilterByProcess() {
		// This is a bit hacky but we run `logcat --help` and analyze the output to see if
		// --pid=<pid> option is there

		try {
			Process process = Runtime.getRuntime().exec(new String[]{"logcat", "--help"});
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()), 1024);

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("--pid=<pid>")) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to figure out if logcat can filter by process id");
		}
		return false;
	}

	//endregion
}
