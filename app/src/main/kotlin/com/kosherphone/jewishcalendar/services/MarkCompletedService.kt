package com.kosherphone.jewishcalendar.services

import android.app.IntentService
import android.content.Intent
import com.kosherphone.jewishcalendar.extensions.eventsDB
import com.kosherphone.jewishcalendar.extensions.updateTaskCompletion
import com.kosherphone.jewishcalendar.extensions.updateWidgets
import com.kosherphone.jewishcalendar.helpers.ACTION_MARK_COMPLETED
import com.kosherphone.jewishcalendar.helpers.EVENT_ID
import com.kosherphone.jewishcalendar.helpers.EVENT_OCCURRENCE_TS

class MarkCompletedService : IntentService("MarkCompleted") {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.action == ACTION_MARK_COMPLETED) {
            val taskId = intent.getLongExtra(EVENT_ID, 0L)
            val task = eventsDB.getTaskWithId(taskId)?.apply {
                val occurrenceTS = intent.getLongExtra(EVENT_OCCURRENCE_TS, 0L)
                if (occurrenceTS != 0L) {
                    startTS = occurrenceTS
                    endTS = occurrenceTS
                }
            }

            if (task != null) {
                updateTaskCompletion(task, completed = true)
                updateWidgets()
            }
        }
    }
}
