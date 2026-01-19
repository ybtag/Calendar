package com.kosherphone.jewishcalendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.kosherphone.jewishcalendar.extensions.eventsDB
import com.kosherphone.jewishcalendar.extensions.notifyEvent
import com.kosherphone.jewishcalendar.extensions.scheduleNextEventReminder
import com.kosherphone.jewishcalendar.extensions.updateListWidget
import com.kosherphone.jewishcalendar.helpers.EVENT_ID
import com.kosherphone.jewishcalendar.helpers.Formatter
import com.kosherphone.jewishcalendar.helpers.REMINDER_NOTIFICATION
import org.fossify.commons.helpers.ensureBackgroundThread

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "simplecalendar:notificationreceiver")
        wakelock.acquire(3000)

        ensureBackgroundThread {
            handleIntent(context, intent)
        }
    }

    private fun handleIntent(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EVENT_ID, -1L)
        if (id != -1L) {
            context.updateListWidget()
            val event = context.eventsDB.getEventOrTaskWithId(id)
            if (
                event == null
                || event.getReminders().none { it.type == REMINDER_NOTIFICATION }
                || event.repetitionExceptions.contains(Formatter.getTodayCode())
            ) {
                return
            }

            if (event.isAttendeeInviteDeclined()) {
                return
            }

            if (!event.repetitionExceptions.contains(Formatter.getDayCodeFromTS(event.startTS))) {
                context.notifyEvent(event)
            }
            context.scheduleNextEventReminder(event, false)
        }
    }
}
