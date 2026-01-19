package com.kosherphone.jewishcalendar.dialogs

import android.app.Activity
import android.widget.NumberPicker
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherphone.jewishcalendar.databinding.DialogJewishMonthPickerBinding
import com.kosherphone.jewishcalendar.helpers.JewishCalendarHelper
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff

class JewishMonthPickerDialog(
    val activity: Activity,
    val jewishYear: Int,
    val jewishMonth: Int,
    val callback: (year: Int, month: Int) -> Unit
) {
    private val binding = DialogJewishMonthPickerBinding.inflate(activity.layoutInflater)

    init {
        setupYearPicker()
        setupMonthPicker()

        activity.getAlertDialogBuilder()
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ ->
                val selectedYear = binding.jewishYearPicker.value
                val selectedMonthIndex = binding.jewishMonthPicker.value
                val selectedMonth = getMonthFromIndex(selectedYear, selectedMonthIndex)
                callback(selectedYear, selectedMonth)
            }
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun setupYearPicker() {
        binding.jewishYearPicker.apply {
            minValue = jewishYear - 100
            maxValue = jewishYear + 100
            value = jewishYear
            wrapSelectorWheel = false

            // Display Hebrew year numbers
            setFormatter { year ->
                JewishCalendarHelper.getHebrewYear(JewishCalendar(year, JewishCalendar.TISHREI, 1))
            }

            setOnValueChangedListener { _, _, newYear ->
                updateMonthPicker(newYear)
            }
        }
    }

    private fun setupMonthPicker() {
        updateMonthPicker(jewishYear)
        binding.jewishMonthPicker.value = getIndexFromMonth(jewishYear, jewishMonth)
    }

    private fun updateMonthPicker(year: Int) {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        val monthNames = getMonthNames(isLeapYear)

        binding.jewishMonthPicker.apply {
            displayedValues = null // Reset before changing min/max
            minValue = 0
            maxValue = monthNames.size - 1
            displayedValues = monthNames
            wrapSelectorWheel = true
        }
    }

    /**
     * Get month names in Jewish year order (Tishrei first)
     */
    private fun getMonthNames(isLeapYear: Boolean): Array<String> {
        val months = mutableListOf(
            "\u05EA\u05E9\u05E8\u05D9",      // Tishrei
            "\u05D7\u05E9\u05D5\u05DF",      // Cheshvan
            "\u05DB\u05E1\u05DC\u05D5",      // Kislev
            "\u05D8\u05D1\u05EA",            // Teves
            "\u05E9\u05D1\u05D8",            // Shevat
        )

        if (isLeapYear) {
            months.add("\u05D0\u05D3\u05E8 \u05D0\u05F3")  // Adar I
            months.add("\u05D0\u05D3\u05E8 \u05D1\u05F3")  // Adar II
        } else {
            months.add("\u05D0\u05D3\u05E8")              // Adar
        }

        months.addAll(listOf(
            "\u05E0\u05D9\u05E1\u05DF",      // Nisan
            "\u05D0\u05D9\u05D9\u05E8",      // Iyar
            "\u05E1\u05D9\u05D5\u05DF",      // Sivan
            "\u05EA\u05DE\u05D5\u05D6",      // Tammuz
            "\u05D0\u05D1",                  // Av
            "\u05D0\u05DC\u05D5\u05DC"       // Elul
        ))

        return months.toTypedArray()
    }

    /**
     * Convert display index (0=Tishrei) to JewishCalendar month number
     */
    private fun getMonthFromIndex(year: Int, index: Int): Int {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        return when {
            index <= 4 -> index + JewishCalendar.TISHREI  // 0-4 -> Tishrei(7) - Shevat(11)
            isLeapYear && index == 5 -> JewishCalendar.ADAR        // Adar I (12)
            isLeapYear && index == 6 -> JewishCalendar.ADAR_II     // Adar II (13)
            isLeapYear -> index - 7 + JewishCalendar.NISSAN        // 7-12 -> Nisan(1) - Elul(6)
            index == 5 -> JewishCalendar.ADAR                      // Adar (12) in non-leap
            else -> index - 6 + JewishCalendar.NISSAN              // 6-11 -> Nisan(1) - Elul(6)
        }
    }

    /**
     * Convert JewishCalendar month number to display index (0=Tishrei)
     */
    private fun getIndexFromMonth(year: Int, month: Int): Int {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        return when {
            month >= JewishCalendar.TISHREI -> month - JewishCalendar.TISHREI  // Tishrei(7)-Shevat(11) -> 0-4
            month == JewishCalendar.ADAR && isLeapYear -> 5                     // Adar I -> 5
            month == JewishCalendar.ADAR_II -> 6                                // Adar II -> 6
            month == JewishCalendar.ADAR -> 5                                   // Adar in non-leap -> 5
            isLeapYear -> month + 6                                             // Nisan(1)-Elul(6) -> 7-12
            else -> month + 5                                                   // Nisan(1)-Elul(6) -> 6-11
        }
    }
}
