# include in this file rules Apptentive want applied to a
# consumer of this library when the consumer proguards itself.

# Apptentive also depends on Android support libraries. If host app needs to keep certain Android support libraries,
# for instance to work around https://code.google.com/p/android/issues/detail?id=78293, those rules need to be
# added to app's proguard file as needed.

-dontwarn com.apptentive.android.sdk.**
-keepattributes SourceFile,LineNumberTable
-keep class com.apptentive.android.sdk.** { *; }
-keep class android.support.v4.app.FragmentManagerImpl { *; }