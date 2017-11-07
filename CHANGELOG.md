# 2017-11-06 - v4.1.0

#### Improvements

* Improved accessibility of Surveys and Message center for the visually impaired
* Made use of vector drawables to cut down on AAR and APK size
* Added the ability to enable troubleshooting mode in the SDK, and easily email logs to Apptentive support

#### Bugs Fixed

* Fix global configuration fetching
* Fix Love Dialog to stack buttons if their labels are too long

# 2017-08-15 - v4.0.2

#### Bugs Fixed

* Fixes Message Center UI bugs.

# 2017-07-25 - v4.0.1

#### Improvements

* Improved proguard configuration to allow certain Apptentive classes to be shrunk. This removes about 1200 methods.

#### Bugs Fixed

* Fixes a bug that prevented interactions from being downloaded when the app is in release mode.

# 2017-07-19 - v4.0.0

#### Improvements

* Added the ability to log customers in to different private conversations. Customers who are logged in will be able to see a private conversation that is distinct from the converations of other customers using the same app on the same device. When they log out, their data is protected with strong encryption. Logging back in unlocks their data again, and Apptentive resumes providing services to the customer.

# 2017-03-02 - v3.4.1

#### Improvements

* Added an internal method `Apptentive.dismissAllInteraction()` for closing Apptentive UI.

# 2016-12-07 - v3.4.0

#### Improvements

* Rebuilt Message Center to use modern `RecyclerView` for better performance and stability.
* Improved accessibility of our UI, including TalkBack support and fixing hit target sizes.
* Improved version targeting

#### Bugs Fixed

* Fixed a bug where the Profile Card in Message Center wouldn't let a user focus the email field.
* Fixed a bug where the Survey "thank you" message text was the wrong color.

# 2016-10-21 - v3.3.0

#### Improvements

* Added new APIs for handling Apptentive push notifications. You can now get a `PendingIntent` from Apptentive if we sent the push. The `PendingIntent` can be used to launch Message Center directly from your Notification object. Old APIs are deprecated. 
* Send debug status to the server so you can target only debug or non-debug builds with your interactions.
 
# 2016-10-11 - v3.2.2

#### Bugs Fixed

* Fixed a bug where apps with names or versions ending in an underscore weren't tracked properly.
* Fixed a bug that sometimes prevented the Message Center composing area from gaining focus, even when tapped.
* Removed unneeded multidex config.

# 2016-08-09 - v3.2.1

#### Bugs Fixed

* Add proguard rule to prevent obfuscating `android.support.v4.app.FragmentManagerImpl`, which we load via reflection in a workaround for a bug in the support library.
* Prevent NPE when animation runs after object is nulled.
* Fix a bug that sometimes prevented the Message Center Composing view from gaining focus on user touch.

# 2016-07-13 - v3.2.0

#### Improvements

* Added a new "NPS" question type for surveys.

#### Bugs Fixed

* Fixed Message Center composing bar exception thrown when the animation was in play after the fragment was detached.
* Fixed Message Center exception caused by requesting focus on a nulled EditText.
* Moved database calls off the UI thread.

# 2016-06-20 - v3.1.1

#### Improvements

* Defer message polling task until Message Center is opened, UnreadMessagesListener is registered, or a Push is received.
* Add internal method to set the application theme programmatically from which the Apptentive UI will inherit styles.

#### Bugs Fixed

* statusBarColor attribute was causing run-time exception on pre-21 devices.

# 2016-06-08 - v3.1.0

#### Improvements

* We've added a new answer type to multiple choice surveys. You can now specify an "Other" answer type for multiple choice questions. When a user selects this answer, a text input field will display. Any text entered in that field will be included in the survey response.

# 2016-05-25 - v3.0.1

#### Bugs Fixed

* Thumbnail in Recents Screen was not using host app's theme when the top Activity was an Apptentive Activity.
* Improve foreground / background detection.
* Fix borderless button styling before API level 23.
* Fix our UI to work better when the host app uses a translucent status bar.
* Fix window panning issue that can result in the keyboard coering part of a survey. 

# 2016-04-26 - v3.0.0

#### Improvements

* We've made it much easier to integrate the Apptentive Android SDK into your app. Now, just add a single line to your `build.gradle`, and one to your `Application` class. If you are migrating from a prior version, make sure to follow our simple [Migration Guide](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_3.0.0.md).
* Our SDK's UI will now inherit all styles from your app, as long as you are using an AppCompat or Material theme. If you want to further customize our UI, or you aren't using an AppCompat or Material theme, you can override our styles. Check out our [UI Customization Guide](https://docs.apptentive.com/android/customization/).
* Our Surveys have been completely redesigned to use Material design. Your existing surveys will continue to work as before, but will look much better.
* You can now launch our UI without passing in an Activity. This is useful for recording Events and showing Interactions from a Service or BroadcastReceiver. Interactions launched in this way will launch in a new task, while those launched with an Activity Context will launch on top of the Activity stack as before.
* Other improvements include reducing sdk memory footprint and more accurate application lifecycle management.


# 2016-03-22 - v2.1.4

#### Bug Fixes and Improvements

*   All Apptentive resources now have an "apptentive_" prefix
*   Apptentive's Log class is renamed to "ApptentiveLog" to avoid confusion


# 2016-01-22 - v2.1.3

#### Bug Fixes and Improvements

*  If apps upgrading from pre-2.0.0 still has now obsolete NetworkStateReceiver defined in their manifest, an immediate run-time assertion error will help app developers detect early. 

# 2016-01-08 - v2.1.2

#### Bugs Fixed

* Fixed Message Center bug where non-english messages were either garbled or failed to send to server

# 2015-12-23 - v2.1.1

#### Bugs Fixed

* Fixed a bug where multiple SqlLite Connections were made when Parse push is integrated

# 2015-12-12 - v2.1.0

#### Improvements

* Message Center now allows consumers to attach up to four images to each outgoing message. You can also attach files to your replies, and they will be downloaded and displayed to consumers when they open Message Center.
* You've always had the ability to send custom data in the Person, Deveice, Message, and Event objects. However, you can now send custom data that is a Number or Boolean, and use that custom data in your Interaction targeting logic.

# 2015-09-15 - v2.0.1

#### Bugs Fixed

* Fixed a [potentially conflicting resource name. [Issue 105](https://github.com/apptentive/apptentive-android/issues/105)
* Fixed a bug affecting display of Surveys.

# 2015-09-09 - v2.0.0

#### Improvements

* Message Center: We've completely rebuilt Message Center from the ground up. Our new Message Center uses Material Design throughout, and features streamlined and optimized behavior to help you get feedback from your customers, or start conversations. Message Center is now much more configurable from the server, so you can make changes to text and behavior after you release your app.
* Apptentive Push: We can now send push notifications to your GCM enabled device without going through a third party push provider. See the [integrations section](https://be.apptentive.com/apps/current/settings/integrations), and check out the [documentation](http://www.apptentive.com/docs/android/integration/#push-notifications).

# 2015-08-12 - v1.7.4

#### Improvements

* Added Polish translations.

# 2015-05-13 - v1.7.3

#### Bugs Fixed

* Fixed a bug where the Feedback Dialog as launched through the Message Center would not be submittable if feedback had already been submitted, Message Center was disabled, and Email was required.

# 2015-05-01 - v1.7.2

#### Improvements

* Updated our Push Notification API to work with Parse 1.8.3 and Urban Airship 6.0.1. If you were using either of these
 push notification providers in a prior version, please see our [Migration Guide](https://github.com/apptentive/apptentive-android-private/blob/push_migration/docs/migrating_to_1.7.2_with_push_notifications.md).

# 2015-04-24 - v1.7.1

* Fixed a bug where HTTP response inputstreams weren't being closed.
* Fixed a bug that prevented the Feedback Thank You dialog from displaying under some circumstances.

# 2015-4-23 - v1.6.7

#### Bugs fixed

* Fixed a bug where HTTP response inputstreams weren't being closed.

# 2015-02-10 - v1.7.0

This release includes support for Notes, which allows you to send messages to your customers and gives them the option
 to take surveys, navigate directly to content within your app, and more. Notes is currently in beta with select 
 customers. If you are interested in trying out Notes, please contact us.

#### Improvements

* Added the ability to display Notes Interactions.
* Added support for invoking nested Interactions, such as launching a Survey from a Note, and opening a Deep Link from a Note.
* Updated to use the latest versions of Android build tools and Android Gradle plugin.

#### Bugs fixed

* Fixed a bug that allowed internal analytics to send multiple launch events per displayed Interaction.

# 2014-11-25 - v1.6.6

#### Improvements

* Update to latest versions of Gradle (2.2), Android Gradle Plugin (0.14.4), Android build tools (21.1.1), and Target SDK Version (21).
* You can now set Apptentive's minimum log level by setting a flag in the manifest.

#### Bugs fixed

* Fixed a bug that prevented feedback from being submitted when email is required, but already supplied.

# 2014-11-17 - v1.6.5

#### Improvements

* Minor improvements to prepare for updated Trigger.io module.

# 2014-10-20 - v1.6.4

#### Improvements

* Dim background when dialogs are shown.
* Request gzip compression from server to save bandwidth.

# 2014-10-15 - v1.6.2

#### Improvements

* Added translations for Arabic, Greek, Brazilian Portuguese, Korean, and Turkish.
* Update to Gradle 2.1 and Android Plugin 0.13.+
* Add gradle configuration for uploading AAR and Javadoc to central maven repo. You can now integrate Apptentive using Maven!
* Refactored message polling and payload sending for simplicity.
* Deprecated NetworkStateReceiver.
* Added method for checking to see if an Interaction will show for a given Event.

# 2014-09-28 - v1.6.1

#### Bugs fixed

* Fix enable Message Center setting.
* Use proper extension when uploading Attachment files.

# 2014-09-17 - v1.5.2

#### Major changes

* Don't check for messages while Message Center is disabled.
* If no network connection is present, network worker threads will go back to sleep instead of exit.

# 2014-09-12 - v1.6.0

#### Major changes

* Refactored repo to be more Gradle friendly. See these instructions if you are upgrading from a previous release.
* Added Gradle support.
* Added legacy IntelliJ project files for those who do not yet use Gradle.
* Simplified the sample apps.
* Added ability to send CustomData and EventData with Events.
* Added Parse push notification support.
* Added ability to set initial user name through API.

#### Bugs fixed

* Fixed Eclipse project files.
* Fixed JsonDiffer to handle zero value float diffing.

If you were using a previous version, see [Migrating to 1.6.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.6.0.md)

# 2014-07-16 - v1.5.1

#### Major changes

* Simplified Apptentive's native UI styling. Common things like buttons in our UI now use the same styles, so changes can easily be made to all similar elements at once.
* Added translations for French Canadian (fr-rCA).
* Added Apache Ant build scripts for the SDK, dev app, and tests.
* Added support to hide Apptentive branding for enterprise accounts.

#### Bugs fixed

* Fixed a rare crash that can occur when a user's database is stored on an SD card and that card is removed.
* Fixed a potential memory leak.
* Fixed default text in the Enjoyment Dialog portion of the Ratings Prompt.
* Removed some benign lint warnings.
* Removed unused resources.
* Fixed a bug that could result in a delay when sending data to the server.
* During development only, if you forget to specify an API key, we will display a dialog to let you know. Previously we only logged it.


# 2014-05-27 - v1.5.0

#### Major changes

We've refactored [Surveys](http://www.apptentive.com/docs/android/features/#surveys) to use our [Event Framework](http://www.apptentive.com/docs/android/features/#events-and-interactions)! You will now have the ability to show surveys at any Event in your code. This release also lets you show a Survey instead of asking for feedback if your customer says they don't love your app from the [Ratings Prompt](http://www.apptentive.com/docs/android/features/#ratings-prompt) Interaction.

See [Migrating to 1.5.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.5.0.md)

#### Bugs fixed

* Fixed a potential crash in the Feedback Dialog if submitted with blank feedback.

# 2014-04-28 - v1.4.3

#### Bugs fixed

* ANDROID-323 Localized strings not properly populated with app name.

# 2014-04-18 - v1.4.2

#### Major changes

* Added support for Amason Web Services (AWS) Simple Notification Service (SNS) push notifications. If you use AWS SNS, you can now notify your customers when you reply to their conversation.

#### Bugs fixed

* Improved robustness of message polling.

# 2014-04-14 - v1.4.1

#### Bugs fixed

* ANDROID-318 Surveys targeting custom person data don't return until the second time they are requested.
* Event Labels were being encoded incorrectly.

# 2014-04-07 - v1.4.0

#### Major changes

* Added new Engagement method. This method will trigger events, and show interactions to the user. Interactions can be configured on the server, and can be based on the count and time that events are shown.
* Moved the existing Rating feature to be an Interaction in this new system. Removed old methods.
* Lots of bug fixes and UI tweaks.
* Introduced background polling for messages. We will poll for new messages every 60 seconds whie the app is up, or every 8 seconds while our Message Center is in the foreground.
* Improved performance and robustness of SDK.

# 2014-04-03 - v1.3.1

#### Major changes

* Add setting to allow Message Center to be disabled by default, even before it has connected to the server.

# 2014-02-24 - v1.3.0

#### Major changes

* Added support for push notifications! If you are using a third party push notification provider, you can configure our server to send a push on your behalf when you reply to a customer. We currently support Urban Airship, but if you are using another provider, please let us know and we will add support.
* Added the ability to send Upgrade Messages to your users. Now you can notify users of new features targeted to specific builds or versions of your app, even if they have opted to auto-update your app in the app store.
* Added the ability to send hidden attachments to the server. You can now send texts, logs, images, and other files to the server to help provide better support. These files are visible to you in teh conversation view, but are not shown to the end user.

# 2014-01-20 - v1.2.7

#### Bugs fixed

* ANDROID-239 PACKAGE_RESTARTED permission causes app to wake often

# 2014-01-19 - v1.2.6

#### Bugs fixed

* ANDROID-238 Survey module doesn't respect "multiple_responses" flag

# 2013-12-17 - v1.2.5

#### Major changes

* Ability to pass Apptentive custom integration details. For instance, you can pass in the app's Urban Airship App ID, allowing us to send push notifications to your device.
* Add the ability to pass push notification intents into Apptentive code, and have Apptentive perform actions. Push integration currently supports Urban Airship, and can be configured in your app settings.
* ANDROID-224 Allow survey thank you dialog to contain links.

# 2013-12-12 - v1.2.4

#### Major changes

Slightly modified the behavior of the rating flow. The user can now close the rating flow by pressing the back button. This should create a less intrusive experience for the people using your app.

#### Bugs fixed

* ANDROID-201 Survey targeting doesn't work until after survey cache expires.
* ANDROID-223 Trim Survey text responses
* Fixed a bug where the rating flow could launch twice, especially on devices running KitKat.

# 2013-11-13 - v1.2.3

#### Major changes

* Add method to API. New way of opening Message Center allowing you to include custom data to be sent with the next message the user sends while the message center is open.
* Implemented an internal error trapping and reporting system. Exceptions in Apptentive code are now caught and logged to our server so we can maintain a reliable app experience and proactively fix hard to reproduce bugs.

#### Bugs fixed

* Fixed a potential crash in survey fetching code.

# 2013-11-05 - v1.2.2

#### Bugs fixed

* Fix memory leak cause by hanging on to Activity context in database code.

# 2013-10-21 - v1.2.1

#### Bugs fixed

* ANDROID-188 Rating logic doesn't respect 0 values with OR

# 2013-10-17 - v1.2.0

#### Major changes

* Send app's TargetSdkVersion with app_release, and OS's API Level with device.
* Use the server setting "Require Email" so that feedback can only be submitted if the user supplies a valid email.
* Get list of available Google account emails and provide them in an AutoCompleteTextView.
* Some minor tweaks to make the IntroDialog look great.
* Added new methods for adding and removing custom data set on the device and person objects. These methods can now be called anywhere in the app's lifecycle, and their values will be picked up the next time an activity starts.
* Added Swedish translation, updated translations for other languages.
* Restyled the screenshot confirmation dialog to match the other dialogs.
* Restyled Thank You dialog shown when surveys are completed, if so specified.

#### Bugs fixed

* ANDROID-178 ConcurrentModificationException in survey code
* ANDROID-181 Surveys can be sent twice
* ANDROID-173 Database locking issue

# 2013-09-15 - v1.1.2

#### Bugs fixed

* ANDROID-155 Message Center can't scroll

# 2013-08-31 - v1.1.1

#### Bugs fixed

* ANDROID-141 Simplified Chinese strings file contains Traditional Chinese
* ANDROID-142 Save the text entered in Message Center if the user closes it.
* ANDROID-145 Dev app can crash in tablet portrait mode.

# 2013-08-29 - v1.1.0

#### Major changes

* Support new enterprise survey features: tagged surveys, rate limiting, delivery capping, and time range constraining.
* New clean UI fro survey display.
* New clean UI for about page.
* Added ability to set the display name of this app from the web UI. The display name is used to surface a different name than the one specified in your package, and is handled transparently from the client's perspective.

# 2013-08-16 - v1.0.4

#### Major changes

* First release of a localized SDK. See README for supported languages.

#### Bugs Fixed

* ANDROID-131 Survey clipping background.
* ANDROID-132 Radio Version is not being set in Device object
* ANDROID-134 Rating logic does not respect 0 values

# 2013-07-19 - v1.0.3

#### Bugs Fixed

* ANDROID-130 Lower part of message center is transparent.

# 2013-06-28 - v1.0.2

#### Major changes

* Replaced Feedback with new Message Center. Message Center allows two way communication between the app developer and customers, and the implementation is 100% native, and requires no active network. App users can send messages and attach screenshots, and receive a reply in-app. If they choose to specify an email address, they can also be contacted through email.

# 2013-04-10 - v0.6.5

#### Bugs fixed

* Fix XML examples in README.
* ANDROID-82 Populate the feedback source field with "enjoyment_dialog" when launched from the ratings prompt

# 2013-12-19 - v0.6.4

#### Bugs fixed

* ANDROID-114 Rating Dialog comes up twice.

# 2012-12-18 - v0.6.3

#### Bugs fixed

* ANDROID-112 Bug in rating flow.
* ANDROID-113 Bug in rating flow.

# 2012-12-14 - v0.6.2

#### Bugs fixed

* ANDROID-109 Google Play opens up on top of host application when rating app
* ANDROID-110 Ratings prompt doesn't respect zero value settings.

# 2012-10-10 - v0.6.1

#### Major changes

* App starts are now detected by looking for a 10 second gap where no App Activities are running. No major changes

#### Bugs fixed

* ANDROID-95 Come up with a new way to detect app starts and stops
* ANDROID-96 Crash on pre API 5 phones

# 2012-10-10 - v0.6.0

#### Major Changes

* Added README.md
* Testing work

#### Bugs fixed

* ANDROID-95 Move to a queue and timer based solution instead of an event based one. Tear out the old stuff.
* ANDROID-96 Fix two bugs. Add an android for a resource that needed it, and allow pre API level 5 devices to work.
* ANDROID-97 Starting apps takes 30 seconds when server is slow and host app uses AsyncTask. Solution is to simply stop using AsyncTasks altogether.

# 2012-09-13 - v0.5.2

#### Bugs fixed

* ANDROID-84 Remove checked in API keys from the Android repository
* ANDROID-85 Example app has API key in the resources file, but also requires updating the manifest
* ANDROID-89 Add callback after survey completion

# 2012-09-12 - v0.5.1

#### Major changes

* There was a problem with how we initialize our SDK that could lead to NPEs if the main Activity had been garbage collected, and interfered with unit testing individual child Activities. Fixed that.
* Fixed a problem with how we check for app uses. Now we keep track of each Activity instead of the Application as a whole.

#### Bugs fixed

* ANDROID-83: App crashes when no permissions are set for the app
* ANDROID-88: Android initialization needs rework

# 2012-08-15 - v0.5.0

#### Major changes

* Updated what counts as a "use". Before, we were incrementing uses on app launch, defined as a call to the main Activity's onCreate() method. That is not very useful, since hitting home screen backgrounds the app, but won't increment uses upon return. So instead, we figure out when the app is put into the background, and when it comes back.
* Updated app integration process. Instead of making a bunch of Apptentive API calls, you can now inherit from our ApptentiveActivity or ApptentiveListActivity. These classes allow you to save time integrating. You can also delegate your Activity's event handlers into Apptentive manually if you can't inherit from us.
* Redid the example apps. The previous "Demo" app was not really a demo, but a testing app. Renamed it accordingly. Also added two new "Example" apps: one using inheritance for integration, and the other using delegation.


#### Bugs fixed

* ANDROID-79: Add Changelog
* ANDROID-78: Externalize strings
* ANDROID-77: Send UUID with survey response - Android
* ANDROID-76: Check out using a res bundle for apptentive configuration.
* ANDROID-74: Responses to multichoice questions are being sent as an array
* ANDROID-66: When tapping on a multiple choice question, if the keyboard is up, hide it
* ANDROID-65: App config should be cached unless debugging
* ANDROID-63: Add check for min_selections
* ANDROID-61: If an API key isn't specified, return a better error message
* ANDROID-60: Survey answer options that are more than one line don't display well
* ANDROID-56: Send survey time to completion with responses
* ANDROID-55: Disable Ratings Flow when ratings_enabled preference is false
* ANDROID-35: Don't start rating flow when no data connection is present
