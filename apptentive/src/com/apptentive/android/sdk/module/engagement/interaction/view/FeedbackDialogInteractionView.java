/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.AutomatedMessage;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.model.TextMessage;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.FeedbackDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.common.ApptentiveDialogButton;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.view.EmailValidationFailedDialog;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Sky Kelsey
 */
public class FeedbackDialogInteractionView extends InteractionView<FeedbackDialogInteraction> {

	private static final String CODE_POINT_CANCEL = "cancel";
	private static final String CODE_POINT_DECLINE = "decline";
	private static final String CODE_POINT_SUBMIT = "submit";
	private static final String CODE_POINT_SKIP_VIEW_MESSAGES = "skip_view_messages";
	private static final String CODE_POINT_VIEW_MESSAGES = "view_messages";

	private CharSequence email;
	private CharSequence message;

	// Don't show the wrong view when we rotate.
	private static final String THANK_YOU_DIALOG_VISIBLE = "thank_you_dialog_visible";
	private boolean thankYouDialogVisible = false;

	public FeedbackDialogInteractionView(FeedbackDialogInteraction interaction) {
		super(interaction);
	}

	@Override
	public void doOnCreate(final Activity activity, Bundle savedInstanceState) {
		activity.setContentView(R.layout.apptentive_feedback_dialog_interaction);

		// Legacy support: We can remove this when we switch over to 100% interaction based Message Center.
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SHOULD_SHOW_INTRO_DIALOG, false).commit();

		if (savedInstanceState != null) {
			thankYouDialogVisible = savedInstanceState.getBoolean(THANK_YOU_DIALOG_VISIBLE, false);
		}
		if (!thankYouDialogVisible) {
			final AutoCompleteTextView emailView = (AutoCompleteTextView) activity.findViewById(R.id.email);
			EditText messageView = (EditText) activity.findViewById(R.id.message);
			ApptentiveDialogButton noButton = (ApptentiveDialogButton) activity.findViewById(R.id.decline);
			final ApptentiveDialogButton sendButton = (ApptentiveDialogButton) activity.findViewById(R.id.submit);

			// Title
			String title = interaction.getTitle();
			if (title != null) {
				TextView titleView = (TextView) activity.findViewById(R.id.title);
				titleView.setText(title);
			}

			// Body
			String body = interaction.getBody(activity);
			if (body != null) {
				TextView bodyView = (TextView) activity.findViewById(R.id.body);
				bodyView.setText(body);
			}

			// Email
			String personEnteredEmail = PersonManager.loadPersonEmail(activity);
			if (!interaction.isAskForEmail()) {
				emailView.setVisibility(View.GONE);
			} else if (!Util.isEmpty(personEnteredEmail)) {
				emailView.setVisibility(View.GONE);
				email = personEnteredEmail;
			} else {
				String personInitialEmail = PersonManager.loadInitialPersonEmail(activity);
				if (!Util.isEmpty(personInitialEmail)) {
					emailView.setText(personInitialEmail);
					email = personInitialEmail;
				}

				String emailHintText = interaction.getEmailHintText();
				if (emailHintText != null) {
					emailView.setHint(emailHintText);
				} else if (interaction.isEmailRequired()) {
					emailView.setHint(R.string.apptentive_edittext_hint_email_required);
				}

				// Pre-populate a list of possible emails based on those pulled from the phone.
				ArrayAdapter<String> emailAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, Util.getAllUserAccountEmailAddresses(activity));
				emailView.setAdapter(emailAdapter);
				emailView.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						emailView.showDropDown();
						return false;
					}
				});
				emailView.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
					}

					@Override
					public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
						email = charSequence;
						validateForm(sendButton);
					}

					@Override
					public void afterTextChanged(Editable editable) {
					}
				});
			}

			// Message
			String messageHintText = interaction.getMessageHintText();
			if (messageHintText != null) {
				messageView.setHint(messageHintText);
			}
			messageView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
					message = charSequence;
					validateForm(sendButton);
				}

				@Override
				public void afterTextChanged(Editable editable) {
				}
			});


			// No
			String no = interaction.getDeclineText();
			if (no != null) {
				noButton.setText(no);
			}
			noButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					EngagementModule.engageInternal(activity, interaction, CODE_POINT_DECLINE);
					activity.finish();
				}
			});

			// Send
			String send = interaction.getSubmitText();
			if (send != null) {
				sendButton.setText(send);
			}
			sendButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Util.hideSoftKeyboard(activity, view);

					if (email != null && email.length() != 0 && !Util.isEmailValid(email.toString())) {
						EmailValidationFailedDialog dialog = new EmailValidationFailedDialog(activity);
						dialog.show();
						return;
					}

					// Before we send this message, send an auto message.
					createMessageCenterAutoMessage(activity);

					sendMessage(activity);

					EngagementModule.engageInternal(activity, interaction, CODE_POINT_SUBMIT);
					thankYouDialogVisible = true;
					activity.findViewById(R.id.feedback_dialog).setVisibility(View.GONE);
					activity.findViewById(R.id.thank_you_dialog).setVisibility(View.VISIBLE);
				}
			});
			validateForm(sendButton);
		} else {
			activity.findViewById(R.id.feedback_dialog).setVisibility(View.GONE);
			activity.findViewById(R.id.thank_you_dialog).setVisibility(View.VISIBLE);
		}

		// Thank You Title
		TextView thankYouTitleView = (TextView) activity.findViewById(R.id.thank_you_title);
		String thankYouTitle = interaction.getThankYouTitle();
		if (thankYouTitle != null) {
			thankYouTitleView.setText(thankYouTitle);
		}

		// Thank You Body
		TextView thankYouBodyView = (TextView) activity.findViewById(R.id.thank_you_body);
		String thankYouBody = interaction.getThankYouBody();
		if (thankYouBody != null) {
			thankYouBodyView.setText(thankYouBody);
		}

		// Thank You Close Button
		ApptentiveDialogButton thankYouCloseButton = (ApptentiveDialogButton) activity.findViewById(R.id.thank_you_close);
		String thankYouCloseText = interaction.getThankYouCloseText();
		if (thankYouCloseText != null) {
			thankYouCloseButton.setText(thankYouCloseText);
		}
		thankYouCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EngagementModule.engageInternal(activity, interaction, CODE_POINT_SKIP_VIEW_MESSAGES);
				activity.finish();
			}
		});

		// Thank You View Messages Button
		ApptentiveDialogButton thankYouViewMessagesButton = (ApptentiveDialogButton) activity.findViewById(R.id.thank_you_view_messages);
		if (interaction.isMessageCenterEnabled()) {
			String thankYouViewMessages = interaction.getThankYouViewMessagesText();
			if (thankYouViewMessages != null) {
				thankYouViewMessagesButton.setText(thankYouViewMessages);
			}
			thankYouViewMessagesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					EngagementModule.engageInternal(activity, interaction, CODE_POINT_VIEW_MESSAGES);
					activity.finish();
				}
			});
		} else {
			thankYouViewMessagesButton.setVisibility(View.GONE);
		}
	}

	public static void createMessageCenterAutoMessage(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		boolean shownAutoMessage = prefs.getBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_AUTO_MESSAGE, false);

		// Migrate old values if needed.
		boolean shownManual = prefs.getBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_MANUAL, false);
		boolean shownNoLove = prefs.getBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_NO_LOVE, false);
		if (!shownAutoMessage) {
			if (shownManual || shownNoLove) {
				shownAutoMessage = true;
				prefs.edit().putBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_AUTO_MESSAGE, true).commit();
			}
		}

		AutomatedMessage message = null;

		if (!shownAutoMessage) {
			prefs.edit().putBoolean(Constants.PREF_KEY_AUTO_MESSAGE_SHOWN_AUTO_MESSAGE, true).commit();
			message = AutomatedMessage.createWelcomeMessage(context);
		}
		if (message != null) {
			ApptentiveDatabase db = ApptentiveDatabase.getInstance(context);
			db.addOrUpdateMessages(message);
			db.addPayload(message);
		}
	}

	private void sendMessage(final Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SHOULD_SHOW_INTRO_DIALOG, false).commit();
		// Save the email.
		if (interaction.isAskForEmail()) {
			if (email != null && email.length() != 0) {
				PersonManager.storePersonEmail(activity, email.toString());
				Person person = PersonManager.storePersonAndReturnDiff(activity);
				if (person != null) {
					Log.d("Person was updated.");
					Log.v(person.toString());
					ApptentiveDatabase.getInstance(activity).addPayload(person);
				} else {
					Log.d("Person was not updated.");
				}
			}
		}
		// Send the message.
		final TextMessage textMessage = new TextMessage();
		textMessage.setBody(message.toString());
		textMessage.setRead(true);
		MessageManager.sendMessage(activity, textMessage);
	}

	private void validateForm(ApptentiveDialogButton sendButton) {
		boolean passedEmail = true;
		if (interaction.isEmailRequired() && !Util.isEmpty(PersonManager.loadInitialPersonEmail(sendButton.getContext()))) {
			passedEmail = !Util.isEmpty(email);
		}
		boolean passedMessage = !Util.isEmpty(message);

		sendButton.setEnabled(passedEmail && passedMessage);
	}

	@Override
	public boolean onBackPressed(Activity activity) {
		EngagementModule.engageInternal(activity, interaction, CODE_POINT_CANCEL);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(THANK_YOU_DIALOG_VISIBLE, thankYouDialogVisible);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		thankYouDialogVisible = savedInstanceState.getBoolean(THANK_YOU_DIALOG_VISIBLE, false);
	}
}
