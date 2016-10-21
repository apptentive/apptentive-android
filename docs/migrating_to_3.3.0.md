# Migrating to 3.3.0

Version 3.3.0 of the Apptentive Android SDK has a redesigned set of APIs for receiving and displaying push notifications from Apptentive. If you did not use Apptentive push notifications using a previous version of our SDK, you should skip this migration guide.

## Major changes

### Easier Integration

* We've built simpler methods for opening a push notification that is sent from Apptentive.
* Instead of saving a push notification, opening your `MainActivity`, and then launching Apptentive from the saved push, we provide you a `PendingIntent` right when the push notification is received. You then create a `Notification` object to display to your customers, and set that `PendingIntent` into the `Notification`. When the `Notification` is opened, it will automatically launch the appropriate Apptentive Interaction.

**Note:** The changes to push require us to send the push notification in a format that is not compatible with versions of our SDK older than 3.3.0. Please make sure you migrate to these new methods when you upgrad to 3.3.0.

## How to migrate

The migration process should take 5-10 minutes to complete.

1. Remove any calls to `Apptentive.setPendingPushNotification()`.
2. Remove any calls to `Apptentive.handleOpenedPushNotification()`.
3. Follow the updated [Push Notification](https://learn.apptentive.com/knowledge-base/android-integration/#5-push-notifications) integration instructions for your push provider.
4. There is no change to the process of setting the push token using `Apptentive.setPushNotificationIntegration()`.

