package com.kosherphone.jewishcalendar.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.kosherphone.jewishcalendar.adapters.EventListWidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapter(applicationContext, intent)
}
