/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

/**
 * @author Sky Kelsey
 */
public interface KeyValueStore {
	public String getKeyValue(String key);
	public void putKeyValue(String key, String value);
}
