This document tracks changes to the API between versions.


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
