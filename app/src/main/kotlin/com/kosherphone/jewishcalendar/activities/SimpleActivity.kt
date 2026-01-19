package com.kosherphone.jewishcalendar.activities

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.CalendarContract
import androidx.core.app.NotificationManagerCompat
import com.kosherphone.jewishcalendar.BuildConfig
import com.kosherphone.jewishcalendar.R
import com.kosherphone.jewishcalendar.extensions.config
import com.kosherphone.jewishcalendar.extensions.getAlarmManager
import com.kosherphone.jewishcalendar.extensions.refreshCalDAVCalendars
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.dialogs.PermissionRequiredDialog
import org.fossify.commons.extensions.openNotificationSettings
import org.fossify.commons.extensions.openRequestExactAlarmSettings
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.helpers.isSPlus
import org.fossify.commons.helpers.isTiramisuPlus

open class SimpleActivity : BaseSimpleActivity() {
    val CALDAV_REFRESH_DELAY = 3000L
    val calDAVRefreshHandler = Handler()
    var calDAVRefreshCallback: (() -> Unit)? = null

    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher_red,
        R.mipmap.ic_launcher_pink,
        R.mipmap.ic_launcher_purple,
        R.mipmap.ic_launcher_deep_purple,
        R.mipmap.ic_launcher_indigo,
        R.mipmap.ic_launcher_blue,
        R.mipmap.ic_launcher_light_blue,
        R.mipmap.ic_launcher_cyan,
        R.mipmap.ic_launcher_teal,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher_light_green,
        R.mipmap.ic_launcher_lime,
        R.mipmap.ic_launcher_yellow,
        R.mipmap.ic_launcher_amber,
        R.mipmap.ic_launcher_orange,
        R.mipmap.ic_launcher_deep_orange,
        R.mipmap.ic_launcher_brown,
        R.mipmap.ic_launcher_blue_grey,
        R.mipmap.ic_launcher_grey_black
    )

    override fun getAppLauncherName() = getString(R.string.app_launcher_name)

    override fun getRepositoryName() = "Calendar"


    fun Context.syncCalDAVCalendars(callback: () -> Unit) {
        calDAVRefreshCallback = callback
        ensureBackgroundThread {
            val uri = CalendarContract.Calendars.CONTENT_URI
            contentResolver.unregisterContentObserver(calDAVSyncObserver)
            contentResolver.registerContentObserver(uri, false, calDAVSyncObserver)
            refreshCalDAVCalendars(config.caldavSyncedCalendarIds, true)
        }
    }

    // caldav refresh content observer triggers multiple times in a row at updating, so call the callback only a few seconds after the (hopefully) last one
    private val calDAVSyncObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if (!selfChange) {
                calDAVRefreshHandler.removeCallbacksAndMessages(null)
                calDAVRefreshHandler.postDelayed({
                    ensureBackgroundThread {
                        unregisterObserver()
                        calDAVRefreshCallback?.invoke()
                        calDAVRefreshCallback = null
                    }
                }, CALDAV_REFRESH_DELAY)
            }
        }
    }

    private fun unregisterObserver() {
        contentResolver.unregisterContentObserver(calDAVSyncObserver)
    }

    protected fun handleNotificationAvailability(callback: () -> Unit) {
        handleNotificationPermission { granted ->
            if (granted) {
                if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    callback()
                } else {
                    ConfirmationDialog(
                        activity = this,
                        messageId = org.fossify.commons.R.string.notifications_disabled,
                        positive = org.fossify.commons.R.string.ok,
                        negative = 0
                    ) {
                        callback()
                    }
                }
            } else {
                PermissionRequiredDialog(
                    this,
                    org.fossify.commons.R.string.allow_notifications_reminders,
                    { openNotificationSettings() })
            }
        }
    }

    fun maybeRequestExactAlarmPermission(callback: () -> Unit = {}) {
        if (isSPlus() && !isTiramisuPlus()) {
            // SCHEDULE_EXACT_ALARM *may* be revoked by users/system on Android 12
            if (getAlarmManager().canScheduleExactAlarms()) {
                callback()
            } else {
                PermissionRequiredDialog(
                    activity = this,
                    textId = R.string.allow_alarms_reminders,
                    positiveActionCallback = {
                        openRequestExactAlarmSettings(BuildConfig.APPLICATION_ID)
                    },
                )
            }
        } else {
            callback()
        }
    }

    // Softkey guide support (Kyocera KYY31 devices)
    private var softkeyGuide: com.kosherphone.jewishcalendar.quirks.kyocera.KYY31SoftkeyGuide? = null

    /**
     * Hook for activities to configure softkey labels / drawables.
     * Override in subclasses to call guide.setText/setDrawable/etc.
     */
    protected open fun configureSoftkeyGuide(guide: com.kosherphone.jewishcalendar.quirks.kyocera.KYY31SoftkeyGuide) {
        // no-op by default
    }

    override fun onResume() {
        super.onResume()
        try {
            val guide = com.kosherphone.jewishcalendar.quirks.kyocera.KYY31SoftkeyGuide.createFor(window)
            if (guide != null) {
                softkeyGuide = guide
                try {
                    configureSoftkeyGuide(guide)
                    guide.show()
                } catch (e: Exception) {
                    // ignore
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onPause() {
        try {
            softkeyGuide?.hide()
            softkeyGuide = null
        } catch (e: Exception) {
            // ignore
        }
        super.onPause()
    }

    // D-pad and softkey handling for non-touch devices
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        try {
            // Log all key events for debugging softkey/dpad issues
            val keyName = android.view.KeyEvent.keyCodeToString(keyCode)
            val focus = currentFocus
            val focusClass = focus?.javaClass?.simpleName ?: "null"
            android.util.Log.d("SoftkeyTest", "onKeyDown: keyCode=$keyCode ($keyName), focus=$focusClass, repeatCount=${event.repeatCount}")
            
            // don't intercept typing / text input
            if (focus is android.widget.EditText) {
                android.util.Log.d("SoftkeyTest", "  -> Skipping: EditText has focus")
                return super.onKeyDown(keyCode, event)
            }

            when (keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_DPAD_LEFT handled")
                    if (onDpadLeft()) return true
                }
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_DPAD_RIGHT handled")
                    if (onDpadRight()) return true
                }
                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_DPAD_UP handled")
                    if (onDpadUp()) return true
                }
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_DPAD_DOWN handled")
                    if (onDpadDown()) return true
                }
                android.view.KeyEvent.KEYCODE_DPAD_CENTER, android.view.KeyEvent.KEYCODE_ENTER -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_DPAD_CENTER/ENTER handled")
                    if (onDpadCenter()) return true
                }
                android.view.KeyEvent.KEYCODE_SOFT_LEFT -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_SOFT_LEFT RECEIVED & handled")
                    if (onSoftLeft()) return true
                }
                android.view.KeyEvent.KEYCODE_SOFT_RIGHT -> {
                    android.util.Log.d("SoftkeyTest", "  -> KEYCODE_SOFT_RIGHT RECEIVED & handled")
                    if (onSoftRight()) return true
                }
                else -> {
                    // Log unhandled keys too
                    if (keyCode in 80..87) { // Common physical buttons range
                        android.util.Log.d("SoftkeyTest", "  -> Unhandled special key: $keyName")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SoftkeyTest", "Exception in onKeyDown", e)
            // ignore
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Override these in activities that want to handle dpad / soft keys. Return true if handled.
     */
    protected open fun onDpadLeft(): Boolean = false
    protected open fun onDpadRight(): Boolean = false
    protected open fun onDpadUp(): Boolean = false
    protected open fun onDpadDown(): Boolean = false
    protected open fun onDpadCenter(): Boolean {
        // prefer activating focused view by default
        val focus = currentFocus
        if (focus is android.view.View && focus.isClickable) {
            focus.performClick()
            return true
        }
        return false
    }

    protected open fun onSoftLeft(): Boolean = false
    protected open fun onSoftRight(): Boolean = false
}
