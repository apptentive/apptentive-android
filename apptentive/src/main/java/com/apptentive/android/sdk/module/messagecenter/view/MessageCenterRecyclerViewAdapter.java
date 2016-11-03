/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.messagecenter.model.Composer;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.ContextMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
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

import java.util.List;

import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.GREETING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.MESSAGE_AUTO;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.MESSAGE_COMPOSER;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.MESSAGE_CONTEXT;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.MESSAGE_INCOMING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.MESSAGE_OUTGOING;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.STATUS;
import static com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem.WHO_CARD;

public class MessageCenterRecyclerViewAdapter extends RecyclerView.Adapter {

	MessageCenterFragment fragment;
	MessageAdapter.OnListviewItemActionListener listener;
	RecyclerView recyclerView;
	Interaction interaction;
	List<MessageCenterUtil.MessageCenterListItem> messages;

	public MessageCenterRecyclerViewAdapter(MessageCenterFragment fragment, MessageAdapter.OnListviewItemActionListener listener, Interaction interaction, List<MessageCenterUtil.MessageCenterListItem> messages) {
		this.fragment = fragment;
		this.listener = listener;
		this.interaction = interaction;
		this.messages = messages;
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		this.recyclerView = recyclerView;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ApptentiveLog.e("onCreateViewHolder()");
		switch (viewType) {
			case MESSAGE_COMPOSER: {
				ApptentiveLog.w("-> Message Composer");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_composer, parent, false);
				return new MessageComposerHolder(view);
			}
			case STATUS: {
				ApptentiveLog.w("-> Status");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_status, parent, false);
				return new StatusHolder(view);
			}
			case GREETING: {
				ApptentiveLog.w("-> Greeting");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_greeting, parent, false);
				return new GreetingHolder(view);
			}
			case MESSAGE_OUTGOING: {
				ApptentiveLog.w("-> Message Outgoing");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_outgoing, parent, false);
				return new OutgoingCompoundMessageHolder(view);
			}
			case MESSAGE_INCOMING: {
				ApptentiveLog.w("-> Message Incoming");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_incoming, parent, false);
				return new IncomingCompoundMessageHolder(view);
			}
			case MESSAGE_AUTO: {
				ApptentiveLog.w("-> Message Auto");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_auto, parent, false);
				return new AutomatedMessageHolder(view);
			}
			case WHO_CARD: {
				ApptentiveLog.w("-> Who Card");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_who_card, parent, false);
				return new WhoCardHolder(this, view);
			}
			case MESSAGE_CONTEXT: {
				ApptentiveLog.w("-> Message Context");
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				View view = inflater.inflate(R.layout.apptentive_message_center_context_message, parent, false);
				return new ContextMessageHolder(view);
			}
		}
		ApptentiveLog.e("onCreateViewHolder(%d) returning null.", viewType);
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ApptentiveLog.e("onBindViewHolder()");
		switch (getItemViewType(position)) {
			case MESSAGE_COMPOSER: {
				ApptentiveLog.w("-> Message Composer");
				Composer composer = (Composer) messages.get(position);
				MessageComposerHolder composerHolder = (MessageComposerHolder) holder;
				composerHolder.bindView(fragment, this, composer);
				break;
			}
			case STATUS: {
				ApptentiveLog.w("-> Status");
				MessageCenterStatus status = (MessageCenterStatus) messages.get(position);
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
				ApptentiveLog.w("-> Greeting");
				MessageCenterGreeting greeting = (MessageCenterGreeting) messages.get(position);
				GreetingHolder greetingHolder = (GreetingHolder) holder;
				greetingHolder.bindView(greeting);
				break;
			}
			case MESSAGE_INCOMING: {
				ApptentiveLog.w("-> Message Incoming");
				CompoundMessage compoundMessage = (CompoundMessage) messages.get(position);
				IncomingCompoundMessageHolder compoundHolder = (IncomingCompoundMessageHolder) holder;
				compoundHolder.bindView(recyclerView, compoundMessage);
				break;
			}
			case MESSAGE_OUTGOING: {
				ApptentiveLog.w("-> Message Outgoing");
				CompoundMessage compoundMessage = (CompoundMessage) messages.get(position);
				OutgoingCompoundMessageHolder compoundHolder = (OutgoingCompoundMessageHolder) holder;
				compoundHolder.bindView(recyclerView, compoundMessage);
				break;
			}
			case MESSAGE_AUTO: {
				ApptentiveLog.w("-> Message Auto");
				CompoundMessage autoMessage = (CompoundMessage) messages.get(position);
				AutomatedMessageHolder autoHolder = (AutomatedMessageHolder) holder;
				autoHolder.bindView(recyclerView, autoMessage);
				break;
			}
			case WHO_CARD: {
				ApptentiveLog.w("-> Who Card");
				WhoCard whoCard = (WhoCard) messages.get(position);
				WhoCardHolder whoCardHolder = (WhoCardHolder) holder;
				whoCardHolder.bindView(recyclerView, whoCard);
				break;
			}
			case MESSAGE_CONTEXT: {
				ApptentiveLog.w("-> Message Context");
				ContextMessage contextMessage = (ContextMessage) messages.get(position);
				ContextMessageHolder contextMessageHolder = (ContextMessageHolder) holder;
				contextMessageHolder.bindView(contextMessage);
				break;
			}
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		super.onViewRecycled(holder);
		ApptentiveLog.e("View recycled: %s", holder.toString());
		// TODO: Remove listeners, and clean up here?
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}

	@Override
	public int getItemViewType(int position) {
		MessageCenterUtil.MessageCenterListItem message = messages.get(position);
		return message.getListItemType();
	}

	public void setPaused(boolean bPause) {
		//isInPauseState = bPause; // TODO
	}

	public Parcelable getWhoCardNameState() {
		// TODO
		return new Parcelable() {
			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {

			}
		};
	}

	public Parcelable getWhoCardEmailState() {
		// TODO
		return new Parcelable() {
			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {

			}
		};
	}

	public String getWhoCardAvatarFileName() {
		return null; // TODO
	}

	public void addImagestoComposer(MessageComposerHolder composer, List<ImageItem> images) {
		composer.addImagesToImageAttachmentBand(images);
		//notifyDataSetChanged();
	}

	public void removeImageFromComposer(MessageComposerHolder composer, int position) {
		if (composer != null) {
			composer.removeImageFromImageAttachmentBand(position);
		}
	}

	public View getWhoCardView() {
		return new View(fragment.getContext()); // TODO
	}

	public MessageCenterComposingActionBarView getComposingActionBarView() {
		return new MessageCenterComposingActionBarView(fragment, null, null); // TODO
	}

	public View getComposingAreaView() {
		return new View(fragment.getContext()); // TODO
	}

	public MessageAdapter.OnListviewItemActionListener getListener() {
		return listener;
	}
}
