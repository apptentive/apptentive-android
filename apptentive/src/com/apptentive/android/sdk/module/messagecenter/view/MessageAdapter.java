/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.util.ImageUtil;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.view.ApptentiveMaterialIndeterminateProgressBar;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageAdapter<T extends MessageCenterListItem> extends ArrayAdapter<T> {

	private static final int TYPE_TXT_IN = 0, TYPE_TXT_OUT = 1, TYPE_FILE_IN = 2, TYPE_FILE_OUT = 3,
			TYPE_AUTO = 4, TYPE_GREETING = 5, TYPE_STATUS = 6, TYPE_Composing = 7;

	private static final int INVALID_POSITION = -1;

	private final static float MAX_IMAGE_SCREEN_PROPORTION_X = 0.5f;
	private final static float MAX_IMAGE_SCREEN_PROPORTION_Y = 0.6f;

	// Some absolute size limits to keep bitmap sizes down.
	private final static int MAX_IMAGE_DISPLAY_WIDTH = 800;
	private final static int MAX_IMAGE_DISPLAY_HEIGHT = 800;

	private boolean isInPauseState = false;
	private Bitmap avatarCache;
	private Context context;
	private int pendingUpdateIndex = INVALID_POSITION;
	private int composingViewIndex = INVALID_POSITION;
	private MessageCenterComposingView composingView;

	public MessageAdapter(Context context, List<MessageCenterListItem> items) {
		super(context, 0, (List<T>) items);
		this.context = context;
	}

	private class MessageViewHolder {

		protected int position = INVALID_POSITION;
		protected TextView messageTitleTextView;
		protected TextView messageBodyTextView;
		protected ImageView fileImageView;

		protected TextView timestampView;

		public void updateMessage(String messageTitle, String messageBody, String timeStamp, Bitmap imageBitmap) {
			// Set timestamp
			if (timestampView != null & timeStamp != null) {
				timestampView.setText(timeStamp);
			}

			if (messageTitleTextView != null && messageTitle != null) {
				messageTitleTextView.setText(messageTitle);
			}

			if (messageBodyTextView != null) {
				if (messageBody != null) {
					messageBodyTextView.setVisibility(View.VISIBLE);
					messageBodyTextView.setText(messageBody);
				} else {
					messageBodyTextView.setVisibility(View.GONE);
				}
			}

			// Set avatar or screenshot
			if (fileImageView != null && imageBitmap != null) {
				fileImageView.setImageBitmap(imageBitmap);
			}
		}
	}

	private class InComingMessageViewHolder extends MessageViewHolder {
		AvatarView avatarView;
		CollapsibleTextView collapsible;

		public void updateMessage(String messageTitle, String messageBody, String timeStamp,
															Bitmap fileBitmap, Bitmap avatarBitmap) {
			if (avatarView != null && avatarBitmap != null) {
				avatarView.setImageBitmap(avatarBitmap);
			}

			if (collapsible != null) {
				collapsible.setDesc(messageBody);
			}
			super.updateMessage(messageTitle, messageBody, timeStamp, fileBitmap);
		}
	}

	private class OutGoingMessageViewHolder extends MessageViewHolder {
		ApptentiveMaterialIndeterminateProgressBar progressBar;
		FrameLayout mainLayout;
		CollapsibleTextView collapsible;

		public void updateMessage(String messageTitle, String messageBody, boolean sent, boolean paused, String timeStamp,
															Bitmap fileBitmap) {
			// Set Progress indicator
			if (progressBar != null) {
				if (!sent && !paused) {
					progressBar.setVisibility(View.VISIBLE);
					progressBar.start();
				} else {
					progressBar.stop();
					progressBar.setVisibility(View.GONE);
				}
			}

			if (mainLayout != null) {
				mainLayout.setBackgroundColor((!sent && paused) ? context.getResources().getColor(R.color.apptentive_message_center_toolbar) :
						context.getResources().getColor(R.color.apptentive_message_center_outgoing_frame_background));
			}

			if (collapsible != null) {
				collapsible.setDesc(messageBody);
			}
			super.updateMessage(messageTitle, messageBody, timeStamp, fileBitmap);
		}
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
		} else if (listItem instanceof MessageCenterStatus) {
			return TYPE_STATUS;
		} else if (listItem instanceof MessageCenterComposingItem) {
			return TYPE_Composing;
		}
		return IGNORE_ITEM_VIEW_TYPE;
	}

	@Override
	public int getViewTypeCount() {
		return 8;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MessageCenterListItem listItem = getItem(position);

		int type = getItemViewType(position);
		MessageViewHolder holderMessage = null;
		if (null == convertView) {
			switch (type) {
				case TYPE_TXT_IN: {
					// Incoming message has: message, avatar, timeStamp
					InComingMessageViewHolder holderInMessage = new InComingMessageViewHolder();
					TextMessageView tv = new TextMessageView(parent.getContext(), (TextMessage) listItem);
					holderInMessage.avatarView = (AvatarView) tv.findViewById(R.id.avatar);
					holderInMessage.collapsible = tv.getCollapsibleContainer();
					holderInMessage.timestampView = (TextView) tv.findViewById(R.id.timestamp);
					convertView = tv;
					holderMessage = holderInMessage;
					break;
				}
				case TYPE_TXT_OUT: {
					// Outgoing message has: message, progressbar, timeStamp
					OutGoingMessageViewHolder holderOutMessage = new OutGoingMessageViewHolder();
					TextMessageView tv = new TextMessageView(parent.getContext(), (TextMessage) listItem);
					holderOutMessage.collapsible = tv.getCollapsibleContainer();
					holderOutMessage.progressBar = (ApptentiveMaterialIndeterminateProgressBar) tv.findViewById(R.id.progressBar);
					holderOutMessage.timestampView = (TextView) tv.findViewById(R.id.timestamp);
					holderOutMessage.mainLayout = (FrameLayout) tv.findViewById(R.id.outgoing_message_frame_bg);
					convertView = tv;
					holderMessage = holderOutMessage;
					break;
				}
				case TYPE_FILE_IN: {
					// Incoming File message has: avatar, file image, timeStamp
					InComingMessageViewHolder holderInMessage = new InComingMessageViewHolder();
					FileMessageView fv = new FileMessageView(parent.getContext(), (FileMessage) listItem);
					holderInMessage.avatarView = (AvatarView) fv.findViewById(R.id.avatar);
					holderInMessage.timestampView = (TextView) fv.findViewById(R.id.timestamp);
					holderInMessage.fileImageView = (ImageView) fv.findViewById(R.id.apptentive_file_message_image);
					convertView = fv;
					holderMessage = holderInMessage;
					break;
				}
				case TYPE_FILE_OUT: {
					// Outgoing File message has: file image, progressbar, timeStamp
					OutGoingMessageViewHolder holderOutMessage = new OutGoingMessageViewHolder();
					FileMessageView fv = new FileMessageView(parent.getContext(), (FileMessage) listItem);
					holderOutMessage.progressBar = (ApptentiveMaterialIndeterminateProgressBar) fv.findViewById(R.id.progressBar);
					holderOutMessage.timestampView = (TextView) fv.findViewById(R.id.timestamp);
					holderOutMessage.fileImageView = (ImageView) fv.findViewById(R.id.apptentive_file_message_image);
					holderOutMessage.mainLayout = (FrameLayout) fv.findViewById(R.id.outgoing_message_frame_bg);
					convertView = fv;
					holderMessage = holderOutMessage;
					break;
				}
				case TYPE_GREETING: {
					// Greeting message has: tile, body
					holderMessage = new MessageViewHolder();
					MessageCenterGreetingView gv = new MessageCenterGreetingView(parent.getContext(), (MessageCenterGreeting) listItem);
					holderMessage.messageBodyTextView = (TextView) gv.findViewById(R.id.body);
					holderMessage.messageTitleTextView = (TextView) gv.findViewById(R.id.title);
					convertView = gv;
					break;
				}
				case TYPE_STATUS: {
					// Greeting message has: tile, body
					holderMessage = new MessageViewHolder();
					MessageCenterStatusView sv = new MessageCenterStatusView(parent.getContext(), (MessageCenterStatus) listItem);
					holderMessage.messageBodyTextView = (TextView) sv.findViewById(R.id.body);
					holderMessage.messageTitleTextView = (TextView) sv.findViewById(R.id.title);
					convertView = sv;
					break;
				}
				case TYPE_Composing: {
					holderMessage = new MessageViewHolder();
					if (composingView == null) {
						composingView = new MessageCenterComposingView(context, position);
					}
					/*LayoutInflater inflater = LayoutInflater.from(context);
					View cv = inflater.inflate(R.layout.apptentive_message_center_composing, parent, false);
					et = (EditText) cv.findViewById(R.id.composing_et);
					et.setOnTouchListener(new View.OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_UP) {
								composingViewIndex = position;
							}
							return false;
						}
					});*/
					focusOnEditText();
					convertView = composingView;
					break;
				}
				default:
					break;
			}
			if (convertView != null) {
				convertView.setTag(holderMessage);
			}
		} else {
			holderMessage = (MessageViewHolder) convertView.getTag();
		}

		if (holderMessage != null) {
			String timestamp;
			switch (type) {
				case TYPE_TXT_IN:
					if (avatarCache == null) {
						startDownloadAvatarTask(((InComingMessageViewHolder) holderMessage).avatarView, ((TextMessage) listItem).getSenderProfilePhoto());
					}
					timestamp = createTimestamp(((TextMessage) listItem).getCreatedAt());
					((InComingMessageViewHolder) holderMessage).updateMessage(null, ((TextMessage) listItem).getBody(), timestamp, null, avatarCache);
					break;
				case TYPE_TXT_OUT: {
					Double sentTime = ((TextMessage) listItem).getCreatedAt();
					timestamp = createTimestamp(sentTime);
					((OutGoingMessageViewHolder) holderMessage).updateMessage(null, ((TextMessage) listItem).getBody(), (sentTime != null), isInPauseState, timestamp, null);
					break;
				}
				case TYPE_FILE_IN:
					if (avatarCache == null) {
						startDownloadAvatarTask(((InComingMessageViewHolder) holderMessage).avatarView, ((FileMessage) listItem).getSenderProfilePhoto());
					}
					if (position != holderMessage.position && position != pendingUpdateIndex) {
						pendingUpdateIndex = position;
						startLoadImageTask((FileMessage) listItem, position, holderMessage);
					}
					timestamp = createTimestamp(((FileMessage) listItem).getCreatedAt());
					((InComingMessageViewHolder) holderMessage).updateMessage(null, null, timestamp, null, avatarCache);
					break;
				case TYPE_FILE_OUT: {
					if (position != holderMessage.position && position != pendingUpdateIndex) {
						pendingUpdateIndex = position;
						startLoadImageTask((FileMessage) listItem, position, holderMessage);
					}
					Double sentTime = ((FileMessage) listItem).getCreatedAt();
					timestamp = createTimestamp(((FileMessage) listItem).getCreatedAt());
					((OutGoingMessageViewHolder) holderMessage).updateMessage(null, null, (sentTime != null), isInPauseState, timestamp, null);
					break;
				}
				case TYPE_GREETING:
					holderMessage.updateMessage(((MessageCenterGreeting) listItem).getTitle(), ((MessageCenterGreeting) listItem).getBody(), null, null);
					break;
				case TYPE_STATUS:
					holderMessage.updateMessage(((MessageCenterStatus) listItem).getTitle(), ((MessageCenterStatus) listItem).getBody(), null, null);
					break;
				case TYPE_Composing:
					//if (composingView != null) {
					//	focusOnEditText();
					//}
					break;
				default:
					return null;
			}
			holderMessage.position = position;
		}
		/*if (et!= null) {
			et.clearFocus();
			if (composingViewIndex != INVALID_POSITION && composingViewIndex == position) {
				et.requestFocus();
				et.setSelection(et.getText().length());
			}
		}*/
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

	public EditText getEditTextInComposing() {
		if (composingView != null) {
			return composingView.getEditText();
		}
		return null;
	}

	public void setPaused(boolean bPause) {
		isInPauseState = bPause;
	}

	public void focusOnEditText() {
		EditText et = composingView.getEditText();
		et.requestFocus();
	}
 public void clearComposing() {
	 composingView = null;
 }

	protected String createTimestamp(Double seconds) {
		if (seconds != null) {
			Date date = new Date(Math.round(seconds * 1000));
			DateFormat mediumDateShortTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
			return mediumDateShortTimeFormat.format(date);
		}
		return isInPauseState ? context.getResources().getString(R.string.apptentive_paused)
				: context.getResources().getString(R.string.apptentive_sending);
	}


	private Point getBitmapDimensions(StoredFile storedFile) {
		Point ret = null;
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(storedFile.getLocalFilePath());

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(fis, null, options);

			Point point = Util.getScreenSize(context);
			int maxImageWidth = (int) (MAX_IMAGE_SCREEN_PROPORTION_X * point.x);
			int maxImageHeight = (int) (MAX_IMAGE_SCREEN_PROPORTION_Y * point.x);
			maxImageWidth = maxImageWidth > MAX_IMAGE_DISPLAY_WIDTH ? MAX_IMAGE_DISPLAY_WIDTH : maxImageWidth;
			maxImageHeight = maxImageHeight > MAX_IMAGE_DISPLAY_HEIGHT ? MAX_IMAGE_DISPLAY_HEIGHT : maxImageHeight;
			float scale = ImageUtil.calculateBitmapScaleFactor(options.outWidth, options.outHeight, maxImageWidth, maxImageHeight);
			ret = new Point((int) (scale * options.outWidth), (int) (scale * options.outHeight));
		} catch (Exception e) {
			Log.e("Error opening stored file.", e);
		} finally {
			Util.ensureClosed(fis);
		}
		return ret;
	}

	private void startLoadImageTask(FileMessage message, int position, MessageViewHolder holder) {
		StoredFile storedFile = message.getStoredFile(context);
		String mimeType = storedFile.getMimeType();
		String imagePath;

		if (mimeType != null) {
			imagePath = storedFile.getLocalFilePath();
			if (mimeType.contains("image")) {
				holder.fileImageView.setVisibility(View.INVISIBLE);

				Point dimensions = getBitmapDimensions(storedFile);
				if (dimensions != null) {
					holder.fileImageView.setPadding(dimensions.x, dimensions.y, 0, 0);
				}
			}
			LoadImageTask task = new LoadImageTask(position, holder);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath);
			} else {
				task.execute(imagePath);
			}
		}

	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
		private int position;
		private WeakReference<MessageViewHolder> holderRef;

		public LoadImageTask(int position, MessageViewHolder holder) {
			this.position = position;
			this.holderRef = new WeakReference<>(holder);
		}

		@Override
		protected Bitmap doInBackground(String... paths) {
			FileInputStream fis = null;
			Bitmap imageBitmap = null;
			try {
				fis = context.openFileInput(paths[0]);
				Point point = Util.getScreenSize(context);
				int maxImageWidth = (int) (MAX_IMAGE_SCREEN_PROPORTION_X * point.x);
				int maxImageHeight = (int) (MAX_IMAGE_SCREEN_PROPORTION_Y * point.x);
				maxImageWidth = maxImageWidth > MAX_IMAGE_DISPLAY_WIDTH ? MAX_IMAGE_DISPLAY_WIDTH : maxImageWidth;
				maxImageHeight = maxImageHeight > MAX_IMAGE_DISPLAY_HEIGHT ? MAX_IMAGE_DISPLAY_HEIGHT : maxImageHeight;
				imageBitmap = ImageUtil.createScaledBitmapFromStream(fis, maxImageWidth, maxImageHeight, null);
				Log.v("Loaded bitmap and re-sized to: %d x %d", imageBitmap.getWidth(), imageBitmap.getHeight());
			} catch (Exception e) {
				Log.e("Error opening stored image.", e);
			} catch (OutOfMemoryError e) {
				// It's generally not a good idea to catch an OutOfMemoryException. But in this case, the OutOfMemoryException
				// had to result from allocating a bitmap, so the system should be in a good state.
				// TODO: Log an event to the server so we know an OutOfMemoryException occurred.
				Log.e("Ran out of memory opening image.", e);
			} finally {
				Util.ensureClosed(fis);
			}
			return imageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (null == bitmap) {
				return;
			}
			MessageViewHolder holder = holderRef.get();
			if (holder != null && holder.position == position) {
				if (position == pendingUpdateIndex) {
					pendingUpdateIndex = INVALID_POSITION;
				}
				holder.fileImageView.setPadding(0, 0, 0, 0);
				holder.fileImageView.setImageBitmap(bitmap);
				holder.fileImageView.setVisibility(View.VISIBLE);
			}
		}
	}

	private void startDownloadAvatarTask(AvatarView view, String imageUrl) {
		DownloadImageTask task = new DownloadImageTask(view);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
		} else {
			task.execute(imageUrl);
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<AvatarView> resultView;

		DownloadImageTask(AvatarView view) {
			resultView = new WeakReference<>(view);
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
			AvatarView view = resultView.get();
			if (view != null) {
				view.setImageBitmap(result);
			}
		}

		private Bitmap loadImageFromNetwork(String imageUrl) throws IOException {
			URL url = new URL(imageUrl);
			return BitmapFactory.decodeStream(url.openStream());
		}
	}
}
