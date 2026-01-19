package com.kosherphone.jewishcalendar.dialogs

import android.app.Activity
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherphone.jewishcalendar.databinding.DialogJewishYearPickerBinding
import com.kosherphone.jewishcalendar.helpers.JewishCalendarHelper
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff

class JewishYearPickerDialog(
    val activity: Activity,
    val jewishYear: Int,
    val callback: (year: Int) -> Unit
) {
    private val binding = DialogJewishYearPickerBinding.inflate(activity.layoutInflater)

    init {
        setupYearPicker()

        activity.getAlertDialogBuilder()
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ ->
                callback(binding.jewishYearPicker.value)
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
        }
    }
}
