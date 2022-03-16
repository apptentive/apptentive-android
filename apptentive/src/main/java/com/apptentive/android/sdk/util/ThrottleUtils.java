package com.apptentive.android.sdk.util;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

import java.util.concurrent.TimeUnit;


public class ThrottleUtils {

    public ThrottleUtils(Long ratingThrottle, SharedPreferences globalSharedPrefs) {
        ratingThrottleLength = ratingThrottle;
        sharedPrefs = globalSharedPrefs;
    }

    private final Long ratingThrottleLength;
    private final SharedPreferences sharedPrefs;

    private final long defaultThrottleLength = TimeUnit.SECONDS.toMillis(1);

    @SuppressLint("ApplySharedPref")
    public boolean shouldThrottleInteraction(Interaction.Type interactionType) {
        String interactionName = interactionType.name();
        long currentTime = System.currentTimeMillis();
        long interactionLastThrottled = sharedPrefs.getLong(interactionName, 0);
        boolean interactionIsRating = (interactionType == Interaction.Type.InAppRatingDialog
                || interactionType == Interaction.Type.RatingDialog);

        if ((interactionIsRating && (currentTime - interactionLastThrottled) < ratingThrottleLength)) {
            logThrottle(interactionName, ratingThrottleLength, currentTime, interactionLastThrottled);
            return true;
        } else if (!interactionIsRating && (currentTime - interactionLastThrottled) < defaultThrottleLength) {
            logThrottle(interactionName, defaultThrottleLength, currentTime, interactionLastThrottled);
            return true;
        } else {
            sharedPrefs.edit().putLong(interactionName, currentTime).commit();
            return false;
        }
    }

    public boolean shouldThrottleResetConversation() {
        String sdkVersion = sharedPrefs.getString(Constants.PREF_KEY_THROTTLE_VERSION, "");
        String apptentiveSDKVersion = Constants.getApptentiveSdkVersion();

        if (sdkVersion.isEmpty() || !sdkVersion.equals(apptentiveSDKVersion)) {
            ApptentiveLog.d("Conversation reset not throttled");
            sharedPrefs.edit().putString(Constants.PREF_KEY_THROTTLE_VERSION, Constants.getApptentiveSdkVersion()).apply();
            return false;
        } else {
            ApptentiveLog.d("Conversation reset throttled");
            return true;
        }
    }

    private void logThrottle(String interactionName, Long throttleLength, Long currentTime, Long lastThrottledTime) {
        ApptentiveLog.w(interactionName + " throttled. Throttle length is " + throttleLength +
                "ms. Can be shown again in " + (currentTime - lastThrottledTime) + "ms.");
    }
}
