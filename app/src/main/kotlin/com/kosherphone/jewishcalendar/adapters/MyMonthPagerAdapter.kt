package com.kosherphone.jewishcalendar.adapters

import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kosherphone.jewishcalendar.fragments.MonthFragment
import com.kosherphone.jewishcalendar.helpers.DAY_CODE
import com.kosherphone.jewishcalendar.interfaces.NavigationListener

class MyMonthPagerAdapter(fm: FragmentManager, private val mCodes: MutableList<String>, private val mListener: NavigationListener) : FragmentStatePagerAdapter(fm) {
    private val mFragments = SparseArray<MonthFragment>()

    override fun getCount() = mCodes.size

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        val code = mCodes[position]
        bundle.putString(DAY_CODE, code)

        val fragment = MonthFragment()
        fragment.arguments = bundle
        fragment.listener = mListener

        mFragments.put(position, fragment)
        return fragment
    }

    fun addCodes(codes: List<String>) {
        mCodes.addAll(codes)
        notifyDataSetChanged()
    }

    fun updateCalendars(pos: Int) {
        for (i in -1..1) {
            mFragments[pos + i]?.updateCalendar()
        }
    }

    fun printCurrentView(pos: Int) {
        mFragments[pos].printCurrentView()
    }
}
