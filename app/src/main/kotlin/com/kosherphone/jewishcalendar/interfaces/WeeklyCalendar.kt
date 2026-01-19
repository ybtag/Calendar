package com.kosherphone.jewishcalendar.interfaces

import com.kosherphone.jewishcalendar.models.Event

interface WeeklyCalendar {
    fun updateWeeklyCalendar(events: ArrayList<Event>)
}
