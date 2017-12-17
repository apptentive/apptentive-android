package com.apptentive.android.sdk.widget;

/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;

public class ApptentiveMessageCenterBadge extends RelativeLayout {

	private LayoutInflater inflater;

	private UnreadMessagesListener unreadMessagesListener;

	/**
	 * Constant representing normal size {@code 14dp}. Value: 0x0
	 */
	public static final int SIZE_NORMAL = 0;

	/**
	 * Constant representing mini size {@code 8dp}. Value: 0x1
	 */
	public static final int SIZE_MINI = 1;

	private int badgeSize;
	private int badgeTextColor;
	private int unreadMsgCounter;

	private boolean bUpdateLocked;

	private ImageView iv;
	private TextView tv;

	/**
	 * Gets abstract size of this badge.
	 *
	 * @return {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
	 */
	public int getSize() {
		return badgeSize;
	}

	/**
	 * <p>Sets abstract size for this badge.</p>
	 * <p>Xml attribute: {@code apptentive:badgeSize}</p>
	 *
	 * @param size {@link #SIZE_NORMAL} or {@link #SIZE_MINI}
	 */
	public void setSize(int size) {
		boolean changed = badgeSize != size;

		badgeSize = size;

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
		return badgeTextColor;
	}

	/**
	 * <p>Sets counter text color of the badge.</p>
	 * <p>Xml attribute: {@code apptentive:counterColor}</p>
	 * NOTE: this method sets the <code>mColorStateList</code> field to <code>null</code>
	 *
	 * @param color color
	 */
	public void setTextColor(int color) {
		boolean changed = badgeTextColor != color;

		badgeTextColor = color;


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
		return unreadMsgCounter;
	}

	/**
	 * Sets unread counter for this badge.
	 *
	 *
	 * @param newCount
	 */
	public void setCounter(int newCount) {
		boolean changed = unreadMsgCounter != newCount;

		unreadMsgCounter = newCount;

		if (changed) {
			updateBadgeCounter();
		}
	}

	public void lockUpdate() {
		bUpdateLocked = true;
	}

	public void unlockUpdate() {
		bUpdateLocked = false;
	}


	public ApptentiveMessageCenterBadge(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		init(context, null, 0);

	}

	public ApptentiveMessageCenterBadge(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflater = LayoutInflater.from(context);
		init(context, attrs, defStyle);
	}

	public ApptentiveMessageCenterBadge(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		init(context, attrs, R.attr.apptentiveMessageCenterBadgeStyle);
	}

	public void init(final Context context, AttributeSet attrs, int defStyle) {
		final View v = inflater.inflate(R.layout.apptentive_widget_message_center_badge, this, true);
		iv = (ImageView) v.findViewById(R.id.message_center_badge_icon);
    tv = (TextView) v.findViewById(R.id.message_center_badge_counter);

		badgeSize = SIZE_NORMAL;
		badgeTextColor = Color.BLACK;

		if (!ApptentiveInternal.isApptentiveRegistered()) {
			v.setVisibility(GONE);
			return;
		}
		unreadMsgCounter = Apptentive.getUnreadMessageCount();

		unreadMessagesListener = new UnreadMessagesListener() {
			public void onUnreadMessageCountChanged(final int unreadMessages) {
				((Activity)getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setCounter(unreadMessages);
					}
				});
			}
		};
		Apptentive.addUnreadMessagesListener(unreadMessagesListener);

		TypedArray a;

		if (attrs == null) {
			return;
		}

		Resources.Theme theme = context.getTheme();
		if (theme == null) {
			return;
		}

		a = theme.obtainStyledAttributes(attrs, R.styleable.ApptentiveMessageCenterBadge, defStyle, 0);
		if (a == null) {
			return;
		}

		try {
			lockUpdate();
			setSize(a.getInteger(R.styleable.ApptentiveMessageCenterBadge_apptentive_badgeSize, SIZE_NORMAL));
			setTextColor(a.getColor(R.styleable.ApptentiveMessageCenterBadge_apptentive_counterColor, Color.BLACK));
		} finally {
			unlockUpdate();
			a.recycle();
		}
		updateEntireBadge();
	}

	private void updateEntireBadge() {
		if (!bUpdateLocked) {
			if (badgeSize == SIZE_NORMAL) {
				tv.setTextSize(getResources().getDimension(R.dimen.apptentive_message_center_badge_normal));
			} else {
				tv.setTextSize(getResources().getDimension(R.dimen.apptentive_message_center_badge_mini));
			}

			tv.setTextColor(badgeTextColor);
			updateBadgeCounter();
		}
	}

	private void updateBadgeCounter() {
		if (!bUpdateLocked) {
			tv.setText(Integer.toString(unreadMsgCounter));
			invalidate();
		}
	}

}
