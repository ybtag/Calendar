package com.kosherphone.jewishcalendar.interfaces

import com.kosherphone.jewishcalendar.models.CalendarEntity

interface DeleteCalendarsListener {
    fun deleteCalendars(calendars: ArrayList<CalendarEntity>, deleteEvents: Boolean): Boolean
}
