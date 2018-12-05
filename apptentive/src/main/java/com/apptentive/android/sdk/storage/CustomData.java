/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.ApptentiveLog;

import org.json.JSONException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class CustomData extends HashMap<String, Serializable> implements Saveable {

	private static final long serialVersionUID = 1L;

	//region Listeners
	private transient DataChangedListener listener;

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	//endregion


	//region Saving when modified
	@Override
	public Serializable put(String key, Serializable value) {
		Serializable ret = super.put(key, value);
		notifyDataChanged();
		return ret;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Serializable> m) {
		super.putAll(m);
		notifyDataChanged();
	}

	@Override
	public Serializable remove(Object key) {
		Serializable ret = super.remove(key);
		notifyDataChanged();
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		notifyDataChanged();
	}
	//endregion

	public com.apptentive.android.sdk.model.CustomData toJson() {
		try {
			com.apptentive.android.sdk.model.CustomData ret = new com.apptentive.android.sdk.model.CustomData();
			Set<String> keys = keySet();
			for (String key : keys) {
				ret.put(key, get(key));
			}
			return ret;
		} catch (JSONException e) {
			ApptentiveLog.e(e, "Exception while creating custom data");
			logException(e);
		}
		return null;
	}
}
