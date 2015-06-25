/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.metric.MetricModule;

import java.io.IOException;
import java.net.URL;


/**
 * @author Sky Kelsey
 */
public class ApptentiveAvatarView extends ImageView {

	Rect viewRect;

	int border;
	int borderColor;
	int borderRadius;
	Paint borderPaint;

	Bitmap avatar;
	Rect avatarRect;
	int avatarWidth;
	int avatarHeight;
	Matrix shaderMatrix;
	BitmapShader shader;
	Paint shaderPaint;
	int imageRadius;

	public ApptentiveAvatarView(Context context) {
		super(context);
	}

	public ApptentiveAvatarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ApptentiveAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ApptentiveAvatarView, defStyleAttr, 0);
		try {
			border = attributes.getDimensionPixelSize(R.styleable.ApptentiveAvatarView_border, 0);
			borderColor = attributes.getColor(R.styleable.ApptentiveAvatarView_borderColor, Color.BLACK);
		} finally {
			attributes.recycle();
		}
		setup();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (getDrawable() != null) {
			if (border > 0) {
				canvas.drawCircle(getWidth() / 2, getHeight() / 2, borderRadius, borderPaint);
			}
			canvas.drawCircle(getWidth() / 2, getHeight() / 2, imageRadius, shaderPaint);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		avatar = bm;
		setup();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		avatar = getBitmapFromDrawable(drawable);
		setup();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		avatar = getBitmapFromDrawable(getDrawable());
		setup();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		avatar = getBitmapFromDrawable(getDrawable());
		setup();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setup();
	}

	private Bitmap getBitmapFromDrawable(Drawable d) {
		if (d == null) {
			return null;
		}

		if (d instanceof BitmapDrawable) {
			return ((BitmapDrawable) d).getBitmap();
		} else {
			try {
				Bitmap b;

				if (d instanceof ColorDrawable) {
					b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
				} else {
					b = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				}

				Canvas canvas = new Canvas(b);
				d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
				d.draw(canvas);
				return b;
			} catch (OutOfMemoryError e) {
				Log.w("Error creating bitmap.", e);
				return null;
			}
		}
	}

	protected synchronized void setup() {
		if (avatar == null) {
			return;
		}
		avatarWidth = avatar.getWidth();
		avatarHeight = avatar.getHeight();
		shader = new BitmapShader(avatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		if (shaderPaint == null) {
			shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		}
		shaderPaint.setShader(shader);

		if (borderPaint == null) {
			borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			borderPaint.setStyle(Paint.Style.STROKE);
		}
		borderPaint.setColor(borderColor);
		borderPaint.setStrokeWidth(border);

		if (viewRect == null) {
			viewRect = new Rect();
		}
		viewRect.set(0, 0, getWidth(), getHeight());

		// Painting using STROKE style is measured from the center of the line, so include border in the radius calculation.
		borderRadius = (int) (((float) Math.min(viewRect.width(), viewRect.height()) - border) / 2);

		if (avatarRect == null) {
			avatarRect = new Rect();
		}
		avatarRect.set(0, 0, viewRect.width() - border, viewRect.height() - border);

		// The image radius will now be smaller by half the border.
		int halfBorder = (int) ((float) border / 2);
		imageRadius = borderRadius - halfBorder;

		// setup the matrix
		if (shaderMatrix == null) {
			shaderMatrix = new Matrix();
		}
		shaderMatrix.set(null);


		ImageScale imageScale = scaleImage(avatarWidth, avatarHeight, avatarRect.width(), avatarRect.height());
		shaderMatrix.setScale(imageScale.scale, imageScale.scale);
		shaderMatrix.postTranslate(imageScale.deltaX + 0.5f + border, imageScale.deltaY + 0.5f + border);

		shader.setLocalMatrix(shaderMatrix);
		invalidate();
	}


	public void fetchImage(final String urlString) {
		if (urlString == null) {
			return;
		}
		Thread thread = new Thread() {
			public void run() {
				Bitmap bitmap = null;
				try {
					URL url = new URL(urlString);
					bitmap = BitmapFactory.decodeStream(url.openStream());
				} catch (IOException e) {
					Log.d("Error opening avatar from URL: \"%s\"", e, urlString);
				}
				if (bitmap != null) {
					final Bitmap finalBitmap = bitmap;
					post(new Runnable() {
						public void run() {
							setImageBitmap(finalBitmap);
						}
					});
				}
			}
		};
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Log.w("UncaughtException in AvatarView.", throwable);
				MetricModule.sendError(getContext().getApplicationContext(), throwable, null, null);
			}
		};
		thread.setUncaughtExceptionHandler(handler);
		thread.setName("Apptentive-AvatarView.fetchImage()");
		thread.start();

	}

	/**
	 * This scales the image so that it fits within the container. The container may have empty space
	 * at the ends, but the entire image will be displayed.
	 */
	private ImageScale scaleImage(int imageX, int imageY, int containerX, int containerY) {
		ImageScale ret = new ImageScale();
		// Compare aspects faster by multiplying out the divisors.
		if (imageX * containerY > imageY * containerX) {
			// Image aspect wider than container
			ret.scale = (float) containerX / imageX;
			ret.deltaY = ((float) containerY - (ret.scale * imageY)) / 2.0f;
		} else {
			// Image aspect taller than container
			ret.scale = (float) containerY / imageY;
			ret.deltaX = ((float) containerX - (ret.scale * imageX)) / 2.0f;
		}
		return ret;
	}

	private class ImageScale {
		public float scale = 1.0f;
		public float deltaX = 0.0f;
		public float deltaY = 0.0f;

		@Override
		public String toString() {
			return String.format("scale = %f, deltaX = %f, deltaY = %f", scale, deltaX, deltaY);
		}
	}
}
