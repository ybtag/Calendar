package com.kosherphone.jewishcalendar.extensions

import com.kosherphone.jewishcalendar.models.MonthViewEvent

fun MonthViewEvent.shouldStrikeThrough() = isTaskCompleted || isAttendeeInviteDeclined || isEventCanceled
