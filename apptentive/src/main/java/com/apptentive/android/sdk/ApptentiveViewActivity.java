/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import com.apptentive.android.sdk.adapter.ApptentiveViewPagerAdapter;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.model.FragmentFactory;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.notifications.ApptentiveNotification;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_CONVERSATION_STATE_DID_CHANGE;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_INTERACTIONS_SHOULD_DISMISS;
import static com.apptentive.android.sdk.ApptentiveNotifications.NOTIFICATION_KEY_CONVERSATION;
import static com.apptentive.android.sdk.debug.Assert.notNull;


public class ApptentiveViewActivity extends ApptentiveBaseActivity implements ApptentiveBaseFragment.OnFragmentTransitionListener {
	private static final String FRAGMENT_TAG = "fragmentTag";
	private int fragmentType;

	private Toolbar toolbar;
	private ViewPager viewPager;

	private ApptentiveViewPagerAdapter viewPager_Adapter;

	private int current_tab;

	private View decorView;
	private View contentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			if (!ApptentiveInternal.isApptentiveRegistered()) {
				ApptentiveLog.e("Apptentive instance is not properly initialized. Finishing activity...");
				finish();
				return;
			}

			dispatchOnConversationQueue(new DispatchTask() {
				@Override
				protected void execute() {
					Conversation conversation = notNull(ApptentiveInternal.getInstance().getConversation());
					if (conversation == null) {
						dispatchOnMainQueue(new DispatchTask() {
							@Override
							protected void execute() {
								finish();
							}
						});
					}
				}
			});

			if (getIntent().getExtras() == null) {
				ApptentiveLog.w("ApptentiveViewActivity was started without any extras, which isn't allowed. Finishing Activity.");
				finish();
				return;
			}

			Bundle bundle = FragmentFactory.addDisplayModeToFragmentBundle(getIntent().getExtras());
			boolean isInteractionModal = bundle.getBoolean(Constants.FragmentConfigKeys.MODAL);

			ApptentiveBaseFragment newFragment = null;
			if (savedInstanceState != null) {
				// retrieve the retained fragment after orientation change using saved tag
				String savedFragmentTag = savedInstanceState.getString(FRAGMENT_TAG);
				newFragment = (ApptentiveBaseFragment) getSupportFragmentManager().findFragmentByTag(savedFragmentTag);
				/* Since we always store tags of fragments in the ViewPager upon orientation change,
				 * failure of retrieval indicate internal error
				 */
				if (newFragment == null) {
					finish();
					return;
				}
			}
			fragmentType = bundle.getInt(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.UNKNOWN);

			if (fragmentType != Constants.FragmentTypes.UNKNOWN) {
				if (fragmentType == Constants.FragmentTypes.INTERACTION ||
					fragmentType == Constants.FragmentTypes.MESSAGE_CENTER_ERROR ||
					fragmentType == Constants.FragmentTypes.ABOUT) {
					bundle.putInt("toolbarLayoutId", R.id.apptentive_toolbar);
					if (newFragment == null) {
						newFragment = FragmentFactory.createFragmentInstance(bundle);
						isInteractionModal = newFragment.isShownAsModalDialog();
					}
					if (newFragment != null) {
						applyApptentiveTheme(isInteractionModal);
						newFragment.setOnTransitionListener(this);
					}
				}

				if (newFragment == null) {
					if (fragmentType == Constants.FragmentTypes.ENGAGE_INTERNAL_EVENT) {
						String eventName = getIntent().getStringExtra(Constants.FragmentConfigKeys.EXTRA);
						if (eventName != null) {
							engageInternal(eventName);
						}
					}
					finish();
					return;
				}
			}

			setContentView(R.layout.apptentive_viewactivity);

			toolbar = (Toolbar) findViewById(R.id.apptentive_toolbar);
			setSupportActionBar(toolbar);

			/* Add top padding by the amount of Status Bar height to avoid toolbar being covered when
			 * status bar is translucent
			 */
			toolbar.setPadding(0, getToolbarHeightAdjustment(!isInteractionModal), 0, 0);

			ActionBar actionBar = getSupportActionBar();

			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				int navIconResId = newFragment.getToolbarNavigationIconResourceId(getTheme());
				// Check if fragment may show an alternative navigation icon
				if (navIconResId != 0) {
					/* In order for the alternative icon has the same color used by toolbar icon,
					 * need to apply the same color in toolbar theme
					 * By default colorControlNormal has same value as textColorPrimary defined in toolbar theme overlay
					 */
					// Allows loading of vector drawable resources from XML
					AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
					final Drawable alternateUpArrow = ResourcesCompat.getDrawable(getResources(),
						navIconResId,
						getTheme());

					int colorControlNormal = Util.getThemeColor(ApptentiveInternal.getInstance().getApptentiveToolbarTheme(), R.attr.colorControlNormal);
					alternateUpArrow.setColorFilter(colorControlNormal, PorterDuff.Mode.SRC_ATOP);
					actionBar.setHomeAsUpIndicator(alternateUpArrow);
				}

				String contentDescription = newFragment.getToolbarNavigationContentDescription();
				if (!StringUtils.isNullOrEmpty(contentDescription)) {
					actionBar.setHomeActionContentDescription(contentDescription);
				}
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
					final ApptentiveBaseFragment currentFragment = (ApptentiveBaseFragment) viewPager_Adapter.getItem(viewPager.getCurrentItem());
					// Set the Activity title for TalkBack support
					final String title = currentFragment.getTitle();
					if (currentFragment != null && currentFragment.getActivity() != null) {
						currentFragment.getActivity().setTitle(title);
					}
					if (!currentFragment.isShownAsModalDialog()) {
						toolbar.post(new Runnable() { // TODO: replace with DispatchQueue
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
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreate(). Finishing activity...", ApptentiveViewActivity.class.getSimpleName());
			logException(e);
			finish();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				exitActivity(ApptentiveViewExitType.MENU_ITEM);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Helper to clean up the Activity, whether it is exited through the toolbar back button, or the hardware back button.
	 */
	private void exitActivity(ApptentiveViewExitType exitType) {
		try {
			exitActivityGuarded(exitType);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while trying to exit activity (type=%s)", exitType);
			logException(e);
		}
	}

	private void exitActivityGuarded(ApptentiveViewExitType exitType) {
		Util.hideSoftKeyboard(this, getCurrentFocus());

		ApptentiveBaseFragment currentFragment = (ApptentiveBaseFragment) viewPager_Adapter.getItem(viewPager.getCurrentItem());
		if (currentFragment != null && currentFragment.isVisible()) {
			if (currentFragment.onFragmentExit(exitType)) {
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

	public void onBackPressed() {
		exitActivity(ApptentiveViewExitType.BACK_BUTTON);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.apptentive_slide_down_out);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		overridePendingTransition(R.anim.apptentive_slide_up_in, 0);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save the tag of the current fragment before orientation change
		outState.putString(FRAGMENT_TAG, viewPager_Adapter.getFragmentTag(current_tab));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onFragmentTransition(ApptentiveBaseFragment currentFragment) {
		if (currentFragment != null) {
			int numberOfPages = viewPager_Adapter.getCount();
			for (int i = 0; i < numberOfPages; ++i) {
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

	private void applyApptentiveTheme(boolean isModalInteraction) {
		// Update the activity theme to reflect current attributes
		try {
			ApptentiveInternal.getInstance().updateApptentiveInteractionTheme(this, getTheme());

			if (isModalInteraction) {
				getTheme().applyStyle(R.style.ApptentiveBaseDialogTheme, true);
				setStatusBarColor();
			}

			// Change the thumbnail header color in task list
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				int colorPrimary = Util.getThemeColor(getTheme(), R.attr.colorPrimary);
				ActivityManager.TaskDescription taskDes = new ActivityManager.TaskDescription(null, null, colorPrimary);
				setTaskDescription(taskDes);
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error apply Apptentive Theme.");
			logException(e);
		}
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
	 * context, or application context, exiting fom Apptentive interaction will land on a  default app
	 * activity, instead of desktop.
	 * */
	private void startLauncherActivityIfRoot() {
		try {
			if (isTaskRoot()) {
				PackageManager packageManager = getPackageManager();
				Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
			/*
			 Make this work with Instant Apps. It is possible and even likely to create an Instant App
			 that doesn't have the Main Activity included in its APK. In such cases, this Intent is null,
			 and we can't do anything apart from exiting our Activity.
			  */
				if (intent != null) {
					ComponentName componentName = intent.getComponent();
					/* Backwards compatible method that will clear all activities in the stack. */
					Intent mainIntent = Util.makeRestartActivityTask(componentName);
					if (mainIntent != null) {
						startActivity(mainIntent);
					}
				} else {
					ApptentiveLog.w("Unable to start app's main activity: launch intent is missing");
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while starting app's main activity");
			logException(e);
		}
	}

	/* Android versions starting with API Level 19, setting translucent statusbar would have two implications:
	* 1. toolbar of non-model interaction would be partially covered
	* 2. Keyboard launch won't resize window. (Bug: https://code.google.com/p/android/issues/detail?id=63777)
	* The following method will fix both issues
	*/
	private int getToolbarHeightAdjustment(boolean bToolbarShown) {
		int adjustAmount = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			boolean translucentStatus = false;
			// check theme attrs to see if translucent statusbar is set explicitly
			int[] attrs = {android.R.attr.windowTranslucentStatus};
			TypedArray a = getTheme().obtainStyledAttributes(attrs);
			try {
				translucentStatus = a.getBoolean(0, false);
			} finally {
				a.recycle();
			}

			// also check window flags in case translucent statusbar is set implicitly
			WindowManager.LayoutParams winParams = getWindow().getAttributes();
			int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			if ((winParams.flags & bits) != 0) {
				translucentStatus = true;
			}

			if (translucentStatus) {
				if (bToolbarShown) {
					int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
					if (resourceId > 0) {
						adjustAmount = getResources().getDimensionPixelSize(resourceId);
					}
				}

				/* Add layout listener to ensure keyboard launch resize the screen when android:windowTranslucentStatus=true
				 * Fixing workaround found here:
				 * http://stackoverflow.com/questions/8398102/androidwindowsoftinputmode-adjustresize-doesnt-make-any-difference
				 */
				decorView = getWindow().getDecorView();
				contentView = decorView.findViewById(android.R.id.content);
				decorView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardPresencelLayoutListener);
			}
		}
		return adjustAmount;
	}

	ViewTreeObserver.OnGlobalLayoutListener keyboardPresencelLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			Rect r = new Rect();
			decorView.getWindowVisibleDisplayFrame(r);

			int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
			int diff = height - r.bottom;

			// Detect keyboard launch when use-able screen height differs from the total screen height
			if (diff != 0) {
				//check if the padding is 0 (if yes set the padding for the keyboard)
				if (contentView.getPaddingBottom() != diff) {
					//set the padding of the contentView for the keyboard
					contentView.setPadding(0, 0, 0, diff);
				}
			} else {
				//check if the padding is != 0 (if yes reset the padding)
				if (contentView.getPaddingBottom() != 0) {
					//reset the padding of the contentView
					contentView.setPadding(0, 0, 0, 0);
				}
			}
		}
	};

	/* Set status bar color when dialog style modal interactions, such as Rating prompt, Note .. are shown.
	 * It is the default status color alpha blended with the Apptentive translucent
	* color defined by apptentive_activity_frame
	* @param statusBarDefaultColor the default activity status bar color specified by the app
	*/
	private void setStatusBarColor() {
		// Changing status bar color is a post-21 feature
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int statusBarDefaultColor = ApptentiveInternal.getInstance().getDefaultStatusBarColor();
			int overlayColor = ContextCompat.getColor(this, R.color.apptentive_activity_frame_dark);
			getWindow().setStatusBarColor(Util.alphaMixColors(statusBarDefaultColor, overlayColor));
		}
	}

	//region ApptentiveNotificationObserver

	@Override
	public void onReceiveNotification(ApptentiveNotification notification) {
		checkConversationQueue();

		if (notification.hasName(NOTIFICATION_INTERACTIONS_SHOULD_DISMISS)) {
			dispatchOnMainQueue(new DispatchTask() {
				@Override
				protected void execute() {
					dismissActivity();
				}
			});
		} else if (notification.hasName(NOTIFICATION_CONVERSATION_STATE_DID_CHANGE)) {
			final Conversation conversation = notification.getUserInfo(NOTIFICATION_KEY_CONVERSATION, Conversation.class);
			Assert.assertNotNull(conversation, "Conversation expected to be not null");
			if (conversation != null && !conversation.hasActiveState()) {
				dispatchOnMainQueue(new DispatchTask() {
					@Override
					protected void execute() {
						dismissActivity();
					}
				});
			}
		}
	}

	//endregion

	//region Helpers

	private void dismissActivity() {
		if (!isFinishing()) {
			exitActivity(ApptentiveViewExitType.NOTIFICATION); // TODO: different exit types for different notifications?
		}
	}

	//endregion
}
