package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.PingScheduledHeaderBinding
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledHeader
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.ScheduledPingsHeaderListener

class SentPingsScheduledHeaderItemViewHolder(val binding: PingScheduledHeaderBinding, val listener: ScheduledPingsHeaderListener?) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SentPingScheduledHeader) {
        binding.pingSentItemShceduledHeaderTv.setOnClickListener {
            listener?.onHeaderClicked()
        }
        binding.scheduledPingsFoldedIcon.setOnClickListener {
            listener?.onHeaderClicked()
        }
        if (item.expanded) {
            setHidePingsIcon()
        } else {
            setShowPingsIcon()
        }
    }

    private fun setShowPingsIcon() {
        binding.scheduledPingsFoldedIcon.setImageResource(R.drawable.scheduled_pings_folded)
    }

    private fun setHidePingsIcon() {
        binding.scheduledPingsFoldedIcon.setImageResource(R.drawable.scheduled_pings_unfolded)
    }
}