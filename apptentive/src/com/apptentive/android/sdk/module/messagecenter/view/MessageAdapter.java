/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.os.AsyncTask;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;

import java.io.IOException;
import java.net.URL;

/**
 * @author Sky Kelsey
 */
public class MessageAdapter<T extends MessageCenterListItem> extends ArrayAdapter<T> {

	private final int TYPE_TXT = 0,TYPE_FILE = 1,TYPE_AUTO = 2,TYPE_GREETING=3;

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
						return TYPE_TXT;
					case FileMessage:
						return TYPE_FILE;
					case AutomatedMessage:
						return TYPE_AUTO;
				}
			}
		} else if (listItem instanceof MessageCenterGreeting) {
			return TYPE_GREETING;
		}
		return TYPE_TXT;
	}

	@Override
	public int getViewTypeCount()
	{
		return 4;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageCenterListItem listItem = getItem(position);
		TextViewHolder holder1 = null;
		FileViewHolder holder2 = null;
		AutoViewHolder holder3 = null;
		GreetingViewHolder holder4 = null;

		int type = getItemViewType(position);
		if(null==convertView)
		{
			switch (type) {
				case TYPE_TXT:
					holder1=new TextViewHolder();
					TextMessageView tv = new TextMessageView(parent.getContext(), (TextMessage) listItem);
					holder1.view = tv;
					convertView = tv;
					convertView.setTag(holder1);
					break;
				case TYPE_FILE:
					holder2=new FileViewHolder();
					FileMessageView fv = new FileMessageView(parent.getContext(), (FileMessage) listItem);
					holder2.view = fv;
					convertView = fv;
					convertView.setTag(holder2);
					break;
				case TYPE_AUTO:
					holder3=new AutoViewHolder();
					AutomatedMessageView av = new AutomatedMessageView(parent.getContext(), (AutomatedMessage) listItem);
					holder3.view = av;
					convertView = av;
					convertView.setTag(holder3);
					break;
				case TYPE_GREETING:
					holder4=new GreetingViewHolder();
					MessageCenterGreetingView gv = new MessageCenterGreetingView(parent.getContext(), (MessageCenterGreeting) listItem);
					convertView = gv;
					holder4.view = gv;
					convertView.setTag(holder4);
					break;
				default:
					break;
			}
		}
		else
		{
			switch (type) {
				case TYPE_TXT:
					holder1=(TextViewHolder)convertView.getTag();
					break;
				case TYPE_FILE:
					holder2=(FileViewHolder)convertView.getTag();
					break;
				case TYPE_AUTO:
					holder3=(AutoViewHolder)convertView.getTag();
					break;
				case TYPE_GREETING:
					holder4=(GreetingViewHolder)convertView.getTag();
					break;
				default:
					break;
			}
		}
		switch (type)
		{
			case TYPE_TXT:
				holder1.view.updateMessage((TextMessage) listItem);
				boolean bShowAvatar = !((TextMessage) listItem).isOutgoingMessage();
				if (avatarCache == null && bShowAvatar) {
					new DownloadImageTask(holder1).execute(((TextMessage) listItem).getSenderProfilePhoto());
				} else if (bShowAvatar) {
					holder1.view.setAvatar(avatarCache);
				}
				break;
			case TYPE_FILE:
				holder2.view.updateMessage((FileMessage) listItem);
				break;
			case TYPE_AUTO:
				holder3.view.updateMessage((AutomatedMessage) listItem);
				break;
			case TYPE_GREETING:
				holder4.view.updateMessage((MessageCenterGreeting) listItem);
			default:
				break;
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

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private TextViewHolder resultView;

		DownloadImageTask(TextViewHolder resultView) {
			this.resultView = resultView;
		}

		protected Bitmap doInBackground(String... urls) {
			//if (avatarCache != null) {
			//	return avatarCache;
			//}
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
			super.onPostExecute(result);
			avatarCache = result;
			resultView.view.setAvatar(result);
		}

		private Bitmap loadImageFromNetwork(String imageUrl) throws IOException {
			URL url = new URL(imageUrl);
			return BitmapFactory.decodeStream(url.openStream());
		}
	}
}
