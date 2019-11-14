/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchQueueType;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;
import static com.apptentive.android.sdk.util.Constants.LOG_FILE_EXT;
import static com.apptentive.android.sdk.util.Constants.LOG_FILE_PREFIX;

public class AsyncLogWriter implements ApptentiveLog.LogListener {
	/**
	 * Initial buffer size for queued messages
	 */
	private static final int MESSAGE_QUEUE_SIZE = 256;

	/** Mutex object for operation synchronization */
	private final Object mutex = new Object();

	/**
	 * Dest directory for storing log files
	 */
	private final File destDir;

	/**
	 * How many log files should we keep
	 */
	private final int logHistorySize;

	/**
	 * Stores pending log message before they written to a log file
	 * NOTE: this field should only be accessed withing a synchronized context
	 */
	private final List<String> pendingMessages;

	/**
	 * Dispatch queue for writing logs in the background
	 */
	private final DispatchQueue writeQueue;

	/**
	 * Dispatch task for writing messages to a log file
	 */
	private final DispatchTask writeQueueTask;

	public AsyncLogWriter(File destDir, int logHistorySize) {
		this(destDir, DispatchQueue.createBackgroundQueue("Apptentive Log Queue", DispatchQueueType.Serial), logHistorySize);
	}

	AsyncLogWriter(File destDir, DispatchQueue writeQueue, int logHistorySize) {
		if (destDir == null) {
			throw new IllegalArgumentException("Dest dir is null");
		}

		if (writeQueue == null) {
			throw new IllegalArgumentException("Write queue is null");
		}

		if (logHistorySize < 1) {
			throw new IllegalArgumentException("Illegal log history size: " + logHistorySize);
		}

		this.destDir = destDir;
		this.logHistorySize = logHistorySize;
		this.writeQueue = writeQueue;

		pendingMessages = new ArrayList<>(MESSAGE_QUEUE_SIZE);

		File logFile = new File(destDir, createLogFilename());
		ApptentiveLog.v(UTIL, "Log file: " + logFile);
		writeQueueTask = new LogFileWriteTask(logFile);

		// run initialization as the first task on the write queue
		writeQueue.dispatchAsync(createInitializationTask());
	}

	// for unit-testing
	@NonNull
	protected String createLogFilename() {
		return Util.currentDateAsFilename(LOG_FILE_PREFIX, LOG_FILE_EXT);
	}

	private DispatchTask createInitializationTask() {
		return new DispatchTask() {
			@Override
			protected void execute() {
				// list existing log files
				File[] files = destDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(LOG_FILE_EXT);
					}
				});

				// anything to clear?
				if (files == null || files.length == 0) {
					return;
				}

				// sort existing log files by modification date (newest come first)
				Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						// first we try to compare modification dates
						int cmp = (int) (o2.lastModified() - o1.lastModified());
						if (cmp != 0) {
							return cmp;
						}

						// if for any reason they are the same - compare filenames
						return o2.getName().compareTo(o1.getName());
					}
				});

				// delete oldest files if the total count exceed the log history size
				for (int i = logHistorySize - 1; i < files.length; ++i) {
					files[i].delete();
				}
			}
		};
	}

	@Override
	public void onLogMessage(@NonNull ApptentiveLog.Level level, @NonNull String message) {
		synchronized (mutex) {
			pendingMessages.add(level.getShortName() + ": " + message);
			writeQueue.dispatchAsyncOnce(writeQueueTask);
		}
	}

	private class LogFileWriteTask extends DispatchTask {
		private final File file;
		private final List<String> queuedMessagesTemp;

		private LogFileWriteTask(File file) {
			if (file == null) {
				throw new IllegalArgumentException("File is null");
			}
			this.file = file;
			queuedMessagesTemp = new ArrayList<>(MESSAGE_QUEUE_SIZE);
		}

		@Override
		protected void execute() {
			// we don't want to acquire the mutex for too long so just copy pending messages
			// to the temp list which would be used in a blocking IO
			synchronized (mutex) {
				queuedMessagesTemp.addAll(pendingMessages);
				pendingMessages.clear();
			}

			try {
				Util.writeText(file, queuedMessagesTemp, true);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while writing log file: " + destDir);
				ErrorMetrics.logException(e);
			}
			queuedMessagesTemp.clear();
		}
	}
}
