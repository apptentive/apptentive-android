/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.ImageUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollapsibleTextView extends LinearLayout implements View.OnClickListener {

	/** default text show max lines */
	private static final int DEFAULT_MAX_LINE_COUNT = 2;

	private static final int COLLAPSIBLE_STATE_NONE = 0;
	private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;
	private static final int COLLAPSIBLE_STATE_SPREAD = 2;

	private TextView desc;
	private TextView descOp;

	private String shrinkup;
	private String spread;
	private int mState;
	private boolean flag;
	private Context context;

	public CollapsibleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		shrinkup = context.getString(R.string.apptentive_message_less);
		spread = context.getString(R.string.apptentive_message_more);
		View view = inflate(context, R.layout.apptentive_message_center_collapsible_textview, this);
		view.setPadding(0, -1, 0, 0);
		desc = (TextView) view.findViewById(R.id.apptentive_text_message_body);
		descOp = (TextView) view.findViewById(R.id.apptentive_text_message_more_less);
		descOp.setOnClickListener(this);
	}

	public CollapsibleTextView(Context context) {
		this(context, null);
	}

	public final void setDesc(String str) {
		SpannableString ss = new SpannableString(str);
		Pattern p=Pattern.compile("apptentive-file-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
		Matcher m=p.matcher(str);
		while(m.find()){
			ImageSpan span = new ImageSpan(context, ImageUtil.resizeImageForImageView(context, m.group()));
			ss.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		desc.setText(ss);
		mState = COLLAPSIBLE_STATE_SPREAD;
		flag = false;
		requestLayout();
	}

	@Override
	public void onClick(View v) {
		flag = false;
		requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (!flag) {
			flag = true;
			if (desc.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
				mState = COLLAPSIBLE_STATE_NONE;
				descOp.setVisibility(View.GONE);
				desc.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
			} else {
				post(new InnerRunnable());
			}
		}
	}

	class InnerRunnable implements Runnable {
		@Override
		public void run() {
			if (mState == COLLAPSIBLE_STATE_SPREAD) {
				desc.setMaxLines(DEFAULT_MAX_LINE_COUNT);
				descOp.setVisibility(View.VISIBLE);
				descOp.setText(spread);
				mState = COLLAPSIBLE_STATE_SHRINKUP;
			} else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
				desc.setMaxLines(Integer.MAX_VALUE);
				descOp.setVisibility(View.VISIBLE);
				descOp.setText(shrinkup);
				mState = COLLAPSIBLE_STATE_SPREAD;
			}
		}
	}
}