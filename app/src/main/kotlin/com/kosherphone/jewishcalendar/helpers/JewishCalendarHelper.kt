package com.kosherphone.jewishcalendar.helpers

import android.content.Context
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherphone.jewishcalendar.extensions.config
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
        // Create a JewishCalendar and set from Gregorian date components only (no time/sunset consideration)
        // Note: setGregorianDate expects 0-indexed month (0-11), but DateTime.monthOfYear is 1-indexed (1-12)
        return JewishCalendar().apply {
            setGregorianDate(dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth)
        }
    }

    fun getJewishCalendarFromTS(ts: Long): JewishCalendar {
        val dateTime = Formatter.getDateTimeFromTS(ts)
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
        return if (isLeapYear(year)) 13 else 12
    }

    fun isLeapYear(year: Int): Boolean {
        // Create a JewishCalendar instance to check if the year is a leap year
        val jc = JewishCalendar(year, JewishCalendar.TISHREI, 1)
        return jc.isJewishLeapYear
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
     * Jewish year order: Tishrei(7) -> Cheshvan(8) -> ... -> Adar(12) -> [Adar II(13)] -> Nisan(1) -> ... -> Elul(6) -> Tishrei(7) of NEXT year
     */
    fun getNextJewishMonth(jewishYear: Int, jewishMonth: Int): Pair<Int, Int> {
        return when {
            // Elul (6) -> Tishrei (7) = NEW YEAR
            jewishMonth == JewishCalendar.ELUL -> Pair(jewishYear + 1, JewishCalendar.TISHREI)
            // Adar -> Adar II in leap year, or Nisan in non-leap year
            jewishMonth == JewishCalendar.ADAR && isLeapYear(jewishYear) -> Pair(jewishYear, JewishCalendar.ADAR_II)
            // Adar II -> Nisan (same year)
            jewishMonth == JewishCalendar.ADAR_II -> Pair(jewishYear, JewishCalendar.NISSAN)
            // Adar -> Nisan in non-leap year (same year)
            jewishMonth == JewishCalendar.ADAR -> Pair(jewishYear, JewishCalendar.NISSAN)
            // Normal progression
            else -> Pair(jewishYear, jewishMonth + 1)
        }
    }

    /**
     * Get the previous Jewish month from the current one
     * Going backwards: Tishrei(7) -> Elul(6) of PREVIOUS year
     */
    fun getPreviousJewishMonth(jewishYear: Int, jewishMonth: Int): Pair<Int, Int> {
        return when {
            // Tishrei (7) -> Elul (6) = PREVIOUS YEAR
            jewishMonth == JewishCalendar.TISHREI -> Pair(jewishYear - 1, JewishCalendar.ELUL)
            // Nisan -> Adar II in leap year, or Adar in non-leap year (same year)
            jewishMonth == JewishCalendar.NISSAN && isLeapYear(jewishYear) -> Pair(jewishYear, JewishCalendar.ADAR_II)
            jewishMonth == JewishCalendar.NISSAN -> Pair(jewishYear, JewishCalendar.ADAR)
            // Adar II -> Adar
            jewishMonth == JewishCalendar.ADAR_II -> Pair(jewishYear, JewishCalendar.ADAR)
            // Normal progression backwards
            else -> Pair(jewishYear, jewishMonth - 1)
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
            "\u05E9\u05D1\u05EA",                 // Shabbos (Saturday)

        )
    }

    /**
     * Get short Hebrew weekday names (single letter)
     */
    fun getHebrewWeekDayNamesShort(): Array<String> {
        return arrayOf(
            "\u05D0",  // Aleph (Sunday)
            "\u05D1",  // Bes (Monday)
            "\u05D2",  // Gimel (Tuesday)
            "\u05D3",  // Dalet (Wednesday)
            "\u05D4",  // Hei (Thursday)
            "\u05D5",  // Vav (Friday)
            "\u05E9",   // Shin (Shabbat)

        )
    }

    /**
     * Get a JewishCalendar configured with the inIsrael setting
     */
    fun getJewishCalendarWithConfig(context: Context, dateTime: DateTime): JewishCalendar {
        return getJewishCalendar(dateTime).apply {
            inIsrael = context.config.inIsrael
        }
    }

    /**
     * Get holiday name for a given date, or null if no holiday
     */
    fun getHolidayName(context: Context, dateTime: DateTime): String? {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return getHolidayNameFromCalendar(jc)
    }

    /**
     * Get holiday name from a JewishCalendar instance
     */
    fun getHolidayNameFromCalendar(jc: JewishCalendar): String? {
        val yomTovIndex = jc.yomTovIndex

        // Check for Chanukah (has its own method)
        val chanukahDay = jc.dayOfChanukah
        if (chanukahDay > 0) {
            val dayNum = getHebrewDayNumber(chanukahDay)
            return if (chanukahDay == 8) {
                "\u05D7\u05E0\u05D5\u05DB\u05D4 - \u05D6\u05D0\u05EA \u05D7\u05E0\u05D5\u05DB\u05D4" // חנוכה - זאת חנוכה
            } else {
                "\u05D7\u05E0\u05D5\u05DB\u05D4 - \u05E0\u05E8 $dayNum" // חנוכה - נר X
            }
        }

        return when (yomTovIndex) {
            // Tishrei holidays
            JewishCalendar.EREV_ROSH_HASHANA -> "\u05E2\u05E8\u05D1 \u05E8\u05D0\u05E9 \u05D4\u05E9\u05E0\u05D4" // ערב ראש השנה
            JewishCalendar.ROSH_HASHANA -> "\u05E8\u05D0\u05E9 \u05D4\u05E9\u05E0\u05D4" // ראש השנה
            JewishCalendar.FAST_OF_GEDALYAH -> "\u05E6\u05D5\u05DD \u05D2\u05D3\u05DC\u05D9\u05D4" // צום גדליה
            JewishCalendar.EREV_YOM_KIPPUR -> "\u05E2\u05E8\u05D1 \u05D9\u05D5\u05DD \u05DB\u05D9\u05E4\u05D5\u05E8" // ערב יום כיפור
            JewishCalendar.YOM_KIPPUR -> "\u05D9\u05D5\u05DD \u05DB\u05D9\u05E4\u05D5\u05E8" // יום כיפור
            JewishCalendar.EREV_SUCCOS -> "\u05E2\u05E8\u05D1 \u05E1\u05D5\u05DB\u05D5\u05EA" // ערב סוכות
            JewishCalendar.SUCCOS -> "\u05E1\u05D5\u05DB\u05D5\u05EA" // סוכות
            JewishCalendar.CHOL_HAMOED_SUCCOS -> "\u05D7\u05D5\u05DC \u05D4\u05DE\u05D5\u05E2\u05D3 \u05E1\u05D5\u05DB\u05D5\u05EA" // חול המועד סוכות
            JewishCalendar.HOSHANA_RABBA -> "\u05D4\u05D5\u05E9\u05E2\u05E0\u05D0 \u05E8\u05D1\u05D4" // הושענא רבה
            JewishCalendar.SHEMINI_ATZERES -> if (jc.inIsrael) "\u05E9\u05DE\u05D9\u05E0\u05D9 \u05E2\u05E6\u05E8\u05EA / \u05E9\u05DE\u05D7\u05EA \u05EA\u05D5\u05E8\u05D4" else "\u05E9\u05DE\u05D9\u05E0\u05D9 \u05E2\u05E6\u05E8\u05EA" // שמיני עצרת (/ שמחת תורה in Israel)
            JewishCalendar.SIMCHAS_TORAH -> "\u05E9\u05DE\u05D7\u05EA \u05EA\u05D5\u05E8\u05D4" // שמחת תורה (Chutz LaAretz only)

            // Chanukah and winter
            JewishCalendar.TENTH_OF_TEVES -> "\u05E2\u05E9\u05E8\u05D4 \u05D1\u05D8\u05D1\u05EA" // עשרה בטבת
            JewishCalendar.TU_BESHVAT -> "\u05D8\u05F4\u05D5 \u05D1\u05E9\u05D1\u05D8" // ט״ו בשבט

            // Purim
            JewishCalendar.FAST_OF_ESTHER -> "\u05EA\u05E2\u05E0\u05D9\u05EA \u05D0\u05E1\u05EA\u05E8" // תענית אסתר
            JewishCalendar.PURIM -> "\u05E4\u05D5\u05E8\u05D9\u05DD" // פורים
            JewishCalendar.SHUSHAN_PURIM -> "\u05E9\u05D5\u05E9\u05DF \u05E4\u05D5\u05E8\u05D9\u05DD" // שושן פורים
            JewishCalendar.PURIM_KATAN -> "\u05E4\u05D5\u05E8\u05D9\u05DD \u05E7\u05D8\u05DF" // פורים קטן
            JewishCalendar.SHUSHAN_PURIM_KATAN -> "\u05E9\u05D5\u05E9\u05DF \u05E4\u05D5\u05E8\u05D9\u05DD \u05E7\u05D8\u05DF" // שושן פורים קטן

            // Pesach
            JewishCalendar.EREV_PESACH -> "\u05E2\u05E8\u05D1 \u05E4\u05E1\u05D7" // ערב פסח
            JewishCalendar.PESACH -> "\u05E4\u05E1\u05D7" // פסח
            JewishCalendar.CHOL_HAMOED_PESACH -> "\u05D7\u05D5\u05DC \u05D4\u05DE\u05D5\u05E2\u05D3 \u05E4\u05E1\u05D7" // חול המועד פסח
            JewishCalendar.PESACH_SHENI -> "\u05E4\u05E1\u05D7 \u05E9\u05E0\u05D9" // פסח שני
            JewishCalendar.SHAVUOS -> "\u05E9\u05D1\u05D5\u05E2\u05D5\u05EA" // שבועות
            JewishCalendar.EREV_SHAVUOS -> "\u05E2\u05E8\u05D1 \u05E9\u05D1\u05D5\u05E2\u05D5\u05EA" // ערב שבועות

            // Summer fasts
            JewishCalendar.SEVENTEEN_OF_TAMMUZ -> "\u05D9\u05F4\u05D6 \u05D1\u05EA\u05DE\u05D5\u05D6" // י״ז בתמוז
            JewishCalendar.TISHA_BEAV -> "\u05EA\u05E9\u05E2\u05D4 \u05D1\u05D0\u05D1" // תשעה באב

            // Special Shabbatot and other days
            JewishCalendar.LAG_BAOMER -> "\u05DC\u05F4\u05D2 \u05D1\u05E2\u05D5\u05DE\u05E8" // ל״ג בעומר
            JewishCalendar.YOM_HASHOAH -> "\u05D9\u05D5\u05DD \u05D4\u05E9\u05D5\u05D0\u05D4" // יום השואה
            JewishCalendar.YOM_HAZIKARON -> "\u05D9\u05D5\u05DD \u05D4\u05D6\u05D9\u05DB\u05E8\u05D5\u05DF" // יום הזיכרון
            JewishCalendar.YOM_HAATZMAUT -> "\u05D9\u05D5\u05DD \u05D4\u05E2\u05E6\u05DE\u05D0\u05D5\u05EA" // יום העצמאות
            JewishCalendar.YOM_YERUSHALAYIM -> "\u05D9\u05D5\u05DD \u05D9\u05E8\u05D5\u05E9\u05DC\u05D9\u05DD" // יום ירושלים

            else -> null
        }
    }

    /**
     * Check if a date is a Yom Tov (holiday where work is forbidden)
     */
    fun isYomTov(context: Context, dateTime: DateTime): Boolean {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return jc.isYomTov
    }

    /**
     * Check if a date is Chol HaMoed
     */
    fun isCholHamoed(context: Context, dateTime: DateTime): Boolean {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return jc.isCholHamoed
    }

    /**
     * Check if a date is a fast day
     */
    fun isTaanis(context: Context, dateTime: DateTime): Boolean {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return jc.isTaanis
    }

    /**
     * Get the day of Omer (1-49) or -1 if not during Sefirah
     */
    fun getDayOfOmer(context: Context, dateTime: DateTime): Int {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return jc.dayOfOmer
    }

    /**
     * Get the Sefirat HaOmer text for a date, or null if not during Sefirah
     */
    fun getOmerText(context: Context, dateTime: DateTime): String? {
        val dayOfOmer = getDayOfOmer(context, dateTime)
        if (dayOfOmer < 1) return null

        val weeks = dayOfOmer / 7
        val days = dayOfOmer % 7
        val dayNum = getHebrewDayNumber(dayOfOmer)

        return if (weeks == 0) {
            // Just days
            "\u05E1\u05E4\u05D9\u05E8\u05EA \u05D4\u05E2\u05D5\u05DE\u05E8 - \u05D9\u05D5\u05DD $dayNum" // ספירת העומר - יום X
        } else if (days == 0) {
            // Complete weeks
            val weekNum = getHebrewDayNumber(weeks)
            "\u05E1\u05E4\u05D9\u05E8\u05EA \u05D4\u05E2\u05D5\u05DE\u05E8 - $dayNum (\u05E9\u05D1\u05D5\u05E2 $weekNum)" // ספירת העומר - X (שבוע Y)
        } else {
            // Weeks and days
            val weekNum = getHebrewDayNumber(weeks)
            "\u05E1\u05E4\u05D9\u05E8\u05EA \u05D4\u05E2\u05D5\u05DE\u05E8 - \u05D9\u05D5\u05DD $dayNum" // ספירת העומר - יום X
        }
    }

    /**
     * Get the day of Chanukah (1-8) or -1 if not Chanukah
     */
    fun getDayOfChanukah(context: Context, dateTime: DateTime): Int {
        val jc = getJewishCalendarWithConfig(context, dateTime)
        return jc.dayOfChanukah
    }

    /**
     * Get all holiday/special day info for a date
     * Returns a list of strings (holiday name, omer count, etc.)
     */
    fun getSpecialDayInfo(context: Context, dateTime: DateTime): List<String> {
        if (!context.config.showJewishHolidays) return emptyList()

        val result = mutableListOf<String>()

        // Add holiday name if exists
        getHolidayName(context, dateTime)?.let { result.add(it) }

        // Add Omer count if during Sefirah (but not if already showing Lag BaOmer)
        val dayOfOmer = getDayOfOmer(context, dateTime)
        if (dayOfOmer > 0 && dayOfOmer != 33) { // Don't duplicate Lag BaOmer
            getOmerText(context, dateTime)?.let { result.add(it) }
        }

        return result
    }

    /**
     * Get a short holiday indicator for calendar day display
     */
    fun getShortHolidayIndicator(context: Context, dateTime: DateTime): String? {
        if (!context.config.showJewishHolidays) return null

        val jc = getJewishCalendarWithConfig(context, dateTime)

        // Check Chanukah first
        val chanukahDay = jc.dayOfChanukah
        if (chanukahDay > 0) {
            return "\u05D7" + getHebrewDayNumber(chanukahDay) // ח + day number
        }

        // Check Omer
        val omerDay = jc.dayOfOmer
        if (omerDay > 0) {
            return getHebrewDayNumber(omerDay) + "\u05E2" // day number + ע (for Omer)
        }

        // Return first letter of holiday name if it's a significant day
        return when (jc.yomTovIndex) {
            JewishCalendar.ROSH_HASHANA -> "\u05E8\u05D4" // ר״ה
            JewishCalendar.YOM_KIPPUR -> "\u05D9\u05DB" // י״כ
            JewishCalendar.SUCCOS, JewishCalendar.CHOL_HAMOED_SUCCOS, JewishCalendar.HOSHANA_RABBA -> "\u05E1" // ס
            JewishCalendar.SHEMINI_ATZERES, JewishCalendar.SIMCHAS_TORAH -> "\u05E9\u05E2" // ש״ע
            JewishCalendar.PESACH, JewishCalendar.CHOL_HAMOED_PESACH -> "\u05E4" // פ
            JewishCalendar.SHAVUOS -> "\u05E9" // ש
            JewishCalendar.PURIM, JewishCalendar.SHUSHAN_PURIM -> "\u05E4\u05D5" // פו
            JewishCalendar.TU_BESHVAT -> "\u05D8\u05D5" // ט״ו
            JewishCalendar.LAG_BAOMER -> "\u05DC\u05D2" // ל״ג
            JewishCalendar.FAST_OF_GEDALYAH, JewishCalendar.FAST_OF_ESTHER,
            JewishCalendar.TENTH_OF_TEVES, JewishCalendar.SEVENTEEN_OF_TAMMUZ,
            JewishCalendar.TISHA_BEAV -> "\u05E6" // צ (for fast)
            else -> null
        }
    }
}
