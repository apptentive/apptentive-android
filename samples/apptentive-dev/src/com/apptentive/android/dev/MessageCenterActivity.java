/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class MessageCenterActivity extends ApptentiveActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_center);
	}

	public void clearCustomData(@SuppressWarnings("unused") View view) {
		((TextView) findViewById(R.id.add_custom_device_data_key)).setText("");
		((TextView) findViewById(R.id.add_custom_device_data_value)).setText("");
	}

	public void showMessageCenter(@SuppressWarnings("unused") View view) {
		String key = ((TextView) findViewById(R.id.add_custom_device_data_key)).getText().toString();
		String value = ((TextView) findViewById(R.id.add_custom_device_data_value)).getText().toString();

		Map<String, String> customData = null;
		if (!Util.isEmpty(key)) {
			customData = new HashMap<String, String>();
			customData.put(key, value);
		}
		clearCustomData(null);
		Apptentive.showMessageCenter(this, customData);
	}

	public void sendAttachmentImage(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		this.startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER);
	}

	public void sendAttachmentFile(View view) {
		EditText text = (EditText) findViewById(R.id.attachment_file_content);
		byte[] bytes = text.getText().toString().getBytes();
		Apptentive.sendAttachmentFile(this, bytes, "text/plain");
		text.setText("");
	}

	public void sendAttachmentText(View view) {
		EditText text = (EditText) findViewById(R.id.attachment_text);
		Apptentive.sendAttachmentText(this, text.getText().toString());
		text.setText("");
	}

	public void doSendAttachment(Uri uri) {
		Apptentive.sendAttachmentFile(this, uri.toString());
	}

	public void alternateDoSendAttachment(Context context, Uri uri) {

		ContentResolver resolver = context.getContentResolver();
		String mimeType = resolver.getType(uri);

		InputStream is = null;
		try {
			is = new BufferedInputStream(context.getContentResolver().openInputStream(uri));
			Apptentive.sendAttachmentFile(this, is, mimeType);
		} catch (FileNotFoundException e) {
			Log.e("Not found.", e);
		} finally {
			Util.ensureClosed(is);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case Constants.REQUEST_CODE_PHOTO_FROM_MESSAGE_CENTER:
					doSendAttachment(data.getData());
					break;
				default:
					break;
			}
		}
	}
}
