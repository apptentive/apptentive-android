/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.os.AsyncTask;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apptentive.android.sdk.ApptentiveHelper;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.model.ApptentiveMessage;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.OnListviewItemActionListener;
import com.apptentive.android.sdk.module.messagecenter.model.Composer;
import com.apptentive.android.sdk.module.messagecenter.model.ContextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.WhoCard;
import com.apptentive.android.sdk.module.messagecenter.view.holder.AutomatedMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.ContextMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.GreetingHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.IncomingCompoundMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.MessageComposerHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.OutgoingCompoundMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.StatusHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.WhoCardHolder;
import com.apptentive.android.sdk.util.image.ImageItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.GREETING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_AUTO;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_COMPOSER;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_CONTEXT;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_INCOMING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.MESSAGE_OUTGOING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.STATUS;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem.WHO_CARD;

public class MessageCenterRecyclerViewAdapter extends RecyclerView.Adapter {

	MessageCenterFragment fragment;
	OnListviewItemActionListener listener;
	RecyclerView recyclerView;
	Interaction interaction;
	List<MessageCenterListItem> listItems;
	// maps to prevent redundant asynctasks
	private ArrayList<ApptentiveMessage> messagesWithPendingReadStatusUpdate = new ArrayList<ApptentiveMessage>();

	public MessageCenterRecyclerViewAdapter(MessageCenterFragment fragment, OnListviewItemActionListener listener, Interaction interaction, List<MessageCenterListItem> listItems) {
		this.fragment = fragment;
		this.listener = listener;
		this.interaction = interaction;
		this.listItems = listItems;
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		this.recyclerView = recyclerView;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case MESSAGE_COMPOSER: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_composer, parent, false);
				return new MessageComposerHolder(view);
			}
			case STATUS: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_status, parent, false);
				return new StatusHolder(view);
			}
			case GREETING: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_greeting, parent, false);
				return new GreetingHolder(view);
			}
			case MESSAGE_OUTGOING: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_outgoing, parent, false);
				return new OutgoingCompoundMessageHolder(view);
			}
			case MESSAGE_INCOMING: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_incoming, parent, false);
				return new IncomingCompoundMessageHolder(view);
			}
			case MESSAGE_AUTO: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_auto, parent, false);
				return new AutomatedMessageHolder(view);
			}
			case WHO_CARD: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_who_card, parent, false);
				return new WhoCardHolder(this, view);
			}
			case MESSAGE_CONTEXT: {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_context_message, parent, false);
				return new ContextMessageHolder(view);
			}
		}
		ApptentiveLog.w(MESSAGES, "onCreateViewHolder(%d) returning null.", viewType);
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		try {
			switch (getItemViewType(position)) {
				case MESSAGE_COMPOSER: {
					Composer composer = (Composer) listItems.get(position);
					MessageComposerHolder composerHolder = (MessageComposerHolder) holder;
					composerHolder.bindView(fragment, this, composer);
					break;
				}
				case STATUS: {
					MessageCenterStatus status = (MessageCenterStatus) listItems.get(position);
					StatusHolder statusHolder = (StatusHolder) holder;
					statusHolder.body.setText(status.body);
					if (status.icon != null) {
						statusHolder.icon.setImageResource(status.icon);
						statusHolder.icon.setVisibility(View.VISIBLE);
					} else {
						statusHolder.icon.setVisibility(View.GONE);
					}
					break;
				}
				case GREETING: {
					MessageCenterGreeting greeting = (MessageCenterGreeting) listItems.get(position);
					GreetingHolder greetingHolder = (GreetingHolder) holder;
					greetingHolder.bindView(greeting);
					break;
				}
				case MESSAGE_INCOMING: {
					CompoundMessage compoundMessage = (CompoundMessage) listItems.get(position);
					IncomingCompoundMessageHolder compoundHolder = (IncomingCompoundMessageHolder) holder;
					compoundHolder.bindView(fragment, recyclerView, this, compoundMessage);
					// Mark as read
					if (!compoundMessage.isRead() && !messagesWithPendingReadStatusUpdate.contains(compoundMessage)) {
						messagesWithPendingReadStatusUpdate.add(compoundMessage);
						startUpdateUnreadMessageTask(compoundMessage);
					}
					break;
				}
				case MESSAGE_OUTGOING: {
					CompoundMessage compoundMessage = (CompoundMessage) listItems.get(position);
					OutgoingCompoundMessageHolder compoundHolder = (OutgoingCompoundMessageHolder) holder;
					compoundHolder.bindView(fragment, recyclerView, this, compoundMessage);
					break;
				}
				case MESSAGE_AUTO: {
					CompoundMessage autoMessage = (CompoundMessage) listItems.get(position);
					AutomatedMessageHolder autoHolder = (AutomatedMessageHolder) holder;
					autoHolder.bindView(recyclerView, autoMessage);
					break;
				}
				case WHO_CARD: {
					WhoCard whoCard = (WhoCard) listItems.get(position);
					WhoCardHolder whoCardHolder = (WhoCardHolder) holder;
					whoCardHolder.bindView(recyclerView, whoCard);
					break;
				}
				case MESSAGE_CONTEXT: {
					ContextMessage contextMessage = (ContextMessage) listItems.get(position);
					ContextMessageHolder contextMessageHolder = (ContextMessageHolder) holder;
					contextMessageHolder.bindView(contextMessage);
					break;
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while binding view holder");
			logException(e);
		}
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);
		if (holder instanceof MessageComposerHolder) {
			MessageComposerHolder composer = (MessageComposerHolder) holder;
			composer.onViewAttachedToWindow();
		}
	}

	@Override
	public int getItemCount() {
		return listItems.size();
	}

	@Override
	public int getItemViewType(int position) {
		MessageCenterListItem message = listItems.get(position);
		return message.getListItemType();
	}

	public String getWhoCardAvatarFileName() {
		return null; // TODO
	}

	public void addImagestoComposer(MessageComposerHolder composer, List<ImageItem> images) {
		composer.addImagesToImageAttachmentBand(images);
		composer.setSendButtonState();
	}

	public void removeImageFromComposer(MessageComposerHolder composer, int position) {
		if (composer != null) {
			composer.removeImageFromImageAttachmentBand(position);
			composer.setSendButtonState();
		}
	}

	public OnListviewItemActionListener getListener() {
		return listener;
	}

	private void startUpdateUnreadMessageTask(CompoundMessage message) {
		UpdateUnreadMessageTask task = new UpdateUnreadMessageTask(message);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
	}

	private class UpdateUnreadMessageTask extends AsyncTask<ApptentiveMessage, Void, Void> {
		private ApptentiveMessage message;

		public UpdateUnreadMessageTask(ApptentiveMessage message) {
			this.message = message;
		}

		@Override
		protected Void doInBackground(ApptentiveMessage... messages) {
			final ApptentiveMessage message = messages[0];
			message.setRead(true);
			JSONObject data = new JSONObject();
			try {
				data.put("message_id", message.getId());
				data.put("message_type", message.getMessageType().name());
			} catch (JSONException e) {
				logException(e);
			}
			fragment.engageInternal(MessageCenterInteraction.EVENT_NAME_READ, data.toString());

			dispatchConversationTask(new ConversationDispatchTask() {
				@Override
				protected boolean execute(Conversation conversation) {
					MessageManager mgr = conversation.getMessageManager();
					if (mgr != null) {
						mgr.updateMessage(message);
						mgr.notifyHostUnreadMessagesListeners(mgr.getUnreadMessageCount());
					}
					return false;
				}
			}, "update message");

			return null;
		}

		@Override
		protected void onCancelled() {
			messagesWithPendingReadStatusUpdate.remove(message);
		}

		@Override
		protected void onPostExecute(Void result) {
			messagesWithPendingReadStatusUpdate.remove(message);
		}
	}

	private void logException(Exception e) {
		ErrorMetrics.logException(e);
	}
}
