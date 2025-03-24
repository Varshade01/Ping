package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter

import androidx.recyclerview.widget.DiffUtil

class PingItemsDiffCallback: DiffUtil.ItemCallback<DisplayablePingItem>() {
    override fun areItemsTheSame(oldItem: DisplayablePingItem, newItem: DisplayablePingItem): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: DisplayablePingItem, newItem: DisplayablePingItem): Boolean = oldItem == newItem
}