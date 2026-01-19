package com.kosherphone.jewishcalendar.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.kosherphone.jewishcalendar.R
import com.kosherphone.jewishcalendar.databinding.DialogCustomEventRepeatIntervalBinding
import com.kosherphone.jewishcalendar.helpers.DAY
import com.kosherphone.jewishcalendar.helpers.MONTH
import com.kosherphone.jewishcalendar.helpers.WEEK
import com.kosherphone.jewishcalendar.helpers.YEAR
import org.fossify.commons.extensions.*

class CustomEventRepeatIntervalDialog(val activity: Activity, val callback: (seconds: Int) -> Unit) {
    private var dialog: AlertDialog? = null
    private val binding by activity.viewBinding(DialogCustomEventRepeatIntervalBinding::inflate)

    init {
        binding.dialogRadioView.check(R.id.dialog_radio_days)

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> confirmRepeatInterval() }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(binding.dialogCustomRepeatIntervalValue)
                }
            }
    }

    private fun confirmRepeatInterval() {
        val value = binding.dialogCustomRepeatIntervalValue.value
        val multiplier = getMultiplier(binding.dialogRadioView.checkedRadioButtonId)
        val days = Integer.valueOf(value.ifEmpty { "0" })
        callback(days * multiplier)
        activity.hideKeyboard()
        dialog?.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_weeks -> WEEK
        R.id.dialog_radio_months -> MONTH
        R.id.dialog_radio_years -> YEAR
        else -> DAY
    }
}
