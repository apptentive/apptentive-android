package com.apptentive.android.sdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * All public methods altering code point values should be synchronized.
 *
 * @author Sky Kelsey
 */
public class CodePointStore extends JSONObject {

	private static final String KEY_TOTAL = "total";
	private static final String KEY_LAST_OCCURED = "last_occured";

	private static CodePointStore instance;

	private static CodePointStore getInstance(Context context) {
		if (instance == null) {
			instance = load(context);
		}
		return instance;
	}

	private CodePointStore() {
		super();
	}

	private CodePointStore(String json) throws JSONException {
		super(json);
	}

	private static void save(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_CODE_POINT_STORE, instance.toString()).commit();
	}

	private static CodePointStore load(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		return CodePointStore.load(prefs);
	}

	private static CodePointStore load(SharedPreferences prefs) {
		String json = prefs.getString(Constants.PREF_KEY_CODE_POINT_STORE, null);
		try {
			if (json != null) {
				return new CodePointStore(json);
			}
		} catch (JSONException e) {
			Log.e("Error loading CodePointStore from SharedPreferences.", e);
		}
		return new CodePointStore();
	}

	public static synchronized void storeCodePointForCurrentAppVersion(Context context, String name) {
		String version = Util.getAppVersionName(context);
		storeCodePoint(context, name, version);
	}

	public static synchronized void storeCodePoint(Context context, String name, String version) {
		CodePointStore store = getInstance(context);
		if (store != null && name != null && version != null) {
			try {
				// If the code point name is unknown, create it.
				JSONObject codePointsForName = null;
				if (store.isNull(name)) {
					codePointsForName = new JSONObject();
					store.put(name, codePointsForName);
				}
				codePointsForName = store.getJSONObject(name);

				// If the code point version is unknown, create it.
				JSONObject codePointsForNameAndVersion = null;
				if (codePointsForName.isNull(version)) {
					codePointsForNameAndVersion = new JSONObject();
					codePointsForName.put(version, codePointsForNameAndVersion);
				}
				codePointsForNameAndVersion = codePointsForName.getJSONObject(version);

				// Set the actual information for this code point. Total and last update.
				int total = 0;
				if (!codePointsForNameAndVersion.isNull(KEY_TOTAL)) {
					total = codePointsForNameAndVersion.getInt(KEY_TOTAL);
				}
				codePointsForNameAndVersion.put(KEY_TOTAL, total + 1);

				codePointsForNameAndVersion.put(KEY_LAST_OCCURED, Util.getCurrentTime());
				save(context);
				// TODO: Test this out.
			} catch (JSONException e) {
				Log.w("Unable to store code point %s.", e, name);
			}
		}
	}

	// TODO: Methods for retrieving metrics for a given code point.
}
