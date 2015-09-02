/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
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
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.apptentive.android.sdk.Log;

import java.io.*;
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
	public static void hideSoftKeyboard(Context context, View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}


	public static void showSoftKeyboard(Activity activity, View target) {
		if (activity.getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(target, 0);
		}
	}


	public static String[] getAllUserAccountEmailAddresses(Context context) {
		List<String> emails = new ArrayList<String>();
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE) {
			if (Util.packageHasPermission(context, "android.permission.GET_ACCOUNTS")) {
				accounts = accountManager.getAccountsByType("com.google");
			}
		}
		if (accounts != null) {
			for (Account account : accounts) {
				emails.add(account.name);
			}
		}
		return emails.toArray(new String[emails.size()]);
	}

	private static String getUserEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE) {
			if (Util.packageHasPermission(context, "android.permission.GET_ACCOUNTS")) {
				accounts = accountManager.getAccountsByType("com.google");
			}
		}

		if (accounts != null && accounts.length > 0) {
			// It seems that the first google account added will always be at the end of this list. That SHOULD be the main account.
			Account account = accounts[accounts.length - 1];
			if (account != null) {
				return account.name;
			}
		}
		return null;
	}

	public static boolean isNetworkConnectionPresent(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null;
	}

	public static void ensureClosed(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// Ignore
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

	public static boolean isEmpty(CharSequence charSequence) {
		return charSequence == null || charSequence.length() == 0;
	}

	public static Integer parseCacheControlHeader(String cacheControlHeader) {
		if (cacheControlHeader != null) {
			int indexOfOpenBracket = cacheControlHeader.indexOf("[");
			int indexOfLastBracket = cacheControlHeader.lastIndexOf("]");
			cacheControlHeader = cacheControlHeader.substring(indexOfOpenBracket+1, indexOfLastBracket);
			String[] cacheControlParts = cacheControlHeader.split(",");
			for (String part : cacheControlParts) {
				part = part.trim();
				if (part.startsWith("max-age=")) {
					String[] maxAgeParts = part.split("=");
					if (maxAgeParts.length == 2) {
						String expiration = null;
						try {
							expiration = maxAgeParts[1];
							Integer ret = Integer.parseInt(expiration);
							return ret;
						} catch (NumberFormatException e) {
							Log.e("Error parsing cache expiration as number: %s", e, expiration);
						}
					}
				}
			}
		}
		return null;
	}

	public static boolean isEmailValid(String email) {
		return email.matches("^[^\\s@]+@[^\\s@]+$");
	}

	public static boolean getPackageMetaDataBoolean(Context context, String key) {
		try {
			return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getBoolean(key, false);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static Object getPackageMetaData(Context context, String key) {
		try {
			return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.get(key);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * <p>This method will allow you to pass in literal strings. You must wrap the string in single quotes in order to ensure it is not modified
	 * by Android. Android will try to coerce the string to a float, Integer, etc., if it looks like one.</p>
	 * <p/>
	 * <p>Example: <code>&lt;meta-data android:name="sdk_distribution" android:value="'1.00'"/></code></p>
	 * <p>This will evaluate to a String "1.00". If you leave off the single quotes, this method will just cast to a String, so the result would be a String "1.0".</p>
	 */
	public static String getPackageMetaDataSingleQuotedString(Context context, String key) {
		Object object = getPackageMetaData(context, key);
		if (object == null) {
			return null;
		}
		String ret = object.toString();
		if (ret.endsWith("'")) {
			ret = ret.substring(0, ret.length() - 1);
		}
		if (ret.startsWith("'")) {
			ret = ret.substring(1, ret.length());
		}
		return ret;
	}

	public static String stackTraceAsString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}

	public static String getAppVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Error getting app version name.", e);
		}
		return null;
	}

	public static int getAppVersionCode(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Error getting app version code.", e);
		}
		return -1;
	}

	/**
	 * Converts the current time to a double representing seconds, instead of milliseconds. It will have millisecond
	 * precision as fractional seconds. This is the default time format used throughout the Apptentive SDK.
	 *
	 * @return A double representing the current time in seconds.
	 */
	public static double currentTimeSeconds() {
		long millis = System.currentTimeMillis();
		double point = (double) millis;
		return point / 1000;
	}

	public static int getUtcOffset() {
		TimeZone timezone = TimeZone.getDefault();
		return timezone.getOffset(System.currentTimeMillis()) / 1000;
	}

	public static String getInstallerPackageName(Context context) {
		try {
			return context.getPackageManager().getInstallerPackageName(context.getPackageName());
		} catch (Exception e) {
			// Just return.
		}
		return null;
	}

	public static String readStringFromInputStream(InputStream is, String charEncoding) {
		Reader reader = null;
		StringBuilder out = new StringBuilder();
		final char[] buf = new char[8196];
		try {
			reader = new InputStreamReader(is, charEncoding);
			while (true) {
				int read = reader.read(buf, 0, 8196);
				if (read < 0) {
					break;
				}
				out.append(buf, 0, read);
			}
		} catch (Exception e) {
			//
		} finally {
			Util.ensureClosed(reader);
		}
		return out.toString();
	}

	public static Integer getMajorOsVersion() {
		try {
			String release = Build.VERSION.RELEASE;
			String[] parts = release.split("\\.");
			if (parts != null && parts.length != 0) {
				return Integer.parseInt(parts[0]);
			}
		} catch (Exception e) {
			Log.w("Error getting major OS version", e);
		}
		return -1;
	}

	/**
	 * The web standard for colors is RGBA, but Android uses ARGB. This method provides a way to convert RGBA to ARGB.
	 */
	public static Integer parseWebColorAsAndroidColor(String input) {
		// Swap if input is #RRGGBBAA, but not if it is #RRGGBB
		Boolean swapAlpha = (input.length() == 9);
		try {
			Integer ret = Color.parseColor(input);
			if (swapAlpha) {
				ret = (ret >>> 8) | ((ret & 0x000000FF) << 24);
			}
			return ret;
		} catch (IllegalArgumentException e) {
			//
		}
		return null;
	}

	public static void calculateListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {

			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		int newHeight = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		int HeightDifference = params.height - newHeight;

		//listView.setLayoutParams(params);
	}

	/**
	 * helper method to set the background depending on the android version
	 *
	 * @param v
	 * @param d
	 */
	public static void setBackground(View v, Drawable d) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			v.setBackgroundDrawable(d);
		} else {
			v.setBackground(d);
		}
	}

	/**
	 * helper method to set the background depending on the android version
	 *
	 * @param v
	 * @param drawableRes
	 */
	public static void setBackground(View v, int drawableRes) {
		setBackground(v, getCompatDrawable(v.getContext(), drawableRes));
	}

	/**
	 * helper method to get the drawable by its resource id, specific to the correct android version
	 *
	 * @param c
	 * @param drawableRes
	 * @return
	 */
	public static Drawable getCompatDrawable(Context c, int drawableRes) {
		Drawable d = null;
		try {
			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				d = c.getResources().getDrawable(drawableRes);
			} else {
				d = c.getResources().getDrawable(drawableRes, c.getTheme());
			}
		} catch (Exception ex) {
		}
		return d;
	}

	public static int getThemeColor(Context ctx, int attr) {
		TypedValue tv = new TypedValue();
		if (ctx.getTheme().resolveAttribute(attr, tv, true)) {
			return tv.data;
		}
		return 0;
	}

	/**
	 * helper method to get the color by attr (if defined in the style) or by resource.
	 *
	 * @param ctx
	 * @param attr attribute that defines the color
	 * @param res  color resource id
	 * @return
	 */
	public static int getThemeColorFromAttrOrRes(Context ctx, int attr, int res) {
		int color = getThemeColor(ctx, attr);
		// If this color is not styled, use the default from the resource
		if (color == 0) {
			color = ctx.getResources().getColor(res);
		}
		return color;
	}


	/**
	 * helper method to generate the ImageButton background with specified highlight color.
	 *
	 * @param selected_color the color shown as highlight
	 * @return
	 */
	public static StateListDrawable getSelectableImageButtonBackground(int selected_color) {
		ColorDrawable selectedColor = new ColorDrawable(selected_color);
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[]{android.R.attr.state_pressed}, selectedColor);
		states.addState(new int[]{android.R.attr.state_activated}, selectedColor);
		return states;
	}

	public static int lighter(int color, float factor) {
		int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
		int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
		int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
		return Color.argb(Color.alpha(color), red, green, blue);
	}

}

