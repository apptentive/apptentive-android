package com.apptentive.android.sdk.module.engagement.interaction.fragment;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;

import com.apptentive.android.sdk.module.engagement.interaction.model.AppStoreRatingInteraction;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.rating.InsufficientRatingArgumentsException;

import java.util.HashMap;
import java.util.Map;

//NON UI Fragment
public class AppStoreRatingFragment extends ApptentiveBaseFragment<AppStoreRatingInteraction> {

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
		boolean showingDialog = false;
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

			if (!finalRatingProviderArgs.containsKey("package")) {
				finalRatingProviderArgs.put("package", getActivity().getPackageName());
			}
			if (!finalRatingProviderArgs.containsKey("name")) {
				finalRatingProviderArgs.put("name", appDisplayName);
			}

			ratingProvider.startRating(activity, finalRatingProviderArgs);
		} catch (ActivityNotFoundException e) {
			showingDialog = true;
			displayError(activity, errorMessage);
		} catch (InsufficientRatingArgumentsException e) {
			// TODO: Log a message to apptentive to let the developer know that their custom rating provider puked?
			showingDialog = true;
			ApptentiveLog.e(e.getMessage());
			displayError(activity, activity.getString(R.string.apptentive_rating_error));
		} finally {
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		transit();
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	private void displayError(final Activity activity, String message) {
		ApptentiveLog.e(message);
		final Context contextThemeWrapper = new ContextThemeWrapper(activity, ApptentiveInternal.getInstance().getApptentiveTheme());
		final AlertDialog alertDialog = new AlertDialog.Builder(contextThemeWrapper).create();
		alertDialog.setTitle(activity.getString(R.string.apptentive_oops));
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.apptentive_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				alertDialog.dismiss();
			}
		});
		alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				transit();
			}
		});
		alertDialog.show();
	}
}