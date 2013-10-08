# Apptentive Android SDK

The Apptentive Android SDK lets you provide a powerful and simple channel to communicate with your customers. With it, you can manage your app's ratings, let your customers give you feedback, respond to customer feedback, show surveys at specific points within your app, and more.

Note: For developers with apps created before June 28, 2013, please contact us to have your account upgraded to the new Message Center UI on our website.

-------------------------------------------------

The Apptentive SDK works on devices with **Android 2.1 (API Level 7)** and newer. You must also build your app against Android SDK 3.1 (API 12) or newer. This will not cause your app to stop working on pre 3.1 devices, but allows us to use newer XML syntax for forward compatibility.

The following languages are supported:
<table>
  <tr>
    <th>Locale Qualifier</th>
    <th>Language Name</th>
  </tr>
  <tr>
    <td>en</td>
    <td>English</td>
  </tr>
  <tr>
    <td>da</td>
    <td>Danish</td>
  </tr>
  <tr>
    <td>de</td>
    <td>German</td>
  </tr>
  <tr>
    <td>es</td>
    <td>Spanish</td>
  </tr>
  <tr>
    <td>fr</td>
    <td>French</td>
  </tr>
  <tr>
    <td>it</td>
    <td>Italian</td>
  </tr>
  <tr>
    <td>ja</td>
    <td>Japanese</td>
  </tr>
  <tr>
    <td>nl</td>
    <td>Dutch</td>
  </tr>
  <tr>
    <td>ru</td>
    <td>Russian</td>
  </tr>
  <tr>
    <td>sv</td>
    <td>Swedish</td>
  </tr>
  <tr>
    <td>zh</td>
    <td>Chinese (Traditional)</td>
  </tr>
  <tr>
    <td>zh-rCN</td>
    <td>Chinese (Simplified)</td>
  </tr>
</table>

## Install Guide

This walk-through will guide you through the installation and configuration of the Apptentive SDK in your Android apps. Laid
out below are instructions that will allow you to ask users to rate your app, give and receive feedback about app performance, and
show surveys to your users.

Note: The installation instructions are also presented to you on [Apptentive](http://www.apptentive.com) when you add an app. This document is kept in sync with our web documentation.

-

### Get Apptentive 

All of our client code is open source and available on [GitHub](https://github.com/apptentive/apptentive-android). We believe in "Your App, Your Code". 

Our code can be accessed in two ways.

* Download the latest release [here](https://github.com/apptentive/apptentive-android/tags).
* Alternatively, you can clone our Android SDK using git: `git clone https://github.com/apptentive/apptentive-android.git`

-

### Setup Android Workspace

#### Using Eclipse

These instructions were tested for the Juno Eclipse release.

1. From the menu bar, click `File` -> `Import`
2. Under `General`, select `Existing Projects into Workspace`
3. Click `Next`
4. In the Package Explorer, select your project
5. From the menu bar, click `Project` -> `Properties`
6. On the left side, click `Android`
7. Under the `Library` section, click `Add`
8. Select `apptentive-android-sdk`
9. Click `OK`

The your Android app now references the Apptentive Android SDK.

#### Using IntelliJ IDEA

These instructions were tested for IntelliJ IDEA 12.1.6

1. From the menu bar, click `File` -> `Import Module`
2. Select the `apptentive-android-sdk` directory
3. Click the `Create module from existing sources` radio button
4. Click `Next` until finished
5. From the menu bar, click `File` -> `Project Structure...`
6. Under `Project Settings` click `Modules`
7. Select your Android app's module
7. Click the `Dependencies` tab, and then click the small `+` button in the lower left corner of that pane
8. Choose `Module Dependency...`, select `apptentive-android-sdk` module, and click `OK`
9. Click `OK` to save and close the settings

The your Android app now references the Apptentive Android SDK.

-

### Implement Apptentive

#### Modify your AndroidManifest.xml

You will need to copy in the bold text below into your AndroidManifest.xml. Comments note the required and optional changes.

<pre><code>&lt;?xml version="1.0" encoding="utf-8"?>
&lt;manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.apptentive.android.example"
          android:versionCode="1"
          android:versionName="1.0">

    <strong>&lt;!-- All permissions required except GET_ACCOUNTS, which is only need to pull user email -->
    &lt;uses-permission android:name="android.permission.INTERNET"/>
    &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    &lt;uses-permission android:name="android.permission.GET_ACCOUNTS"/></strong>

    <strong>&lt;!-- This is not part of the integration, but make sure you are supporting high resolution screens so Apptentive UI elements look great! --></strong>
    &lt;supports-screens android:largeScreens="true"
                      android:normalScreens="true"
                      android:smallScreens="true"
                      android:anyDensity="true"/></strong>

    <strong>&lt;!-- minSDKVersion must be at least 7 --></strong>
    &lt;uses-sdk android:minSdkVersion="7"
              android:maxSdkVersion="18"
              android:targetSdkVersion="18"/>

    &lt;application android:label="@string/app_name" android:icon="@drawable/icon">
        &lt;activity android:name=".ExampleActivity"
                  android:label="@string/app_name"
                  android:configChanges="orientation|keyboardHidden"
                  android:launchMode="singleTop">
            &lt;intent-filter>
                &lt;action android:name="android.intent.action.MAIN"/>
                &lt;category android:name="android.intent.category.LAUNCHER"/>
            &lt;/intent-filter>
        &lt;/activity>

        <strong>&lt;!-- Include your App's Apptentive API key. This is available in your app's "settings" page on www.apptentive.com -->
        &lt;meta-data android:name="apptentive_api_key" android:value="YOUR_API_KEY_GOES_HERE"/></strong>

        <strong>&lt;!-- Copy in this code. It sets up the single Activity we use to launch our views, and allows us to be
            notified when the internet connection comes up, so we can handle sending and receiving message reliably -->
        &lt;activity android:name="com.apptentive.android.sdk.ViewActivity"
                  android:theme="@style/Apptentive.Theme.Transparent"/>

        &lt;receiver android:name="com.apptentive.android.sdk.comm.NetworkStateReceiver">
            &lt;intent-filter>
                &lt;action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
                &lt;action android:name="android.intent.action.PACKAGE_RESTARTED"/>
            &lt;/intent-filter>
        &lt;/receiver></strong>

    &lt;/application>
&lt;/manifest>
</code></pre>

Note: Be sure to input your Apptentive API Key where it says "YOUR_API_KEY_GOES_HERE".

#### Integrate your Activities with Apptentive

In order to keep track of Application state, we need to hook into a few of the Activity lifecycle hooks in your Activities.
There are two ways of doing this: Inheritence, and Delegation. Inheritence is the easiest method, while delegation is
provided if you can't or don't want to inherit from our Activities. 

Add one of the following code snippets to ALL of the Activities you define in your manifest (mix and match is OK too).

##### Inheritence

<pre><code><strong>import com.apptentive.android.sdk.ApptentiveActivity;</strong>

public class ExampleActivity <strong>extends ApptentiveActivity</strong> {
</code></pre>

##### Delegation

<pre><code><strong>import com.apptentive.android.sdk.ApptentiveActivity;</strong>

    &#8942

    @Override
    protected void onStart() {
        super.onStart();
        <strong>Apptentive.onStart(this);</strong>
    }

    @Override
    protected void onStop() {
        super.onStop();
        <strong>Apptentive.onStop(this);</strong>
    }
</code></pre>

#### Message Center

You can add a button that will show the Apptentive feedback UI when pressed. Here is an example button click handler:

<pre><code>Button messageCenterButton = (Button)findViewById(R.id.your_message_center_button);
messageCenterButton.setOnClickListener(new View.OnClickListener(){
    public void onClick(View v) {
        <strong>Apptentive.showMessageCenter(YourActivity.this);</strong>
    }
});</code></pre>

You can also receive a notification when the number of unread messages waiting to be viewed by the user changes.
Do this in your main Activity's onCreate() method:

<pre><code>Apptentive.setUnreadMessagesListener(new UnreadMessagesListener() {
    public void onUnreadMessageCountChanged(final int unreadMessages) {
        // Use the updated count.
    }
});</code></pre>

#### Ratings

Apptentive can ask users to rate your app after a set of conditions are met. Those conditions can be specified in your
Apptentive settings page so you don't have to submit a new version to the app store for changes to take effect. All you
have to do is call the ratings module when you want to show the dialog. Here is an example in your main Activity:

<pre><code>@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
        <strong>Apptentive.showRatingFlowIfConditionsAreMet(this);</strong>
    }
}</code></pre>

You can change the conditions necessary for the ratings flow to be shown by logging into your [Apptentive](http:s//apptentive.com) account.
Ratings can be shown based on a combination of days since first launch, uses, and significant events. We keep track of
days and uses for you, but you will need to tell us each time the user performs what you deem to be a significant event.

<pre><code><strong>Apttentive.logSignificantEvent(this);</strong></code></pre>

#### Surveys

Surveys are fetched from the server when the app starts, so you don't have to worry about managing them. To show a
survey, simply call `Apptentive.showSurvey()`. You can optionally passing in a set of tags to match against. Tags are defined
when you create a survey on [Apptentive](http://apptentive.com). You can also pass in a listener and be notified when the user submits
or skips a survey.

##### Method

```
showSurvey(Activity activity, OnSurveyFinishedListener listener, String... tags);
```

##### Example

```
Apptentive.showSurvey(this,
  new OnSurveyCompletedListener() {
    public void onSurveyCompletedListener() {
      // Code that runs when the survey was successfully completed.
    }
  });
});
```

To first check to see if a survey can be shown, call `Apptentive.isSurveyAvailable()`.

#### Extra Configuration (Optional)

These steps must all be performed in your main Activity's `onCreate()` method.

##### Support for Amazon Appstore

If your app is being built for the Amazon Appstore, you will want to make sure users who want to rate you app are taken
there instead of to Google Play. To do this, simply add the following line in onCreate(). 

<pre><code>Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());</code></pre>

If you omit this line, ratings will go to Google Play.

##### Specifying the User's Email Address

If you are authorized to access the user's email address, you may specify it during initialization so that in the event
the user does not respond in-app, your message can still get to them through email.

<pre><code>Apptentive.setUserEmail("user_email@example.com");</code></pre>

##### Send Custom Data to Apptentive

<pre><code>Apptentive.addCustomData(Context context, String key, String value);
</code></pre>

<pre><code>Apptentive.removeCustomData(Context context, String key);
</code></pre>

<pre><code>Apptentive.setCustomData(Context context, Map&lt;String, String&gt; customData);
</code></pre>