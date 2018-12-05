/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ImageItem implements Parcelable{
	public String originalPath;
	public String localCachePath;
	public String mimeType;
	public long time;

	private static final String KEY_ORIGINAL = "original";
	private static final String KEY_LOCAL = "local_path";
	private static final String KEY_MIME = "mimeType";
	private static final String KEY_TIME = "time";


	public ImageItem(String originalPath, String path, String type, long time) {
		this.originalPath = originalPath;
		this.localCachePath = path;
		this.time = time;
		this.mimeType = type;
	}

	public ImageItem(JSONObject json) throws JSONException {
		this.originalPath = json.optString(KEY_ORIGINAL);
		this.localCachePath = json.optString(KEY_LOCAL);
		this.mimeType = json.optString(KEY_MIME);
		this.time = json.optLong(KEY_TIME, 0);
	}

	public JSONObject toJSON() {
		JSONObject json = null;
		try {
			json = new JSONObject();
			json.put(KEY_ORIGINAL, this.originalPath);
			json.put(KEY_LOCAL, this.localCachePath);
			json.put(KEY_MIME, this.mimeType);
			json.put(KEY_TIME, this.time);
		} catch (JSONException e) {
			logException(e);
		}
		return  json;
	}

	@Override
	public boolean equals(Object o) {
		try {
			ImageItem other = (ImageItem) o;
			return this.originalPath.equals(other.originalPath);
		} catch (ClassCastException e) {
			logException(e);
		}
		return super.equals(o);
	}

	// Parcelling part
	private ImageItem(Parcel in){
		String[] data = new String[4];

		in.readStringArray(data);
		this.originalPath = data[0];
		this.localCachePath = data[1];
		this.mimeType = data[2];
		this.time = Long.valueOf(data[3]);
	}

	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] {this.originalPath,
				this.localCachePath,
				this.mimeType,
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