/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.cache.ImageMemoryCache;
import com.apptentive.android.sdk.util.task.ApptentiveDownloaderTask;
import com.apptentive.android.sdk.util.task.ApptentiveDrawableLoaderTask;

/**
 * @author Barry Li
 */
public class ApptentiveAttachmentLoader {

	public static final int DRAWABLE_DOWNLOAD_TAG = R.id.drawable_downloader;

	private ImageMemoryCache bitmapMemoryCache;
	private ArrayList<LoaderRequest> queuedLoaderRequests;
	private ArrayList<LoaderRequest> runningLoaderRequests;
	private HashMap<String, ArrayList<LoaderRequest>> duplicateDownloads;
	private int maxDownloads;
	private Drawable mErrorDrawable;
	private Drawable mInProgressDrawable;
	private int mErrorDrawableResource;
	private int mInProgressDrawableResource;

	public static interface LoaderCallback {
		public void onLoaded(ImageView view, int pos, Drawable d);

		public void onDownloadStart();

		public void onDownloadProgress(int progress);

	}

	private volatile static ApptentiveAttachmentLoader instance;

	/**
	 * Returns singleton class instance
	 */
	public static ApptentiveAttachmentLoader getInstance() {
		if (instance == null) {
			synchronized (ApptentiveAttachmentLoader.class) {
				if (instance == null) {
					instance = new ApptentiveAttachmentLoader();
				}
			}
		}
		return instance;
	}

	protected ApptentiveAttachmentLoader() {
		setup(5);
	}

	protected ApptentiveAttachmentLoader(int maxDownloads) {
		setup(maxDownloads);
	}

	private void setup(int maxDownloads) {
		queuedLoaderRequests = new ArrayList<LoaderRequest>();
		runningLoaderRequests = new ArrayList<LoaderRequest>();
		this.maxDownloads = maxDownloads;
		duplicateDownloads = new HashMap<String, ArrayList<LoaderRequest>>();
		bitmapMemoryCache = new ImageMemoryCache(5);
	}

	public void setErrorDrawable(Drawable errorDrawable) {
		mErrorDrawable = errorDrawable;
		mErrorDrawableResource = -1;
	}

	public void setInProgressDrawable(Drawable inProgressDrawable) {
		mInProgressDrawable = inProgressDrawable;
		mInProgressDrawableResource = -1;
	}

	public void setErrorDrawable(int errorDrawable) {
		mErrorDrawable = null;
		mErrorDrawableResource = errorDrawable;
	}

	public void setInProgressDrawable(int inProgressDrawable) {
		mInProgressDrawable = null;
		mInProgressDrawableResource = inProgressDrawable;
	}

	public void load(String uri, String diskFilePath, int pos, ImageView imageView, int width, int height, boolean bLoadImage, LoaderCallback callback) {
		LoaderRequest d = new LoaderRequest(uri, diskFilePath, pos, imageView, width, height, bLoadImage, callback);
		d.load();
	}

	public void cancelAllDownloads() {
		queuedLoaderRequests.clear();
		for (LoaderRequest loaderRequest : runningLoaderRequests) {
			ApptentiveDownloaderTask task = loaderRequest.getDrawableDownloaderTask();
			if (task != null) {
				task.cancel(true);
			}
		}
		runningLoaderRequests.clear();
	}

	public class LoaderRequest implements ApptentiveDownloaderTask.FileDownloadListener, ApptentiveDrawableLoaderTask.BitmapLoadListener {
		private String uri;
		private String diskCacheFilePath;
		private WeakReference<ImageView> mImageViewRef;
		private ApptentiveDownloaderTask mDrawableDownloaderTask;
		private ApptentiveDrawableLoaderTask mDrawableLoaderTask;
		private boolean mIsCancelled;
		private boolean mWasDownloaded = false;
		private int imageViewWidth;
		private int imageViewHeight;
		private boolean bLoadImage;
		private LoaderCallback loadingTaskCallback;
		private int pos;

		public LoaderRequest(String url, String diskPath, int position, ImageView imageView, int width, int height, boolean bLoadImage, LoaderCallback loadingTaskCallback) {
			this.uri = url;
			this.diskCacheFilePath = diskPath;
			this.imageViewWidth = width;
			this.imageViewHeight = height;
			this.mImageViewRef = new WeakReference<ImageView>(imageView);
			mIsCancelled = false;
			this.bLoadImage = bLoadImage;
			this.loadingTaskCallback = loadingTaskCallback;
			this.pos = position;
		}

		public ApptentiveDownloaderTask getDrawableDownloaderTask() {
			return mDrawableDownloaderTask;
		}

		public ImageView getImageView() {
			return mImageViewRef.get();
		}

		public String getUrl() {
			return uri;
		}

		public boolean isLoadingImage() {
			return bLoadImage;
		}

		public LoaderCallback getLoaderCallback() {
			return loadingTaskCallback;
		}

		public void load() {
			ImageView imageView = mImageViewRef.get();
			if (imageView != null) {
				// find the old download, cancel it and set this download as the current
				// download for the imageview
				LoaderRequest oldLoaderRequest = (LoaderRequest) imageView.getTag(DRAWABLE_DOWNLOAD_TAG);
				if (oldLoaderRequest != null) {
					oldLoaderRequest.cancel();
				}

				if (TextUtils.isEmpty(uri)) {
					return;
				}

				Bitmap cachedBitmap = (bLoadImage) ? (Bitmap) bitmapMemoryCache.getObjectFromCache(ImageMemoryCache.generateMemoryCacheEntryKey(uri, imageViewWidth, imageViewHeight)) :
						null;
				if (cachedBitmap != null) {
					mWasDownloaded = false;
					BitmapDrawable bm = new BitmapDrawable(imageView.getResources(), cachedBitmap);
					loadDrawable(bm);
					imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
				} else {
					imageView.setTag(DRAWABLE_DOWNLOAD_TAG, this);
					if (bLoadImage) {
						loadImageFromDisk(imageView);
					} else {
						loadAttachmentFromDisk(imageView);
					}
				}
			}
		}

		public void doDownload() {
			if (mIsCancelled) {
				// if the download has been cancelled, do not download
				// this image, but start the next one
				if (!queuedLoaderRequests.isEmpty() && runningLoaderRequests.size() < maxDownloads) {
					LoaderRequest d = queuedLoaderRequests.remove(0);
					d.doDownload();
				}
				return;
			}
			ImageView imageView = mImageViewRef.get();
			if (imageView != null && imageView.getTag(DRAWABLE_DOWNLOAD_TAG) == this) {
				mDrawableDownloaderTask = new ApptentiveDownloaderTask(imageView, this);
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mDrawableDownloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, diskCacheFilePath);
					} else {
						mDrawableDownloaderTask.execute(uri, diskCacheFilePath);
					}
				} catch (RejectedExecutionException e) {
				}
				Log.d("doDownload: " + uri);
				runningLoaderRequests.add(this);
			}
		}

		private boolean isBeingDownloaded() {
			for (LoaderRequest loaderRequest : runningLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && loaderRequest.getUrl().equals(uri)) {
					return true;
				}
			}
			return false;
		}

		@SuppressLint("NewApi")
		private void loadImageFromDisk(ImageView imageView) {
			if (imageView != null && !mIsCancelled) {
				mDrawableLoaderTask = new ApptentiveDrawableLoaderTask(imageView.getContext(), imageView, this);
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mDrawableLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, diskCacheFilePath, String.valueOf(imageViewWidth), String.valueOf(imageViewHeight));
					} else {
						mDrawableLoaderTask.execute(uri, diskCacheFilePath, String.valueOf(imageViewWidth), String.valueOf(imageViewHeight));
					}
				} catch (RejectedExecutionException e) {
				}
			}
		}

		@SuppressLint("NewApi")
		private void loadAttachmentFromDisk(ImageView imageView) {
			if (!mIsCancelled) {
				notFound();
			}
		}

		private void cancel() {
			Log.d("cancel requested for: " + uri);
			mIsCancelled = true;
			if (queuedLoaderRequests.contains(this)) {
				queuedLoaderRequests.remove(this);
			}
			if (mDrawableDownloaderTask != null) {
				mDrawableDownloaderTask.cancel(true);
			}
			if (mDrawableLoaderTask != null) {
				mDrawableLoaderTask.cancel(true);
			}
		}

		private int indexOfDownloadWithDifferentURL() {
			for (LoaderRequest loaderRequest : runningLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && !loaderRequest.getUrl().equals(uri)) {
					return runningLoaderRequests.indexOf(loaderRequest);
				}
			}
			return -1;
		}

		private boolean isQueuedForDownload() {
			for (LoaderRequest loaderRequest : queuedLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && loaderRequest.getUrl().equals(uri)) {
					return true;
				}
			}
			return false;
		}

		private int indexOfQueuedDownloadWithDifferentURL() {
			for (LoaderRequest loaderRequest : queuedLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && !loaderRequest.getUrl().equals(uri)) {
					return queuedLoaderRequests.indexOf(loaderRequest);
				}
			}
			return -1;
		}

		private boolean isAnotherQueuedOrRunningWithSameUrl() {
			for (LoaderRequest loaderRequest : queuedLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				if (loaderRequest.getUrl().equals(uri)) {
					return true;
				}
			}
			for (LoaderRequest loaderRequest : runningLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				if (loaderRequest.getUrl().equals(uri)) {
					return true;
				}
			}
			return false;
		}

		private void loadDrawable(Drawable d) {
			loadDrawable(d, true);
		}

		private void loadDrawable(Drawable d, boolean animate) {
			Log.d("loadDrawable: " + d);
			ImageView imageView = getImageView();
			if (imageView != null) {
				if (loadingTaskCallback != null) {
					loadingTaskCallback.onLoaded(imageView, pos, d);
				}
			}
		}

		// called when the download starts
		@Override
		public void onDownloadStart() {
			if (loadingTaskCallback != null) {
				loadingTaskCallback.onDownloadStart();
			}
		}

		// called when the download is in progress
		@Override
		public void onProgress(int progress) {
			if (loadingTaskCallback != null) {
				loadingTaskCallback.onDownloadProgress(progress);
			}
		}

		// called when the download has completed
		@Override
		public void onDownloadComplete() {
			Log.d("onDownloadComplete: " + uri);

			runningLoaderRequests.remove(this);
			mWasDownloaded = true;

			ImageView imageView = mImageViewRef.get();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				if (!bLoadImage) {
					if (loadingTaskCallback != null) {
						loadingTaskCallback.onLoaded(imageView, pos, null);
					}
				} else {
					loadImageFromDisk(getImageView());
				}
			}

			ArrayList<LoaderRequest> duplicates = duplicateDownloads.get(uri);
			if (duplicates != null) {
				for (LoaderRequest dup : duplicates) {
					Log.d("onDownloadComplete: " + dup.uri);
					// load the image.
					if (dup != null && dup.getImageView() != null &&
							dup.getImageView().getTag(DRAWABLE_DOWNLOAD_TAG) == dup) {
						if (!dup.isLoadingImage()) {
							if (dup.getLoaderCallback() != null) {
								dup.getLoaderCallback().onLoaded(dup.getImageView(), dup.pos, null);
							}
						} else {
							dup.loadImageFromDisk(dup.getImageView());
						}
					}
				}
				duplicateDownloads.remove(uri);
			}

			if (!queuedLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedLoaderRequests.remove(0);
				d.doDownload();
			}
		}

		// called if there is an error with the download
		@Override
		public void onDownloadError() {
			Log.d("onDownloadError: " + uri);
			runningLoaderRequests.remove(this);
			ImageView imageView = mImageViewRef.get();
			mWasDownloaded = true;
			if (imageView != null) {
				//loadErrorDrawable(imageView);
			}

			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			if (!queuedLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedLoaderRequests.remove(0);
				d.doDownload();
			}
		}

		private void loadErrorDrawable(ImageView imageView) {
			if (mErrorDrawableResource == -1 && mErrorDrawable != null) {
				imageView.setImageDrawable(mErrorDrawable);
			} else if (mErrorDrawableResource != -1) {
				imageView.setImageResource(mErrorDrawableResource);
			}
		}

		// called if the download is cancelled
		@Override
		public void onDownloadCancel() {
			mIsCancelled = true;
			Log.d("onDownloadCancel: " + uri);
			runningLoaderRequests.remove(this);

			ImageView imageView = mImageViewRef.get();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			if (!queuedLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedLoaderRequests.remove(0);
				Log.d("starting DL of: " + d.getUrl());
				d.doDownload();
			}
		}

		// called if the file is not found on the file system
		@Override
		public void notFound() {
			Log.d("notFound: " + uri);
			if (mIsCancelled) return;
			ImageView imageView = getImageView();

			if (imageView == null || this != imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) return;


			if (isAnotherQueuedOrRunningWithSameUrl()) {
				if (duplicateDownloads.containsKey(uri)) {
					ArrayList<LoaderRequest> arr = duplicateDownloads.get(uri);
					arr.add(this);
					duplicateDownloads.put(uri, arr);
				} else {
					ArrayList<LoaderRequest> arr = new ArrayList<LoaderRequest>();
					arr.add(this);
					duplicateDownloads.put(uri, arr);
				}
			} else {
				// check if this imageView is being used with a different URL, if so
				// cancel the other one.
				int queuedIndex = indexOfQueuedDownloadWithDifferentURL();
				int downloadIndex = indexOfDownloadWithDifferentURL();
				while (queuedIndex != -1) {
					queuedLoaderRequests.remove(queuedIndex);
					Log.d("notFound(Removing): " + uri);
					queuedIndex = indexOfQueuedDownloadWithDifferentURL();
				}
				if (downloadIndex != -1) {
					LoaderRequest runningLoaderRequest = runningLoaderRequests.get(downloadIndex);
					ApptentiveDownloaderTask downloadTask = runningLoaderRequest.getDrawableDownloaderTask();
					if (downloadTask != null) {
						downloadTask.cancel(true);
						Log.d("notFound(Cancelling): " + uri);
					}
				}

				if (!(isBeingDownloaded() || isQueuedForDownload())) {
					if (runningLoaderRequests.size() >= maxDownloads) {
						Log.d("notFound(Queuing): " + uri);
						queuedLoaderRequests.add(this);
					} else {
						Log.d("notFound(Downloading): " + uri);
						doDownload();
					}
				}
			}
		}

		@Override
		public void loadBitmap(Bitmap b) {
			Log.d("loadBitmap: " + uri);
			bitmapMemoryCache.addObjectToCache(ImageMemoryCache.generateMemoryCacheEntryKey(uri, imageViewWidth, imageViewHeight), b);
			ImageView imageView = getImageView();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				BitmapDrawable bm = new BitmapDrawable(imageView.getResources(), b);
				loadDrawable(bm);
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			mWasDownloaded = false;
		}

		@Override
		public void onLoadError() {
			Log.d("onLoadError: " + uri);
			ImageView imageView = getImageView();
			if (imageView != null) {
				imageView.setImageDrawable(mInProgressDrawable);
			}

			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
		}

		@Override
		public void onLoadCancelled() {
			Log.d("onLoadCancelled: " + uri);
			ImageView imageView = getImageView();
			imageView.setImageDrawable(mInProgressDrawable);
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
		}
	}

	public boolean isBitmapLoaded(String memoryKey) {
		Bitmap cachedBitmap = (Bitmap) bitmapMemoryCache.getObjectFromCache(memoryKey);
		return cachedBitmap != null;
	}

	/**
	 * Clears memory cache
	 */
	public void clearMemoryCache() {
		bitmapMemoryCache.evictAll();
	}

}
