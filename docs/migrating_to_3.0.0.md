# Migrating to 3.0

Version 3.0 of the Apptentive Android SDK has extensive improvements to ease integration, improve performance and reliability, and seamlessly consume your app's styles

## Major changes

1. Minimum SDK Version: **14**

    Dropping support for ancient versions of Android ensures the Apptentive Android SDK uses that latest tools and patterns to make a great and robust experience.

2. Easier Integration

    * We've leveraged `ActivityLifecycleCallbacks`, which means you no longer need to add Apptentive into all of your Activities. Instead, add a single line of code to your `Application.onCreate()`.
    * We now use the Gradle manifest merger so that you don't need to add XML to your `AndroidManifest.xml`.
    * Gradle automatically downloads AAR transitive dependencies, so you only need to add a dependency to the Apptentive Android SDK to integrate.
    
3. Styling

    * The SDK now inherits all styles from apps that are using an AppCompat or Material theme. This means that right out of the box, the UI of our SDK will look a lot more like your app, including colors and fonts. We also provide a way to further override our UI styles in XML.
    
    
## How to migrate

Our SDK API has changed so you will need to modify your integration. Rest assured that this is a straight forward process that shouldn't take more than 20 minutes.

1. Modify your `build.gradle` 
    1. In your `build.gradle`, add a dependency to Apptentive with `compile 'com.apptentive:apptentive-android:3.0.0'`.
    2. make sure that this dependency no longer ends in `@aar`, which will prevent gradle from downloading our transitive dependencies.
    3. You can remove any transitive dependencies that you added to support Apptentive. These will be imported by gradle for you.
2. Modify your `AndroidManifest.xml`
    1. Remove everything Apptentive related. This includes permissions, Activity declarations, and the API key `<meta-data.` tag. The new integration doesn't require modifying your manifest by default.
    2. If you added a `<meta-data>` tag for overriding our log level, you can leave this in.
3. In each Activity in your app, remove references to `ApptentiveActivity`, `Apptentive.onStart()`, and `Apptentive.onStop()`. These are no longer necessary, and have been removed from our API to ensure your integration is correct.
4. In your `Application.onCreate()`, add a call to `Apptentive.register()`. This is where you will pass in your Apptentive API Key.
5. Various other Apptentive API method signatures have changed.
    1. Any method that formerly required an `Activity` now requires a `Context`. That means that you can call them in cases where you don't have access to an Activity, such as from a `BroadcastReceiver` or `Service`, and we will still be able to display our UI.
    2. Any method that formerly required a `Context` now does not require one. Integrating from the Application guarantees we will have access to your Application Context, so we don't need you to pass it in anymore.
    
## Styling the SDK
If you have specified an AppCompat or Material theme in your `AndroidManifest.xml`, our SDK inherits its look and feel from your theme. If you are using a legacy theme, such as Holo, then our SDK will not inherit your theme, and will retain our default red on white styling by default. In both cases, you can override our styles.

Please see our [style guide]() for more information.


