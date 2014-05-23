# Migrating to 1.4.0

If you have integrated a previous version of Apptentive's ratings, you will need to migrate to our new
**Interaction** and **Event** based API. The new API provides a generic interface through which all future
**Interactions** will be displayed.

## Ratings

### Displaying Ratings

The previous method, `Apptentive.showRatingFlowIfConditionsAreMet(Activity activity)`, has been removed. You will need to
replace it with a call to `Apptentive.engage(Activity activity, String eventName)`. The parameter `eventName` is a
string that you will define that identifies the place in the code that you are calling `engage()` from.

For instance, if you were showing ratings when the your main activity gains focus, you could call
`Apptentive.engage(activity, "main_activity_focused")`. By default, the new **Ratings Prompt Interaction** is targeted
to an **Event** called "init".

**Note:** By default, the new Ratings Prompt will be triggered by the `init` **Event**. You can trigger this **Event**
with `Apptentive.engage(activity, "init")`.

### Logging Significant Events

If you were previously using `Apptentive.logSignificantEvent(Context context)`, you will need to instead use the
`engage()` method as described above. For instance, if you were logging a significant event when your customer uploaded
a file, you can replace that call with `Apptentive.engage(activity, "customer_uploaded_file")`. This allows your
**Enteractions** to depend on multiple **Events** instead of just one.

### Setting things up on the server

Any previous Ratings configuration settings on the server have been migrated to use the new system. Old SDKs will
continue to work for previous client versions. In order to use the new system, you will need go to
*Interactions -> Ratings Prompt*, and then select *Who &amp; When*. You will notice that these settings are almost
identical to the previous settings. However, you will need to select the name of the **Event** which should display the
Ratings Prompt under *When will this be shown?*. If you are targeting significant events, you will also need to enable
this in the *The ratings prompt will be displayed if:* section.

If you are trying to configure the Ratings Prompt with **Events**, and you have never sent an **Event** to the server,
you can manually add them so that they can be used in the Ratings Prompt Configuration. Simply go to *Interactions ->
Events*, and enter the new **Events**.

**Note:** The old Ratings Prompt settings required you to use a value of zero to turn off each piece of the ratings
logic. The new Ratings Prompt logic will treat a zero value literally. To turn it off, simply uncheck the appropriate
field.

##### Server Configuration

![Using Custom Events](https://raw.githubusercontent.com/apptentive/apptentive-android/master/etc/screenshots/ratings_prompt_interaction_config.png)

## Upgrade Messages

Prevously to 1.4.0, Upgrade Messages were displayed using a built in Apptentive **Event** that was triggered on App
Launch. Starting with 1.4.0, Upgrade Messages will be targeted to an **Event** that you must trigger, called `init`.
This is because you know best what constitutes the launch of your app. Simply call `Apptentie.engage(activity, "init")`.