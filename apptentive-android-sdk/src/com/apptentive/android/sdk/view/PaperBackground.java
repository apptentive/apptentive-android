/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.apptentive.android.sdk.R;

/**
 * @author Sky Kelsey
 */
public class PaperBackground extends View {
	private Paint mRoundEdgePaint;
	private Bitmap mBitmap;
	private Rect mSize;
	private RectF mSizeF;
	private Drawable mPaperDrawable;
	private Paint mDuffPaint;

	public PaperBackground(Context context, AttributeSet attributeSet){
		super(context, attributeSet);
		
		mDuffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mDuffPaint.setColor(Color.RED);
		mDuffPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.DST_IN));
		
		mRoundEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRoundEdgePaint.setColor(Color.RED);
		mSize = new Rect();
		mSizeF = new RectF();

		mBitmap = null;

		Resources res = getResources();
		mPaperDrawable = res.getDrawable(R.drawable.apptentive_paper_bg);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if (changed) {
			mBitmap = null;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth()+1;
		int height = getHeight()+1;
		mSize.set(0, 0, width, height);

		// Use masking
		if (mBitmap == null) {
			mSizeF.set(mSize);
			mBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			Canvas paperCanvas = new Canvas(mBitmap);
			paperCanvas.drawRoundRect(mSizeF, 15.0f, 15.0f, mRoundEdgePaint);
		}
		
		mPaperDrawable.setBounds(mSize);
		mPaperDrawable.draw(canvas);

		canvas.drawBitmap(mBitmap, 0, 0, mDuffPaint);
	}
}
