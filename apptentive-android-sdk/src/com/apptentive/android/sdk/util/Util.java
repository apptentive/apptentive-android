/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
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
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import com.apptentive.android.sdk.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sky Kelsey
 */
public class Util {

	// These date formats are as close as Java can get to ISO 8601 without royally screwing up.
	public static String PSEUDO_ISO8601_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ"; // 2011-01-01 11:59:59-0800
	public static String PSEUDO_ISO8601_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSSZ"; // 2011-01-01 11:59:59.123-0800 or 2011-01-01 11:59:59.23-0800

	public static String dateToIso8601String(Date date) {
		return dateToString(new SimpleDateFormat(PSEUDO_ISO8601_DATE_FORMAT_MILLIS), date.getTime());
	}

	public static String dateToIso8601String(long when) {
		return dateToString(new SimpleDateFormat(PSEUDO_ISO8601_DATE_FORMAT_MILLIS), when);
	}

	public static String dateToString(DateFormat format, long when){
		return format.format(new Date(when));
	}

	public static Date parseIso8601Date(final String iso8601DateString) {
		// Normalize timezone.
		String s = iso8601DateString.trim().replace("Z", "+00:00").replace("T", " ");
		try{
			// Remove colon in timezone.
			int lastColonIndex = s.lastIndexOf(":");
			s = s.substring(0, lastColonIndex) + s.substring(lastColonIndex+1);

			// Right pad millis to 3 places. ISO 8601 supplies fractions of seconds, but Java interprets them as millis.
			int milliStart = s.lastIndexOf('.');
			int milliEnd = (s.lastIndexOf('+') != -1) ? s.lastIndexOf('+') : s.lastIndexOf('-');
			if(milliStart != -1) {
				String start = s.substring(0, milliStart+1);
				String millis = s.substring(milliStart+1, milliEnd);
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
			if(s.contains(".")) {
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

	public static int getStatusBarHeight(Window window){
		Rect rectangle = new Rect();
		window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
		return rectangle.top;
	}


	private static List<PackageInfo> getPermissions(Context context){
		return context.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
	}

	public static boolean packageHasPermission(Context context, String permission){
		String packageName = context.getApplicationContext().getPackageName();
		return packageHasPermission(context, packageName, permission);
	}

	public static boolean packageHasPermission(Context context, String packageName, String permission){
		List<PackageInfo> packageInfos = getPermissions(context);
		for(PackageInfo packageInfo : packageInfos){
			if(packageInfo.packageName.equals(packageName) && packageInfo.requestedPermissions != null){
				for(String permissionName : packageInfo.requestedPermissions){
					if(permissionName.equals(permission)){
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int dipsToPixels(Context context, int dp){
		final float scale = context.getResources().getDisplayMetrics().density;
		return ((int) (dp * scale + 0.5f));
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
		return "";
	}

	public static String getEmail(Context context){
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);
		if(account == null){
			return null;
		}else{
			return account.name;
		}
	}

	// TODO: Use reflection to load this so we can drop 2.1 API requirement.
	private static Account getAccount(AccountManager accountManager){
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0){
			account = accounts[0];
		}else{
			account = null;
		}
		return account;
	}

	public static boolean isNetworkConnectionPresent(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null;
	}

}
