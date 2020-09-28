package com.apptentive.android.sdk.external;

import android.content.Context;

import androidx.annotation.NonNull;

public interface InAppReviewManagerFactory {
    InAppReviewManager createReviewManager(@NonNull Context context);
}
