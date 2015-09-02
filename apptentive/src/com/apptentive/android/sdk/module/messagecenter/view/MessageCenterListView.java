/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.apptentive.android.sdk.R;


public class MessageCenterListView extends ListView {

	public interface ApptentiveMessageCenterListAdapter extends ListAdapter {
		/**
		 * True if views of given type will be sticky at the top
		 */
		boolean isItemSticky(int viewType);
	}

	/**
	 * Wrapper class for sticky view and its position in the list.
	 */
	static class StickyWrapper {
		public View view;
		public int position;
		public long id;
		public int additionalIndent;
	}

	private final Rect touchRect = new Rect();
	private final PointF touchPt = new PointF();
	private int touchSlop;
	private View touchTarget;
	private MotionEvent downEvent;

	// fields used for drawing shadow under the sticky header
	private GradientDrawable shadowDrawable;
	private int shadowHeight;

	// Optional delegating listener
	OnScrollListener delegateScrollListener;

	// shadow for being recycled
	StickyWrapper recycledHeaderView;

	/**
	 * shadow instance with a sticky view, can be null.
	 */
	StickyWrapper stickyWrapper;


	/**
	 * Scroll listener
	 */
	private final OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (delegateScrollListener != null) {
				delegateScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			if (delegateScrollListener != null) {
				delegateScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}

			ListAdapter adapter = getAdapter();
			if (adapter == null || visibleItemCount == 0) return; // nothing to do

			final boolean isFirstVisibleItemHeader =
					isItemSticky(adapter, adapter.getItemViewType(firstVisibleItem));

			if (isFirstVisibleItemHeader) {
				View headerView = getChildAt(0);
				int headerTop = headerView.getTop();
				int pad = getPaddingTop();
				if (headerTop == pad) {
					// view sticks to the top, do not render shadow
					destroyStickyShadow();
				} else {
					tryCreateShadowAtPosition(firstVisibleItem, firstVisibleItem, visibleItemCount);
				}

			} else {
				// header is not at the first visible position
				int headerPosition = findCurrentHeaderPosition(firstVisibleItem);
				if (headerPosition > -1) {
					tryCreateShadowAtPosition(headerPosition, firstVisibleItem, visibleItemCount);
				} else { // there is no section for the first visible item, destroy shadow
					destroyStickyShadow();
				}
			}
		}

	};

	/**
	 * Default change observer.
	 */
	private final DataSetObserver dataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			recreateStickyShadow();
		}

		@Override
		public void onInvalidated() {
			recreateStickyShadow();
		}
	};


	public MessageCenterListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public MessageCenterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		setOnScrollListener(mOnScrollListener);
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		initShadow(true);
	}


	public void initShadow(boolean visible) {
		if (visible) {
			if (shadowDrawable == null) {
				shadowDrawable = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.apptentive_listview_item_shadow);
				shadowHeight = (int) (8 * getResources().getDisplayMetrics().density);
			}
		} else {
			if (shadowDrawable != null) {
				shadowDrawable = null;
				shadowHeight = 0;
			}
		}
	}

	/**
	 * Create shadow wrapper with a sticky view  at given position
	 */
	void createStickyShadow(int position) {

		// recycle shadow
		StickyWrapper stickyViewShadow = recycledHeaderView;
		recycledHeaderView = null;

		// create new shadow, if needed
		if (stickyViewShadow == null) {
			stickyViewShadow = new StickyWrapper();
		}
		// request new view using recycled view, if such
		View stickyView = getAdapter().getView(position, stickyViewShadow.view, MessageCenterListView.this);

		// read layout parameters
		LayoutParams layoutParams = (LayoutParams) stickyView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = (LayoutParams) generateDefaultLayoutParams();
			stickyView.setLayoutParams(layoutParams);
		}

		View childLayout = ((ViewGroup) stickyView).getChildAt(0);
		int heightMode = MeasureSpec.getMode(layoutParams.height);
		int heightSize = MeasureSpec.getSize(layoutParams.height);

		if (heightMode == MeasureSpec.UNSPECIFIED) {
			heightMode = MeasureSpec.EXACTLY;
		}

		int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
		if (heightSize > maxHeight) {
			heightSize = maxHeight;
		}
		// assuming left and right additional paddings are the same
		int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
		int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
		stickyView.measure(ws, hs);
		stickyView.layout(0, 0, stickyView.getMeasuredWidth(), stickyView.getMeasuredHeight());

		// initialize shadow
		stickyViewShadow.view = stickyView;
		stickyViewShadow.position = position;
		stickyViewShadow.id = getAdapter().getItemId(position);
		stickyViewShadow.additionalIndent = childLayout.getPaddingLeft();

		stickyWrapper = stickyViewShadow;
	}

	/**
	 * Destroy shadow wrapper for current sticky view
	 */
	void destroyStickyShadow() {
		if (stickyWrapper != null) {
			// keep shadow for being recycled later
			recycledHeaderView = stickyWrapper;
			stickyWrapper = null;
		}
	}

	/**
	 * Create sticky shadowded view at a given item position.
	 */
	void tryCreateShadowAtPosition(int headerPosition, int firstVisibleItem, int visibleItemCount) {
		if (visibleItemCount < 1) {
			// no need for creating shadow if no visible item
			destroyStickyShadow();
			return;
		}

		if (stickyWrapper != null
				&& stickyWrapper.position != headerPosition) {
			// invalidate shadow, if required
			destroyStickyShadow();
		}

		if (stickyWrapper == null) {
			createStickyShadow(headerPosition);
		}

	}


	int findCurrentHeaderPosition(int fromPosition) {
		ListAdapter adapter = getAdapter();

		if (fromPosition >= adapter.getCount()) return -1; // dataset has changed, no candidate

		// Only need to look through to the next section item above
		for (int position = fromPosition; position >= 0; position--) {
			int viewType = adapter.getItemViewType(position);
			if (isItemSticky(adapter, viewType)) return position;
		}
		return -1;
	}

	void recreateStickyShadow() {
		destroyStickyShadow();
		ListAdapter adapter = getAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			int firstVisiblePosition = getFirstVisiblePosition();
			int headerPosition = findCurrentHeaderPosition(firstVisiblePosition);
			if (headerPosition == -1) {
				return;
			}
			tryCreateShadowAtPosition(headerPosition,
					firstVisiblePosition, getLastVisiblePosition() - firstVisiblePosition + 1);
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		if (listener == mOnScrollListener) {
			super.setOnScrollListener(listener);
		} else {
			delegateScrollListener = listener;
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		post(new Runnable() {
			@Override
			public void run() {
				// restore view after configuration change
				recreateStickyShadow();
			}
		});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// unregister observer at old adapter and register on new one
		ListAdapter oldAdapter = getAdapter();
		if (oldAdapter != null) {
			oldAdapter.unregisterDataSetObserver(dataSetObserver);
		}
		if (adapter != null) {
			adapter.registerDataSetObserver(dataSetObserver);
		}

		if (oldAdapter != adapter) {
			destroyStickyShadow();
		}

		super.setAdapter(adapter);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (stickyWrapper != null) {
			int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
			int shadowWidth = stickyWrapper.view.getWidth();
			if (parentWidth != shadowWidth) {
				recreateStickyShadow();
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (stickyWrapper != null) {

			int pLeft = getListPaddingLeft();
			int pTop = getListPaddingTop();
			View view = stickyWrapper.view;
			int headerTop = view.getTop();
			pLeft += stickyWrapper.additionalIndent;
			// draw child
			canvas.save();

			int clipHeight = view.getHeight() +
					(shadowDrawable == null ? 0 : shadowHeight);
			canvas.clipRect(pLeft, pTop, pLeft + view.getWidth() - 2 * stickyWrapper.additionalIndent, pTop + clipHeight);

			canvas.translate(pLeft - stickyWrapper.additionalIndent, pTop - headerTop);
			drawChild(canvas, stickyWrapper.view, getDrawingTime());

			if (shadowDrawable != null) {
				shadowDrawable.setBounds(stickyWrapper.view.getLeft(),
						stickyWrapper.view.getBottom(),
						stickyWrapper.view.getRight(),
						stickyWrapper.view.getBottom() + shadowHeight);
				shadowDrawable.draw(canvas);
			}

			canvas.restore();
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		final float x = ev.getX();
		final float y = ev.getY();
		final int action = ev.getAction();

		if (action == MotionEvent.ACTION_DOWN
				&& touchTarget == null
				&& stickyWrapper != null
				&& isStickyViewTouched(stickyWrapper.view, x, y)) {
			touchTarget = stickyWrapper.view;
			touchPt.x = x;
			touchPt.y = y;

			downEvent = MotionEvent.obtain(ev);
		}

		if (touchTarget != null) {
			if (isStickyViewTouched(touchTarget, x, y)) {
				// forward event to header view
				touchTarget.dispatchTouchEvent(ev);
			}

			if (action == MotionEvent.ACTION_UP) {
				super.dispatchTouchEvent(ev);
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_CANCEL) {
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_MOVE) {
				if (Math.abs(y - touchPt.y) > touchSlop) {

					MotionEvent event = MotionEvent.obtain(ev);
					event.setAction(MotionEvent.ACTION_CANCEL);
					touchTarget.dispatchTouchEvent(event);
					event.recycle();

					super.dispatchTouchEvent(downEvent);
					super.dispatchTouchEvent(ev);
					clearTouchTarget();

				}
			}

			return true;
		}

		return super.dispatchTouchEvent(ev);
	}

	private boolean isStickyViewTouched(View view, float x, float y) {
		view.getHitRect(touchRect);

		touchRect.bottom += getPaddingTop() - view.getTop();
		touchRect.left += getPaddingLeft();
		touchRect.right -= getPaddingRight();
		return touchRect.contains((int) x, (int) y);
	}

	private void clearTouchTarget() {
		touchTarget = null;
		if (downEvent != null) {
			downEvent.recycle();
			downEvent = null;
		}
	}


	public static boolean isItemSticky(ListAdapter adapter, int viewType) {
		if (adapter instanceof HeaderViewListAdapter) {
			adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		}
		return ((ApptentiveMessageCenterListAdapter) adapter).isItemSticky(viewType);
	}

}