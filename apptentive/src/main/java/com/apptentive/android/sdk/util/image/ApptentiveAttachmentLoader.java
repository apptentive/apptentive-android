/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.cache.ImageMemoryCache;
import com.apptentive.android.sdk.util.task.ApptentiveDownloaderTask;
import com.apptentive.android.sdk.util.task.ApptentiveDrawableLoaderTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.RejectedExecutionException;

import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;

public class ApptentiveAttachmentLoader {

	public static final int DRAWABLE_DOWNLOAD_TAG = R.id.apptentive_drawable_downloader;

	private ImageMemoryCache bitmapMemoryCache;
	private ArrayList<LoaderRequest> queuedDownLoaderRequests;
	private ArrayList<LoaderRequest> runningDownLoaderRequests;
	private HashSet<String> filesBeingDownloaded;
	private HashMap<String, ArrayList<LoaderRequest>> duplicateDownloads;
	private int maxDownloads;


	public static interface LoaderCallback {
		void onLoaded(ImageView view, int pos, Bitmap d);

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
		setup(10);
	}

	protected ApptentiveAttachmentLoader(int maxDownloads) {
		setup(maxDownloads);
	}

	private void setup(int maxDownloads) {
		queuedDownLoaderRequests = new ArrayList<LoaderRequest>();
		runningDownLoaderRequests = new ArrayList<LoaderRequest>();
		filesBeingDownloaded = new HashSet<String>();
		this.maxDownloads = maxDownloads;
		duplicateDownloads = new HashMap<String, ArrayList<LoaderRequest>>();
		bitmapMemoryCache = new ImageMemoryCache(30);
	}

	/* Check if a file is being downloaded. If true, the file is not completely written by download task yet.
	*  This method is to be used with File.exists() to make sure file can only be viewed after fully downloaded
	*/
	public boolean isFileCompletelyDownloaded(String path) {
		return !filesBeingDownloaded.contains(path);
	}

	public void load(String conversationToken, String uri, String diskFilePath, int pos, ImageView imageView, int width, int height, boolean bLoadImage, LoaderCallback callback) {
		LoaderRequest d = new LoaderRequest(conversationToken, uri, diskFilePath, pos, imageView, width, height, bLoadImage, callback);
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
		filesBeingDownloaded.clear();
	}

	public class LoaderRequest implements ApptentiveDownloaderTask.FileDownloadListener, ApptentiveDrawableLoaderTask.BitmapLoadListener {
		private final String conversationToken;
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

		public LoaderRequest(String conversationToken, String url, String diskPath, int position, ImageView imageView, int width, int height, boolean bLoadImage, LoaderCallback loadingTaskCallback) {
			if (conversationToken == null) {
				throw new IllegalArgumentException("Conversation token is null");
			}
			this.conversationToken = conversationToken;
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
				ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader load requested:" + uri);
				ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader load requested on:" + imageView.toString() );

				// Handle the duplicate requests on the same grid item view
				LoaderRequest oldLoaderRequest = (LoaderRequest) imageView.getTag(DRAWABLE_DOWNLOAD_TAG);
				if (oldLoaderRequest != null) {
					// If old request on the same view also loads from the same source, cancel the current one
					if (oldLoaderRequest.getUrl().equals(uri)) {
						ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader load new request denied:" + uri );
						return;
					}
					// If old request on the same view loads from different source, cancel the old one
					oldLoaderRequest.cancel();
				}

				if (TextUtils.isEmpty(uri)) {
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader loadDrawable(clear)");
					loadDrawable(null);
					imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
					return;
				}

				Bitmap cachedBitmap = (bLoadImage) ? (Bitmap) bitmapMemoryCache.getObjectFromCache(ImageMemoryCache.generateMemoryCacheEntryKey(uri, imageViewWidth, imageViewHeight)) :
						null;
				if (cachedBitmap != null) {
					mWasDownloaded = false;

					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader loadDrawable(found in cache)");
					loadDrawable(cachedBitmap);
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
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader doDownload: " + uri);
					// Conversation token is needed if the download url is a redirect link from an Apptentive endpoint
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mDrawableDownloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri, diskCacheFilePath, conversationToken);
					} else {
						mDrawableDownloaderTask.execute(uri, diskCacheFilePath, conversationToken);
					}
				} catch (RejectedExecutionException e) {
				}
				runningDownLoaderRequests.add(this);
				filesBeingDownloaded.add(diskCacheFilePath);
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
				ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader loadImageFromDisk: " + uri);
				mDrawableLoaderTask = new ApptentiveDrawableLoaderTask(imageView, this);
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
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader cancel requested for: " + uri);
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

		private void loadDrawable(Bitmap d) {
			loadDrawable(d, true);
		}

		private void loadDrawable(Bitmap d, boolean animate) {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader loadDrawable");
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
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadStarted");
			ImageView imageView = getImageView();
			if (imageView != null) {
				if (loadingTaskCallback != null) {
					loadingTaskCallback.onDownloadStart();
				}
			}
		}

		// called when the download is in progress
		@Override
		public void onProgress(int progress) {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onProgress: " + progress);
			ImageView imageView = getImageView();
			if (imageView != null) {
				if (loadingTaskCallback != null) {
					loadingTaskCallback.onDownloadProgress(progress);
				}
			}

			ArrayList<LoaderRequest> duplicates = duplicateDownloads.get(uri);
			if (duplicates != null) {
				for (LoaderRequest dup : duplicates) {
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onProgress (dup): " + progress);
					// update the progress on the duplicate downloads
					if (dup != null && dup.getImageView() != null &&
							dup.getImageView().getTag(DRAWABLE_DOWNLOAD_TAG) == dup) {
						dup.getLoaderCallback().onDownloadProgress(progress);
					}
				}
			}
		}

		// called when the download has completed
		@Override
		public void onDownloadComplete() {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadComplete: " + uri);

			runningDownLoaderRequests.remove(this);
			filesBeingDownloaded.remove(diskCacheFilePath);
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
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadComplete (dup): " + dup.uri);
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
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadError: " + uri);
			runningDownLoaderRequests.remove(this);
			filesBeingDownloaded.remove(diskCacheFilePath);
			ImageView imageView = getImageView();
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
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadError (dup): " + dup.uri);
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

		// called if the download is cancelled
		@Override
		public void onDownloadCancel() {
			mIsCancelled = true;
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onDownloadCancel: " + uri);
			runningDownLoaderRequests.remove(this);
			filesBeingDownloaded.remove(diskCacheFilePath);

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
				ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader starting DL of: " + d.getUrl());
				d.doDownload();
			}
		}

		// called if the file is not found on the file system
		@Override
		public void notFound() {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader notFound: " + uri);
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
					ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader notFound(Removing): " + uri);
					queuedIndex = indexOfQueuedDownloadWithDifferentURL();
				}
				if (downloadIndex != -1) {
					LoaderRequest runningLoaderRequest = runningDownLoaderRequests.get(downloadIndex);
					ApptentiveDownloaderTask downloadTask = runningLoaderRequest.getDrawableDownloaderTask();
					if (downloadTask != null) {
						downloadTask.cancel(true);
						ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader notFound(Cancelling): " + uri);
					}
				}

				if (!(isBeingDownloaded() || isQueuedForDownload())) {
					if (runningDownLoaderRequests.size() >= maxDownloads) {
						ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader notFound(Queuing): " + uri);
						queuedDownLoaderRequests.add(this);
					} else {
						ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader notFound(Downloading): " + uri);
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
				ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader loadDrawable(add to cache)");
				loadDrawable(b);
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);
			}
			mWasDownloaded = false;
		}

		@Override
		public void onLoadError() {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onLoadError: " + uri);
			ImageView imageView = getImageView();

			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);


				if (loadingTaskCallback != null) {
					loadingTaskCallback.onLoadTerminated();
				}
			}
		}

		@Override
		public void onLoadCancelled() {
			ApptentiveLog.v(UTIL, "ApptentiveAttachmentLoader onLoadCancelled: " + uri);
			ImageView imageView = getImageView();
			if (imageView != null && this == imageView.getTag(DRAWABLE_DOWNLOAD_TAG)) {
				imageView.setTag(DRAWABLE_DOWNLOAD_TAG, null);

				if (loadingTaskCallback != null) {
					loadingTaskCallback.onLoadTerminated();
				}
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
