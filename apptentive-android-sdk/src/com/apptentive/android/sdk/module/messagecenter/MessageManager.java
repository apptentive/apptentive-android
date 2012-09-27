/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.messagecenter.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageManager {

	public static void updateMessages() {
		List<Message> message = fetchMessages(getLastMessageGuid());

	}

	private static List<Message> fetchMessages(String lastMessageGuid) {
		ApptentiveHttpResponse response = ApptentiveClient.getMessages(lastMessageGuid);

		return new ArrayList<Message>();
	}

	private static String getLastMessageGuid() {
		// Do this.
		return null;
	}
}
