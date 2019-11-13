package com.apptentive.android.sdk;

import androidx.appcompat.app.AppCompatActivity;

public class Availability {
	/**
	 * Checks if the app is built with AndroidX dependencies
	 */
	public static boolean isAndroidX() {
		try {
			// we check if a class from androidx.appcompat:appcompat library is available
			AppCompatActivity.class.getName();
			return true;
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}
}
