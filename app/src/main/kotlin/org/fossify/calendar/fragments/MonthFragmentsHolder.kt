package org.fossify.calendar.fragments

import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.viewpager.widget.ViewPager
import org.fossify.calendar.activities.MainActivity
import org.fossify.calendar.adapters.MyMonthPagerAdapter
import org.fossify.calendar.databinding.FragmentMonthsHolderBinding
import org.fossify.calendar.extensions.getMonthCode
import org.fossify.calendar.helpers.DAY_CODE
import org.fossify.calendar.helpers.Formatter
import org.fossify.calendar.helpers.JewishCalendarHelper
import org.fossify.calendar.helpers.MONTHLY_VIEW
import org.fossify.calendar.interfaces.NavigationListener
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.views.MyViewPager
import org.joda.time.DateTime

class MonthFragmentsHolder : MyFragmentHolder(), NavigationListener {
    private val PREFILLED_MONTHS = 251

    private lateinit var viewPager: MyViewPager
    private var defaultMonthlyPage = 0
    private var todayDayCode = ""
    private var currentDayCode = ""
    private var isGoToTodayVisible = false

    override val viewType = MONTHLY_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDayCode = arguments?.getString(DAY_CODE) ?: ""
        todayDayCode = Formatter.getTodayCode()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentMonthsHolderBinding.inflate(inflater, container, false)
        binding.root.background = ColorDrawable(requireContext().getProperBackgroundColor())
        viewPager = binding.fragmentMonthsViewpager
        viewPager.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return binding.root
    }

    private fun setupFragment() {
        val codes = getMonths(currentDayCode)
        val monthlyAdapter = MyMonthPagerAdapter(requireActivity().supportFragmentManager, codes, this)
        defaultMonthlyPage = codes.size / 2

        viewPager.apply {
            adapter = monthlyAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    currentDayCode = codes[position]
                    val shouldGoToTodayBeVisible = shouldGoToTodayBeVisible()
                    if (isGoToTodayVisible != shouldGoToTodayBeVisible) {
                        (activity as? MainActivity)?.toggleGoToTodayVisibility(shouldGoToTodayBeVisible)
                        isGoToTodayVisible = shouldGoToTodayBeVisible
                    }
                }
            })
            currentItem = defaultMonthlyPage
        }
    }

    private fun getMonths(code: String): List<String> {
        val months = ArrayList<String>(PREFILLED_MONTHS)
        val today = Formatter.getDateTimeFromCode(code)

        // Get the Jewish calendar for the current date
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(today)
        var currentJewishYear = jewishCalendar.jewishYear
        var currentJewishMonth = jewishCalendar.jewishMonth

        // Go back PREFILLED_MONTHS/2 Jewish months
        for (i in 0 until PREFILLED_MONTHS / 2) {
            val (prevYear, prevMonth) = JewishCalendarHelper.getPreviousJewishMonth(currentJewishYear, currentJewishMonth)
            currentJewishYear = prevYear
            currentJewishMonth = prevMonth
        }

        // Now add PREFILLED_MONTHS Jewish months
        for (i in 0 until PREFILLED_MONTHS) {
            val firstDayOfMonth = JewishCalendarHelper.getFirstDayOfJewishMonth(currentJewishYear, currentJewishMonth)
            months.add(Formatter.getDayCodeFromDateTime(firstDayOfMonth))

            val (nextYear, nextMonth) = JewishCalendarHelper.getNextJewishMonth(currentJewishYear, currentJewishMonth)
            currentJewishYear = nextYear
            currentJewishMonth = nextMonth
        }

        return months
    }

    override fun goLeft() {
        viewPager.currentItem = viewPager.currentItem - 1
    }

    override fun goRight() {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    override fun goToDateTime(dateTime: DateTime) {
        currentDayCode = Formatter.getDayCodeFromDateTime(dateTime)
        setupFragment()
    }

    override fun goToToday() {
        currentDayCode = todayDayCode
        setupFragment()
    }

    override fun showGoToDateDialog() {
        if (activity == null) {
            return
        }

        val datePicker = getDatePickerView()
        datePicker.findViewById<View>(Resources.getSystem().getIdentifier("day", "id", "android")).beGone()

        val dateTime = getCurrentDate()!!
        datePicker.init(dateTime.year, dateTime.monthOfYear - 1, 1, null)

        activity?.getAlertDialogBuilder()!!
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> datePicked(dateTime, datePicker) }
            .apply {
                activity?.setupDialogStuff(datePicker, this)
            }
    }

    private fun datePicked(dateTime: DateTime, datePicker: DatePicker) {
        val month = datePicker.month + 1
        val year = datePicker.year
        val newDateTime = dateTime.withDate(year, month, 1)
        goToDateTime(newDateTime)
    }

    override fun refreshEvents() {
        (viewPager.adapter as? MyMonthPagerAdapter)?.updateCalendars(viewPager.currentItem)
    }

    override fun shouldGoToTodayBeVisible() = currentDayCode.getMonthCode() != todayDayCode.getMonthCode()

    override fun getNewEventDayCode() = if (shouldGoToTodayBeVisible()) currentDayCode else todayDayCode

    override fun printView() {
        (viewPager.adapter as? MyMonthPagerAdapter)?.printCurrentView(viewPager.currentItem)
    }

    override fun getCurrentDate(): DateTime? {
        return if (currentDayCode != "") {
            Formatter.getLocalDateTimeFromCode(currentDayCode)
        } else {
            null
        }
    }
}
