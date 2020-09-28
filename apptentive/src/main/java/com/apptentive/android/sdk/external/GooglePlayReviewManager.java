package com.apptentive.android.sdk.external;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import static com.apptentive.android.sdk.ApptentiveLogTag.IN_APP_REVIEW;

class GooglePlayReviewManager implements InAppReviewManager {
    private final ReviewManager reviewManager;

    public GooglePlayReviewManager(Activity activity) {
        reviewManager = ReviewManagerFactory.create(activity);
    }

    @Override
    public void launchReview(@NonNull final Context context, final InAppReviewListener callback) {
        final Activity activity = ObjectUtils.as(context, Activity.class);
        if (activity == null) {
            notifyFailure(callback, null, "Failed to launch in-app review flow: make sure you pass Activity object into your Apptentive.engage() calls.");
            return;
        }

        try {
            launchReviewGuarded(callback, activity);
        } catch (Exception e) {
            notifyFailure(callback, e, "Exception while launching in-app review flow");
        }
    }

    private void launchReviewGuarded(final InAppReviewListener callback, final Activity activity) {
        final long startTime = System.currentTimeMillis();

        ApptentiveLog.d(IN_APP_REVIEW, "Requesting in-app review...");

        Task<ReviewInfo> task = reviewManager.requestReviewFlow();
        task.addOnSuccessListener(new OnSuccessListener<ReviewInfo>() {
            @Override
            public void onSuccess(ReviewInfo result) {
                final long elapsedTime = System.currentTimeMillis() - startTime;
                try {
                    ApptentiveLog.d(IN_APP_REVIEW, "ReviewInfo request complete (took %d ms). Launching review flow...", elapsedTime);
                    launchReviewFlow(activity, result, callback);
                } catch (Exception e) {
                    notifyFailure(callback, e, "Failed to launch review flow (took %d ms): make sure your device has Google Play Services installed.", elapsedTime);
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                notifyFailure(callback, e, "Failed to request ReviewInfo (took %d ms)", elapsedTime);
            }
        });
    }

    private void launchReviewFlow(@NonNull final Activity activity, ReviewInfo reviewInfo, final InAppReviewListener callback) {
        final long startTime = System.currentTimeMillis();

        Task<Void> task = reviewManager.launchReviewFlow(activity, reviewInfo);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime < 1000L) {
                    notifyFailure(callback, null, "In-app review flow completed too fast (%d ms) and we have good reasons to believe it just failed silently.", elapsedTime); // FIXME: better error message
                } else {
                    ApptentiveLog.d(IN_APP_REVIEW, "In-app review complete (took %d ms)", elapsedTime);
                    callback.onReviewFlowComplete();
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                notifyFailure(callback, e, "Unable to launch in-app review (took %d ms)", elapsedTime);
            }
        });
    }

    private void notifyFailure(InAppReviewListener listener, @Nullable Throwable e, String format, Object... args) {
        final String message = StringUtils.format(format, args);
        ApptentiveLog.e(IN_APP_REVIEW, e, message);
        listener.onReviewFlowFailed(message);
    }

    @Override
    public boolean isInAppReviewSupported() {
        return true;
    }
}
