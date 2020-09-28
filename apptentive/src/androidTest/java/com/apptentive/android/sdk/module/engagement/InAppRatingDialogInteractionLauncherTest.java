package com.apptentive.android.sdk.module.engagement;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.InstrumentationTestCaseBase;
import com.apptentive.android.sdk.external.Engagement;
import com.apptentive.android.sdk.external.InAppReviewListener;
import com.apptentive.android.sdk.external.InAppReviewManager;
import com.apptentive.android.sdk.external.InAppReviewManagerFactory;
import com.apptentive.android.sdk.module.engagement.interaction.model.InAppRatingDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Callback;
import com.apptentive.android.sdk.util.StringUtils;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.junit.Test;

import java.util.Map;

public class InAppRatingDialogInteractionLauncherTest extends InstrumentationTestCaseBase {
    private static final String FALLBACK_INTERACTION_ID = "1234567";

    private static final String JSON = "{'type':'InAppRatingDialog','version':1,'configuration':{'not_shown_interaction': '" + FALLBACK_INTERACTION_ID + "'},'display_type':null,'id':'58eebbf2127096704e0000d0'}";
    private static final String JSON_MISSING_FALLBACK = "{'type':'InAppRatingDialog','version':1,'configuration':{'not_shown_interaction': '0000000'},'display_type':null,'id':'58eebbf2127096704e0000d0'}";
    private static final String JSON_NO_FALLBACK = "{'type':'InAppRatingDialog','version':1,'configuration':{},'display_type':null,'id':'58eebbf2127096704e0000d0'}";

    //region In-App Review supported

    @Test
    public void testInAppReviewSupportedLaunchSucceed() {
        InAppRatingDialogInteractionLauncher launcher = createLauncher(MockInAppReviewManager.successful());
        launcher.launch(getContext(), createInteraction(JSON));

        assertResult(
                "engage=InAppRatingDialog#request",
                "engage=InAppRatingDialog#shown"
        );
    }

    @Test
    public void testInAppReviewSupportedLaunchFailed() {
        InAppRatingDialogInteractionLauncher launcher = createLauncher(MockInAppReviewManager.failed("Something went wrong"));
        launcher.launch(getContext(), createInteraction(JSON));

        assertResult(
                "engage=InAppRatingDialog#request",
                "engage=InAppRatingDialog#not_shown data='cause':'Something went wrong'"
        );
    }

    //endregion

    //region In-App Review unsupported

    @Test
    public void testInAppReviewUnsupportedWithFallbackAction() {
        InAppRatingDialogInteractionLauncher launcher = createLauncher(MockInAppReviewManager.unsupported());
        launcher.launch(getContext(), createInteraction(JSON));

        assertResult(
                "engage=InAppRatingDialog#request",
                "engage=InAppRatingDialog#not_supported",
                "launch=1234567", // fallback interaction
                "engage=InAppRatingDialog#launch"
        );
    }

    @Test
    public void testInAppReviewUnsupportedWithNoFallbackAction() {
        InAppRatingDialogInteractionLauncher launcher = createLauncher(MockInAppReviewManager.unsupported());
        launcher.launch(getContext(), createInteraction(JSON_NO_FALLBACK));

        assertResult(
                "engage=InAppRatingDialog#request",
                "engage=InAppRatingDialog#not_supported"
        );
    }

    @Test
    public void testInAppReviewUnsupportedWithMissingFallbackAction() {
        InAppRatingDialogInteractionLauncher launcher = createLauncher(MockInAppReviewManager.unsupported());
        launcher.launch(getContext(), createInteraction(JSON_MISSING_FALLBACK));

        assertResult(
                "engage=InAppRatingDialog#request",
                "engage=InAppRatingDialog#not_supported"
        );
    }

    //endregion

    //region Helpers

    private InAppRatingDialogInteraction createInteraction(String json) {
        try {
            return new InAppRatingDialogInteraction(json.replace("'", "\""));
        } catch (JSONException e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

    private InAppRatingDialogInteractionLauncher createLauncher(InAppReviewManager manager) {
        Engagement engagement = new Engagement() {
            @Override
            public void engageInternal(Context context, Interaction interaction, String eventName, @Nullable Map<String, Object> data) {
                String event = interaction.getType() + "#" + eventName;
                if (data != null) {
                    addResult("engage=%s data=%s", event, StringUtils.toString(data));
                } else {
                    addResult("engage=%s", event);
                }
            }

            @Override
            public void launchInteraction(Context context, String interactionId, Callback<Boolean> callback) {
                if (FALLBACK_INTERACTION_ID.equals(interactionId)) {
                    addResult("launch=%s", interactionId);
                    callback.onFinish(Boolean.TRUE);
                } else {
                    callback.onFinish(Boolean.FALSE);
                }
            }
        };

        return new InAppRatingDialogInteractionLauncher(new MockInAppReviewManagerFactory(manager), engagement);
    }

    //endregion

    //region Mocks

    private static class MockInAppReviewManager implements InAppReviewManager {
        private final String errorMessage;
        private final boolean supported;

        private MockInAppReviewManager(boolean supported, String errorMessage) {
            this.errorMessage = errorMessage;
            this.supported = supported;
        }

        public static InAppReviewManager successful() {
            return new MockInAppReviewManager(true, null);
        }

        public static InAppReviewManager failed(String errorMessage) {
            return new MockInAppReviewManager(true, errorMessage);
        }

        public static InAppReviewManager unsupported() {
            return new MockInAppReviewManager(false, null);
        }

        @Override
        public void launchReview(@NonNull Context context, InAppReviewListener callback) {
            if (supported) {
                if (errorMessage != null) {
                    callback.onReviewFlowFailed(errorMessage);
                } else {
                    callback.onReviewFlowComplete();
                }
            } else {
                throw new AssertionFailedError("Should not get there");
            }
        }

        @Override
        public boolean isInAppReviewSupported() {
            return supported;
        }
    }

    private static class MockInAppReviewManagerFactory implements InAppReviewManagerFactory {
        private final InAppReviewManager manager;

        private MockInAppReviewManagerFactory(InAppReviewManager manager) {
            this.manager = manager;
        }

        @Override
        public InAppReviewManager createReviewManager(@NonNull Context context) {
            return manager;
        }
    }

    private static class MockInteraction extends Interaction {
        public static Interaction create(String json) {
            try {
                return new MockInteraction(json);
            } catch (JSONException e) {
                throw new AssertionFailedError(e.getMessage());
            }
        }

        private MockInteraction(String json) throws JSONException {
            super(json);
        }
    }

    //endregion
}