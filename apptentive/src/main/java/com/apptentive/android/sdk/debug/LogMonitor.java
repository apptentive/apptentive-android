/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.debug;

import android.content.Context;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.network.HttpJsonRequest;
import com.apptentive.android.sdk.network.HttpRequest;
import com.apptentive.android.sdk.network.HttpRequestManager;
import com.apptentive.android.sdk.network.HttpRequestMethod;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicy;
import com.apptentive.android.sdk.network.HttpRequestRetryPolicyDefault;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;
import org.json.JSONObject;

import static com.apptentive.android.sdk.ApptentiveHelper.*;
import static com.apptentive.android.sdk.ApptentiveLogTag.*;
import static com.apptentive.android.sdk.comm.ApptentiveHttpClient.USER_AGENT_STRING;

public final class LogMonitor {
	/**
	 * Text prefix for a valid access token
	 */
	private static final String DEBUG_TEXT_HEADER = "com.apptentive.debug:";

	/**
	 * Access token verification request tag
	 */
	private static final String TAG_VERIFICATION_REQUEST = "VERIFICATION_REQUEST";

	/**
	 * Holds current session instance (if any).
	 * NOTE: This field should only be accessed on the conversation queue.
	 */
	private static @Nullable LogMonitorSession currentSession;

	// no instancing or subclassing
	private LogMonitor() {
	}

	//region Session

	/**
	 * Attempts to start a new troubleshooting session. First the SDK will check if there is
	 * an existing session stored in the persistent storage and then check if the clipboard
	 * contains a valid access token.
	 * This call is async and returns immediately.
	 */
	public static void startSession(final Context context, final String appKey, final String appSignature) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				try {
					startSessionGuarded(context, appKey, appSignature);
				} catch (Exception e) {
					ApptentiveLog.e(TROUBLESHOOT, e, "Unable to start Apptentive Log Monitor");
					logException(e);
				}
			}
		});
	}

	private static void startSessionGuarded(final Context context, String appKey, String appSignature) {
		checkConversationQueue();

		// check if another session is currently active
		if (currentSession != null) {
			return;
		}

		// attempt to load an existing session
		final LogMonitorSession existingSession = LogMonitorSessionIO.readCurrentSession(context);
		if (existingSession != null) {
			ApptentiveLog.i(TROUBLESHOOT, "Previous Apptentive Log Monitor session loaded from persistent storage: %s", existingSession);
			startSession(context, existingSession);
			return;
		}

		// attempt to create a new session based on the clipboard content
		final String accessToken = readAccessTokenFromClipboard(context);

		// no access token was found
		if (accessToken == null) {
			ApptentiveLog.v(TROUBLESHOOT, "No access token found in clipboard");
			return;
		}

		// clear the clipboard
		Util.setClipboardText(context, ""); // clear the clipboard contents after the data is parsed

		// check if access token
		HttpRequest existingRequest = HttpRequestManager.sharedManager().findRequest(TAG_VERIFICATION_REQUEST);
		if (existingRequest != null) {
			ApptentiveLog.v(TROUBLESHOOT, "Another access token verification request is running");
			return;
		}

		// create and send a token verification request
		HttpRequest verificationRequest = createTokenVerificationRequest(appKey, appSignature, accessToken, new HttpRequest.Adapter<HttpJsonRequest>() {
			@Override
			public void onFinish(HttpJsonRequest request) {
				checkConversationQueue();

				JSONObject response = request.getResponseObject();
				boolean tokenValid = response.optBoolean("valid", false);
				if (!tokenValid) {
					ApptentiveLog.w(TROUBLESHOOT, "Unable to start Apptentive Log Monitor: the access token was rejected on the server (%s)", accessToken);
					Util.showToast(context, "Token rejected", Toast.LENGTH_LONG);
					return;
				}

				LogMonitorSession session = LogMonitorSessionIO.readSessionFromJWT(accessToken);
				if (session == null) {
					ApptentiveLog.w(TROUBLESHOOT, "Unable to start Apptentive Log Monitor: failed to parse the access token (%s)", accessToken);
					Util.showToast(context, "Token invalid", Toast.LENGTH_LONG);
					return;
				}

				// store the current session to make sure we can resume log monitoring on the next application start
				LogMonitorSessionIO.saveCurrentSession(context, session);

				// start the session
				startSession(context, session);
			}

			@Override
			public void onFail(HttpJsonRequest request, String reason) {
				ApptentiveLog.e(TROUBLESHOOT, "Unable to start Apptentive Log Monitor: failed to verify the access token (%s)\n%s", accessToken, reason);
				Util.showToast(context, "Can't verify token", Toast.LENGTH_LONG);
			}
		});
		verificationRequest.setCallbackQueue(conversationQueue());
		verificationRequest.start();
	}

	private static void startSession(Context context, LogMonitorSession session) {
		currentSession = session;
		session.start(context);
	}

	static void stopSession(final Context context) {
		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				if (currentSession != null) {
					currentSession.stop();
					currentSession = null;
				}
				LogMonitorSessionIO.deleteCurrentSession(context);
			}
		});
	}

	//endregion

	//region Access Token

	/**
	 * Attempts to read access token from the clipboard
	 */
	private static @Nullable String readAccessTokenFromClipboard(Context context) {
		String text = Util.getClipboardText(context);

		if (StringUtils.isNullOrEmpty(text)) {
			return null;
		}

		//Since the token string should never contain spaces, attempt to repair line breaks introduced in the copying process.
		text = text.replaceAll("\\s+", "");

		if (!text.startsWith(DEBUG_TEXT_HEADER)) {
			return null;
		}

		// Remove the header
		return text.substring(DEBUG_TEXT_HEADER.length());
	}

	//endregion

	//region Token Verification

	private static HttpRequest createTokenVerificationRequest(String apptentiveAppKey, String apptentiveAppSignature, String token, HttpRequest.Listener<HttpJsonRequest> listener) {
		// TODO: move this logic to ApptentiveHttpClient
		String URL = Constants.CONFIG_DEFAULT_SERVER_URL + "/debug_token/verify";
		HttpRequest request = new HttpJsonRequest(URL, createVerityRequestObject(token));
		request.setTag(TAG_VERIFICATION_REQUEST);
		request.setMethod(HttpRequestMethod.POST);
		request.setRequestManager(HttpRequestManager.sharedManager());
		request.setRequestProperty("X-API-Version", Constants.API_VERSION);
		request.setRequestProperty("APPTENTIVE-KEY", apptentiveAppKey);
		request.setRequestProperty("APPTENTIVE-SIGNATURE", apptentiveAppSignature);
		request.setRequestProperty("Content-Type", "application/json");
		request.setRequestProperty("Accept", "application/json");
		request.setRequestProperty("User-Agent", String.format(USER_AGENT_STRING, Constants.getApptentiveSdkVersion()));
		request.setRetryPolicy(createVerityRequestRetryPolicy());
		request.addListener(listener);
		return request;
	}

	private static JSONObject createVerityRequestObject(String token) {
		try {
			JSONObject postBodyJson = new JSONObject();
			postBodyJson.put("debug_token", token);
			return postBodyJson;
		} catch (JSONException e) {
			// should not happen but it's better to throw an exception
			throw new IllegalArgumentException("Token is invalid:" + token, e);
		}
	}

	private static HttpRequestRetryPolicy createVerityRequestRetryPolicy() {
		return new HttpRequestRetryPolicyDefault() {
			@Override
			public boolean shouldRetryRequest(int responseCode, int retryAttempt) {
				return false; // fail fast: do not retry
			}
		};
	}

	//endregion

	//region Error Reporting

	private static void logException(Exception e) {
		ErrorMetrics.logException(e);
	}

	//endregion
}
