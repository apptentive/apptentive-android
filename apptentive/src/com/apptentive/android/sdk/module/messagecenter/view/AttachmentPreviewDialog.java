/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.ImageUtil;
import com.apptentive.android.sdk.util.image.PreviewImageView;


import java.io.File;
import java.io.IOException;

/**
 * @author Barry Li
 */
public class AttachmentPreviewDialog extends DialogFragment implements DialogInterface.OnDismissListener,
		PreviewImageView.GestureCallback {

	private View previewContainer;
	private ProgressBar progressBar;
	private PreviewImageView previewImageView;
	private ViewGroup header;
	private ImageButton closeButton;
	private int width;
	private int height;


	private ImageItem currentImage;

	public static AttachmentPreviewDialog newInstance(ImageItem image) {
		AttachmentPreviewDialog dialog = new AttachmentPreviewDialog();
		Bundle args = new Bundle();
		args.putParcelable("image", image);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.apptentive_dialog_image_preview, container);
		previewContainer = rootView.findViewById(R.id.preview_container);
		progressBar = (ProgressBar) rootView.findViewById(R.id.preview_progress);
		previewImageView = (PreviewImageView) rootView.findViewById(R.id.preview_image);

		previewImageView.setGestureCallback(this);
		header = (ViewGroup) rootView.findViewById(R.id.header_bar);
		closeButton = (ImageButton) header.findViewById(R.id.close_dialog);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// show the progress bar while we load content...
		progressBar.setVisibility(View.VISIBLE);

		currentImage = getArguments().getParcelable("image");

		AsyncTask<Object, Void, Bitmap> task = new AsyncTask<Object, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				return prepareAttachmentPreview(currentImage);
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				progressBar.setVisibility(View.GONE);
				if (!isAdded()) {
					return;
				}
				displayPreview(bitmap);
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}

		width = inflater.getContext().getResources().getDisplayMetrics().widthPixels;
		height = inflater.getContext().getResources().getDisplayMetrics().heightPixels;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
		previewContainer.setLayoutParams(lp);


		getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				//Clear the not focusable flag from the window
				getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

				//Update the WindowManager with the new attributes
				WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(getDialog().getWindow().getAttributes());
				lp.gravity = Gravity.BOTTOM;
				lp.width = WindowManager.LayoutParams.MATCH_PARENT;
				lp.height = WindowManager.LayoutParams.MATCH_PARENT;
				wm.updateViewLayout(getDialog().getWindow().getDecorView(), lp);
			}
		});

		return rootView;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.getWindow().getAttributes().windowAnimations = R.style.ApptentiveDialogAnimation;

		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.gravity = Gravity.BOTTOM;
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


	public Bitmap prepareAttachmentPreview(ImageItem imageItem) {

		// Show a preview of the image.
		Bitmap preview = null;
		String imagePathString = imageItem.originalPath;
		// Always try to load preview from cached file
		if (!TextUtils.isEmpty(imageItem.localCachePath)) {
			File imageFile = new File(imageItem.localCachePath);
			if (imageFile.exists()) {
				imagePathString = imageItem.localCachePath;
			}
		}

		if (imagePathString == null) {
			return null;
		}

		// Retrieve image orientation
		int imageOrientation = 0;
		try {
			if (imagePathString != null) {
				ExifInterface exif = new ExifInterface(imagePathString);
				imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			}
		} catch (IOException e) {

		}
		try {
			//final int dialogWidth
			preview = ImageUtil.createScaledBitmapFromLocalImageSource(getContext(), imagePathString, width, height, null, imageOrientation);
		} catch (Throwable e) {
			//ignore
		}

		return preview;
	}


	private void displayPreview(Bitmap preview) {
		previewContainer.setVisibility(View.VISIBLE);
		if (preview != null) {
			previewImageView.setImageBitmap(preview);
		}
	}

}
