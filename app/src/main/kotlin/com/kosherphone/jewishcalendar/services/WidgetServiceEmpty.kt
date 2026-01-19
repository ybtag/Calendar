package com.kosherphone.jewishcalendar.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.kosherphone.jewishcalendar.adapters.EventListWidgetAdapterEmpty

class WidgetServiceEmpty : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapterEmpty(applicationContext)
}
