package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.received.ReceivedPingsFragment
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.SentPingsFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReceivedPingsFragment()
            1 -> SentPingsFragment()
            else -> throw IllegalStateException()
        }
    }
}