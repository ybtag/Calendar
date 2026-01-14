package org.fossify.calendar.helpers

import android.content.Context
import android.util.SparseArray
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import org.fossify.calendar.extensions.eventsHelper
import org.fossify.calendar.extensions.seconds
import org.fossify.calendar.interfaces.YearlyCalendar
import org.fossify.calendar.models.DayYearly
import org.fossify.calendar.models.Event
import org.joda.time.DateTime

class YearlyCalendarImpl(val callback: YearlyCalendar, val context: Context, val year: Int) {

    // For Jewish calendar, 'year' represents the Jewish year
    private val jewishYear = year

    fun getEvents(year: Int) {
        // Get the Gregorian date range for the Jewish year
        val firstDayOfYear = JewishCalendarHelper.getFirstDayOfJewishMonth(jewishYear, JewishCalendar.TISHREI)
        val lastMonth = if (JewishCalendarHelper.isLeapYear(jewishYear)) JewishCalendar.ELUL else JewishCalendar.ELUL
        val daysInLastMonth = JewishCalendarHelper.getDaysInJewishMonth(JewishCalendar(jewishYear, lastMonth, 1))
        val lastDayOfYear = JewishCalendarHelper.getDateTimeForJewishDate(jewishYear, lastMonth, daysInLastMonth)

        val startTS = firstDayOfYear.seconds()
        val endTS = lastDayOfYear.plusDays(1).seconds()
        context.eventsHelper.getEvents(startTS, endTS) {
            gotEvents(it)
        }
    }

    private fun gotEvents(events: MutableList<Event>) {
        // Jewish years can have 12 or 13 months
        val monthsInYear = JewishCalendarHelper.getMonthsInJewishYear(jewishYear)
        val arr = SparseArray<ArrayList<DayYearly>>(monthsInYear)

        events.forEach {
            val startDateTime = Formatter.getDateTimeFromTS(it.startTS)
            markDay(arr, startDateTime, it)

            val startCode = Formatter.getDayCodeFromDateTime(startDateTime)
            val endDateTime = Formatter.getDateTimeFromTS(it.endTS)
            val endCode = Formatter.getDayCodeFromDateTime(endDateTime)
            if (startCode != endCode) {
                var currDateTime = startDateTime
                while (Formatter.getDayCodeFromDateTime(currDateTime) != endCode) {
                    currDateTime = currDateTime.plusDays(1)
                    markDay(arr, currDateTime, it)
                }
            }
        }
        callback.updateYearlyCalendar(arr, events.hashCode())
    }

    private fun markDay(arr: SparseArray<ArrayList<DayYearly>>, dateTime: DateTime, event: Event) {
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        val month = jewishCalendar.jewishMonth
        val day = jewishCalendar.jewishDayOfMonth

        if (arr[month] == null) {
            arr.put(month, ArrayList())
            // Jewish months can have up to 30 days
            for (i in 1..31)
                arr.get(month).add(DayYearly())
        }

        if (jewishCalendar.jewishYear == jewishYear) {
            arr.get(month)[day].addColor(event.color)
        }
    }
}
