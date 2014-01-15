package com.apptentive.android.sdk.module.engagement.interaction.model;

import android.app.Activity;
import org.json.JSONException;

/**
 * @author Sky Kelsey
 */
public class RatingDialogInteraction extends Interaction {

	public RatingDialogInteraction(String json) throws JSONException {
		super(json);
	}

	@Override
	public boolean run(Activity activity) {
		return false;
	}
}
