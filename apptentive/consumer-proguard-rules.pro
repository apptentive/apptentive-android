# include in this file rules Apptentive want applied to a
# consumer of this library when the consumer proguards itself.

# Marshmallow removed Notification.setLatestEventInfo()
-dontwarn android.app.Notification
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn android.support.v4.app.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-dontwarn com.google.android.gms.**
-keep public class com.google.android.gms.**

-dontwarn com.apptentive.android.sdk.**
-keepattributes SourceFile,LineNumberTable
-keep class com.apptentive.android.sdk.** { *; }