/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Apptentive.AuthenticationFailedListener;
import com.apptentive.android.sdk.Apptentive.LoginCallback;
import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationProxy;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.ApptentiveTaskManager;
import com.apptentive.android.sdk.util.Nullsafe;

import java.util.Map;

public interface ApptentiveInstance extends Nullsafe {

	boolean showMessageCenterInternal(@NonNull Context context, @Nullable Map<String, Object> customData);

	void login(String token, LoginCallback callback);
	void logout();

	void updateApptentiveInteractionTheme(Context context, Resources.Theme theme);

	void notifyInteractionUpdated(boolean successful); // TODO: remove this method and replace the call with a notification

	void showAboutInternal(Context context, boolean showBrandingBand);

	OnSurveyFinishedListener getOnSurveyFinishedListener();
	void setOnSurveyFinishedListener(OnSurveyFinishedListener listener);
	void setAuthenticationFailedListener(AuthenticationFailedListener listener);
	void setRatingProvider(@NonNull IRatingProvider ratingProvider);
	void putRatingProviderArg(@NonNull String key, String value);
	void addInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener);
	void removeInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener);

	@Nullable Context getApplicationContext(); // TODO: this should not be here
	@Nullable Conversation getConversation();
	@Nullable ConversationProxy getConversationProxy();
	@Nullable AppRelease getAppRelease();
	@Nullable ApptentiveTaskManager getApptentiveTaskManager();
	@Nullable ApptentiveHttpClient getApptentiveHttpClient();
	@Nullable SharedPreferences getGlobalSharedPrefs();

	@Nullable Map<String, Object> getAndClearCustomData();
	@Nullable IRatingProvider getRatingProvider();
	@Nullable Map<String, String> getRatingProviderArgs();

	Activity getCurrentTaskStackTopActivity(); // TODO: this should not be here

	String getApplicationVersionName();        // TODO: this should not be here
	int getApplicationVersionCode();           // TODO: this should not be here

	int getDefaultStatusBarColor();
	Resources.Theme getApptentiveToolbarTheme();
	boolean isAppUsingAppCompatTheme();

	String getDefaultAppDisplayName();
}
