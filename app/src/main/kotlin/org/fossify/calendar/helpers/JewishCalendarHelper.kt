package org.fossify.calendar.helpers

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import org.joda.time.DateTime
import java.util.Calendar

object JewishCalendarHelper {
    private val hebrewFormatter = HebrewDateFormatter().apply {
        isHebrewFormat = true
    }

    // Hebrew month names
    private val HEBREW_MONTH_NAMES = arrayOf(
        "\u05E0\u05D9\u05E1\u05DF",      // Nissan
        "\u05D0\u05D9\u05D9\u05E8",      // Iyar
        "\u05E1\u05D9\u05D5\u05DF",      // Sivan
        "\u05EA\u05DE\u05D5\u05D6",      // Tammuz
        "\u05D0\u05D1",                  // Av
        "\u05D0\u05DC\u05D5\u05DC",      // Elul
        "\u05EA\u05E9\u05E8\u05D9",      // Tishrei
        "\u05D7\u05E9\u05D5\u05DF",      // Cheshvan
        "\u05DB\u05E1\u05DC\u05D5",      // Kislev
        "\u05D8\u05D1\u05EA",            // Tevet
        "\u05E9\u05D1\u05D8",            // Shevat
        "\u05D0\u05D3\u05E8",            // Adar (or Adar I in leap year)
        "\u05D0\u05D3\u05E8 \u05D1"      // Adar II (leap year only)
    )

    // Hebrew day letters for displaying day numbers
    private val HEBREW_NUMERALS = arrayOf(
        "\u05D0", "\u05D1", "\u05D2", "\u05D3", "\u05D4",  // 1-5
        "\u05D5", "\u05D6", "\u05D7", "\u05D8",            // 6-9
        "\u05D9",                                          // 10
        "\u05D9\u05D0", "\u05D9\u05D1", "\u05D9\u05D2", "\u05D9\u05D3",  // 11-14
        "\u05D8\u05D5",                                    // 15 (tet-vav, not yud-hei)
        "\u05D8\u05D6",                                    // 16 (tet-zayin, not yud-vav)
        "\u05D9\u05D6", "\u05D9\u05D7", "\u05D9\u05D8",    // 17-19
        "\u05DB",                                          // 20
        "\u05DB\u05D0", "\u05DB\u05D1", "\u05DB\u05D2", "\u05DB\u05D3",  // 21-24
        "\u05DB\u05D4", "\u05DB\u05D5", "\u05DB\u05D6", "\u05DB\u05D7", "\u05DB\u05D8",  // 25-29
        "\u05DC"                                           // 30
    )

    fun getJewishCalendar(dateTime: DateTime): JewishCalendar {
        val calendar = Calendar.getInstance().apply {
            set(dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth)
        }
        return JewishCalendar(calendar)
    }

    fun getJewishCalendarFromTS(ts: Long): JewishCalendar {
        val dateTime = DateTime(ts * 1000L)
        return getJewishCalendar(dateTime)
    }

    fun getHebrewMonthName(jewishCalendar: JewishCalendar): String {
        val month = jewishCalendar.jewishMonth
        val isLeapYear = jewishCalendar.isJewishLeapYear

        return when {
            month == JewishCalendar.ADAR && isLeapYear -> HEBREW_MONTH_NAMES[11] // Adar I
            month == JewishCalendar.ADAR_II -> HEBREW_MONTH_NAMES[12] // Adar II
            month == JewishCalendar.ADAR -> HEBREW_MONTH_NAMES[11] // Adar in non-leap year
            else -> HEBREW_MONTH_NAMES[month - 1]
        }
    }

    fun getHebrewDayNumber(day: Int): String {
        return if (day in 1..30) {
            HEBREW_NUMERALS[day - 1]
        } else {
            day.toString()
        }
    }

    fun getJewishDay(jewishCalendar: JewishCalendar): Int {
        return jewishCalendar.jewishDayOfMonth
    }

    fun getJewishMonth(jewishCalendar: JewishCalendar): Int {
        return jewishCalendar.jewishMonth
    }

    fun getJewishYear(jewishCalendar: JewishCalendar): Int {
        return jewishCalendar.jewishYear
    }

    fun getHebrewYear(jewishCalendar: JewishCalendar): String {
        return hebrewFormatter.formatHebrewNumber(jewishCalendar.jewishYear)
    }

    fun getDaysInJewishMonth(jewishCalendar: JewishCalendar): Int {
        return jewishCalendar.daysInJewishMonth
    }

    fun getMonthsInJewishYear(year: Int): Int {
        return if (JewishCalendar.isJewishLeapYear(year)) 13 else 12
    }

    fun isLeapYear(year: Int): Boolean {
        return JewishCalendar.isJewishLeapYear(year)
    }

    /**
     * Get the DateTime for the first day of the given Jewish month/year
     */
    fun getFirstDayOfJewishMonth(jewishYear: Int, jewishMonth: Int): DateTime {
        val jc = JewishCalendar(jewishYear, jewishMonth, 1)
        val gregorianDate = jc.gregorianCalendar
        return DateTime(
            gregorianDate.get(Calendar.YEAR),
            gregorianDate.get(Calendar.MONTH) + 1,
            gregorianDate.get(Calendar.DAY_OF_MONTH),
            0, 0
        )
    }

    /**
     * Get DateTime for a specific day in a Jewish month/year
     */
    fun getDateTimeForJewishDate(jewishYear: Int, jewishMonth: Int, jewishDay: Int): DateTime {
        val jc = JewishCalendar(jewishYear, jewishMonth, jewishDay)
        val gregorianDate = jc.gregorianCalendar
        return DateTime(
            gregorianDate.get(Calendar.YEAR),
            gregorianDate.get(Calendar.MONTH) + 1,
            gregorianDate.get(Calendar.DAY_OF_MONTH),
            0, 0
        )
    }

    /**
     * Get the next Jewish month from the current one
     */
    fun getNextJewishMonth(jewishYear: Int, jewishMonth: Int): Pair<Int, Int> {
        val monthsInYear = getMonthsInJewishYear(jewishYear)
        return if (jewishMonth >= monthsInYear) {
            Pair(jewishYear + 1, 1)
        } else {
            // Handle the special case of Adar in leap years
            val nextMonth = when {
                jewishMonth == JewishCalendar.ADAR && isLeapYear(jewishYear) -> JewishCalendar.ADAR_II
                else -> jewishMonth + 1
            }
            Pair(jewishYear, nextMonth)
        }
    }

    /**
     * Get the previous Jewish month from the current one
     */
    fun getPreviousJewishMonth(jewishYear: Int, jewishMonth: Int): Pair<Int, Int> {
        return if (jewishMonth <= 1) {
            val prevYear = jewishYear - 1
            val monthsInPrevYear = getMonthsInJewishYear(prevYear)
            Pair(prevYear, monthsInPrevYear)
        } else {
            // Handle the special case of Adar II in leap years
            val prevMonth = when {
                jewishMonth == JewishCalendar.ADAR_II -> JewishCalendar.ADAR
                else -> jewishMonth - 1
            }
            Pair(jewishYear, prevMonth)
        }
    }

    /**
     * Format a full Hebrew date string
     */
    fun formatFullHebrewDate(jewishCalendar: JewishCalendar): String {
        val day = getHebrewDayNumber(jewishCalendar.jewishDayOfMonth)
        val month = getHebrewMonthName(jewishCalendar)
        val year = getHebrewYear(jewishCalendar)
        return "$day $month $year"
    }

    /**
     * Get the Jewish calendar order (1-based index for iteration purposes)
     * In Jewish calendar: Tishrei=7, Cheshvan=8, ..., Elul=6
     * For display, we want: Tishrei=1, Cheshvan=2, ..., Elul=12/13
     */
    fun getJewishMonthOrder(month: Int): Int {
        return when {
            month >= JewishCalendar.TISHREI -> month - JewishCalendar.TISHREI + 1
            else -> month + 6 // Nissan-Elul become 7-12
        }
    }

    /**
     * Get the Jewish month from display order (1=Tishrei, etc.)
     */
    fun getJewishMonthFromOrder(order: Int, isLeapYear: Boolean): Int {
        val maxMonths = if (isLeapYear) 13 else 12
        val adjustedOrder = ((order - 1) % maxMonths) + 1
        return when {
            adjustedOrder <= 6 -> adjustedOrder + 6 // 1-6 -> Tishrei(7)-Adar(12/13)
            else -> adjustedOrder - 6 // 7-12/13 -> Nissan(1)-Elul(6)
        }
    }

    /**
     * Get Hebrew weekday names
     */
    fun getHebrewWeekDayNames(): Array<String> {
        return arrayOf(
            "\u05E8\u05D0\u05E9\u05D5\u05DF",    // Rishon (Sunday)
            "\u05E9\u05E0\u05D9",                // Sheni (Monday)
            "\u05E9\u05DC\u05D9\u05E9\u05D9",    // Shlishi (Tuesday)
            "\u05E8\u05D1\u05D9\u05E2\u05D9",    // Revi'i (Wednesday)
            "\u05D7\u05DE\u05D9\u05E9\u05D9",    // Chamishi (Thursday)
            "\u05E9\u05D9\u05E9\u05D9",          // Shishi (Friday)
            "\u05E9\u05D1\u05EA"                 // Shabbat (Saturday)
        )
    }

    /**
     * Get short Hebrew weekday names (single letter)
     */
    fun getHebrewWeekDayNamesShort(): Array<String> {
        return arrayOf(
            "\u05D0",  // Aleph (Sunday)
            "\u05D1",  // Bet (Monday)
            "\u05D2",  // Gimel (Tuesday)
            "\u05D3",  // Dalet (Wednesday)
            "\u05D4",  // Hei (Thursday)
            "\u05D5",  // Vav (Friday)
            "\u05E9"   // Shin (Shabbat)
        )
    }
}
