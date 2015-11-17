/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.os.AsyncTask;
import android.widget.EditText;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.model.ApptentiveMessage;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterComposingItem;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterGreeting;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterStatus;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil;
import com.apptentive.android.sdk.module.messagecenter.model.MessageCenterUtil.MessageCenterListItem;
import com.apptentive.android.sdk.module.messagecenter.view.holder.AutomatedMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.HolderFactory;
import com.apptentive.android.sdk.module.messagecenter.view.holder.IncomingCompoundMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.MessageCenterListItemHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.OutgoingCompoundMessageHolder;
import com.apptentive.android.sdk.module.messagecenter.view.holder.StatusHolder;
import com.apptentive.android.sdk.util.AnimationUtil;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.ImageUtil;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class MessageAdapter<T extends MessageCenterUtil.MessageCenterListItem> extends ArrayAdapter<T>
		implements MessageCenterListView.ApptentiveMessageCenterListAdapter {

	private static final int
			TYPE_COMPOUND_INCOMING = 0,
			TYPE_COMPOUND_OUTGOING = 1,
			TYPE_GREETING = 2,
			TYPE_STATUS = 3,
			TYPE_AUTO = 4,
			TYPE_COMPOSING_AREA = 5,
			TYPE_COMPOSING_BAR = 6,
			TYPE_WHOCARD = 7;

	private static final int INVALID_POSITION = -1;

	private final static float MAX_IMAGE_SCREEN_PROPORTION_X = 0.5f;
	private final static float MAX_IMAGE_SCREEN_PROPORTION_Y = 0.6f;

	// Some absolute size limits to keep bitmap sizes down.
	private final static int MAX_IMAGE_DISPLAY_WIDTH = 800;
	private final static int MAX_IMAGE_DISPLAY_HEIGHT = 800;

	// If message sending is paused or not
	private boolean isInPauseState = false;

	private Context activityContext;

	private MessageCenterInteraction interaction;

	// Variables used in composing message
	private int composingViewIndex = INVALID_POSITION;
	private MessageCenterComposingView composingView;
	private EditText composingEditText;
	private boolean updateComposingViewImageBand = true;

	private MessageCenterComposingActionBarView composingActionBarView;

	private boolean forceShowKeyboard = true;

	// Variables used in Who Card
	private int whoCardViewIndex = INVALID_POSITION;
	private boolean focusOnNameField = false;
	private MessageCenterWhoCardView whoCardView;
	private EditText emailEditText;
	private EditText nameEditText;

	// Variables to track showing animation on incoming/outgoing messages
	private int lastAnimatedMessagePosition;
	private boolean showMessageAnimation;

	private boolean showComposingBarAnimation = true;

	// maps to prevent redundant asynctasks
	private ArrayList<Integer> positionsWithPendingUpdateTask = new ArrayList<Integer>();

	private OnListviewItemActionListener composingActionListener;

	public interface OnListviewItemActionListener {
		void onComposingViewCreated();

		void onComposingBarCreated();

		void beforeComposingTextChanged(CharSequence str);

		void onComposingTextChanged(CharSequence str);

		void afterComposingTextChanged(String str);

		void onCancelComposing();

		void onFinishComposing();

		void onWhoCardViewCreated(EditText nameEt, EditText emailEt);

		void onSubmitWhoCard(String buttonLabel);

		void onCloseWhoCard(String buttonLabel);

		void onAttachImage();

		void onClickAttachment(int position, ImageItem image);
	}

	public MessageAdapter(Context activityContext, List<MessageCenterListItem> items, OnListviewItemActionListener listener, MessageCenterInteraction interaction) {
		super(activityContext, 0, (List<T>) items);
		this.activityContext = activityContext;
		this.composingActionListener = listener;
		this.interaction = interaction;
	}

	@Override
	public int getItemViewType(int position) {
		MessageCenterListItem listItem = getItem(position);
		if (listItem instanceof ApptentiveMessage) {
			ApptentiveMessage apptentiveMessage = (ApptentiveMessage) listItem;
			if (apptentiveMessage.getBaseType() == Payload.BaseType.message) {
				switch (apptentiveMessage.getType()) {
					case CompoundMessage:
						if (apptentiveMessage.isAutomatedMessage()) {
							return TYPE_AUTO;
						} else if (!apptentiveMessage.isOutgoingMessage()) {
							return TYPE_COMPOUND_INCOMING;
						} else {
							return TYPE_COMPOUND_OUTGOING;
						}
					default:
						break;
				}
			}
		} else if (listItem instanceof MessageCenterGreeting) {
			return TYPE_GREETING;
		} else if (listItem instanceof MessageCenterStatus) {
			return TYPE_STATUS;
		} else if (listItem instanceof MessageCenterComposingItem) {
			if (((MessageCenterComposingItem) listItem).getType() ==
					MessageCenterComposingItem.COMPOSING_ITEM_AREA) {
				return TYPE_COMPOSING_AREA;
			} else if (((MessageCenterComposingItem) listItem).getType() ==
					MessageCenterComposingItem.COMPOSING_ITEM_ACTIONBAR) {
				return TYPE_COMPOSING_BAR;
			} else {
				return TYPE_WHOCARD;
			}
		}
		return IGNORE_ITEM_VIEW_TYPE;
	}

	@Override
	public int getViewTypeCount() {
		return 8;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view;
		showMessageAnimation = false;

		MessageCenterListItem listItem = getItem(position);
		int type = getItemViewType(position);
		MessageCenterListItemHolder holder = null;
		boolean bLoadAvatar = false;
		if (null == convertView) {
			// TODO: Do we need this switch anymore?
			switch (type) {
				case TYPE_COMPOUND_INCOMING:
					view = new CompoundMessageView(parent.getContext(), (CompoundMessage) listItem, composingActionListener);
					bLoadAvatar = true;
					break;
				case TYPE_COMPOUND_OUTGOING:
					view = new CompoundMessageView(parent.getContext(), (CompoundMessage) listItem, composingActionListener);
					break;
				case TYPE_GREETING: {
					MessageCenterGreeting greeting = (MessageCenterGreeting) listItem;
					MessageCenterGreetingView newView = new MessageCenterGreetingView(parent.getContext(), greeting);
					ImageUtil.startDownloadAvatarTask(newView.avatar,
							((MessageCenterGreeting) listItem).avatar);
					view = newView;
					break;
				}
				case TYPE_STATUS: {
					MessageCenterStatusView newView = new MessageCenterStatusView(parent.getContext());
					view = newView;
					break;
				}
				case TYPE_COMPOSING_AREA: {
					if (composingView == null) {
						composingView = new MessageCenterComposingView(activityContext, (MessageCenterComposingItem) listItem, composingActionListener);
						setupComposingView(position);
					}
					view = composingView;
					break;
				}
				case TYPE_COMPOSING_BAR: {
					if (composingActionBarView == null) {
						composingActionBarView = new MessageCenterComposingActionBarView(activityContext, (MessageCenterComposingItem) listItem, composingActionListener);
					}
					showComposingBarAnimation();
					view = composingActionBarView;
					break;
				}
				case TYPE_WHOCARD: {
					if (whoCardView == null) {
						whoCardView = new MessageCenterWhoCardView(activityContext, composingActionListener);
						whoCardView.updateUi((MessageCenterComposingItem) listItem, Apptentive.getPersonName(activityContext),
								Apptentive.getPersonEmail(activityContext));
						setupWhoCardView(position);
					}
					view = whoCardView;
					break;
				}
				case TYPE_AUTO:
					view = new AutomatedMessageView(parent.getContext(), (CompoundMessage) listItem);
					break;
				default:
					view = null;
					Log.i("Unrecognized type: %d", type);
					break;
			}
			if (view != null) {
				holder = HolderFactory.createHolder((MessageCenterListItemView) view);
				view.setTag(holder);
			}
		} else {
			/* System may recycle the view after composing view
			** is removed and recreated
			 */
			if (type == TYPE_COMPOSING_AREA) {
				if (composingView == null) {
					updateComposingViewImageBand = true;
					composingView = (MessageCenterComposingView) convertView;
					setupComposingView(position);
				} else if (updateComposingViewImageBand) {
					updateComposingViewImageBand = false;
				}
				view = composingView;
			} else if (type == TYPE_WHOCARD) {
				if (whoCardView == null) {
					whoCardView = (MessageCenterWhoCardView) convertView;
					whoCardView.updateUi((MessageCenterComposingItem) listItem, Apptentive.getPersonName(activityContext),
							Apptentive.getPersonEmail(activityContext));
					setupWhoCardView(position);
				}
				view = whoCardView;
			} else if (type == TYPE_COMPOSING_BAR) {
				composingActionBarView = (MessageCenterComposingActionBarView) convertView;
				showComposingBarAnimation();
				view = composingActionBarView;
			} else {
				view = convertView;
				holder = (MessageCenterListItemHolder) convertView.getTag();
			}
		}

		if (holder != null) {
			switch (type) {
				case TYPE_COMPOUND_INCOMING: {
					showMessageAnimation = true;
					if (bLoadAvatar) {
						ImageUtil.startDownloadAvatarTask(((IncomingCompoundMessageHolder) holder).avatar,
								((CompoundMessage) listItem).getSenderProfilePhoto());
					}
					final CompoundMessage compoundMessage = (CompoundMessage) listItem;
					String datestamp = ((CompoundMessage) listItem).getDatestamp();
					View container = view.findViewById(R.id.apptentive_compound_message_body_container);
					int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
					view.measure(widthMeasureSpec, 0);
					int viewWidth = container.getMeasuredWidth();
					((IncomingCompoundMessageHolder) holder).updateMessage(compoundMessage.getSenderUsername(),
							datestamp, compoundMessage.getBody(), viewWidth - container.getPaddingLeft() - container.getPaddingRight(),
							activityContext.getResources().getInteger(R.integer.apptentive_image_grid_default_column_number_incoming),
							compoundMessage.getRemoteAttachments());
					if (!compoundMessage.isRead() && !positionsWithPendingUpdateTask.contains(position)) {
						positionsWithPendingUpdateTask.add(position);
						startUpdateUnreadMessageTask(compoundMessage, position);
					}
					break;
				}
				case TYPE_COMPOUND_OUTGOING: {
					showMessageAnimation = true;

					CompoundMessage textMessage = (CompoundMessage) listItem;
					String datestamp = textMessage.getDatestamp();
					Double createdTime = textMessage.getCreatedAt();
					List<StoredFile> files = textMessage.getAssociatedFiles(activityContext);
					String messageBody = textMessage.getBody();
					String status;
					boolean bShowProgress;
					if (createdTime == null || createdTime > Double.MIN_VALUE) {
						status = createStatus(createdTime, textMessage.isLastSent());
						// show progress bar if: 1. no sent time set, and 2. not paused, and 3. have either text or files to sent
						bShowProgress = createdTime == null && !isInPauseState && (files != null || !TextUtils.isEmpty(messageBody));
					} else {
						status = activityContext.getResources().getString(R.string.apptentive_failed);
						bShowProgress = false;
					}
					View container = view.findViewById(R.id.apptentive_compound_message_body_container);
					int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
					view.measure(widthMeasureSpec, 0);
					int viewWidth = container.getMeasuredWidth();
					int statusTextColor = getStatusColor(createdTime);
					((OutgoingCompoundMessageHolder) holder).updateMessage(datestamp, status, statusTextColor,
							bShowProgress, messageBody, viewWidth - container.getPaddingLeft() - container.getPaddingRight(),
							activityContext.getResources().getInteger(R.integer.apptentive_image_grid_default_column_number), files);
					break;
				}
				case TYPE_STATUS: {
					MessageCenterStatus status = (MessageCenterStatus) listItem;
					((StatusHolder) holder).updateMessage(status.body, status.icon);
					break;
				}
				case TYPE_AUTO: {
					CompoundMessage autoMessage = (CompoundMessage) listItem;
					String dateStamp = autoMessage.getDatestamp();
					((AutomatedMessageHolder) holder).updateMessage(dateStamp, autoMessage);
					break;
				}
				default:
					return null;
			}
			holder.position = position;
		}
		if (composingEditText != null) {
			if (composingViewIndex != INVALID_POSITION && composingViewIndex == position) {
				composingEditText.requestFocus();
			}
		} else if (nameEditText != null) {
			if (whoCardViewIndex != INVALID_POSITION && whoCardViewIndex == position) {
				if (focusOnNameField) {
					nameEditText.requestFocus();
				} else {
					emailEditText.requestFocus();
				}
			}
		}
		if (showMessageAnimation && position > lastAnimatedMessagePosition) {
			AnimatorSet set = AnimationUtil.buildListViewRowShowAnimator(view, new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					view.invalidate();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			}, null);
			set.start();
			lastAnimatedMessagePosition = position;
		}
		return view;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	private void showComposingBarAnimation() {
		if (showComposingBarAnimation) {
			AnimatorSet set = AnimationUtil.buildListViewRowShowAnimator(composingActionBarView, new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					composingActionListener.onComposingBarCreated();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			}, null);
			set.start();
			showComposingBarAnimation = false;
		}
	}

	public Parcelable getWhoCardNameState() {
		if (whoCardView != null) {
			return whoCardView.getNameField().onSaveInstanceState();
		}
		return null;
	}

	public Parcelable getWhoCardEmailState() {
		if (whoCardView != null) {
			return whoCardView.getEmailField().onSaveInstanceState();
		}
		return null;
	}

	public String getWhoCardAvatarFileName() {
		return null;
	}

	public EditText getEditTextInComposing() {
		if (composingView != null) {
			return composingView.getEditText();
		}
		return null;
	}

	private void setupComposingView(final int position) {
		composingEditText = composingView.getEditText();
		composingViewIndex = position;
		composingEditText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					composingViewIndex = position;
				}
				return false;
			}
		});
		AnimatorSet set = AnimationUtil.buildListViewRowShowAnimator(composingView, new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (forceShowKeyboard) {
					Util.showSoftKeyboard((Activity) activityContext, composingEditText);
				}
				if (updateComposingViewImageBand) {
					updateComposingViewImageBand = false;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		}, null);
		set.start();
		composingActionListener.onComposingViewCreated();
	}

	public void clearComposing() {
		// Composing view may be recylcled for later usage. Clear the content from previous usage
		if (composingEditText != null) {
			composingEditText.setText("");
			composingEditText = null;
		}
		if (composingView != null) {
			composingView.clearImageAttachmentBand();
		}
		composingView = null;
		composingViewIndex = INVALID_POSITION;
		showComposingBarAnimation = true;
		updateComposingViewImageBand = true;
	}

	private void setupWhoCardView(final int position) {
		emailEditText = whoCardView.getEmailField();
		whoCardViewIndex = position;
		focusOnNameField = true;
		emailEditText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					whoCardViewIndex = position;
					focusOnNameField = false;
				}
				return false;
			}
		});
		nameEditText = whoCardView.getNameField();
		if (nameEditText.getVisibility() == View.VISIBLE) {
			nameEditText.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						whoCardViewIndex = position;
						focusOnNameField = true;
					}
					return false;
				}
			});
		} else {
			focusOnNameField = false;
		}
		AnimatorSet set = AnimationUtil.buildListViewRowShowAnimator(whoCardView, new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (forceShowKeyboard) {
					if (focusOnNameField) {
						Util.showSoftKeyboard((Activity) activityContext, nameEditText);
					} else {
						Util.showSoftKeyboard((Activity) activityContext, emailEditText);
					}
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		}, null);
		set.start();
		composingActionListener.onWhoCardViewCreated(nameEditText, emailEditText);
	}

	public View getWhoCardView() {
		return whoCardView;
	}

	public View getComposingAreaView() {
		return composingView;
	}

	public MessageCenterComposingActionBarView getComposingActionBarView() {
		return composingActionBarView;
	}

	public void clearWhoCard() {
		whoCardView = null;
		emailEditText = null;
		nameEditText = null;
		whoCardViewIndex = INVALID_POSITION;
	}

	public void setPaused(boolean bPause) {
		isInPauseState = bPause;
	}

	public void setForceShowKeyboard(boolean bVal) {
		forceShowKeyboard = bVal;
	}

	public void addImagestoComposer(final List<ImageItem> images) {
		composingView.addImagesToImageAttachmentBand(images);
		notifyDataSetChanged();
	}

	public void removeImageFromComposer(int position) {
		composingView.removeImageFromImageAttachmentBand(position);
		notifyDataSetChanged();
	}

	protected String createStatus(Double seconds, boolean showSent) {
		if (seconds == null) {
			return isInPauseState ? activityContext.getResources().getString(R.string.apptentive_failed) : null;
		}
		return (showSent) ? activityContext.getResources().getString(R.string.apptentive_sent) : null;
	}

	protected int getStatusColor(Double seconds) {
		if (seconds == null) {
			// failed color (red)
			return isInPauseState ? Util.getThemeColorFromAttrOrRes(activityContext, R.attr.apptentive_material_selected_text,
					R.color.apptentive_material_selected_text) : 0;
		}
		// other status color
		return Util.getThemeColorFromAttrOrRes(activityContext, R.attr.apptentive_material_disabled_text,
				R.color.apptentive_material_disabled_text);
	}

	private Point getBitmapDimensions(StoredFile storedFile) {
		Point ret = null;
		FileInputStream fis = null;
		try {
			fis = activityContext.openFileInput(storedFile.getLocalFilePath());

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(fis, null, options);

			Point point = Util.getScreenSize(activityContext.getApplicationContext());
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

	private void startUpdateUnreadMessageTask(CompoundMessage message, int position) {
		UpdateUnreadMessageTask task = new UpdateUnreadMessageTask(position);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
		} else {
			task.execute(message);
		}
	}

	@Override
	public boolean isItemSticky(int viewType) {
		return (viewType == TYPE_COMPOSING_BAR);
	}

	private class UpdateUnreadMessageTask extends AsyncTask<CompoundMessage, Void, Void> {
		private int position;

		public UpdateUnreadMessageTask(int position) {
			this.position = position;
		}

		@Override
		protected Void doInBackground(CompoundMessage... textMessages) {
			textMessages[0].setRead(true);
			JSONObject data = new JSONObject();
			try {
				data.put("message_id", textMessages[0].getId());
				data.put("message_type", textMessages[0].getType().name());
			} catch (JSONException e) {
				//
			}
			if (activityContext instanceof Activity) {
				EngagementModule.engageInternal((Activity) activityContext, interaction, MessageCenterInteraction.EVENT_NAME_READ, data.toString());
			}
			MessageManager.updateMessage(activityContext.getApplicationContext(), textMessages[0]);
			MessageManager.notifyHostUnreadMessagesListeners(MessageManager.getUnreadMessageCount(activityContext.getApplicationContext()));
			return null;
		}

		@Override
		protected void onCancelled() {
			positionsWithPendingUpdateTask.remove(new Integer(position));
		}

		@Override
		protected void onPostExecute(Void result) {
			positionsWithPendingUpdateTask.remove(new Integer(position));
		}

	}

	private void startLoadAttachedImageTask(CompoundMessage message, int position, OutgoingCompoundMessageHolder holder) {
		List<StoredFile> storedFiles = message.getAssociatedFiles(activityContext.getApplicationContext());
		if (storedFiles == null || storedFiles.size() == 0) {
			return;
		}
		for (int i = 0; i < storedFiles.size(); i++) {
			StoredFile storedFile = storedFiles.get(i);
			String mimeType = storedFile.getMimeType();
			String imagePath;

			if (mimeType != null) {
				imagePath = storedFile.getLocalFilePath();
				if (mimeType.contains("image")) {
					//holder.image.setVisibility(View.INVISIBLE);

					Point dimensions = getBitmapDimensions(storedFile);
					if (dimensions != null) {
						//holder.image.setPadding(dimensions.x, dimensions.y, 0, 0);
					}
				}
				LoadAttachedImageTask task = new LoadAttachedImageTask(position, holder);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath);
				} else {
					task.execute(imagePath);
				}
			}
		}
	}

	private class LoadAttachedImageTask extends AsyncTask<String, Void, Bitmap> {
		private int position;
		private WeakReference<OutgoingCompoundMessageHolder> holderRef;

		public LoadAttachedImageTask(int position, OutgoingCompoundMessageHolder holder) {
			this.position = position;
			this.holderRef = new WeakReference<OutgoingCompoundMessageHolder>(holder);
		}

		@Override
		protected Bitmap doInBackground(String... paths) {
			Bitmap imageBitmap = null;
			try {
				Point point = Util.getScreenSize(activityContext.getApplicationContext());
				int maxImageWidth = (int) (MAX_IMAGE_SCREEN_PROPORTION_X * point.x);
				int maxImageHeight = (int) (MAX_IMAGE_SCREEN_PROPORTION_Y * point.x);
				maxImageWidth = maxImageWidth > MAX_IMAGE_DISPLAY_WIDTH ? MAX_IMAGE_DISPLAY_WIDTH : maxImageWidth;
				maxImageHeight = maxImageHeight > MAX_IMAGE_DISPLAY_HEIGHT ? MAX_IMAGE_DISPLAY_HEIGHT : maxImageHeight;
				// Loading image from File Store. Pass 0 for orientation because images have been rotated when stored
				imageBitmap = ImageUtil.createScaledBitmapFromLocalImageSource(activityContext, paths[0], maxImageWidth, maxImageHeight, null, 0);
				Log.v("Loaded bitmap and re-sized to: %d x %d", imageBitmap.getWidth(), imageBitmap.getHeight());
			} catch (Exception e) {
				Log.e("Error opening stored image.", e);
			} catch (OutOfMemoryError e) {
				// It's generally not a good idea to catch an OutOfMemoryException. But in this case, the OutOfMemoryException
				// had to result from allocating a bitmap, so the system should be in a good state.
				// TODO: Log an event to the server so we know an OutOfMemoryException occurred.
				Log.e("Ran out of memory opening image.", e);
			}
			return imageBitmap;
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (null == bitmap) {
				return;
			}
			OutgoingCompoundMessageHolder holder = holderRef.get();
			if (holder != null && holder.position == position) {
				//Todo: load image into imageview
			}
		}
	}

}
