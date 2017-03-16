/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomData extends HashMap<String, Object> implements Saveable {

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

	@Override
	public void onDeserialize() {
	}

	//endregion


	//region Saving when modified
	@Override
	public Object put(String key, Object value) {
		Object ret = super.put(key, value);
		notifyDataChanged();
		return ret;
	}

	@Override
	public void putAll(Map<? extends String, ?> m) {
		super.putAll(m);
		notifyDataChanged();
	}

	@Override
	public Object remove(Object key) {
		Object ret = super.remove(key);
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
		} catch (JSONException e) {
			// This can't happen.
		}
		return null;
	}
}
