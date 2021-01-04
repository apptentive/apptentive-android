package com.apptentive.android.sdk.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;

public final class NestedScrollViewHelper {
    public static void scrollToChild(NestedScrollView scrollView, View childView) {
        int scrollPos = findTopRelativeToParent(scrollView, childView);
        scrollView.smoothScrollTo(0, scrollPos);
    }

    private static int findTopRelativeToParent(ViewGroup parent, View child) {
        int top = child.getTop();

        View directParent = ((View) child.getParent());
        boolean isDirectChild = (directParent.getId() == parent.getId());

        while (!isDirectChild) {
            top += directParent.getTop();
            directParent = ((View) directParent.getParent());
            isDirectChild = (directParent.getId() == parent.getId());
        }

        return top;
    }
}
