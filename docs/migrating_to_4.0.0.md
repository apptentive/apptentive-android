# Migrating to 4.0.0

Version 4.0.0 of the Apptentive Android SDK has new APIs for customer login, as well as some changed APIs for SDK registration. If you were using any previous version of the Apptentive SDK in your app, you will need to follow one or more of the applicable steps below.

## Major changes

### Registration Has Changed

* Registration now requires two pieces of information instead of one. Where before you needed to copy your API Key from the Apptentive website, now you need to copy the Apptentive App Key and Apptentive App Signature. You can find both of those in the [API & Development](https://be.apptentive.com/apps/current/settings/api) page.

### Push Notification Changes

* If you are not using the new Customer Login feature, you don't strictly have to follow this step, but it is a good idea in case you add it in the future.
* When you reply to your customers from the Apptentive website, we will send a push, if configured. If the customer for which the push is intended is not logged in, then the push should not be displayed. This small change means that you need to check whether the push is and Apptentive push, or should be handled by your existing push code, and then check where it can be displayed. See the [Push Notifications](https://learn.apptentive.com/knowledge-base/android-integration-reference/#push-notifications) documentation for code that demonstrates this change for the push provider you are using.

## How to migrate

The migration process should take 5-10 minutes to complete.

1. Replace the call to `Apptentive.register(Application yourApp, String apiKey)` with a call to `Apptentive.register(Application yourApp, String apptentiveKey, String apptentiveSignature)`.
2. If you were calling `Apptentive.register(Application yourApp)`, and specifying the API Key in your manifest, you can continue to do so in version 4.0.0, you will just need to remove the old manifest element and add two new elements for the Apptentive App Key and Apptentive App Signature.

   Remove:

   `<meta-data android:name="apptentive_api_key" android:value="<YOUR_API_KEY>">`

   Add:

   `<meta-data android:name="apptentive_key" android:value="<YOUR_APPTENTIVE_APP_KEY>">`

   `<meta-data android:name="apptentive_signature" android:value="<YOUR_APPTENTIVE_APP_SIGNATURE>">`

3. Make sure to check whether a push came from Apptentive, and handle it yourself if `Apptentive.isApptentivePushNotification()` returns `false`.

