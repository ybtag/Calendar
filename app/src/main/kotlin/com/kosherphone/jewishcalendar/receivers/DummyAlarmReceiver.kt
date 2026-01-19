package com.kosherphone.jewishcalendar.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kosherphone.jewishcalendar.extensions.scheduleDummyAlarm

class DummyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.scheduleDummyAlarm()
    }
}
