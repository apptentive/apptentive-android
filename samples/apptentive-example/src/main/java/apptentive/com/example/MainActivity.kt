package apptentive.com.example

import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import com.apptentive.android.sdk.Apptentive
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_love_dialog.setOnClickListener { engage("love_dialog") }
        button_survey.setOnClickListener { engage("survey") }
        button_note.setOnClickListener { engage("note") }
        button_upgrade_message.setOnClickListener { engage("upgrade_message") }
        button_message_center.setOnClickListener { Apptentive.showMessageCenter(this) }
    }

    private fun engage(event: String) {
        Apptentive.engage(this, event) { engaged ->
            if (!engaged) {
                Toast.makeText(this, "Event not engaged '$event'", LENGTH_LONG).show()
            }
        }
    }
}
