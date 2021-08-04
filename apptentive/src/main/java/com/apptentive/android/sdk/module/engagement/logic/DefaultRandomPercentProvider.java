package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;

import com.apptentive.android.sdk.util.ApplicationInfo;
import com.apptentive.android.sdk.util.RuntimeUtils;

import java.util.Random;

/**
 * A concrete implementation of [RandomPercentProvider] which only generates a random percent for a
 * give key once and stores it in shared preferences.
 */
public class DefaultRandomPercentProvider implements RandomPercentProvider {
    private final Context context;
    private final String id;

    /**
     * @param id - unique key for making a distinction between same keys used in different conversations.
     */
    public DefaultRandomPercentProvider(Context context, String id) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        this.context = context.getApplicationContext();
        this.id = id;
    }

    @Override
    public double getPercent(String key) {
        final SharedPreferences prefs = getPrefs(context);
        if (key == null) {
            return getRandomPercent();
        } else {
            final String prefsKey = id + "_" + key;
            if (prefs.contains(prefsKey)) {
                return prefs.getFloat(prefsKey, 0.0f);
            }
            final float percent = getRandomPercent();
            prefs.edit().putFloat(prefsKey, percent).apply();
            return percent;
        }
    }

    private float getRandomPercent() {
        ApplicationInfo applicationInfo = RuntimeUtils.getApplicationInfo(context);

        if (applicationInfo.isDebuggable()) {
            return (float) 50;
        } else {
            return new Random().nextFloat() * 100;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("com.apptentive.RandomPercentProvider", Context.MODE_PRIVATE);
    }

    @VisibleForTesting
    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}
