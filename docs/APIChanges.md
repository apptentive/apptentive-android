This document tracks changes to the API between versions.

# 5.0.0

Read the [Migrating to 5.0.0](migrating_to_5.0.0.md) guide.

| Modified Methods |
| ---------------- |
| public static void buildPendingIntentFromPushNotification(@NonNull final PendingIntentCallback callback, @NonNull final Intent intent) |
| public static void buildPendingIntentFromPushNotification(@NonNull final PendingIntentCallback callback, @NonNull final Bundle bundle) |
| public static void buildPendingIntentFromPushNotification(final PendingIntentCallback callback, @NonNull final Map<String, String> data) |
| public static void canShowMessageCenter(BooleanCallback callback) |

| Added Methods |
| ------------- |
| public static void showMessageCenter(Context context, BooleanCallback callback) |
| public static void showMessageCenter(final Context context, final BooleanCallback callback, final Map<String, Object> customData) |
| public static synchronized void engage(Context context, String event, BooleanCallback callback) |
| public static synchronized void engage(Context context, String event, BooleanCallback callback, Map<String, Object> customData) |
| public static synchronized void engage(final Context context, final String event, final BooleanCallback callback, final Map<String, Object> customData, final ExtendedData... extendedData) |
| public static synchronized void queryCanShowInteraction(final String event, BooleanCallback callback) |

| Removed Methods |
| --------------- |
| public static synchronized void canShowInteraction(final String event) |

# 4.0.0

Read the [Migrating to 4.0.0](migrating_to_4.0.0.md) guide.

| Added Methods |
| ------------- |
| public static void login(String token, LoginCallback callback) |
| public static void logout() |

| Modified Methods |
| ---------------- |
| public static void register(Application application, String apptentiveKey, String apptentiveSignature) |

| Added Classes |
| ------------- |
| public interface Apptentive.LoginCallback |
| public interface Apptentive.AuthenticationFailedListener |
| public enum Apptentive.AuthenticationFailedReason |

# 3.4.1

| Added Methods|
| ------------------ |
| public static void dismissAllInteractions() |

# 3.3.0

Read the [Migrating to 3.3.0](migrating_to_3.3.0.md) guide.

| Deprecated Methods |
| ------------------ |
| public static boolean setPendingPushNotification(Intent intent) |
| public static boolean setPendingPushNotification(Bundle data) |
| public static boolean handleOpenedPushNotification(Context context) |

| Added Methods|
| ------------------ |
| public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Intent intent) |
| public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Bundle bundle) |
| public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Map<String, String> data) |
| public static String getTitleFromApptentivePush(Intent intent) |
| public static String getTitleFromApptentivePush(Bundle bundle) |
| public static String getTitleFromApptentivePush(Map<String, String> data) |
| public static String getBodyFromApptentivePush(Intent intent) |
| public static String getBodyFromApptentivePush(Bundle bundle) |
| public static String getBodyFromApptentivePush(Map<String, String> data) |

# 3.0.0

| Removed Methods |
| ------------------ |
| public static void onStart(android.app.Activity activity) |
| public static void onStop(android.app.Activity activity) |

| Removed Classes |
| ------------------ |
| ApptentiveActivity.java |
| ApptentiveListActivity.java |
| ViewActivity.java |

| Added Classes |
| ------------------ |
| ApptentiveViewActivity.java |

The following methods were modified to no longer take a `Context`.

| Modified Methods |
| ---------------- |
| [public static void addCustomDeviceData(String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.String) |
| [public static void addCustomDeviceData(String key, Number value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.Number) |
| [public static void addCustomDeviceData(String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.String) |
| [public static void addCustomDeviceData(String key, Version value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.Version) |
| [public static void addCustomDeviceData(String key, DateTime value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.DateTime) |
| [public static void addCustomPersonData(String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-java.lang.String-java.lang.String) |
| [public static void addCustomPersonData(String key, Number value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-java.lang.String-java.lang.Number) |
| [public static void addCustomPersonData(String key, Boolean value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-java.lang.String-java.lang.Boolean) |
| [public static void addCustomPersionData(String key, Version value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.Version) |
| [public static void addCustomPersionData(String key, DateTime value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-java.lang.String-java.lang.DateTime) |
| [public static void canShowMessageCenter()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#canShowMessageCenter) |
| [public static void canShowInteraction(String interaction)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#canShowInteraction-java.lang.String) |
| [public static void getPersonEmail()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getPersonEmail) |
| [public static void getPersonName()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getPersonName) |
| [public static void getUnreadMessageCount()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getUnreadMessageCount) |
| [public static boolean handleOpenedPushNotification(Context context)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#handleOpenedPushNotification) |
| [public static void removeCustomDeviceData(String key)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#removeCustomDeviceData-java.lang.String) |
| [public static void removeCustomPersonData(String key)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#removeCustomPersonData-java.lang.String) |
| [public static void sendAttachmentFile(String uri)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile-java.lang.String-) |
| [public static void sendAttachmentFile(byte\[\] content, String mimeType)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile-byte:A-java.lang.String-) |
| [public static void sendAttachmentFile(InputStream is, String mimeType)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile-java.io.InputStream-java.lang.String-) |
| [public static void sendAttachmentText(String text)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentText-java.lang.String-) |
| [public static boolean setPendingPushNotification(Bundle bundle)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#setPendingPushNotification-android.os.Bundle-) |
| [public static boolean setPendingPushNotification(Intent intent)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#setPendingPushNotification-android.content.Intent-) |
| [public static void setPersonEmail(String email)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPersonEmail-java.lang.String) |
| [public static void setPersonName(String name)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPersonName-java.lang.String) |
| [public static void setPushNotificationIntegration(int pushProvider, String token)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPushNotificationIntegration-int-java.lang.String) |
| [public static void showMessageCenter()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter-android.content.Context) |
| [public static void showMessageCenter(Map<String, Object>)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter-java.util.Map) |

The following methods were modified to take a `Context` instead of an `Activity`.

| Modified Methods |
| ---------------- |
| [public static synchronized boolean engage(Context context, String event)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#engage-android.content.Context-java.lang.String-) |
| [public static synchronized boolean engage(Context context, String event, Map<String, Object> customData)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#engage-android.content.Context-java.lang.String-java.util.Map-) |
| [public static synchronized boolean engage(Context context, String event, Map<String, Object> customData, ExtendedData... extendedData)](https://docs.apptentive.com/android/api/com/apptentive/android/sdk/Apptentive.html#engage-android.content.Context-java.lang.String-java.util.Map-com.apptentive.android.sdk.model.ExtendedData...-) |

# 2.1.0

* Added a methods for setting custom data of type String, Number, and Boolean on the Device and Person objects.
* Deprecated `setCustomDeviceData()` and `setCustomDeviceData()`.
* Modified `showMessageCenter()` to allow custom data of type Number and Boolean, in addition to the existing String.

| Added Methods |
| ------------- |
| [public static void addCustomDeviceData(Context context, String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-android.content.Context-java.lang.String-java.lang.String) |
| [public static void addCustomDeviceData(Context context, String key, Number value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-android.content.Context-java.lang.String-java.lang.Number) |
| [public static void addCustomDeviceData(Context context, String key, Boolean value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData-android.content.Context-java.lang.String-java.lang.Boolean) |
| [public static void addCustomPersonData(Context context, String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-android.content.Context-java.lang.String-java.lang.String) |
| [public static void addCustomPersonData(Context context, String key, Number value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-android.content.Context-java.lang.String-java.lang.Number) |
| [public static void addCustomPersonData(Context context, String key, Boolean value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData-android.content.Context-java.lang.String-java.lang.Boolean) |

| Modified Methods |
| ------------------ |
| [public static void showMessageCenter(Context context, Map<String, Object>)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter-android.content.Context-java.util.Map) |

| Deprecated Methods |
| ------------------ |
| [public static void setCustomDeviceData(Context context, Map<String, String>)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setCustomDeviceData-android.content.Context-java.util.Map) |
| [public static void setCustomPersonData(Context context, Map<String, String>)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setCustomPersonData-android.content.Context-java.util.Map) |

# 2.0.0

* Modified `showMessageCenter()` to return a boolean. Returns true if Message Center was shown, else false.
* Added a method `canShowMessageCenter()` for checking to see if Message Center is available to be shown. The Apptentive SDK needs to connect to our server at least once after upgrading to 2.0.0 in order for Message Center to be able to display.
* Deprecated `setUnreadMessagesListener()` and added `addUnreadMessagesListener()` in its place.
* Removed existing push provider token setup methods and replaced them with a unified method for setting the device token and specifying the provider.
* Replaced `setInitialUserName()` and `setInitialUserEmail()` with `setPersonName()` and `setPersonEmail()`. The new methods will override what you previously set. Because we now allow your customer to supply their name and email through Message Center, you may want to check to see if these fields are set by calling `getPersonName()` and `getPersonEmail()` before you overwrite them.

| Removed Methods |
| --------------- |
| `public static void setInitialUserName(Context context, String name)` |
| `public static void setInitialUserEmail(Context context, String email)` |
| `public static void addAmazonSnsPushIntegration(Context context, String registrationId)` |
| `public static void addParsePushIntegration(Context context, String deviceToken)` |
| `public static void addUrbanAirshipPushIntegration(Context context, String channelId)` |

| Added Methods |
| ------------- |
| [public static void getPersonName(Context context)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getPersonName-android.content.Context) |
| [public static void setPersonName(Context context, String name)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPersonName-android.content.Context-java.lang.String) |
| [public static void getPersonEmail(Context context)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getPersonEmail-android.content.Context) |
| [public static void setPersonEmail(Context context, String email)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPersonEmail-android.content.Context-java.lang.String) |
| [public static void canShowMessageCenter(Context context)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#canShowMessageCenter-android.content.Context) |
| [public static void addUnreadMessagesListener(UnreadMessagesListener listener)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addUnreadMessagesListener-com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener) |
| [public static void setPushNotificationIntegration(Context context, int pushProvider, String token)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPushNotificationIntegration-android.content.Context-int-java.lang.String) |

| Modified Methods |
| ---------------- |
| [public static boolean showMessageCenter(Activity activity)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter-android.app.Activity) |

| Deprecated Methods |
| ------------------ |
| [public static void setUnreadMessagesListener(UnreadMessagesListener listener)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setUnreadMessagesListener-com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener) |


# 1.7.2

* Removed a method to set the callback Activity that Apptentive will return to after handling a push notification. This
 method is no longer needed because the Parse SDK has changed, and therefore the Apptentive integration changed. Please
 see this [Migration Guide](https://github.com/apptentive/apptentive-android-private/blob/push_migration/docs/migrating_to_1.7.2_with_push_notifications.md).

| Removed Methods |
| --------------- |
| public static void setParsePushCallback(Class<? extends Activity> activity) |

* Added new methods to work with Urban Airship's new integration style. Again, please see the [Migration Guide](https://github.com/apptentive/apptentive-android-private/blob/push_migration/docs/migrating_to_1.7.2_with_push_notifications.md).

| Added Methods |
| ------------- |
| [public static boolean setPendingPushNotification(Context context, Bundle bundle)]() |
| [public static boolean isApptentivePushNotification(Bundle bundle)]() |

# 1.6.2

* Added API method [Apptentive.willShowInteraction()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#willShowInteraction-android.content.Context-java.lang.String-).
    If a call to this method returns true, then a call to engage() with the same Event name will result in an Interaction being displayed.

| Added Methods |
| ------------- |
| [public static synchronized boolean willShowInteraction(Activity activity, String event)]() |


# 1.6.0

* Added ability to send custom data and EventData with Events.
* Added support for Parse push notifications.
* Added ability to set initial user name.
* The repo has been refactored to use a more Gradle friendly format.

[Migrating to 1.6.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.6.0.md)

| Added Methods |
| ------------- |
| [public static synchronized boolean engage(Activity activity, String event, Map<String, Object> customData)]() |
| [public static synchronized boolean engage(Activity activity, String event, Map<String, Object> customData, ExtendedData... extendedData)]() |
| [public static void addParsePushIntegration(Context context, String deviceToken)]() |
| [public static void setParsePushCallback(Class<? extends Activity> activity)]() |
| [public static void setInitialUserName(Context context, String name)]() |


# 1.5.0

Refactored [Surveys](http://www.apptentive.com/docs/android/features/#surveys) to use the new Event framework. You should use [Apptentive.engage(Activity activity, String eventName)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29) instead of `Apptentive.showSurvey()`.

[New Survey integration instructions.](http://www.apptentive.com/docs/android/integration/#surveys)

[Migrating to 1.5.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.5.0.md)

| Removed Methods |
| --------------- |
| `public static boolean showSurvey(Activity activity, OnSurveyFinishedListener listener, String... tags)` |

| Added Methods |
| ------------- |
| [public static boolean Apptentive.engage(Activity activity, String eventName)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29) |
| [public static void setOnSurveyFinishedListener(OnSurveyFinishedListener listener)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setOnSurveyFinishedListener%28com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener%29) |

# 1.4.2

Added support for AWS SNS push notifications

| Added Methods |
| --------------- |
|`public static void addAmazonSnsPushIntegration(Context context, String registrationId)`|


# 1.4.0

The Rating Dialog has been rebuilt to use the new unified `engage()` method. See the **Interactions** section of the README for more information.

| Removed Methods |
| --------------- |
|`public static void Apptentive.logSignificantEvent(Context context)`|
|`public static boolean showRatingFlowIfConditionsAreMet(Activity activity)`|

| Added Methods |
| --------------- |
|`public static boolean Apptentive.engage(Activity activity, String eventName)`|


# 1.2.7

###### Changes
Please remove the line `<action android:name="android.intent.action.PACKAGE_RESTARTED"/>` action from Apptentive's NetworkStateReceiver in your manifest. This is not needed, and may cause excessive battery drain.


# 1.2.5
| New Methods |
| ----------- |
| `public static void Apptentive.addUrbanAirshipPushIntegration(Context context, String apid)` |
| `public static void setPendingPushNotification(Context context, Intent intent)` |
| `public boolean handleOpenedPushNotification(Activity activity)` |
| `public static void setPendingPushNotification(Context context, Intent intent)` |


# 1.2.3
| New Methods |
| ----------- |
| `public static void addIntegration(Context context, String integration, Map<String, String> config)` |
| `public static void addUrbanAirshipPushIntegration(Context context, String apid)` |
| `public static void handleOpenedPushNotification(Activity activity, Intent intent)` |
| `public static boolean isApptentivePushNotification(Intent intent)` |

###### Changes
| Old Method Signature | New Method Signature |
| -------------------- | -------------------- |
| `public static void showRatingFlowIfConditionsAreMet(Activity activity)` | `public static boolean showRatingFlowIfConditionsAreMet(Activity activity)`


# 1.2.1
###### Additions
| New Methods |
| ----------- |
| `public static void showMessageCenter(Activity activity, Map<String, String> customData)` |


# 1.2.0
To improve the quality of the Apptentive SDK, and to make it easier to integrate, the following API method signatures of `Apptentive.java` have been changed or added.

###### Changes
| Old Method Signature | New Method Signature |
| -------------------- | -------------------- |
| `public static void setUserEmail(String email)` | `public static void setInitialUserEmail(Context context, String email)`
| `public static void setCustomData(Map<String, String> customData)` | `public static void setCustomDeviceData(Context context, Map<String, String> customDeviceData)` |

###### Additions
| New Methods |
| ----------- |
| `public static void addCustomDeviceData(Context context, String key, String value)` |
| `public static void removeCustomDeviceData(Context context, String key)` |
| `public static void setCustomPersonData(Context context, Map<String, String> customPersonData)` |
| `public static void addCustomPersonData(Context context, String key, String value)` |
| `public static void removeCustomPersonData(Context context, String key)` |
| `public static int getUnreadMessageCount(Context context)` |


# 1.0
The following changes from the 0.6.x series were made.

We are moving over to a unified message center, which is an expansion of the feedback API. We have decided to take the opportunity to clean up the ratings flow API, and simplify how you interact with the SDK in general. Below are detailed changes that have been made to the API, but from a simple perspective, you'll want to:

General setup:

* Replace

`Apptentive.getFeedbackModule().addDataField("username", "Sky Kelsey");`

with

<pre><code>Map<String, String> customData = new HashMap<String, String>();
customData.put("username", "Sky Kelsey");
Apptentive.setCustomData(customData);
</code></pre>

* Replace `Apptentive.getRatingModule().setRatingProvider(new AmazonAppstoreRatingProvider());` with `Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());`

To launch feedback:

* Replace `Apptentive.getFeedbackModule().forceShowFeedbackDialog(YourActivity.this);` with `Apptentive.showMessageCenter(YourActivity.this);`.

In ratings:

* Replace `Apptentive.getRatingModule().run(YourActivity.this);` with `Apptentive.showRatingFlowIfConditionsAreMet(YourActivity.this);`
* Replace `Apptentive.getRatingModule().logEvent();` with `Apptentive.logSignificantEvent();`.
