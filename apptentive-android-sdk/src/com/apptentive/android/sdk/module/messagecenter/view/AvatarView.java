/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * @author Sky Kelsey
 * TODO: Save the final bitmap so it's not constructed each time onDraw is called.
 */
public class AvatarView extends View {

	private int avatar;

	public AvatarView(Context context){
		super(context);
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth()+1;
		int height = getHeight()+1;
		RectF rect = new RectF(1, 1, width-1, height-1);

		Matrix matrix = new Matrix();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.YELLOW);

		Bitmap image = BitmapFactory.decodeResource(getResources(), avatar);
		Bitmap duplicate = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas maskCanvas = new Canvas(mask);
		maskCanvas.drawRoundRect(rect, 20.0f, 20.0f, paint);

		matrix.setScale((float)width / image.getWidth(), (float)height / image.getHeight());

		Canvas dupCanvas = new Canvas(duplicate);
		dupCanvas.drawBitmap(image, matrix, null);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		dupCanvas.drawBitmap(mask, 0, 0, paint);

		canvas.drawBitmap(duplicate, 0, 0, null);
	}
}
