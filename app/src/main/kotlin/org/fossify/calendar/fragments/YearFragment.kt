package org.fossify.calendar.fragments

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import org.fossify.calendar.activities.MainActivity
import org.fossify.calendar.databinding.FragmentYearBinding
import org.fossify.calendar.databinding.SmallMonthViewHolderBinding
import org.fossify.calendar.databinding.TopNavigationBinding
import org.fossify.calendar.extensions.config
import org.fossify.calendar.extensions.getProperDayIndexInWeek
import org.fossify.calendar.extensions.getViewBitmap
import org.fossify.calendar.extensions.printBitmap
import org.fossify.calendar.helpers.JewishCalendarHelper
import org.fossify.calendar.helpers.YEAR_LABEL
import org.fossify.calendar.helpers.YearlyCalendarImpl
import org.fossify.calendar.interfaces.NavigationListener
import org.fossify.calendar.interfaces.YearlyCalendar
import org.fossify.calendar.models.DayYearly
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.updateTextColors
import org.joda.time.DateTime

class YearFragment : Fragment(), YearlyCalendar {
    private var mYear = 0  // This is the Jewish year
    private var mFirstDayOfWeek = 0
    private var isPrintVersion = false
    private var lastHash = 0
    private var mCalendar: YearlyCalendarImpl? = null

    var listener: NavigationListener? = null

    private lateinit var binding: FragmentYearBinding
    private lateinit var topNavigationBinding: TopNavigationBinding
    private lateinit var monthHolders: List<SmallMonthViewHolderBinding>

    // Hebrew month names in Jewish calendar order (Tishrei first)
    // Tishrei=7, Cheshvan=8, Kislev=9, Tevet=10, Shevat=11, Adar=12, Nissan=1, Iyar=2, Sivan=3, Tammuz=4, Av=5, Elul=6
    private val hebrewMonthNames = arrayOf(
        "\u05EA\u05E9\u05E8\u05D9",      // Tishrei
        "\u05D7\u05E9\u05D5\u05DF",      // Cheshvan
        "\u05DB\u05E1\u05DC\u05D5",      // Kislev
        "\u05D8\u05D1\u05EA",            // Tevet
        "\u05E9\u05D1\u05D8",            // Shevat
        "\u05D0\u05D3\u05E8",            // Adar (or Adar I in leap year)
        "\u05D0\u05D3\u05E8 \u05D1",     // Adar II (leap year only)
        "\u05E0\u05D9\u05E1\u05DF",      // Nissan
        "\u05D0\u05D9\u05D9\u05E8",      // Iyar
        "\u05E1\u05D9\u05D5\u05DF",      // Sivan
        "\u05EA\u05DE\u05D5\u05D6",      // Tammuz
        "\u05D0\u05D1",                  // Av
        "\u05D0\u05DC\u05D5\u05DC"       // Elul
    )

    // Jewish month numbers in display order (starting from Tishrei)
    private val jewishMonthOrder = arrayOf(
        JewishCalendar.TISHREI,  // 7
        JewishCalendar.CHESHVAN, // 8
        JewishCalendar.KISLEV,   // 9
        JewishCalendar.TEVES,    // 10
        JewishCalendar.SHEVAT,   // 11
        JewishCalendar.ADAR,     // 12
        JewishCalendar.ADAR_II,  // 13 (leap year only)
        JewishCalendar.NISSAN,   // 1
        JewishCalendar.IYAR,     // 2
        JewishCalendar.SIVAN,    // 3
        JewishCalendar.TAMMUZ,   // 4
        JewishCalendar.AV,       // 5
        JewishCalendar.ELUL      // 6
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentYearBinding.inflate(inflater, container, false)
        topNavigationBinding = TopNavigationBinding.bind(binding.root)
        monthHolders = arrayListOf(
            binding.month1Holder, binding.month2Holder, binding.month3Holder, binding.month4Holder, binding.month5Holder, binding.month6Holder,
            binding.month7Holder, binding.month8Holder, binding.month9Holder, binding.month10Holder, binding.month11Holder, binding.month12Holder
        )

        mYear = requireArguments().getInt(YEAR_LABEL)  // This is the Jewish year
        requireContext().updateTextColors(binding.calendarWrapper)
        setupMonths()
        setupButtons()

        mCalendar = YearlyCalendarImpl(this, requireContext(), mYear)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        mFirstDayOfWeek = requireContext().config.firstDayOfWeek
    }

    override fun onResume() {
        super.onResume()
        val firstDayOfWeek = requireContext().config.firstDayOfWeek
        if (firstDayOfWeek != mFirstDayOfWeek) {
            mFirstDayOfWeek = firstDayOfWeek
            setupMonths()
        }
        updateCalendar()
    }

    fun updateCalendar() {
        mCalendar?.getEvents(mYear)
    }

    private fun setupMonths() {
        val isLeapYear = JewishCalendarHelper.isLeapYear(mYear)

        // Get the month indices to display (skip Adar II in non-leap years)
        val monthsToDisplay = if (isLeapYear) {
            // In leap year, show all 13 months but layout only has 12 slots
            // Show Tishrei through Adar I (6 months), then Adar II through Elul (7 months) = 13 months
            // For now, we'll show 12 months (Tishrei to Elul, with Adar I/II combined display)
            listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12) // Skip Adar II (index 6) for layout constraints
        } else {
            // Non-leap year: show 12 months
            listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12) // Skip Adar II (index 6)
        }

        monthHolders.forEachIndexed { index, monthHolder ->
            if (index < monthsToDisplay.size) {
                monthHolder.root.beVisible()
                val monthNameIndex = monthsToDisplay[index]
                val jewishMonth = jewishMonthOrder[monthNameIndex]

                val monthView = monthHolder.smallMonthView
                val curTextColor = when {
                    isPrintVersion -> resources.getColor(org.fossify.commons.R.color.theme_light_text_color)
                    else -> requireContext().getProperTextColor()
                }

                // Set Hebrew month name
                monthHolder.monthLabel.text = hebrewMonthNames[monthNameIndex]
                monthHolder.monthLabel.setTextColor(curTextColor)

                // Get the first day of this Jewish month
                val firstDayOfJewishMonth = JewishCalendarHelper.getFirstDayOfJewishMonth(mYear, jewishMonth)
                monthView.firstDay = requireContext().getProperDayIndexInWeek(firstDayOfJewishMonth)

                // Get number of days in this Jewish month
                val numberOfDays = JewishCalendarHelper.getDaysInJewishMonth(JewishCalendar(mYear, jewishMonth, 1))
                monthView.setDays(numberOfDays)

                monthView.setOnClickListener {
                    // Open the first day of this Jewish month
                    (activity as MainActivity).openMonthFromYearly(firstDayOfJewishMonth)
                }
            } else {
                monthHolder.root.beGone()
            }
        }

        if (!isPrintVersion) {
            markCurrentMonth()
        }
    }

    private fun setupButtons() {
        val textColor = requireContext().getProperTextColor()
        topNavigationBinding.topLeftArrow.apply {
            applyColorFilter(textColor)
            background = null
            setOnClickListener {
                listener?.goLeft()
            }

            val pointerLeft = requireContext().getDrawable(org.fossify.commons.R.drawable.ic_chevron_left_vector)
            pointerLeft?.isAutoMirrored = true
            setImageDrawable(pointerLeft)
        }

        topNavigationBinding.topRightArrow.apply {
            applyColorFilter(textColor)
            background = null
            setOnClickListener {
                listener?.goRight()
            }

            val pointerRight = requireContext().getDrawable(org.fossify.commons.R.drawable.ic_chevron_right_vector)
            pointerRight?.isAutoMirrored = true
            setImageDrawable(pointerRight)
        }

        topNavigationBinding.topValue.apply {
            setTextColor(requireContext().getProperTextColor())
            setOnClickListener {
                (activity as MainActivity).showGoToDateDialog()
            }
        }
    }

    private fun markCurrentMonth() {
        val now = DateTime()
        val todayJewish = JewishCalendarHelper.getJewishCalendar(now)

        if (todayJewish.jewishYear == mYear) {
            val currentJewishMonth = todayJewish.jewishMonth
            val isLeapYear = JewishCalendarHelper.isLeapYear(mYear)

            // Find the index of the current month in our display order
            val monthsToDisplay = if (isLeapYear) {
                listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12)
            } else {
                listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12)
            }

            val displayIndex = monthsToDisplay.indexOfFirst { jewishMonthOrder[it] == currentJewishMonth }
            if (displayIndex >= 0 && displayIndex < monthHolders.size) {
                val monthHolder = monthHolders[displayIndex]
                monthHolder.monthLabel.setTextColor(requireContext().getProperPrimaryColor())
                monthHolder.smallMonthView.todaysId = todayJewish.jewishDayOfMonth
            }
        }
    }

    override fun updateYearlyCalendar(events: SparseArray<ArrayList<DayYearly>>, hashCode: Int) {
        if (!isAdded) {
            return
        }

        if (hashCode == lastHash) {
            return
        }

        lastHash = hashCode
        val isLeapYear = JewishCalendarHelper.isLeapYear(mYear)
        val monthsToDisplay = if (isLeapYear) {
            listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12)
        } else {
            listOf(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12)
        }

        monthHolders.forEachIndexed { index, monthHolder ->
            if (index < monthsToDisplay.size) {
                val monthView = monthHolder.smallMonthView
                val jewishMonth = jewishMonthOrder[monthsToDisplay[index]]
                monthView.setEvents(events.get(jewishMonth))
            }
        }

        topNavigationBinding.topValue.post {
            // Display Hebrew year
            val jewishCalendar = JewishCalendar(mYear, JewishCalendar.TISHREI, 1)
            topNavigationBinding.topValue.text = JewishCalendarHelper.getHebrewYear(jewishCalendar)
        }
    }

    fun printCurrentView() {
        isPrintVersion = true
        setupMonths()
        toggleSmallMonthPrintModes()

        requireContext().printBitmap(binding.calendarWrapper.getViewBitmap())

        isPrintVersion = false
        setupMonths()
        toggleSmallMonthPrintModes()
    }

    private fun toggleSmallMonthPrintModes() {
        monthHolders.forEach {
            it.smallMonthView.togglePrintMode()
        }
    }
}
