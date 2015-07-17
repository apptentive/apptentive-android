/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

/**
 * @author Sky Kelsey
 */
public class MessageCenterComposingItem implements MessageCenterListItem {
	public static int COMPOSING_ITEM_AREA = 0;
	public static int COMPOSING_ITEM_ACTIONBAR = 1;

	private int type;

	public MessageCenterComposingItem(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}