package com.kosherphone.jewishcalendar.services

import android.app.IntentService
import android.content.Intent
import com.kosherphone.jewishcalendar.extensions.config
import com.kosherphone.jewishcalendar.extensions.eventsDB
import com.kosherphone.jewishcalendar.extensions.rescheduleReminder
import com.kosherphone.jewishcalendar.helpers.EVENT_ID

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val eventId = intent.getLongExtra(EVENT_ID, 0L)
            val event = eventsDB.getEventOrTaskWithId(eventId)
            rescheduleReminder(event, config.snoozeTime)
        }
    }
}
