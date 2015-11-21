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
import android.webkit.URLUtil;
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
	private ArrayList<LoaderRequest> queuedDownLoaderRequests;
	private ArrayList<LoaderRequest> runningDownLoaderRequests;
	private HashMap<String, ArrayList<LoaderRequest>> duplicateDownloads;
	private int maxDownloads;
	private Drawable mErrorDrawable;
	private Drawable mInProgressDrawable;
	private int mErrorDrawableResource;
	private int mInProgressDrawableResource;

	public static interface LoaderCallback {
		void onLoaded(ImageView view, int pos, Drawable d);

		void onLoadTerminated();

		void onDownloadStart();

		void onDownloadProgress(int progress);

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
		queuedDownLoaderRequests = new ArrayList<LoaderRequest>();
		runningDownLoaderRequests = new ArrayList<LoaderRequest>();
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
		queuedDownLoaderRequests.clear();
		for (LoaderRequest loaderRequest : runningDownLoaderRequests) {
			ApptentiveDownloaderTask task = loaderRequest.getDrawableDownloaderTask();
			if (task != null) {
				task.cancel(true);
			}
		}
		runningDownLoaderRequests.clear();
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

		public int getPosition() {
			return pos;
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
				Log.d("ApptentiveAttachmentLoader load requested:" + uri );
				Log.d("ApptentiveAttachmentLoader load requested on:" + imageView.toString() );
				// find the old download, cancel it and set this download as the current
				// download for the imageview
				LoaderRequest oldLoaderRequest = (LoaderRequest) imageView.getTag(DRAWABLE_DOWNLOAD_TAG);
				if (oldLoaderRequest != null) {
					oldLoaderRequest.cancel();
				}

				if (TextUtils.isEmpty(uri)) {
					loadDrawable(null);
					imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
					return;
				}

				Bitmap cachedBitmap = (bLoadImage) ? (Bitmap) bitmapMemoryCache.getObjectFromCache(ImageMemoryCache.generateMemoryCacheEntryKey(uri, imageViewWidth, imageViewHeight)) :
						null;
				if (cachedBitmap != null) {
					mWasDownloaded = false;
					BitmapDrawable bm = new BitmapDrawable(imageView.getResources(), cachedBitmap);
					Log.d("ApptentiveAttachmentLoader loadDrawable(found in cache)");
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
				if (!queuedDownLoaderRequests.isEmpty() && runningDownLoaderRequests.size() < maxDownloads) {
					LoaderRequest d = queuedDownLoaderRequests.remove(0);
					d.doDownload();
				}
				return;
			}
			ImageView imageView = mImageViewRef.get();
			if (imageView != null && imageView.getTag(DRAWABLE_DOWNLOAD_TAG) == this && URLUtil.isNetworkUrl(uri)) {
				mDrawableDownloaderTask = new ApptentiveDownloaderTask(imageView, this);
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mDrawableDownloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, diskCacheFilePath);
					} else {
						mDrawableDownloaderTask.execute(uri, diskCacheFilePath);
					}
				} catch (RejectedExecutionException e) {
				}
				Log.d("ApptentiveAttachmentLoader doDownload: " + uri);
				runningDownLoaderRequests.add(this);
			}
		}

		private boolean isBeingDownloaded() {
			for (LoaderRequest loaderRequest : runningDownLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if ((otherImageView.equals(thisImageView) || loaderRequest.getPosition() == getPosition())
						&& loaderRequest.getUrl().equals(uri)) {
					return true;
				}
			}
			return false;
		}

		@SuppressLint("NewApi")
		private void loadImageFromDisk(ImageView imageView) {
			if (imageView != null && !mIsCancelled) {
				Log.d("ApptentiveAttachmentLoader loadImageFromDisk: " + uri);
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
			Log.d("ApptentiveAttachmentLoader cancel requested for: " + uri);
			mIsCancelled = true;

			ArrayList<LoaderRequest> duplicates = duplicateDownloads.get(uri);
			if (duplicates != null) {
				duplicates.remove(this);
				if (duplicates.size() > 0) {
					duplicateDownloads.put(uri, duplicates);
				} else {
					duplicateDownloads.remove(uri);
				}
			}


			if (queuedDownLoaderRequests.contains(this)) {
				queuedDownLoaderRequests.remove(this);
			}
			if (mDrawableDownloaderTask != null) {
				mDrawableDownloaderTask.cancel(true);
			}
			if (mDrawableLoaderTask != null) {
				mDrawableLoaderTask.cancel(true);
			}
		}

		private int indexOfDownloadWithDifferentURL() {
			for (LoaderRequest loaderRequest : runningDownLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && !loaderRequest.getUrl().equals(uri)) {
					return runningDownLoaderRequests.indexOf(loaderRequest);
				}
			}
			return -1;
		}

		private boolean isQueuedForDownload() {
			for (LoaderRequest loaderRequest : queuedDownLoaderRequests) {
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
			for (LoaderRequest loaderRequest : queuedDownLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				ImageView otherImageView = loaderRequest.getImageView();
				ImageView thisImageView = getImageView();
				if (thisImageView == null || otherImageView == null) {
					continue;
				}
				if (otherImageView.equals(thisImageView) && !loaderRequest.getUrl().equals(uri)) {
					return queuedDownLoaderRequests.indexOf(loaderRequest);
				}
			}
			return -1;
		}

		private LoaderRequest isAnotherQueuedOrRunningWithSameUrl() {
			for (LoaderRequest loaderRequest : queuedDownLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				if (loaderRequest.getUrl().equals(uri)) {
					return loaderRequest;
				}
			}
			for (LoaderRequest loaderRequest : runningDownLoaderRequests) {
				if (loaderRequest == null) {
					continue;
				}
				if (loaderRequest.getUrl().equals(uri)) {
					return loaderRequest;
				}
			}
			return null;
		}

		private void loadDrawable(Drawable d) {
			loadDrawable(d, true);
		}

		private void loadDrawable(Drawable d, boolean animate) {
			Log.d("ApptentiveAttachmentLoader loadDrawable");
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
			Log.d("ApptentiveAttachmentLoader onProgress: " + progress);
			if (loadingTaskCallback != null) {
				loadingTaskCallback.onDownloadProgress(progress);
			}
		}

		// called when the download has completed
		@Override
		public void onDownloadComplete() {
			Log.d("ApptentiveAttachmentLoader onDownloadComplete: " + uri);

			runningDownLoaderRequests.remove(this);
			mWasDownloaded = true;

			ImageView imageView = mImageViewRef.get();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				if (!bLoadImage) {
					imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
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
					Log.d("ApptentiveAttachmentLoader onDownloadComplete (dup): " + dup.uri);
					// load the image.
					if (dup != null && dup.getImageView() != null &&
							dup.getImageView().getTag(DRAWABLE_DOWNLOAD_TAG) == dup) {
						if (!dup.isLoadingImage()) {
							dup.getImageView().setTag(DRAWABLE_DOWNLOAD_TAG, null);
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

			if (!queuedDownLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedDownLoaderRequests.remove(0);
				d.doDownload();
			}
		}

		// called if there is an error with the download
		@Override
		public void onDownloadError() {
			Log.d("ApptentiveAttachmentLoader onDownloadError: " + uri);
			runningDownLoaderRequests.remove(this);
			ImageView imageView = mImageViewRef.get();
			mWasDownloaded = true;
			if (imageView != null) {
				if (loadingTaskCallback != null) {
					loadingTaskCallback.onDownloadProgress(-1);
				}
			}

			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}

			ArrayList<LoaderRequest> duplicates = duplicateDownloads.get(uri);
			if (duplicates != null) {
				duplicates.remove(this);
				if (duplicates.size() > 0) {
					duplicateDownloads.put(uri, duplicates);
				} else {
					duplicateDownloads.remove(uri);
				}
				for (LoaderRequest dup : duplicates) {
					Log.d("ApptentiveAttachmentLoader onDownloadError (dup): " + dup.uri);
					// load the image.
					if (dup != null && dup.getImageView() != null &&
							dup.getImageView().getTag(DRAWABLE_DOWNLOAD_TAG) == dup) {
						duplicates.remove(0);
						if (duplicates.size() > 0) {
							duplicateDownloads.put(uri, duplicates);
						} else {
							duplicateDownloads.remove(uri);
						}
						dup.doDownload();
						return;
					}
				}
			}

			if (!queuedDownLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedDownLoaderRequests.remove(0);
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
			Log.d("ApptentiveAttachmentLoader onDownloadCancel: " + uri);
			runningDownLoaderRequests.remove(this);

			ImageView imageView = mImageViewRef.get();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}

			ArrayList<LoaderRequest> duplicates = duplicateDownloads.get(uri);
			if (duplicates != null) {
				duplicates.remove(this);
				if (duplicates.size() > 0) {
					duplicateDownloads.put(uri, duplicates);
				} else {
					duplicateDownloads.remove(uri);
				}
				for (LoaderRequest dup : duplicates) {
					// start next download task in the duplicate queue
					if (dup != null && dup.getImageView() != null &&
							dup.getImageView().getTag(DRAWABLE_DOWNLOAD_TAG) == dup) {
						duplicates.remove(0);
						if (duplicates.size() > 0) {
							duplicateDownloads.put(uri, duplicates);
						} else {
							duplicateDownloads.remove(uri);
						}
						dup.doDownload();
						return;
					}
				}
			}

			if (!queuedDownLoaderRequests.isEmpty()) {
				LoaderRequest d = queuedDownLoaderRequests.remove(0);
				Log.d("ApptentiveAttachmentLoader starting DL of: " + d.getUrl());
				d.doDownload();
			}
		}

		// called if the file is not found on the file system
		@Override
		public void notFound() {
			Log.d("ApptentiveAttachmentLoader notFound: " + uri);
			if (mIsCancelled) {
				return;
			}
			ImageView imageView = getImageView();

			if (imageView == null || this != imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				return;
			}


			if (isAnotherQueuedOrRunningWithSameUrl() != null) {
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
					queuedDownLoaderRequests.remove(queuedIndex);
					Log.d("ApptentiveAttachmentLoader notFound(Removing): " + uri);
					queuedIndex = indexOfQueuedDownloadWithDifferentURL();
				}
				if (downloadIndex != -1) {
					LoaderRequest runningLoaderRequest = runningDownLoaderRequests.get(downloadIndex);
					ApptentiveDownloaderTask downloadTask = runningLoaderRequest.getDrawableDownloaderTask();
					if (downloadTask != null) {
						downloadTask.cancel(true);
						Log.d("ApptentiveAttachmentLoader notFound(Cancelling): " + uri);
					}
				}

				if (!(isBeingDownloaded() || isQueuedForDownload())) {
					if (runningDownLoaderRequests.size() >= maxDownloads) {
						Log.d("ApptentiveAttachmentLoader notFound(Queuing): " + uri);
						queuedDownLoaderRequests.add(this);
					} else {
						Log.d("ApptentiveAttachmentLoader notFound(Downloading): " + uri);
						doDownload();
					}
				}
			}
		}

		@Override
		public void loadBitmap(Bitmap b) {
			bitmapMemoryCache.addObjectToCache(ImageMemoryCache.generateMemoryCacheEntryKey(uri, imageViewWidth, imageViewHeight), b);
			ImageView imageView = getImageView();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				BitmapDrawable bm = new BitmapDrawable(imageView.getResources(), b);
				Log.d("ApptentiveAttachmentLoader loadDrawable(add to cache)");
				loadDrawable(bm);
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			mWasDownloaded = false;
		}

		@Override
		public void onLoadError() {
			Log.d("ApptentiveAttachmentLoader onLoadError: " + uri);
			ImageView imageView = getImageView();
			/*if (imageView != null) {
				imageView.setImageDrawable(mInProgressDrawable);
			}*/

			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}

			if (loadingTaskCallback != null) {
				loadingTaskCallback.onLoadTerminated();
			}
		}

		@Override
		public void onLoadCancelled() {
			Log.d("ApptentiveAttachmentLoader onLoadCancelled: " + uri);
			ImageView imageView = getImageView();
			//imageView.setImageDrawable(mInProgressDrawable);
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			if (loadingTaskCallback != null) {
				loadingTaskCallback.onLoadTerminated();
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
