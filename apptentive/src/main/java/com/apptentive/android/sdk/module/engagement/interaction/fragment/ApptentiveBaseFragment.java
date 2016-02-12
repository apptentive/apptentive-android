/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ApptentiveBaseFragment extends DialogFragment {


	private final String fragmentName = getClass().getSimpleName();
	private FragmentManager retainedChildFragmentManager;
	private int toolbarLayoutId = 0;
	private Toolbar toolbar = null;
	private List fragmentMenuItems = null;
	private boolean isChangingConfigurations;
	private boolean bShownAsModel;
	protected String sectionTitle;


	public FragmentManager getRetainedChildFragmentManager() {
		if (retainedChildFragmentManager == null) {
			retainedChildFragmentManager = getChildFragmentManager();
		}

		return retainedChildFragmentManager;
	}

	public boolean isChangingConfigurations() {
		// When true, it indicates the fragment was destroyed then re-created again (as result of change in orientation )
		return isChangingConfigurations;
	}

	public Context getContext() {
		Context context = super.getContext();
		return context != null ? context : ApptentiveInternal.getApplicationContext();
	}

	public void onAttach(Context context) {
		super.onAttach(context);
		if (ApptentiveInternal.getApplicationContext() == null) {
			ApptentiveInternal.setApplicationContext(context.getApplicationContext());
		}

		if (retainedChildFragmentManager != null) {
			try {
				Field e = Fragment.class.getDeclaredField("mChildFragmentManager");
				e.setAccessible(true);
				e.set(this, this.retainedChildFragmentManager);
			} catch (NoSuchFieldException nosuchfieldexception) {
				Log.d("NoSuchFieldException", nosuchfieldexception);
			} catch (IllegalAccessException illegalaccessexception) {
				Log.d("IllegalAccessException", illegalaccessexception);
			}
		}

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();

		if (bundle != null) {
			toolbarLayoutId = bundle.getInt(Constants.FragmentConfigKeys.TOOLBAR_ID);
			bShownAsModel = bundle.getBoolean(Constants.FragmentConfigKeys.MODAL, false);
		}

		if (toolbarLayoutId != 0 && getMenuResourceId() != 0) {
			setHasOptionsMenu(true);
		}
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (toolbarLayoutId != 0) {
			toolbar = (Toolbar) this.getActivity().findViewById(toolbarLayoutId);
			if (getMenuResourceId() != 0) {
				Menu parentMenu = toolbar.getMenu();
				ArrayList parentMenuItems = new ArrayList();

				for (int fragmentMenu = 0; fragmentMenu < parentMenu.size(); ++fragmentMenu) {
					parentMenuItems.add(Integer.valueOf(parentMenu.getItem(fragmentMenu).getItemId()));
				}

				toolbar.inflateMenu(getMenuResourceId());
				attachFragmentMenuListeners(toolbar.getMenu());
				Menu menu = toolbar.getMenu();

				fragmentMenuItems = new ArrayList();

				for (int i = 0; i < menu.size(); ++i) {
					int menuItemId = menu.getItem(i).getItemId();

					if (!parentMenuItems.contains(Integer.valueOf(menuItemId))) {
						fragmentMenuItems.add(Integer.valueOf(menuItemId));
					}
				}
			}
		}

		if (bShownAsModel) {
				setStatusBarColor(ApptentiveInternal.statusBarColorDefault);
		}
	}

	public void onStop() {
		super.onStop();
		if (Build.VERSION.SDK_INT >= 11) {
			isChangingConfigurations = getActivity().isChangingConfigurations();
		}

	}

	public void onDestroyView() {
		super.onDestroyView();
		if (toolbar != null && fragmentMenuItems != null) {
			Menu toolbarMenu = toolbar.getMenu();
			Iterator it = fragmentMenuItems.iterator();

			while (it.hasNext()) {
				Integer menuItem = (Integer) it.next();

				toolbarMenu.removeItem(menuItem.intValue());
			}
		}

	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(this.getMenuResourceId(), menu);
		attachFragmentMenuListeners(menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	protected Bundle getBundle() {
		Bundle bundle = new Bundle();

		if (toolbarLayoutId != 0) {
			bundle.putInt(Constants.FragmentConfigKeys.TOOLBAR_ID, toolbarLayoutId);
		}

		return bundle;
	}

	protected int getMenuResourceId() {
		return 0;
	}

	protected void attachFragmentMenuListeners(Menu menu) {}

	public Activity getActivity(Fragment fragment) {
		if (fragment == null) {
			return null;
		} else {
			while (fragment.getParentFragment() != null) {
				fragment = fragment.getParentFragment();
			}

			return fragment.getActivity();
		}
	}

	public boolean isShownAsModelDialog() {
		return bShownAsModel;
	}

	public String getTitle() {
		return sectionTitle;
	}

	public void showToolbarElevation(boolean visible) {
		if (Build.VERSION.SDK_INT >= 21) {
			showToolbarElevationLollipop(visible);
		} else {
			showToolbarElevationPreLollipop(visible);
		}

	}

	public boolean onBackPressed() {
		List fragments = getRetainedChildFragmentManager().getFragments();

		if (fragments != null) {
			Iterator it = fragments.iterator();

			while (it.hasNext()) {
				Fragment fragment = (Fragment) it.next();

				if (fragment != null && fragment.isVisible()) {
					FragmentManager childFragmentManager = fragment.getChildFragmentManager();

					if (childFragmentManager.getBackStackEntryCount() > 0) {
						childFragmentManager.popBackStack();
						return true;
					}
				}
			}
		}

		return false;
	}

	@TargetApi(21)
	private void showToolbarElevationLollipop(boolean visible) {
		if (toolbar != null) {
			if (visible) {
				toolbar.setElevation(Util.dipsToPixels(getContext(), 4.0F));
			} else {
				toolbar.setElevation(0.0F);
			}
		} else {
			ActionBar actionBar = ((AppCompatActivity) getActivity(this)).getSupportActionBar();

			if (actionBar != null) {
				if (visible) {
					actionBar.setElevation(Util.dipsToPixels(getContext(), 4.0F));
				} else {
					actionBar.setElevation(0.0F);
				}
			}
		}

	}

	private void showToolbarElevationPreLollipop(boolean visible) {
		FrameLayout pager = (FrameLayout) getActivity().findViewById(R.id.apptentive_vp_container);

		if (pager != null) {
			if (visible) {
				Drawable shadow = getResources().getDrawable(R.drawable.apptentive_actionbar_compat_shadow);

				pager.setForeground(shadow);
			} else {
				pager.setForeground(new ColorDrawable(0));
			}
		}

	}

	public static void replaceFragment(FragmentManager fragmentManager, int fragmentContainerId, Fragment fragment, String tag, String parentFragment, boolean showAnimation, boolean executePendingTransactions) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (showAnimation) {
			fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
		}

		fragmentTransaction.replace(fragmentContainerId, fragment, tag);
		if (!TextUtils.isEmpty(parentFragment)) {
			fragmentTransaction.addToBackStack(parentFragment);
		}

		fragmentTransaction.commit();
		if (executePendingTransactions) {
			fragmentManager.executePendingTransactions();
		}

	}

	public static void popBackStack(FragmentManager fragmentManager, String backStackName) {
		fragmentManager.popBackStack(backStackName, 1);
	}

	public static void popBackStackImmediate(FragmentManager fragmentManager, String backStackName) {
		fragmentManager.popBackStackImmediate(backStackName, 1);
	}

	public static void removeFragment(FragmentManager fragmentManager, Fragment fragment) {
		fragmentManager.beginTransaction().remove(fragment).commit();
	}

	/* Set status bar color when dialog style model interactions, such as Rating prompt, Note .. are shown.
	 * It is the default status color alpha blended with the Apptentive translucent
	* color apptentive_activity_frame
	* @param statusBarDefaultColor the default activity status bar color specified by the app
	*/
	private void setStatusBarColor(int statusBarDefaultColor) {
		// Changing status bar color is a post-21 feature
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Resources.Theme newTheme = getResources().newTheme();
			newTheme.applyStyle(R.style.ApptentiveThemeOverride, true);
			TypedArray a = newTheme.obtainStyledAttributes(new int[]{android.R.attr.statusBarColor});
			int statusBarColorOveride;
			try {
				// Use android:statusBarColor specified in ApptentiveThemeOverride
				statusBarColorOveride = a.getColor(0, statusBarDefaultColor);
			} finally {
				a.recycle();
			}

			int overlayColor = ContextCompat.getColor(getContext(), R.color.apptentive_activity_frame);
			getActivity().getWindow().setStatusBarColor(Util.alphaMixColors(statusBarColorOveride, overlayColor));
		}
	}
}
