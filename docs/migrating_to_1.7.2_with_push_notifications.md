# Migrating to 1.7.2 with Push Notifications

If you are using a previous version of Apptentive with your push notification provider to send push notifications to your customers when you reply to their feedback, please read the following migration guide.

## Parse

Parse's API has changed in their recent versions. If you are using Parse, our integration API has been significantly improved and simplified. Some older methods were removed, as they are no longer supported in new versions of the Parse SDK. Please refer to the [Parse integration instructions](https://www.apptentive.com/docs/android/integration/#using-parse).

## Urban Airship

The API for Urban Airship needed to change slightly in order to support the latest Urban Airship SDK. Please refer to the 
[Urban Airship integration instructions](https://www.apptentive.com/docs/android/integration/#using-urban-airship).

## Amazon SNS

If you are using Amazon SNS for push notifications, there are no changes necessary.
