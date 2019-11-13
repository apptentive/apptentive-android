/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;

public class ApptentiveViewPagerAdapter extends FragmentPagerAdapter {

	private List<ApptentiveBaseFragment> fragments = new ArrayList<>();
	private List<String> tabTitles = new ArrayList<>();
	private List<String> tags = new ArrayList<>();


	public ApptentiveViewPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ApptentiveBaseFragment fragment = (ApptentiveBaseFragment) super.instantiateItem(container, position);
		fragments.set(position, fragment);
		tabTitles.set(position, fragment.getTitle());
		tags.set(position, fragment.getTag());
		return getItem(position);
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabTitles.get(position);
	}

	public String getFragmentTag(int position) {
		return tags.get(position);
	}

	public void removeItem(int position) {
		if (position <= getCount()) {
			fragments.remove(position);
			tabTitles.remove(position);
			tags.remove(position);
		}
	}

	public void add(ApptentiveBaseFragment f, String title) {
		fragments.add(f);
		tabTitles.add(title);
		tags.add("");
	}


}
