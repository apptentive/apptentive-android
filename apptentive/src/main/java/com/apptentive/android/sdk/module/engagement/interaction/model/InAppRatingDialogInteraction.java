package com.apptentive.android.sdk.module.engagement.interaction.model;

import androidx.annotation.Nullable;

import org.json.JSONException;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class InAppRatingDialogInteraction extends Interaction {
    private static final String KEY_FALLBACK_INTERACTION_ID = "not_shown_interaction";

    public InAppRatingDialogInteraction(String json) throws JSONException {
        super(json);
    }

    public @Nullable String getFallbackInteractionId() {
        try {
            InteractionConfiguration configuration = getConfiguration();
            if (configuration != null && configuration.has(KEY_FALLBACK_INTERACTION_ID)) {
                return configuration.getString(KEY_FALLBACK_INTERACTION_ID);
            }
        } catch (JSONException e) {
            logException(e);
        }
        return null;
    }
}
