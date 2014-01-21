package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;

/**
 * @author Sky Kelsey
 */
public class UpgradeMessageInteractionView extends InteractionView<UpgradeMessageInteraction> {
	public UpgradeMessageInteractionView(UpgradeMessageInteraction interaction) {
		super(interaction);
	}

	@Override
	public void show(Activity activity) {
		Log.e("Showing view.");
		super.show(activity);
		ViewGroup container = (ViewGroup) activity.findViewById(R.id.interaction_content);
		LayoutInflater inflater = activity.getLayoutInflater();
		View root = inflater.inflate(R.layout.apptentive_upgrade_message_interaction_content, container);

		ImageView iconView = (ImageView) root.findViewById(R.id.icon);
		Drawable icon = getIconDrawableResourceId(activity);
		if (icon != null) {
			iconView.setImageDrawable(icon);
		} else {
			iconView.setVisibility(View.GONE);
		}
		WebView webview = (WebView) root.findViewById(R.id.webview);
		webview.loadData(interaction.getBody(), "text/html", "UTF-8");
		webview.setBackgroundColor(Color.TRANSPARENT); // Hack to keep webview background from being colored after load.
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onBackPressed() {
	}

	private Drawable getIconDrawableResourceId(Activity activity) {
		try {
			PackageManager pm = activity.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(activity.getPackageName(), 0);
			return activity.getResources().getDrawable(pi.applicationInfo.icon);
		} catch (Exception e) {
			Log.e("Error loading app icon.", e);
		}
		return null;
	}
}
