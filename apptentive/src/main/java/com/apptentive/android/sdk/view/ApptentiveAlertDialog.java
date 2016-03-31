package com.apptentive.android.sdk.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;

public class ApptentiveAlertDialog extends DialogFragment {

	public static void show(Fragment hostingFragment, Bundle bundle, int requestCode) {
		FragmentTransaction ft = hostingFragment.getFragmentManager().beginTransaction();
		Fragment prev = hostingFragment.getFragmentManager().findFragmentByTag("apptentiveAlertDialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = ApptentiveAlertDialog.newInstance(hostingFragment, bundle, requestCode);
		newFragment.show(ft, "apptentiveAlertDialog");
	}

	public static ApptentiveAlertDialog newInstance(Fragment parentFragment, Bundle bundle, int requestCode) {
		ApptentiveAlertDialog fragment = new ApptentiveAlertDialog();
		fragment.setArguments(bundle);
		fragment.setTargetFragment(parentFragment, requestCode);
		return fragment;
	}

	public interface OnDismissListener {
		/**
		 * This method will be invoked when the alert dialog is dismissed.
		 */
		public void onDismissAlert();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		final Context contextThemeWrapper = new ContextThemeWrapper(getContext(), ApptentiveInternal.getInstance().getApptentiveTheme());
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
		View view = null;
		try {
			view = inflater.inflate(R.layout.apptentive_dialog_alert, null);

			TextView title = (TextView) view.findViewById(R.id.text_title);
			String titleText = bundle.getString("title");
			if (!TextUtils.isEmpty(titleText)) {
				title.setVisibility(View.VISIBLE);
				title.setText(titleText);
			}

			TextView message = (TextView) view.findViewById(R.id.text_message);
			message.setText(bundle.getString("message"));

			Button positiveButton = (Button) view.findViewById(R.id.button_positive);
			String positiveButtonTxt = bundle.getString("positive");
			if (TextUtils.isEmpty(positiveButtonTxt)) {
				positiveButton.setVisibility(View.GONE);
			} else {
				positiveButton.setText(positiveButtonTxt);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
					}
				});
			}

			Button negativeButton = (Button) view.findViewById(R.id.button_negative);
			String negativeButtonTxt = bundle.getString("negative");
			if (TextUtils.isEmpty(negativeButtonTxt)) {
				negativeButton.setVisibility(View.GONE);
			} else {
				negativeButton.setText(negativeButtonTxt);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
					}
				});
			}
		} catch (Exception e) {
			ApptentiveLog.e("Error:", e);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (view != null) {
			builder.setView(view);
		}
		return builder.create();
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		final Fragment hostingFragment = getTargetFragment();
		if (hostingFragment instanceof ApptentiveAlertDialog.OnDismissListener) {
			((ApptentiveAlertDialog.OnDismissListener) hostingFragment).onDismissAlert();
		}
	}


}