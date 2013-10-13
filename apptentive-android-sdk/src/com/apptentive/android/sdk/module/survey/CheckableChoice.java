/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.widget.CheckBox;
import com.apptentive.android.sdk.util.Constants;

/**
 * @author Sky Kelsey.
 */
public class CheckableChoice extends BaseChoice {

	protected CheckBox checkBox;

	public CheckableChoice(Context context) {
		super(context);
	}

	@Override
	protected void initView() {
		super.initView();
		checkBox = new CheckBox(appContext);
		checkBox.setClickable(false); // We want the containing view to handle clicks.
		checkBox.setLayoutParams(Constants.ITEM_LAYOUT);
		container.addView(checkBox);
	}

	public void toggle() {
		checkBox.toggle();
	}
	public boolean isChecked() {
		return checkBox.isChecked();
	}
}
