package com.apptentive.android.sdk.external;

import android.content.Context;

import androidx.annotation.NonNull;

class UnsupportedReviewManager implements InAppReviewManager {
    @Override
    public void launchReview(@NonNull Context context, InAppReviewListener callback) {
        callback.onReviewFlowFailed("In-app review is not supported");
    }

    @Override
    public boolean isInAppReviewSupported() {
        return false;
    }
}
