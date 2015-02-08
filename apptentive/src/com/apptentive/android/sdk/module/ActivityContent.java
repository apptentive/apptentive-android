/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module;

import android.app.Activity;
import android.os.Bundle;
import com.apptentive.android.sdk.Log;

/**
 * @author Sky Kelsey
 */
public abstract class ActivityContent {

	public static final String KEY = "activityContent";

	protected Type type;

	public abstract void onCreate(Activity activity, Bundle onSavedInstanceState);
	public abstract void onSaveInstanceState(Bundle outState);
	public abstract void onRestoreInstanceState(Bundle savedInstanceState);

	/**
	 * Called from the container Activity when the Android back button is pressed. When done processing the back button,
	 * return true if you would like the container Activity to process the back button press as usual. Return false if you
	 * do not. Returning false will result in the back button press being ignored by the container Activity, and the
	 * current view will remain in place.
	 * @return True if this back button press should propagate back to the parent object, else false.
	 */
	public abstract boolean onBackPressed(Activity activity);

	public Type getType() {
		return type;
	}

	public enum Type {
		ABOUT,
		MESSAGE_CENTER,
		INTERACTION,
		unknown;

		public static Type parse(String type) {
			try {
				if (type != null) {
					return Type.valueOf(type);
				}
			} catch (IllegalArgumentException e) {
				Log.v("Error parsing unknown ActivityContent.Type: " + type);
			}
			return unknown;
		}
	}
}
