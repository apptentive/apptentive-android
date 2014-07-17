# 2014-07-16 skykelsey 1.5.1

#### Major changes

* Simplified Apptentive's native UI styling. Common things like buttons in our UI now use the same styles, so changes can easily be made to all similar elements at once.
* Added translations for French Canadian (fr-rCA).
* Added Apache Ant build scripts for the SDK, dev app, and tests.
* Added support to hide Apptentive branding for enterprise accounts.

#### Bugs fixed:

* Fixed a rare crash that can occur when a user's database is stored on an SD card and that card is removed.
* Fixed a potential memory leak.
* Fixed default text in the Enjoyment Dialog portion of the Ratings Prompt.
* Removed some benign lint warnings.
* Removed unused resources.
* Fixed a bug that could result in a delay when sending data to the server.
* During development only, if you forget to specify an API key, we will display a dialog to let you know. Previously we only logged it.


# 2014-05-27 skykelsey 1.5.0

#### Major changes

We've refactored [Surveys](http://www.apptentive.com/docs/android/features/#surveys) to use our [Event Framework](http://www.apptentive.com/docs/android/features/#events-and-interactions)! You will now have the ability to show surveys at any Event in your code. This release also lets you show a Survey instead of asking for feedback if your customer says they don't love your app from the [Ratings Prompt](http://www.apptentive.com/docs/android/features/#ratings-prompt) Interaction.

See [Migrating to 1.5.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_t_1.5.0.md)

#### Bugs fixed:

* Fixed a potential crash in the Feedback Dialog if submitted with blank feedback.

# 2014-04-28 skykelsey 1.4.3

#### Bugs fixed

* ANDROID-323 Localized strings not properly populated with app name.

# 2014-04-18 skykelsey 1.4.2

#### Major changes

* Added support for Amason Web Services (AWS) Simple Notification Service (SNS) push notifications. If you use AWS SNS, you can now notify your customers when you reply to their conversation.

#### Bugs fixed

* Improved robustness of message polling.

# 2014-04-14 skykelsey 1.4.1

#### Bugs fixed

* ANDROID-318 Surveys targeting custom person data don't return until the second time they are requested.
* Event Labels were being encoded incorrectly.

# 2014-04-07 skykelsey 1.4.0

#### Major changes

* Added new Engagement method. This method will triggere events, and show interactions to the user. Interactions can be configured on the server, and can be based on the count and time that events are shown.
* Moved the existing Rating feature to be an Interaction in this new system. Removed old methods.
* Lots of bug fixes and UI tweaks.
* Introduced background polling for messages. We will poll for new messages every 60 seconds whie the app is up, or every 8 seconds while our Message Center is in the foreground.
* Improved performance and robustness of SDK.

# 2014-04-03 skykelsey 1.3.1

#### Major changes

* Add setting to allow Message Center to be disabled by default, even before it has connected to the server.

# 2014-02-24 skykelsey 1.3.0

#### Major changes

* Added support for push notifications! If you are using a third party push notification provider, you can configure our server to send a push on your behalf when you reply to a customer. We currently support Urban Airship, but if you are using another provider, please let us know and we will add support.
* Added the ability to send Upgrade Messages to your users. Now you can notify users of new features targeted to specific builds or versions of your app, even if they have opted to auto-update your app in the app store.
* Added the ability to send hidden attachments to the server. You can now send texts, logs, images, and other files to the server to help provide better support. These files are visible to you in teh conversation view, but are not shown to the end user.

# 2014-01-20 skykelsey 1.2.7

#### Bugs fixed

* ANDROID-239 PACKAGE_RESTARTED permission causes app to wake often

# 2014-01-19 skykelsey 1.2.6

#### Bugs fixed

* ANDROID-238 Survey module doesn't respect "multiple_responses" flag

# 2013-12-17 skykelsey  1.2.5

#### Major changes

* Ability to pass Apptentive custom integration details. For instance, you can pass in the app's Urban Airship App ID, allowing us to send push notifications to your device.
* Add the ability to pass push notification intents into Apptentive code, and have Apptentive perform actions. Push integration currently supports Urban Airship, and can be configured in your app settings.
* ANDROID-224 Allow survey thank you dialog to contain links.

# 2013-12-12 skykelsey 1.2.4

#### Major changes

Slightly modified the behavior of the rating flow. The user can now close the rating flow by pressing the back button. This should create a less intrusive experience for the people using your app.

#### Bugs fixed

* ANDROID-201 Survey targeting doesn't work until after survey cache expires.
* ANDROID-223 Trim Survey text responses
* Fixed a bug where the rating flow could launch twice, especially on devices running KitKat.

# 2013-11-13 skykelsey 1.2.3

#### Major changes

* Add method to API. New way of opening Message Center allowing you to include custom data to be sent with the next message the user sends while the message center is open.
* Implemented an internal error trapping and reporting system. Exceptions in Apptentive code are now caught and logged to our server so we can maintain a reliable app experience and proactively fix hard to reproduce bugs.

#### Bugs fixed

* Fixed a potential crash in survey fetching code.

# 2013-11-05 skykelsey 1.2.2

#### Bugs fixed

* Fix memory leak cause by hanging on to Activity context in database code.

# 2013-10-21 skykelsey 1.2.1

#### Bugs fixed

* ANDROID-188 Rating logic doesn't respect 0 values with OR

# 2013-10-17 skykelsey 1.2.0

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

# 2013-09-15 skykelsey 1.1.2

#### Bugs fixed

* ANDROID-155 Message Center can't scroll

# 2013-08-31 skykelsey 1.1.1

#### Bugs fixed

* ANDROID-141 Simplified Chinese strings file contains Traditional Chinese
* ANDROID-142 Save the text entered in Message Center if the user closes it.
* ANDROID-145 Dev app can crash in tablet portrait mode.

# 2013-08-29 skykelsey 1.1.0

#### Major changes

* Support new enterprise survey features: tagged surveys, rate limiting, delivery capping, and time range constraining.
* New clean UI fro survey display.
* New clean UI for about page.
* Added ability to set the display name of this app from the web UI. The display name is used to surface a different name than the one specified in your package, and is handled transparently from the client's perspective.

# 2013-08-16 skykelsey 1.0.4

#### Major changes

* First release of a localized SDK. See README for supported languages.

#### Bugs Fixed

* ANDROID-131 Survey clipping background.
* ANDROID-132 Radio Version is not being set in Device object
* ANDROID-134 Rating logic does not respect 0 values

# 2013-07-19 skykelsey 1.0.3

#### Bugs Fixed

* ANDROID-130 Lower part of message center is transparent.

# 2013-06-28 skykelsey 1.0.2

#### Major changes

* Replaced Feedback with new Message Center. Message Center allows two way communication between the app developer and customers, and the implementation is 100% native, and requires no active network. App users can send messages and attach screenshots, and receive a reply in-app. If they choose to specify an email address, they can also be contacted through email.

# 2013-04-10 skykelsey v0.6.5

#### Bugs fixed

* Fix XML examples in README.
* ANDROID-82 Populate the feedback source field with "enjoyment_dialog" when launched from the ratings prompt

# 2013-12-19 skykelsey v0.6.4

#### Bugs fixed

* ANDROID-114 Rating Dialog comes up twice.

# 2012-12-18 skykelsey v0.6.3

#### Bugs fixed

* ANDROID-112 Bug in rating flow.
* ANDROID-113 Bug in rating flow.

# 2012-12-14 skykelsey v0.6.2

#### Bugs fixed

* ANDROID-109 Google Play opens up on top of host application when rating app
* ANDROID-110 Ratings prompt doesn't respect zero value settings.

# 2012-10-10 skykelsey v0.6.1

#### Major changes

* App starts are now detected by looking for a 10 second gap where no App Activities are running. No major changes

#### Bugs fixed

* ANDROID-95 Come up with a new way to detect app starts and stops
* ANDROID-96 Crash on pre API 5 phones

# 2012-10-10 skykelsey v0.6.0

#### Major Changes

* Added README.md
* Testing work

#### Bugs fixed

* ANDROID-95 Move to a queue and timer based solution instead of an event based one. Tear out the old stuff.
* ANDROID-96 Fix two bugs. Add an android for a resource that needed it, and allow pre API level 5 devices to work.
* ANDROID-97 Starting apps takes 30 seconds when server is slow and host app uses AsyncTask. Solution is to simply stop using AsyncTasks altogether.

# 2012-09-13 skykelsey v0.5.2

#### Bugs fixed

* ANDROID-84 Remove checked in API keys from the Android repository
* ANDROID-85 Example app has API key in the resources file, but also requires updating the manifest
* ANDROID-89 Add callback after survey completion

# 2012-09-12 skykelsey v0.5.1

#### Major changes

* There was a problem with how we initialize our SDK that could lead to NPEs if the main Activity had been garbage collected, and interfered with unit testing individual child Activities. Fixed that.
* Fixed a problem with how we check for app uses. Now we keep track of each Activity instead of the Application as a whole.

#### Bugs fixed

* ANDROID-83: App crashes when no permissions are set for the app
* ANDROID-88: Android initialization needs rework

# 2012-08-15 skykelsey v0.5.0

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
