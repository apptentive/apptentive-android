/*
 * BaseActivity.java
 *
 * Created by Sky Kelsey on 2011-10-09.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.activity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public abstract class BaseActivity extends Activity {

	// The top level content view.
	private ViewGroup viewGroup = null;

	@Override
	protected void onResume() {
		System.gc();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		System.gc();
	}

	@Override
	public void setContentView(int layoutResID) {
		ViewGroup mainView = (ViewGroup) LayoutInflater.from(this).inflate(layoutResID, null);

		setContentView(mainView);
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);

		viewGroup = (ViewGroup) view;
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		super.setContentView(view, params);

		viewGroup = (ViewGroup) view;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Fixes android memory  issue 8488 :
		// http://code.google.com/p/android/issues/detail?id=8488
		nullViewDrawablesRecursive(viewGroup);

		viewGroup = null;
		System.gc();
	}

	private void nullViewDrawablesRecursive(View view) {
		if (view != null) {
			try {
				ViewGroup viewGroup = (ViewGroup) view;

				int childCount = viewGroup.getChildCount();
				for (int index = 0; index < childCount; index++) {
					View child = viewGroup.getChildAt(index);
					nullViewDrawablesRecursive(child);
				}
			} catch (Exception e) {
			}

			nullViewDrawable(view);
		}
	}

	private void nullViewDrawable(View view) {
		try {
			view.setBackgroundDrawable(null);
		} catch (Exception e) {
		}

		try {
			ImageView imageView = (ImageView) view;
			imageView.setImageDrawable(null);
			imageView.setBackgroundDrawable(null);
		} catch (Exception e) {
		}
	}
}