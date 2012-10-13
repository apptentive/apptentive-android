/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.offline;

/**
 * @author Sky Kelsey
 */
public interface PayloadStore {

	public void addPayload(Payload payload);

	public void deletePayload(Payload payload);

	public Payload getNextPayload();

}
