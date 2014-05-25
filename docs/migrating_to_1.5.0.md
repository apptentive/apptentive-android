# Migrating to 1.5.0

This release refactors our [Surveys](http://www.apptentive.com/docs/android/features/#surveys) to use our new [Event & Interaction](http://www.apptentive.com/docs/android/features/#events-and-interactions) framework. If you are using a previous version of our SDK, and utilizing **Surveys**, you will need to perform the following migration. If you are not already using **Surveys**, you can ignore this document.

**Note:** If you were using a version of our SDK prior to 1.4.0, and you were using [Ratings](http://www.apptentive.com/docs/android/features/#surveys), you will also need to read [Migrating to 1.4.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.4.0.md).

## Interactions

[Interactions](http://www.apptentive.com/docs/android/features/#interactions) are views that can be displayed to a person using your app. **Interactions** include **Surveys**, the **Ratings Prompt**, and **Upgrade Messages**, with more being added all the time. **Interactions** are triggered using the method [boolean Apptentive.engage(Activity activity, String eventName)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29). When you call this method, you will need to pass in an **Event Name** of your choosing. On the server, you can then configure an **Interaction** to run when this **Event** is hit.


### Surveys

[Setting up Surveys](http://www.apptentive.com/docs/android/integration/#surveys)

### Displaying a Survey

The previous method, `boolean Apptentive.showSurvey(Activity activity, String... tags)` has been removed. You will need to replace it with a call to [Apptentive.engage(Activity activity, String eventName)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#engage%28android.app.Activity,%20java.lang.String%29). The parameter `eventName` is a string that you will define that identifies the place in the code that you are calling `engage()` from.

### Checking to See if a Survey Can be Shown

Note that `boolean Apptentive.isSurveyAvailable(Context context, String... tags)` has also been removed. You can acheive the same result by checking the return value of `engage()`, which will return true if an interaction was shown.

### Survey Finished Listener

The new `engage()` method doesn't take a listener like the old `showSurvey()` method did. Instead, you can register a listener with [Apptentive.setOnSurveyFinishedListener(OnSurveyFinishedListener listener)](http://www.apptentive.com/docs/android/api/com/apptentive/android/sdk/Apptentive.html#setOnSurveyFinishedListener%28com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener%29).

### Setting things up on the server

Any previous Survey configuration settings on the server have been migrated to use the new system. Old SDKs will continue to work for previous client versions. In order to use the new system, you will need go to *Interactions -> Surveys*, and then select *Who &amp; When*. You will notice that these settings are almost identical to the previous settings. However, you will need to select the name of the **Event** which should display the **Survey** under _When will this be shown?_.

If you are trying to configure a Survey with **Events**, and you have never sent an **Event** to the server, you can manually add them so that they can be used in the **Survey** Configuration. Simply go to *Interactions -> Events*, and enter the new **Event**. If you don't add them manually, they will also be available once your test client sends them to the server.

### Showing a Survey in the Ratings Prompt

This release also adds the ability to show a survey when your customer chooses "No" when asked whether they love your app in the **Ratings Prompt Interaction**. To enable this behavior, you will need to create a survey, then go to **_Interactions -> Ratings Prompt_**. Under the tab _The Prompt_, change the dropdown near the bottom from "asked to submit feedback…" to "taken to a survey…". You can then choose which survey to show. Note that any limiting or targeting criteria you have specified for the survey will be ignored if it is invoked in this manner.
