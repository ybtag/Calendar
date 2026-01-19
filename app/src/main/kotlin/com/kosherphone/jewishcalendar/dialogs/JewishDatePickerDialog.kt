package com.kosherphone.jewishcalendar.dialogs

import android.app.Activity
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherphone.jewishcalendar.databinding.DialogJewishDatePickerBinding
import com.kosherphone.jewishcalendar.helpers.JewishCalendarHelper
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.joda.time.DateTime

class JewishDatePickerDialog(
    val activity: Activity,
    val dateTime: DateTime,
    val callback: (dateTime: DateTime) -> Unit
) {
    private val binding = DialogJewishDatePickerBinding.inflate(activity.layoutInflater)
    private val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)

    init {
        setupYearPicker()
        setupMonthPicker()
        setupDayPicker()

        activity.getAlertDialogBuilder()
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ ->
                val selectedYear = binding.jewishYearPicker.value
                val selectedMonthIndex = binding.jewishMonthPicker.value
                val selectedMonth = getMonthFromIndex(selectedYear, selectedMonthIndex)
                val selectedDay = binding.jewishDayPicker.value

                val resultDateTime = JewishCalendarHelper.getDateTimeForJewishDate(selectedYear, selectedMonth, selectedDay)
                callback(resultDateTime)
            }
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun setupYearPicker() {
        val currentYear = jewishCalendar.jewishYear
        binding.jewishYearPicker.apply {
            minValue = currentYear - 100
            maxValue = currentYear + 100
            value = currentYear
            wrapSelectorWheel = false

            setFormatter { year ->
                JewishCalendarHelper.getHebrewYear(JewishCalendar(year, JewishCalendar.TISHREI, 1))
            }

            setOnValueChangedListener { _, _, newYear ->
                updateMonthPicker(newYear)
                updateDayPicker()
            }
        }
    }

    private fun setupMonthPicker() {
        updateMonthPicker(jewishCalendar.jewishYear)
        binding.jewishMonthPicker.value = getIndexFromMonth(jewishCalendar.jewishYear, jewishCalendar.jewishMonth)

        binding.jewishMonthPicker.setOnValueChangedListener { _, _, _ ->
            updateDayPicker()
        }
    }

    private fun setupDayPicker() {
        updateDayPicker()
        binding.jewishDayPicker.value = jewishCalendar.jewishDayOfMonth
    }

    private fun updateMonthPicker(year: Int) {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        val monthNames = getMonthNames(isLeapYear)

        binding.jewishMonthPicker.apply {
            displayedValues = null
            minValue = 0
            maxValue = monthNames.size - 1
            displayedValues = monthNames
            wrapSelectorWheel = true
        }
    }

    private fun updateDayPicker() {
        val year = binding.jewishYearPicker.value
        val monthIndex = binding.jewishMonthPicker.value
        val month = getMonthFromIndex(year, monthIndex)
        val daysInMonth = JewishCalendar(year, month, 1).daysInJewishMonth

        val currentDay = binding.jewishDayPicker.value
        binding.jewishDayPicker.apply {
            displayedValues = null
            minValue = 1
            maxValue = daysInMonth

            // Create Hebrew day number display values
            val dayNames = (1..daysInMonth).map { JewishCalendarHelper.getHebrewDayNumber(it) }.toTypedArray()
            displayedValues = dayNames

            value = if (currentDay > daysInMonth) daysInMonth else currentDay
        }
    }

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

    private fun getMonthFromIndex(year: Int, index: Int): Int {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        return when {
            index <= 4 -> index + JewishCalendar.TISHREI
            isLeapYear && index == 5 -> JewishCalendar.ADAR
            isLeapYear && index == 6 -> JewishCalendar.ADAR_II
            isLeapYear -> index - 7 + JewishCalendar.NISSAN
            index == 5 -> JewishCalendar.ADAR
            else -> index - 6 + JewishCalendar.NISSAN
        }
    }

    private fun getIndexFromMonth(year: Int, month: Int): Int {
        val isLeapYear = JewishCalendarHelper.isLeapYear(year)
        return when {
            month >= JewishCalendar.TISHREI -> month - JewishCalendar.TISHREI
            month == JewishCalendar.ADAR && isLeapYear -> 5
            month == JewishCalendar.ADAR_II -> 6
            month == JewishCalendar.ADAR -> 5
            isLeapYear -> month + 6
            else -> month + 5
        }
    }
}
