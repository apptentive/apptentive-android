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

import com.apptentive.android.sdk.comm.ApptentiveHttpClient;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationProxy;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.ApptentiveTaskManager;

import java.util.Map;

class ApptentiveNullInstance implements ApptentiveInstance {
	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public boolean showMessageCenterInternal(@NonNull Context context, @Nullable Map<String, Object> customData) {
		failMethodCall("showMessageCenterInternal");
		return false;
	}

	@Override
	public void login(String token, Apptentive.LoginCallback callback) {
		failMethodCall("login");
	}

	@Override
	public void logout() {
		failMethodCall("logout");
	}

	@Override
	public void updateApptentiveInteractionTheme(Context context, Resources.Theme theme) {
		failMethodCall("updateApptentiveInteractionTheme");
	}

	@Override
	public void notifyInteractionUpdated(boolean successful) {
		failMethodCall("notifyInteractionUpdated");
	}

	@Override
	public void showAboutInternal(Context context, boolean showBrandingBand) {
		failMethodCall("showAboutInternal");
	}

	@Override
	public OnSurveyFinishedListener getOnSurveyFinishedListener() {
		failMethodCall("getOnSurveyFinishedListener");
		return null;
	}

	@Override
	public void setOnSurveyFinishedListener(OnSurveyFinishedListener listener) {
		failMethodCall("setOnSurveyFinishedListener");
	}

	@Override
	public void setAuthenticationFailedListener(Apptentive.AuthenticationFailedListener listener) {
		failMethodCall("setAuthenticationFailedListener");
	}

	@Override
	public void setRatingProvider(@NonNull IRatingProvider ratingProvider) {
		failMethodCall("setRatingProvider");
	}

	@Override
	public void putRatingProviderArg(@NonNull String key, String value) {
		failMethodCall("putRatingProviderArg");
	}

	@Override
	public void addInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener) {
		failMethodCall("addInteractionUpdateListener");
	}

	@Override
	public void removeInteractionUpdateListener(InteractionManager.InteractionUpdateListener listener) {
		failMethodCall("removeInteractionUpdateListener");
	}

	@Nullable
	@Override
	public Context getApplicationContext() {
		failMethodCall("getApplicationContext");
		return null;
	}

	@Nullable
	@Override
	public Conversation getConversation() {
		failMethodCall("getConversation");
		return null;
	}

	@Nullable
	@Override
	public ConversationProxy getConversationProxy() {
		failMethodCall("getConversationProxy");
		return null;
	}

	@Nullable
	@Override
	public AppRelease getAppRelease() {
		failMethodCall("getAppRelease");
		return null;
	}

	@Nullable
	@Override
	public ApptentiveTaskManager getApptentiveTaskManager() {
		failMethodCall("getApptentiveTaskManager");
		return null;
	}

	@Nullable
	@Override
	public ApptentiveHttpClient getApptentiveHttpClient() {
		failMethodCall("getApptentiveHttpClient");
		return null;
	}

	@Nullable
	@Override
	public SharedPreferences getGlobalSharedPrefs() {
		failMethodCall("getGlobalSharedPrefs");
		return null;
	}

	@Nullable
	@Override
	public Map<String, Object> getAndClearCustomData() {
		failMethodCall("getAndClearCustomData");
		return null;
	}

	@Nullable
	@Override
	public IRatingProvider getRatingProvider() {
		failMethodCall("getRatingProvider");
		return null;
	}

	@Nullable
	@Override
	public Map<String, String> getRatingProviderArgs() {
		failMethodCall("getRatingProviderArgs");
		return null;
	}

	@Override
	public Activity getCurrentTaskStackTopActivity() {
		failMethodCall("getCurrentTaskStackTopActivity");
		return null;
	}

	@Override
	public String getApplicationVersionName() {
		failMethodCall("getApplicationVersionName");
		return null;
	}

	@Override
	public int getApplicationVersionCode() {
		failMethodCall("getApplicationVersionCode");
		return 0;
	}

	@Override
	public int getDefaultStatusBarColor() {
		failMethodCall("getDefaultStatusBarColor");
		return 0;
	}

	@Override
	public Resources.Theme getApptentiveToolbarTheme() {
		failMethodCall("getApptentiveToolbarTheme");
		return null;
	}

	@Override
	public boolean isAppUsingAppCompatTheme() {
		failMethodCall("isAppUsingAppCompatTheme");
		return false;
	}

	@Override
	public String getDefaultAppDisplayName() {
		failMethodCall("getDefaultAppDisplayName");
		return null;
	}

	private void failMethodCall(String method) {
		Assert.assertFail("Unable to invoke '%s': Apptentive SDK is not properly initialized", method);
	}
}
