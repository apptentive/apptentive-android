/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.model.FileMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.util.ImageUtil;
import com.apptentive.android.sdk.util.Util;

import java.io.FileInputStream;

/**
 * @author Sky Kelsey
 */
public class FileMessageView extends PersonalMessageView<FileMessage> {


	public FileMessageView(Context context, FileMessage message) {
		super(context, message);
	}

	protected void init(Context context, FileMessage message) {
		super.init(context, message);
		LayoutInflater inflater = LayoutInflater.from(context);
		FrameLayout bodyLayout = (FrameLayout) findViewById(R.id.body);
		inflater.inflate(R.layout.apptentive_message_body_file, bodyLayout);
	}

}
