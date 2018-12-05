/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.module.messagecenter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;


import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveToastNotification;
import com.apptentive.android.sdk.module.messagecenter.view.ApptentiveNotificationToastView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


public class ApptentiveToastNotificationManager {

	private WindowManager wManager;
	private ApptentiveNotificationToastView tContainer;
	private Queue<ApptentiveToastNotification> pendingNotificationQueue;

	private static ApptentiveToastNotificationManager sApptentiveToastNotificationManager;
	private Context context;


	private boolean isProcessingQueue = false;

	private Map<Integer, ApptentiveToastNotification> codeToNotificationMap;
	private NotificationManager notificationManager = null;


	public static synchronized ApptentiveToastNotificationManager getInstance(Context activityContext, boolean updateContext) {

		if (activityContext != null) {
			if (sApptentiveToastNotificationManager == null) {
				sApptentiveToastNotificationManager = new ApptentiveToastNotificationManager(activityContext);
			} else if (updateContext) {
				sApptentiveToastNotificationManager.updateContext(activityContext);
			}
		}
		return sApptentiveToastNotificationManager;
	}

	private ApptentiveToastNotificationManager(Context context) {
		this.context = context;
		codeToNotificationMap = new HashMap<Integer, ApptentiveToastNotification>();
		pendingNotificationQueue = new LinkedList<ApptentiveToastNotification>();
		wManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void updateContext(Context context) {
		this.context = context;
		wManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	private synchronized void notifyInternal(ApptentiveToastNotification notification) {

		// Replace the existing notification with the same code
		if (codeToNotificationMap.containsKey(notification.getCode())) {
			pendingNotificationQueue.remove(codeToNotificationMap.get(notification.getCode()));
		}
		codeToNotificationMap.put(notification.getCode(), notification);
		pendingNotificationQueue.add(notification);

		if (!isProcessingQueue) {
			processNext();
		}
	}

	public synchronized void notify(int code, ApptentiveToastNotification notification) {
		notification.setCode(code);
		notifyInternal(notification);
	}


	public synchronized void remove(ApptentiveToastNotification notification) {
		remove(notification.getCode());
	}

	public void remove(int code) {
		if (codeToNotificationMap.containsKey(code)) {
			pendingNotificationQueue.remove(codeToNotificationMap.get(code));
		}
		if (tContainer != null && tContainer.getApptentiveFloatingNotification().getCode() == code) {
			startDismissalAnimation();
		}

	}


	private synchronized void processNext() {
		if (!pendingNotificationQueue.isEmpty()) {
			ApptentiveToastNotification notification = pendingNotificationQueue.poll();
			codeToNotificationMap.remove(notification.getCode());


			if (notification.getCustomView() != null || !notification.isActivateStatusBar()) {
				isProcessingQueue = true;
				showFloatingNotification(notification);
			} else {
				isProcessingQueue = false;
				notificationManager.notify(notification.getCode(),
						notification.getNotificationBuilder().setIcon(notification.getIcon()).build());
			}
		} else {
			isProcessingQueue = false;
		}
	}


	private void showFloatingNotification(ApptentiveToastNotification notification) {

		tContainer = new ApptentiveNotificationToastView(context);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.format = PixelFormat.TRANSLUCENT;
		params.gravity = Gravity.CENTER | Gravity.TOP;
		params.x = tContainer.originalLeft;
		params.y = 0;
		params.alpha = 1f;
		try {
			wManager.addView(tContainer, params);
		} catch (Exception e) {
			logException(e);
		}
		ObjectAnimator a = ObjectAnimator.ofFloat(tContainer.rootView, "translationY", -700, 0);
		a.setDuration(600);
		a.start();
		tContainer.setNotification(notification);

	}


	public void dismiss() {
		if (tContainer != null && tContainer.getParent() != null) {
			tContainer.dismiss();
		}
	}

	private void doDismiss() {
		if (tContainer.getParent() != null) {
			wManager.removeView(tContainer);
			tContainer.postDelayed(new Runnable() { // TODO: replace with DispatchQueue
				@Override
				public void run() {
					processNext();
				}
			}, 1000);
		}

	}

	public void startDismissalAnimation() {

		if (tContainer != null && tContainer.getParent() != null) {

			ObjectAnimator a = ObjectAnimator.ofFloat(tContainer.rootView, "translationY", 0, -700);
			a.setDuration(600);
			a.start();

			a.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animator) {

				}

				@Override
				public void onAnimationEnd(Animator animator) {
					doDismiss();
				}

				@Override
				public void onAnimationCancel(Animator animator) {
				}

				@Override
				public void onAnimationRepeat(Animator animator) {
				}
			});
		}

	}

	public void startDismissalAnimationAt(ApptentiveToastNotification notification) {
		if (tContainer != null && tContainer.getParent() != null &&
				tContainer.getApptentiveFloatingNotification().getCode() == notification.getCode()) {
			startDismissalAnimation();
		}
	}


	public void notifyDefaultSilently(ApptentiveToastNotification apptentiveNotification) {
		Notification notification = apptentiveNotification.getDefaultSilentNotification();
		if (notification != null) {
			notificationManager.notify(apptentiveNotification.getCode(), notification);
		}
	}


	public void cleanUp() {
		removeAllNotifications();
		if (tContainer != null && tContainer.getParent() != null) {
			wManager.removeView(tContainer);
		}
		wManager = null;
		notificationManager = null;
		sApptentiveToastNotificationManager = null;
	}


	public void removeAllNotifications() {
		pendingNotificationQueue.clear();

		if (tContainer != null && tContainer.getParent() != null) {
			startDismissalAnimation();
		}
	}
}