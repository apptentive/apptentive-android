package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;
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

	public List<Interaction> getInteractionList(String codePoint) {
		List<Interaction> ret = new ArrayList<Interaction>();
		try {
			JSONObject interactions = getJSONObject(KEY_INTERACTIONS);
			if(!interactions.isNull(codePoint)) {
				JSONArray interactionsForCodePoint = interactions.getJSONArray(codePoint);
				for (int i = 0; i < interactionsForCodePoint.length(); i++) {
					String interactionString = interactionsForCodePoint.getJSONObject(i).toString();
					Interaction interaction = Interaction.Factory.parseInteraction(interactionString);
					if (interaction != null) {
						ret.add(interaction);
					}
				}
			}
		} catch (JSONException e) {
			Log.w("Exception parsing interactions array.", e);
		}
		return ret;
	}
}
