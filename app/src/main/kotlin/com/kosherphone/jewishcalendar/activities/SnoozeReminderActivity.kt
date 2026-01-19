package com.kosherphone.jewishcalendar.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kosherphone.jewishcalendar.extensions.config
import com.kosherphone.jewishcalendar.extensions.eventsDB
import com.kosherphone.jewishcalendar.extensions.rescheduleReminder
import com.kosherphone.jewishcalendar.helpers.EVENT_ID
import org.fossify.commons.extensions.showPickSecondsDialogHelper
import org.fossify.commons.helpers.ensureBackgroundThread

class SnoozeReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showPickSecondsDialogHelper(config.snoozeTime, true, cancelCallback = { dialogCancelled() }) {
            ensureBackgroundThread {
                val eventId = intent.getLongExtra(EVENT_ID, 0L)
                val event = eventsDB.getEventOrTaskWithId(eventId)
                config.snoozeTime = it / 60
                rescheduleReminder(event, it / 60)
                runOnUiThread {
                    finishActivity()
                }
            }
        }
    }

    private fun dialogCancelled() {
        finishActivity()
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(0, 0)
    }
}
