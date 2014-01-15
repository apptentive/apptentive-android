package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class CombinationPredicate extends Predicate {

	protected Operation operation;
	protected List<Predicate> children;

	protected CombinationPredicate(Context context, Operation operation, Object object) throws JSONException {
		this.operation = operation;
		this.children = new ArrayList<Predicate>();
		if (object instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) object;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject child = (JSONObject) jsonArray.get(i);
				children.add(Predicate.parse(context, null, child));
			}
		} else if (object instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) object;
			@SuppressWarnings("unchecked")
			Iterator<String> it = (Iterator<String>) jsonObject.keys();
			while (it.hasNext()) {
				String key =  it.next();
				children.add(Predicate.parse(context, key, jsonObject.get(key)));
			}
		} else {
			Log.w("Unrecognized Combination Predicate: %s", object.toString());
		}

	}

	public boolean apply(Context context) {
		try {
			Log.v("Start: Combination Predicate: %s", operation.name());
			if (this.operation == Operation.$and) { // $and
				for (Predicate predicate : children) {
					boolean ret = predicate.apply(context);
					Log.v("=> %b", ret);
					if (!ret) {
						return false;
					}

				}
				return true;
			} else { // $or
				for (Predicate predicate : children) {
					boolean ret = predicate.apply(context);
					Log.v("=> %b", ret);
					if (ret) {
						return true;
					}
				}
				return false;
			}
		} finally {
			Log.v("End:   Combination Predicate: %s", operation.name());
		}
	}
}
