package com.kosherphone.jewishcalendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.kosherphone.jewishcalendar.activities.MainActivity
import com.kosherphone.jewishcalendar.adapters.MyDayPagerAdapter
import com.kosherphone.jewishcalendar.databinding.FragmentDaysHolderBinding
import com.kosherphone.jewishcalendar.dialogs.JewishDatePickerDialog
import com.kosherphone.jewishcalendar.helpers.DAILY_VIEW
import com.kosherphone.jewishcalendar.helpers.DAY_CODE
import com.kosherphone.jewishcalendar.helpers.Formatter
import com.kosherphone.jewishcalendar.interfaces.NavigationListener
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.views.MyViewPager
import org.joda.time.DateTime

class DayFragmentsHolder : MyFragmentHolder(), NavigationListener {
    private val PREFILLED_DAYS = 251

    private lateinit var viewPager: MyViewPager
    private var defaultDailyPage = 0
    private var todayDayCode = ""
    private var currentDayCode = ""
    private var isGoToTodayVisible = false

    override val viewType = DAILY_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentDayCode = arguments?.getString(DAY_CODE) ?: ""
        todayDayCode = Formatter.getTodayCode()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentDaysHolderBinding.inflate(inflater, container, false)
        binding.root.background = ColorDrawable(requireContext().getProperBackgroundColor())
        viewPager = binding.fragmentDaysViewpager
        viewPager.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return binding.root
    }

    private fun setupFragment() {
        val codes = getDays(currentDayCode)
        val dailyAdapter = MyDayPagerAdapter(requireActivity().supportFragmentManager, codes, this)
        defaultDailyPage = codes.size / 2

        viewPager.apply {
            adapter = dailyAdapter
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
            currentItem = defaultDailyPage
        }
    }

    private fun getDays(code: String): List<String> {
        val days = ArrayList<String>(PREFILLED_DAYS)
        val today = Formatter.getDateTimeFromCode(code)
        for (i in -PREFILLED_DAYS / 2..PREFILLED_DAYS / 2) {
            days.add(Formatter.getDayCodeFromDateTime(today.plusDays(i)))
        }
        return days
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
        JewishDatePickerDialog(requireActivity(), dateTime) { newDateTime ->
            goToDateTime(newDateTime)
        }
    }

    override fun refreshEvents() {
        (viewPager.adapter as? MyDayPagerAdapter)?.updateCalendars(viewPager.currentItem)
    }

    override fun shouldGoToTodayBeVisible() = currentDayCode != todayDayCode

    override fun getNewEventDayCode() = currentDayCode

    override fun printView() {
        (viewPager.adapter as? MyDayPagerAdapter)?.printCurrentView(viewPager.currentItem)
    }

    override fun getCurrentDate(): DateTime? {
        return if (currentDayCode != "") {
            Formatter.getLocalDateTimeFromCode(currentDayCode)
        } else {
            null
        }
    }
}
