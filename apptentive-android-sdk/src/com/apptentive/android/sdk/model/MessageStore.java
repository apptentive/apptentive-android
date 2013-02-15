/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public interface MessageStore extends RecordStore {

	public String getLastReceivedMessageId();

	public List<Message> getAllMessages();
}
