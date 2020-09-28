package com.apptentive.android.sdk.external;

import android.content.Context;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Callback;

import java.util.Map;

public interface Engagement {
    void engageInternal(Context context, Interaction interaction, String eventName, @Nullable Map<String, Object> data);
    void launchInteraction(Context context, String interactionId, Callback<Boolean> callback);
}
