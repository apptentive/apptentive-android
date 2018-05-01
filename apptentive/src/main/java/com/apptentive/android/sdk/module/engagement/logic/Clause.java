/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.util.IndentPrinter;

public interface Clause {
	boolean evaluate(FieldManager fieldManager, IndentPrinter printer);
}
