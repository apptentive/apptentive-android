/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.StoredFile;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

	public static boolean isNetworkConnectionPresent(Context appContext) {
		ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
			cacheControlHeader = cacheControlHeader.substring(indexOfOpenBracket + 1, indexOfLastBracket);
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

	public static boolean canLaunchIntent(Context context, Intent intent) {
		PackageManager pm = context.getPackageManager();
		ComponentName cn = intent.resolveActivity(pm);
		if (cn != null) {
			return true;
		}
		return false;
	}

	public static String classToString(Object object) {
		if (object == null) {
			return "null";
		} else {
			return String.format("%s(%s)", object.getClass().getSimpleName(), object);
		}
	}

	public static String getMimeTypeFromUri(Context context, Uri contentUri) {
		return context.getContentResolver().getType(contentUri);
	}

	public static String getRealFilePathFromUri(Context context, Uri contentUri) {
		if (!hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			return null;
		}
		Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String document_id = cursor.getString(0);
			document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();

			cursor = context.getContentResolver().query(
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
			if (cursor != null && cursor.moveToFirst()) {
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();
				return path;
			}
		}
		return null;
	}

	public static long getContentCreationTime(Context context, Uri contentUri) {
		if (!hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			return 0;
		}
		Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String document_id = cursor.getString(0);
			document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();

			cursor = context.getContentResolver().query(
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
			if (cursor != null && cursor.moveToFirst()) {
				long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
				cursor.close();
				return time;
			}
		}

		return 0;
	}

	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				hexString.append(Integer.toHexString(0xFF & aMessageDigest));
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Generate cached file name use md5 from image originalPath and image created time
	 */
	public static String generateCacheFileFullPath(String url, File cacheDir) {
		String fileName = md5(url);
		File cacheFile = new File(cacheDir, fileName);
		return cacheFile.getPath();
	}

	/*
	 * Generate cached file name use md5 from file originalPath and created time
	 */
	public static String generateCacheFileFullPath(Uri fileOriginalUri, File cacheDir, long createdTime) {
		String source = fileOriginalUri.toString() + Long.toString(createdTime);
		String fileName = md5(source);
		File cacheFile = new File(cacheDir, fileName);
		return cacheFile.getPath();
	}


	public static File getDiskCacheDir(Context context) {
		File appCacheDir = null;
		if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable())
				&& hasPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE")) {
			appCacheDir = context.getExternalCacheDir();
		}

		if (appCacheDir == null) {
			appCacheDir = context.getCacheDir();
		}
		return appCacheDir;
	}

	public static String generateCacheFilePathFromNonceOrPrefix(Context context, String nonce, String prefix) {
		String fileName = (prefix == null) ? "apptentive-api-file-" + nonce : prefix;
		File cacheDir = getDiskCacheDir(context);
		File cacheFile = new File(cacheDir, fileName);
		return cacheFile.getPath();
	}

	public static boolean hasPermission(Context context, final String permission) {
		int perm = context.checkCallingOrSelfPermission(permission);
		return perm == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * This function launchs the default app to view the selected file, based on mime type
	 *
	 * @param sourcePath
	 * @param selectedFilePath the full path to the local storage
	 * @param mimeTypeString   the mime type of the file to be opened
	 * @return true if file can be viewed
	 */
	public static boolean openFileAttachment(final Context context, final String sourcePath, final String selectedFilePath, final String mimeTypeString) {
		if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable())
				&& hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

			File selectedFile = new File(selectedFilePath);
			String selectedFileName = null;
			if (selectedFile.exists()) {
				selectedFileName = selectedFile.getName();
				final Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				/* Attachments were downloaded into app private data dir. In order for external app to open
				 * the attachments, the file need to be copied to a download folder that is accessible to public
			   * The folder will be sdcard/Downloads/apptentive-received/<file name>
         */
				File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File apptentiveSubFolder = new File(downloadFolder, "apptentive-received");
				if (!apptentiveSubFolder.exists()) {
					apptentiveSubFolder.mkdir();
				}

				File tmpfile = new File(apptentiveSubFolder, selectedFileName);
				String tmpFilePath = tmpfile.getPath();
				// If destination file already exists, overwrite it; otherwise, delete all existing files in the same folder first.
				if (!tmpfile.exists()) {
					String[] children = apptentiveSubFolder.list();
					if (children != null) {
						for (int i = 0; i < children.length; i++) {
							new File(apptentiveSubFolder, children[i]).delete();
						}
					}
				}
				if (copyFile(selectedFilePath, tmpFilePath) == 0) {
					return false;
				}

				intent.setDataAndType(Uri.fromFile(tmpfile), mimeTypeString);
				try {
					context.startActivity(intent);
					return true;
				} catch (ActivityNotFoundException e) {
					Log.e("Activity not found to open attachment: ", e);
				}
			}
		} else {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourcePath));
			if (Util.canLaunchIntent(context, browserIntent)) {
				context.startActivity(browserIntent);
			}
		}
		return false;
	}

	/**
	 * This function copies file from one location to another
	 *
	 * @param from the full path to the source file
	 * @param to   the full path to the destination file
	 * @return total bytes copied. 0 indicates
	 */
	public static int copyFile(String from, String to) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int bytesum = 0;
			int byteread;
			File oldfile = new File(from);
			if (oldfile.exists()) {
				inStream = new FileInputStream(from);
				fs = new FileOutputStream(to);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
			}
			return bytesum;
		} catch (Exception e) {
			return 0;
		} finally {
			Util.ensureClosed(inStream);
			Util.ensureClosed(fs);
		}
	}

	public static boolean isMimeTypeImage(String mimeType) {
		if (TextUtils.isEmpty(mimeType)) {
			return false;
		}

		String fileType = mimeType.substring(0, mimeType.indexOf("/"));
		return (fileType.equalsIgnoreCase("Image"));
	}

	/**
	 * This method creates a cached file exactly copying from the input stream.
	 *
	 * @param context       context for resolving uri
	 * @param sourceUrl     the source file path or uri string
	 * @param localFilePath the cache file path string
	 * @param mimeType      the mimeType of the source inputstream
	 * @return null if failed, otherwise a StoredFile object
	 */
	public static StoredFile createLocalStoredFile(Context context, String sourceUrl, String localFilePath, String mimeType) {
		InputStream is = null;
		try {
			if (URLUtil.isContentUrl(sourceUrl) && context != null) {
				Uri uri = Uri.parse(sourceUrl);
				is = context.getContentResolver().openInputStream(uri);
			} else {
				File file = new File(sourceUrl);
				is = new FileInputStream(file);
			}
			return createLocalStoredFile(is, sourceUrl, localFilePath, mimeType);

		} catch (FileNotFoundException e) {
			return null;
		} finally {
			ensureClosed(is);
		}
	}

	/**
	 * This method creates a cached file copy from the source input stream.
	 *
	 * @param is            the source input stream
	 * @param sourceUrl     the source file path or uri string
	 * @param localFilePath the cache file path string
	 * @param mimeType      the mimeType of the source inputstream
	 * @return null if failed, otherwise a StoredFile object
	 */
	public static StoredFile createLocalStoredFile(InputStream is, String sourceUrl, String localFilePath, String mimeType) {

		if (is == null) {
			return null;
		}
		// Copy the file contents over.
		CountingOutputStream cos = null;
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			File localFile = new File(localFilePath);
	  /* Local cache file name may not be unique, and can be reused, in which case, the previously created
       * cache file need to be deleted before it is being copied over.
       */
			if (localFile.exists()) {
				localFile.delete();
			}
			fos = new FileOutputStream(localFile);
			bos = new BufferedOutputStream(fos);
			cos = new CountingOutputStream(bos);
			byte[] buf = new byte[2048];
			int count;
			while ((count = is.read(buf, 0, 2048)) != -1) {
				cos.write(buf, 0, count);
			}
			Log.d("File saved, size = " + (cos.getBytesWritten() / 1024) + "k");
		} catch (IOException e) {
			Log.e("Error creating local copy of file attachment.");
			return null;
		} finally {
			Util.ensureClosed(cos);
			Util.ensureClosed(bos);
			Util.ensureClosed(fos);
		}

		// Create a StoredFile database entry for this locally saved file.
		StoredFile storedFile = new StoredFile();
		storedFile.setSourceUriOrPath(sourceUrl);
		storedFile.setLocalFilePath(localFilePath);
		storedFile.setMimeType(mimeType);
		return storedFile;
	}

}

