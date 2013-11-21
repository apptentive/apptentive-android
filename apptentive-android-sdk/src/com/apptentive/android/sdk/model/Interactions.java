package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class Interactions extends JSONObject {
	private static final String KEY_INTERACTIONS = "interactions";

	public Interactions(String json) throws JSONException {
		super(json);
	}

	public List<Interaction> getInteractions(String codePoint) {
		List<Interaction> ret = new ArrayList<Interaction>();
		try {
			JSONObject interactions = getJSONObject(KEY_INTERACTIONS);
			if(!interactions.isNull(codePoint)) {
				JSONArray interactionsForCodePoint = interactions.getJSONArray(codePoint);
				for (int i = 0; i < interactionsForCodePoint.length(); i++) {
					Interaction interaction = new Interaction(interactionsForCodePoint.getJSONObject(i).toString());
					ret.add(interaction);
				}
			}
		} catch (JSONException e) {
			Log.w("Exception parsing interactions array.", e);
		}
		return ret;
	}

}
