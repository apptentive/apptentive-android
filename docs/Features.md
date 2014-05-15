# System Requirements

### SDK Versions

**Minimum SDK version:** 7 (Android 2.1)

**Build SDK version:** 12 (3.1)

The Apptentive SDK works on devices with **Android SDK 2.1 (API Level 7)** and newer. You must also build your app
against **Android SDK 3.1 (API 12)** or newer. This will not cause your app to stop working on pre-3.1 devices, but
allows us to use newer XML syntax for forward compatibility.

###Supported Languages

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

---

# Events and Interactions

Our SDK lets you keep track of customer behavior, and initiate conversations with each customer based on their behavior.
**Interactions** are the UI elements that you use to interact with each customer, and **Events** are the records of past
customer behavior.

### Events

**Events** are records of actions taken in your app. What constitutes an **Event** is up to you. For example, you may
choose to keep track of each time a user has logs into your app, the app crashes, or they pass a level. **Events** are
stored on the client, and can be used to determine when and where to show **Interactions**. Your app should contain at
least five **Events**. The more **Events** you define in your app, the more powerful useful the Apptentive SDK will be.

**Events** are invoked using the [Apptentive.engage()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29) method.
When your app invokes an **Event**, it will be sent to our server. It wiill show up on [apptentive.com](https://be.apptentive.com)
under *Interactions -> Events*, so you can verify that **Events** are making it to our server. If you would like to
predefine an **Event** name so that you can use it in an **Interaction** before you make any changes on the client, you
can do so by entering it manually here as well.

### Interactions

**Interactions** are views that you can use to easily and proactively start conversations with your customers. You
configure their content, the conditions necessary to show them, and which **Event** should trigger them,
and the Apptentive SDK takes care of fetching them, evaluating the logic they contain, and displaying them. Each
**Interaction** is configured on the server, so you can easily add or modify them after you've released your app,
without modifying the source code. **Interactions** are displayed using the same [Apptentive.engage()](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29)
method as **Events**, which is why adding several **Events** to your app during development is important. It allows you
to configure an **Interaction** to display at **Event** of your choosing after you have shipped your app. The difference
between an **Event** and an **Interaction** is that an **Event** is a record of the `engage()` method being called,
while an **Interaction** is a view that is displayed when `engage()` is called.

**Interactions** can be configured on [apptentive.com](https://be.apptentive.com) by clicking the *Interactions*
link in the top bar.

## Ratings Prompt

The **Ratings Prompt Interaction** is a powerful tool to help you get better ratings and more reviews from happy
customers, and talk to customers that aren't happy with your app. The **Ratings Prompt** is actually a series of views.
The inital view is called the *Enjoyment Dialog*, and ask the customer whether they love your app. If the answer is yes,
then they are shown the *Rating Dialog*, which offers them the oportunity to go to the app store to rate the app. If
 they don't love your app, will be taken to the [Message Center](#message-center) where they can give you feedback.

![Enjoyment Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/enjoyment_dialog.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Rating Dialog](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/rating_dialog.png)

To set up the **Ratings Prompt**, visit [apptentive.com](https://be.apptentive.com) and go to *Interactions ->
Ratings Prompt*. There, you can customize the **Ratings Prompt** content, specify which **Event** to target the prompt
with, and configure when to thos the prompt, and who to show it to.

![Using Custom Events](https://raw.githubusercontent.com/apptentive/apptentive-android/master/etc/screenshots/ratings_prompt_interaction_config.png)

## Surveys

**Surveys** are **Interactions** that help you understand the wants and needs of your customers. **Surveys** are
composed of one of more questions, and since they are **Interactions**, can be targeted to any **Event** you have
configured in your app. There are three supported question types: Single Select, Multiple Select, and Free Form. To set
up a survey on [apptentive.com](https://be.apptentive.com), navigate to *Interactions -> Surveys*.

![Survey Incomplete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_incomplete.png)
![spacer](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/10px.png)
![Survey Complete](https://raw.github.com/apptentive/apptentive-android/master/etc/screenshots/survey_complete.png)

## Upgrade Messages

Many customers choose to have their apps automatically update. While this is convenient for them, it mean they often
won't see announcements or release notes that you display on your app store page. **Upgrade Messages** solve this
problem. You can configure an **Upgrade Message** for each release of your app. When the user upgrades from a previous
version of your app to one of the targeted versions, whey will see the **Upgrade Message**. To view and create **Upgrade
Messages** on [apptentive.com](https://be.apptentive.com), navigate to *Interactions -> Upgrade Messages*.

**Note**: **Upgrade Messages** are always targeted to the special `init` **Event**. you should trigger `init` at
the first opportunity when your app starts up by calling `Apptentive.engage(this, "init")`.

---

# Message Center

The Apptentive Message Center you and your customers talk directly without making them leave your app. By providing
support through the app, the customer, you are able to provide high quality support and make your customers feel loved.

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

### Displaying Message Center

The **Message Center** is displayed by calling [Apptentive.showMessageCenter(Activity activity)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter%28android.app.Activity%29).

### Passing Custom Data on Messages

Additionally, you can supply custom key/value pairs that will be sent in the next message that the customer sends while
the **Message Center** is open. For instance, if you have a dining app, you could pass in a key of `restaurant` and
value of `Joe's Pizza`. If the customer sends a more than one message, only the first message will include this custom
data. If you wish to add more custom data to another subsequent message, you will need to call this method with custom
data again. When the message comes in on the Apptnetive website, it will have this custom data attached so you can
understand what yoru customer was doing when they decided to send feedback.

[Apptentive.showMessageCenter(Activity activity, Map<String, String> customData)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#showMessageCenter%28android.app.Activity,%20java.util.Map%29).

### New Message Notifications

You can also receive a notification when the number of unread messages waiting to be viewed by the customer changes.

[Apptentive.setUnreadMessageListener(UnreadMessageListener listener)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setUnreadMessagesListener%28com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener%29)

### Checking Unread Message Count

You can also check to see how many messages are waiting to be read in the customer's **Message Center**.

[Aptentive.getUnreadMessageCount(Context context)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#getUnreadMessageCount%28android.content.Context%29)

### Sending Hidden Messages and Attachments

You can also send hidden messages from the client that will show up in your customer convesation. They won't be visible
to the customer, but you can send file and text messages that help fill in details about the state of the app, so you
can better support your customers.

#### Sending File Attachments

* [Apptentive.sendAttachmentFile(Context context, byte&#91;&#93; data, String mimeType)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile%28android.content.Context,%20byte[],%20java.lang.String%29)
* [Apptentive.sendAttachmentFile(Context context, InputStream is, String mimeType)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile%28android.content.Context,%20java.io.InputStream,%20java.lang.String%29)
* [Apptentive.sendAttachmentFile(Context context, String Uri)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentFile%28android.content.Context,%20java.lang.String%29)

#### Sending Hidden Text Messages

* [Apptentive.sendAttachmentText(Context context, String text)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#sendAttachmentText%28android.content.Context,%20java.lang.String%29)

---

# Misc

## Setting Rating Provider

By default, the Ratings Prompt will open the Google Play app store. You can force ratings to use a different app store
with the [Apptentive.setRatingProvider(IRatingProvider ratingProvider)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setRatingProvider%28com.apptentive.android.sdk.module.rating.IRatingProvider%29)
method. If you would like to open an app store we don't yet support, you can implement the
[IRatingProvider](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/module/rating/IRatingProvider.html)
interface.

### Supported App Store Rating Providers

* [Google Play](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/module/rating/impl/GooglePlayRatingProvider.html)
* [Amazon Appstore](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/module/rating/impl/AmazonAppstoreRatingProvider.html)

### Settings Rating Provider Arguments

Your Rating Provider may need additional data passed to it.

[Apptentive.putRatingProviderArg(String, key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#putRatingProviderArg%28java.lang.String,%20java.lang.String%29)

## Push Notifications

You can leverage your push notification provider to notify customers when you respond to their feedback. With push
notifications, your customer will see a notification, even if they haven't recently opened your app.

### Supported Push Providers

* Urban Airship
* Amason Web Services SNS

### Setting Up the Client

You should make sure your app is already set up to work with your chosen push provider. You will then need to pass
**Apptentive** the token that the push provider assigns your app.

#### Urban Airship

In order to use **Urban Airship**, you will need to first setup **Urban Airship** to work within your app. Then, you
will need to set your `App Key`, `App Secret`, and `App Master Secret` on the website at
*Settings -> Integrations -> Urban Airship*.

When your app registers with **Urban Airship**, it will need to send the *Airship Push ID* (`APID`) to us so that we can
send push notifications to the correct device. The `APID` is available only after you initialize **Urban Airship**, so
you will have to read it from the `BroadcastReceiver` you use to receive **Urban Airship** `Intents`.

[Apptentive.addUrbanAirshipPushIntegration(Activity activity, String apid)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addUrbanAirshipPushIntegration%28android.content.Context,%20java.lang.String%29)

###### Example

```java
String  apid = PushManager.shared().getAPID();
Apptentive.addUrbanAirshipPushIntegration(this, apid);
```

#### Amazon SNS

In order to use **Amazon Web Services (AWS) Simple Notification Service (SNS)**, you will need to first set up
**AWS SNS** to work within your app. Then, you will need to set your `Access Key ID`, `Secret Access Key`, and `ARN` on
the website at *Settings -> Integrations -> Amazon Web Services SNS*.

##### Sending the AWS SNS Registration ID

To set up push notifications, you must pass in the **Registration ID** you get from **AWS SNS**. The **Registration ID**
is returned when you register for push notifications with
[GoogleCloudMessaging.register(String... senderIds)](http://developer.android.com/reference/com/google/android/gms/gcm/GoogleCloudMessaging.html#register%28java.lang.String...%29).
You can then pass the Registration ID to us using this method:

[Apptentive.addAmazonSnsPushIntegration(Activity activity, String registrationId)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addAmazonSnsPushIntegration%28android.content.Context,%20java.lang.String%29)

###### Example

```java
GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
String  registrationId = gcm.register(getString(R.string.project_number));
Apptentive.addAmazonSnsPushIntegration(this, registrationId);
```

### Passing Apptentive the Push Intent

When the customer opens a push notification, you will receive an `Intent` in your `BroadcastReceiver`. You must always
pass that `Intent` to Apptentive, so we can check to see if the push came from us, and save our data to use when we launch.

[Apptentive.setPendingPushNotification(Context context, Intent intent)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setPendingPushNotification%28android.content.Context,%20android.content.Intent%29)

#### Running the Apptentive Push UI

Next, in the `Activity` that you launched, you will need to allow Apptentive to run based on the push `Intent`. If the
push notification came from us, this version of the SDK is compatible with the notification, and other conditions are
met, then we will perform an action. This is generally to show a UI, such as **Message Center**. If we show a UI, this
method will return true, else false. This method is a no-op if the push notification was not from **Apptentive**.

[Apptentive.handleOpenedPushNotification(Activity activity)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#handleOpenedPushNotification%28android.app.Activity%29)

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

## Custom Data

You may send us custom data associated with the **Device** or **Person**, that will be surfaced for you on our website. Data must be
key/value string pairs. You can use this data simply to fill in information about the customer, but you can also use it
in the logic that **[Interactions](Interactions)** use to determine when and if they can be displayed.

* [Apptentive.addCustomDeviceData(Context context, String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomDeviceData%28android.content.Context,%20java.lang.String,%20java.lang.String%29)
* [Apptentive.removeCustomDeviceData(Context context, String key)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#removeCustomDeviceData%28android.content.Context,%20java.lang.String%29)
* [Apptentive.setCustomDeviceData(Context context, Map<String, String> customDeviceData)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setCustomDeviceData%28android.content.Context,%20java.util.Map%29)
* [Apptentive.addCustomPersonData(Context context, String key, String value)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#addCustomPersonData%28android.content.Context,%20java.lang.String,%20java.lang.String%29)
* [Apptentive.removeCustomPersonData(Context context, String key)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#removeCustomPersonData%28android.content.Context,%20java.lang.String%29)
* [Apptentive.setCustomPersonData(Context context, Map<String, String> customDeviceData)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setCustomPersonData%28android.content.Context,%20java.util.Map%29)

## Specifying a Customer's Email Address

If you are authorized to access the customer's email address, you may specify it during initialization so that in the
event the customer does not open the app to view your reply, your message can still get to them via email. Note that if
ths customer updates their email through an Apptentive UI, we will use that email instead.

[Apptentive.setInitialUserEmail(Context context, String email)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setInitialUserEmail%28android.content.Context,%20java.lang.String%29)

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
