# Migrating to 2.0.0

Please see the latest [API Changes]().

## Message Center

Our new Message Center is completely redesigned from the ground up, and we've made it much more configurable by modifying settings on the Apptentive dashboard. Because of this, it may not be available for display immediately after install. We've changed the signature of `showMessageCenter()` to return a `boolean` to tell you whether we displayed Message Center. We've also added a new method `canShowMessageCenter()` that you can use to determine whether to add a "Feedback" button to your app's UI.

## Push Notifications

We have simplified our push notification integration by allowing your apps to receive push notifications directly from our server to your GCM enabled app. We kept support for using Parse, Urban Airship, and Amazon SNS as well, but streamlined the existing API methods.

If you were previously using push notifications sent from Apptentive in your app, see our [push integration instructions](https://www.apptentive.com/docs/android/integration/#push-notifications).

## Customer Information

We have also changed the way that you set your customer's name and email. The new methods `setPersonName()` and `setPersonEmail()` will immediately overwrite the value you or your customer have previously set, so you should only call them when you have a new value.

## Unread Messages Listener

We've modified how you pass in an `UnreadMessagesListener` when you want to be notified that the number of messages waiting to be read by your customer has changed. You should replace your calls to the deprecated `setUnreadMessagesListener()` with the new `addUnreadMessagesListener()`.