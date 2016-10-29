/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

public class Composer implements MessageCenterUtil.MessageCenterListItem {

	public String title;
	public String closeBody;
	public String closeDiscard;
	public String closeCancel;
	public String sendButton;
	public String messageHint;

	public Composer(String title, String closeBody, String closeDiscard, String closeCancel, String sendButton, String messageHint) {
		this.title = title;
		this.closeDiscard = closeDiscard;
		this.closeBody = closeBody;
		this.closeCancel = closeCancel;
		this.sendButton = sendButton;
		this.messageHint = messageHint;
	}

	@Override
	public int getListItemType() {
		return MESSAGE_COMPOSER;
	}
}
