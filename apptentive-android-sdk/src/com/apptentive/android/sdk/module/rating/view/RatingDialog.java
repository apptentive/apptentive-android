package com.apptentive.android.sdk.module.rating.view;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class RatingDialog extends ApptentiveBaseDialog {

	private OnChoiceMadeListener onChoiceMadeListener;

	public RatingDialog(final Context context) {
		super(context, R.layout.apptentive_rating_dialog);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Button rateButton = (Button) findViewById(R.id.rate);
		final Button remindButton = (Button) findViewById(R.id.remind);
		final Button noButton = (Button) findViewById(R.id.no);

		rateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (onChoiceMadeListener != null) {
					onChoiceMadeListener.onRate();
				}
			}
		});

		remindButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (onChoiceMadeListener != null) {
					onChoiceMadeListener.onRemind();
				}
			}
		});

		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (onChoiceMadeListener != null) {
					onChoiceMadeListener.onNo();
				}
			}
		});
	}

	@Override
	public void setTitle(CharSequence text) {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(text);
	}

	public void setBody(CharSequence text) {
		TextView body = (TextView) findViewById(R.id.body);
		body.setText(text);
	}

	public void setRateButtonText(CharSequence text) {
		Button rateButton = (Button) findViewById(R.id.rate);
		rateButton.setText(text);
	}

	public void setOnChoiceMadeListener(OnChoiceMadeListener onChoiceMadeListener) {
		this.onChoiceMadeListener = onChoiceMadeListener;
	}

	public interface OnChoiceMadeListener {
		public void onRate();
		public void onRemind();
		public void onNo();
	}
}