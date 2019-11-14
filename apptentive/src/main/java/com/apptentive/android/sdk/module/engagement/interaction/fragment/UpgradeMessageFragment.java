/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;

public class UpgradeMessageFragment extends ApptentiveBaseFragment<UpgradeMessageInteraction> {

	private static final String CODE_POINT_DISMISS = "dismiss";

	public static UpgradeMessageFragment newInstance(Bundle bundle) {
		UpgradeMessageFragment fragment = new UpgradeMessageFragment();
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.apptentive_upgrade_message_interaction, container, false);

		try {
			ImageView iconView = (ImageView) v.findViewById(R.id.icon);
			Drawable icon = getIconDrawableResourceId();
			if (icon != null) {
				iconView.setImageDrawable(icon);
			} else {
				iconView.setVisibility(View.GONE);
			}
			WebView webview = (WebView) v.findViewById(R.id.webview);
			webview.loadData(interaction.getBody(), "text/html", "UTF-8");
			webview.setBackgroundColor(Color.TRANSPARENT); // Hack to keep webview background from being colored after load.

			// If branding is not desired, turn the view off.
			final View branding = v.findViewById(R.id.apptentive_branding_view);
			if (branding != null) {
				if (!interaction.isShowPoweredBy() || Configuration.load().isHideBranding(getContext())) {
					branding.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", UpgradeMessageFragment.class.getSimpleName());
			logException(e);
		}

		return v;
	}

	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		engageInternal(CODE_POINT_DISMISS, exitTypeToDataJson(exitType));
		return false;
	}

	private Drawable getIconDrawableResourceId() {
		try {
			Context context = getContext();
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return ContextCompat.getDrawable(getContext(), pi.applicationInfo.icon);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error loading app icon.");
			logException(e);
		}
		return null;
	}
}