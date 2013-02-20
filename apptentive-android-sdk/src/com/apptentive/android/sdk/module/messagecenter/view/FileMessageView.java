/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.model.FileMessage;

/**
 * @author Sky Kelsey
 */
public class FileMessageView extends MessageView<FileMessage> {

	public FileMessageView(Context context, FileMessage message) {
		super(context, message);
	}

	protected void init(FileMessage message) {
		super.init(message);
		LayoutInflater inflater = LayoutInflater.from(context);
		FrameLayout bodyLayout = (FrameLayout) findViewById(R.id.apptentive_message_body);
		inflater.inflate(R.layout.apptentive_message_body_file, bodyLayout);
	}

	public void updateMessage(final FileMessage newMessage) {
		FileMessage oldMessage = message;
		super.updateMessage(newMessage);

		boolean hasNoOldUri = oldMessage == null;
		boolean hasNewUri = newMessage != null && newMessage.getLocalUri() != null;
		boolean uriDiffers = newMessage != null && oldMessage != null && !newMessage.getLocalUri().equals(oldMessage.getLocalUri());
		if ((hasNoOldUri && hasNewUri) || hasNewUri && uriDiffers) {
			// TODO: Figure out a way to group into classes by mime type (image, text, other).
			String mimeType = newMessage.getMimeType();
			if(mimeType == null) {
				Log.e("FileMessage mime type is null.");
				return;
			}
			if (mimeType.contains("image")) {
				ImageView imageView = (ImageView) findViewById(R.id.apptentive_file_message_image);
				imageView.setImageURI(Uri.parse(newMessage.getLocalUri()));
				imageView.setVisibility(View.VISIBLE);
			} else {
				TextView textView = (TextView) findViewById(R.id.apptentive_file_message_text);
				textView.setVisibility(View.VISIBLE);
				if (mimeType.contains("text")) {
					// Set content
					// TODO: Populate this view with the file contents. Truncate to just a few hundred characters maybe?
				} else {
					textView.setText(newMessage.getMimeType());
				}
			}
		}
	}
}
