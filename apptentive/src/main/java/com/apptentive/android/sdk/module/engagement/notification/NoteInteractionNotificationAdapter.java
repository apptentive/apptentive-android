/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import androidx.core.app.NotificationCompat;
import android.util.TypedValue;

import com.apptentive.android.sdk.ApptentiveHelper;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Invocation;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Action;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.Actions;
import com.apptentive.android.sdk.module.engagement.interaction.model.common.LaunchInteractionAction;
import com.apptentive.android.sdk.module.engagement.logic.FieldManager;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.ApptentiveLogTag.NOTIFICATION_INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_ACTION_DELETE;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_ACTION_DISPLAY;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_ACTION_NOTE_BUTTON_PRESSED;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_ID;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_INTERACTION_DEFINITION;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_INTERACTION_TYPE;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_EXTRA_NOTE_ACTION_INDEX;
import static com.apptentive.android.sdk.util.Constants.NOTIFICATION_ID_DEFAULT;

public class NoteInteractionNotificationAdapter implements InteractionNotificationAdapter {

	@Override
	public void handleInteractionNotificationAction(Context context, String channelId, Intent intent) {

		String action = intent.getAction();
		String interactionString = intent.getStringExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION);
		TextModalInteraction interaction;
		try {
			interaction = new TextModalInteraction(intent.getStringExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION));
		} catch (JSONException e) {
			ApptentiveLog.w(NOTIFICATION_INTERACTIONS, "Unable to parse interaction: %s", interactionString);
			logException(e);
			return;
		}
		if (StringUtils.equal(action, NOTIFICATION_ACTION_DISPLAY)) {
			actionDisplayNotification(context, channelId, interaction);
		} else if (StringUtils.equal(action, NOTIFICATION_ACTION_DELETE)) {
			actionDelete(context, interaction);
		} else if (StringUtils.equal(action, NOTIFICATION_ACTION_NOTE_BUTTON_PRESSED)) {
			actionButtonPressed(context, intent, interaction);
		} else {
			ApptentiveLog.w(NOTIFICATION_INTERACTIONS, "Unsupported action %s for Interaction type %s", action, interaction.getType().name());
		}
	}

	protected void actionDisplayNotification(final Context context, final String channelId, final TextModalInteraction interaction) {

		// Build notification
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

		builder.setVibrate(new long[]{0, 100, 100, 100, 100, 100})
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.apptentive_ic_stat_chat_bubble) // Set as default. Overridden below.
				.setWhen(System.currentTimeMillis());

		// Set up the Intent that is triggered if the user swipes or clears the Notification.
		Intent deleteIntent = new Intent(context, ApptentiveNotificationInteractionBroadcastReceiver.class);
		deleteIntent.putExtra(NOTIFICATION_EXTRA_ID, NOTIFICATION_ID_DEFAULT);
		deleteIntent.putExtra(NOTIFICATION_EXTRA_INTERACTION_TYPE, interaction.getType().name());
		deleteIntent.putExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION, interaction.toString());
		deleteIntent.setAction(NOTIFICATION_ACTION_DELETE);
		PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);
		builder.setDeleteIntent(deletePendingIntent);

		// Set up the text content of the Notification.
		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
		if (!StringUtils.isNullOrEmpty(interaction.getTitle())) {
			builder.setContentTitle(interaction.getTitle());
			bigTextStyle.setBigContentTitle(interaction.getTitle());
		}
		if (!StringUtils.isNullOrEmpty(interaction.getBody())) {
			builder.setContentText(interaction.getBody());
			bigTextStyle.bigText(interaction.getBody());
		}
		builder.setStyle(bigTextStyle);

		// Set up the action buttons, if any are specified. Up to 3 are allowed.
		Actions actions = interaction.getActions();
		if (actions != null) {
			List<Action> actionsList = actions.getAsList();
			for (int i = 0; i < actionsList.size(); i++) {
				Action action = actionsList.get(i);

				if (i > 3) {
					ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Can't have more than 3 buttons on a Note.");
					return;
				}
				Action.Type actionType = action.getType();

				Intent intent = new Intent(context, ApptentiveNotificationInteractionBroadcastReceiver.class);
				intent.putExtra(NOTIFICATION_EXTRA_ID, NOTIFICATION_ID_DEFAULT);
				intent.putExtra(NOTIFICATION_EXTRA_INTERACTION_TYPE, interaction.getType().name());
				intent.putExtra(NOTIFICATION_EXTRA_INTERACTION_DEFINITION, interaction.toString());
				intent.putExtra(NOTIFICATION_EXTRA_NOTE_ACTION_INDEX, i);
				switch (actionType) {
					// Don't worry about what each button does now, let the adapter make that choice when it does get pressed.
					case interaction:
					case dismiss:
						intent.setAction(NOTIFICATION_ACTION_NOTE_BUTTON_PRESSED);
						break;
					case unknown:
						return;
				}
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), intent, PendingIntent.FLAG_ONE_SHOT);
				builder.addAction(new NotificationCompat.Action.Builder(0, action.getLabel(), pendingIntent).build());
			}
		}

		// Set styles on the Notification pulled from the app theme.
		Resources.Theme theme = Util.buildApptentiveInteractionTheme(context);
		if (theme != null) {
			TypedValue icon = new TypedValue();
			if (theme.resolveAttribute(R.attr.apptentiveInteractionNotificationSmallIcon, icon, true)) {
				builder.setSmallIcon(icon.resourceId);
			} else {
				ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Unable to find icon in theme for setting Notification icon.");
			}
			TypedValue color = new TypedValue();
			if (theme.resolveAttribute(R.attr.apptentiveInteractionNotificationColor, color, true)) {
				builder.setColor(color.data);
			} else {
				ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Unable to find color in theme for setting Notification icon.");
			}
		} else {
			ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Unable to build theme for getting Notification icon.");
		}

		Notification notification = builder.build();
		((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID_DEFAULT, notification);

		// Sending an internal event here for tracking Interaction state transitions on the server. These events sent in this class are designed to duplicate the behavior of the existing Activity based display_type for Notes.
		final JSONObject data = new JSONObject();
		try {
			data.put(TextModalInteraction.KEY_DISPLAY_TYPE, interaction.getDisplayType().name());
		} catch (JSONException e) {
			ApptentiveLog.e(NOTIFICATION_INTERACTIONS, e, "Error creating Event data object.");
			logException(e);
		}
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				return EngagementModule.engageInternal(context, conversation, interaction, ApptentiveBaseFragment.EVENT_NAME_LAUNCH, data.toString());
			}
		}, "engage Note Notification launch");
	}

	protected void actionButtonPressed(final Context context, final Intent incomingIntent, final TextModalInteraction interaction) {

		int notificationId = incomingIntent.getIntExtra(Constants.NOTIFICATION_EXTRA_ID, NOTIFICATION_ID_DEFAULT);
		final int index = incomingIntent.getIntExtra(Constants.NOTIFICATION_EXTRA_NOTE_ACTION_INDEX, Integer.MIN_VALUE);

		// Perform the action specified for this button.
		List<Action> actions = interaction.getActions().getAsList();
		final Action action = actions.get(index);
		Action.Type actionType = action.getType();
		ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "Note Notification button pressed with index %d and action type %s", index, actionType.name());
		switch (actionType) {
			case interaction: {
				// First, make sure the Notification Drawer is dismissed.
				context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

				// Dispatch a task to handle the rest, since it requires a Conversation.
				ApptentiveHelper.dispatchConversationTask(new ConversationDispatchTask() {
					@Override
					protected boolean execute(Conversation conversation) {
						// Retrieve the Interaction this button is supposed to launch
						LaunchInteractionAction launchInteractionAction = (LaunchInteractionAction) action;
						List<Invocation> invocations = launchInteractionAction.getInvocations();
						String interactionIdToLaunch = null;

						// Need to check each Invocation object's criteria to find the right one.
						for (Invocation invocation : invocations) {
							FieldManager fieldManager = new FieldManager(context, conversation.getVersionHistory(), conversation.getEventData(), conversation.getPerson(), conversation.getDevice(), conversation.getAppRelease());
							if (invocation.isCriteriaMet(fieldManager, true)) {
								interactionIdToLaunch = invocation.getInteractionId();
								ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "Found an Interaction to launch with id %s", interactionIdToLaunch);
								break;
							}
						}

						// If an Interaction can be launched, fetch its definition.
						Interaction invokedInteraction = null;
						if (interactionIdToLaunch != null) {
							String interactionsString = conversation.getInteractions();
							if (interactionsString != null) {
								try {
									Interactions interactions = new Interactions(interactionsString);
									invokedInteraction = interactions.getInteraction(interactionIdToLaunch);
								} catch (JSONException e) {
									logException(e);
								}
							}
						}

						// Send the tracking event, now that we have all the necessary information.
						final JSONObject data = new JSONObject();
						try {
							data.put(TextModalInteraction.EVENT_KEY_ACTION_ID, action.getId());
							data.put(Action.KEY_LABEL, action.getLabel());
							data.put(TextModalInteraction.EVENT_KEY_ACTION_POSITION, index);
							data.put(TextModalInteraction.EVENT_KEY_INVOKED_INTERACTION_ID, invokedInteraction == null ? JSONObject.NULL : invokedInteraction.getId());
							data.put(TextModalInteraction.KEY_DISPLAY_TYPE, interaction.getDisplayType().name());
						} catch (JSONException e) {
							ApptentiveLog.e(NOTIFICATION_INTERACTIONS, e, "Error creating Event data object.");
							logException(e);
						}
						EngagementModule.engageInternal(context, conversation, interaction, TextModalInteraction.EVENT_NAME_INTERACTION, data.toString());

						// Finally, launch the interaction, if there is one
						if (invokedInteraction != null) {
							ApptentiveLog.d(NOTIFICATION_INTERACTIONS, "Launching interaction from Note Notification action: %s", interactionIdToLaunch);
							EngagementModule.launchInteraction(context, invokedInteraction);
						} else {
							ApptentiveLog.w(NOTIFICATION_INTERACTIONS, "No Interaction was found to display matching id %s", interactionIdToLaunch);
						}
						return false;
					}
				}, "choosing and launching Interaction from Note Notification Action");


				break;
			}
			case dismiss:
				// Just send the tracking event
				final JSONObject data = new JSONObject();
				try {
					data.put(TextModalInteraction.EVENT_KEY_ACTION_ID, action.getId());
					data.put(Action.KEY_LABEL, action.getLabel());
					data.put(TextModalInteraction.EVENT_KEY_ACTION_POSITION, index);
					data.put(TextModalInteraction.KEY_DISPLAY_TYPE, interaction.getDisplayType().name());
				} catch (JSONException e) {
					ApptentiveLog.e(NOTIFICATION_INTERACTIONS, e, "Error creating Event data object.");
					logException(e);
				}
				dispatchConversationTask(new ConversationDispatchTask() {
					@Override
					protected boolean execute(Conversation conversation) {
						return EngagementModule.engageInternal(context, conversation, interaction, TextModalInteraction.EVENT_NAME_DISMISS, data.toString());
					}
				}, "engage Note Notification dismiss");

				break;
			case unknown:
				ApptentiveLog.w(NOTIFICATION_INTERACTIONS, "Unknown Note Interaction Notification button action. Can't do anything.");
				break;
		}

		// Remove the Notification
		((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
	}

	protected void actionDelete(final Context context, final TextModalInteraction interaction) {
		ApptentiveLog.v(NOTIFICATION_INTERACTIONS, "Delete intent received.");
		final JSONObject data = new JSONObject();
		try {
			data.put(TextModalInteraction.KEY_DISPLAY_TYPE, interaction.getDisplayType().name());
		} catch (JSONException e) {
			ApptentiveLog.e(NOTIFICATION_INTERACTIONS, e, "Error creating Event data object.");
			logException(e);
		}
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				return EngagementModule.engageInternal(context, conversation, interaction, TextModalInteraction.EVENT_NAME_CANCEL, data.toString());
			}
		}, "engage Note Notification cancel");
	}
}
