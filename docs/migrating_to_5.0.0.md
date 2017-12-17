# Migrating to 5.0.0

Version 5.0 of our SDK has changed many of our synchronous API methods to be asynchronous. This will allow your app to perform better when using Apptentive.

You will need to follow this if:

    You were previously using a version of our SDk prior to 5.0, and you are:
        * Using the return value of the Apptentive.engage() methods
        * Using Apptentive.canShowInteraction()
        * Using Apptentive push notifications

## What changed?

In 5.0, we have made much of our API asynchronous to avoid slowing down your app. For most of our methods, you won’t have to change anything in your app. But for some, where we return a value to you after performing a potentially time-consuming operation, we now return the value in a callback. And that requires an API change. Methods that have changed will now return void, and give you back a value through a callback when the operation has completed.

Please see our [API Changes](APIChanges.md) document for a complete list of method changes.