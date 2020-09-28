package com.apptentive.android.sdk.external;

import android.content.Context;

import androidx.annotation.NonNull;

public interface InAppReviewManager {
    void launchReview(@NonNull Context context, InAppReviewListener callback);

    boolean isInAppReviewSupported();
}
