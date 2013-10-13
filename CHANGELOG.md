2013-??-?? skykelsey next
--------------------------

Major changes:

* Send app's TargetSdkVersion with app_release, and OS's API Level with device.
* Use the server setting "Require Email" so that feedback can only be submitted if the user supplies a valid email.
* Get list of available Google account emails and provide them in an AutoCompleteTextView.
* Some minor tweaks to make the IntroDialog look great.
* Added new methods for adding and removing custom data set on the device and person objects. These methods can now be called anywhere in the app's lifecycle, and their values will be picked up the next time an activity starts.
* Added Swedish translation, updated translations for other languages.
* Restyled the screenshot confirmation dialog to match the other dialogs.

Bugs fixed:

* ANDROID-178 ConcurrentModificationException in survey code

2013-09-15 skykelsey 1.1.2
--------------------------

Bugs fixed:

* ANDROID-155 Message Center can't scroll

2013-08-31 skykelsey 1.1.1
--------------------------

Bugs fixed:

* ANDROID-141 Simplified Chinese strings file contains Traditional Chinese
* ANDROID-142 Save the text entered in Message Center if the user closes it.
* ANDROID-145 Dev app can crash in tablet portrait mode.

2013-08-29 skykelsey 1.1.0
--------------------------

Major changes:

* Support new enterprise survey features: tagged surveys, rate limiting, delivery capping, and time range constraining.
* New clean UI fro survey display.
* New clean UI for about page.
* Added ability to set the display name of this app from the web UI. The display name is used to surface a different name than the one specified in your package, and is handled transparently from the client's perspective.

2013-08-16 skykelsey 1.0.4
--------------------------

Major changes:

* First release of a localized SDK. See README for supported languages.

Bugs Fixed:

* ANDROID-131 Survey clipping background.
* ANDROID-132 Radio Version is not being set in Device object
* ANDROID-134 Rating logic does not respect 0 values

2013-07-19 skykelsey 1.0.3
--------------------------

Bugs Fixed:

* ANDROID-130 Lower part of message center is transparent.

2013-06-28 skykelsey 1.0.2
--------------------------

Major changes:

* Replaced Feedback with new Message Center. Message Center allows two way communication between the app developer and customers, and the implementation is 100% native, and requires no active network. App users can send messages and attach screenshots, and receive a reply in-app. If they choose to specify an email address, they can also be contacted through email.

2013-04-10 skykelsey v0.6.5
---------------------------

Bugs fixed:

* Fix XML examples in README.
* ANDROID-82 Populate the feedback source field with "enjoyment_dialog" when launched from the ratings prompt

2013-12-19 skykelsey v0.6.4
---------------------------

Bugs fixed:

* ANDROID-114 Rating Dialog comes up twice.

2012-12-18 skykelsey v0.6.3
---------------------------

Bugs fixed:

* ANDROID-112 Bug in rating flow.
* ANDROID-113 Bug in rating flow.

2012-12-14 skykelsey v0.6.2
---------------------------

Bugs fixed:

* ANDROID-109 Google Play opens up on top of host application when rating app
* ANDROID-110 Ratings prompt doesn't respect zero value settings.

2012-10-10 skykelsey v0.6.1
---------------------------

Major changes:

* App starts are now detected by looking for a 10 second gap where no App Activities are running. No major changes

Bugs fixed:

* ANDROID-95 Come up with a new way to detect app starts and stops
* ANDROID-96 Crash on pre API 5 phones

2012-10-10 skykelsey v0.6.0
---------------------------

* Added README.md
* Testing work

Bugs fixed:

* ANDROID-95 Move to a queue and timer based solution instead of an event based one. Tear out the old stuff.
* ANDROID-96 Fix two bugs. Add an android for a resource that needed it, and allow pre API level 5 devices to work.
* ANDROID-97 Starting apps takes 30 seconds when server is slow and host app uses AsyncTask. Solution is to simply stop using AsyncTasks altogether.

2012-09-13 skykelsey v0.5.2
---------------------------

Bugs fixed:

* ANDROID-84 Remove checked in API keys from the Android repository
* ANDROID-85 Example app has API key in the resources file, but also requires updating the manifest
* ANDROID-89 Add callback after survey completion

2012-09-12 skykelsey v0.5.1
---------------------------

Major changes:

* There was a problem with how we initialize our SDK that could lead to NPEs if the main Activity had been garbage collected, and interfered with unit testing individual child Activities. Fixed that.
* Fixed a problem with how we check for app uses. Now we keep track of each Activity instead of the Application as a whole.

Bugs fixed:

* ANDROID-83: App crashes when no permissions are set for the app
* ANDROID-88: Android initialization needs rework

2012-08-15 skykelsey v0.5.0
---------------------------

Major changes:

* Updated what counts as a "use". Before, we were incrementing uses on app launch, defined as a call to the main Activity's onCreate() method. That is not very useful, since hitting home screen backgrounds the app, but won't increment uses upon return. So instead, we figure out when the app is put into the background, and when it comes back.
* Updated app integration process. Instead of making a bunch of Apptentive API calls, you can now inherit from our ApptentiveActivity or ApptentiveListActivity. These classes allow you to save time integrating. You can also delegate your Activity's event handlers into Apptentive manually if you can't inherit from us.
* Redid the example apps. The previous "Demo" app was not really a demo, but a testing app. Renamed it accordingly. Also added two new "Example" apps: one using inheritance for integration, and the other using delegation.


Bugs fixed:

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
