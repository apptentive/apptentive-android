package com.apptentive.android.sdk.module.survey;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey.
 */
public class ReorderableChoice extends BaseChoice {

	protected ImageView listHandle;

	public ReorderableChoice(Context context) {
		super(context);
	}

	@Override
	protected void initView() {
		super.initView();
		listHandle = new ImageView(appContext);
		listHandle.setLayoutParams(Constants.ITEM_LAYOUT);
		listHandle.setImageResource(R.drawable.list_item_handle_25x20);
		listHandle.setBackgroundColor(Color.YELLOW);
		int dip5 = Util.dipsToPixels(appContext, 5);
		listHandle.setPadding(dip5, dip5, dip5, dip5);
		container.addView(listHandle);
	}
}
