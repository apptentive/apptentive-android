package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.ActivityContent;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionView<T> extends ActivityContent {

	protected T interaction;

	public InteractionView(T interaction) {
		this.interaction = interaction;
	}

	public void show(Activity activity) {
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		activity.setContentView(R.layout.apptentive_interaction_dialog);
	}
}
