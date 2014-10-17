/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.rating.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Sky Kelsey
 */
public abstract class ApptentiveBaseDialog extends Dialog {
	public ApptentiveBaseDialog(final Context context, int layout) {
		super(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layout); // This needs to be set before the window layout is messed with below.

		// Let the dialog take up the whole device width.
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.width = ViewGroup.LayoutParams.FILL_PARENT;
		params.height = ViewGroup.LayoutParams.FILL_PARENT;
		params.gravity = Gravity.CENTER;
		params.dimAmount = 0.5f;
		getWindow().setAttributes(params);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
}
