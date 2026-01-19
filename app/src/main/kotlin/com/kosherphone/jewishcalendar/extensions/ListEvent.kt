package com.kosherphone.jewishcalendar.extensions

import com.kosherphone.jewishcalendar.models.ListEvent

fun ListEvent.shouldStrikeThrough() = isTaskCompleted || isAttendeeInviteDeclined || isEventCanceled
