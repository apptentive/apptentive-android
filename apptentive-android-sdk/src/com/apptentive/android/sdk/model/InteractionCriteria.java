package com.apptentive.android.sdk.model;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.logic.Predicate;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Sky Kelsey
 */
public class InteractionCriteria extends JSONObject {

	public InteractionCriteria(String json) throws JSONException {
		super(json);
	}

	public boolean shouldRun (Context context) {
		try {
			Predicate criteria = Predicate.parse(context, null, this);
			return criteria.apply(context);
		} catch (JSONException e) {
			Log.w("Error parsing and running InteractionCriteria predicate logic.", e);
		}
		return false;
	}
}
