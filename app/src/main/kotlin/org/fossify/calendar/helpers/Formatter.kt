package org.fossify.calendar.helpers

import android.content.Context
import org.fossify.calendar.extensions.config
import org.fossify.calendar.extensions.seconds
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

object Formatter {
    const val DAYCODE_PATTERN = "YYYYMMdd"
    const val YEAR_PATTERN = "YYYY"
    const val TIME_PATTERN = "HHmmss"
    private const val MONTH_PATTERN = "MMM"
    private const val DAY_PATTERN = "d"
    private const val DAY_OF_WEEK_PATTERN = "EEE"
    private const val DATE_DAY_PATTERN = "d EEEE"
    private const val PATTERN_TIME_12 = "hh:mm a"
    private const val PATTERN_TIME_24 = "HH:mm"

    private const val PATTERN_HOURS_12 = "h a"
    private const val PATTERN_HOURS_24 = "HH"

    fun getDateFromCode(context: Context, dayCode: String, shortMonth: Boolean = false): String {
        val dateTime = getDateTimeFromCode(dayCode)
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        val day = JewishCalendarHelper.getHebrewDayNumber(jewishCalendar.jewishDayOfMonth)
        val month = JewishCalendarHelper.getHebrewMonthName(jewishCalendar)
        val year = JewishCalendarHelper.getHebrewYear(jewishCalendar)

        val currentJewishCalendar = JewishCalendarHelper.getJewishCalendar(DateTime())
        var date = "$day $month"
        if (jewishCalendar.jewishYear != currentJewishCalendar.jewishYear) {
            date += " $year"
        }

        return date
    }

    fun getDayTitle(context: Context, dayCode: String, addDayOfWeek: Boolean = true): String {
        val date = getDateFromCode(context, dayCode)
        val dateTime = getDateTimeFromCode(dayCode)
        val hebrewWeekDays = JewishCalendarHelper.getHebrewWeekDayNames()
        // DateTime uses 1=Monday, 7=Sunday, but we need 0=Sunday, 6=Saturday
        val dayOfWeekIndex = if (dateTime.dayOfWeek == 7) 0 else dateTime.dayOfWeek
        val day = hebrewWeekDays[dayOfWeekIndex]
        return if (addDayOfWeek)
            "$date ($day)"
        else
            date
    }

    fun getDateDayTitle(dayCode: String): String {
        val dateTime = getDateTimeFromCode(dayCode)
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        val day = JewishCalendarHelper.getHebrewDayNumber(jewishCalendar.jewishDayOfMonth)
        val hebrewWeekDays = JewishCalendarHelper.getHebrewWeekDayNames()
        val dayOfWeekIndex = if (dateTime.dayOfWeek == 7) 0 else dateTime.dayOfWeek
        val dayOfWeek = hebrewWeekDays[dayOfWeekIndex]
        return "$day $dayOfWeek"
    }

    fun getLongMonthYear(context: Context, dayCode: String): String {
        val dateTime = getDateTimeFromCode(dayCode)
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        val month = JewishCalendarHelper.getHebrewMonthName(jewishCalendar)
        val year = JewishCalendarHelper.getHebrewYear(jewishCalendar)

        val currentJewishCalendar = JewishCalendarHelper.getJewishCalendar(DateTime())
        var date = month

        if (jewishCalendar.jewishYear != currentJewishCalendar.jewishYear) {
            date += " $year"
        }

        return date
    }

    fun getDate(context: Context, dateTime: DateTime, addDayOfWeek: Boolean = true) = getDayTitle(context, getDayCodeFromDateTime(dateTime), addDayOfWeek)

    fun getFullDate(context: Context, dateTime: DateTime): String {
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        return JewishCalendarHelper.formatFullHebrewDate(jewishCalendar)
    }

    fun getTodayCode() = getDayCodeFromTS(getNowSeconds())

    fun getTodayDayNumber(): String {
        val jewishCalendar = JewishCalendarHelper.getJewishCalendarFromTS(getNowSeconds())
        return JewishCalendarHelper.getHebrewDayNumber(jewishCalendar.jewishDayOfMonth)
    }

    fun getCurrentMonthShort(): String {
        val jewishCalendar = JewishCalendarHelper.getJewishCalendarFromTS(getNowSeconds())
        return JewishCalendarHelper.getHebrewMonthName(jewishCalendar)
    }

    fun getTime(context: Context, dateTime: DateTime) = dateTime.toString(getTimePattern(context))

    fun getDateTimeFromCode(dayCode: String) = DateTimeFormat.forPattern(DAYCODE_PATTERN).withZone(DateTimeZone.UTC).parseDateTime(dayCode)

    fun getLocalDateTimeFromCode(dayCode: String) =
        DateTimeFormat.forPattern(DAYCODE_PATTERN).withZone(DateTimeZone.getDefault()).parseLocalDate(dayCode).toDateTimeAtStartOfDay()

    fun getTimeFromTS(context: Context, ts: Long) = getTime(context, getDateTimeFromTS(ts))

    fun getDayStartTS(dayCode: String) = getLocalDateTimeFromCode(dayCode).seconds()

    fun getDayEndTS(dayCode: String) = getLocalDateTimeFromCode(dayCode).plusDays(1).minusMinutes(1).seconds()

    fun getDayCodeFromDateTime(dateTime: DateTime) = dateTime.toString(DAYCODE_PATTERN)

    fun getDateFromTS(ts: Long) = LocalDate(ts * 1000L, DateTimeZone.getDefault())

    fun getDateTimeFromTS(ts: Long, tz: DateTimeZone = DateTimeZone.getDefault()) = DateTime(ts * 1000L, tz)

    fun getUTCDateTimeFromTS(ts: Long) = DateTime(ts * 1000L, DateTimeZone.UTC)

    // Hebrew month names for Jewish calendar
    private val HEBREW_MONTH_NAMES = arrayOf(
        "\u05E0\u05D9\u05E1\u05DF",      // Nissan (1)
        "\u05D0\u05D9\u05D9\u05E8",      // Iyar (2)
        "\u05E1\u05D9\u05D5\u05DF",      // Sivan (3)
        "\u05EA\u05DE\u05D5\u05D6",      // Tammuz (4)
        "\u05D0\u05D1",                  // Av (5)
        "\u05D0\u05DC\u05D5\u05DC",      // Elul (6)
        "\u05EA\u05E9\u05E8\u05D9",      // Tishrei (7)
        "\u05D7\u05E9\u05D5\u05DF",      // Cheshvan (8)
        "\u05DB\u05E1\u05DC\u05D5",      // Kislev (9)
        "\u05D8\u05D1\u05EA",            // Tevet (10)
        "\u05E9\u05D1\u05D8",            // Shevat (11)
        "\u05D0\u05D3\u05E8",            // Adar (12) or Adar I in leap year
        "\u05D0\u05D3\u05E8 \u05D1"      // Adar II (13, leap year only)
    )

    fun getMonthName(context: Context, id: Int): String {
        // id is the Jewish month number (1=Nissan, 7=Tishrei, etc.)
        return if (id in 1..13) {
            HEBREW_MONTH_NAMES[id - 1]
        } else {
            HEBREW_MONTH_NAMES[0]
        }
    }

    fun getShortMonthName(context: Context, id: Int): String {
        // Same as full name for Hebrew
        return getMonthName(context, id)
    }

    fun getHourPattern(context: Context) = if (context.config.use24HourFormat) PATTERN_HOURS_24 else PATTERN_HOURS_12

    fun getTimePattern(context: Context) = if (context.config.use24HourFormat) PATTERN_TIME_24 else PATTERN_TIME_12

    fun getExportedTime(ts: Long): String {
        val dateTime = DateTime(ts, DateTimeZone.UTC)
        return "${dateTime.toString(DAYCODE_PATTERN)}T${dateTime.toString(TIME_PATTERN)}Z"
    }

    fun getDayCodeFromTS(ts: Long, tz: DateTimeZone = DateTimeZone.getDefault()): String {
        val daycode = getDateTimeFromTS(ts, tz).toString(DAYCODE_PATTERN)
        return if (daycode.isNotEmpty()) {
            daycode
        } else {
            "0"
        }
    }

    fun getUTCDayCodeFromTS(ts: Long) = getUTCDateTimeFromTS(ts).toString(DAYCODE_PATTERN)

    fun getYearFromDayCode(dayCode: String) = getDateTimeFromCode(dayCode).toString(YEAR_PATTERN)

    fun getShiftedTS(dateTime: DateTime, toZone: DateTimeZone) = dateTime.withTimeAtStartOfDay().withZoneRetainFields(toZone).seconds()

    fun getShiftedLocalTS(ts: Long) = getShiftedTS(dateTime = getUTCDateTimeFromTS(ts), toZone = DateTimeZone.getDefault())

    fun getShiftedUtcTS(ts: Long) = getShiftedTS(dateTime = getDateTimeFromTS(ts), toZone = DateTimeZone.UTC)
}
