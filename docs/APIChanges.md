This document tracks changes to the API between versions.

# 1.2.0

To improve the quality of the Apptentive SDK, and to make it easier to integrate, the following API method signatures of `Apptentive.java` have been changed or added.

###### Changes
<table>
    <tr><th>Old Method Signature</th><th>New Method Signature</th></tr>
    <tr><td><code>public static void setUserEmail(String email)</code></td><td><code>public static void setInitialUserEmail(Context context, String email)</code></td></tr>
    <tr><td><code>public static void setCustomData(Map<String, String> customData)</code></td><td><code>public static void setCustomDeviceData(Context context, Map<String, String> customDeviceData)</code></td></tr>
</table>

###### Additions
<table>
    <tr><th>New Methods</th></tr>
    <tr><td><code>public static void addCustomDeviceData(Context context, String key, String value)</code></td></tr>
    <tr><td><code>public static void removeCustomDeviceData(Context context, String key)</code></td></tr>
    <tr><td><code>public static void setCustomPersonData(Context context, Map<String, String> customPersonData)</code></td></tr>
    <tr><td><code>public static void addCustomPersonData(Context context, String key, String value)</code></td></tr>
    <tr><td><code>public static void removeCustomPersonData(Context context, String key)</code></td></tr>
    <tr><td><code>public static int getUnreadMessageCount(Context context)</code></td></tr>
</table>

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
