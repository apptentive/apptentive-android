/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.rating.view.ApptentiveBaseDialog;
import com.apptentive.android.sdk.util.image.ImageItem;
import com.apptentive.android.sdk.util.image.ImageUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sky Kelsey
 */
public class AttachmentPreviewDialog extends ApptentiveBaseDialog {

	private OnActionButtonListener listener;

	public AttachmentPreviewDialog(Context context) {
		super(context, R.layout.apptentive_message_center_attachment_preview);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ImageButton no = (ImageButton) findViewById(R.id.no);
		ImageButton yes = (ImageButton) findViewById(R.id.yes);
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (AttachmentPreviewDialog.this.listener != null) {
					listener.onTakeAction();
					dismiss();
				}
			}
		});
	}

	public boolean setImage(ImageItem imageItem) {
		ImageView image = (ImageView) findViewById(R.id.image);
		// Show a thumbnail version of the image.
		InputStream is = null;
		final Bitmap thumbnail;
		try {
			String imagePathString = null;
			// Always try to load preview from cached file
			if (!TextUtils.isEmpty(imageItem.localCachePath)) {
				File imageFile = new File(imageItem.localCachePath);
        if (imageFile.exists()) {
					is = new FileInputStream(imageFile);
					imagePathString = imageItem.localCachePath;
				}
			}
			// If no cache, load from the original originalPath
      if (is == null) {
				if (imageItem.time == 0) {
					is = getContext().getContentResolver().openInputStream(Uri.parse(imageItem.originalPath));
				} else {
					File imageFile = new File(imageItem.originalPath);
					if (imageFile.exists()) {
						is = new FileInputStream(imageFile);
						imagePathString = imageItem.originalPath;
					}
				}
			}

			if (is == null) {
				return false;
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


			//final int dialogWidth
			if (is != null) {
				thumbnail = ImageUtil.createLightweightScaledBitmapFromStream(is, 320, 300, null, imageOrientation);
			} else {
				thumbnail = null;
			}

		} catch (FileNotFoundException e) {
			// TODO: Error toast?
			return false;
		} finally {
			Util.ensureClosed(is);
		}

		if (thumbnail == null) {
			return false;
		}

		image.setImageBitmap(thumbnail);
		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				thumbnail.recycle();
				System.gc();
			}
		});
		return true;
	}


	public void setOnAttachmentAcceptedListener(OnActionButtonListener listener) {
		this.listener = listener;
	}

	public void hideActionButton () {
		ImageButton no = (ImageButton) findViewById(R.id.no);
		no.setVisibility(View.GONE);
	}

	public interface OnActionButtonListener {
		void onTakeAction();
	}
}
