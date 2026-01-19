package com.kosherphone.jewishcalendar

import com.kosherphone.jewishcalendar.extensions.hasDummyAlarm
import com.kosherphone.jewishcalendar.jobs.AppStartupWorker
import org.fossify.commons.FossifyApp

class App : FossifyApp() {
    override fun onCreate() {
        super.onCreate()
        if (!hasDummyAlarm()) {
            AppStartupWorker.start(this)
        }
    }
}
