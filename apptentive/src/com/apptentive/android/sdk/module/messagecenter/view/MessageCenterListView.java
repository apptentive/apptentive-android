/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */
package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;


public class MessageCenterListView extends ListView {

	public interface ApptentiveMessageCenterListAdapter extends ListAdapter {
		/**
		 * Return 'true' if views of given type will be sticky at the top
		 */
		boolean isItemSticky(int viewType);
	}

	/**
	 * Wrapper class for pinned section view and its position in the list.
	 */
	static class StickyGroup {
		public View view;
		public int position;
		public long id;
	}

	// fields used for handling touch events
	private final Rect mTouchRect = new Rect();
	private final PointF mTouchPoint = new PointF();
	private int mTouchSlop;
	private View mTouchTarget;
	private MotionEvent mDownEvent;

	// fields used for drawing shadow under a pinned section
	private GradientDrawable mShadowDrawable;
	private int mSectionsDistanceY;
	private int mShadowHeight;

	/**
	 * Optional delegating listener
	 */
	OnScrollListener mDelegateOnScrollListener;

	/**
	 * shadow for being recycled
	 */
	StickyGroup mRecycleSection;

	/**
	 * shadow instance with a sticky view, can be null.
	 */
	StickyGroup mStickyGroup;

	/**
	 * Sticky view Y-translation
	 */
	int mTranslateY;

	/**
	 * Scroll listener
	 */
	private final OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			if (mDelegateOnScrollListener != null) {
				mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}

			// get expected adapter or fail fast
			ListAdapter adapter = getAdapter();
			if (adapter == null || visibleItemCount == 0) return; // nothing to do

			final boolean isFirstVisibleItemSection =
					isItemSticky(adapter, adapter.getItemViewType(firstVisibleItem));

			if (isFirstVisibleItemSection) {
				View sectionView = getChildAt(0);
				int setcionTop = sectionView.getTop();
				int pad = getPaddingTop();
				if (setcionTop == pad) {
				// view sticks to the top, no need for pinned shadow
					destroyStickyShadow();
				} else {
				// section doesn't stick to the top, make sure we have a pinned shadow
					ensureShadowForPosition(firstVisibleItem, firstVisibleItem, visibleItemCount);
				}

			} else { // section is not at the first visible position
				int sectionPosition = findCurrentSectionPosition(firstVisibleItem);
				if (sectionPosition > -1) { // we have section position
					ensureShadowForPosition(sectionPosition, firstVisibleItem, visibleItemCount);
				} else { // there is no section for the first visible item, destroy shadow
					destroyStickyShadow();
				}
			}
		}

	};

	/**
	 * Default change observer.
	 */
	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			recreateStickyShadow();
		}

		;

		@Override
		public void onInvalidated() {
			recreateStickyShadow();
		}
	};

	//-- constructors

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
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		initShadow(true);
	}

	//-- public API methods

	public void setShadowVisible(boolean visible) {
		initShadow(visible);
		if (mStickyGroup != null) {
			View v = mStickyGroup.view;
			invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom() + mShadowHeight);
		}
	}

	//-- pinned section drawing methods

	public void initShadow(boolean visible) {
		if (visible) {
			if (mShadowDrawable == null) {
				mShadowDrawable = new GradientDrawable(Orientation.TOP_BOTTOM,
						new int[]{Color.parseColor("#ffa0a0a0"), Color.parseColor("#50a0a0a0"), Color.parseColor("#00a0a0a0")});
				mShadowHeight = (int) (8 * getResources().getDisplayMetrics().density);
			}
		} else {
			if (mShadowDrawable != null) {
				mShadowDrawable = null;
				mShadowHeight = 0;
			}
		}
	}

	/**
	 * Create shadow wrapper with a pinned view for a view at given position
	 */
	void createStickyShadow(int position) {

		// try to recycle shadow
		StickyGroup pinnedShadow = mRecycleSection;
		mRecycleSection = null;

		// create new shadow, if needed
		if (pinnedShadow == null) pinnedShadow = new StickyGroup();
		// request new view using recycled view, if such
		View pinnedView = getAdapter().getView(position, pinnedShadow.view, MessageCenterListView.this);

		// read layout parameters
		LayoutParams layoutParams = (LayoutParams) pinnedView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = (LayoutParams) generateDefaultLayoutParams();
			pinnedView.setLayoutParams(layoutParams);
		}

		View childLayout = ((ViewGroup) pinnedView).getChildAt(0);
		int heightMode = MeasureSpec.getMode(layoutParams.height);
		int heightSize = MeasureSpec.getSize(layoutParams.height);

		if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

		int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
		if (heightSize > maxHeight) heightSize = maxHeight;

		int widthSize = getWidth() - getListPaddingLeft() - getListPaddingRight() -
				childLayout.getPaddingLeft() - childLayout.getPaddingRight();
		// measure & layout
		int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight() -
				childLayout.getPaddingLeft() - childLayout.getPaddingRight(), MeasureSpec.EXACTLY);
		//int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
		int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
		pinnedView.measure(ws, hs);
		pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
		mTranslateY = 0;

		// initialize pinned shadow
		pinnedShadow.view = pinnedView;
		pinnedShadow.position = position;
		pinnedShadow.id = getAdapter().getItemId(position);

		// store pinned shadow
		mStickyGroup = pinnedShadow;
	}

	/**
	 * Destroy shadow wrapper for currently pinned view
	 */
	void destroyStickyShadow() {
		if (mStickyGroup != null) {
			// keep shadow for being recycled later
			mRecycleSection = mStickyGroup;
			mStickyGroup = null;
		}
	}

	/**
	 * Makes sure we have an actual pinned shadow for given position.
	 */
	void ensureShadowForPosition(int sectionPosition, int firstVisibleItem, int visibleItemCount) {
		if (visibleItemCount < 1) { // no need for creating shadow at all, we have a single visible item
			destroyStickyShadow();
			return;
		}

		if (mStickyGroup != null
				&& mStickyGroup.position != sectionPosition) { // invalidate shadow, if required
			destroyStickyShadow();
		}

		if (mStickyGroup == null) { // create shadow, if empty
			createStickyShadow(sectionPosition);
		}

		// align shadow according to next section position, if needed
		int nextPosition = sectionPosition + 1;
		int count = getCount();
		if (nextPosition < count) {
			int nextSectionPosition = findFirstVisibleSectionPosition(nextPosition,
					visibleItemCount - (nextPosition - firstVisibleItem));
			if (nextSectionPosition > -1) {
				View nextSectionView = getChildAt(nextSectionPosition - firstVisibleItem);
				final int bottom = mStickyGroup.view.getBottom() + getPaddingTop();
				mSectionsDistanceY = nextSectionView.getTop() - bottom;
				if (mSectionsDistanceY < 0) {
					// next section overlaps pinned shadow, move it up
					mTranslateY = mSectionsDistanceY;
				} else {
					// next section does not overlap with pinned, stick to top
					mTranslateY = 0;
				}
			} else {
				// no other sections are visible, stick to top
//				View header = getChildAt(sectionPosition - firstVisibleItem);
//				float headerTop = header.getTop();
//				float pinnedHeaderHeight = mStickyGroup.view.getMeasuredHeight();
//				if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
//					mTranslateY = (int) headerTop - header.getHeight();
//				} else {
				mTranslateY = 0;
//				}
				mSectionsDistanceY = Integer.MAX_VALUE;
			}
		}

	}

	int findFirstVisibleSectionPosition(int firstVisibleItem, int visibleItemCount) {
		ListAdapter adapter = getAdapter();

		int adapterDataCount = adapter.getCount();
		if (getLastVisiblePosition() >= adapterDataCount)
			return -1; // dataset has changed, no candidate

		if (firstVisibleItem + visibleItemCount >= adapterDataCount) {//added to prevent index Outofbound (in case)
			visibleItemCount = adapterDataCount - firstVisibleItem;
		}

		for (int childIndex = 0; childIndex < visibleItemCount; childIndex++) {
			int position = firstVisibleItem + childIndex;
			int viewType = adapter.getItemViewType(position);
			if (isItemSticky(adapter, viewType)) return position;
		}
		return -1;
	}

	int findCurrentSectionPosition(int fromPosition) {
		ListAdapter adapter = getAdapter();

		if (fromPosition >= adapter.getCount()) return -1; // dataset has changed, no candidate

		if (adapter instanceof SectionIndexer) {
			// try fast way by asking section indexer
			SectionIndexer indexer = (SectionIndexer) adapter;
			int sectionPosition = indexer.getSectionForPosition(fromPosition);
			int itemPosition = indexer.getPositionForSection(sectionPosition);
			int typeView = adapter.getItemViewType(itemPosition);
			if (isItemSticky(adapter, typeView)) {
				return itemPosition;
			} // else, no luck
		}

		// try slow way by looking through to the next section item above
		for (int position = fromPosition; position >= 0; position--) {
			int viewType = adapter.getItemViewType(position);
			if (isItemSticky(adapter, viewType)) return position;
		}
		return -1; // no candidate found
	}

	void recreateStickyShadow() {
		destroyStickyShadow();
		ListAdapter adapter = getAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			int firstVisiblePosition = getFirstVisiblePosition();
			int sectionPosition = findCurrentSectionPosition(firstVisiblePosition);
			if (sectionPosition == -1) return; // no views to pin, exit
			ensureShadowForPosition(sectionPosition,
					firstVisiblePosition, getLastVisiblePosition() - firstVisiblePosition + 1);
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		if (listener == mOnScrollListener) {
			super.setOnScrollListener(listener);
		} else {
			mDelegateOnScrollListener = listener;
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		post(new Runnable() {
			@Override
			public void run() { // restore pinned view after configuration change
				recreateStickyShadow();
			}
		});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {

		// unregister observer at old adapter and register on new one
		ListAdapter oldAdapter = getAdapter();
		if (oldAdapter != null) oldAdapter.unregisterDataSetObserver(mDataSetObserver);
		if (adapter != null) adapter.registerDataSetObserver(mDataSetObserver);

		// destroy pinned shadow, if new adapter is not same as old one
		if (oldAdapter != adapter) destroyStickyShadow();

		super.setAdapter(adapter);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mStickyGroup != null) {
			int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
			int shadowWidth = mStickyGroup.view.getWidth();
			if (parentWidth != shadowWidth) {
				recreateStickyShadow();
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mStickyGroup != null) {

			// prepare variables
			int pLeft = getListPaddingLeft();
			int pTop = getListPaddingTop();
			View view = mStickyGroup.view;
			View child = ((ViewGroup) mStickyGroup.view).getChildAt(0);
			pLeft += child.getPaddingLeft();
			// draw child
			canvas.save();

			int clipHeight = view.getHeight() +
					(mShadowDrawable == null ? 0 : Math.min(mShadowHeight, mSectionsDistanceY));
			canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop + clipHeight);

			canvas.translate(pLeft, pTop + mTranslateY);
			drawChild(canvas, mStickyGroup.view, getDrawingTime());

			if (mShadowDrawable != null && mSectionsDistanceY > 0) {
				mShadowDrawable.setBounds(mStickyGroup.view.getLeft(),
						mStickyGroup.view.getBottom(),
						mStickyGroup.view.getRight() + child.getPaddingLeft(),
						mStickyGroup.view.getBottom() + mShadowHeight);
				mShadowDrawable.draw(canvas);
			}

			canvas.restore();
		}
	}

	//-- touch handling methods

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		final float x = ev.getX();
		final float y = ev.getY();
		final int action = ev.getAction();

		if (action == MotionEvent.ACTION_DOWN
				&& mTouchTarget == null
				&& mStickyGroup != null
				&& isStickyViewTouched(mStickyGroup.view, x, y)) { // create touch target

			// user touched pinned view
			mTouchTarget = mStickyGroup.view;
			mTouchPoint.x = x;
			mTouchPoint.y = y;

			// copy down event for eventually be used later
			mDownEvent = MotionEvent.obtain(ev);
		}

		if (mTouchTarget != null) {
			if (isStickyViewTouched(mTouchTarget, x, y)) { // forward event to pinned view
				mTouchTarget.dispatchTouchEvent(ev);
			}

			if (action == MotionEvent.ACTION_UP) { // perform onClick on pinned view
				super.dispatchTouchEvent(ev);
				//performStickyItemClick();
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_CANCEL) { // cancel
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_MOVE) {
				if (Math.abs(y - mTouchPoint.y) > mTouchSlop) {

					// cancel sequence on touch target
					MotionEvent event = MotionEvent.obtain(ev);
					event.setAction(MotionEvent.ACTION_CANCEL);
					mTouchTarget.dispatchTouchEvent(event);
					event.recycle();

					// provide correct sequence to super class for further handling
					super.dispatchTouchEvent(mDownEvent);
					super.dispatchTouchEvent(ev);
					clearTouchTarget();

				}
			}

			return true;
		}

		// call super if this was not our pinned view
		return super.dispatchTouchEvent(ev);
	}

	private boolean isStickyViewTouched(View view, float x, float y) {
		view.getHitRect(mTouchRect);

		mTouchRect.top += mTranslateY;

		mTouchRect.bottom += mTranslateY + getPaddingTop();
		mTouchRect.left += getPaddingLeft();
		mTouchRect.right -= getPaddingRight();
		return mTouchRect.contains((int) x, (int) y);
	}

	private void clearTouchTarget() {
		mTouchTarget = null;
		if (mDownEvent != null) {
			mDownEvent.recycle();
			mDownEvent = null;
		}
	}

	private boolean performStickyItemClick() {
		if (mStickyGroup == null) return false;

		OnItemClickListener listener = getOnItemClickListener();
		if (listener != null && getAdapter().isEnabled(mStickyGroup.position)) {
			View view = mStickyGroup.view;
			if (view != null) {
				//view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
			}
			listener.onItemClick(this, view, mStickyGroup.position, mStickyGroup.id);
			return true;
		}
		return false;
	}

	public static boolean isItemSticky(ListAdapter adapter, int viewType) {
		if (adapter instanceof HeaderViewListAdapter) {
			adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		}
		return ((ApptentiveMessageCenterListAdapter) adapter).isItemSticky(viewType);
	}

}