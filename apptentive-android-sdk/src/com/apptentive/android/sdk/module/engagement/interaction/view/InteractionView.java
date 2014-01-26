package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.view.View;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionView<T extends Interaction> extends ActivityContent {

	protected T interaction;

	public InteractionView(T interaction) {
		this.interaction = interaction;
	}

	public void show(Activity activity) {
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		activity.setContentView(R.layout.apptentive_interaction_dialog);

		// If branding is not desired, turn the view off.
		if (!interaction.isShowPoweredBy()) {
			activity.findViewById(R.id.apptentive_branding_view).setVisibility(View.GONE);
		}
	}
}
