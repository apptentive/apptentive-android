/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;
import com.apptentive.android.sdk.util.CountingOutputStream;
import com.apptentive.android.sdk.util.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ImageUtil {

	private static final int MAX_SENT_IMAGE_EDGE = 1024;

	/**
	 * From <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Loading Large Bitmaps Efficiently</a>
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * This method decodes a bitmap from a file, and does pixel combining in order to produce an in-memory bitmap that is
	 * smaller than the original. It will create only the returned bitmap in memory.
	 * From <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Loading Large Bitmaps Efficiently</a>
	 *
	 * @param fileAbsolutePath Full absolute path  to the image file. (optional, maybe null)
	 * @param fileUri          content uri of the source image. (optional, maybe null)
	 * @param minShrunkWidth   If edge of this image is greater than minShrunkWidth, the image will be shrunken such it is not smaller than minShrunkWidth.
	 * @param minShrunkHeight  If edge of this image is greater than minShrunkHeight, the image will be shrunken such it is not smaller than minShrunkHeight.
	 * @param config           You can use this to change the number of bytes per pixel using various bitmap configurations.
	 * @param orientation      The orientation for the image expressed as degrees
	 * @return A bitmap whose edges are equal to or less than minShrunkEdge in length.
	 */
	private static Bitmap createLightweightScaledBitmap(String fileAbsolutePath, Uri fileUri, int minShrunkWidth, int minShrunkHeight, Bitmap.Config config, int orientation) {
		boolean bCreateFromUri;
		Context context = ApptentiveInternal.getInstance().getApplicationContext();
		if (context != null && fileUri != null) {
			bCreateFromUri = true;
		} else if (!TextUtils.isEmpty(fileAbsolutePath)) {
			bCreateFromUri = false;
		} else {
			return null;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		if (config != null) {
			options.inPreferredConfig = config;
		}

		final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
		decodeBoundsOptions.inJustDecodeBounds = true;
		decodeBoundsOptions.inScaled = false;

		// Obtain image dimensions without actually decode the image into memory
		if (bCreateFromUri && context != null) {
			InputStream is = null;
			try {
				is = context.getContentResolver().openInputStream(fileUri);
				BitmapFactory.decodeStream(is, null, decodeBoundsOptions);
			} catch (FileNotFoundException e) {
				throw new NullPointerException("Failed to decode image");
			} finally {
				Util.ensureClosed(is);
			}
		} else if (!bCreateFromUri){
			BitmapFactory.decodeFile(fileAbsolutePath, decodeBoundsOptions);
		}


		int width, height;

		if (orientation == 90 || orientation == 270) {
			//noinspection SuspiciousNameCombination
			width = decodeBoundsOptions.outHeight;
			//noinspection SuspiciousNameCombination
			height = decodeBoundsOptions.outWidth;
		} else {
			width = decodeBoundsOptions.outWidth;
			height = decodeBoundsOptions.outHeight;
		}

		ApptentiveLog.v(UTIL, "Original bitmap dimensions: %d x %d", width, height);
		int sampleRatio = Math.min(width / minShrunkWidth, height / minShrunkHeight);
		if (sampleRatio >= 2) {
			options.inSampleSize = sampleRatio;
		}
		options.inScaled = false;
		options.inJustDecodeBounds = false;
		ApptentiveLog.v(UTIL, "Bitmap sample size = %d", options.inSampleSize);

		Bitmap retImg = null;
		if (bCreateFromUri && context != null) {
			InputStream is = null;
			try {
				is = context.getContentResolver().openInputStream(fileUri);
				retImg = BitmapFactory.decodeStream(is, null, options);
			} catch (FileNotFoundException e) {
				throw new NullPointerException("Failed to decode image");
			} finally {
				Util.ensureClosed(is);
			}
		} else if (!bCreateFromUri){
			retImg = BitmapFactory.decodeFile(fileAbsolutePath, options);
		}


		ApptentiveLog.v(UTIL, "Sampled bitmap size = %d X %d", options.outWidth, options.outHeight);

		if ((orientation != 0 && orientation != -1) && retImg != null) {
			Matrix matrix = new Matrix();
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					matrix.postRotate(90);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					matrix.postRotate(180);
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					matrix.postRotate(270);
					break;
			}
            try {
				retImg = Bitmap.createBitmap(retImg, 0, 0, retImg.getWidth(),
						retImg.getHeight(), matrix, true);
			} catch (IllegalArgumentException e) {
				throw new NullPointerException("Failed to decode image");
			}
		}

		if (retImg == null) {
			throw new NullPointerException("Failed to decode image");
		}

		return retImg;
	}

	/**
	 * This method first uses a straight binary pixel conversion to shrink an image to *almost* the right size, and then
	 * performs a scaling of this resulting bitmap to achieve the final size. It will create two bitmaps in memory while it
	 * is running.
	 *
	 * @param fileUrl     either full absolute path to the source image file or the content uri to the source image
	 * @param maxWidth    The maximum width to scale this image to, or 0 to ignore this parameter.
	 * @param maxHeight   The maximum height to scale this image to, or 0 to ignore this parameter.
	 * @param config      A Bitmap.Config to apply to the Bitmap as it is read in.
	 * @param orientation The orientation for the image expressed as degrees
	 * @return A Bitmap scaled by maxWidth, maxHeight, and config.
	 */
	public synchronized static Bitmap createScaledBitmapFromLocalImageSource(String fileUrl, int maxWidth, int maxHeight, Bitmap.Config config, int orientation)
			throws FileNotFoundException {
		Bitmap tempBitmap = null;

		if (URLUtil.isContentUrl(fileUrl)) {
			try {
				Uri uri = Uri.parse(fileUrl);
				tempBitmap = createLightweightScaledBitmap(null, uri, maxWidth, maxHeight, config, orientation);
			} catch (NullPointerException e) {
				throw new NullPointerException("Failed to create scaled bitmap");
			}
		} else {
			File file = new File(fileUrl);
			if (file.exists()) {
				try {
					tempBitmap = createLightweightScaledBitmap(fileUrl, null, maxWidth, maxHeight, config, orientation);
				} catch (NullPointerException e) {
					throw new NullPointerException("Failed to create scaled bitmap");
				}
			} else {
				throw new FileNotFoundException("Source file does not exist any more");
			}
		}

		// Start by grabbing the bitmap from file, sampling down a little first if the image is huge.
		if (tempBitmap == null) {
			return null;
		}

		Bitmap outBitmap = tempBitmap;
		int width = tempBitmap.getWidth();
		int height = tempBitmap.getHeight();

		// Find the greatest ration difference, as this is what we will shrink both sides to.
		float ratio = calculateBitmapScaleFactor(width, height, maxWidth, maxHeight);

		if (ratio < 1.0f) { // Don't blow up small images, only shrink bigger ones.
			int newWidth = (int) (ratio * width);
			int newHeight = (int) (ratio * height);
			ApptentiveLog.v(UTIL, "Scaling image further down to %d x %d", newWidth, newHeight);
			try {
				outBitmap = Bitmap.createScaledBitmap(tempBitmap, newWidth, newHeight, true);
			} catch (IllegalArgumentException e) {
				throw new NullPointerException("Failed to create scaled bitmap");
			}
			ApptentiveLog.v(UTIL, "Final bitmap dimensions: %d x %d", outBitmap.getWidth(), outBitmap.getHeight());
			tempBitmap.recycle();
		}
		return outBitmap;
	}

	public static float calculateBitmapScaleFactor(int width, int height, int maxWidth, int maxHeight) {
		float widthRatio = maxWidth <= 0 ? 1.0f : (float) maxWidth / width;
		float heightRatio = maxHeight <= 0 ? 1.0f : (float) maxHeight / height;
		return Math.min(1.0f, Math.min(widthRatio, heightRatio)); // Don't scale above 1.0x
	}


	public static void startDownloadAvatarTask(ApptentiveAvatarView view, String imageUrl) {
		DownloadImageTask task = new DownloadImageTask(view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
		} else {
			task.execute(imageUrl);
		}
	}

	/**
	 * This method creates a cached version of the original image, and compresses it in the process so it doesn't fill up the disk. Therefore, do not use
	 * it to store an exact copy of the file in question.
	 *
	 * @param sourcePath     can be full file path or content uri string
	 * @param cachedFileName file name of the cache to be created (with full path)
	 * @return true if cache file is created successfully
	 */
	public static boolean createScaledDownImageCacheFile(String sourcePath, String cachedFileName) {
		File localFile = new File(cachedFileName);

		// Retrieve image orientation
		int imageOrientation = 0;
		try {
			ExifInterface exif = new ExifInterface(sourcePath);
			imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e) {

		}

		// Copy the file contents over.
		CountingOutputStream cos = null;
		try {
			cos = new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(localFile)));
			System.gc();
			Bitmap smaller = ImageUtil.createScaledBitmapFromLocalImageSource(sourcePath, MAX_SENT_IMAGE_EDGE, MAX_SENT_IMAGE_EDGE, null, imageOrientation);
			// TODO: Is JPEG what we want here?
			smaller.compress(Bitmap.CompressFormat.JPEG, 95, cos);
			cos.flush();
			ApptentiveLog.v(UTIL, "Bitmap saved, size = " + (cos.getBytesWritten() / 1024) + "k");
			smaller.recycle();
			System.gc();
		} catch (FileNotFoundException e) {
			ApptentiveLog.e(UTIL, e, "File not found while storing image.");
			logException(e);
			return false;
		} catch (Exception e) {
			ApptentiveLog.a(UTIL, e, "Error storing image.");
			logException(e);
			return false;
		} finally {
			Util.ensureClosed(cos);
		}

		return true;
	}

	public static boolean appendScaledDownImageToStream(String sourcePath, OutputStream outputStream) {
		// Retrieve image orientation
		int imageOrientation = 0;
		try {
			ExifInterface exif = new ExifInterface(sourcePath);
			imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e) {
			logException(e);
		}

		// Copy the file contents over.
		CountingOutputStream cos = null;
		try {
			cos = new CountingOutputStream(new BufferedOutputStream(outputStream));
			System.gc();
			Bitmap smaller = ImageUtil.createScaledBitmapFromLocalImageSource(sourcePath, MAX_SENT_IMAGE_EDGE, MAX_SENT_IMAGE_EDGE, null, imageOrientation);
			smaller.compress(Bitmap.CompressFormat.JPEG, 95, cos);
			cos.flush();
			ApptentiveLog.v(UTIL, "Bitmap bytes appended, size = " + (cos.getBytesWritten() / 1024) + "k");
			smaller.recycle();
			return true;
		} catch (Exception e) {
			ApptentiveLog.a(UTIL, e, "Error storing image.");
			logException(e);
			return false;
		} finally {
			Util.ensureClosed(cos);
		}
	}

	private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<ApptentiveAvatarView> resultView;

		DownloadImageTask(ApptentiveAvatarView view) {
			resultView = new WeakReference<ApptentiveAvatarView>(view);
		}

		protected Bitmap doInBackground(String... urls) {
			Bitmap bmp = null;
			try {
				bmp = this.loadImageFromNetwork(urls[0]);
			} catch (IOException e) {
				logException(e);
			}
			return bmp;
		}


		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}

			ApptentiveAvatarView view = resultView.get();
			if (view != null) {
				view.setImageBitmap(result);
			}
		}

		private Bitmap loadImageFromNetwork(String imageUrl) throws IOException {
			URL url = new URL(imageUrl);
			return BitmapFactory.decodeStream(url.openStream());
		}
	}
}
