package com.apptentive.android.sdk.widget;

/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;

public class ApptentiveMessageCenterBadge extends RelativeLayout {

	private LayoutInflater mInflater;

	private UnreadMessagesListener unreadMessagesListener;

	/**
	 * Constant representing normal size {@code 14dp}. Value: 0x0
	 */
	public static final int SIZE_NORMAL = 0;

	/**
	 * Constant representing mini size {@code 8dp}. Value: 0x1
	 */
	public static final int SIZE_MINI = 1;

	private int mSize;
	private int mTextColor;
	private int mCounter;

	private boolean mUpdateLocked;

	private ImageView iv;
	private TextView tv;

	/**
	 * Gets abstract size of this badge.
	 *
	 * @return {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
	 */
	public int getSize() {
		return mSize;
	}

	/**
	 * Sets abstract size for this badge.
	 * <p/>
	 * Xml attribute: {@code apptentive:badgeSize}
	 *
	 * @param size {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
	 */
	public void setSize(int size) {
		boolean changed = mSize != size;

		mSize = size;

		if (changed) {
			updateEntireBadge();
		}
	}

	/**
	 * Gets counter text color of the badge.
	 *
	 * @return color
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * Sets counter text color of the badge .
	 * <p/>
	 * Xml attribute: {@code apptentive:counterColor}
	 * <p/>
	 * NOTE: this method sets the <code>mColorStateList</code> field to <code>null</code>
	 *
	 * @param color color
	 */
	public void setTextColor(int color) {
		boolean changed = mTextColor != color;

		mTextColor = color;


		if (changed) {
			updateEntireBadge();
		}
	}

	/**
	 * Gets unread counter of this badge.
	 *
	 * @return count
	 */
	public int getCounter() {
		return mCounter;
	}

	/**
	 * Sets unread counter for this badge.
	 *
	 *
	 * @param newCount
	 */
	public void setCounter(int newCount) {
		boolean changed = mCounter != newCount;

		mCounter = newCount;

		if (changed) {
			updateBadgeCounter();
		}
	}

	public void lockUpdate() {
		mUpdateLocked = true;
	}

	public void unlockUpdate() {
		mUpdateLocked = false;
	}


	public ApptentiveMessageCenterBadge(Context context) {
		super(context);
		mInflater = LayoutInflater.from(context);
		init(context, null, 0);

	}

	public ApptentiveMessageCenterBadge(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		init(context, attrs, defStyle);
	}

	public ApptentiveMessageCenterBadge(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
		init(context, attrs, R.attr.apptentiveMessageCenterBadgeStyle);
	}

	public void init(final Context context, AttributeSet attrs, int defStyle) {
		final View v = mInflater.inflate(R.layout.apptentive_widget_message_center_badge, this, true);
		iv = (ImageView) v.findViewById(R.id.message_center_badge_icon);
    tv = (TextView) v.findViewById(R.id.message_center_badge_counter);

		unreadMessagesListener = new UnreadMessagesListener() {
			public void onUnreadMessageCountChanged(final int unreadMessages) {
				setCounter(unreadMessages);
			}
		};

		Apptentive.addUnreadMessagesListener(unreadMessagesListener);
		TypedArray a = null;
		try {
			if (attrs != null) {
				Resources.Theme theme = context.getTheme();
				if (theme != null) {
					a = theme.obtainStyledAttributes(attrs, R.styleable.ApptentiveMessageCenterBadge, defStyle, R.style.ApptentiveMessageCenterBadge);
					if (a == null) {
						return;
					}
				}
			}
		} finally {
			mSize = SIZE_NORMAL;
			mTextColor = Color.BLACK;
			mCounter = MessageManager.getUnreadMessageCount(context);
		}

		try {
			lockUpdate();
			setSize(a.getInteger(R.styleable.ApptentiveMessageCenterBadge_badgeSize, SIZE_NORMAL));
			setTextColor(a.getColor(R.styleable.ApptentiveMessageCenterBadge_counterColor, Color.BLACK));
		} finally {
			unlockUpdate();
			a.recycle();
		}
		updateEntireBadge();
	}

	private void updateEntireBadge() {
		if (!mUpdateLocked) {
			if (mSize == SIZE_NORMAL) {
				tv.setTextSize(getResources().getDimension(R.dimen.apptentive_message_center_badge_normal));
			} else {
				tv.setTextSize(getResources().getDimension(R.dimen.apptentive_message_center_badge_mini));
			}

			tv.setTextColor(mTextColor);
			updateBadgeCounter();
		}
	}

	private void updateBadgeCounter() {
		if (!mUpdateLocked) {
			tv.setText(Integer.toString(mCounter));
			invalidate();
		}
	}

}
