package com.kosherphone.jewishcalendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.kosherphone.jewishcalendar.activities.MainActivity
import com.kosherphone.jewishcalendar.adapters.MyYearPagerAdapter
import com.kosherphone.jewishcalendar.databinding.FragmentYearsHolderBinding
import com.kosherphone.jewishcalendar.dialogs.JewishYearPickerDialog
import com.kosherphone.jewishcalendar.helpers.Formatter
import com.kosherphone.jewishcalendar.helpers.JewishCalendarHelper
import com.kosherphone.jewishcalendar.helpers.YEARLY_VIEW
import com.kosherphone.jewishcalendar.helpers.YEAR_TO_OPEN
import com.kosherphone.jewishcalendar.interfaces.NavigationListener
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.views.MyViewPager
import org.joda.time.DateTime

class YearFragmentsHolder : MyFragmentHolder(), NavigationListener {
    private val PREFILLED_YEARS = 61

    private lateinit var viewPager: MyViewPager
    private var defaultYearlyPage = 0
    private var todayYear = 0
    private var currentYear = 0
    private var isGoToTodayVisible = false

    override val viewType = YEARLY_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dateTimeString = arguments?.getString(YEAR_TO_OPEN)
        val dateTime = if (dateTimeString != null) DateTime.parse(dateTimeString) else DateTime()
        // Use Jewish year instead of Gregorian
        val jewishCalendar = JewishCalendarHelper.getJewishCalendar(dateTime)
        currentYear = jewishCalendar.jewishYear
        todayYear = JewishCalendarHelper.getJewishCalendar(DateTime()).jewishYear
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentYearsHolderBinding.inflate(inflater, container, false)
        binding.root.background = ColorDrawable(requireContext().getProperBackgroundColor())
        viewPager = binding.fragmentYearsViewpager
        viewPager.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return binding.root
    }

    private fun setupFragment() {
        val years = getYears(currentYear)
        val yearlyAdapter = MyYearPagerAdapter(requireActivity().supportFragmentManager, years, this)
        defaultYearlyPage = years.size / 2

        viewPager.apply {
            adapter = yearlyAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    currentYear = years[position]
                    val shouldGoToTodayBeVisible = shouldGoToTodayBeVisible()
                    if (isGoToTodayVisible != shouldGoToTodayBeVisible) {
                        (activity as? MainActivity)?.toggleGoToTodayVisibility(shouldGoToTodayBeVisible)
                        isGoToTodayVisible = shouldGoToTodayBeVisible
                    }
                }
            })
            currentItem = defaultYearlyPage
        }
    }

    private fun getYears(targetYear: Int): List<Int> {
        val years = ArrayList<Int>(PREFILLED_YEARS)
        years += targetYear - PREFILLED_YEARS / 2..targetYear + PREFILLED_YEARS / 2
        return years
    }

    override fun goLeft() {
        viewPager.currentItem = viewPager.currentItem - 1
    }

    override fun goRight() {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    override fun goToDateTime(dateTime: DateTime) {}

    override fun goToToday() {
        currentYear = todayYear
        setupFragment()
    }

    override fun showGoToDateDialog() {
        if (activity == null) {
            return
        }

        JewishYearPickerDialog(requireActivity(), currentYear) { year ->
            if (currentYear != year) {
                currentYear = year
                setupFragment()
            }
        }
    }

    override fun refreshEvents() {
        (viewPager.adapter as? MyYearPagerAdapter)?.updateCalendars(viewPager.currentItem)
    }

    override fun shouldGoToTodayBeVisible() = currentYear != todayYear

    override fun getNewEventDayCode() = Formatter.getTodayCode()

    override fun printView() {
        (viewPager.adapter as? MyYearPagerAdapter)?.printCurrentView(viewPager.currentItem)
    }

    override fun getCurrentDate() = null
}
