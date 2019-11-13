package apptentive.com.example

import android.app.Application
import com.apptentive.android.sdk.Apptentive
import com.apptentive.android.sdk.ApptentiveConfiguration
import com.apptentive.android.sdk.ApptentiveLog

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val configuration = ApptentiveConfiguration("", "")
        Apptentive.register(this, configuration)
    }
}