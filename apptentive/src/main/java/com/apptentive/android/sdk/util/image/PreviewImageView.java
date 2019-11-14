/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.image;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

public class PreviewImageView extends AppCompatImageView implements OnScaleGestureListener,
		OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener

{

	public static final float SCALE_MAX = 4.0f;
	/**
	 * Initial scale factor: less than 1 if the image width/height is greater than screen width/height
	 */
	private float initScale = 1.0f;

	/**
	 * for matrix scale/translate
	 */
	private final float[] matrixValues = new float[9];

	private boolean once = true;

	/**
	 * detect scale gesture
	 */
	private ScaleGestureDetector scaleGestureDetector = null;

	/**
	 * detect translate gesture and tap gesture
	 */
	private GestureDetectorCompat gestureDetector = null;

	private final Matrix scaleMatrix = new Matrix();

	/**
	 * callback when tap is detected
	 */
	private GestureCallback externalCallback;

	private int touchSlop;

	private int lastPointerCount;

	private float lastX, lastY;

	private boolean isCanDrag, isCheckTopAndBottom, isCheckLeftAndRight;

	/**
	 * Callback Interface
	 */
	public interface GestureCallback {
		void onSingleTapDetected();

		void onFlingDetected();

	}

	public PreviewImageView(Context context) {
		this(context, null);
	}

	public PreviewImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setScaleType(ScaleType.MATRIX);
		scaleGestureDetector = new ScaleGestureDetector(context, this);
		gestureDetector = new GestureDetectorCompat(context, new PreviewGestureListener());
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		super.setScaleType(ScaleType.MATRIX);
		this.setOnTouchListener(this);
	}

	public void setGestureCallback(final GestureCallback callback) {
		externalCallback = callback;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();

		if (getDrawable() == null)
			return true;

		/**
		 * Scale range check
		 */
		if ((scale < SCALE_MAX && scaleFactor > 1.0f)
				|| (scale >= initScale && scaleFactor < 1.0f)) {

			if (scaleFactor * scale < initScale) {
				scaleFactor = initScale / scale;
			}
			if (scaleFactor * scale > SCALE_MAX) {
				scaleFactor = SCALE_MAX / scale;
			}
			/**
			 * Set scaling into matrix
			 */
			scaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());
			checkBorderAndCenterWhenScale();
			setImageMatrix(scaleMatrix);
		}
		return true;

	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		scaleGestureDetector.onTouchEvent(event);

		float x = 0, y = 0;
		// Get multiple touch points
		final int pointerCount = event.getPointerCount();
		// Calculate average x and y
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x = x / pointerCount;
		y = y / pointerCount;

		/**
		 * Reset lastX and lastY
		 */
		if (pointerCount != lastPointerCount) {
			isCanDrag = false;
			lastX = x;
			lastY = y;
		}


		lastPointerCount = pointerCount;

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				float dx = x - lastX;
				float dy = y - lastY;

				if (!isCanDrag) {
					isCanDrag = isCanDrag(dx, dy);
				}
				if (isCanDrag) {
					RectF rectF = getMatrixRectF();
					if (getDrawable() != null) {
						isCheckLeftAndRight = isCheckTopAndBottom = true;
						// No left/right translation if image width is less than screen width
						if (rectF.width() < getWidth()) {
							dx = 0;
							isCheckLeftAndRight = false;
						}
						// No Up/Down translation if image height is less than screen height
						if (rectF.height() < getHeight()) {
							dy = 0;
							isCheckTopAndBottom = false;
						}
						scaleMatrix.postTranslate(dx, dy);
						checkMatrixBounds();
						setImageMatrix(scaleMatrix);
					}
				}
				lastX = x;
				lastY = y;
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				lastPointerCount = 0;
				break;
		}
		return true;
	}


	/**
	 * Get current scale factor (x/y universal scaling)
	 *
	 * @return
	 */
	public final float getScale() {
		scaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		if (once) {
			Drawable d = getDrawable();
			if (d == null)
				return;

			int viewWidth = getWidth();
			int viewHeight = getHeight();
			// Get image width and height
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();
			float scale = 1.0f;
			// When image width/height is greater than the imageView
			if (dw > viewWidth && dh <= viewHeight) {
				scale = viewWidth * 1.0f / dw;
			}
			if (dh > viewHeight && dw <= viewWidth) {
				scale = viewHeight * 1.0f / dh;
			}
			// If both width and height greater than the imageView, find the smaller scale
			if (dw > viewWidth && dh > viewHeight) {
				scale = Math.min(viewWidth * 1.0f / dw, viewHeight * 1.0f / dh);
			}
			initScale = scale;
			// Center the image
			scaleMatrix.postTranslate((viewWidth - dw) / 2, (viewHeight - dh) / 2);
			scaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
			setImageMatrix(scaleMatrix);
			once = false;
		} else {
			checkBorderAndCenterWhenScale();
			setImageMatrix(scaleMatrix);
		}
	}

	/**
	 * Prevent visual artifact when scaling
	 */
	private void checkBorderAndCenterWhenScale() {

		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		if (rect.width() >= width) {
			if (rect.left > 0) {
				deltaX = -rect.left;
			}
			if (rect.right < width) {
				deltaX = width - rect.right;
			}
		}
		if (rect.height() >= height) {
			if (rect.top > 0) {
				deltaY = -rect.top;
			}
			if (rect.bottom < height) {
				deltaY = height - rect.bottom;
			}
		}
		// Always center the image when it's smaller than the imageView
		if (rect.width() < width) {
			deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
		}
		if (rect.height() < height) {
			deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
		}

		scaleMatrix.postTranslate(deltaX, deltaY);

	}

	/**
	 * Get image boundary from matrix
	 *
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = scaleMatrix;
		RectF rect = new RectF();
		Drawable d = getDrawable();
		if (null != d) {
			rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect;
	}

	/**
	 * Check image bounday against imageView
	 */
	private void checkMatrixBounds() {
		RectF rect = getMatrixRectF();

		float deltaX = 0, deltaY = 0;
		final float viewWidth = getWidth();
		final float viewHeight = getHeight();
		// Check if image boundary exceeds imageView boundary
		if (rect.top > 0 && isCheckTopAndBottom) {
			deltaY = -rect.top;
		}
		if (rect.bottom < viewHeight && isCheckTopAndBottom) {
			deltaY = viewHeight - rect.bottom;
		}
		if (rect.left > 0 && isCheckLeftAndRight) {
			deltaX = -rect.left;
		}
		if (rect.right < viewWidth && isCheckLeftAndRight) {
			deltaX = viewWidth - rect.right;
		}
		scaleMatrix.postTranslate(deltaX, deltaY);
	}

	private boolean isCanDrag(float dx, float dy) {
		return Math.sqrt((dx * dx) + (dy * dy)) >= touchSlop;
	}

	private class PreviewGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			externalCallback.onSingleTapDetected();
			return true;
		}

	}
}