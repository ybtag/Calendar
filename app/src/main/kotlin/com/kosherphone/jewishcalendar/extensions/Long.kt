package com.kosherphone.jewishcalendar.extensions

import com.kosherphone.jewishcalendar.helpers.Formatter
import com.kosherphone.jewishcalendar.models.Event

fun Long.isTsOnProperDay(event: Event): Boolean {
    val dateTime = Formatter.getDateTimeFromTS(this)
    val power = 1 shl (dateTime.dayOfWeek - 1)
    return event.repeatRule and power != 0
}
