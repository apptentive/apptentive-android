package com.apptentive.android.sdk.external;

import android.content.Context;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;

public class DefaultEngagement implements Engagement {
    @Override
    public void engageInternal(final Context context, final Interaction interaction, final String eventName, @Nullable final Map<String, Object> data) {
        dispatchConversationTask(new ConversationDispatchTask() {
            @Override
            protected boolean execute(Conversation conversation) {
                String jsonData = createJsonData(data);
                return EngagementModule.engageInternal(context, conversation, interaction, eventName, jsonData);
            }
        }, "engage event '" + eventName + "'");
    }

    @Override
    public void launchInteraction(final Context context, final String interactionId, final Callback<Boolean> callback) {
        dispatchConversationTask(new ConversationDispatchTask() {
            @Override
            protected boolean execute(Conversation conversation) {
                Interaction interaction = getInteraction(conversation);
                if (interaction != null) {
                    EngagementModule.launchInteraction(context, conversation, interaction);
                    callback.onFinish(Boolean.TRUE);
                } else {
                    callback.onFinish(Boolean.FALSE);
                }

                return true;
            }

            private @Nullable Interaction getInteraction(Conversation conversation) {
                try {
                    return conversation.getInteraction(interactionId);
                } catch (JSONException e) {
                    ApptentiveLog.e(ApptentiveLogTag.CONVERSATION, e, "Unable to get interaction '%s'", interactionId);
                }
                return null;
            }
        }, "launch interaction '" + interactionId + "'");
    }

    private static String createJsonData(Map<String, Object> data) {
        if (data != null && data.size() > 0) {
            return new JSONObject(data).toString();
        }

        return null;
    }
}
