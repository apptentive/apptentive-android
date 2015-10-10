/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.net.Uri;

public class ImageItem {
	public Uri uri;
	public String name;
	public long time;

	public ImageItem(Uri uri, String name, long time) {
		this.uri = uri;
		this.name = name;
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		try {
			ImageItem other = (ImageItem) o;
			return this.uri.equals(other.uri);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return super.equals(o);
	}
}