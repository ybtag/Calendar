package com.kosherphone.jewishcalendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.kosherphone.jewishcalendar.activities.MainActivity
import com.kosherphone.jewishcalendar.adapters.MyMonthDayPagerAdapter
import com.kosherphone.jewishcalendar.databinding.FragmentMonthsDaysHolderBinding
import com.kosherphone.jewishcalendar.dialogs.JewishMonthPickerDialog
import com.kosherphone.jewishcalendar.extensions.getMonthCode
import com.kosherphone.jewishcalendar.helpers.DAY_CODE
import com.kosherphone.jewishcalendar.helpers.Formatter
import com.kosherphone.jewishcalendar.helpers.JewishCalendarHelper
import com.kosherphone.jewishcalendar.helpers.MONTHLY_DAILY_VIEW
import com.kosherphone.jewishcalendar.interfaces.NavigationListener
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.views.MyViewPager
import org.joda.time.DateTime

class MonthDayFragmentsHolder : MyFragmentHolder(), NavigationListener {
    private val PREFILLED_MONTHS = 251
    private val PREFETCH_THRESHOLD_MONTHS = 12
    private val APPENDED_MONTHS = 120

    private lateinit var viewPager: MyViewPager
    private lateinit var monthCodes: MutableList<String>
    private var defaultMonthlyPage = 0
    private var todayDayCode = ""
    private var currentDayCode = ""
    private var isGoToTodayVisible = false

    override val viewType = MONTHLY_DAILY_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDayCode = arguments?.getString(DAY_CODE) ?: ""
        todayDayCode = Formatter.getTodayCode()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentMonthsDaysHolderBinding.inflate(inflater, container, false)
        binding.root.background = ColorDrawable(requireContext().getProperBackgroundColor())
        viewPager = binding.fragmentMonthsDaysViewpager
        viewPager.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return binding.root
    }

    private fun setupFragment() {
        monthCodes = getMonths(currentDayCode)
        val monthlyDailyAdapter = MyMonthDayPagerAdapter(requireActivity().supportFragmentManager, monthCodes, this)
        defaultMonthlyPage = monthCodes.size / 2

        viewPager.apply {
            adapter = monthlyDailyAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    currentDayCode = monthCodes[position]
                    appendFutureMonthsIfNeeded(position)
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

    private fun getMonths(code: String): MutableList<String> {
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

    private fun appendFutureMonthsIfNeeded(position: Int) {
        if (position < monthCodes.size - PREFETCH_THRESHOLD_MONTHS) {
            return
        }

        val newMonths = getNextMonths(monthCodes.last(), APPENDED_MONTHS)
        (viewPager.adapter as? MyMonthDayPagerAdapter)?.addCodes(newMonths)
    }

    private fun getNextMonths(lastCode: String, count: Int): List<String> {
        val months = ArrayList<String>(count)
        val lastDateTime = Formatter.getDateTimeFromCode(lastCode)
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(lastDateTime)
        var (currentJewishYear, currentJewishMonth) = JewishCalendarHelper.getNextJewishMonth(
            jewishCalendar.jewishYear,
            jewishCalendar.jewishMonth
        )

        repeat(count) {
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

        val dateTime = getCurrentDate()!!
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)

        JewishMonthPickerDialog(requireActivity(), jewishCalendar.jewishYear, jewishCalendar.jewishMonth) { year, month ->
            val newDateTime = JewishCalendarHelper.getFirstDayOfJewishMonth(year, month)
            goToDateTime(newDateTime)
        }
    }

    override fun refreshEvents() {
        (viewPager.adapter as? MyMonthDayPagerAdapter)?.updateCalendars(viewPager.currentItem)
    }

    override fun shouldGoToTodayBeVisible() = currentDayCode.getMonthCode() != todayDayCode.getMonthCode()

    override fun getNewEventDayCode() = (viewPager.adapter as? MyMonthDayPagerAdapter)?.getNewEventDayCode(viewPager.currentItem)
        ?: if (shouldGoToTodayBeVisible()) {
            currentDayCode
        } else {
            todayDayCode
        }

    override fun printView() {}

    override fun getCurrentDate(): DateTime? {
        return if (currentDayCode != "") {
            Formatter.getLocalDateTimeFromCode(currentDayCode)
        } else {
            null
        }
    }
}
