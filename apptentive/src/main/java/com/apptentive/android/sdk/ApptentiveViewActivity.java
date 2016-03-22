/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.apptentive.android.sdk.adapter.ApptentiveViewPagerAdapter;
import com.apptentive.android.sdk.model.FragmentFactory;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.Constants;


public class ApptentiveViewActivity extends AppCompatActivity implements ApptentiveBaseFragment.OnFragmentTransitionListener {

	private int fragmentType;

	private Toolbar toolbar;
	private ViewPager viewPager;

	private ApptentiveViewPagerAdapter viewPager_Adapter;

	private int current_tab;

	protected void onCreate(Bundle savedInstanceState) {
		ApptentiveBaseFragment newFragment = null;
		try {
			Bundle bundle = getIntent().getExtras();
			fragmentType = bundle.getInt(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.UNKNOWN);

			if (fragmentType != Constants.FragmentTypes.UNKNOWN) {
				if (fragmentType == Constants.FragmentTypes.INTERACTION ||
						fragmentType == Constants.FragmentTypes.MESSAGE_CENTER_ERROR ||
						fragmentType == Constants.FragmentTypes.ABOUT) {
					bundle.putInt("toolbarLayoutId", R.id.apptentive_toolbar);
					newFragment = FragmentFactory.createFragmentInstance(bundle);
					if (newFragment != null) {
						newFragment.setOnTransitionListener(this);
						applyApptentiveTheme(newFragment.isShownAsModelDialog());
					}
				}

				super.onCreate(savedInstanceState);

				if (newFragment == null) {
					if (fragmentType == Constants.FragmentTypes.ENGAGE_INTERNAL_EVENT) {
						String eventName = getIntent().getStringExtra(Constants.FragmentConfigKeys.EXTRA);
						if (eventName != null) {
							EngagementModule.engageInternal(this, eventName);
						}
					}
					finish();
					return;
				}

			}
		} catch (Exception e) {
			Log.e("Error creating ApptentiveViewActivity.", e);
			MetricModule.sendError(e, null, null);
		}

		setContentView(R.layout.apptentive_viewactivity);

		toolbar = (Toolbar) findViewById(R.id.apptentive_toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		//current_tab = extra.getInt(SELECTED_TAB_EXTRA_KEY, 0);
		current_tab = 0;


		addFragmentToAdapter(newFragment, newFragment.getTitle());

		// Get the ViewPager and set it's PagerAdapter so that it can display items
		viewPager = (ViewPager) findViewById(R.id.apptentive_vp);
		viewPager.setAdapter(viewPager_Adapter);


		ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
			Boolean first = true;

			@Override
			public void onPageSelected(int position) {
				ApptentiveBaseFragment currentFragment = (ApptentiveBaseFragment) viewPager_Adapter.getItem(viewPager.getCurrentItem());
				if (!currentFragment.isShownAsModelDialog()) {

					final String title = currentFragment.getTitle();
					toolbar.post(new Runnable() {
						@Override
						public void run() {
							toolbar.setVisibility(View.VISIBLE);
							toolbar.setTitle(title);
						}
					});
				} else {
					toolbar.setVisibility(View.GONE);
				}
				current_tab = position;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (first && positionOffset == 0 && positionOffsetPixels == 0) {
					onPageSelected(current_tab);
					first = false;
				}
			}

			@Override
			public void onPageScrollStateChanged(int pos) {
				// TODO Auto-generated method stub
			}
		};

		viewPager.addOnPageChangeListener(pageChangeListener);


		// Needed to prevent the window from being panned up when the keyboard is opened.
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	@Override
	public void onBackPressed() {
		ApptentiveBaseFragment currentFragment = (ApptentiveBaseFragment) viewPager_Adapter.getItem(viewPager.getCurrentItem());

		if (currentFragment != null && currentFragment.isVisible()) {
			if (currentFragment.onBackPressed()) {
				return;
			}

			FragmentManager childFragmentManager = currentFragment.getChildFragmentManager();

			if (childFragmentManager.getBackStackEntryCount() > 0) {
				childFragmentManager.popBackStack();
			}
		}

		super.onBackPressed();
		startLauncherActivityIfRoot();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.slide_down_out);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		overridePendingTransition(R.anim.slide_up_in, 0);
	}

	@Override
	public void onFragmentTransition(ApptentiveBaseFragment currentFragment) {
		if (currentFragment != null) {
			int numberOfPages = viewPager_Adapter.getCount();
			for (int i = 0; i< numberOfPages; ++i) {
				ApptentiveBaseFragment fragment = (ApptentiveBaseFragment) viewPager_Adapter.getItem(i);
				if (currentFragment == fragment) {
					if (numberOfPages == 1) {
						finish();
					} else {
						currentFragment.dismiss();
						viewPager_Adapter.removeItem(i);
						viewPager_Adapter.notifyDataSetChanged();
					}
					return;
				}
			}
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void applyApptentiveTheme(boolean isModalInteraction) {
		Resources.Theme apptentiveTheme = ApptentiveInternal.getInstance().getApptentiveTheme();
		if (isModalInteraction) {
			apptentiveTheme.applyStyle(R.style.ApptentiveBaseDialogTheme, true);
		}
		getTheme().setTo(apptentiveTheme);
	}

	private void addFragmentToAdapter(ApptentiveBaseFragment f, String title) {
		if (viewPager_Adapter == null) {
			viewPager_Adapter = new ApptentiveViewPagerAdapter(getSupportFragmentManager());
		}
		viewPager_Adapter.add(f, title);
		viewPager_Adapter.notifyDataSetChanged();
	}

	/* If Apptentive interaction activity is the only activity of a task, backing from it will
	 * automatically launch the app main activity.
	 *
	 * This is to make sure when Apptentive interaction is
	 * launched from non-activity context, such as pending intent when application is not running,service
	 * context, or applciation context, exiting fom Apptentive interaction will land on a  default app
	 * activity, instead of desktop.
	 * */
	private void startLauncherActivityIfRoot() {
		if (isTaskRoot()) {
			PackageManager packageManager = getPackageManager();
			Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
			ComponentName componentName = intent.getComponent();
			/** Backwards compatible method that will clear all activities in the stack. */
			Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
			startActivity(mainIntent);
		}
	}

}
