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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentHostCallback;
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
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.apptentive.android.sdk.debug.Assert.assertNotNull;

public abstract class ApptentiveBaseFragment<T extends Interaction> extends DialogFragment implements InteractionManager.InteractionUpdateListener {

	protected static final String EVENT_NAME_LAUNCH = "launch";
	private static final String HAS_LAUNCHED = "has_launched";

	private final String fragmentName = getClass().getSimpleName();

	/* Nested Fragment with ChildFragmentManager lost state in rev20/rev21 of Android support library
	 * The following are needed to work around this issue
	 */
	private FragmentManager retainedChildFragmentManager;
	private Class fragmentImplClass;
	private Field hostField;

	private int toolbarLayoutId = 0;
	private Toolbar toolbar = null;
	private List fragmentMenuItems = null;
	private boolean isChangingConfigurations;
	private boolean bShownAsModal;

	protected T interaction;
	protected boolean hasLaunched;
	protected String sectionTitle;

	private Conversation conversation;
	private OnFragmentTransitionListener onTransitionListener;

	public interface OnFragmentTransitionListener {
		void onFragmentTransition(ApptentiveBaseFragment currentFragment);
	}

	{

		// Prepare the reflections to manage hidden fields
		try {
			fragmentImplClass = Class.forName("android.support.v4.app.FragmentManagerImpl");
			hostField = fragmentImplClass.getDeclaredField("mHost");
			hostField.setAccessible(true);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("FragmentManagerImpl is renamed due to the " +
					"change of Android SDK, this workaround doesn't work any more. " +
					"See the issue at " +
					"https://code.google.com/p/android/issues/detail?id=74222", e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("FragmentManagerImpl.mHost is found due to the " +
					"change of Android SDK, this workaround doesn't work any more. " +
					"See the issue at " +
					"https://code.google.com/p/android/issues/detail?id=74222", e);
		}
	}

	//use the retained childFragmentManager after the rotation.
	public FragmentManager getRetainedChildFragmentManager() {
		if (retainedChildFragmentManager == null) {
			retainedChildFragmentManager = getChildFragmentManager();
		}
		return retainedChildFragmentManager;
	}

	public String getFragmentName() {
		return fragmentName;
	}

	public boolean isChangingConfigurations() {
		// When true, it indicates the fragment was destroyed then re-created again (as result of change in orientation )
		return isChangingConfigurations;
	}

	public Context getContext() {
		Context context = super.getContext();
		return context != null ? context : ApptentiveInternal.getInstance().getApplicationContext();
	}

	public void setOnTransitionListener(OnFragmentTransitionListener onTransitionListener) {
		this.onTransitionListener = onTransitionListener;
	}

	public void transit() {
		if (onTransitionListener != null) {
			onTransitionListener.onFragmentTransition(this);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_LAUNCHED, hasLaunched);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (retainedChildFragmentManager != null) {
			//Use the retained child fragment manager after rotation
			try {
				//Set the retained ChildFragmentManager to the field
				Field field = Fragment.class.getDeclaredField("mChildFragmentManager");
				field.setAccessible(true);
				field.set(this, retainedChildFragmentManager);

				updateHosts(getFragmentManager(), (FragmentHostCallback) hostField.get(getFragmentManager()));
			} catch (Exception e) {
				ApptentiveLog.w(e, e.getMessage());
			}
		} else {
			//If the child fragment manager has not been retained yet
			retainedChildFragmentManager = getChildFragmentManager();
		}

	}

	private void updateHosts(FragmentManager fragmentManager, FragmentHostCallback currentHost) throws IllegalAccessException {
		if (fragmentManager != null) {
			replaceFragmentManagerHost(fragmentManager, currentHost);
		}

		//replace host(activity) of fragments already added
		List<Fragment> fragments = fragmentManager.getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (fragment != null) {
					try {
						//Copy the mHost(Activity) to retainedChildFragmentManager
						Field mHostField = Fragment.class.getDeclaredField("mHost");
						mHostField.setAccessible(true);
						mHostField.set(fragment, currentHost);
					} catch (Exception e) {
						ApptentiveLog.w(e, e.getMessage());
					}
					if (fragment.getChildFragmentManager() != null) {
						updateHosts(fragment.getChildFragmentManager(), currentHost);
					}
				}
			}
		}
	}

	private void replaceFragmentManagerHost(FragmentManager fragmentManager, FragmentHostCallback currentHost) throws IllegalAccessException {
		if (currentHost != null) {
			hostField.set(fragmentManager, currentHost);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();

		if (bundle != null) {
			toolbarLayoutId = bundle.getInt(Constants.FragmentConfigKeys.TOOLBAR_ID);
			bShownAsModal = bundle.getBoolean(Constants.FragmentConfigKeys.MODAL, false);
			String interactionString = bundle.getString("interaction");
			if (!TextUtils.isEmpty(interactionString)) {
				interaction = (T) Interaction.Factory.parseInteraction(interactionString);
			}
		}

		if (interaction != null) {
			// Set the title for modal Interactions for TalkBack support here. Fullscreen Interactions will set title in the ViewPager when they page into view.
			if (bShownAsModal) {
				getActivity().setTitle(interaction.getTitle());
			} else {
				sectionTitle = interaction.getTitle();
			}
		}

		if (toolbarLayoutId != 0 && getMenuResourceId() != 0) {
			setHasOptionsMenu(true);
		}

		if (savedInstanceState != null) {
			hasLaunched = savedInstanceState.getBoolean(HAS_LAUNCHED);
		}
		if (!hasLaunched) {
			hasLaunched = true;
			sendLaunchEvent(getActivity());
		}
	}

	/**
	 * Override this in cases where the behavior needs to be different. For instance, the About screen doesn't have an interaction.
	 *
	 * @param activity The launching Activity
	 */
	protected void sendLaunchEvent(Activity activity) {
		if (interaction != null) {
			engageInternal(EVENT_NAME_LAUNCH);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (toolbarLayoutId != 0) {
			toolbar = (Toolbar) getActivity().findViewById(toolbarLayoutId);
			if (getMenuResourceId() != 0 && toolbar != null) {
				Menu parentMenu = toolbar.getMenu();
				ArrayList parentMenuItems = new ArrayList();
        // Before creating fragment, host activity may already has menu items on toolbar
				for (int fragmentMenu = 0; fragmentMenu < parentMenu.size(); ++fragmentMenu) {
					parentMenuItems.add(Integer.valueOf(parentMenu.getItem(fragmentMenu).getItemId()));
				}
        // Add to toolbar menu items and menu listeners created this fragment
				toolbar.inflateMenu(getMenuResourceId());
				attachFragmentMenuListeners(toolbar.getMenu());

				//
				Menu combinedMenu = toolbar.getMenu();
				fragmentMenuItems = new ArrayList();

				int colorControlNormal = Util.getThemeColor(ApptentiveInternal.getInstance().getApptentiveToolbarTheme(), R.attr.colorControlNormal);
				for (int i = 0; i < combinedMenu.size(); ++i) {
					int menuItemId = combinedMenu.getItem(i).getItemId();
          // fragmentMenuItems contains new menu items added by this fragment
					if (!parentMenuItems.contains(Integer.valueOf(menuItemId))) {
						fragmentMenuItems.add(Integer.valueOf(menuItemId));
						Drawable drawable = combinedMenu.getItem(i).getIcon();
						if(drawable != null) {
							drawable.mutate();
							drawable.setColorFilter(colorControlNormal, PorterDuff.Mode.SRC_ATOP);
						}
					}
				}
			}
		}

	}


	@Override
	public void onResume() {
		ApptentiveInternal.getInstance().addInteractionUpdateListener(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		ApptentiveInternal.getInstance().removeInteractionUpdateListener(this);
		super.onPause();
	}

	@Override
	public void onInteractionUpdated(boolean successful) {
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Build.VERSION.SDK_INT >= 11 && getActivity() != null) {
			isChangingConfigurations = getActivity().isChangingConfigurations();
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (toolbar != null && fragmentMenuItems != null) {
			Menu toolbarMenu = toolbar.getMenu();
			Iterator it = fragmentMenuItems.iterator();
      // Remove menu items added by this fragment
			while (it.hasNext()) {
				Integer menuItem = (Integer) it.next();

				toolbarMenu.removeItem(menuItem.intValue());
			}
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(getMenuResourceId(), menu);
		// Make menu icon color same as toolbar up arrow. Both use ?colorControlNormal
		// By default colorControlNormal has same value as textColorPrimary defined in toolbar theme overlay
		int colorControlNormal = Util.getThemeColor(ApptentiveInternal.getInstance().getApptentiveToolbarTheme(), R.attr.colorControlNormal);
		for(int i = 0; i < menu.size(); i++){
			Drawable drawable = menu.getItem(i).getIcon();
			if(drawable != null) {
				drawable.mutate();
				drawable.setColorFilter(colorControlNormal, PorterDuff.Mode.SRC_ATOP);
			}
		}
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

	// If return 0, toolbar use up arrow as default navigation icon
	public int getToolbarNavigationIconResourceId(Resources.Theme activityTheme) {
		return 0;
	}

	public String getToolbarNavigationContentDescription() {
		return null;
	}

	protected void attachFragmentMenuListeners(Menu menu) {
	}

	protected void updateMenuVisibility() {

	}

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

	public boolean isShownAsModalDialog() {
		Bundle bundle = getArguments();

		if (bundle != null) {
			return bundle.getBoolean(Constants.FragmentConfigKeys.MODAL, false);
		}
		return false;
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

	/**
	 * Delegates the hardware or software back button press to the Interaction Fragment.
	 * @return
	 */
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
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

	protected String exitTypeToDataJson(ApptentiveViewExitType exitType) {
		return exitType.isShouldAddToEngage() ? StringUtils.asJson("cause", exitType.getName()) : null;
	}

	@TargetApi(21)
	private void showToolbarElevationLollipop(boolean visible) {
		if (!isAdded()) {
			return;
		}
		if (toolbar != null) {
			if (visible) {
				toolbar.setElevation(Util.dipsToPixels(getContext(), 4.0F));
			} else {
				toolbar.setElevation(0.0F);
			}
		} else {
			ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

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
		if (!isAdded()) {
			return;
		}

		FrameLayout pager = (FrameLayout) getActivity().findViewById(R.id.apptentive_vp_container);

		if (pager != null) {
			if (visible) {
				Drawable shadow = ContextCompat.getDrawable(getContext(), R.drawable.apptentive_actionbar_compat_shadow);
				pager.setForeground(shadow);
			} else {
				pager.setForeground(new ColorDrawable(0));
			}
		}

	}

	public static void replaceFragment(FragmentManager fragmentManager, int fragmentContainerId, Fragment fragment, String tag, String parentFragment, boolean showAnimation, boolean executePendingTransactions) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (showAnimation) {
			fragmentTransaction.setCustomAnimations(R.anim.apptentive_slide_right_in, R.anim.apptentive_slide_left_out, R.anim.apptentive_slide_left_in, R.anim.apptentive_slide_right_out);
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

	//region Helpers

	public boolean engage(String vendor, String interaction, String interactionId, String eventName, String data, Map<String, Object> customData, ExtendedData... extendedData) {
		Conversation conversation = getConversation();
		assertNotNull(conversation, "Attempted to engage '%s' event without an active conversation", eventName);
		return conversation != null && EngagementModule.engage(getActivity(), conversation, vendor, interaction, interactionId, eventName, data, customData, extendedData);
	}

	public boolean engageInternal(String eventName) {
		return engageInternal(eventName, null);
	}

	public boolean engageInternal(String eventName, String data) {
		Conversation conversation = getConversation();
		assertNotNull(conversation, "Attempted to engage '%s' event without an active conversation", eventName);
		return conversation != null && EngagementModule.engageInternal(getActivity(), conversation, interaction, eventName, data);
	}

	protected Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	//endregion

}
