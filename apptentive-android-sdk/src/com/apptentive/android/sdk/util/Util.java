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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sky Kelsey
 */
public class Util {
	public static SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ"); // 2011-01-01 11:59:59-0800
	public static SimpleDateFormat STRINGSAFE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS"); // 2011-01-01_11-59-59

	public static String dateToIso8601String(long when) {
		return dateToString(ISO8601_DATE_FORMAT, when);
	}

	public static Date iso8601StringToDate(String date) throws ParseException {
		return stringToDate(date, Util.ISO8601_DATE_FORMAT);
	}

	public static String dateToString(DateFormat format, long when){
		return format.format(new Date(when));
	}

	public static Date stringToDate(String date, SimpleDateFormat format) throws ParseException{
		return format.parse(date);
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
			if(packageInfo.packageName.equals(packageName)){
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
