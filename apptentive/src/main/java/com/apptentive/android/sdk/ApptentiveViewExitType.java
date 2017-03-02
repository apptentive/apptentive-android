/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

/**
 * Enum class describing the action which cased the activity to finish.
 */
public enum ApptentiveViewExitType {
	MENU_ITEM("menu_item", false), // user selected options menu item
	BACK_BUTTON("back_button", false), // user pressed back button
	NOTIFICATION("notification", true); // dismiss-ui notification was received

	/**
	 * Exit mode name as sent with 'exit' event
	 */
	private final String name;

	/**
	 * If <code>true</code> the exit mode will be sent with engage event.
	 */
	private final boolean shouldAddToEngage;

	ApptentiveViewExitType(String name, boolean shouldAddToEngage) {
		this.name = name;
		this.shouldAddToEngage = shouldAddToEngage;
	}

	public String getName() {
		return name;
	}

	public boolean isShouldAddToEngage() {
		return shouldAddToEngage;
	}
}
