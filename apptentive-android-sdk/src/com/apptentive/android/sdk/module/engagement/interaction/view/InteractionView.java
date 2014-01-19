package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import com.apptentive.android.sdk.module.ActivityContent;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionView<T> extends ActivityContent {

	protected T interaction;

	public InteractionView(T interaction) {
		this.interaction = interaction;
	}

	public abstract void show(Activity activity);
}
