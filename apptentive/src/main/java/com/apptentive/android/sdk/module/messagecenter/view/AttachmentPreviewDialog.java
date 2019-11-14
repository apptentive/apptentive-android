/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.DialogFragment;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.image.ApptentiveAttachmentLoader;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.PreviewImageView;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;
import static com.apptentive.android.sdk.util.Util.guarded;


public class AttachmentPreviewDialog extends DialogFragment implements DialogInterface.OnDismissListener, PreviewImageView.GestureCallback {

	private static final String KEY_IMAGE = "image";
	private static final String KEY_CONVERSATION_TOKEN = "token";

	private String conversationToken;
	private View previewContainer;
	private ProgressBar progressBar;
	private PreviewImageView previewImageView;
	private ImageView previewImagePlaceholderView;
	private ViewGroup header;
	private ImageButton closeButton;
	private int width;
	private int height;


	private ImageItem currentImage;

	public static AttachmentPreviewDialog newInstance(ImageItem image, String conversationToken) {
		AttachmentPreviewDialog dialog = new AttachmentPreviewDialog();
		Bundle args = new Bundle();
		args.putParcelable(KEY_IMAGE, image);
		args.putString(KEY_CONVERSATION_TOKEN, conversationToken);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ApptentiveTheme_Base_Versioned_TranslucentStatus_FullScreen);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.apptentive_dialog_image_preview, container);
		try {
			previewContainer = rootView.findViewById(R.id.preview_container);
			progressBar = (ProgressBar) rootView.findViewById(R.id.preview_progress);
			previewImageView = (PreviewImageView) rootView.findViewById(R.id.preview_image);
			previewImagePlaceholderView = (ImageView) rootView.findViewById(R.id.preview_image_placeholder);

			previewImageView.setGestureCallback(this);
			header = (ViewGroup) rootView.findViewById(R.id.header_bar);
			closeButton = (ImageButton) header.findViewById(R.id.close_dialog);
			closeButton.setOnClickListener(guarded(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			}));

			// show the progress bar while we load content...
			progressBar.setVisibility(View.VISIBLE);

			currentImage = getArguments().getParcelable(KEY_IMAGE);
			conversationToken = getArguments().getString(KEY_CONVERSATION_TOKEN);


			width = inflater.getContext().getResources().getDisplayMetrics().widthPixels;
			height = inflater.getContext().getResources().getDisplayMetrics().heightPixels;
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
			previewContainer.setLayoutParams(lp);


			ApptentiveAttachmentLoader.getInstance().load(conversationToken, currentImage.originalPath, currentImage.localCachePath, 0, previewImageView, width, height, true,
					new ApptentiveAttachmentLoader.LoaderCallback() {
						@Override
						public void onLoaded(ImageView view, int pos, Bitmap d) {
							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}

							if (previewImageView == view) {
								previewContainer.setVisibility(View.VISIBLE);
								if (!d.isRecycled()) {
									previewImageView.setImageBitmap(d);
									previewImagePlaceholderView.setVisibility(View.GONE);
								}
							}
						}

						@Override
						public void onLoadTerminated() {
							if (progressBar != null) {
								progressBar.setVisibility(View.GONE);
							}
						}

						@Override
						public void onDownloadStart() {
							if (progressBar != null) {
								progressBar.setVisibility(View.VISIBLE);
							}
						}

						@Override
						public void onDownloadProgress(int progress) {
						}
					});
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception in %s.onCreateView()", AttachmentPreviewDialog.class.getSimpleName());
			logException(e); // TODO: add more context info
		}

		return rootView;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().getAttributes().windowAnimations = R.style.ApptentiveDialogAnimation;

		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.gravity = Gravity.BOTTOM;
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		dialog.getWindow().setAttributes(lp);
		return dialog;
	}


	@Override
	public void onSingleTapDetected() {
		if (closeButton.getVisibility() == View.GONE) {
			closeButton.setVisibility(View.VISIBLE);
		} else {
			closeButton.setVisibility(View.GONE);
		}
	}

	@Override
	public void onFlingDetected() {
		dismiss();
	}


}
