package org.fossify.calendar.helpers

import android.content.Context
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import org.fossify.calendar.extensions.eventsHelper
import org.fossify.calendar.extensions.getProperDayIndexInWeek
import org.fossify.calendar.extensions.isWeekendIndex
import org.fossify.calendar.extensions.seconds
import org.fossify.calendar.interfaces.MonthlyCalendar
import org.fossify.calendar.models.DayMonthly
import org.fossify.calendar.models.Event
import org.joda.time.DateTime
import kotlin.math.min

class MonthlyCalendarImpl(val callback: MonthlyCalendar, val context: Context) {
    private val DAYS_CNT = 42

    private val mToday: String = DateTime().toString(Formatter.DAYCODE_PATTERN)
    private var mEvents = ArrayList<Event>()

    lateinit var mTargetDate: DateTime

    // Store the target Jewish month and year for navigation
    private var mTargetJewishYear: Int = 0
    private var mTargetJewishMonth: Int = 0

    fun updateMonthlyCalendar(targetDate: DateTime) {
        mTargetDate = targetDate

        // Get Jewish calendar for target date
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(targetDate)
        mTargetJewishYear = jewishCalendar.jewishYear
        mTargetJewishMonth = jewishCalendar.jewishMonth

        // Calculate the Gregorian date range for the Jewish month
        val firstDayOfJewishMonth = JewishCalendarHelper.getFirstDayOfJewishMonth(mTargetJewishYear, mTargetJewishMonth)
        val daysInMonth = JewishCalendarHelper.getDaysInJewishMonth(JewishCalendar(mTargetJewishYear, mTargetJewishMonth, 1))
        val lastDayOfJewishMonth = JewishCalendarHelper.getDateTimeForJewishDate(mTargetJewishYear, mTargetJewishMonth, daysInMonth)

        val startTS = firstDayOfJewishMonth.minusDays(7).seconds()
        val endTS = lastDayOfJewishMonth.plusDays(14).seconds()
        context.eventsHelper.getEvents(startTS, endTS) {
            gotEvents(it)
        }
    }

    fun getMonth(targetDate: DateTime) {
        updateMonthlyCalendar(targetDate)
    }

    fun getDays(markDaysWithEvents: Boolean) {
        val days = ArrayList<DayMonthly>(DAYS_CNT)

        // Get the first day of the Jewish month in Gregorian
        val firstDayOfJewishMonth = JewishCalendarHelper.getFirstDayOfJewishMonth(mTargetJewishYear, mTargetJewishMonth)
        val firstDayIndex = context.getProperDayIndexInWeek(firstDayOfJewishMonth)

        // Get days in current and previous Jewish month
        val currMonthDays = JewishCalendarHelper.getDaysInJewishMonth(JewishCalendar(mTargetJewishYear, mTargetJewishMonth, 1))
        val (prevYear, prevMonth) = JewishCalendarHelper.getPreviousJewishMonth(mTargetJewishYear, mTargetJewishMonth)
        val prevMonthDays = JewishCalendarHelper.getDaysInJewishMonth(JewishCalendar(prevYear, prevMonth, 1))

        var isThisMonth = false
        var isToday: Boolean
        var jewishDay = prevMonthDays - firstDayIndex + 1
        var currentJewishYear = prevYear
        var currentJewishMonth = prevMonth

        for (i in 0 until DAYS_CNT) {
            when {
                i < firstDayIndex -> {
                    isThisMonth = false
                    currentJewishYear = prevYear
                    currentJewishMonth = prevMonth
                }

                i == firstDayIndex -> {
                    jewishDay = 1
                    isThisMonth = true
                    currentJewishYear = mTargetJewishYear
                    currentJewishMonth = mTargetJewishMonth
                }

                jewishDay > currMonthDays && isThisMonth -> {
                    jewishDay = 1
                    isThisMonth = false
                    val (nextYear, nextMonth) = JewishCalendarHelper.getNextJewishMonth(mTargetJewishYear, mTargetJewishMonth)
                    currentJewishYear = nextYear
                    currentJewishMonth = nextMonth
                }
            }

            // Get the Gregorian DateTime for this Jewish date
            val gregorianDate = JewishCalendarHelper.getDateTimeForJewishDate(currentJewishYear, currentJewishMonth, jewishDay)
            val dayCode = Formatter.getDayCodeFromDateTime(gregorianDate)

            isToday = dayCode == mToday

            val day = DayMonthly(
                jewishDay,
                isThisMonth,
                isToday,
                dayCode,
                gregorianDate.weekOfWeekyear,
                ArrayList(),
                i,
                context.isWeekendIndex(i)
            )
            days.add(day)
            jewishDay++
        }

        if (markDaysWithEvents) {
            markDaysWithEvents(days)
        } else {
            callback.updateMonthlyCalendar(context, monthName, days, false, mTargetDate)
        }
    }

    // it works more often than not, don't touch
    private fun markDaysWithEvents(days: ArrayList<DayMonthly>) {
        val dayEvents = HashMap<String, ArrayList<Event>>()
        mEvents.forEach { event ->
            val startDateTime = Formatter.getDateTimeFromTS(event.startTS)
            val endDateTime = Formatter.getDateTimeFromTS(event.endTS)
            val endCode = Formatter.getDayCodeFromDateTime(endDateTime)

            var currDay = startDateTime
            var dayCode = Formatter.getDayCodeFromDateTime(currDay)
            var currDayEvents = dayEvents[dayCode] ?: ArrayList()
            currDayEvents.add(event)
            dayEvents[dayCode] = currDayEvents

            while (Formatter.getDayCodeFromDateTime(currDay) != endCode) {
                currDay = currDay.plusDays(1)
                dayCode = Formatter.getDayCodeFromDateTime(currDay)
                currDayEvents = dayEvents[dayCode] ?: ArrayList()
                currDayEvents.add(event)
                dayEvents[dayCode] = currDayEvents
            }
        }

        days.filter { dayEvents.keys.contains(it.code) }.forEach {
            it.dayEvents = dayEvents[it.code]!!
        }
        callback.updateMonthlyCalendar(context, monthName, days, true, mTargetDate)
    }

    private val monthName: String
        get() {
            val jewishCalendar = JewishCalendar(mTargetJewishYear, mTargetJewishMonth, 1)
            var month = JewishCalendarHelper.getHebrewMonthName(jewishCalendar)
            val currentJewishCalendar = JewishCalendarHelper.getJewishCalendar(DateTime())
            if (mTargetJewishYear != currentJewishCalendar.jewishYear) {
                month += " ${JewishCalendarHelper.getHebrewYear(jewishCalendar)}"
            }
            return month
        }

    private fun gotEvents(events: ArrayList<Event>) {
        mEvents = events
        getDays(true)
    }
}
