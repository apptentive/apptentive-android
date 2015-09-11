/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveAvatarView;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * @author Sky Kelsey
 */
public class ImageUtil {

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
	 * @param is              An InputStream containing the bytes of an image.
	 * @param minShrunkWidth  If edge of this image is greater than minShrunkWidth, the image will be shrunken such it is not smaller than minShrunkWidth.
	 * @param minShrunkHeight If edge of this image is greater than minShrunkHeight, the image will be shrunken such it is not smaller than minShrunkHeight.
	 * @param config          You can use this to change the number of bytes per pixel using various bitmap configurations.
	 * @param orientation     The orientation for the image expressed as degrees
	 * @return A bitmap whose edges are equal to or less than minShrunkEdge in length.
	 */
	public static Bitmap createLightweightScaledBitmapFromStream(InputStream is, int minShrunkWidth, int minShrunkHeight, Bitmap.Config config, int orientation) {

		BufferedInputStream bis = new BufferedInputStream(is, 32 * 1024);
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (config != null) {
				options.inPreferredConfig = config;
			}

			final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
			decodeBoundsOptions.inJustDecodeBounds = true;
			bis.mark(Integer.MAX_VALUE);
			BitmapFactory.decodeStream(bis, null, decodeBoundsOptions);
			bis.reset();

			int width, height;

			if (orientation == 90 || orientation == 270) {
				width = decodeBoundsOptions.outHeight;
				height = decodeBoundsOptions.outWidth;
			} else {
				width = decodeBoundsOptions.outWidth;
				height = decodeBoundsOptions.outHeight;
			}

			Log.v("Original bitmap dimensions: %d x %d", width, height);
			int sampleRatio = Math.min(width / minShrunkWidth, height / minShrunkHeight);
			if (sampleRatio >= 2) {
				options.inSampleSize = sampleRatio;
			}
			Log.v("Bitmap sample size = %d", options.inSampleSize);

			Bitmap retImg = BitmapFactory.decodeStream(bis, null, options);
			Log.d("Sampled bitmap size = %d X %d", options.outWidth, options.outHeight);

			if (orientation > 0) {
				Matrix matrix = new Matrix();
				matrix.postRotate(orientation);

				retImg = Bitmap.createBitmap(retImg, 0, 0, retImg.getWidth(),
						retImg.getHeight(), matrix, true);
			}

			return retImg;
		} catch (IOException e) {
			Log.e("Error resizing bitmap from InputStream.", e);
		} finally {
			Util.ensureClosed(bis);
		}
		return null;
	}

	/**
	 * This method first uses a straight binary pixel conversion to shrink an image to *almost* the right size, and then
	 * performs a scaling of this resulting bitmap to achieve the final size. It will create two bitmaps in memory while it
	 * is running.
	 *
	 * @param is        An InputStream from the image file.
	 * @param maxWidth  The maximum width to scale this image to, or 0 to ignore this parameter.
	 * @param maxHeight The maximum height to scale this image to, or 0 to ignore this parameter.
	 * @param config    A Bitmap.Config to apply to the Bitmap as it is read in.
	 * @param orientation     The orientation for the image expressed as degrees
	 * @return A Bitmap scaled by maxWidth, maxHeight, and config.
	 */
	public static Bitmap createScaledBitmapFromStream(InputStream is, int maxWidth, int maxHeight, Bitmap.Config config, int orientation) {

		// Start by grabbing the bitmap from file, sampling down a little first if the image is huge.
		Bitmap tempBitmap = createLightweightScaledBitmapFromStream(is, maxWidth, maxHeight, config, orientation);

		Bitmap outBitmap = tempBitmap;
		int width = tempBitmap.getWidth();
		int height = tempBitmap.getHeight();

		// Find the greatest ration difference, as this is what we will shrink both sides to.
		float ratio = calculateBitmapScaleFactor(width, height, maxWidth, maxHeight);

		if (ratio < 1.0f) { // Don't blow up small images, only shrink bigger ones.
			int newWidth = (int) (ratio * width);
			int newHeight = (int) (ratio * height);
			Log.v("Scaling image further down to %d x %d", newWidth, newHeight);
			outBitmap = Bitmap.createScaledBitmap(tempBitmap, newWidth, newHeight, true);
			Log.d("Final bitmap dimensions: %d x %d", outBitmap.getWidth(), outBitmap.getHeight());
			tempBitmap.recycle();
		}
		return outBitmap;
	}

	public static float calculateBitmapScaleFactor(int width, int height, int maxWidth, int maxHeight) {
		float widthRatio = maxWidth <= 0 ? 1.0f : (float) maxWidth / width;
		float heightRatio = maxHeight <= 0 ? 1.0f : (float) maxHeight / height;
		return Math.min(1.0f, Math.min(widthRatio, heightRatio)); // Don't scale above 1.0x
	}

	public static Bitmap resizeImageForImageView(Context context, String imagePath) {
		Bitmap resizedBitmap = null;

		FileInputStream fis = null;
		try {
			fis = context.openFileInput(imagePath);
			Point point = Util.getScreenSize(context);
			int maxImageWidth = (int) (0.5 * point.x);
			int maxImageHeight = (int) (0.5 * point.x);
			maxImageWidth = maxImageWidth > 800 ? 800 : maxImageWidth;
			maxImageHeight = maxImageHeight > 800 ? 800 : maxImageHeight;
			resizedBitmap = createScaledBitmapFromStream(fis, maxImageWidth, maxImageHeight, null, 0);
		} catch (Exception e) {
			Log.e("Error opening stored image.", e);
		} catch (OutOfMemoryError e) {
			// It's generally not a good idea to catch an OutOfMemoryException. But in this case, the OutOfMemoryException
			// had to result from allocating a bitmap, so the system should be in a good state.
			// TODO: Log an event to the server so we know an OutOfMemoryException occurred.
			Log.e("Ran out of memory opening image.", e);
		} finally {
			Util.ensureClosed(fis);
		}

		return resizedBitmap;
	}

	public static void startDownloadAvatarTask(ApptentiveAvatarView view, String imageUrl) {
		DownloadImageTask task = new DownloadImageTask(view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
		} else {
			task.execute(imageUrl);
		}
	}

	private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<ApptentiveAvatarView> resultView;

		DownloadImageTask(ApptentiveAvatarView view) {
			resultView = new WeakReference<>(view);
		}

		protected Bitmap doInBackground(String... urls) {
			Bitmap bmp = null;
			try {
				bmp = this.loadImageFromNetwork(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
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
