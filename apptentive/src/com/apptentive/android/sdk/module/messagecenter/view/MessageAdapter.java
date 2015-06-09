/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.os.AsyncTask;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * @author Sky Kelsey
 */
public class MessageAdapter<T extends MessageCenterListItem> extends ArrayAdapter<T> {

	private final int TYPE_TXT_IN = 0, TYPE_TXT_OUT = 1, TYPE_FILE_IN = 2, TYPE_FILE_OUT = 3,
		TYPE_AUTO = 4, TYPE_GREETING = 5;

	private Bitmap avatarCache;

	public MessageAdapter(Context context) {
		super(context, 0);
	}

	private static class TextViewHolder {
		TextMessageView view;
	}

	private static class FileViewHolder {
		FileMessageView view;
	}

	private static class AutoViewHolder {
		AutomatedMessageView view;
	}

	private static class GreetingViewHolder {
		MessageCenterGreetingView view;
	}

	@Override
	public int getItemViewType(int position) {
		MessageCenterListItem listItem = getItem(position);
		if (listItem instanceof Message) {
			Message message = (Message) listItem;
			if (message.getBaseType() == Payload.BaseType.message) {
				switch (message.getType()) {
					case TextMessage:
						return (message.isOutgoingMessage()) ? TYPE_TXT_OUT : TYPE_TXT_IN;
					case FileMessage:
						return (message.isOutgoingMessage()) ? TYPE_FILE_OUT : TYPE_FILE_IN;
					case AutomatedMessage:
						return TYPE_AUTO;
				}
			}
		} else if (listItem instanceof MessageCenterGreeting) {
			return TYPE_GREETING;
		}
		return IGNORE_ITEM_VIEW_TYPE;
	}

	@Override
	public int getViewTypeCount() {
		return 6;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageCenterListItem listItem = getItem(position);
		TextViewHolder holderTxt = null;
		FileViewHolder holderFile = null;
		AutoViewHolder holderAuto = null;
		GreetingViewHolder holderGreeting = null;

		int type = getItemViewType(position);
		if (null == convertView) {
			switch (type) {
				case TYPE_TXT_IN:
				case TYPE_TXT_OUT:
					holderTxt = new TextViewHolder();
					TextMessageView tv = new TextMessageView(parent.getContext(), (TextMessage) listItem);
					holderTxt.view = tv;
					convertView = tv;
					convertView.setTag(holderTxt);
					break;
				case TYPE_FILE_IN:
				case TYPE_FILE_OUT:
					holderFile = new FileViewHolder();
					FileMessageView fv = new FileMessageView(parent.getContext(), (FileMessage) listItem);
					holderFile.view = fv;
					convertView = fv;
					convertView.setTag(holderFile);
					break;
				case TYPE_AUTO:
					holderAuto = new AutoViewHolder();
					AutomatedMessageView av = new AutomatedMessageView(parent.getContext(), (AutomatedMessage) listItem);
					holderAuto.view = av;
					convertView = av;
					convertView.setTag(holderAuto);
					break;
				case TYPE_GREETING:
					holderGreeting = new GreetingViewHolder();
					MessageCenterGreetingView gv = new MessageCenterGreetingView(parent.getContext(), (MessageCenterGreeting) listItem);
					convertView = gv;
					holderGreeting.view = gv;
					convertView.setTag(holderGreeting);
					break;
				default:
					break;
			}
		} else {
			switch (type) {
				case TYPE_TXT_IN:
				case TYPE_TXT_OUT:
					holderTxt = (TextViewHolder) convertView.getTag();
					break;
				case TYPE_FILE_IN:
				case TYPE_FILE_OUT:
					holderFile = (FileViewHolder) convertView.getTag();
					break;
				case TYPE_AUTO:
					holderAuto = (AutoViewHolder) convertView.getTag();
					break;
				case TYPE_GREETING:
					holderGreeting = (GreetingViewHolder) convertView.getTag();
					break;
				default:
					break;
			}
		}
		switch (type) {
			case TYPE_TXT_IN:
				holderTxt.view.updateMessage((TextMessage) listItem);

				if (avatarCache == null) {
					startDownloadAvatarTask(holderTxt.view, ((TextMessage) listItem).getSenderProfilePhoto());
				} else {
					holderTxt.view.setAvatar(avatarCache);
				}
				break;
			case TYPE_TXT_OUT:
				holderTxt.view.updateMessage((TextMessage) listItem);
				break;
			case TYPE_FILE_IN:
				holderFile.view.updateMessage((FileMessage) listItem);
				if (avatarCache == null) {
					startDownloadAvatarTask(holderFile.view, ((FileMessage) listItem).getSenderProfilePhoto());
				} else {
					holderFile.view.setAvatar(avatarCache);
				}
				break;
			case TYPE_FILE_OUT:
				holderFile.view.updateMessage((FileMessage) listItem);
				break;
			case TYPE_AUTO:
				holderAuto.view.updateMessage((AutomatedMessage) listItem);
				break;
			case TYPE_GREETING:
				holderGreeting.view.updateMessage((MessageCenterGreeting) listItem);
				break;
			default:
				return null;
		}
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	private void startDownloadAvatarTask(PersonalMessageView view, String imageUrl) {
		DownloadImageTask task = new DownloadImageTask(view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
		} else {
			task.execute(imageUrl);
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<PersonalMessageView> resultView;

		DownloadImageTask(PersonalMessageView view) {
			resultView = new WeakReference<PersonalMessageView>(view);
		}

		protected Bitmap doInBackground(String... urls) {
			Bitmap bmp = null;
			try {
				bmp = this.loadImageFromNetwork(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}
			avatarCache = result;
			PersonalMessageView view = resultView.get();
			if (view != null) {
				view.setAvatar(result);
			}
		}

		private Bitmap loadImageFromNetwork(String imageUrl) throws IOException {
			URL url = new URL(imageUrl);
			return BitmapFactory.decodeStream(url.openStream());
		}
	}
}
