package com.apptentive.android.sdk.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.debug.ErrorMetrics;

import static com.apptentive.android.sdk.util.Util.guarded;

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
		View view = null;
		try {

			view = getActivity().getLayoutInflater().inflate(R.layout.apptentive_dialog_alert, null);
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
				positiveButton.setOnClickListener(guarded(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dismiss();
								// TODO
								getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
							}
						}));
			}

			Button negativeButton = (Button) view.findViewById(R.id.button_negative);
			String negativeButtonTxt = bundle.getString("negative");
			if (TextUtils.isEmpty(negativeButtonTxt)) {
				negativeButton.setVisibility(View.GONE);
			} else {
				negativeButton.setText(negativeButtonTxt);
				negativeButton.setOnClickListener(guarded(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dismiss();
								getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
							}
						}));
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Error:");
			ErrorMetrics.logException(e);
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