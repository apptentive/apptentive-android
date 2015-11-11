/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.util.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.image.ImageGridViewAdapter;
import com.apptentive.android.sdk.util.image.ImageUtil;

/**
 * @author Barry Li
 */
public class ApptentiveDrawableLoaderTask extends AsyncTask<String, Void, Bitmap> {
	private WeakReference<ImageView> imageViewReference;
	private WeakReference<Context> contextReference;
	private BitmapLoadListener mListener;
	private boolean mError;
	protected BitmapFactory.Options options;
	private FileInputStream local;
	private Context context;


	public interface BitmapLoadListener {
		public void notFound();

		public void loadBitmap(Bitmap b);

		public void onLoadError();

		public void onLoadCancelled();
	}

	public ApptentiveDrawableLoaderTask(Context context, ImageView imageView, BitmapLoadListener listener) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		contextReference = new WeakReference<Context>(context);
		mListener = listener;
	}

	/**
	 * Conservatively estimates inSampleSize. Given a required width and height,
	 * this method calculates an inSampleSize that will result in a bitmap that is
	 * approximately the size requested, but guaranteed to not be smaller than
	 * what is requested.
	 *
	 * @param options   the {@link BitmapFactory.Options} obtained by decoding the image
	 *                  with inJustDecodeBounds = true
	 * @param reqWidth  the required width
	 * @param reqHeight the required height
	 * @return the calculated inSampleSize
	 */
	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}


	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		InputStream is = null;
		String uri = params[0];
		String filePath = params[1];
		int width = Integer.parseInt(params[2]);
		int height = Integer.parseInt(params[3]);
		if (isCancelled()) {
			return null;
		}

		try {
			String imagePathString = null;
			File imageFile;
			// Always try to load preview from cached file
			if (!TextUtils.isEmpty(filePath)) {
				imageFile = new File(filePath);
				if (imageFile.exists()) {
					is = new FileInputStream(imageFile);
					imagePathString = filePath;
				}
			}

			// If no cache, load from the original file path
			imageFile = new File(uri);
			if (imageFile.exists()) {
				is = new FileInputStream(imageFile);
				imagePathString = uri;
			}

			// If original file path was not obtained, treat it as uri
			if (is == null) {
				Context context = contextReference.get();
				if (context != null) {
					is = context.getContentResolver().openInputStream(Uri.parse(uri));
				}
			}

			if (is == null) {
				return null;
			}

			// Retrieve image orientation
			int imageOrientation = 0;
			try {
				if (imagePathString != null) {
					ExifInterface exif = new ExifInterface(imagePathString);
					imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				}
			} catch (IOException e) {

			}

			//final int dialogWidth
			if (is != null) {
				bitmap = ImageUtil.createLightweightScaledBitmapFromStream(is, width, height, null, imageOrientation);
			} else {
				bitmap = null;
			}

		} catch (FileNotFoundException e) {
			// TODO: Error toast?
			return null;
		} finally {
			Util.ensureClosed(is);
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap == null && !isCancelled()) {
			mListener.notFound();
		} else {
			if (isCancelled()) {
				bitmap = null;
			}
			ImageView imageView = imageViewReference.get();

			if (imageView != null && !mError) {

				if (bitmap != null) {
					mListener.loadBitmap(bitmap);
				} else if (!isCancelled()) {
					mListener.onLoadError();
				} else if (isCancelled()) {
					mListener.onLoadCancelled();
				}
			} else {
				mListener.onLoadError();
			}
		}
	}
}
