# Migrating to 1.5.0

This release refactors our **Surveys** to use our new **Interaction** and **Event** framework. If you are using a previous
version of our SDK, and utilizing our Survey product, you will need to perform the following migration. If you are not
already using surveys, you can ignore this document.

**Note:** If you were using a version of our SDK prior to 1.4.0, and you were using **Ratings**, you will also need to
read [Migrating to 1.4.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.4.0.md).

## Interactions

**Interactions** are Views that can be displayed to a person using your app. **Interactions** include **Surveys**,
**Ratings Prompts**, and **Upgrade Messages**, with more being added all the time. Interactions are triggered using the
method `boolean Apptentive.engage(Activity activity, String eventName)`. When you call this method, you will need to
pass in an **Event Name** of your choosing. On the server, you can then configure this **Interaction** to run when this
**Event** is hit. **Events** can also be used in the logic criteria that determines if and when an **Interaction** can
be shown. For instance, you can choose to show a **Survey Interaction** at the **Event** `main_activity_focused`, but
only if they have previously seen the **Event** `beat_level_2`.

### Surveys

### Displaying a Survey

The previous method, `boolean Apptentive.showSurvey(Activity activity, String... tags)` has been removed. You will need
to replace it with a call to `boolean Apptentive.engage(Activity activity, String eventName)`. The parameter `eventName`
is a string that you will define that identifies the place in the code that you are calling `engage()` from.

### Checking to See if a Survey Can be Shown

Note that `boolean Apptentive.isSurveyAvailable(Context context, String... tags)` has also been removed. You can acheive
the same result by checking the return value of `engage()`, which will return true if an interaction was shown.

### Setting things up on the server


TODO...


Any previous Survey configuration settings on the server have been migrated to use the new system. Old SDKs will
continue to work for previous client versions. In order to use the new system, you will need go to *Interactions -> Surveys*,
and then select *Who &amp; When*. You will notice that these settings are almost identical to the previous settings.
However, you will need to select the name of the **Event** which should display the Ratings Prompt under *When will this
be shown?*. If you are targeting significant events, you will also need to enable event this in the *The ratings prompt
will be displayed if:* section.

If you are trying to configure the Ratings Prompt with **Events**, and you have never sent an **Event** to the server,
you can manually add them so that they can be used in the Ratings Prompt Configuration. Simply go to *Interactions ->
Events*, and enter the new **Events**.

**Note:** The old Ratings Prompt settings required you to use a value of zero to turn off each piece of the ratings
logic. The new Ratings Prompt logic will treat a zero value literally. To turn it off, simply uncheck the appropriate
field.

##### Server Configuration

![Using Custom Events](https://raw.githubusercontent.com/apptentive/apptentive-android/master/etc/screenshots/ratings_prompt_interaction_config.png)
