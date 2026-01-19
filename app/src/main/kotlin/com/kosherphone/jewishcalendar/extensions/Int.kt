package com.kosherphone.jewishcalendar.extensions

import com.kosherphone.jewishcalendar.helpers.MONTH
import com.kosherphone.jewishcalendar.helpers.WEEK
import com.kosherphone.jewishcalendar.helpers.YEAR

fun Int.isXWeeklyRepetition() = this != 0 && this % WEEK == 0

fun Int.isXMonthlyRepetition() = this != 0 && this % MONTH == 0

fun Int.isXYearlyRepetition() = this != 0 && this % YEAR == 0
