/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import com.apptentive.android.sdk.module.messagecenter.view.AutomatedMessageView;
import com.apptentive.android.sdk.module.messagecenter.view.CompoundMessageView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterListItemView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterStatusView;

/**
 * @author Sky Kelsey
 */
public class HolderFactory {

	public static MessageCenterListItemHolder createHolder(MessageCenterListItemView messageCenterListItemView) {
		MessageCenterListItemHolder holder = null;
		if (messageCenterListItemView instanceof CompoundMessageView) {
			CompoundMessageView textMessageView = (CompoundMessageView) messageCenterListItemView;
			holder = (textMessageView.isViewShowingOutgoingMessage())? new OutgoingCompoundMessageHolder(textMessageView) :
			new IncomingCompoundMessageHolder(textMessageView);
		} else if (messageCenterListItemView instanceof AutomatedMessageView) {
			AutomatedMessageView automatedView = (AutomatedMessageView) messageCenterListItemView;
			holder = new AutomatedMessageHolder(automatedView);
		} else if (messageCenterListItemView instanceof MessageCenterStatusView) {
			MessageCenterStatusView messageCenterStatusView = (MessageCenterStatusView) messageCenterListItemView;
			holder = new StatusHolder(messageCenterStatusView);
		}
		return holder;
	}
}
