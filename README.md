# Overview
This document will guide you through the installation and configuration of the Apptentive SDK in your Android apps. Laid
out below are instructions that will allow you to ask users to rate your app, give feedback about app performance, and
show surveys to your users.

*Note: The installation instructions are also presented to you on [www.apptentive.com](http://www.apptentive.com) when you add an app. This document is kept in sync with our web documentation.

Integration takes three steps:

1. Get the source
2. Add the source to your project
3. Modify your AndroidManifest.xml and Activities

#Get the Apptentive Source
All of our client code is open source, and [available here on github](https://github.com/apptentive/apptentive-android). We believe in "Your App, Your Code". Our code can be accessed in two ways:

* Download the latest tagged release [here](https://github.com/apptentive/apptentive-android/tags).
* Clone our SDK using git: ``git clone https://github.com/apptentive/apptentive-android.git``

#Add Apptentive to Your Project
##Using Eclipse
These instructions were tested on the Juno release

1. From the menu bar, click `File` -> `Import`. Under `General`, select `Existing Projects into Workspace`. Click `Next`.
2. In the Package Explorer, select your project.
3. From the menu bar, click `Project` -> `Properties`.
4. On the left side, click `Android`. Then, under the `Library` section, click `Add`.
5. Select `apptentive-android-sdk`, and click `OK`.

The Apptentive SDK is now available in your Eclipse project.

##Using IntelliJ IDEA
These instructions were tested on IDEA 12.1

1. From the menu bar, click `File` -> `Import Module`.
2. Select the apptentive-android-sdk directory
3. Click the `Create module from existing sources` radio button, and click `Next` until finished.
4. From the menu bar, click `File` -> `Project Structure...`.
5. Under `Project Settings` click `Modules`, and then select your app's module.
6. Click the `Dependencies` tab, and then click the small `+` button in the lower left corner of that pane.
7. Choose `Module Dependency...`, select `apptentive-android-sdk` module, and click `OK`.
8. Click `OK` to save and close the settings.

The Apptentive SDK is now available in your IntelliJ IDEA project.

#Using Apptentive in your Android App

##1. Modify your AndroidManifest.xml
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

    <strong>&lt;!-- This is not part of the integration, but make sure you are supporting high resolution screens so Apptentive
        UI elements look great! -->
    &lt;supports-screens android:largeScreens="true"
                      android:normalScreens="true"
                      android:smallScreens="true"
                      android:anyDensity="true"/></strong>

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
        &lt;activity android:name="com.apptentive.android.sdk.ViewActivity"/>
        &lt;receiver android:name="com.apptentive.android.sdk.comm.NetworkStateReceiver">
            &lt;intent-filter>
                &lt;action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
                &lt;action android:name="android.intent.action.PACKAGE_RESTARTED"/>
            &lt;/intent-filter>
        &lt;/receiver></strong>

    &lt;/application>
&lt;/manifest>
</code></pre>

##2. Integrate your Activities with Apptentive
In order to keep track of Application state, we need to hook into a few of the Activity lifecycle hooks in your Activities.
There are two ways of doing this: Inheritence, and Delegation. Inheritence is the easiest method, while delegation is
provided if you can't or don't want to inherit from our Activities. Use one of these methods (mix and match is OK too) on
ALL of the Activities you define in your manifest.

### Inheritence

<pre><code><strong>import com.apptentive.android.sdk.ApptentiveActivity;</strong>

public class ExampleActivity <strong>extends ApptentiveActivity</strong> {
</code></pre>

### Delegation

<pre><code><strong>import com.apptentive.android.sdk.ApptentiveActivity;</strong>
    <br/>
    &#8942
    <br/>
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

##3. Call into Apptentive for ratings, feedback, and surveys

### Ratings
Apptentive can ask users to rate your app after a set of conditions are met. Those conditions can be specified in your
Apptentive settings page so you don't have to submit a new version to the app store for changes to take effect. All you
have to do is call the ratings module when you want to show the dialog. Here is an example in your main Activity:

<pre><code>@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
        <strong>Apttentive.showRatingFlowIfConditionsAreMet(this);</strong>
    }
}
</code></pre>

You can change the conditions necessary for the ratings flow to be shown by looging into your www.apptentive.com acount.
Ratings can be shown based on a combination of days since first launch, uses, and significant events. We keep track of
days and uses for you, but you will need to tell us each time the user performs what you deem to be a significant event.

<pre><code><strong>Apttentive.logSignificantEvent(this);</strong></code></pre>

### Message Center
You can add a button that will show the Message Center when pressed. Here is an example button click handler:

<pre><code>Button messageCenterButton = (Button)findViewById(R.id.your_message_center_button);
messageCenterButton.setOnClickListener(new View.OnClickListener(){
    public void onClick(View v) {
        <strong>Apptentive.showMessageCenter(YourActivity.this);</strong>
    }
});
</code></pre>

You can also receive a notification when the number of unread messages waiting to be viewed by the user changes.
Do this in your main Activity's onCreate() method:

<pre><code>Apptentive.setUnreadMessagesListener(new UnreadMessagesListener() {
    public void onUnreadMessageCountChanged(final int unreadMessages) {
        // Use the updated count.
    }
});
</code></pre>

### Survey
Surveys currently require a fetch and then show operation. The easiest way to show a survey is to pass a listener into
the survey fetch method, and then show a survey when the listener gets a callback. You can also pass a listener into the
survey show method, and be notified of successful survey completions.

<pre><code>Apptentive.getSurveyModule().fetchSurvey(new OnSurveyFetchedListener() {
    public void onSurveyFetched(final boolean success) {
        Apptentive.getSurveyModule().show(this, new OnSurveyCompletedListener() {
            public void onSurveyCompletedListener() {
                // Code that runs when the survey was successfully completed.
            }
        });
    }
});
</code></pre>

##4. Extra configuration (Optional)

These steps must all be performed in your main Activity's **onCreate()** method.

#### Support for Amazon Appstore (Optional)

If your app is being built for the Amazon Appstore, you will want to make sure users who want to rate you app are taken
there instead of to Google Play. To do this, simply add the following line in onCreate(). If you omit this line, ratings
will go to Google Play.

<pre><code>Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());
</code></pre>

#### Specifying the user's email address

If you are authorized to access the user's email address, you may specify it during initialization so that in the event
the user does not respond in-app, your message can still get to them through email.

<pre><code>Apptentive.setUserEmail("user_email@example.com");
</code></pre>

#### Send custom data to apptentive.com

This data will show up when you view the profile of the person who sent you feedback. It is especially useful for
specifying any internal user IDs that you use to identify users.

<pre><code>Map&lt;String, String&gt; customData = new HashMap&lt;String, String&gt;();
customData.put("user-id", "1234567890");
Apptentive.setCustomData(customData);
</code></pre>


# Done
That's it. If you have any questions or concerns, please email <strong>sky@apptentive.com</strong>.
<br/>
<br/>
