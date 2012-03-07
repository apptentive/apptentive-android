/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sky Kelsey
 */
public class Util {
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SZ"); // 2011-01-01 11:59:59-0800
	public static SimpleDateFormat STRINGSAFE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.S"); // 2011-01-01_11-59-59

	public static String dateToString(Date date){
		return dateToString(date, Util.DATE_FORMAT);
	}
	public static Date stringToDate(String date) throws ParseException{
		return stringToDate(date, Util.DATE_FORMAT);
	}

	public static String dateToString(Date date, SimpleDateFormat format){
		return format.format(date);
	}
	public static Date stringToDate(String date, SimpleDateFormat format) throws ParseException{
		return format.parse(date);
	}

	public static int getStatusBarHeight(Window window){
		Rect rectangle = new Rect();
		window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
		return rectangle.top;
	}

	public static Date addDaysToDate(Date start, int days){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(start);
		cal.add(Calendar.DAY_OF_MONTH, days);
		Log.d("Apptentive Utils", cal.toString());
		return cal.getTime();
	}

	public static boolean timeHasElapsed(Date start, int days){
		return new Date().after(addDaysToDate(start, days));
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

}
