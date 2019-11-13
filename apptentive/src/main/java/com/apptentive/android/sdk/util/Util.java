/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.util.AtomicFile;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

// TODO: this class does too much - split into smaller classes and clean up
public class Util {
	private static final String ENCRYPTED_FILENAME_SUFFIX = ".encrypted";

	public static int getStatusBarHeight(Window window) {
		Rect rectangle = new Rect();
		window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
		return rectangle.top;
	}

	public static int pixelsToDips(@NonNull Context context, int px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(px / scale);
	}

	public static float dipsToPixels(@NonNull Context context, float dp) {
		return context.getResources().getDisplayMetrics().density * dp;
	}

	public static float dipsToPixelsFloat(@NonNull Context context, int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return dp * scale;
	}

	/**
	 * Internal use only.
	 */
	public static void hideSoftKeyboard(Context context, View view) {
		if (context != null && view != null) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}


	public static void showSoftKeyboard(Activity activity, View target) {
		if (activity != null && activity.getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(target, 0);
		}
	}

	public static void showToast(final Context context, final String message, final int duration) {
		if (!DispatchQueue.isMainQueue()) {
			DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					showToast(context, message, duration);
				}
			});
			return;
		}

		try {
			Toast.makeText(context, message, duration).show();
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while trying to display toast message");
			logException(e);
		}
	}

	public static boolean isNetworkConnectionPresent() {
		Context context = ApptentiveInternal.getInstance().getApplicationContext();
		if (context == null) {
			return false;
		}

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null) {
				return activeNetwork.isConnectedOrConnecting();
			}
		}
		return false;
	}

	public static void ensureClosed(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				logException(e);
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

	public static String trim(String string) {
		if (string != null) {
			return string.trim();
		}
		return null;
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
							ApptentiveLog.e(e, "Error parsing cache expiration as number: %s", expiration);
							logException(e);
						}
					}
				}
			}
		}
		return null;
	}

	public static boolean isEmailValid(String email) {
		return !StringUtils.isNullOrEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	public static boolean getPackageMetaDataBoolean(Context context, String key) {
		try {
			return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getBoolean(key, false);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static Object getPackageMetaData(Context appContext, String key) {
		try {
			return appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData.get(key);
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
	public static String getPackageMetaDataSingleQuotedString(Context appContext, String key) {
		Object object = getPackageMetaData(appContext, key);
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

	public static String getStackTraceString(Throwable throwable) {
		try {
			return android.util.Log.getStackTraceString(throwable);
		} catch (Exception e) {
			return stackTraceAsString(throwable); // fallback for unit-tests
		}
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
			ApptentiveLog.w(UTIL, e, "Error getting major OS version");
			logException(e);
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
			logException(e);
		}
		return null;
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
			d = ContextCompat.getDrawable(c, drawableRes);
		} catch (Exception ex) {
			logException(ex);
		}
		return d;
	}

	public static int getResourceIdFromAttribute(Resources.Theme theme, int attr) {
		TypedValue tv = new TypedValue();
		if (theme.resolveAttribute(attr, tv, true)) {
			return tv.resourceId;
		}
		return 0;
	}

	public static int getThemeColor(Context context, int attr) {
		if (context == null) {
			return 0;
		}
		return getThemeColor(context.getTheme(), attr);
	}

	public static int getThemeColor(Resources.Theme theme, int attr) {
		TypedValue tv = new TypedValue();
		if (theme.resolveAttribute(attr, tv, true)) {
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
			color = ContextCompat.getColor(ctx, res);
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

	public static int brighter(int color, float factor) {
		int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
		int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
		int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
		return Color.argb(Color.alpha(color), red, green, blue);
	}

	public static int dimmer(int color, float factor) {
		int alpha = (int) (Color.alpha(color) * factor);
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}

	/* Use alpha channel to compute percentage RGB share in blending.
*    * Background color may be visible only if foreground alpha is smaller than 100%
*    */
	public static int alphaMixColors(int backgroundColor, int foregroundColor) {
		final byte ALPHA_CHANNEL = 24;
		final byte RED_CHANNEL = 16;
		final byte GREEN_CHANNEL = 8;
		final byte BLUE_CHANNEL = 0;

		final double ap1 = (double) (backgroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
		final double ap2 = (double) (foregroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
		final double ap = ap2 + (ap1 * (1 - ap2));

		final double amount1 = (ap1 * (1 - ap2)) / ap;
		final double amount2 = amount1 / ap;

		int a = ((int) (ap * 255d)) & 0xff;

		int r = ((int) (((float) (backgroundColor >> RED_CHANNEL & 0xff) * amount1) +
				((float) (foregroundColor >> RED_CHANNEL & 0xff) * amount2))) & 0xff;
		int g = ((int) (((float) (backgroundColor >> GREEN_CHANNEL & 0xff) * amount1) +
				((float) (foregroundColor >> GREEN_CHANNEL & 0xff) * amount2))) & 0xff;
		int b = ((int) (((float) (backgroundColor & 0xff) * amount1) +
				((float) (foregroundColor & 0xff) * amount2))) & 0xff;

		return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
	}


	public static boolean canLaunchIntent(Context context, Intent intent) {
		if (context == null) {
			return false;
		}

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
		return (context != null) ? context.getContentResolver().getType(contentUri) : null;
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
			logException(e);
		}
		return null;
	}

	public static String generateRandomFilename() {
		return UUID.randomUUID().toString();
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

		if (appCacheDir == null && context != null) {
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
		if (context == null) {
			return false;
		}
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
					ApptentiveLog.e(e, "Activity not found to open attachment: ");
					logException(e);
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

	public static void writeBytes(File file, byte[] bytes) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("'file' is null");
		}

		if (bytes == null) {
			throw new IllegalArgumentException("'bytes' is null");
		}

		File parentFile = file.getParentFile();
		if (!parentFile.exists() && !parentFile.mkdirs()) {
			throw new IOException("Parent file could not be created: " + parentFile);
		}

		ByteArrayInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new ByteArrayInputStream(bytes);
			output = new FileOutputStream(file);
			copy(input, output);
		} finally {
			ensureClosed(input);
			ensureClosed(output);
		}
	}

	public static byte[] readBytes(File file) throws IOException {
		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			appendFileToStream(file, output);
			return output.toByteArray();
		} finally {
			ensureClosed(output);
		}
	}

	public static void writeText(File file, String text) throws IOException {
		if (text == null) {
			throw new IllegalArgumentException("'text' is null");
		}

		PrintStream output = null;
		try {
			output = openTextWrite(file, false); // TODO: make a parameter
			output.print(text);
		} finally {
			ensureClosed(output);
		}
	}

	public static void writeText(File file, List<String> text) throws IOException {
		writeText(file, text, false);
	}

	public static void writeText(File file, List<String> text, boolean append) throws IOException {
		if (text == null) {
			throw new IllegalArgumentException("'text' is null");
		}

		PrintStream output = null;
		try {
			output = openTextWrite(file, append);
			for (String line : text) {
				output.println(line);
			}
		} finally {
			ensureClosed(output);
		}
	}

	private static PrintStream openTextWrite(File file, boolean append) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("'file' is null");
		}

		File parentFile = file.getParentFile();
		if (!parentFile.exists() && !parentFile.mkdirs()) {
			throw new IOException("Parent file could not be created: " + parentFile);
		}

		return new PrintStream(new FileOutputStream(file, append), false, "UTF-8");
	}

	public static void appendFileToStream(File file, OutputStream outputStream) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("'file' is null");
		}

		if (!file.exists()) {
			throw new FileNotFoundException("File does not exist: " + file);
		}

		if (file.isDirectory()) {
			throw new FileNotFoundException("File is directory: " + file);
		}

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			copy(input, outputStream);
		} finally {
			ensureClosed(input);
		}
	}

	/**
	 * Performs an 'atomic' write to a file (to avoid data corruption)
	 */
	public static void writeAtomically(File file, byte[] bytes) throws IOException {
		AtomicFile atomicFile = new AtomicFile(file);
		FileOutputStream stream = null;
		try {
			stream = atomicFile.startWrite();
			stream.write(bytes);
			atomicFile.finishWrite(stream); // serialization was successful
		} catch (Exception e) {
			atomicFile.failWrite(stream); // serialization failed
			throw new IOException(e); // throw exception up the chain
		}
	}

	private static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) > 0) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static void writeNullableUTF(DataOutput out, @Nullable String value) throws IOException {
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeUTF(value);
		}
	}

	public static String readNullableUTF(DataInput in) throws IOException {
		boolean notNull = in.readBoolean();
		return notNull ? in.readUTF() : null;
	}

	public static void writeNullableBoolean(DataOutput out, Boolean value) throws IOException {
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeBoolean(value);
		}
	}

	public static Boolean readNullableBoolean(DataInput in) throws IOException {
		boolean notNull = in.readBoolean();
		return notNull ? in.readBoolean() : null;
	}

	public static void writeNullableDouble(DataOutput out, Double value) throws IOException {
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeDouble(value);
		}
	}

	public static Double readNullableDouble(DataInput in) throws IOException {
		boolean notNull = in.readBoolean();
		return notNull ? in.readDouble() : null;
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
	 * @param sourceUrl     the source file path or uri string
	 * @param localFilePath the cache file path string
	 * @param mimeType      the mimeType of the source inputstream
	 * @return null if failed, otherwise a StoredFile object
	 */
	public static StoredFile createLocalStoredFile(String sourceUrl, String localFilePath, String mimeType) {
		InputStream is = null;
		try {
			Context context = ApptentiveInternal.getInstance().getApplicationContext();
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
			ApptentiveLog.v(UTIL, "File saved, size = " + (cos.getBytesWritten() / 1024) + "k");
		} catch (IOException e) {
			ApptentiveLog.e(UTIL, "Error creating local copy of file attachment.");
			logException(e);
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

	public static Activity castContextToActivity(Context context) {
		if (context == null) {
			return null;
		} else if (context instanceof Activity) {
			return (Activity) context;
		} else if (context instanceof ContextWrapper) {
			return castContextToActivity(((ContextWrapper) context).getBaseContext());
		}

		return null;
	}


	/* Utility function to override system default font with an font ttf file from asset. The override
	 * will be applied to the entire application. The ideal place to call this method is from the onCreate()
	  * of the Application.
	  *
	 * Usage: Util.replaceDefaultFont(this, "Tinos-Regular.ttf");
	 *
	 * @param context The application context the font override will be applied to
	 * @param fontFilePath  The file path to the font file in the assets directory
	*/
	public static void replaceDefaultFont(Context context, String fontFilePath) {
		final Typeface newTypeface = Typeface.createFromAsset(context.getAssets(), fontFilePath);

		TypedValue tv = new TypedValue();
		String staticTypefaceFieldName = null;
		Map<String, Typeface> newMap = null;

		Resources.Theme apptentiveTheme = context.getResources().newTheme();
		ApptentiveInternal.getInstance().updateApptentiveInteractionTheme(context, apptentiveTheme);

		if (apptentiveTheme == null) {
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (apptentiveTheme.resolveAttribute(R.attr.apptentiveFontFamilyDefault, tv, true)) {
				newMap = new HashMap<String, Typeface>();
				newMap.put(tv.string.toString(), newTypeface);
			}
			if (apptentiveTheme.resolveAttribute(R.attr.apptentiveFontFamilyMediumDefault, tv, true)) {
				if (newMap == null) {
					newMap = new HashMap<String, Typeface>();
				}
				newMap.put(tv.string.toString(), newTypeface);
			}
			if (newMap != null) {
				try {
					final Field staticField = Typeface.class.getDeclaredField("sSystemFontMap");
					staticField.setAccessible(true);
					staticField.set(null, newMap);
				} catch (NoSuchFieldException e) {
					ApptentiveLog.e(e, "Exception replacing system font");
					logException(e);
				} catch (IllegalAccessException e) {
					ApptentiveLog.e(e, "Exception replacing system font");
					logException(e);
				}
			}
		} else {
			if (apptentiveTheme.resolveAttribute(R.attr.apptentiveTypefaceDefault, tv, true)) {
				staticTypefaceFieldName = "DEFAULT";
				if (tv.data == context.getResources().getInteger(R.integer.apptentive_typeface_monospace)) {
					staticTypefaceFieldName = "MONOSPACE";
				} else if (tv.data == context.getResources().getInteger(R.integer.apptentive_typeface_serif)) {
					staticTypefaceFieldName = "SERIF";
				} else if (tv.data == context.getResources().getInteger(R.integer.apptentive_typeface_sans)) {
					staticTypefaceFieldName = "SANS_SERIF";
				}
				try {
					final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
					staticField.setAccessible(true);
					staticField.set(null, newTypeface);
				} catch (NoSuchFieldException e) {
					ApptentiveLog.e(e, "Exception replacing system font");
					logException(e);
				} catch (IllegalAccessException e) {
					ApptentiveLog.e(e, "Exception replacing system font");
					logException(e);
				}
			}
		}
	}

	/**
	 * Builds out the main theme that we would like to use for all Apptentive UI, basing it on the
	 * existing app theme, and adding Apptentive's theme where it doesn't override the existing app's
	 * attributes. Finally, it forces changes to the theme using ApptentiveThemeOverride.
	 * @param context The context for the app or Activity whose theme we want to inherit from.
	 * @return A {@link Resources.Theme}
	 */
	public static Resources.Theme buildApptentiveInteractionTheme(Context context) {
		Resources.Theme theme = context.getResources().newTheme();

		// 1. Start by basing this on the Apptentive theme.
		theme.applyStyle(R.style.ApptentiveTheme_Base_Versioned, true);

		// 2. Get the theme from the host app. Overwrite what we have so far with the app's theme from
		// the AndroidManifest.xml. This ensures that the app's styling shows up in our UI.
		int appTheme;
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			ApplicationInfo ai = packageInfo.applicationInfo;
			appTheme = ai.theme;
			if (appTheme != 0) {
				theme.applyStyle(appTheme, true);
			}
		} catch (PackageManager.NameNotFoundException e) {
			// Can't happen
			return null;
		}

		// Step 3: Restore Apptentive UI window properties that may have been overridden in Step 2. This
		// ensures Apptentive interaction has a modal feel and look.
		theme.applyStyle(R.style.ApptentiveBaseFrameTheme, true);

		// Step 4: Apply optional theme override specified in host app's style
		int themeOverrideResId = context.getResources().getIdentifier("ApptentiveThemeOverride", "style", context.getPackageName());
		if (themeOverrideResId != 0) {
			theme.applyStyle(themeOverrideResId, true);
		}
		return theme;
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String getAndroidID(Context context) {
		if (context == null) {
			return null;
		}
		return Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	}
	/**
	 * Returns and internal storage directory
	 */
	public static File getInternalDir(Context context, String path, boolean createIfNecessary) {
		File filesDir = context.getFilesDir();
		File internalDir = new File(filesDir, path);
		if (!internalDir.exists() && createIfNecessary) {
			boolean succeed = internalDir.mkdirs();
			if (!succeed) {
				ApptentiveLog.w(UTIL, "Unable to create internal directory: %s", internalDir);
			}
		}
		return internalDir;
	}

	/**
	 * Helper method for resolving manifest metadata string value
	 *
	 * @return null if key is missing or exception is thrown
	 */
	public static String getManifestMetadataString(Context context, String key) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (key == null) {
			throw new IllegalArgumentException("Key is null");
		}

		try {
			String appPackageName = context.getPackageName();
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_META_DATA | PackageManager.GET_RECEIVERS);
			Bundle metaData = packageInfo.applicationInfo.metaData;
			if (metaData != null) {
				return Util.trim(metaData.getString(key));
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Unexpected error while reading application or package info.");
			logException(e);
		}

		return null;
	}

	public static String getClipboardText(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		if (!manager.hasPrimaryClip()) {
			return null;
		}

		ClipDescription primaryClipDescription = manager.getPrimaryClipDescription();
		if (!primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && !primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
			return null;
		}

		ClipData clip = manager.getPrimaryClip();
		if (clip != null && clip.getItemCount() > 0) {
			return clip.getItemAt(0).getText().toString();
		}

		return null;
	}

	public static void setClipboardText(Context context, String text) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		manager.setPrimaryClip(ClipData.newPlainText(null, text));
	}

	/**
	 * Creates a fail-safe try..catch wrapped listener
	 */
	public static @Nullable View.OnClickListener guarded(@Nullable final View.OnClickListener listener) {
		if (listener != null) {
			return new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						listener.onClick(v);
					} catch (Exception e) {
						ApptentiveLog.e(e, "Exception while handling click listener");
						logException(e);
					}
				}
			};
		}

		return null;
	}
  
	public static String currentDateAsFilename(String prefix, String suffix) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.US);
		return prefix + df.format(new Date()) + suffix;
	}

	public static @Nullable Intent makeRestartActivityTask(ComponentName cn) {
		try {
			return makeRestartActivityTaskGuarded(cn);
		}
		catch (Exception e) {
			ApptentiveLog.e(e, "Exception in makeRestartActivityTask");
			logException(e);
		}

		return null;
	}

	private static Intent makeRestartActivityTaskGuarded(ComponentName cn) throws InvocationException {
		try {
			return Intent.makeRestartActivityTask(cn);
		} catch (NoSuchMethodError e) {
			//return IntentCompat.makeRestartActivityTask(cn);
			Invocation invocation = Invocation.fromClass(IntentCompat.class);
			return (Intent) invocation.invokeMethod("makeRestartActivityTask", new Class<?>[]{ComponentName.class}, new Object[]{cn});
		}
	}

	public static File getEncryptedFilename(File file) {
		String filename = file.getName();
		return filename.endsWith(ENCRYPTED_FILENAME_SUFFIX) ? file : new File(file.getParent(), filename + ENCRYPTED_FILENAME_SUFFIX);
	}

	public static File getUnencryptedFilename(File file) {
		String filename = file.getName();
		return filename.endsWith(ENCRYPTED_FILENAME_SUFFIX) ? new File(file.getParent(), filename.substring(0, filename.length() - ENCRYPTED_FILENAME_SUFFIX.length())) : file;
	}
}
