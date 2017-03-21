/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.InteractionManifest;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interactions;
import com.apptentive.android.sdk.module.engagement.interaction.model.Targets;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.DataChangedListener;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;

import java.io.File;

import static com.apptentive.android.sdk.debug.Tester.dispatchDebugEvent;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.conversation.ConversationState.*;
import static com.apptentive.android.sdk.debug.TesterEvent.*;

public class Conversation implements DataChangedListener {

    /**
     * Conversation data for this class to manage
     */
    private ConversationData data;

    /**
     * File which represents this conversation on the disk
     */
    private File file;

    // TODO: Maybe move this up to a wrapping Conversation class?
    private InteractionManager interactionManager;

    private ConversationState state = ConversationState.UNDEFINED;

    // we keep references to the tasks in order to dispatch them only once
    private final DispatchTask fetchInteractionsTask = new DispatchTask() {
        @Override
        protected void execute() {
            final boolean updateSuccessful = fetchInteractionsSync();

            // Update pending state on UI thread after finishing the task
            DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
                @Override
                protected void execute() {
                    if (hasActiveState()) {
                        ApptentiveInternal.getInstance().notifyInteractionUpdated(updateSuccessful);
                        dispatchDebugEvent(EVT_INTERACTION_FETCH, updateSuccessful);
                    }
                }
            });
        }
    };

    // we keep references to the tasks in order to dispatch them only once
    private final DispatchTask saveConversationTask = new DispatchTask() {
        @Override
        protected void execute() {
            save();
        }
    };

    public Conversation() {
        data = new ConversationData();
    }

    //region Interactions

    /**
     * Returns an Interaction for <code>eventLabel</code> if there is one that can be displayed.
     */
    public Interaction getApplicableInteraction(String eventLabel) {
        String targetsString = getTargets();
        if (targetsString != null) {
            try {
                Targets targets = new Targets(getTargets());
                String interactionId = targets.getApplicableInteraction(eventLabel);
                if (interactionId != null) {
                    String interactionsString = getInteractions();
                    if (interactionsString != null) {
                        Interactions interactions = new Interactions(interactionsString);
                        return interactions.getInteraction(interactionId);
                    }
                }
            } catch (JSONException e) {
                ApptentiveLog.e(e, "Exception while getting applicable interaction: %s", eventLabel);
            }
        }
        return null;
    }

    boolean fetchInteractions(Context context) {
        boolean cacheExpired = getInteractionExpiration() > Util.currentTimeSeconds();
        if (cacheExpired || RuntimeUtils.isAppDebuggable(context)) {
            return DispatchQueue.backgroundQueue().dispatchAsyncOnce(fetchInteractionsTask); // do not allow multiple fetches at the same time
        }

        ApptentiveLog.v(CONVERSATION, "Interaction cache is still valid");
        return false;
    }

    /**
     * Fetches interaction synchronously. Returns <code>true</code> if succeed.
     */
    private boolean fetchInteractionsSync() {
        ApptentiveLog.v(CONVERSATION, "Fetching Interactions");
        ApptentiveHttpResponse response = ApptentiveClient.getInteractions();

        // TODO: Move this to global config
        SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
        boolean updateSuccessful = true;

        // We weren't able to connect to the internet.
        if (response.isException()) {
            prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, false).apply();
            updateSuccessful = false;
        }
        // We got a server error.
        else if (!response.isSuccessful()) {
            prefs.edit().putBoolean(Constants.PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT, true).apply();
            updateSuccessful = false;
        }

        if (updateSuccessful) {
            String interactionsPayloadString = response.getContent();

            // Store new integration cache expiration.
            String cacheControl = response.getHeaders().get("Cache-Control");
            Integer cacheSeconds = Util.parseCacheControlHeader(cacheControl);
            if (cacheSeconds == null) {
                cacheSeconds = Constants.CONFIG_DEFAULT_INTERACTION_CACHE_EXPIRATION_DURATION_SECONDS;
            }
            setInteractionExpiration(Util.currentTimeSeconds() + cacheSeconds);
            try {
                InteractionManifest payload = new InteractionManifest(interactionsPayloadString);
                Interactions interactions = payload.getInteractions();
                Targets targets = payload.getTargets();
                if (interactions != null && targets != null) {
                    setTargets(targets.toString());
                    setInteractions(interactions.toString());
                } else {
                    ApptentiveLog.e(CONVERSATION, "Unable to save interactionManifest.");
                }
            } catch (JSONException e) {
                ApptentiveLog.e(e, "Invalid InteractionManifest received.");
            }
        }
        ApptentiveLog.v(CONVERSATION, "Fetching new Interactions asyncTask finished. Successful? %b", updateSuccessful);

        return updateSuccessful;
    }

    //endregion

    //region Saving

    /**
     * Saves conversation data to the disk synchronously. Returns <code>true</code>
     * if succeed.
     */
    synchronized boolean save() {
        if (file == null) {
            ApptentiveLog.e(CONVERSATION, "Unable to save conversation: destination file not specified");
            return false;
        }

        ApptentiveLog.d(CONVERSATION, "Saving Conversation");
        ApptentiveLog.v(CONVERSATION, "EventData: %s", getEventData().toString()); // TODO: remove

        try {
            FileSerializer serializer = new FileSerializer(file);
            serializer.serialize(this);
            return true;
        } catch (Exception e) {
            ApptentiveLog.e(e, "Unable to save conversation");
            return false;
        }
    }

    //endregion

    //region Listeners

    @Override
    public void onDataChanged() {
        if (hasFile()) {
            boolean scheduled = DispatchQueue.backgroundQueue().dispatchAsyncOnce(saveConversationTask, 100L);
            if (scheduled) {
                ApptentiveLog.d(CONVERSATION, "Scheduling conversation save.");
            } else {
                ApptentiveLog.d(CONVERSATION, "Conversation save already scheduled.");
            }
        } else {
            ApptentiveLog.v(CONVERSATION, "Can't save conversation data: storage file is not specified");
        }
    }

    //endregion

    //region Getters & Setters

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        // TODO: check if state transition would make sense (for example you should not be able to move from 'logged' state to 'anonymous', etc.)
        this.state = state;
    }

    /**
     * Returns <code>true</code> if conversation is in the given state
     */
    public boolean hasState(ConversationState s) {
        return state.equals(s);
    }

    /**
     * Returns <code>true</code> if conversation is in one of the given states
     */
    public boolean hasState(ConversationState... states) {
        for (ConversationState s : states) {
            if (s.equals(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if conversation is in "active" state (after receiving server response)
     */
    public boolean hasActiveState() {
        return hasState(ConversationState.LOGGED_IN, ANONYMOUS);
    }

    public String getConversationToken() {
        return data.getConversationToken();
    }

    public void setConversationToken(String conversationToken) {
        data.setConversationToken(conversationToken);
    }

    public String getConversationId() {
        return data.getConversationId();
    }

    public void setConversationId(String conversationId) {
        data.setConversationId(conversationId);
    }

    public String getPersonId() {
        return data.getPersonId();
    }

    public void setPersonId(String personId) {
        data.setPersonId(personId);
    }

    public String getPersonEmail() {
        return data.getPersonEmail();
    }

    public void setPersonEmail(String personEmail) {
        data.setPersonEmail(personEmail);
    }

    public String getPersonName() {
        return data.getPersonName();
    }

    public void setPersonName(String personName) {
        data.setPersonName(personName);
    }

    public Device getDevice() {
        return data.getDevice();
    }

    public void setDevice(Device device) {
        data.setDevice(device);
    }

    public Device getLastSentDevice() {
        return data.getLastSentDevice();
    }

    public void setLastSentDevice(Device lastSentDevice) {
        data.setLastSentDevice(lastSentDevice);
    }

    public Person getPerson() {
        return data.getPerson();
    }

    public void setPerson(Person person) {
        data.setPerson(person);
    }

    public Person getLastSentPerson() {
        return data.getLastSentPerson();
    }

    public void setLastSentPerson(Person lastSentPerson) {
        data.setLastSentPerson(lastSentPerson);
    }

    public Sdk getSdk() {
        return data.getSdk();
    }

    public void setSdk(Sdk sdk) {
        data.setSdk(sdk);
    }

    public AppRelease getAppRelease() {
        return data.getAppRelease();
    }

    public void setAppRelease(AppRelease appRelease) {
        data.setAppRelease(appRelease);
    }

    public EventData getEventData() {
        return data.getEventData();
    }

    public void setEventData(EventData eventData) {
        data.setEventData(eventData);
    }

    public String getLastSeenSdkVersion() {
        return data.getLastSeenSdkVersion();
    }

    public void setLastSeenSdkVersion(String lastSeenSdkVersion) {
        data.setLastSeenSdkVersion(lastSeenSdkVersion);
    }

    public VersionHistory getVersionHistory() {
        return data.getVersionHistory();
    }

    public void setVersionHistory(VersionHistory versionHistory) {
        data.setVersionHistory(versionHistory);
    }

    public boolean isMessageCenterFeatureUsed() {
        return data.isMessageCenterFeatureUsed();
    }

    public void setMessageCenterFeatureUsed(boolean messageCenterFeatureUsed) {
        data.setMessageCenterFeatureUsed(messageCenterFeatureUsed);
    }

    public boolean isMessageCenterWhoCardPreviouslyDisplayed() {
        return data.isMessageCenterWhoCardPreviouslyDisplayed();
    }

    public void setMessageCenterWhoCardPreviouslyDisplayed(boolean messageCenterWhoCardPreviouslyDisplayed) {
        data.setMessageCenterWhoCardPreviouslyDisplayed(messageCenterWhoCardPreviouslyDisplayed);
    }

    public String getMessageCenterPendingMessage() {
        return data.getMessageCenterPendingMessage();
    }

    public void setMessageCenterPendingMessage(String messageCenterPendingMessage) {
        data.setMessageCenterPendingMessage(messageCenterPendingMessage);
    }

    public String getMessageCenterPendingAttachments() {
        return data.getMessageCenterPendingAttachments();
    }

    public void setMessageCenterPendingAttachments(String messageCenterPendingAttachments) {
        data.setMessageCenterPendingAttachments(messageCenterPendingAttachments);
    }

    public String getTargets() {
        return data.getTargets();
    }

    public void setTargets(String targets) {
        data.setTargets(targets);
    }

    public String getInteractions() {
        return data.getInteractions();
    }

    public void setInteractions(String interactions) {
        data.setInteractions(interactions);
    }

    public double getInteractionExpiration() {
        return data.getInteractionExpiration();
    }

    public void setInteractionExpiration(double interactionExpiration) {
        data.setInteractionExpiration(interactionExpiration);
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public void setInteractionManager(InteractionManager interactionManager) {
        this.interactionManager = interactionManager;
    }

    synchronized boolean hasFile() {
        return file != null;
    }

    synchronized File getFile() {
        return file;
    }

    synchronized void setFile(File file) {
        this.file = file;
    }

    //endregion
}
