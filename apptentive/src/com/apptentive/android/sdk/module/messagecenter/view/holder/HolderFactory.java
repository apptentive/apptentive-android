/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import com.apptentive.android.sdk.module.messagecenter.view.FileMessageView;
import com.apptentive.android.sdk.module.messagecenter.view.IncomingTextMessageView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterGreetingView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterListItemView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterStatusView;
import com.apptentive.android.sdk.module.messagecenter.view.OutgoingTextMessageView;

/**
 * @author Sky Kelsey
 */
public class HolderFactory {

	public static MessageCenterListItemHolder createHolder(MessageCenterListItemView messageCenterListItemView) {
		MessageCenterListItemHolder holder = null;
		if (messageCenterListItemView instanceof OutgoingTextMessageView) {
			OutgoingTextMessageView textMessageView = (OutgoingTextMessageView) messageCenterListItemView;
			holder = new OutgoingTextMessageHolder(textMessageView);
		} else if (messageCenterListItemView instanceof IncomingTextMessageView) {
			IncomingTextMessageView textMessageView = (IncomingTextMessageView) messageCenterListItemView;
			holder = new IncomingTextMessageHolder(textMessageView);
		} else if (messageCenterListItemView instanceof FileMessageView) {
			FileMessageView fileMessageView = (FileMessageView) messageCenterListItemView;
			holder = new OutgoingFileMessageHolder(fileMessageView);
		} else if (messageCenterListItemView instanceof MessageCenterGreetingView) {
			MessageCenterGreetingView messageCenterGreetingView = (MessageCenterGreetingView) messageCenterListItemView;
			holder = new GreetingHolder(messageCenterGreetingView);
		} else if (messageCenterListItemView instanceof MessageCenterStatusView) {
			MessageCenterStatusView messageCenterGreetingView = (MessageCenterStatusView) messageCenterListItemView;
			holder = new StatusHolder(messageCenterGreetingView);
		}
		return holder;
	}
}
