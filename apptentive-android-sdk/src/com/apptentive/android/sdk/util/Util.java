/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import com.apptentive.android.sdk.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sky Kelsey
 */
public class Util {

	// These date formats are as close as Java can get to ISO 8601 without royally screwing up.
	public static final String PSEUDO_ISO8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ"; // 2011-01-01 11:59:59-0800
	public static final String PSEUDO_ISO8601_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSSZ"; // 2011-01-01 11:59:59.123-0800 or 2011-01-01 11:59:59.23-0800

	public static String dateToIso8601String(long millis) {
		return dateToString(new SimpleDateFormat(PSEUDO_ISO8601_DATE_FORMAT_MILLIS), new Date(millis));
	}

	public static String secondsToDisplayString(String format, Double seconds) {
		String dateString = dateToString(new SimpleDateFormat(format), new Date(Math.round(seconds * 1000)));
		return dateString.replace("PM", "pm").replace("AM", "am");
	}

	public static String dateToString(DateFormat format, Date date) {
		return format.format(date);
	}

	public static Date parseIso8601Date(final String iso8601DateString) {
		// Normalize timezone.
		String s = iso8601DateString.trim().replace("Z", "+00:00").replace("T", " ");
		try {
			// Remove colon in timezone.
			if (s.charAt(s.length() - 3) == ':') {
				int lastColonIndex = s.lastIndexOf(":");
				s = s.substring(0, lastColonIndex) + s.substring(lastColonIndex + 1);
			}
			// Right pad millis to 3 places. ISO 8601 supplies fractions of seconds, but Java interprets them as millis.
			int milliStart = s.lastIndexOf('.');
			int milliEnd = (s.lastIndexOf('+') != -1) ? s.lastIndexOf('+') : s.lastIndexOf('-');
			if (milliStart != -1) {
				String start = s.substring(0, milliStart + 1);
				String millis = s.substring(milliStart + 1, milliEnd);
				String end = s.substring(milliEnd);
				millis = String.format("%-3s", millis).replace(" ", "0");
				s = start + millis + end;
			}
		} catch (Exception e) {
			Log.e("Error parsing date: " + iso8601DateString, e);
			return new Date();
		}
		// Parse, accounting for millis, if provided.
		try {
			if (s.contains(".")) {
				return new SimpleDateFormat(PSEUDO_ISO8601_DATE_FORMAT_MILLIS).parse(s);
			} else {
				return new SimpleDateFormat(PSEUDO_ISO8601_DATE_FORMAT).parse(s);
			}
		} catch (ParseException e) {
			Log.e("Exception parsing date: " + s, e);
		}

		// Return null as default. Nothing we can do but log it.
		return null;
	}

	public static int getStatusBarHeight(Window window) {
		Rect rectangle = new Rect();
		window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
		return rectangle.top;
	}


	private static List<PackageInfo> getPermissions(Context context) {
		return context.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
	}

	public static boolean packageHasPermission(Context context, String permission) {
		String packageName = context.getApplicationContext().getPackageName();
		return packageHasPermission(context, packageName, permission);
	}

	public static boolean packageHasPermission(Context context, String packageName, String permission) {
		List<PackageInfo> packageInfos = getPermissions(context);
		for (PackageInfo packageInfo : packageInfos) {
			if (packageInfo.packageName.equals(packageName) && packageInfo.requestedPermissions != null) {
				for (String permissionName : packageInfo.requestedPermissions) {
					if (permissionName.equals(permission)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int pixelsToDips(Context context, int px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(px / scale);
	}

	public static int dipsToPixels(Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
	}

	public static float dipsToPixelsFloat(Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return dp * scale;
	}

	/**
	 * Internal use only.
	 */
	public static void hideSoftKeyboard(Activity activity, View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

/*
	public void showSoftKeyboard(Activity activity, View target) {
		if (activity.getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(target, 0);
		}
	}
*/


	public static String getUserEmail(Context context) {
		if (Util.packageHasPermission(context, "android.permission.GET_ACCOUNTS")) {
			String email = getEmail(context);
			if (email != null) {
				return email;
			}
		}
		return null;
	}

	private static String getEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);
		if (account == null) {
			return null;
		} else {
			return account.name;
		}
	}

	// TODO: Use reflection to load this so we can drop 2.1 API requirement.
	private static Account getAccount(AccountManager accountManager) {
		Account account = null;
		try {
			Account[] accounts = accountManager.getAccountsByType("com.google");
			if (accounts.length > 0) {
				// It seems that the first google account added will always be at the end of this list. That SHOULD be the main account.
				account = accounts[accounts.length - 1];
			}
		} catch (VerifyError e) {
			// Ignore here because the phone is on a pre API Level 5 SDK.
		}
		return account;
	}

	public static boolean isNetworkConnectionPresent(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null;
	}

	public static void ensureClosed(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static void ensureClosed(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static Point getScreenSize(Context context) {
		Point ret = new Point();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		// TODO: getWidth(), getHeight(), and getOrientation() are deprecated in API 13 in favor of getSize() and getRotation().
		ret.set(display.getWidth(), display.getHeight());
		return ret;
	}

	public static void printDebugInfo(Context context) {
		// Print screen dimensions.
		// Huawei Comet: Port: PX=240x320  DP=320x427, Land: PX=320x240 DP=427x320
		// Galaxy Nexus: Port: PX=720x1184 DP=360x592, Land: PX=1196x720 DP=598x360
		// Nexus 7:      Port: PX=800x1205 DP=601x905, Land: PX=1280x736 DP=962x553
		Point point = Util.getScreenSize(context);
		Log.e("Screen size: PX=%dx%d DP=%dx%d", point.x, point.y, Util.pixelsToDips(context, point.x), Util.pixelsToDips(context, point.y));
	}

	public static boolean isEmpty(String theString) {
		return theString == null || theString.length() == 0;
	}

	public static boolean isEmailValid(String email) {
		return email.matches("^[^\\s@]+@[^\\s@]+$");
	}
}
