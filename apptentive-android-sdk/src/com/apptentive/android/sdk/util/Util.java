/*
 * Util.java
 *
 * Created by Sky Kelsey on 2011-09-16.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.view.Window;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mmZ"); // 2011-01-01 11:59-0700
	public static SimpleDateFormat STRINGSAFE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 2011-01-01_11-59-59

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
		return cal.getTime();
	}

	public static boolean timeHasElapsed(Date start, int days){
		return new Date().after(addDaysToDate(start, days));
	}

	private static List<PackageInfo> getPermissions(Activity activity){
		return activity.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
	}

	public static boolean packageHasPermission(Activity activity, String permission){
		String packageName = activity.getApplicationContext().getPackageName();
		return packageHasPermission(activity, packageName, permission);
	}

	public static boolean packageHasPermission(Activity activity, String packageName, String permission){
		List<PackageInfo> packageInfos = getPermissions(activity);
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

}
