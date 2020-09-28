package com.apptentive.android.sdk.external;

public interface InAppReviewListener {
    void onReviewFlowComplete();
    void onReviewFlowFailed(String message);
}
