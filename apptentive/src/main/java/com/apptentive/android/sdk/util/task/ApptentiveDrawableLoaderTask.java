/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.util.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.apptentive.android.sdk.util.image.ImageUtil;

/**
 * @author Barry Li
 */
public class ApptentiveDrawableLoaderTask extends AsyncTask<String, Void, Bitmap> {
	private WeakReference<ImageView> imageViewReference;
	private WeakReference<Context> contextReference;
	private BitmapLoadListener mListener;
	protected BitmapFactory.Options options;
	private boolean decoderError;


	public interface BitmapLoadListener {
		void notFound();

		void loadBitmap(Bitmap b);

		void onLoadError();

		void onLoadCancelled();
	}

	public ApptentiveDrawableLoaderTask(Context context, ImageView imageView, BitmapLoadListener listener) {
		imageViewReference = new WeakReference<ImageView>(imageView);
		contextReference = new WeakReference<Context>(context);
		mListener = listener;
	}


	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		String uri = params[0];
		String cachedFilePath = params[1];
		int width = Integer.parseInt(params[2]);
		int height = Integer.parseInt(params[3]);
		if (isCancelled()) {
			return null;
		}


		File imageFile;

		// Always try to load image from apptentive cached copy first
		if (!TextUtils.isEmpty(cachedFilePath)) {
			imageFile = new File(cachedFilePath);
			if (imageFile.exists()) {
				bitmap = loadFromLocalImageSource(cachedFilePath, width, height, false);
			}
		}
		// Then try to load the image from the original uri/file path to the source
		if (bitmap == null) {
			bitmap = loadFromLocalImageSource(uri, width, height, false);
		}

		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap == null && !decoderError && !isCancelled()) {
			// bitmap not found locally. Make callback to download
			mListener.notFound();
		} else {
			if (isCancelled()) {
				bitmap = null;
			}
			ImageView imageView = imageViewReference.get();
			if (imageView != null && !decoderError) {
                // Able to decode bitmap from local cache or local uri, go ahead loading it.
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

	private Bitmap loadFromLocalImageSource(final String fileLocation, final int width, final int height, final boolean bDeleteSourceIfCorrupted) {
		Bitmap bitmap = null;

		if (TextUtils.isEmpty(fileLocation)) {
			return null;
		}

		int imageOrientation = 0;
		try {
			ExifInterface exif = new ExifInterface(fileLocation);
			imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e) {
			// Fail to obtain orientation from Exif, Just ignore, and treat it as 0
		}

		decoderError = false;
		try {
			bitmap = ImageUtil.createScaledBitmapFromLocalImageSource(contextReference.get(), fileLocation, width, height, null, imageOrientation);
		} catch (FileNotFoundException e) {
			if (!URLUtil.isValidUrl(fileLocation)) {
				decoderError = true;
			}
		} catch (Exception e) {
			decoderError = true;
			if (bDeleteSourceIfCorrupted) {
				File cachedFile = new File(fileLocation);
				if (cachedFile.exists()) {
					cachedFile.delete();
				}
			}
		}

		return bitmap;
	}
}
