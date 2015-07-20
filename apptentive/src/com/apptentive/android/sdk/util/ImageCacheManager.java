/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;


public class ImageCacheManager {

	private static final int MAX_CACHE_COUNT = 1;

	private final LruCache<String, Object> bufferCache;

	public ImageCacheManager() {
		bufferCache = new LruCache<String, Object>(
				MAX_CACHE_COUNT) {
			/**
			 * recycle the removed bitmap from memory
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
				if (oldValue != null) {
					if (oldValue instanceof Bitmap) {
						((Bitmap)oldValue).recycle();
					} else if (oldValue instanceof BitmapDrawable) {
						((BitmapDrawable) oldValue).getBitmap().recycle();
					}
					oldValue = null;
				}
			}
		};
	}

	public ImageCacheManager(int maxMega) {
		bufferCache = new LruCache<String, Object>(maxMega * 1024 * 1024) { // by default use 5M as a limit for the in memory Lrucache
			/**
			 * recycle the removed bitmap from memory
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
				if (oldValue != null) {
					if (oldValue instanceof Bitmap) {
						((Bitmap)oldValue).recycle();
					} else if (oldValue instanceof BitmapDrawable) {
						((BitmapDrawable) oldValue).getBitmap().recycle();
					}
					oldValue = null;
				}
			}

			@Override
			protected int sizeOf(String key, Object object) {
				// The cache size will be measured in bytes rather than
				// number of items.
				int byteCount = 0;

				if (object instanceof Bitmap) {
					byteCount = ((Bitmap)object).getRowBytes() * ((Bitmap)object).getHeight();
				} else if (object instanceof BitmapDrawable) {
					Bitmap bm = ((BitmapDrawable) object).getBitmap();
					byteCount = bm.getRowBytes() * bm.getHeight();
				}
				return byteCount;
			}
		};
	}

	public Object getObjectFromCache(String key) {
		if (bufferCache == null) {
			return null;
		}
		return bufferCache.get(key);
	}

	public void addObjectToCache(String key, Object value) {
		if (key.isEmpty() || value == null) {
			return;
		}

		// Add to memory cache
		if (getObjectFromCache(key) == null) {
			bufferCache.put(key, value);
		}
	}

	public void removeObjectFromCache(String key) {
		if (key.isEmpty() ) {
			return;
		}

		// Remove from memory cache
		if (bufferCache != null) {
			bufferCache.remove(key);
		}
	}
	public void evictAll() {
		if (bufferCache != null) {
			bufferCache.evictAll();
		}
	}
}
