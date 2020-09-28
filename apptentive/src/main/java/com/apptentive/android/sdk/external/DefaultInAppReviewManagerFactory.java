package com.apptentive.android.sdk.external;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.apptentive.android.sdk.ApptentiveLog;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.apptentive.android.sdk.ApptentiveLogTag.IN_APP_REVIEW;
import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_INVALID;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_UPDATING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;

public class DefaultInAppReviewManagerFactory implements InAppReviewManagerFactory {
    private static final int MIN_ANDROID_API_VERSION = 21;

    public InAppReviewManager createReviewManager(@NonNull Context context) {
        try {
            if (Build.VERSION.SDK_INT < MIN_ANDROID_API_VERSION) {
                ApptentiveLog.e(IN_APP_REVIEW, "Unable to create InAppReviewManager: Android version is too low %d", Build.VERSION.SDK_INT);
                return new UnsupportedReviewManager();
            }

            int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (result != SUCCESS) {
                ApptentiveLog.e(IN_APP_REVIEW, "Unable to create InAppReviewManager: Google Play Services not available (%s)", getStatusMessage(result));
                return new UnsupportedReviewManager();
            }

            Activity activity = (Activity) context;
            GooglePlayReviewManager reviewManager = new GooglePlayReviewManager(activity);
            ApptentiveLog.d(IN_APP_REVIEW, "Initialized Google Play in-App review manager");
            return reviewManager;
        } catch (Exception e) {
            ApptentiveLog.e(IN_APP_REVIEW, e, "Unable to create in-app review manager");
        }

        return new UnsupportedReviewManager();
    }

    private static String getStatusMessage(int result) {
        switch (result) {
            case SERVICE_MISSING:
                return "SERVICE_MISSING";
            case SERVICE_UPDATING:
                return "SERVICE_UPDATING";
            case SERVICE_VERSION_UPDATE_REQUIRED:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case SERVICE_DISABLED:
                return "SERVICE_DISABLED";
            case SERVICE_INVALID:
                return "SERVICE_INVALID";
            default:
                return "unknown result: " + result;
        }
    }
}