# Apptentive Android SDK

The Apptentive Android SDK lets you provide a powerful and simple channel to communicate with your customers. With it,
you can manage your app's ratings, let your customers give you feedback, respond to customer feedback, show surveys at
specific points within your app, and more.

**Note:** API changes between versions are tracked [here](docs/APIChanges.md).

-------------------------------------------------

The Apptentive SDK works on devices with **Android 2.1 (API Level 7)** and newer. You must also build your app against
Android SDK 3.1 (API 12) or newer. This will not cause your app to stop working on pre 3.1 devices, but allows us to use
newer XML syntax for forward compatibility.

The following languages are supported:

| Locale Qualifier | Language Name |
| ---------------- | ------------- |
| `en`             |  English      |
| `da`             |  Danish       |
| `de`             |  German       |
| `es`             |  Spanish      |
| `fr`             |  French       |
| `it`             |  Italian      |
| `ja`             |  Japanese     |
| `nl`             |  Dutch        |
| `ru`             |  Russian      |
| `sv`             |  Swedish      |
| `zh`             |  Chinese (Traditional) |
| `zh-rCN`         |  Chinese (Simplified)  |

# Install Guide

Laid out below are instructions that will allow you to ask customers to rate your app, give and receive feedback about app performance, and
show surveys to your customers.

Note: These installation instructions are also presented to you on [Apptentive](https://www.apptentive.com) when you add an app. This document is kept in sync with our web documentation.

---

## Get Apptentive

All of our client code is open source and available on [GitHub](https://github.com/apptentive/apptentive-android). We believe in "Your App, Your Code". 

Our code can be accessed in two ways.

* Download the latest release [here](https://github.com/apptentive/apptentive-android/tags).
* Alternatively, you can clone our Android SDK using git: `git clone https://github.com/apptentive/apptentive-android.git`

**Note:** Please make sure to watch and star our github repo so you can be notified when there are updates to the SDK.

---

## Set up Android Workspace

### Using IntelliJ IDEA

These instructions were tested for IntelliJ IDEA 13.1.1

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

### Using Eclipse

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

---

## Implement Apptentive

### Modify your AndroidManifest.xml

You will need to copy in the bold text below into your AndroidManifest.xml. Comments note the required and optional changes.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.apptentive.android.example"
          android:versionCode="1"
          android:versionName="1.0">
    <!-- Required permissions. -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <!-- Optional permissions. GET_ACCOUNTS is used to pre-populate customer's email in form fields. -->
    <uses-permission android:name="android.permission.GET_ACCOUNTS"/>

    <!-- Make sure you are supporting high resolution screens so Apptentive UI elements look great! -->
    <supports-screens android:largeScreens="true"
                      android:normalScreens="true"
                      android:smallScreens="true"
                      android:anyDensity="true"/>

    <!-- minSDKVersion must be at least 7 -->
    <uses-sdk android:minSdkVersion="7"
              android:targetSdkVersion="19"/>

    <application android:label="@string/app_name" android:icon="@drawable/icon">
        <activity android:name=".ExampleActivity"
                  android:label="@string/app_name"
                  android:configChanges="orientation|keyboardHidden"
                  android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <!-- Include your App's Apptentive API key from your app's "settings" page on www.apptentive.com -->
        <meta-data android:name="apptentive_api_key" android:value="YOUR_API_KEY_GOES_HERE"/>

        <!-- Add a reference to Apptentive's ViewActivity and NetworkStateReceiver -->
        <activity android:name="com.apptentive.android.sdk.ViewActivity"
                  android:theme="@style/Apptentive.Theme.Transparent"/>

        <receiver android:name="com.apptentive.android.sdk.comm.NetworkStateReceiver">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

Note: Be sure to input your Apptentive API Key where it says `YOUR_API_KEY_GOES_HERE`.

### Integrate your Activities with Apptentive

In order to keep track of Application state, we need to hook into a few of the Activity lifecycle hooks in your Activities.
There are two ways of doing this: Inheritance, and Delegation. Inheritance is the easiest method, while delegation is
provided if you can't or don't want to inherit from our Activities. 

Add one of the following code snippets to ALL of the Activities you define in your manifest (mix and match is OK too).

###### Inheritance

```java
import com.apptentive.android.sdk.ApptentiveActivity;

public class ExampleActivity extends ApptentiveActivity {
```

###### Delegation

```java
import com.apptentive.android.sdk.ApptentiveActivity;

    ...

    @Override
    protected void onStart() {
        super.onStart();
        Apptentive.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Apptentive.onStop(this);
    }
```

### Message Center

The Apptentive Message Center lets customers send message you about problems they are having, and lets you respond. The
customer stays in the app, and you are able to provide high quality support to make your customers feel loved.

If the Message Center is being opened for the first time, the Intro Dialog will be shown instead. When the customer
submits the Intro Dialog, they are taken to a Thank You Dialog, where they have a chance to open the Message Center.

![Intro Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/intro_dialog_default_blank.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Intro Dialog Completed](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/intro_dialog_default_filled.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Thank You Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/thank_you_dialog.png)

The Message Center displays all messages sent between the customer and you, as well as the times they were sent, and who
they were sent by. Your replies will show up in the Message Center, and the customer will not have to leave your app to
see them. Customers with devices running Android 4+ will also be able to send screenshots.

![Message Center Sent](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/message_center_default_sent.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Message Center Screenshot Sent](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/message_center_default_screenshot_sent.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Message Center Reply Received](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/message_center_default_reply_received.png)

Here is what the other side of the conversation looks like.
![Website Conversation View](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/website_conversation_default_reply.png)

#### Showing Message Center
You can add a button that will show the Apptentive feedback UI when pressed. Here is an example button click handler:

###### Method

```java
public static void Apptentive.showMessageCenter(Activity activity);
```

###### Example

```java
Button messageCenterButton = (Button)findViewById(R.id.your_message_center_button);
messageCenterButton.setOnClickListener(new View.OnClickListener(){
    public void onClick(View v) {
        Apptentive.showMessageCenter(YourActivity.this);
    }
});
```

#### Showing Message Center and Passing in Custom Message Data

Additionally, you can supply custom key/value pairs that will be sent in the next message that the customer sends while
the Message Center is open. For instance, if you have a dining app, you could pass in a key of `restaurant` and value of
`Joe's Pizza`. If the customer sends a more than one message, only the first message will include this custom data. If
you wish to add more custom data to another subsequent message, you will need to call this method with custom data again.

###### Method

```java
public static void Apptentive.showMessageCenter(Activity activity, Map<String, String> customData);
```

###### Example

```java
    Map<String, String> customData = new HashMap<String, String>();
    customData.put("restaurant", "Joe's Pizza");
    Apptentive.showMessageCenter(YourActivity.this, customData);
```

#### Be Notified of New Messages

You can also receive a notification when the number of unread messages waiting to be viewed by the customer changes.
Do this in your main Activity's `onCreate()` method:

###### Method

```java
public static void Apptentive.setUnreadMessagesListener(UnreadMessageListener listener);
```

###### Example

```java
Apptentive.setUnreadMessagesListener(
    new UnreadMessagesListener() {
        public void onUnreadMessageCountChanged(final int unreadMessages) {
            // Use the updated count.
        }
    }
);
```

#### Sending Attachments

You can send attachments to the server. We provide methods for sending text and file messages that are hidden
from the end customer, but visible to you in the conversation view on the server. There are three ways to send a
FileMessage, so that you can easily send files: Using a local URI, an InputStream, or a byte array. Messages sent in
this fashion are diplayed as hidden in the conversation view on the server, so you can differentiate them from messages
sent by the customer.

###### Methods
```java
public static void sendAttachmentText(Context context, String text);
```
```java
public static void sendAttachmentFile(Context context, String uri);
```
```java
public static void sendAttachmentFile(Context context, byte[] content, String mimeType);
```
```java
public static void sendAttachmentFile(Context context, InputStream is, String mimeType);
```


### Surveys

Surveys are a great tool to learn about your customers. You can create surveys that have multiple types of questions:
multiple select, single select, and free text. You can also choose the audience your survey is shown to by choosing to
send it only to devices which match certain criteria. Finally, you can chose to limit how often and how many times a
survey can be shown to each person, or to the entire customer base.

![Survey Incomplete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_incomplete.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Survey Complete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_complete.png)

Surveys are fetched from the server when the app starts, so you don't have to worry about managing them. They are cached
for 24 hours. To show a survey, simply call `Apptentive.showSurvey()`. You can optionally pass in a set of tags to match
against. Tags are arbitrary strings that represent the place in your code that you are calling the survey API. You can
call the survey from multiple places in your code. Then, later on, you can create a survey using one of those tags, and
that survey will only be shown at that tagged API call. You can also pass in a listener and be notified when the
customer submits or skips a survey.

**Note:** If you are unable to show surveys during testing, you may have installed your app before the survey was put live on our server. Just clear or reinstall your app to see surveys.

###### Method

```java
public static boolean Apptentive.showSurvey(Activity activity, OnSurveyFinishedListener listener, String... tags);
```

###### Example

```java
Apptentive.showSurvey(
    this,
    new OnSurveyFinishedListener() {
        public void onSurveyFinishedListener(boolean completed) {
            if (completed) {
                // Code that runs when the survey was successfully completed.
            }
        }
    },
    "completed_level_ten"
);
```

To first check to see if a survey can be shown, call `Apptentive.isSurveyAvailable()`.

###### Method

```java
public static boolean Apptentive.isSurveyAvailable(Context context, String... tags);
```


### Interactions

**Interactions** allow you to proactively start conversations with your customers. Unlike **Message Center** and
feedback in general, you can use **Interactions** to start communicating with a customer based on how they are using the
app. An **Interaction** is a view that is shown to the customer when certain conditions are met.

The core pieces of information used to determine when and where **Interactions** are displayed are called **Events**. An
**Event** represents a place in your code where your customer performed an action. Apptentive keeps track of all
**Events**, and the record of **Events** enables you to perform very fine grained targeting of **Interactions** to
customers. You can configure **Interactions** to run when a certain combination of **Events** has been triggered.

A single API method makes all of this happen: `Apptentive.engage(String eventName)`. When you call `engage()`, not only
are **Events** created, but **Interactions** are run if the necessary conditions are met. This simple, but powerful
method, will let you precisely target who to talk to at the right time. You are recommended to find a few places in your
code that you would like to track, and a few places where it would be appropriate to show an **Interaction**. Come up
with an **Event** name that describes each place, and make a call to `engage()`. Later on, you can configure
**Interactions** based on those **Events**.

###### Event
An **Event** is a record of your customer performing an action. An **Event** is always generated when you call
`Apptentive.engage(String eventName)`. Apptentive stores a record of all events, and events can be used later to
determine when to show interactions to your customer.

###### Interaction
An action performed on the client. **Interactions** are defined on the server, and downloaded to the client.
**Interactions** generally result in a view being shown to your customer. Like **Events**, **Interactions** are launched
by calling `Apptentive.engage(String eventName)`, but only when the necessary conditions are met.

##### Example
Lets say you have a cloud storage app, and you would like to show an **Interaction** when the app starts, provided that
the customer has uploaded at least five files. You could choose to have two **Events**: `main_activity_focused`, and
`user_uploaded_file`. When your main Activity regains focus, you would call `Apptentive.engage("main_activity_focused")`,
and when the customer performs a file upload, you could call `Apptentive.engage("user_uploaded_file")`. You can then go
into the server, and configure the **Interaction** to run when the `main_activity_focused` Event is triggered, and set the
conditions such that the `user_uploaded_file` **Event** had been seen at least five times.

**Note:** You will need to trigger an **Event** called `init`. This **Event** is used as the default point of
display for new **Interactions**. To do so, simply call `Apptentive.engage(activity, "init")` at an appropriate place,
such as when your main Activity gains focus.

Below are the currently supported **Interactions**. To configure **Interactions**, login to
[Apptentive](www.apptentive.com) and click on **_Interactions_**.

#### Ratings Prompt

The **Ratings Prompt Interaction** replaces our previous call to `Apptentive.showRatingFlowIfConditionsAreMet()`.
Instead, the only integration necessary is to define some events in your code, and then choose to target the **Ratings
Prompt Interaction** to one of those events.

![Enjoyment Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/enjoyment_dialog.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Rating Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/rating_dialog.png)

##### Example

Trigger an **Event** when an Activity gains focus.

```java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
        // Engage a code point called "init".
        boolean shown = Apptentive.engage(this, "init");
    }
}
```

You can customize the the content, as well as the display conditions of the **Ratings Prompt Interaction** through your
[Apptentive](https://www.apptentive.com) account. The **Ratings Prompt Interaction** can be targeted to any **Event** you
choose, but by default, it is targeted to the `init` **Event**.

![Using Custom Events](https://raw.githubusercontent.com/apptentive/apptentive-android/master/etc/screenshots/ratings_prompt_interaction_config.png)

**Note:** If you used the previous Rating Prompt in your app, you can replace calls to `logSignificantEvent()` with other
calls to `engage()` with various event names. You can then base the logic that determines when an interaction will be
displayed on these events.

#### Upgrade Messages

You can display an **Upgrade Message Interaction** to customers when they upgrade to a newer version of your app.
Configure which version name or version code of your app each **Interaction** is targeted to, and the **Interaction**
will be shown when that release is launched by the customer. Right now, Upgrade Messages are always targeted to the
`init` **Event**, so make sure you call `Apptentive.engage(activity, "init)`.


## Optional Configuration

### Support for Amazon Appstore

If your app is being built for the Amazon Appstore, you will want to make sure customers who want to rate you app are
taken there instead of to Google Play. To do this, simply add the following line in `onCreate()`.

```java
Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());
```

If you omit this line, ratings will go to Google Play.

### Specifying the Customer's Email Address

If you are authorized to access the customer's email address, you may specify it during initialization so that in the
event the customer does not open the app to view your reply, your message can still get to them via email. Note that if
ths customer updates their email through an Apptentive UI, we will use that email instead.

###### Method

```java
public static void Apptentive.setInitialUserEmail(Context context, String email);
```

### Send Custom Device Data to Apptentive

You may send us custom data associated with the device, that will be surfaced for you on our website. Data must be
key/value string pairs.

###### Method

```java
public static void Apptentive.addCustomDeviceData(Context context, String key, String value);
```

###### Method

```java
public static void Apptentive.removeCustomDeviceData(Context context, String key);
```

### Send Custom Person Data to Apptentive

You may send us custom data associated with the person using the app, that will be surfaced for you on our website.
Data must be key/value string pairs.

###### Method

```java
public static void Apptentive.addCustomPersonData(Context context, String key, String value);
```

###### Method

```java
public static void Apptentive.removeCustomPersonData(Context context, String key);
```

## Third Party Integrations
Apptentive can be configured to send push notifications to your app, using the push notification provider of your choice.
**Urban Airship** and **Amazon's AWS SNS** are currently supported. A push notification is useful for notifying your
customers that they have received a new message while they are not using your app. Push notifications are optional, and
messages will still be delivered when the customer opens the app.

Third party integrations require a Grow Plan.

### Push Notifications

#### Urban Airship Integration

In order to use **Urban Airship**, you will need to first setup **Urban Airship** to work within your app. Then, you
will need to set your `App Key`, `App Secret`, and `App Master Secret` on the website at
**App Settings -> Integrations -> Urban Airship**.

##### Sending the Urban Airship APID

To set up push notifications, you must pass in the `APID` you get from **Urban Airship**. The `APID` is available only
after you initialize **Urban Airship**, so you will have to read it from the `BroadcastReceiver` you use to receive
**Urban Airship** `Intents`.

###### Method
```java
Apptentive.addUrbanAirshipPushIntegration(Context context, String apid);
```

###### Example
```java
String  apid = PushManager.shared().getAPID();
Apptentive.addUrbanAirshipPushIntegration(this, apid);
```

#### Amazon SNS

In order to use **Amazon Web Services (AWS) Simple Notification Service (SNS)**, you will need to first set up
**AWS SNS** to work within your app. Then, you will need to set your `Access Key ID`, `Secret Access Key`, and `ARN` on
the website at **App Settings -> Integrations -> Amazon Web Services SNS**.

##### Sending the AWS SNS Registration ID

To set up push notifications, you must pass in the **Registration ID** you get from **AWS SNS**. The **Registration ID**
is returned when you register for push notifications with
[GoogleCloudMessaging.register(String... senderIds)](http://developer.android.com/reference/com/google/android/gms/gcm/GoogleCloudMessaging.html#register%28java.lang.String...%29).

###### Method
```java
Apptentive.addAmazonSnsPushIntegration(Context context, String registrationId);
```

###### Example
```java
GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
String  registrationId = gcm.register(getString(R.string.project_number));
Apptentive.addAmazonSnsPushIntegration(this, registrationId);
```

#### Passing Apptentive the Push Intent

When the customer opens a push notification, you will receive an `Intent` in your `BroadcastReceiver`. You must always
pass that `Intent` to Apptentive, so we can check to see if the push came from us, and save our data to use when we launch.

###### Method
```java
public static void setPendingPushNotification(Context context, Intent intent);
```

#### Running the Apptentive Push UI

Next, in the `Activity` that you launched, you will need to allow Apptentive to run based on the push `Intent`. If the
push notification came from us, this version of the SDK is compatible with the notification, and other conditions are
met, then we will perform an action. This is generally to show a UI, such as **Message Center**. If we show a UI, this
method will return true, else false. This method is a noop if the push notification was not from Apptentive.

###### Method
```java
public boolean Apptentive.handleOpenedPushNotification(Activity activity);
```

###### Example
```java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
        boolean ranApptentive = Apptentive.handleOpenedPushNotification(this);
    }
}
```
---

## Gradle Integration

The following documents preliminary gradle support for the Apptentive SDK. Since the Android Gradle plugin is constantly
changing, this information may be incompatible with the version of Gradle that you are using. We will attempt to keep
this up to date and working, but there may be a lag between new releases of the Gradle plugin, and updates to this doc.

1. Import the existing Apptentive Android SDK module into your project.
2. Add the android-Gradle facet to the apptentive-android-sdk module (in the module settings).
3. Add a build.gradle file with this content to apptentive-android-sdk:
    ```
    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:0.5.+'
        }
    }
    apply plugin: 'android-library'

    repositories {
        mavenCentral()
    }

    android {
        compileSdkVersion 17
        buildToolsVersion "17.0.0"

        defaultConfig {
            minSdkVersion 7
            targetSdkVersion 16
        }

        sourceSets {
            main {
                manifest.srcFile 'AndroidManifest.xml'
                java.srcDirs = ['src']
                resources.srcDirs = ['src']
                res.srcDirs = ['res']
            }
        }
    }
    ```
4. In your main module's build.gradle file, add a reference to the Apptentive Android SDK :
    ```
    dependencies {
        compile project(":apptentive-android-sdk")
    }
    ```

5. In your settings.gradle file, add an include for apptentive-android-sdk:
    ```
    include ':apptentive-android-sdk', ':your-module'
    ```

6. Adjust the gradle versions to suit your app.


## Building from the command line and with CI

### Building with ant

The Apptentive SDK can be built using the `ant` based build tools bundled with the Android SDK. In order to prepare Apptentive for automated builds with ant, you will need to prepare the project for builds using the `android` tool packaged with the Android SDK. Open a shell and run `android update project -p ./ -t android-18` in the apptentive/apptentive-android-sdk directory. The target, `android-18` in the example, can be any version of android greater than or equal to 3.1 (`android-12`).

Once you have initialized the build files, Apptentive will build automatically as part of your ant based build system. In the event that you update your Android SDK or Android Build Tools, you may need to re-run the `update project` command to generate new build files.

## ProGuard Configuration

Since Apptentive is an open source SDK, it is not necessary to obfuscate Apptentive code. If you are using ProGuard, Apptentive classes and methods will be obfuscated unless you add the following to your project's `proguard-project.txt`:

```java
-keepattributes SourceFile,LineNumberTable
-keep class com.apptentive.android.sdk.** { *; }
```

## Known Issues

* If you are using the [OkHttp](http://square.github.io/okhttp/) library, please make sure you are using OkHttp version 1.5.2 or greater, as previous versions can cause your app to crash when another library attempts to make an SSL connection.
