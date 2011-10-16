/*
 * ModelViewAdapter.java
 *
 * Created by Sky Kelsey on 2011-05-29.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.model;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.offline.Feedback;
import com.apptentive.android.sdk.offline.Payload;
import com.apptentive.android.sdk.offline.PayloadManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class FeedbackController implements Observer, ViewController{

	private ALog log = new ALog(FeedbackController.class);

	private Activity activity;
	private boolean forced;
	public FeedbackController(Activity activity, boolean forced) {
		this.activity = activity;
		this.forced = forced;
		setupForm();
	}

	private void setupForm(){
		ApptentiveModel model = ApptentiveModel.getInstance();

		EditText email = (EditText) activity.findViewById(R.id.apptentive_feedback_user_email);
		email.setText(model.getEmail());
		email.addTextChangedListener(new GenericTextWatcher(email));
		email.setOnTouchListener(new RemoveHintTextListener());

		EditText feedback = (EditText) activity.findViewById(R.id.apptentive_feedback_text);
		feedback.setText(model.getFeedback());
		feedback.addTextChangedListener(new GenericTextWatcher(feedback));
		if(forced){
			feedback.setHint("Feedback");
		}
		feedback.setOnTouchListener(new RemoveHintTextListener());

		EditText name = (EditText) activity.findViewById(R.id.apptentive_feedback_user_name);
		name.setText(model.getName());
		name.addTextChangedListener(new GenericTextWatcher(name));
		name.setOnTouchListener(new RemoveHintTextListener());

		EditText phone = (EditText) activity.findViewById(R.id.apptentive_feedback_user_phone);
		phone.setText(model.getPhone());
		phone.addTextChangedListener(new GenericTextWatcher(phone));
		phone.setOnTouchListener(new RemoveHintTextListener());

		Button cancel = (Button) activity.findViewById(R.id.apptentive_button_cancel);
		cancel.setOnClickListener(clickListener);
		Button next = (Button) activity.findViewById(R.id.apptentive_button_next);
		next.setOnClickListener(clickListener);
		if (!ApptentiveModel.getInstance().isAskForExtraInfo()) {
			next.setText("Submit");
		}

		Button back = (Button) activity.findViewById(R.id.apptentive_button_back);
		back.setOnClickListener(clickListener);

		Button submit = (Button) activity.findViewById(R.id.apptentive_button_submit);
		submit.setOnClickListener(clickListener);

		View branding = activity.findViewById(R.id.apptentive_branding_view);
		branding.setOnClickListener(clickListener);

		Button okay = (Button) activity.findViewById(R.id.apptentive_button_about_okay);
		okay.setOnClickListener(clickListener);

		model.addObserver(this);
		model.forceNotifyObservers();
	}

	private void submit() {
		ApptentiveModel model = ApptentiveModel.getInstance();

		Payload payload = new Feedback(model.getUuid(),
		                                model.getModel(),
		                                model.getVersion(),
		                                model.getCarrier(),
		                                "1.0",
		                                model.getName(),
		                                model.getEmail(),
		                                model.getFeedback(),
		                                model.getFeedbackType(),
		                                new Date()
		);
		model.clearTransientData();
		PayloadManager payloadManager = new PayloadManager(activity.getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE));
		payloadManager.save(payload);
		payloadManager.run();
	}

	public void update(Observable observable, Object o) {
		if(o instanceof ApptentiveModel){
			ApptentiveModel model = (ApptentiveModel) o;
			// Enable the next button if name was supplied.
			Button next = (Button)activity.findViewById(R.id.apptentive_button_next);
			next.setEnabled(model.getFeedback().length() != 0);
		}
	}

	private class RemoveHintTextListener implements View.OnTouchListener{
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if(view instanceof EditText){
				EditText editText = (EditText) view;
				editText.setHint("");
			}
			return false;
		}
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {

			ViewFlipper aboutFlipper = (ViewFlipper) activity.findViewById(R.id.apptentive_activity_about_flipper);
			ViewFlipper flipper = (ViewFlipper) activity.findViewById(R.id.apptentive_feedback_content_flipper);

			Util.hideSoftKeyboard(activity, view);

			switch (view.getId()) {
				case R.id.apptentive_button_cancel:
					activity.finish();
					break;
				case R.id.apptentive_button_next:
					flipper.setInAnimation(Constants.inFromRightAnimation());
					flipper.setOutAnimation(Constants.outToLeftAnimation());
					if (ApptentiveModel.getInstance().isAskForExtraInfo()) {
						flipper.showNext();
					} else {
						submit();
						activity.finish();
					}
					break;
				case R.id.apptentive_button_back:
					flipper.setInAnimation(Constants.inFromLeftAnimation());
					flipper.setOutAnimation(Constants.outToRightAnimation());
					flipper.showPrevious();
					break;
				case R.id.apptentive_button_submit:
					submit();
					activity.finish();
					break;
				case R.id.apptentive_branding_view:
					aboutFlipper.setInAnimation(Constants.inFromBottomAnimation());
					aboutFlipper.setOutAnimation(Constants.outToTopAnimation());
					aboutFlipper.showNext();
					break;
				case R.id.apptentive_button_about_okay:
					aboutFlipper.setInAnimation(Constants.inFromTopAnimation());
					aboutFlipper.setOutAnimation(Constants.outToBottomAnimation());
					aboutFlipper.showPrevious();
					break;
				default:
					break;
			}
		}
	};

	public void cleanup(){
		ApptentiveModel model = ApptentiveModel.getInstance();
		model.deleteObserver(this);
	}

	private class GenericTextWatcher implements TextWatcher{

		private View view;
		private GenericTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		public void afterTextChanged(Editable editable) {
			String text = editable.toString();
			ApptentiveModel model = ApptentiveModel.getInstance();
			
			switch(view.getId()){
				case R.id.apptentive_feedback_user_email:
					model.setEmail(text);
					break;
				case R.id.apptentive_feedback_text:
					model.setFeedback(text);
					break;
				case R.id.apptentive_feedback_user_name:
					model.setName(text);
					break;
				case R.id.apptentive_feedback_user_phone:
					model.setPhone(text);
					break;
			}
		}
	}
}
