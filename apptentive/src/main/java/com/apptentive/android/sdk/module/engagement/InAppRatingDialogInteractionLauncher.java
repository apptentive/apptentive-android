package com.apptentive.android.sdk.module.engagement;

import android.content.Context;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.external.DefaultEngagement;
import com.apptentive.android.sdk.external.DefaultInAppReviewManagerFactory;
import com.apptentive.android.sdk.external.Engagement;
import com.apptentive.android.sdk.external.InAppReviewListener;
import com.apptentive.android.sdk.external.InAppReviewManager;
import com.apptentive.android.sdk.external.InAppReviewManagerFactory;
import com.apptentive.android.sdk.module.engagement.interaction.model.InAppRatingDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Callback;

import java.util.HashMap;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveLogTag.IN_APP_REVIEW;

public class InAppRatingDialogInteractionLauncher implements InteractionLauncher<InAppRatingDialogInteraction> {
    private static final String EVENT_NAME_REQUEST = "request";
    private static final String EVENT_NAME_SHOWN = "shown";
    private static final String EVENT_NAME_NOT_SHOWN = "not_shown";
    private static final String EVENT_NAME_NOT_SUPPORTED = "not_supported";
    private static final String EVENT_NAME_LAUNCH = "launch";

    private static final String DATA_KEY_CAUSE = "cause";

    private final InAppReviewManagerFactory managerFactory;
    private final Engagement engagement;

    public InAppRatingDialogInteractionLauncher() {
        this(new DefaultInAppReviewManagerFactory(), new DefaultEngagement());
    }

    public InAppRatingDialogInteractionLauncher(InAppReviewManagerFactory managerFactory, Engagement engagement) {
        this.managerFactory = managerFactory;
        this.engagement = engagement;
    }

    @Override
    public boolean launch(final Context context, final InAppRatingDialogInteraction interaction) {
        engageInternal(context, interaction, EVENT_NAME_REQUEST);

        InAppReviewManager reviewManager = managerFactory.createReviewManager(context);
        if (reviewManager.isInAppReviewSupported()) {
            reviewManager.launchReview(context, new InAppReviewListener() {
                @Override
                public void onReviewFlowComplete() {
                    onReviewShown(context, interaction);
                }

                @Override
                public void onReviewFlowFailed(String message) {
                    onReviewNotShown(context, interaction, message);
                }
            });
        } else {
            onReviewNotSupported(context, interaction);
        }

        return true;
    }

    private void onReviewShown(Context context, InAppRatingDialogInteraction interaction) {
        engageInternal(context, interaction, EVENT_NAME_SHOWN);
    }

    private void onReviewNotShown(final Context context, final InAppRatingDialogInteraction interaction, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put(DATA_KEY_CAUSE, message);
        engageInternal(context, interaction, EVENT_NAME_NOT_SHOWN, data);
    }

    private void onReviewNotSupported(final Context context, final InAppRatingDialogInteraction interaction) {
        // engage 'not_supported' event
        engageInternal(context, interaction, EVENT_NAME_NOT_SUPPORTED);

        // engage 'fallback' interaction (if any)
        final String fallbackInteractionId = interaction.getFallbackInteractionId();
        if (fallbackInteractionId != null) {
            engagement.launchInteraction(context, fallbackInteractionId, new Callback<Boolean>() {
                @Override
                public void onFinish(Boolean successful) {
                    if (successful) {
                        // engage 'launch' event
                        engagement.engageInternal(context, interaction, EVENT_NAME_LAUNCH, null);
                    } else {
                        ApptentiveLog.e(IN_APP_REVIEW, "Fallback interaction was not launched: %s", fallbackInteractionId);
                    }
                }
            });
        } else {
            ApptentiveLog.d(IN_APP_REVIEW, "No fallback interaction");
        }
    }

    private void engageInternal(Context context, Interaction interaction, String eventName) {
        engageInternal(context, interaction, eventName, null);
    }

    private void engageInternal(final Context context, final Interaction interaction, final String eventName, final Map<String, Object> data) {
        engagement.engageInternal(context, interaction, eventName, data);
    }
}