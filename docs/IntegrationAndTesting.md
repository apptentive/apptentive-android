# Integration & Testing

This document will show you how to integrate the Apptentive Android SDK into your app, configure it, and test to make
sure it's working properly. Each section lists the minimum necessary configuration, as well as optional steps.

# Download

The Apptentive Android SDK is open source. The project is lociated [here](https://github.com/apptentive/apptentive-android).

To download the SDK, either clone the SDK

`git clone https://github.com/apptentive/apptentive-android.git`

Or download the [latest release](https://github.com/apptentive/apptentive-android/releases).

### Keep Up To Date

We strive to fix bugs and add new features as quickly as possible. Please watch our Github repo so stay up to date.

# Setting up the Project

### Using IntelliJ IDEA

These instructions were tested with IntelliJ IDEA 13.1.2

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

# Modifying your Manifest

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

**Note:** Be sure to input your Apptentive API Key where it says `YOUR_API_KEY_GOES_HERE`.

# Integrate your Activities with Apptentive

In order to keep track of Application state, we need to hook into the lifecycle of each Activity defined in your app.
There are two ways of doing this: Inheritance and Delegation. Inheritance is the easiest method, while delegation is
provided if you can't or don't want to inherit from our Activities.

Integrate ALL of the Activities in your app with [ApptentiveActivity](http://www.apptentive.com/docs/android/api/index.html?com/apptentive/android/sdk/ApptentiveActivity.html).
You can mix and match, but make sure they all integrate in one of the following ways.

1. **Inheritance**

    ```java
    import com.apptentive.android.sdk.ApptentiveActivity;

    public class ExampleActivity extends ApptentiveActivity {
    ```

2. **Delegation**

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

# Message Center

The [Message Center](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#message-center) is a self contained Activity that you can launch with the `Apptentive.showMessageCenter()` method.

You should find a place in your app where you can create a link or button that opens your **Message Center**.

###### Example

Here is how you can show **Message Center** by hooking it up to a button in your app.

```java
Button messageCenterButton = (Button)findViewById(R.id.your_message_center_button);
messageCenterButton.setOnClickListener(new View.OnClickListener(){
    public void onClick(View v) {
        Apptentive.showMessageCenter(YourActivity.this);
    }
});
```

### Send Custom Data With a Message (Optional)

Additionally, you can supply custom key/value pairs that will be sent in the next message that the customer sends while
**Message Center** is open. For instance, if you have a dining app, you could pass in a key of `restaurant` and value of
`Joe's Pizza`. If the customer sends a more than one message, only the first message will include this custom data. If
you wish to add more custom data to another subsequent message, you will need to call this method with custom data again.

###### Example

```java
    Map<String, String> customData = new HashMap<String, String>();
    customData.put("restaurant", "Joe's Pizza");
    Apptentive.showMessageCenter(YourActivity.this, customData);
```

### New Message Notification (Optional)

If you would like to be notified when a new message is sent to the client, register a listener using `Apptentive.setUnreadMessagesListener(UnreadMessageListener listener)`.
When the number of unread messages changes, either because your customer read a message, or a new message came in, [onUnreadMessageCountChanged(int unreadMessages)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/module/messagecenter/UnreadMessagesListener.html#onUnreadMessageCountChanged%28int%29)
will be called. Because this listener could be called at any time, you should store the value returned from this method,
and then perform any user interaction you desire at the appropriate time.

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

# Adding Events

You should add a handful of [Events](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#events)
to your app when you integrate. Since **Events** are both records of an action within your app being performed, and an
opportunity to show an [Interaction](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#interactions),
you should choose places within your app that would be appropriate to interact with your customer, as well as places
where a significant event has occured. The more **Events** you add during integration, the more you will learn about
your customers, and the more fine tuned your communications with them can be. Here is a list of potential places to add
**Events**.

Places where you might want to show an **Interaction**:
* Main Activity gains focus
* Settings Activity gains focus
* Customer performs an action that indicates they are confused
* There is a natural pause in the app's UI where starting a conversation would not offend the customer

Places where you might want to record a significant event:
* Customer makes a purchase
* Customer declines to make a purchase
* Customer beats a level
* Customer performs an action that indicates they know how to use your app
* Customer performs an action that indicates they are confused
* Your app crashes

As you can see, there is some overlap in whether you want to just record an **Event**, or also show an **Interaction**.

To add an **Event** and possibly show an **Interaction**, simply call [Apptentive.engage(Activity activity, String eventName)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29)
with an `eventName` of your choosing.

###### Examples

Add an **Event** when your app's main Activity comes up.

```java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
        // Engage a code point called "init".
        boolean shown = Apptentive.engage(this, "main_activity_focused");
    }
}
```

**Note:** Each **Event** should have a unique name.


# Configure Interactions

Once you have configured your app to use several **Events**, you can configure **Interactions** on [apptentive.com](https://be.apptentive.com)


// TODO

# Interaction specific methods

// TODO

Survey Finished Listener

# Push Notifications

**Apptentive** can send [push notifications](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#push-notifications)
to your app when you reply to your customers. Your replies are more likely to be seen by your customer when you do this.
To set up push notifications, you will need to enter your push credentials on [apptentive.com](https://be.apptentive.com),
send us the id that your push provider uses to identify the device, and call into our SDK when you receive and open a
push notification.

### Supported Push Providers

* Urban Airship
* Amazom Web Services SNS

### Configuring Your Push Credentials

To enter your push credentials, go to [apptentive.com](https://be.apptentive.com), select *Settings -> Integrations*,
choose either *Urban Airship* or *Amazon Web Services SNS*, and follow the instructions on that page.

### Setting the Device Token

In order for **Apptentive** to send push notifications to the correct device, you will need to pass us the device
identifier for the push provider you are using.

#### Setting the Urban Airship APID

The Urban Airship device ID is called *APID* (Airship Push ID). You can retreive it in one of two ways:

1. If you set up Urban Airship using a BroadcastReceiver to listen to Intents that Urban Airship sends you, you can
retreive the APID by listening for the Intent with action [PushManager.ACTION_REGISTRATION_FINISHED](http://docs.urbanairship.com/reference/libraries/android/latest/reference/com/urbanairship/push/PushManager.html#ACTION_REGISTRATION_FINISHED),
grabbing the extra data [PushManager.EXTRA_APID](http://docs.urbanairship.com/reference/libraries/android/latest/reference/com/urbanairship/push/PushManager.html#EXTRA_APID),
and passing it to [Apptentive.addUrbanAirshipPushIntegration(Context context, String apid](http://www.apptentive.com/docs/android/api/index.html?com/apptentive/android/sdk/ApptentiveActivity.html).

###### Example

```java
String apid = intent.getStringExtra(PushManager.EXTRA_APID);
Apptentive.addUrbanAirshipPushIntegration(context, apid);
```

This method is preferable, because you will get the APID at the earliest possible time after the app is registered with
UA, and will only need to give it to the Apptentive SDK once.

2. If you are not using a broadcast receiver, you can call [PushManager.getAPID()](http://docs.urbanairship.com/reference/libraries/android/latest/reference/com/urbanairship/push/PushManager.html#getAPID%28%29).
This method may return null if Urban Airship hasn't finished registering, so don't give it to us until it returns an
actual apid.

###### Example

```java
String apid = PushManager.getAPID();
if (apid != null) {
  Apptentive.addUrbanAirshipPushIntegration(context, apid);
}
```

#### Setting the Amazon Web Services SNS Registration ID

Amazzon Web Services SNS uses GCM directly on the client, so you will need to use the GCM API to retreive the
Registration ID. See the [GCM documentation](http://developer.android.com/google/gcm/client.html) if you are unsure how
to retreive your Registration ID. When you have the Registration ID, pass it to [Apptentive.addAmazonSnsPushIntegration(Context context, String registrationId)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addAmazonSnsPushIntegration%28android.content.Context,%20java.lang.String%29).

###### Example
```java
String registrationId;
Apptentive.addAmazonSnsPushIntegration(this, registrationId);
```
### Displaying the Push Notification

Opening an Apptentive push notification involves three easy steps: Pass the push notification to [Apptentive.setPendingPushNotification(Context context, Intent intent)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPendingPushNotification%28android.content.Context,%20android.content.Intent%29),
launch your main Activity, and display the push notification with [Apptentive.handleOpenedPushNotification(Activity activity)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#handleOpenedPushNotification%28android.app.Activity%29).
This two pass approach is necessary to avoid double displaying the push notification. Both of these methods do nothing
if the push notification didn't come from Apptentive.

###### Example

Push notification opened.

```java
Apptentive.setPendingPushNotification(context, intent);

// Launch your main Activity
```

In your main Activity, open the push notification.

```java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
  super.onWindowFocusChanged(hasFocus);
  if (hasFocus) {
    boolean ranApptentive = Apptentive.handleOpenedPushNotification(this);
    if (ranApptentive) {
      // Don't try to take any action here. Wait until the customer closes our UI.
      return;
    }
  }
}
```

# Set Customer Email address

If you already know the customer's email address, you can pass it to us during initialization. Simple call [Apptentive.setInitialUserEmail(Context context, String email)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setInitialUserEmail%28android.content.Context,%20java.lang.String%29).
Make sure to call it in your main Activity's `onCreate()`.

# Custom Data

You can send [Custom Data](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#custom-data)
associated with either the device, or the person using the app. This is useful for sending
user IDs and other information that helps you support your users better. **Custom Data** can also be used for
configuring when [Interactions](https://github.com/skykelsey/apptentive-android/blob/new_docs/docs/Features.md#interactions)
will run. For best results, call this during `onCreate()`.

###### Example

Send the user ID of this person.

```java
Apptentive.addCustomPersonData(this, "1234567890");
```

# Attachments

# Setting Rating Provider

# Sending Attachment Messages

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

#### Surveys

Surveys are a great tool to learn about your customers. You can create surveys that have multiple types of questions:
multiple select, single select, and free text. You can also choose the audience your survey is shown to by choosing to
send it only to devices which match certain criteria. Finally, you can chose to limit how often and how many times a
survey can be shown to each person, or to the entire customer base.

![Survey Incomplete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_incomplete.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Survey Complete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_complete.png)

Surveys a type of **Interaction**, so they are launched using the `Apptentive.engage()` method.

**Note:** If you were using surveys prior to version 1.5.0 of the Apptentive Android SDK, see this Migration Guide for
instructions.

##### Example

Trigger an **Event** when the user does something.

```java
boolean showedInteraction = Apptentive.engage(this, "user_did_something");
```

Surveys can be created and managed on the website at **Interactions -> Surveys**.

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
