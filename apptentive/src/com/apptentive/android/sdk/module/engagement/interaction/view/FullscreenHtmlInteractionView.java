/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.FullscreenHtmlInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;

/**
 * @author Sky Kelsey
 */
public class FullscreenHtmlInteractionView extends InteractionView<FullscreenHtmlInteraction> {
	public FullscreenHtmlInteractionView(FullscreenHtmlInteraction interaction) {
		super(interaction);

	}

	@Override
	public void show(final Activity activity) {
		super.show(activity);
		activity.setContentView(R.layout.apptentive_fullscreen_interaction);

//		EngagementModule.engageInternal(activity, interaction.getType().name(), FullscreenHtmlInteraction.EVENT_NAME_LAUNCH);

		View backButton = activity.findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.finish();
				Log.e("Dismiss.");
//				EngagementModule.engageInternal(activity, interaction.getType().name(), FullscreenHtmlInteraction.EVENT_NAME_DISMISS);
			}
		});

		String title = interaction.getTitle();
		if (title != null) {
			TextView titleView = (TextView) activity.findViewById(R.id.title);
			titleView.setText(title);
			titleView.setVisibility(View.VISIBLE);
		}

		Action action = interaction.getAction();
		if (action != null) {
			View actionButton = activity.findViewById(R.id.button);
			((TextView) actionButton.findViewById(R.id.label)).setText(action.getLabel());
			actionButton.setVisibility(View.VISIBLE);
			actionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Log.e("Action.");
				}
			});
		}

		WebView webview = (WebView) activity.findViewById(R.id.webview);
		webview.loadData(interaction.getBody(), "text/html", "UTF-8");
		webview.setBackgroundColor(Color.TRANSPARENT); // Hack to keep webview background from being colored after load.
	}

	@Override
	public void onStop() {

	}

	@Override
	public boolean onBackPressed(Activity activity) {
//		EngagementModule.engageInternal(activity, interaction.getType().name(), FullscreenHtmlInteraction.EVENT_NAME_CANCEL);
		return true;
	}
}
