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

	private static final String KEY_LAST = "last"; // The last time this codepoint was seen.
	private static final String KEY_VERSION = "version";
	private static final String KEY_BUILD = "build";

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
		int build = Util.getAppVersionCode(context);
		storeCodePoint(context, name, version, build);
	}

	public static synchronized void storeCodePoint(Context context, String name, String version, int build) {
		String buildString = String.valueOf(build);
		CodePointStore store = getInstance(context);
		if (store != null && name != null && version != null) {
			try {
				// Get or create code point object.
				JSONObject codePointJson = null;
				if (!store.isNull(name)) {
					codePointJson = store.getJSONObject(name);
				} else {
					codePointJson = new JSONObject();
					store.put(name, codePointJson);
				}

				// Set the last time this code point was seen to the current time.
				codePointJson.put(KEY_LAST, Util.getCurrentTime());

				// Get or create version object.
				JSONObject versionJson = null;
				if (!codePointJson.isNull(KEY_VERSION)) {
					versionJson = codePointJson.getJSONObject(KEY_VERSION);
				} else {
					versionJson = new JSONObject();
					codePointJson.put(KEY_VERSION, versionJson);
				}

				// Set count for current version.
				int existingVersionCount = 0;
				if (!versionJson.isNull(version)) {
					existingVersionCount = versionJson.getInt(version);
				}
				versionJson.put(version, existingVersionCount + 1);

				// Get or create build object.
				JSONObject buildJson = null;
				if (!codePointJson.isNull(KEY_BUILD)) {
					buildJson = codePointJson.getJSONObject(KEY_BUILD);
				} else {
					buildJson = new JSONObject();
					codePointJson.put(KEY_BUILD, buildJson);
				}

				// Set count for the current build
				int existingBuildCount = 0;
				if (!buildJson.isNull(buildString)) {
					existingBuildCount = buildJson.getInt(buildString);
				}
				buildJson.put(buildString, existingBuildCount + 1);

				save(context);
			} catch (JSONException e) {
				Log.w("Unable to store code point %s.", e, name);
			}
		}
	}

	// TODO: Methods for retrieving metrics for a given code point.

	public static void printDebug(Context context) {
		Log.e("CodePointStore:  %s", getInstance(context).toString());
	}
}
