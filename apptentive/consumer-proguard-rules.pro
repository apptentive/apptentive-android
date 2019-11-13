# include in this file rules Apptentive want applied to a
# consumer of this library when the consumer proguards itself.

# Apptentive also depends on Android support libraries. If host app needs to keep certain Android support libraries,
# for instance to work around https://code.google.com/p/android/issues/detail?id=78293, those rules need to be
# added to app's proguard file as needed.

-dontwarn com.apptentive.android.sdk.**
-keepattributes SourceFile,LineNumberTable
-keepnames class com.apptentive.android.sdk.** { *; }

-keep class * implements com.apptentive.android.sdk.serialization.SerializableObject { *; }
-keep class com.apptentive.android.sdk.** implements java.io.Serializable { *; }


-keep class android.os.Build { *; }
-keep class android.graphics.Typeface { *; }
-keep class androidx.fragment.app.Fragment { *; }
-keep class androidx.fragment.app.FragmentManagerImpl { *; }
