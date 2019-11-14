/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.util.cache;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.collection.LruCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ImageMemoryCache {

	private static final String URI_DIMENTION_SEPARATOR = "_";
	private static final String WIDTH_HEIGHT_SEPARATOR = ":";

	private static final int MAX_CACHE_COUNT = 1;

	private final LruCache<String, Object> bufferCache;

	public ImageMemoryCache() {
		bufferCache = new LruCache<String, Object>(
				MAX_CACHE_COUNT) {
			/**
			 * recycle the removed bitmap from memory
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
				if (oldValue != null) {
					if (oldValue instanceof Bitmap) {
						((Bitmap) oldValue).recycle();
					} else if (oldValue instanceof BitmapDrawable) {
						((BitmapDrawable) oldValue).getBitmap().recycle();
					}
					oldValue = null;
				}
			}
		};
	}

	public ImageMemoryCache(int maxMega) {
		bufferCache = new LruCache<String, Object>(maxMega * 1024 * 1024) { // by default use 1M as a unit for the in memory Lrucache
			/**
			 * recycle the removed bitmap from memory
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, Object oldValue, Object newValue) {
				if (oldValue != null) {
					if (oldValue instanceof Bitmap) {
						((Bitmap) oldValue).recycle();
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
					byteCount = ((Bitmap) object).getRowBytes() * ((Bitmap) object).getHeight();
				} else if (object instanceof BitmapDrawable) {
					Bitmap bm = ((BitmapDrawable) object).getBitmap();
					byteCount = bm.getRowBytes() * bm.getHeight();
				}
				return byteCount;
			}
		};
	}

	public Set<String> getKeySet() {
		Map<String, Object> snapshot = bufferCache.snapshot();
		return snapshot.keySet();
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
		if (key.isEmpty()) {
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

	/**
	 * Generates key for memory cache entry
	 * Format for memory cache key is [imageUri]_[width]:[height].
	 */
	public static String generateMemoryCacheEntryKey(String imageUri, int width, int height) {
		return new StringBuilder(imageUri).append(URI_DIMENTION_SEPARATOR).append(width).append(WIDTH_HEIGHT_SEPARATOR).append(height).toString();
	}

	public static Comparator<String> createKeyComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String key1, String key2) {
				String imageUri1 = key1.substring(0, key1.lastIndexOf(URI_DIMENTION_SEPARATOR));
				String imageUri2 = key2.substring(0, key2.lastIndexOf(URI_DIMENTION_SEPARATOR));
				return imageUri1.compareTo(imageUri2);
			}
		};
	}

	/**
	 * Return all bitmaps in memory cache associated with the given image URI.<br />
	 */
	public static List<Bitmap> getCachedBitmapsForImageUri(String imageUri, ImageMemoryCache memoryCache) {
		List<Bitmap> values = new ArrayList<Bitmap>();
		for (String key : memoryCache.getKeySet()) {
			if (key.startsWith(imageUri)) {
				values.add((Bitmap) memoryCache.getObjectFromCache(key));
			}
		}
		return values;
	}

	/**
	 * Searches all keys in memory cache which are corresponded to incoming URI.<br />
	 */
	public static List<String> findCacheKeysForImageUri(String imageUri, ImageMemoryCache memoryCache) {
		List<String> values = new ArrayList<String>();
		for (String key : memoryCache.getKeySet()) {
			if (key.startsWith(imageUri)) {
				values.add(key);
			}
		}
		return values;
	}

	/**
	 * Removes from memory cache all sizes of a given image URI.<br />
	 */
	public static void removeFromCache(String imageUri, ImageMemoryCache memoryCache) {
		List<String> keysToRemove = new ArrayList<String>();
		for (String key : memoryCache.getKeySet()) {
			if (key.startsWith(imageUri)) {
				keysToRemove.add(key);
			}
		}
		for (String keyToRemove : keysToRemove) {
			memoryCache.removeObjectFromCache(keyToRemove);
		}
	}
}
