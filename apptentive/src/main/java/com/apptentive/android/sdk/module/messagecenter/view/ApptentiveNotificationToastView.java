/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.module.messagecenter.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.module.messagecenter.ApptentiveToastNotificationManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveToastNotification;
import com.apptentive.android.sdk.util.image.ImageUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */

public class ApptentiveNotificationToastView extends LinearLayout {

	private View toastView;
	private float rawX = 0;
	private float rawY = 0;
	private float touchX = 0;
	private float startY = 0;
	public LinearLayout rootView;
	public int originalLeft;
	public int viewWidth;
	private float validWidth;
	private VelocityTracker velocityTracker;
	private int maxVelocity;

	private ApptentiveToastNotification apptentiveNotification;
	// Set by notification duration
	private long countDown;
	// Set by notification
	private Handler mHandle = null;

	//public static WindowManager.LayoutParams winParams = new WindowManager.LayoutParams();

	private CountDownTimer countDownTimer;


	private class CountDownTimer extends Thread {

		@Override
		public void run() {
			super.run();
			while (countDown > 0) {
				try {
					Thread.sleep(1000);
					countDown--;
				} catch (InterruptedException e) {
					e.printStackTrace();
					logException(e);
				}
			}

			if (countDown == 0) {
				mHandle.sendEmptyMessage(0);
			}
		}
	}

	private ScrollOrientationEnum scrollOrientationEnum = ScrollOrientationEnum.NONE;


	public ApptentiveNotificationToastView(final Context context) {
		super(context);
		LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.apptentive_notification_toast_container, null);
		maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
		rootView = (LinearLayout) view.findViewById(R.id.rootView);
		addView(view);
		viewWidth = context.getResources().getDisplayMetrics().widthPixels;
		validWidth = viewWidth / 2.0f;
		originalLeft = 0;
	}

	public void setCustomView(View view) {
		rootView.addView(view);
	}


	public ApptentiveToastNotification getApptentiveFloatingNotification() {
		return apptentiveNotification;
	}

	private int pointerId;

	public boolean onTouchEvent(MotionEvent event) {
		rawX = event.getRawX();
		rawY = event.getRawY();
		acquireVelocityTracker(event);
		countDown = apptentiveNotification.getDuration();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchX = event.getX();
				startY = event.getRawY();
				pointerId = event.getPointerId(0);
				rootView.setBackgroundDrawable(getResources().getDrawable(R.drawable.apptentive_toast_bg_dark_pressed));
				break;
			case MotionEvent.ACTION_MOVE:
				switch (scrollOrientationEnum) {
					case NONE:
						if (Math.abs((rawX - touchX)) > 20) {
							scrollOrientationEnum = ScrollOrientationEnum.HORIZONTAL;

						} else if (startY - rawY > 20) {
							scrollOrientationEnum = ScrollOrientationEnum.VERTICAL;

						}

						break;
					case HORIZONTAL:
						updatePosition((int) (rawX - touchX));
						break;
					case VERTICAL:
						if (startY - rawY > 20) {
							dismiss();
						}
						break;
				}

				break;
			case MotionEvent.ACTION_UP:
				toastView.setBackgroundColor(getResources().
						getColor(android.R.color.transparent));
				velocityTracker.computeCurrentVelocity(1000, maxVelocity);
				int dis = (int) velocityTracker.getYVelocity(pointerId);
				if (scrollOrientationEnum == ScrollOrientationEnum.NONE) {
					if (apptentiveNotification.getNotification().contentIntent != null) {

						try {
							apptentiveNotification.getNotification().contentIntent.send();
							dismiss();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
							logException(e);
						}
					}
					break;
				}


				int toX;
				if (preLeft > 0) {
					toX = preLeft + Math.abs(dis);
				} else {
					toX = preLeft - Math.abs(dis);
				}
				if (toX <= -validWidth) {
					float preAlpha = 1 - Math.abs(preLeft) / validWidth;
					preAlpha = preAlpha >= 0 ? preAlpha : 0;
					translationX(preLeft, -(validWidth + 10), preAlpha, 0);
				} else if (toX <= validWidth) {
					float preAlpha = 1 - Math.abs(preLeft) / validWidth;
					preAlpha = preAlpha >= 0 ? preAlpha : 0;
					translationX(preLeft, 0, preAlpha, 1);

				} else {
					float preAlpha = 1 - Math.abs(preLeft) / validWidth;
					preAlpha = preAlpha >= 0 ? preAlpha : 0;
					translationX(preLeft, validWidth + 10, preAlpha, 0);
				}
				preLeft = 0;
				scrollOrientationEnum = ScrollOrientationEnum.NONE;
				break;
		}

		return super.onTouchEvent(event);

	}

	/**
	 * @param event
	 * @see android.view.VelocityTracker#obtain()
	 * @see android.view.VelocityTracker#addMovement(MotionEvent)
	 */
	private void acquireVelocityTracker(MotionEvent event) {
		if (null == velocityTracker) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);
	}


	private int preLeft;

	public void updatePosition(int left) {

		float preAlpha = 1 - Math.abs(preLeft) / validWidth;
		float leftAlpha = 1 - Math.abs(left) / validWidth;
		preAlpha = preAlpha >= 0 ? preAlpha : 0;
		leftAlpha = leftAlpha >= 0 ? leftAlpha : 0;
		translationX(preLeft, left, preAlpha, leftAlpha);

		preLeft = left;
	}


	public void translationX(float fromX, float toX, float formAlpha, final float toAlpha) {
		ObjectAnimator a1 = ObjectAnimator.ofFloat(rootView, "alpha", formAlpha, toAlpha);
		ObjectAnimator a2 = ObjectAnimator.ofFloat(rootView, "translationX", fromX, toX);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(a1, a2);
		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (toAlpha == 0) {
					ApptentiveToastNotificationManager.getInstance(getContext(), false).startDismissalAnimation();

					countDown = -1;
					if (velocityTracker != null) {
						velocityTracker.clear();
						try {
							velocityTracker.recycle();
						} catch (IllegalStateException e) {
							logException(e);
						}
					}

				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		animatorSet.start();
	}

	public void setNotification(final ApptentiveToastNotification notification) {

		apptentiveNotification = notification;

		mHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// If user doesn't interact with the toast by "duration", it will be relayed to system notification
				if (apptentiveNotification.isActivateStatusBar()) {
					ApptentiveToastNotificationManager.getInstance(getContext(), false).notifyDefaultSilently(apptentiveNotification);
				}
				ApptentiveToastNotificationManager.getInstance(getContext(), false).startDismissalAnimationAt(apptentiveNotification);
			}
		};


		countDownTimer = new CountDownTimer();

		if (!apptentiveNotification.isSticky()) {
			countDownTimer.start();
		}


		countDown = apptentiveNotification.getDuration();

		if (apptentiveNotification.getCustomView() == null) {

			toastView = LayoutInflater.from(getContext()).inflate(R.layout.apptentive_notification_toast, rootView, false);
			rootView.addView(toastView);
			ApptentiveAvatarView imageView = (ApptentiveAvatarView) toastView.findViewById(R.id.toast_avatar);
			TextView titleTV = (TextView) toastView.findViewById(R.id.toast_title);
			TextView timeTV = (TextView) toastView.findViewById(R.id.toast_timestamp);
			TextView messageTV = (TextView) toastView.findViewById(R.id.toast_message);
			if (apptentiveNotification.getAvatarUrl() != null) {
				ImageUtil.startDownloadAvatarTask(imageView, apptentiveNotification.getAvatarUrl());
			} else {
				imageView.setImageResource(apptentiveNotification.getIcon());
			}
			titleTV.setText(apptentiveNotification.getTitleStr());
			messageTV.setText(apptentiveNotification.getMsgStr());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
			timeTV.setText(simpleDateFormat.format(new Date()));
		} else {
			setCustomView(apptentiveNotification.getCustomView());
		}

	}


	public void dismiss() {
		ApptentiveToastNotificationManager.getInstance(getContext(), false).startDismissalAnimation();
		countDown = -1;
		countDownTimer.interrupt();

		if (velocityTracker != null) {
			try {
				velocityTracker.clear();
				velocityTracker.recycle();
			} catch (IllegalStateException e) {
				logException(e);
			}
		}
	}


	enum ScrollOrientationEnum {
		VERTICAL, HORIZONTAL, NONE
	}

	private void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add more context info
	}
}