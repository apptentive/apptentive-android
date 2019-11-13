/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.conversation.ConversationDispatchTask;
import com.apptentive.android.sdk.conversation.ConversationProxy;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.lifecycle.ApptentiveActivityLifecycleCallbacks;
import com.apptentive.android.sdk.model.CommerceExtendedData;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.model.LocationExtendedData;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.model.TimeExtendedData;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.ObjectUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveHelper.checkConversationQueue;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchConversationTask;
import static com.apptentive.android.sdk.ApptentiveHelper.dispatchOnConversationQueue;
import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;
import static com.apptentive.android.sdk.ApptentiveLogTag.MESSAGES;
import static com.apptentive.android.sdk.ApptentiveLogTag.PUSH;
import static com.apptentive.android.sdk.util.StringUtils.trim;

/**
 * This class contains the complete public API for accessing Apptentive features from within your app.
 */
public class Apptentive {

	private static OnPreInteractionListener preInteractionListener;

	/**
	 * Must be called from the {@link Application#onCreate()} method in the {@link Application} object defined in your app's manifest.
	 * Note: application key and signature would be resolved from the AndroidManifest.xml
	 *
	 * @param application The {@link Application} object for this app.
	 * @deprecated Please, use {@link #register(Application, String, String)} or {@link #register(Application, ApptentiveConfiguration)} instead
	 */
	@Deprecated
	public static void register(Application application) {
		if (application == null) {
			throw new IllegalArgumentException("Application is null");
		}

		String apptentiveKey = Util.getManifestMetadataString(application, Constants.MANIFEST_KEY_APPTENTIVE_KEY);
		if (StringUtils.isNullOrEmpty(apptentiveKey)) {
			ApptentiveLog.e("Unable to initialize Apptentive SDK: '%s' manifest key is missing", Constants.MANIFEST_KEY_APPTENTIVE_KEY);
			return;
		}

		String apptentiveSignature = Util.getManifestMetadataString(application, Constants.MANIFEST_KEY_APPTENTIVE_SIGNATURE);
		if (StringUtils.isNullOrEmpty(apptentiveSignature)) {
			ApptentiveLog.e("Unable to initialize Apptentive SDK: '%s' manifest key is missing", Constants.MANIFEST_KEY_APPTENTIVE_SIGNATURE);
			return;
		}

		ApptentiveConfiguration configuration = new ApptentiveConfiguration(apptentiveKey, apptentiveSignature);

		String logLevelString = Util.getManifestMetadataString(application, Constants.MANIFEST_KEY_APPTENTIVE_LOG_LEVEL);
		ApptentiveLog.Level logLevel = ApptentiveLog.Level.parse(logLevelString);
		if (logLevel != ApptentiveLog.Level.UNKNOWN) {
			configuration.setLogLevel(logLevel);
		}

		register(application, configuration);
	}

	/**
	 * Must be called from the {@link Application#onCreate()} method in the {@link Application} object defined in your app's manifest.
	 * @param application Application object.
	 * @param apptentiveKey Apptentive Key.
	 * @param apptentiveSignature Apptentive Signature.
	 */
	public static void register(Application application, String apptentiveKey, String apptentiveSignature) {
		register(application, new ApptentiveConfiguration(apptentiveKey, apptentiveSignature));
	}

	/**
	 * Must be called from the {@link Application#onCreate()} method in the {@link Application} object defined in your app's manifest.
	 * @param application Application object.
	 * @param configuration Apptentive configuration containing SDK initialization data.
	 */
	public static void register(Application application, ApptentiveConfiguration configuration) {
		if (application == null) {
			throw new IllegalArgumentException("Application is null");
		}

		if (configuration == null) {
			throw new IllegalArgumentException("Apptentive configuration is null");
		}

		if (!Availability.isAndroidX()) {
			ApptentiveLog.e("Unable to register Apptentive SDK: AndroidX support required. For more information see: https://learn.apptentive.com/knowledge-base/android-integration-reference/#migrating-from-support-library-to-androidx");
			return;
		}

		try {
			ApptentiveInternal.createInstance(application, configuration);
		} catch (Exception e) {
			ApptentiveLog.e(e, "Exception while registering Apptentive SDK");
			logException(e);
		}
	}

	/**
	 * Register application callbacks. You only need to call this if you register Apptentive instance outside of your
	 * Application class.
	 */
	public static void registerCallbacks(Application application) {
		ApptentiveActivityLifecycleCallbacks.register(application);
	}

	//region Global Data Methods

	/**
	 * Sets the user's email address. This email address will be sent to the Apptentive server to allow out of app
	 * communication, and to help provide more context about this user. This email will be the definitive email address
	 * for this user, unless one is provided directly by the user through an Apptentive UI. Calls to this method are
	 * idempotent. Calls to this method will overwrite any previously entered email, so if you don't want to overwrite
	 * the email provided by the user, make sure to check the value with {@link #getPersonEmail()} before you call this method.
	 *
	 * @param email The user's email address.
	 */
	public static void setPersonEmail(final String email) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal.getInstance().getConversationProxy().setPersonEmail(email);
				return true;
			}
		}, "set person email");
	}

	/**
	 * Retrieves the user's email address. This address may be set via {@link #setPersonEmail(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's email if set, else null.
	 */
	public static String getPersonEmail() {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				ConversationProxy conversation = ApptentiveInternal.getInstance().getConversationProxy();
				if (conversation != null) {
					return conversation.getPersonEmail();
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION,"Exception while getting person email");
			logException(e);
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
	 * @param name The user's name.
	 */
	public static void setPersonName(final String name) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal.getInstance().getConversationProxy().setPersonName(name);
				return true;
			}
		}, "set person name");
	}

	/**
	 * Retrieves the user's name. This name may be set via {@link #setPersonName(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's name if set, else null.
	 */
	public static String getPersonName() {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				ConversationProxy conversation = ApptentiveInternal.getInstance().getConversationProxy();
				if (conversation != null) {
					return conversation.getPersonName();
				}
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, "Exception while getting person name");
			logException(e);
		}
		return null;
	}

	/**
	 * Add a custom data String to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A String value.
	 */
	public static void addCustomDeviceData(final String key, final String value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().put(key, trim(value));
				return true;
			}
		}, "add custom device data");
	}

	/**
	 * Add a custom data Number to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Number value.
	 */
	public static void addCustomDeviceData(final String key, final Number value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().put(key, value);
				return true;
			}
		}, "add custom device data");
	}

	/**
	 * Add a custom data Boolean to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Boolean value.
	 */
	public static void addCustomDeviceData(final String key, final Boolean value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().put(key, value);
				return true;
			}
		}, "add custom device data");
	}

	private static void addCustomDeviceData(final String key, final Version version) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().put(key, version);
				return true;
			}
		}, "add custom device data");
	}

	private static void addCustomDeviceData(final String key, final DateTime dateTime) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().put(key, dateTime);
				return true;
			}
		}, "add custom device data");
	}

	/**
	 * Remove a piece of custom data from the device. Calls to this method are idempotent.
	 *
	 * @param key The key to remove.
	 */
	public static void removeCustomDeviceData(final String key) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getDevice().getCustomData().remove(key);
				return true;
			}
		}, "remove custom device data");
	}

	/**
	 * Add a custom data String to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A String value.
	 */
	public static void addCustomPersonData(final String key, final String value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().put(key, trim(value));
				return true;
			}
		}, "add custom person data");
	}

	/**
	 * Add a custom data Number to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Number value.
	 */
	public static void addCustomPersonData(final String key, final Number value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().put(key, value);
				return true;
			}
		}, "add custom person data");
	}

	/**
	 * Add a custom data Boolean to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Boolean value.
	 */
	public static void addCustomPersonData(final String key, final Boolean value) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().put(key, value);
				return true;
			}
		}, "add custom person data");
	}

	private static void addCustomPersonData(final String key, final Version version) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().put(key, version);
				return true;
			}
		}, "add custom person data");
	}

	private static void addCustomPersonData(final String key, final DateTime dateTime) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().put(key, dateTime);
				return true;
			}
		}, "add custom person data");
	}

	/**
	 * Remove a piece of custom data from the Person. Calls to this method are idempotent.
	 *
	 * @param key The key to remove.
	 */
	public static void removeCustomPersonData(final String key) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getPerson().getCustomData().remove(key);
				return true;
			}
		}, "remove custom person data");
	}

	//endregion

	//region Third Party Integrations

	/**
	 * For internal use only.
	 */
	public static final String INTEGRATION_PUSH_TOKEN = "token";

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
	 *                     <dd>If you are using Apptentive to send pushes directly to GCM or FCM, pass in the GCM/FCM Registration ID, which you can
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
	public static void setPushNotificationIntegration(final int pushProvider, final String token) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				// Store the push stuff globally
				SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
				prefs.edit().putInt(Constants.PREF_KEY_PUSH_PROVIDER, pushProvider)
						.putString(Constants.PREF_KEY_PUSH_TOKEN, token)
						.apply();

				// Also set it on the active Conversation, if there is one.
				conversation.setPushIntegration(pushProvider, token);
				return true;
			}
		}, "set push notification integration");
	}

	//endregion

	//region Push Notifications

	/**
	 * Determines whether this Intent is a push notification sent from Apptentive.
	 *
	 * @param intent The received {@link Intent} you received in your BroadcastReceiver.
	 * @return True if the Intent came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Intent intent) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return false;
			}
			return ApptentiveInternal.getApptentivePushNotificationData(intent) != null;
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while checking for Apptentive push notification intent");
			logException(e);
		}
		return false;
	}

	/**
	 * Determines whether this Bundle came from an Apptentive push notification. This method is used with Urban Airship
	 * integrations.
	 *
	 * @param bundle The push payload bundle passed to GCM onMessageReceived() callback
	 * @return True if the push came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Bundle bundle) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return false;
			}
			return ApptentiveInternal.getApptentivePushNotificationData(bundle) != null;
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while checking for Apptentive push notification bundle");
			logException(e);
		}
		return false;
	}

	/**
	 * Determines whether push payload data came from an Apptentive push notification.
	 *
	 * @param data The push payload data obtained through FCM's RemoteMessage.getData(), when using FCM
	 * @return True if the push came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Map<String, String> data) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return false;
			}
			return ApptentiveInternal.getApptentivePushNotificationData(data) != null;
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while checking for Apptentive push notification data");
			logException(e);
		}
		return false;
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link androidx.core.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Intent} that did
	 * not come from Apptentive will return a null object. If you receive a null object, your app will
	 * need to handle this notification itself.</p>
	 * <p>This task is performed asynchronously.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>GCM</li>
	 * <li>AWS SNS</li>
	 * <li>Parse</li>
	 * </ul>
	 *
	 * @param callback Called after we check to see Apptentive can launch an Interaction from this
	 *                 push. Called with a {@link PendingIntent} to launch an Apptentive Interaction
	 *                 if the push data came from Apptentive, and an Interaction can be shown, or
	 *                 null.
	 * @param intent   An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *                 in the Service or BroadcastReceiver that is used by your chosen push provider.
	 */
	public static void buildPendingIntentFromPushNotification(@NonNull final PendingIntentCallback callback, @NonNull final Intent intent) {
		if (callback == null) {
			throw new IllegalArgumentException("Callback is null");
		}

		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(intent);
				final PendingIntent intent = ApptentiveInternal.generatePendingIntentFromApptentivePushData(conversation, apptentivePushData);
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						callback.onPendingIntent(intent);
					}
				});
				return true;
			}
		}, "build pending intent");
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link androidx.core.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Bundle} that
	 * did not come from Apptentive will return a null object. If you receive a null object, your app
	 * will need to handle this notification itself.</p>
	 * <p>This task is performed asynchronously.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>Urban Airship</li>
	 * </ul>
	 *
	 * @param callback Called after we check to see Apptentive can launch an Interaction from this
	 *                 push. Called with a {@link PendingIntent} to launch an Apptentive Interaction
	 *                 if the push data came from Apptentive, and an Interaction can be shown, or
	 *                 null.
	 * @param bundle   A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *                 the the Service or BroadcastReceiver that is used by your chosen push provider.
	 */
	public static void buildPendingIntentFromPushNotification(@NonNull final PendingIntentCallback callback, @NonNull final Bundle bundle) {
		if (callback == null) {
			throw new IllegalArgumentException("Callback is null");
		}

		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(bundle);
				final PendingIntent intent = ApptentiveInternal.generatePendingIntentFromApptentivePushData(conversation, apptentivePushData);
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						callback.onPendingIntent(intent);
					}
				});
				return true;
			}
		}, "build pending intent");
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link androidx.core.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Bundle} that
	 * did not come from Apptentive will return a null object. If you receive a null object, your app
	 * will need to handle this notification itself.</p>
	 * <p>This task is performed asynchronously.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>Firebase Cloud Messaging (FCM)</li>
	 * </ul>
	 *
	 * @param callback Called after we check to see Apptentive can launch an Interaction from this
	 *                 push. Called with a {@link PendingIntent} to launch an Apptentive Interaction
	 *                 if the push data came from Apptentive, and an Interaction can be shown, or
	 *                 null.
	 * @param data     A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive
	 *                 Push data. Pass in what you receive in the the Service or BroadcastReceiver
	 *                 that is used by your chosen push provider.
	 */
	public static void buildPendingIntentFromPushNotification(final PendingIntentCallback callback, @NonNull final Map<String, String> data) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(data);
				final PendingIntent intent = ApptentiveInternal.generatePendingIntentFromApptentivePushData(conversation, apptentivePushData);
				DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
					@Override
					protected void execute() {
						callback.onPendingIntent(intent);
					}
				});
				return true;
			}
		}, "build pending intent");
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param intent An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *               in the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (intent != null) {
			return getTitleFromApptentivePush(intent.getExtras());
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param intent An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *               in the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (intent != null) {
			return getBodyFromApptentivePush(intent.getExtras());
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param bundle A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *               the the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Bundle bundle) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return null;
			}
			if (bundle == null) {
				return null;
			}
			if (bundle.containsKey(ApptentiveInternal.TITLE_DEFAULT)) {
				return bundle.getString(ApptentiveInternal.TITLE_DEFAULT);
			}
			if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE)) {
				String parseDataString = bundle.getString(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE);
				if (parseDataString != null) {
					try {
						JSONObject parseJson = new JSONObject(parseDataString);
						return parseJson.optString(ApptentiveInternal.TITLE_DEFAULT, null);
					} catch (JSONException e) {
						logException(e);
						return null;
					}
				}
			} else if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_UA)) {
				Bundle uaPushBundle = bundle.getBundle(ApptentiveInternal.PUSH_EXTRA_KEY_UA);
				if (uaPushBundle == null) {
					return null;
				}
				return uaPushBundle.getString(ApptentiveInternal.TITLE_DEFAULT);
			}
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while getting title from Apptentive push");
			logException(e);
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param bundle A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *               the the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Bundle bundle) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return null;
			}
			if (bundle == null) {
				return null;
			}
			if (bundle.containsKey(ApptentiveInternal.BODY_DEFAULT)) {
				return bundle.getString(ApptentiveInternal.BODY_DEFAULT);
			}
			if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE)) {
				String parseDataString = bundle.getString(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE);
				if (parseDataString != null) {
					try {
						JSONObject parseJson = new JSONObject(parseDataString);
						return parseJson.optString(ApptentiveInternal.BODY_PARSE, null);
					} catch (JSONException e) {
						logException(e);
						return null;
					}
				}
			} else if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_UA)) {
				Bundle uaPushBundle = bundle.getBundle(ApptentiveInternal.PUSH_EXTRA_KEY_UA);
				if (uaPushBundle == null) {
					return null;
				}
				return uaPushBundle.getString(ApptentiveInternal.BODY_UA);
			} else if (bundle.containsKey(ApptentiveInternal.BODY_UA)) {
				return bundle.getString(ApptentiveInternal.BODY_UA);
			}
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while getting body from Apptentive push");
			logException(e);
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param data A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive Push
	 *             data. Pass in what you receive in the the Service or BroadcastReceiver that is
	 *             used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Map<String, String> data) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return null;
			}
			if (data == null) {
				return null;
			}
			return data.get(ApptentiveInternal.TITLE_DEFAULT);
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while getting title from Apptentive push");
			logException(e);
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param data A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive Push
	 *             data. Pass in what you receive in the the Service or BroadcastReceiver that is
	 *             used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Map<String, String> data) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return null;
			}
			if (data == null) {
				return null;
			}
			return data.get(ApptentiveInternal.BODY_DEFAULT);
		} catch (Exception e) {
			ApptentiveLog.e(PUSH, e, "Exception while getting body from Apptentive push");
			logException(e);
		}
		return null;
	}

	//endregion

	//region Rating

	/**
	 * Use this to choose where to send the user when they are prompted to rate the app. This should be the same place
	 * that the app was downloaded from.
	 *
	 * @param ratingProvider A {@link IRatingProvider} value.
	 */

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				ApptentiveInternal.getInstance().setRatingProvider(ratingProvider);
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while setting rating provider");
			logException(e);
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
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				ApptentiveInternal.getInstance().putRatingProviderArg(key, value);
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while putting rating provider arg");
			logException(e);
		}
	}

	//endregion

	//region Message Center

	/**
	 * Opens the Apptentive Message Center UI Activity. This task is performed asynchronously. Message
	 * Center configuration may not have been downloaded yet when this is called. If you would like to
	 * know whether this method was able to launch Message Center, use
	 * {@link Apptentive#showMessageCenter(Context, BooleanCallback)}.
	 *
	 * @param context The context from which to launch the Message Center
	 */
	public static void showMessageCenter(Context context) {
		showMessageCenter(context, null, null);
	}

	/**
	 * Opens the Apptentive Message Center UI Activity. This task is performed asynchronously. Message
	 * Center configuration may not have been downloaded yet when this is called.
	 *
	 * @param context  The context from which to launch the Message Center
	 * @param callback Called after we check to see if Message Center can be displayed, but before it
	 *                 is displayed. Called with true if an Interaction will be displayed, else false.
	 */
	public static void showMessageCenter(Context context, BooleanCallback callback) {
		showMessageCenter(context, callback, null);
	}

	/**
	 * Opens the Apptentive Message Center UI Activity, and allows custom data to be sent with the
	 * next message the user sends. If the user sends multiple messages, this data will only be sent
	 * with the first message sent after this method is invoked. Additional invocations of this method
	 * with custom data will repeat this process. If Message Center is closed without a message being
	 * sent, the custom data is cleared. This task is performed asynchronously. Message Center
	 * configuration may not have been downloaded yet when this is called. If you would like to know
	 * whether this method was able to launch Message Center, use
	 * {@link Apptentive#showMessageCenter(Context, BooleanCallback, Map)}.
	 *
	 * @param context    The context from which to launch the Message Center. This should be an
	 *                   Activity, except in rare cases where you don't have access to one, in which
	 *                   case Apptentive Message Center will launch in a new task.
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans.
	 *                   If any message is sent by the Person, this data is sent with it, and then
	 *                   cleared. If no message is sent, this data is discarded.
	 */
	public static void showMessageCenter(Context context, Map<String, Object> customData) {
		showMessageCenter(context, null, customData);
	}

	/**
	 * Opens the Apptentive Message Center UI Activity, and allows custom data to be sent with the
	 * next message the user sends. If the user sends multiple messages, this data will only be sent
	 * with the first message sent after this method is invoked. Additional invocations of this method
	 * with custom data will repeat this process. If Message Center is closed without a message being
	 * sent, the custom data is cleared. This task is performed asynchronously. Message Center
	 * configuration may not have been downloaded yet when this is called.
	 *
	 * @param context    The context from which to launch the Message Center. This should be an
	 *                   Activity, except in rare cases where you don't have access to one, in which
	 *                   case Apptentive Message Center will launch in a new task.
	 * @param callback   Called after we check to see if Message Center can be displayed, but before
	 *                   it is displayed. Called with true if an Interaction will be displayed, else
	 *                   false.
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or
	 *                   Booleans. If any message is sent by the Person, this data is sent with it,
	 *                   and then cleared. If no message is sent, this data is discarded.
	 */
	public static void showMessageCenter(final Context context, final BooleanCallback callback, final Map<String, Object> customData) {
		dispatchConversationTask(new ConversationDispatchTask(callback, DispatchQueue.mainQueue()) {
			@Override
			protected boolean execute(Conversation conversation) {
				return ApptentiveInternal.getInstance().showMessageCenterInternal(context, customData);
			}
		}, "show message center");
	}

	/**
	 * Our SDK must connect to our server at least once to download initial configuration for Message
	 * Center. Call this method to see whether or not Message Center can be displayed. This task is
	 * performed asynchronously.
	 *
	 * @param callback Called after we check to see if Message Center can be displayed, but before it
	 *                 is displayed. Called with true if an Interaction will be displayed, else false.
	 */
	public static void canShowMessageCenter(BooleanCallback callback) {
		dispatchConversationTask(new ConversationDispatchTask(callback, DispatchQueue.mainQueue()) {
			@Override
			protected boolean execute(Conversation conversation) {
				return ApptentiveInternal.canShowMessageCenterInternal(conversation);
			}
		}, "check message center availability");
	}

	/**
	 * Set one and only listener to be notified when the number of unread messages in the Message Center changes.
	 * if the app calls this method to set up a custom listener, the apptentive unread message badge, also an UnreadMessagesListener,
	 * won't get notification. Please use {@link #addUnreadMessagesListener(UnreadMessagesListener)} instead.
	 *
	 * @param listener An UnreadMessagesListener that you instantiate. Pass null to remove existing listener.
	 *                 Do not pass in an anonymous class, such as setUnreadMessagesListener(new UnreadMessagesListener() {...}).
	 *                 Instead, create your listener as an instance variable and pass that in. This
	 *                 allows us to keep a weak reference to avoid memory leaks.
	 */
	@Deprecated
	public static void setUnreadMessagesListener(final UnreadMessagesListener listener) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getMessageManager().setHostUnreadMessagesListener(listener);
				return true;
			}
		}, "set unread message listener");
	}

	/**
	 * Add a listener to be notified when the number of unread messages in the Message Center changes.
	 *
	 * @param listener An UnreadMessagesListener that you instantiate. Do not pass in an anonymous class.
	 *                 Instead, create your listener as an instance variable and pass that in. This
	 *                 allows us to keep a weak reference to avoid memory leaks.
	 */
	public static void addUnreadMessagesListener(final UnreadMessagesListener listener) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				conversation.getMessageManager().addHostUnreadMessagesListener(listener);
				return true;
			}
		}, "add unread message listener");
	}

	/**
	 * Returns the number of unread messages in the Message Center.
	 *
	 * @return The number of unread messages.
	 */
	public static int getUnreadMessageCount() {
		try {
			if (ApptentiveInternal.isApptentiveRegistered()) {
				ConversationProxy conversationProxy = ApptentiveInternal.getInstance().getConversationProxy();
				return conversationProxy != null ? conversationProxy.getUnreadMessageCount() : 0;
			}
		} catch (Exception e) {
			ApptentiveLog.e(MESSAGES, e, "Exception while getting unread message count");
			logException(e);
		}
		return 0;
	}

	/**
	 * Sends a text message to the server. This message will be visible in the conversation view on the server, but will
	 * not be shown in the client's Message Center.
	 *
	 * @param text The message you wish to send.
	 */
	public static void sendAttachmentText(final String text) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				CompoundMessage message = new CompoundMessage();
				message.setBody(text);
				message.setRead(true);
				message.setHidden(true);
				message.setSenderId(conversation.getPerson().getId());
				message.setAssociatedFiles(null);
				conversation.getMessageManager().sendMessage(message);
				return true;
			}
		}, "send attachment text");
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param uri The URI of the local resource file.
	 */
	public static void sendAttachmentFile(final String uri) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				if (TextUtils.isEmpty(uri)) {
					return false; // TODO: add error message
				}
				CompoundMessage message = new CompoundMessage();
				// No body, just attachment
				message.setBody(null);
				message.setRead(true);
				message.setHidden(true);
				message.setSenderId(conversation.getPerson().getId());

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
					return false; // TODO: add error message
				}

				storedFile.setId(message.getNonce());
				attachmentStoredFiles.add(storedFile);

				message.setAssociatedFiles(attachmentStoredFiles);
				conversation.getMessageManager().sendMessage(message);
				return true;
			}
		}, "send attachment file");
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param content  A byte array of the file contents.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(final byte[] content, final String mimeType) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ByteArrayInputStream is = null;
				try {
					is = new ByteArrayInputStream(content);
					sendAttachmentFile(is, mimeType);
				} finally {
					Util.ensureClosed(is);
				}
				return true;
			}
		}, "send attachment file");
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param is       An InputStream from the desired file.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(final InputStream is, final String mimeType) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				if (is == null) {
					return false; // TODO: add error message
				}
				CompoundMessage message = new CompoundMessage();
				// No body, just attachment
				message.setBody(null);
				message.setRead(true);
				message.setHidden(true);
				message.setSenderId(conversation.getPerson().getId());

				ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
				String localFilePath = Util.generateCacheFilePathFromNonceOrPrefix(ApptentiveInternal.getInstance().getApplicationContext(), message.getNonce(), null);

				String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
				if (!TextUtils.isEmpty(extension)) {
					localFilePath += "." + extension;
				}
				// When created from InputStream, there is no source file uri or path, thus just use the cache file path
				StoredFile storedFile = Util.createLocalStoredFile(is, localFilePath, localFilePath, mimeType);
				if (storedFile == null) {
					return false; // TODO: add error message
				}
				storedFile.setId(message.getNonce());
				attachmentStoredFiles.add(storedFile);

				message.setAssociatedFiles(attachmentStoredFiles);
				conversation.getMessageManager().sendMessage(message);
				return true;
			}
		}, "add unread message listener");
	}

	//endregion

	//region Engagement

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method. This task is performed
	 * asynchronously. If you would like to know whether this method will launch an Apptentive
	 * Interaction, use {@link Apptentive#engage(Context, String, BooleanCallback)}
	 *
	 * @param context The context from which to launch the Interaction. This should be an Activity,
	 *                except in rare cases where you don't have access to one, in which case
	 *                Apptentive Interactions will launch in a new task.
	 * @param event   A unique String representing the line this method is called on. For instance,
	 *                you may want to have the ability to target interactions to run after the user
	 *                uploads a file in your app. You may then call
	 *                <strong><code>engage(context, "finished_upload");</code></strong>
	 */
	public static synchronized void engage(Context context, String event) {
		engage(context, event, null, null, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method. This task is performed
	 * asynchronously.
	 *
	 * @param context  The context from which to launch the Interaction. This should be an Activity,
	 *                 except in rare cases where you don't have access to one, in which case
	 *                 Apptentive Interactions will launch in a new task.
	 * @param event    A unique String representing the line this method is called on. For instance,
	 *                 you may want to have the ability to target interactions to run after the user
	 *                 uploads a file in your app. You may then call
	 *                 <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param callback Called after we check to see if an Interaction should be displayed. Called with
	 *                 true if an Interaction will be displayed, else false.
	 */
	public static synchronized void engage(Context context, String event, BooleanCallback callback) {
		engage(context, event, callback, null, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method. This task is performed
	 * asynchronously. If you would like to know whether this method will launch an Apptentive
	 * Interaction, use {@link Apptentive#engage(Context, String, BooleanCallback, Map)}.
	 *
	 * @param context    The context from which to launch the Interaction. This should be an Activity,
	 *                   except in rare cases where you don't have access to one, in which case
	 *                   Apptentive Interactions will launch in a new task.
	 * @param event      A unique String representing the line this method is called on. For instance,
	 *                   you may want to have the ability to target interactions to run after the user
	 *                   uploads a file in your app. You may then call
	 *                   <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or
	 *                   Booleans. This data is sent to the server for tracking information in the
	 *                   context of the engaged Event.
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized void engage(Context context, String event, Map<String, Object> customData) {
		engage(context, event, null, customData, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method. This task is performed
	 * asynchronously.
	 *
	 * @param context    The context from which to launch the Interaction. This should be an Activity,
	 *                   except in rare cases where you don't have access to one, in which case
	 *                   Apptentive Interactions will launch in a new task.
	 * @param event      A unique String representing the line this method is called on. For instance,
	 *                   you may want to have the ability to target interactions to run after the user
	 *                   uploads a file in your app. You may then call
	 *                   <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param callback   Called after we check to see if an Interaction should be displayed. Called with
	 *                   true if an Interaction will be displayed, else false.
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or
	 *                   Booleans. This data is sent to the server for tracking information in the
	 *                   context of the engaged Event.
	 */
	public static synchronized void engage(Context context, String event, BooleanCallback callback, Map<String, Object> customData) {
		engage(context, event, callback, customData, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method. This task is performed
	 * asynchronously. If you would like to know whether this method will launch an Apptentive
	 * Interaction, use {@link Apptentive#engage(Context, String, BooleanCallback, Map, ExtendedData...)}.
	 *
	 * @param context      The context from which to launch the Interaction. This should be an
	 *                     Activity, except in rare cases where you don't have access to one, in which
	 *                     case Apptentive Interactions will launch in a new task.
	 * @param event        A unique String representing the line this method is called on.
	 *                     For instance, you may want to have the ability to target interactions to
	 *                     run after the user uploads a file in your app. You may then call
	 *                     <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param customData   A Map of String keys to Object values. Objects may be Strings, Numbers, or
	 *                     Booleans. This data is sent to the server for tracking information in the
	 *                     context of the engaged Event.
	 * @param extendedData An array of ExtendedData objects. ExtendedData objects used to send
	 *                     structured data that has specific meaning to the server. By using an
	 *                     {@link ExtendedData} object instead of arbitrary customData, special
	 *                     meaning can be derived. Supported objects include {@link TimeExtendedData},
	 *                     {@link LocationExtendedData}, and {@link CommerceExtendedData}. Include
	 *                     each type only once.
	 */
	public static synchronized void engage(Context context, String event, Map<String, Object> customData, ExtendedData... extendedData) {
		engage(context, event, null, customData, extendedData);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited,
	 * determines if there is an interaction that is able to run for this event, and then runs it. If
	 * more than one interaction can run, then the most appropriate interaction takes precedence. Only
	 * one interaction at most will run per invocation of this method.
	 *
	 * @param context      The context from which to launch the Interaction. This should be an
	 *                     Activity, except in rare cases where you don't have access to one, in which
	 *                     case Apptentive Interactions will launch in a new task.
	 * @param event        A unique String representing the line this method is called on.
	 *                     For instance, you may want to have the ability to target interactions to
	 *                     run after the user uploads a file in your app. You may then call
	 *                     <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param callback     Called after we check to see if an Interaction should be displayed. Called with
	 *                     true if an Interaction will be displayed, else false.
	 * @param customData   A Map of String keys to Object values. Objects may be Strings, Numbers, or
	 *                     Booleans. This data is sent to the server for tracking information in the
	 *                     context of the engaged Event.
	 * @param extendedData An array of ExtendedData objects. ExtendedData objects used to send
	 *                     structured data that has specific meaning to the server. By using an
	 *                     {@link ExtendedData} object instead of arbitrary customData, special
	 *                     meaning can be derived. Supported objects include {@link TimeExtendedData},
	 *                     {@link LocationExtendedData}, and {@link CommerceExtendedData}. Include
	 *                     each type only once.
	 */
	public static synchronized void engage(final Context context, final String event, final BooleanCallback callback, final Map<String, Object> customData, final ExtendedData... extendedData) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}

		if (StringUtils.isNullOrEmpty(event)) {
			throw new IllegalArgumentException("Event is null or empty");
		}

		// first, we check if there's an engagement callback to inject
		final OnPreInteractionListener preInteractionListener = Apptentive.preInteractionListener; // capture variable to avoid concurrency issues
		if (preInteractionListener != null) {
			dispatchConversationTask(new ConversationDispatchTask(callback, DispatchQueue.mainQueue()) {
				@Override
				protected boolean execute(Conversation conversation) {
					if (!canShowLocalAppInteraction(conversation, event)) {
						return false;
					}

					boolean allowsInteraction = preInteractionListener.shouldEngageInteraction(event, customData);
					ApptentiveLog.i("Engagement callback allows interaction for event '%s': %b", event, allowsInteraction);
					if (!allowsInteraction) {
						return false;
					}

					return engageLocalAppEvent(context, conversation, event, customData, extendedData); // actually engage event
				}
			}, StringUtils.format("engage '%s' event", event));
			return;
		}

		dispatchConversationTask(new ConversationDispatchTask(callback, DispatchQueue.mainQueue()) {
			@Override
			protected boolean execute(Conversation conversation) {
				return engageLocalAppEvent(context, conversation, event, customData, extendedData);
			}
		}, StringUtils.format("engage '%s' event", event));
	}

	/**
	 * This method can be used to determine if a call to one of the <strong><code>engage()</code></strong> methods such as
	 * {@link #engage(Context, String)} using the same event name will
	 * result in the display of an  Interaction. This is useful if you need to know whether an Interaction will be
	 * displayed before you create a UI Button, etc.
	 *
	 * @param event A unique String representing the line this method is called on. For instance, you may want to have
	 *              the ability to target interactions to run after the user uploads a file in your app. You may then
	 *              call <strong><code>engage(activity, "finished_upload");</code></strong>
	 */
	public static synchronized void queryCanShowInteraction(final String event, BooleanCallback callback) {
		dispatchConversationTask(new ConversationDispatchTask(callback, DispatchQueue.mainQueue()) {
			@Override
			protected boolean execute(Conversation conversation) {
				return canShowLocalAppInteraction(conversation, event);
			}
		}, "check if interaction can be shown");
	}

	/**
	 * Sets an optional engagement callback.
	 */
	public static synchronized void setOnPreInteractionListener(@Nullable OnPreInteractionListener onPreInteractionListener) {
		Apptentive.preInteractionListener = onPreInteractionListener;
	}

	private static boolean engageLocalAppEvent(Context context, Conversation conversation, String event, Map<String, Object> customData, ExtendedData[] extendedData) {
		return EngagementModule.engage(context, conversation, "local", "app", null, event, null, customData, extendedData);
	}

	private static boolean canShowLocalAppInteraction(Conversation conversation, String event) {
		return EngagementModule.canShowInteraction(conversation, "app", event, "local");
	}

	//endregion

	/**
	 * Pass in a listener. The listener will be called whenever a survey is finished.
	 * Do not pass in an anonymous class, such as setOnSurveyFinishedListener(new OnSurveyFinishedListener() {...}).
	 * Instead, create your listener as an instance variable and pass that in. This allows us to keep
	 * a weak reference to avoid memory leaks.
	 *
	 * @param listener The {@link com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener} listener
	 *                 to call when the survey is finished.
	 */
	public static void setOnSurveyFinishedListener(final OnSurveyFinishedListener listener) {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal.getInstance().setOnSurveyFinishedListener(listener);
				return true;
			}
		}, "set survey finish listener");
	}

	//region Login/Logout

	/**
	 * Starts login process asynchronously. This call returns immediately. Using this method requires
	 * you to implement JWT generation on your server. Please read about it in Apptentive's Android
	 * Integration Reference Guide.
	 *
	 * @param token    A JWT signed by your server using the secret from your app's Apptentive settings.
	 * @param callback A LoginCallback, which will be called asynchronously when the login succeeds
	 *                 or fails.
	 */
	public static void login(final String token, final LoginCallback callback) {
		if (StringUtils.isNullOrEmpty(token)) {
			throw new IllegalArgumentException("Token is null or empty");
		}

		dispatchOnConversationQueue(new DispatchTask() {
			@Override
			protected void execute() {
				try {
					loginGuarded(token, callback);
				} catch (Exception e) {
					ApptentiveLog.e(CONVERSATION, e, "Exception while trying to login");
					logException(e);

					notifyFailure(callback, "Exception while trying to login");
				}
			}
		});
	}

	private static void loginGuarded(String token, final LoginCallback callback) {
		checkConversationQueue();

		final ApptentiveInstance sharedInstance = ApptentiveInternal.getInstance();
		if (sharedInstance.isNull()) {
			ApptentiveLog.e(CONVERSATION, "Unable to login: Apptentive instance is not properly initialized");
			notifyFailure(callback, "Apptentive instance is not properly initialized");
		} else {
			sharedInstance.login(token, callback);
		}
	}

	private static void notifyFailure(final LoginCallback callback, final String errorMessage) {
		if (callback != null) {
			DispatchQueue.mainQueue().dispatchAsync(new DispatchTask() {
				@Override
				protected void execute() {
					callback.onLoginFail(errorMessage);
				}
			});
		}
	}

	/**
	 * Callback interface login().
	 */
	public interface LoginCallback {
		/**
		 * Called when a login attempt has completed successfully.
		 */
		void onLoginFinish();

		/**
		 * Called when a login attempt has failed. May be called synchronously, for example, if your JWT
		 * is missing the "sub" claim.
		 *
		 * @param errorMessage failure cause message
		 */
		void onLoginFail(String errorMessage);
	}

	public static void logout() {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal.getInstance().logout();
				return true;
			}
		}, "logout");
	}

	/**
	 * Registers your listener with Apptentive. This listener is stored with a WeakReference, which
	 * means that you must store a static reference to the listener as long as you want it to live.
	 * One possible way to do this is to implement this listener with your Application class, or store
	 * one on your Application.
	 * <p>
	 * This listener will alert you to authentication failures, so that you can either recover from
	 * expired or revoked JWTs, or fix your authentication implementation.
	 *
	 * @param listener A listener that will be called when there is an authentication failure other
	 *                 for the current logged in conversation. If the failure is for another
	 *                 conversation, or there is no active conversation, the listener is not called.
	 */
	public static void setAuthenticationFailedListener(AuthenticationFailedListener listener) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return;
			}
			ApptentiveInternal.getInstance().setAuthenticationFailedListener(listener);
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Error in Apptentive.setUnreadMessagesListener()");
			logException(e);
		}
	}

	public static void clearAuthenticationFailedListener() {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return;
			}
			ApptentiveInternal.getInstance().setAuthenticationFailedListener(null);
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Exception while clearing authentication listener");
			logException(e);
		}
	}

	/**
	 * A Listener you can register globally for the app, that will be called when requests other than
	 * login fail for the active conversation. This includes failure to send queued data to
	 * Apptentive, and failure to fetch app configuration from Apptentive.
	 */
	public interface AuthenticationFailedListener {
		void onAuthenticationFailed(AuthenticationFailedReason reason);
	}

	/**
	 * A list of error codes you will encounter when a JWT failure for logged in conversations occurs.
	 */
	public enum AuthenticationFailedReason {
		/**
		 * This should not happen.
		 */
		UNKNOWN,
		/**
		 * Currently only the HS512 signature algorithm is supported.
		 */
		INVALID_ALGORITHM,
		/**
		 * The JWT structure is constructed improperly (missing a part, etc.)
		 */
		MALFORMED_TOKEN,
		/**
		 * The token is not signed properly, or can't be decoded.
		 */
		INVALID_TOKEN,
		/**
		 * There is no "sub" property in the JWT claims. The "sub" is required, and should be an
		 * immutable, unique id for your user.
		 */
		MISSING_SUB_CLAIM,
		/**
		 * The JWT "sub" claim does not match the one previously registered to the internal Apptentive
		 * conversation. Internal use only.
		 */
		MISMATCHED_SUB_CLAIM,
		/**
		 * Internal use only.
		 */
		INVALID_SUB_CLAIM,
		/**
		 * The expiration "exp" claim is expired. The "exp" claim is a UNIX timestamp in milliseconds.
		 * The JWT will receive this authentication failure when the "exp" time has elapsed.
		 */
		EXPIRED_TOKEN,
		/**
		 * The JWT has been revoked. This happens after a successful logout. In such cases, you will
		 * need a new JWT to login.
		 */
		REVOKED_TOKEN,
		/**
		 * The Apptentive Key field was not specified during registration. You can get this from your app's Apptentive
		 * settings.
		 */
		MISSING_APP_KEY,
		/**
		 * The Apptentive Signature field was not specified during registration. You can get this from your app's Apptentive
		 * settings.
		 */
		MISSING_APP_SIGNATURE,
		/**
		 * The Apptentive Key and Apptentive Signature fields do not match. Make sure you got them from
		 * the same app's Apptentive settings page.
		 */
		INVALID_KEY_SIGNATURE_PAIR;

		private String error;

		public String error() {
			return error;
		}

		public static AuthenticationFailedReason parse(String errorType, String error) {
			try {
				AuthenticationFailedReason ret = AuthenticationFailedReason.valueOf(errorType);
				ret.error = error;
				return ret;
			} catch (Exception e) {
				ApptentiveLog.e(CONVERSATION, "Error parsing unknown Apptentive.AuthenticationFailedReason: %s", errorType);
				logException(e);
			}
			return UNKNOWN;
		}

		@Override
		public String toString() {
			return "AuthenticationFailedReason{" +
					"error='" + error + '\'' +
					"errorType='" + name() + '\'' +
					'}';
		}
	}

	//endregion

	//region Apptimize SDK support

	/**
	 * Invoke this method from your Apptimize.OnExperimentRunListener when an A/B experiment is run.
	 */
	public static void onApptimizeExperimentRun(String experimentName,
												String variantName,
												boolean firstRun) {
		// TODO: update specific experiment only
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal instance = ObjectUtils.as(ApptentiveInternal.getInstance(), ApptentiveInternal.class);
				if (instance != null) {
					instance.tryUpdateApptimizeData();
					return true;
				}

				return false;
			}
		}, "update Apptimize experiment data");
	}

	/**
	 * Invoke this method from your Apptimize.OnExperimentsProcessedListener when A/B experiment configuration is recalculated..
	 */
	public static void onApptimizeExperimentsProcessed() {
		dispatchConversationTask(new ConversationDispatchTask() {
			@Override
			protected boolean execute(Conversation conversation) {
				ApptentiveInternal instance = ObjectUtils.as(ApptentiveInternal.getInstance(), ApptentiveInternal.class);
				if (instance != null) {
					instance.tryUpdateApptimizeData();
					return true;
				}

				return false;
			}
		}, "update Apptimize experiments data");
	}

	//endregion

	//region Error Reporting

	private static void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add more context info
	}

	//endregion

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
	public static class Version implements Serializable, Comparable<Version> {
		private static final long serialVersionUID = 1891878408603512644L;
		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "version";

		private String version;

		public Version() {
		}

		public Version(JSONObject json) throws JSONException {
			this.version = json.optString(TYPE, null);
		}

		public Version(long version) {
			this.version = Long.toString(version);
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public void setVersion(long version) {
			setVersion(Long.toString(version));
		}

		public String getVersion() {
			return version;
		}

		public void toJsonObject() {
			JSONObject ret = new JSONObject();
			try {
				ret.put(KEY_TYPE, TYPE);
				ret.put(TYPE, version);
			} catch (JSONException e) {
				ApptentiveLog.e(e, "Error creating Apptentive.Version.");
				logException(e);
			}
		}

		@Override
		public int compareTo(Version other) {
			String thisVersion = getVersion();
			String thatVersion = other.getVersion();
			String[] thisArray = thisVersion.split("\\.");
			String[] thatArray = thatVersion.split("\\.");

			int maxParts = Math.max(thisArray.length, thatArray.length);
			for (int i = 0; i < maxParts; i++) {
				// If one SemVer has more parts than another, pad out the short one with zeros in each slot.
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
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Version) {
				return compareTo((Version) o) == 0;
			}
			return false;
		}

		@Override
		public String toString() {
			return getVersion();
		}
	}

	public static class DateTime implements Serializable, Comparable<DateTime> {
		private static final long serialVersionUID = -7893194735115350118L;

		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "datetime";
		public static final String SEC = "sec";

		private String sec;

		public DateTime(JSONObject json) throws JSONException {
			this.sec = json.optString(SEC);
		}

		public DateTime(double dateTime) {
			setDateTime(dateTime);
		}

		public void setDateTime(double dateTime) {
			sec = String.valueOf(dateTime);
		}

		public double getDateTime() {
			return Double.valueOf(sec);
		}

		public JSONObject toJSONObject() {
			JSONObject ret = new JSONObject();
			try {
				ret.put(KEY_TYPE, TYPE);
				ret.put(SEC, sec);
			} catch (JSONException e) {
				ApptentiveLog.e(e, "Error creating Apptentive.DateTime.");
				logException(e);
			}
			return ret;
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

	/**
	 * Represents a callback which will be invoked right before an interaction is engaged. Can be used
	 * to intercept the default engagement flow.
	 */
	public interface OnPreInteractionListener {
		/**
		 * @param event event which triggered the interaction.
		 * @param customData optional custom data map passed to the engagement call.
		 * @return <code>true</code> if interaction should be engaged. Otherwise, it would be cancelled.
		 */
		boolean shouldEngageInteraction(String event, @Nullable Map<String, Object> customData);
	}

	/**
	 * Allows certain Apptentive API methods to execute and return a boolean result asynchronously.
	 */
	public interface BooleanCallback {
		/**
		 * Passes the result of an Apptentive API method call.
		 *
		 * @param result true depending on the use of the consuming API method. Check the javadoc for
		 *               the method that uses this callback in its signature.
		 */
		void onFinish(boolean result);
	}

	public interface PendingIntentCallback {
		void onPendingIntent(PendingIntent pendingIntent);
	}
}
