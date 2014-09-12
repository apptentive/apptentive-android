# Migrating to 1.6.0

This release refactors our repo to be more Gradle friendly, cleans up our samples, adds Gradle support, and adds several new features.

**Note:** If you were using an old version, you may also need to read [Migrating to 1.4.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.4.0.md) and [Migrating to 1.5.0](https://github.com/apptentive/apptentive-android/blob/master/docs/migrating_to_1.5.0.md).



### Project Structure

Our new project structure renames the Apptentive SDK directory from `apptentive-android-sdk` to simply `apptentive`. If you were priviously referencing `apptentive-android-sdk` in your Eclipse or IntelliJ IDEA project, you should remove that reference and follow the instructions to add it back in [here](http://www.apptentive.com/docs/android/integration/#setting-up-the-project).

In addition, we have added support for Gradle in this release. While we do not yet upload an AAR to a Maven repo (this is coming soon!), you can now use Apptentive in your gradle projects with minimal extra work. Please see instructions [here](http://www.apptentive.com/docs/android/integration/#using-gradle).
