package com.khrd.pingapp.homescreen.fragments.intouch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.FragmentPingsBinding
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ViewPagerAdapter

class PingsFragment : Fragment() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentPingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPingsBinding.inflate(inflater, container, false)
        initActionBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().findViewById<Button>(R.id.sendPing)?.visibility = View.VISIBLE
        initViewPager()
    }

    private fun initViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager = binding.vpPings
        viewPager.adapter = viewPagerAdapter

        binding.tlPings.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabLayout = (binding.tlPings.getChildAt(0) as ViewGroup).getChildAt(tab!!.position) as LinearLayout
                val tabTextView = tabLayout.getChildAt(1) as TextView
                tabTextView.setTextAppearance(R.style.InTouchPingsTabs_SelectedTextAppearance)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabLayout = (binding.tlPings.getChildAt(0) as ViewGroup).getChildAt(tab!!.position) as LinearLayout
                val tabTextView = tabLayout.getChildAt(1) as TextView
                tabTextView.setTextAppearance(R.style.InTouchPingsTabs_UnselectedTextAppearance)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val tabList = listOf(getString(R.string.received), getString(R.string.sent))
        val iconList = listOf(R.drawable.ic_arrow_down_tab, R.drawable.ic_arrow_up_tab)
        TabLayoutMediator(binding.tlPings, viewPager) { tab, position ->
            tab.text = tabList[position]
            tab.setIcon(iconList[position])
        }.attach()
    }

    private fun initActionBar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.tbPingsBar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.pings)
        }
    }
}