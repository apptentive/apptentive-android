/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.messagecenter.model.CompoundMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.storage.*;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.util.*;

/**
 * This class contains the complete API for accessing Apptentive features from within your app.
 *
 * @author Sky Kelsey
 */
public class Apptentive {


	/**
	 * Must be called from the {@link Application#onCreate()} method in the {@link Application} object defined in your app's manifest.
	 * @param application The {@link Application} object for this app.
	 */
	public static void register(Application application) {
		Apptentive.register(application, null);
	}

	public static void register(Application application, String apptentiveApiKey) {
		Log.i("Registering Apptentive.");
		ApptentiveInternal.createInstance(application, apptentiveApiKey);
		ApptentiveInternal.setLifeCycleCallback();
	}

	// ****************************************************************************************
	// GLOBAL DATA METHODS
	// ****************************************************************************************

	/**
	 * Sets the user's email address. This email address will be sent to the Apptentive server to allow out of app
	 * communication, and to help provide more context about this user. This email will be the definitive email address
	 * for this user, unless one is provided directly by the user through an Apptentive UI. Calls to this method are
	 * idempotent. Calls to this method will overwrite any previously entered email, so if you don't want to overwrite
	 * the email provided by the user, make sure to check the value with {@link #getPersonEmail()} before you call this method.
	 *
	 * @param email   The user's email address.
	 */
	public static void setPersonEmail(String email) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			PersonManager.storePersonEmail(email);
		}
	}

	/**
	 * Retrieves the user's email address. This address may be set via {@link #setPersonEmail(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's email if set, else null.
	 */
	public static String getPersonEmail() {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			return PersonManager.loadPersonEmail();
		}
		return null;
	}

	/**
	 * Sets the user's name. This name will be sent to the Apptentive server and displayed in conversations you have
	 * with this person. This name will be the definitive username for this user, unless one is provided directly by the
	 * user through an Apptentive UI. Calls to this method are idempotent. Calls to this method will overwrite any
	 * previously entered email, so if you don't want to overwrite the email provided by the user, make sure to check
	 * the value with {@link #getPersonName()} before you call this method.
	 *
	 * @param name    The user's name.
	 */
	public static void setPersonName(String name) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			PersonManager.storePersonName(name);
		}
	}

	/**
	 * Retrieves the user's name. This name may be set via {@link #setPersonName(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's name if set, else null.
	 */
	public static String getPersonName(){
		if (ApptentiveInternal.isApptentiveRegistered()) {
			return PersonManager.loadPersonName();
		}
		return null;
	}


	/**
	 * <p>Allows you to pass arbitrary string data to the server along with this device's info. This method will replace all
	 * custom device data that you have set for this app. Calls to this method are idempotent.</p>
	 * <p>To add a single piece of custom device data, use {@link #addCustomDeviceData}</p>
	 * <p>To remove a single piece of custom device data, use {@link #removeCustomDeviceData}</p>
	 *
	 * @param customDeviceData A Map of key/value pairs to send to the server.
	 * @deprecated
	 */
	public static void setCustomDeviceData(Map<String, String> customDeviceData) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			try {
				CustomData customData = new CustomData();
				for (String key : customDeviceData.keySet()) {
					customData.put(key, customDeviceData.get(key));
				}
				DeviceManager.storeCustomDeviceData(customData);
			} catch (JSONException e) {
				Log.w("Unable to set custom device data.", e);
			}
		}
	}

	/**
	 * Add a custom data String to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A String value.
	 */
	public static void addCustomDeviceData(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			if (value != null) {
				value = value.trim();
			}
			ApptentiveInternal.getInstance().addCustomDeviceData(key, value);
		}
	}

	/**
	 * Add a custom data Number to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A Number value.
	 */
	public static void addCustomDeviceData(String key, Number value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomDeviceData(key, value);
		}
	}

	/**
	 * Add a custom data Boolean to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A Boolean value.
	 */
	public static void addCustomDeviceData(String key, Boolean value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomDeviceData(key, value);
		}
	}

	private static void addCustomDeviceData(String key, Version version) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomDeviceData(key, version);
		}
	}

	private static void addCustomDeviceData(String key, DateTime dateTime) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomDeviceData(key, dateTime);
		}
	}

	/**
	 * Remove a piece of custom data from the device. Calls to this method are idempotent.
	 *
	 * @param key     The key to remove.
	 */
	public static void removeCustomDeviceData(String key) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			CustomData customData = DeviceManager.loadCustomDeviceData();
			if (customData != null) {
				customData.remove(key);
				DeviceManager.storeCustomDeviceData(customData);
			}
		}
	}

	/**
	 * <p>Allows you to pass arbitrary string data to the server along with this person's info. This method will replace all
	 * custom person data that you have set for this app. Calls to this method are idempotent.</p>
	 * <p>To add a single piece of custom person data, use {@link #addCustomPersonData}</p>
	 * <p>To remove a single piece of custom person data, use {@link #removeCustomPersonData}</p>
	 *
	 * @param customPersonData A Map of key/value pairs to send to the server.
	 * @deprecated
	 */
	public static void setCustomPersonData(Map<String, String> customPersonData) {
		Log.w("Setting custom person data: %s", customPersonData.toString());
		if (ApptentiveInternal.isApptentiveRegistered()) {
			try {
				CustomData customData = new CustomData();
				for (String key : customPersonData.keySet()) {
					customData.put(key, customPersonData.get(key));
				}
				PersonManager.storeCustomPersonData(customData);
			} catch (JSONException e) {
				Log.e("Unable to set custom person data.", e);
			}
		}
	}

	/**
	 * Add a custom data String to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A String value.
	 */
	public static void addCustomPersonData(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			if (value != null) {
				value = value.trim();
			}
			ApptentiveInternal.getInstance().addCustomPersonData(key, value);
		}
	}

	/**
	 * Add a custom data Number to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A Number value.
	 */
	public static void addCustomPersonData(String key, Number value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomPersonData(key, value);
		}
	}

	/**
	 * Add a custom data Boolean to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key     The key to store the data under.
	 * @param value   A Boolean value.
	 */
	public static void addCustomPersonData(String key, Boolean value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomPersonData(key, value);
		}
	}

	private static void addCustomPersonData(String key, Version version) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomPersonData(key, version);
		}
	}

	private static void addCustomPersonData(String key, DateTime dateTime) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().addCustomPersonData(key, dateTime);
		}
	}

	/**
	 * Remove a piece of custom data from the Person. Calls to this method are idempotent.
	 *
	 * @param key     The key to remove.
	 */
	public static void removeCustomPersonData(String key) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			CustomData customData = PersonManager.loadCustomPersonData();
			if (customData != null) {
				customData.remove(key);
				PersonManager.storeCustomPersonData(customData);
			}
		}
	}


	// ****************************************************************************************
	// THIRD PARTY INTEGRATIONS
	// ****************************************************************************************

	private static final String INTEGRATION_APPTENTIVE_PUSH = "apptentive_push";
	private static final String INTEGRATION_PARSE = "parse";
	private static final String INTEGRATION_URBAN_AIRSHIP = "urban_airship";
	private static final String INTEGRATION_AWS_SNS = "aws_sns";

	private static final String INTEGRATION_PUSH_TOKEN = "token";

	private static void addIntegration(String integration, Map<String, String> config) {
		if (integration == null || config == null) {
			return;
		}
		if (!ApptentiveInternal.isApptentiveRegistered()) {
			return;
		}

		CustomData integrationConfig = DeviceManager.loadIntegrationConfig();
		try {
			JSONObject configJson = null;
			if (!integrationConfig.isNull(integration)) {
				configJson = integrationConfig.getJSONObject(integration);
			} else {
				configJson = new JSONObject();
				integrationConfig.put(integration, configJson);
			}
			for (String key : config.keySet()) {
				configJson.put(key, config.get(key));
			}
			Log.d("Adding integration config: %s", config.toString());
			DeviceManager.storeIntegrationConfig(integrationConfig);
			ApptentiveInternal.getInstance().syncDevice();
		} catch (JSONException e) {
			Log.e("Error adding integration: %s, %s", e, integration, config.toString());
		}
	}

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device without a third party push provider. Requires a valid GCM configuration.
	 */
	public static final int PUSH_PROVIDER_APPTENTIVE = 0;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Parse Push integration. Requires a valid Parse integration.
	 */
	public static final int PUSH_PROVIDER_PARSE = 1;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Urban Airship Push integration. Requires a valid Urban
	 * Airship Push integration.
	 */
	public static final int PUSH_PROVIDER_URBAN_AIRSHIP = 2;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Amazon AWS SNS integration. Requires a valid Amazon AWS SNS
	 * integration.
	 */
	public static final int PUSH_PROVIDER_AMAZON_AWS_SNS = 3;

	/**
	 * Sends push provider information to our server to allow us to send pushes to this device when
	 * you reply to your customers. Only one push provider is allowed to be active at a time, so you
	 * should only call this method once. Please see our
	 * <a href="http://www.apptentive.com/docs/android/integration/#push-notifications">integration guide</a> for
	 * instructions.
	 *
	 * @param pushProvider One of the following:
	 *                     <ul>
	 *                     <li>{@link #PUSH_PROVIDER_APPTENTIVE}</li>
	 *                     <li>{@link #PUSH_PROVIDER_PARSE}</li>
	 *                     <li>{@link #PUSH_PROVIDER_URBAN_AIRSHIP}</li>
	 *                     <li>{@link #PUSH_PROVIDER_AMAZON_AWS_SNS}</li>
	 *                     </ul>
	 * @param token        The push provider token you receive from your push provider. The format is push provider specific.
	 *                     <dl>
	 *                     <dt>Apptentive</dt>
	 *                     <dd>If you are using Apptentive to send pushes directly to GCM, pass in the GCM Registration ID, which you can
	 *                     <a href="https://github.com/googlesamples/google-services/blob/73f8a4fcfc93da08a40b96df3537bb9b6ef1b0fa/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart/RegistrationIntentService.java#L51">access like this</a>.
	 *                     </dd>
	 *                     <dt>Parse</dt>
	 *                     <dd>The Parse <a href="https://parse.com/docs/android/guide#push-notifications">deviceToken</a></dd>
	 *                     <dt>Urban Airship</dt>
	 *                     <dd>The Urban Airship Channel ID, which you can
	 *                     <a href="https://github.com/urbanairship/android-samples/blob/8ad77e5e81a1b0507c6a2c45a5c30a1e2da851e9/PushSample/src/com/urbanairship/push/sample/IntentReceiver.java#L43">access like this</a>.
	 *                     </dd>
	 *                     <dt>Amazon AWS SNS</dt>
	 *                     <dd>The GCM Registration ID, which you can <a href="http://docs.aws.amazon.com/sns/latest/dg/mobile-push-gcm.html#registration-id-gcm">access like this</a>.</dd>
	 *                     </dl>
	 */
	public static void setPushNotificationIntegration(int pushProvider, String token) {
		try {
			if (!ApptentiveInternal.isApptentiveRegistered()) {
				return;
			}
			CustomData integrationConfig = getIntegrationConfigurationWithoutPushProviders();
			JSONObject pushObject = new JSONObject();
			pushObject.put(INTEGRATION_PUSH_TOKEN, token);
			switch (pushProvider) {
				case PUSH_PROVIDER_APPTENTIVE:
					integrationConfig.put(INTEGRATION_APPTENTIVE_PUSH, pushObject);
					break;
				case PUSH_PROVIDER_PARSE:
					integrationConfig.put(INTEGRATION_PARSE, pushObject);
					break;
				case PUSH_PROVIDER_URBAN_AIRSHIP:
					integrationConfig.put(INTEGRATION_URBAN_AIRSHIP, pushObject);
					break;
				case PUSH_PROVIDER_AMAZON_AWS_SNS:
					integrationConfig.put(INTEGRATION_AWS_SNS, pushObject);
					break;
				default:
					Log.e("Invalid pushProvider: %d", pushProvider);
					return;
			}
			DeviceManager.storeIntegrationConfig(integrationConfig);
			ApptentiveInternal.getInstance().syncDevice();
		} catch (JSONException e) {
			Log.e("Error setting push integration.", e);
			return;
		}
	}

	private static CustomData getIntegrationConfigurationWithoutPushProviders() {
		CustomData integrationConfig = DeviceManager.loadIntegrationConfig();
		if (integrationConfig != null) {
			integrationConfig.remove(INTEGRATION_APPTENTIVE_PUSH);
			integrationConfig.remove(INTEGRATION_PARSE);
			integrationConfig.remove(INTEGRATION_URBAN_AIRSHIP);
			integrationConfig.remove(INTEGRATION_AWS_SNS);
		}
		return integrationConfig;
	}

	// ****************************************************************************************
	// PUSH NOTIFICATIONS
	// ****************************************************************************************

	/**
	 * Determines whether this Intent is a push notification sent from Apptentive.
	 *
	 * @param intent The opened push notification Intent you received in your BroadcastReceiver.
	 * @return True if the Intent contains Apptentive push information.
	 */
	public static boolean isApptentivePushNotification(Intent intent) {
		return ApptentiveInternal.getApptentivePushNotificationData(intent) != null;
	}

	/**
	 * Determines whether this Bundle came from an Apptentive push notification. This method is used with Urban Airship
	 * integrations.
	 *
	 * @param bundle The Extra data from an opened push notification.
	 * @return True if the Intent contains Apptentive push information.
	 */
	public static boolean isApptentivePushNotification(Bundle bundle) {
		return ApptentiveInternal.getApptentivePushNotificationData(bundle) != null;
	}

	/**
	 * <p>Saves Apptentive specific data from a push notification Intent. In your BroadcastReceiver, if the push notification
	 * came from Apptentive, it will have data that needs to be saved before you launch your Activity. You must call this
	 * method <strong>every time</strong> you get a push opened Intent, and before you launch your Activity. If the push
	 * notification did not come from Apptentive, this method has no effect.</p>
	 * <p>Use this method when using Parse and Amazon SNS as push providers.</p>
	 *
	 * @param intent  The Intent that you received when the user opened a push notification.
	 * @return true if the push data came from Apptentive.
	 */
	public static boolean setPendingPushNotification(Intent intent) {
		String apptentive = ApptentiveInternal.getApptentivePushNotificationData(intent);
		if (apptentive != null) {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				return ApptentiveInternal.getInstance().setPendingPushNotification(apptentive);
			}
		}
		return false;
	}


	/**
	 * Saves off the data contained in a push notification sent to this device from Apptentive. Use
	 * this method when a push notification is opened, and you only have access to a push data
	 * Bundle containing an "apptentive" key. This will generally be used with direct Apptentive Push
	 * notifications, or when using Urban Airship as a push provider. Calling this method for a push
	 * that did not come from Apptentive has no effect.
	 *
	 * @param data    A Bundle containing the GCM data object from the push notification.
	 * @return true if the push data came from Apptentive.
	 */
	public static boolean setPendingPushNotification( Bundle data) {
		String apptentive = ApptentiveInternal.getApptentivePushNotificationData(data);
		if (apptentive != null) {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				return ApptentiveInternal.getInstance().setPendingPushNotification(apptentive);
			}
		}
		return false;
	}

	/**
	 * Launches Apptentive features based on a push notification Intent. Before you call this, you
	 * must call {@link #setPendingPushNotification(Intent)} or
	 * {@link #setPendingPushNotification(Bundle)} in your Broadcast receiver when
	 * a push notification is opened by the user. This method must be called from the Activity that
	 * you launched from the BroadcastReceiver. This method will only handle Apptentive originated
	 * push notifications, so you can and should call it any time your push notification launches an
	 * Activity.
	 *
	 * @param activity The Activity from which this method is called.
	 * @return True if a call to this method resulted in Apptentive displaying a View.
	 */
	public static boolean handleOpenedPushNotification(Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String pushData = prefs.getString(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION, null);
		prefs.edit().remove(Constants.PREF_KEY_PENDING_PUSH_NOTIFICATION).apply(); // Remove our data so this won't run twice.
		if (pushData != null) {
			Log.i("Handling opened Apptentive push notification.");
			try {
				JSONObject pushJson = new JSONObject(pushData);
				ApptentiveInternal.PushAction action = ApptentiveInternal.PushAction.unknown;
				if (pushJson.has(ApptentiveInternal.PUSH_ACTION)) {
					action = ApptentiveInternal.PushAction.parse(pushJson.getString(ApptentiveInternal.PUSH_ACTION));
				}
				switch (action) {
					case pmc:
						Apptentive.showMessageCenter(activity);
						return true;
					default:
						Log.v("Unknown Apptentive push notification action: \"%s\"", action.name());
				}
			} catch (JSONException e) {
				Log.w("Error parsing JSON from push notification.", e);
				MetricModule.sendError(e, "Parsing Push notification", pushData);
			}
		}
		return false;
	}


	// ****************************************************************************************
	// RATINGS
	// ****************************************************************************************

	/**
	 * Use this to choose where to send the user when they are prompted to rate the app. This should be the same place
	 * that the app was downloaded from.
	 *
	 * @param ratingProvider A {@link IRatingProvider} value.
	 */

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().setRatingProvider(ratingProvider);
		}
	}

	/**
	 * If there are any properties that your {@link IRatingProvider} implementation requires, populate them here. This
	 * is not currently needed with the Google Play and Amazon Appstore IRatingProviders.
	 *
	 * @param key   A String
	 * @param value A String
	 */
	public static void putRatingProviderArg(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().putRatingProviderArg(key, value);
		}
	}

	// ****************************************************************************************
	// MESSAGE CENTER
	// ****************************************************************************************


	/**
	 * Opens the Apptentive Message Center UI Activity
	 *
	 * @param activity The Activity from which to launch the Message Center
	 * @return true if Message Center was shown, else false.
	 */
	public static boolean showMessageCenter(Activity activity) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			return showMessageCenter(activity, null);
		}
		return false;
	}

	/**
	 * Opens the Apptentive Message Center UI Activity, and allows custom data to be sent with the next message the user
	 * sends. If the user sends multiple messages, this data will only be sent with the first message sent after this
	 * method is invoked. Additional invocations of this method with custom data will repeat this process.
	 *
	 * @param activity   The Activity from which to launch the Message Center
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans.
	 *                   If any message is sent by the Person, this data is sent with it, and then
	 *                   cleared. If no message is sent, this data is discarded.
	 * @return true if Message Center was shown, else false.
	 */
	public static boolean showMessageCenter(Activity activity, Map<String, Object> customData) {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				return ApptentiveInternal.getInstance().showMessageCenterInternal(activity, customData);
			}
		} catch (Exception e) {
			Log.w("Error starting Apptentive Activity.", e);
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	/**
	 * Our SDK must connect to our server at least once to download initial configuration for Message
	 * Center. Call this method to see whether or not Message Center can be displayed.
	 *
	 * @return true if a call to {@link #showMessageCenter(Activity)} will display Message Center, else false.
	 */
	public static boolean canShowMessageCenter() {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			return ApptentiveInternal.getInstance().canShowMessageCenterInternal();
		}
		return false;
	}

	/**
	 * Set a listener to be notified when the number of unread messages in the Message Center changes.
	 *
	 * @param listener An UnreadMessageListener that you instantiate.
	 * @deprecated use {@link #addUnreadMessagesListener(UnreadMessagesListener)} instead.
	 */
	@Deprecated
	public static void setUnreadMessagesListener(UnreadMessagesListener listener) {
		MessageManager.setHostUnreadMessagesListener(listener);
	}

	/**
	 * Add a listener to be notified when the number of unread messages in the Message Center changes.
	 *
	 * @param listener An UnreadMessageListener that you instantiate. Do not pass in an anonymous class.
	 *                 Instead, create your listener as an instance variable and pass that in. This
	 *                 allows us to keep a weak reference to avoid memory leaks.
	 */
	public static void addUnreadMessagesListener(UnreadMessagesListener listener) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().getMessageManager().addHostUnreadMessagesListener(listener);
		}
	}

	/**
	 * Returns the number of unread messages in the Message Center.
	 *
	 * @return The number of unread messages.
	 */
	public static int getUnreadMessageCount() {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				return ApptentiveInternal.getInstance().getMessageManager().getUnreadMessageCount();
			}
		} catch (Exception e) {
			MetricModule.sendError(e, null, null);
		}
		return 0;
	}

	/**
	 * Sends a text message to the server. This message will be visible in the conversation view on the server, but will
	 * not be shown in the client's Message Center.
	 *
	 * @param text    The message you wish to send.
	 */
	public static void sendAttachmentText(String text) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			try {
				CompoundMessage message = new CompoundMessage();
				message.setBody(text);
				message.setRead(true);
				message.setHidden(true);
				message.setSenderId(ApptentiveInternal.getInstance().getPersonId());
				message.setAssociatedFiles(null);
				MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();
				if (mgr != null) {
					mgr.sendMessage(message);
				}
			} catch (Exception e) {
				Log.w("Error sending attachment text.", e);
				MetricModule.sendError(e, null, null);
			}
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param uri     The URI of the local resource file.
	 */
	public static void sendAttachmentFile(String uri) {
		try {
			if (TextUtils.isEmpty(uri) || !ApptentiveInternal.isApptentiveRegistered()) {
				return;
			}

			CompoundMessage message = new CompoundMessage();
			// No body, just attachment
			message.setBody(null);
			message.setRead(true);
			message.setHidden(true);
			message.setSenderId(ApptentiveInternal.getInstance().getPersonId());

			ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
			/* Make a local copy in the cache dir. By default the file name is "apptentive-api-file + nonce"
			 * If original uri is known, the name will be taken from the original uri
			 */
			Context context = ApptentiveInternal.getInstance().getApplicationContext();
			String localFilePath = Util.generateCacheFilePathFromNonceOrPrefix(context, message.getNonce(), Uri.parse(uri).getLastPathSegment());

			String mimeType = Util.getMimeTypeFromUri(context, Uri.parse(uri));
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String extension = mime.getExtensionFromMimeType(mimeType);

			// If we can't get the mime type from the uri, try getting it from the extension.
			if (extension == null) {
				extension = MimeTypeMap.getFileExtensionFromUrl(uri);
			}
			if (mimeType == null && extension != null) {
				mimeType = mime.getMimeTypeFromExtension(extension);
			}
			if (!TextUtils.isEmpty(extension)) {
				localFilePath += "." + extension;
			}
			StoredFile storedFile = Util.createLocalStoredFile(uri, localFilePath, mimeType);
			if (storedFile == null) {
				return;
			}

			storedFile.setId(message.getNonce());
			attachmentStoredFiles.add(storedFile);

			message.setAssociatedFiles(attachmentStoredFiles);
			MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();
			if (mgr != null) {
				mgr.sendMessage(message);
			}

		} catch (Exception e) {
			Log.w("Error sending attachment file.", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param context  The Context from which this method was called.
	 * @param content  A byte array of the file contents.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(Context context, byte[] content, String mimeType) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ByteArrayInputStream is = null;
			try {
				is = new ByteArrayInputStream(content);
				sendAttachmentFile(is, mimeType);
			} finally {
				Util.ensureClosed(is);
			}
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param is       An InputStream from the desired file.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(InputStream is, String mimeType) {
		try {
			if (is == null || !ApptentiveInternal.isApptentiveRegistered()) {
				return;
			}

			CompoundMessage message = new CompoundMessage();
			// No body, just attachment
			message.setBody(null);
			message.setRead(true);
			message.setHidden(true);
			message.setSenderId(ApptentiveInternal.getInstance().getPersonId());

			ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
			String localFilePath = Util.generateCacheFilePathFromNonceOrPrefix(ApptentiveInternal.getInstance().getApplicationContext(),
					message.getNonce(), null);

			String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
			if (!TextUtils.isEmpty(extension)) {
				localFilePath += "." + extension;
			}
			// When created from InputStream, there is no source file uri or path, thus just use the cache file path
			StoredFile storedFile = Util.createLocalStoredFile(is, localFilePath, localFilePath, mimeType);
			if (storedFile == null) {
				return;
			}
			storedFile.setId(message.getNonce());
			attachmentStoredFiles.add(storedFile);

			message.setAssociatedFiles(attachmentStoredFiles);
				ApptentiveInternal.getInstance().getMessageManager().sendMessage(message);
		} catch (Exception e) {
			Log.w("Error sending attachment file.", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, figures out
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param activity The Activity from which this method is called.
	 * @param event    A unique String representing the line this method is called on. For instance, you may want to have
	 *                 the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                 call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Activity activity, String event) {
		return EngagementModule.engage(activity, "local", "app", null, event, null, null, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, figures out
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param activity   The Activity from which this method is called.
	 * @param event      A unique String representing the line this method is called on. For instance, you may want to have
	 *                   the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                   call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans. This data
	 *                   is sent to the server for tracking information in the context of the engaged Event.
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Activity activity, String event, Map<String, Object> customData) {
		return EngagementModule.engage(activity, "local", "app", null, event, null, customData, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, figures out
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param activity     The Activity from which this method is called.
	 * @param event        A unique String representing the line this method is called on. For instance, you may want to have
	 *                     the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                     call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @param customData   A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans. This data
	 *                     is sent to the server for tracking information in the context of the engaged Event.
	 * @param extendedData An array of ExtendedData objects. ExtendedData objects used to send structured data that has
	 *                     specific meaning to the server. By using an {@link ExtendedData} object instead of arbitrary
	 *                     customData, special meaning can be derived. Supported objects include {@link TimeExtendedData},
	 *                     {@link LocationExtendedData}, and {@link CommerceExtendedData}. Include each type only once.
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Activity activity, String event, Map<String, Object> customData, ExtendedData... extendedData) {
		return EngagementModule.engage(activity, "local", "app", null, event, null, customData, extendedData);
	}

	/**
	 * @param event   A unique String representing the line this method is called on. For instance, you may want to have
	 *                the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @return true if an immediate call to engage() with the same event name would result in an Interaction being displayed, otherwise false.
	 * @deprecated Use {@link #canShowInteraction(String)}() instead. The behavior is identical. Only the name has changed.
	 */
	public static synchronized boolean willShowInteraction(String event) {
		return canShowInteraction(event);
	}

	/**
	 * This method can be used to determine if a call to one of the <strong><code>engage()</code></strong> methods such as
	 * {@link #engage(Activity, String)} using the same event name will
	 * result in the display of an  Interaction. This is useful if you need to know whether an Interaction will be
	 * displayed before you create a UI Button, etc.
	 *
	 * @param event   A unique String representing the line this method is called on. For instance, you may want to have
	 *                the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @return true if an immediate call to engage() with the same event name would result in an Interaction being displayed, otherwise false.
	 */
	public static synchronized boolean canShowInteraction(String event) {
		try {
			return EngagementModule.canShowInteraction("local", "app", event);
		} catch (Exception e) {
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	/**
	 * Pass in a listener. The listener will be called whenever a survey is finished.
	 *
	 * @param listener The {@link com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener} listener to call when the survey is finished.
	 */
	public static void setOnSurveyFinishedListener(OnSurveyFinishedListener listener) {
		ApptentiveInternal internal = ApptentiveInternal.getInstance();
		if (internal != null) {
			internal.setOnSurveyFinishedListener(listener);
		}
	}

	/**
	 * <p>This type represents a <a href="http://semver.org/">semantic version</a>. It can be initialized
	 * with a string or a long, and there is no limit to the number of parts your semantic version can
	 * contain. The class allows comparison based on semantic version rules.
	 * Valid versions (In sorted order):</p>
	 * <ul>
	 * <li>0</li>
	 * <li>0.1</li>
	 * <li>1.0.0</li>
	 * <li>1.0.9</li>
	 * <li>1.0.10</li>
	 * <li>1.2.3</li>
	 * <li>5</li>
	 * </ul>
	 * Invalid versions:
	 * <ul>
	 * <li>zero</li>
	 * <li>0.1+2015.10.21</li>
	 * <li>1.0.0a</li>
	 * <li>1.0-rc2</li>
	 * <li>1.0.10-SNAPSHOT</li>
	 * <li>5a</li>
	 * <li>FF01</li>
	 * </ul>
	 */
	public static class Version extends JSONObject implements Comparable<Version> {
		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "version";

		public Version() {
		}

		public Version(String json) throws JSONException {
			super(json);
		}

		public Version(long version) {
			super();
			setVersion(version);
		}

		public void setVersion(String version) {
			try {
				put(KEY_TYPE, TYPE);
				put(TYPE, version);
			} catch (JSONException e) {
				Log.e("Error creating Apptentive.Version.", e);
			}
		}

		public void setVersion(long version) {
			setVersion(Long.toString(version));
		}

		public String getVersion() {
			return optString(TYPE, null);
		}

		@Override
		public int compareTo(Version other) {
			String thisVersion = getVersion();
			String thatVersion = other.getVersion();
			String[] thisArray = thisVersion.split("\\.");
			String[] thatArray = thatVersion.split("\\.");

			int maxParts = Math.max(thisArray.length, thatArray.length);
			for (int i = 0; i < maxParts; i++) {
				// If one SemVer has more parts than another, treat pad out the short one with zeros in each slot.
				long left = 0;
				if (thisArray.length > i) {
					left = Long.parseLong(thisArray[i]);
				}
				long right = 0;
				if (thatArray.length > i) {
					right = Long.parseLong(thatArray[i]);
				}
				if (left < right) {
					return -1;
				} else if (left > right) {
					return 1;
				}
			}
			return 0;
		}

		@Override
		public String toString() {
			return getVersion();
		}
	}

	public static class DateTime extends JSONObject implements Comparable<DateTime> {
		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "datetime";
		public static final String SEC = "sec";

		public DateTime(String json) throws JSONException {
			super(json);
		}

		public DateTime(double dateTime) {
			super();
			setDateTime(dateTime);
		}

		public void setDateTime(double dateTime) {
			try {
				put(KEY_TYPE, TYPE);
				put(SEC, dateTime);
			} catch (JSONException e) {
				Log.e("Error creating Apptentive.DateTime.", e);
			}
		}

		public double getDateTime() {
			return optDouble(SEC);
		}

		@Override
		public String toString() {
			return Double.toString(getDateTime());
		}

		@Override
		public int compareTo(DateTime other) {
			double thisDateTime = getDateTime();
			double thatDateTime = other.getDateTime();
			return Double.compare(thisDateTime, thatDateTime);
		}
	}
}
