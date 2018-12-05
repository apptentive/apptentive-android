package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveViewExitType;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;

import com.apptentive.android.sdk.module.engagement.interaction.model.AppStoreRatingInteraction;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;
import com.apptentive.android.sdk.view.ApptentiveAlertDialog;

import java.util.HashMap;
import java.util.Map;

//NON UI Fragment
public class AppStoreRatingFragment extends ApptentiveBaseFragment<AppStoreRatingInteraction>
		implements ApptentiveAlertDialog.OnDismissListener {

	public static AppStoreRatingFragment newInstance(Bundle bundle) {
		AppStoreRatingFragment storeRatingFragment = new AppStoreRatingFragment();
		storeRatingFragment.setArguments(bundle);
		return storeRatingFragment;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return null;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Activity activity = getActivity();
		String errorMessage = activity.getResources().getString(R.string.apptentive_rating_error);

		try {
			IRatingProvider ratingProvider = ApptentiveInternal.getInstance().getRatingProvider();
			errorMessage = ratingProvider.activityNotFoundMessage(activity);

			String appDisplayName = Configuration.load().getAppDisplayName();
			Map<String, String> ratingProviderArgs = ApptentiveInternal.getInstance().getRatingProviderArgs();
			Map<String, String> finalRatingProviderArgs;
			if (ratingProviderArgs != null) {
				finalRatingProviderArgs = new HashMap<String, String>(ratingProviderArgs);
			} else {
				finalRatingProviderArgs = new HashMap<String, String>();
			}

			if (!finalRatingProviderArgs.containsKey("package") && activity != null) {
				finalRatingProviderArgs.put("package", activity.getPackageName());
			}
			if (!finalRatingProviderArgs.containsKey("name")) {
				finalRatingProviderArgs.put("name", appDisplayName);
			}

			ratingProvider.startRating(activity, finalRatingProviderArgs);
		} catch (ActivityNotFoundException e) {
			displayError(activity, errorMessage);
		} catch (InsufficientRatingArgumentsException e) {
			ApptentiveLog.e(e.getMessage());
			displayError(activity, activity.getString(R.string.apptentive_rating_error));
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreate()", getClass().getSimpleName());
			logException(e);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			transit();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		transit();
	}

	@Override
	public boolean onFragmentExit(ApptentiveViewExitType exitType) {
		return false;
	}

	//called when alert dialog had been dismissed
	@Override
	public void onDismissAlert() {
		transit();
	}

	private void displayError(final Activity activity, String message) {
		try {
			ApptentiveLog.e(message);
			Bundle bundle = new Bundle();
			bundle.putString("title", activity.getString(R.string.apptentive_oops));
			bundle.putString("message", message);
			bundle.putString("positive", activity.getString(R.string.apptentive_ok));
			ApptentiveAlertDialog.show(this, bundle, 0);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while displaying an error");
			logException(e);
		}
	}

	@Override
	protected void sendLaunchEvent(Activity activity) {
		// This Interaction type does not send a launch Event.
	}
}