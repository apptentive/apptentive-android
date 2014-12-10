/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view.common;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionButtonViewController<T extends Action> {
	protected Context context;
	protected T interactionButton;
	protected Button button;


	public InteractionButtonViewController(Context context, ViewGroup parent, int layout, T interactionButton) {
		this.context = context;
		this.interactionButton = interactionButton;

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		button = (Button) inflater.inflate(layout, parent, false);
		button.setText(interactionButton.getLabel());
		init();
	}

	public Button getButton() {
		return button;
	}

	public void setOnClickListener(View.OnClickListener listener) {
		button.setOnClickListener(listener);
	}

	protected abstract void init();
}
