/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.model;

import android.app.Notification;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import android.view.View;


public class ApptentiveToastNotification {

	private Context context;
	/**
	 * In Seconds
	 */
	private long duration = 5;
	/**
	 *
	 */
	private Notification notificationDelegate;

	private Builder builder;

	private boolean isSticky = false;

	private boolean activateStatusBar = false;

	private int code;
	private CharSequence titleStr;
	private CharSequence msgStr;
	private int iconRes;
	private String imageUrl;
	private View customView;

	private ApptentiveToastNotification(Context context) {
		this.context = context;
	}


	public static class Builder extends NotificationCompat.Builder {


		private ApptentiveToastNotification apptentiveNotification;

		public Builder(Context context) {
			super(context);
			apptentiveNotification = new ApptentiveToastNotification(context);
		}

		/**
		 * Set the first line of text in the platform notification template.
		 */
		public Builder setContentTitle(CharSequence title) {
			apptentiveNotification.setTitle(title);
			super.setContentTitle(title);
			return this;
		}

		/**
		 * Set the second line of text in the platform notification template.
		 */
		public Builder setContentText(CharSequence text) {
			apptentiveNotification.setMessage(text);
			super.setContentText(text);
			return this;
		}

		public Builder setSmallIcon(int icon) {
			apptentiveNotification.setIcon(icon);
			return this;
		}

		public Builder setIcon(int icon) {
			super.setSmallIcon(icon);
			return this;
		}


		public Builder setSticky(boolean isSticky) {
			apptentiveNotification.setSticky(isSticky);
			return this;
		}


		public ApptentiveToastNotification buildApptentiveToastNotification() {
			apptentiveNotification.setNotification(this.build());
			apptentiveNotification.setNotificationBuilder(this);
			return apptentiveNotification;
		}

		private Notification buildDefaultSilentNotification() {
			super.setSmallIcon(apptentiveNotification.getIcon());
			setDefaults(0);
			return this.build();
		}
	}

	protected void setIcon(int dRes) {
		iconRes = dRes;
	}


	protected void setTitle(CharSequence titleStr) {
		this.titleStr = titleStr;
	}


	protected void setMessage(CharSequence msgStr) {
		this.msgStr = msgStr;
	}


	public Context getContext() {
		return context;
	}

	public long getDuration() {
		return duration;
	}


	public CharSequence getTitleStr() {
		return titleStr;
	}

	public CharSequence getMsgStr() {
		return msgStr;
	}

	public int getIcon() {
		return iconRes;
	}

	public void setDuration(long interval) {
		this.duration = interval;
	}


	public Notification getNotification() {
		return notificationDelegate;
	}

	protected void setNotification(Notification notification) {
		this.notificationDelegate = notification;
	}

	public void setAvatarUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getAvatarUrl() {
		return imageUrl;
	}

	public View getCustomView() {
		return customView;
	}

	public void setCustomView(View customView) {
		this.customView = customView;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Notification getDefaultSilentNotification() {
		return getNotificationBuilder().buildDefaultSilentNotification();
	}


	public Builder getNotificationBuilder() {
		return builder;
	}

	private void setNotificationBuilder(Builder builder) {
		this.builder = builder;
	}


	public boolean isSticky() {
		return isSticky;
	}

	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}


	public boolean isActivateStatusBar() {
		return activateStatusBar;
	}

	public void setActivateStatusBar(boolean activateStatusBar) {
		this.activateStatusBar = activateStatusBar;
	}
}