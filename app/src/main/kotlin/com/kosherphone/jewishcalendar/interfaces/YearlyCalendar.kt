package com.kosherphone.jewishcalendar.interfaces

import android.util.SparseArray
import com.kosherphone.jewishcalendar.models.DayYearly

interface YearlyCalendar {
    fun updateYearlyCalendar(events: SparseArray<ArrayList<DayYearly>>, hashCode: Int)
}
