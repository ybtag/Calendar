package com.kosherphone.jewishcalendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kosherphone.jewishcalendar.extensions.config
import com.kosherphone.jewishcalendar.extensions.recheckCalDAVCalendars
import com.kosherphone.jewishcalendar.extensions.refreshCalDAVCalendars
import com.kosherphone.jewishcalendar.extensions.updateWidgets

class CalDAVSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context.config.caldavSync) {
            context.refreshCalDAVCalendars(context.config.caldavSyncedCalendarIds, false)
        }

        context.recheckCalDAVCalendars(true) {
            context.updateWidgets()
        }
    }
}
