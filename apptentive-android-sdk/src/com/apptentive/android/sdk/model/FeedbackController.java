/*
 * ModelViewAdapter.java
 *
 * Created by Sky Kelsey on 2011-05-29.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.model;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.apptentive.android.sdk.ALog;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.comm.ApptentiveClient;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class FeedbackController implements Observer{

	static final int SCREEN_ANIMATION_DURATION = 300;
	static final Interpolator SCREEN_ANIMATION_INTERPOLATOR = new LinearInterpolator();

	private static ALog log = new ALog(FeedbackController.class);

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

		ApptentiveClient client = new ApptentiveClient(model.getApiKey());
		client.submitFeedback(model.getUuid(), model.getName(), model.getEmail(), model.getModel(), model.getVersion(), model.getCarrier(), model.getFeedback(), model.getFeedbackType(), new Date());
		model.clearTransientData();
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
			ViewFlipper flipper = (ViewFlipper) activity.findViewById(R.id.apptentive_content_flipper);
			ViewFlipper aboutFlipper = (ViewFlipper) activity.findViewById(R.id.apptentive_about_flipper);

			Apptentive.getInstance().hideSoftKeyboard(view);

			switch (view.getId()) {
				case R.id.apptentive_button_cancel:
					activity.finish();
					break;
				case R.id.apptentive_button_next:
					flipper.setInAnimation(inFromRightAnimation());
					flipper.setOutAnimation(outToLeftAnimation());
					if (ApptentiveModel.getInstance().isAskForExtraInfo()) {
						flipper.showNext();
					} else {
						submit();
						activity.finish();
					}
					break;
				case R.id.apptentive_button_back:
					flipper.setInAnimation(inFromLeftAnimation());
					flipper.setOutAnimation(outToRightAnimation());
					flipper.showPrevious();
					break;
				case R.id.apptentive_button_submit:
					submit();
					activity.finish();
					break;

				case R.id.apptentive_branding_view:
					aboutFlipper.setInAnimation(inFromBottomAnimation());
					aboutFlipper.setOutAnimation(outToTopAnimation());
					aboutFlipper.showNext();
					break;
				case R.id.apptentive_button_about_okay:
					aboutFlipper.setInAnimation(inFromTopAnimation());
					aboutFlipper.setOutAnimation(outToBottomAnimation());
					aboutFlipper.showPrevious();
					break;
				default:
					break;
			}
		}
	};

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

	private Animation inFromRightAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation outToLeftAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation inFromLeftAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation outToRightAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation inFromBottomAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation outToTopAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation inFromTopAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

	private Animation outToBottomAnimation() {
		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f);
		animation.setDuration(SCREEN_ANIMATION_DURATION);
		animation.setInterpolator(SCREEN_ANIMATION_INTERPOLATOR);
		return animation;
	}

}
