/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

/**
 * @author Sky Kelsey
 */
public class MessageCenterComposingItem implements MessageCenterUtil.MessageCenterListItem {
	public static int COMPOSING_ITEM_AREA = 0;
	public static int COMPOSING_ITEM_ACTIONBAR = 1;
	// Scenarios of presenting Who Card
	public static int COMPOSING_ITEM_WHOCARD_REQUIRED_INIT = 2;
	public static int COMPOSING_ITEM_WHOCARD_REQUIRED_EDIT = 3;
	public static int COMPOSING_ITEM_WHOCARD_REQUESTED_INIT = 4;
	public static int COMPOSING_ITEM_WHOCARD_REQUESTED_EDIT = 5;

	public final int type;
	/*
	* Refer to https://apptentive.atlassian.net/wiki/display/APPTENTIVE/Message+Center+Interaction
	* for how following strings are mapped to different composing view strings
	 */
	public final String str_1;
	public final String str_2;
	public final String str_3;
	public final String str_4;
	public final String button_1;
	public final String button_2;



	public MessageCenterComposingItem(int type, String str_1, String str_2,
																		String str_3, String str_4,
																		String button_1, String button_2) {
		this.type = type;
		this.str_1 = str_1;
		this.str_2 = str_2;
		this.str_3 = str_3;
		this.str_4 = str_4;
		this.button_1 = button_1;
		this.button_2 = button_2;
	}

	public int getType() {
		return type;
	}

}