/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
	public interface Map<In, Out> {
		Out map(In value);
	}

	public static <In, Out> List<Out> map(In[] input, Map<In, Out> map) {
		List<Out> output = new ArrayList<>();
		for (In value : input) {
			output.add(map.map(value));
		}
		return output;
	}

	public static <In, Out> List<Out> map(List<In> input, Map<In, Out> map) {
		List<Out> output = new ArrayList<>();
		for (In value : input) {
			output.add(map.map(value));
		}
		return output;
	}

	public static void delete(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					delete(f);
				}
			}
		}
		file.delete();
	}

	public static @Nullable String readText(File file) {
		try {
			FileInputStream stream = new FileInputStream(file);
			return Util.readStringFromInputStream(stream, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}
}
