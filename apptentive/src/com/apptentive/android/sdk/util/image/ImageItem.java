/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Parcelable{
	public String originalPath;
	public String localCachePath;
	public long time;

	public ImageItem(String originalPath, String path, long time) {
		this.originalPath = originalPath;
		this.localCachePath = path;
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		try {
			ImageItem other = (ImageItem) o;
			return this.originalPath.equals(other.originalPath);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return super.equals(o);
	}

	// Parcelling part
	private ImageItem(Parcel in){
		String[] data = new String[3];

		in.readStringArray(data);
		this.originalPath = data[0];
		this.localCachePath = data[1];
		this.time = Long.valueOf(data[2]);
	}

	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] {this.originalPath,
				this.localCachePath,
				Long.toString(this.time)});
	}
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public ImageItem createFromParcel(Parcel in) {
			return new ImageItem(in);
		}

		public ImageItem[] newArray(int size) {
			return new ImageItem[size];
		}
	};
}