package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.databinding.*
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.*
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.ScheduledPingsHeaderListener
import com.khrd.pingapp.homescreen.listeners.OnSeenClickedListener
import com.khrd.pingapp.homescreen.states.CancelPingListener

private const val ITEM_TYPE_UNKNOWN = 0
private const val ITEM_TYPE_UNSCHEDULED = 1
private const val ITEM_TYPE_SCHEDULED = 2
private const val ITEM_TYPE_HEADER = 3
private const val ITEM_TYPE_EMPTY_SCHEDULED = 4
private const val ITEM_TYPE_DIVIDER = 5

class PingsRecyclerAdapter(
    private val cancelPingListener: CancelPingListener?,
    private val listener: ScheduledPingsHeaderListener?,
    private val onSeenClickedListener: OnSeenClickedListener?,
    private val onPhotoClickListener: (item: SentPingItem) -> Unit
) : ListAdapter<DisplayablePingItem, RecyclerView.ViewHolder>(PingItemsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_TYPE_UNSCHEDULED -> {
                val binding = PingSentItemUnscheduledBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentPingsItemViewHolder(binding, onSeenClickedListener,onPhotoClickListener)
            }
            ITEM_TYPE_SCHEDULED -> {
                val binding = PingSentItemScheduledBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentPingsScheduledItemViewHolder(binding, cancelPingListener,onPhotoClickListener)
            }
            ITEM_TYPE_HEADER -> {
                SentPingsScheduledHeaderItemViewHolder(
                    PingScheduledHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    listener
                )
            }
            ITEM_TYPE_EMPTY_SCHEDULED -> {
                EmptyScheduledPingsItemViewHolder(PingScheduledEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            ITEM_TYPE_DIVIDER -> {
                SentPingsDividerViewHolder(PingSentDividerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else -> throw IllegalStateException()
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = currentList[position]) {
            is SentPingItem -> (holder as SentPingsItemViewHolder).bind(currentItem)
            is SentPingScheduledItem -> (holder as SentPingsScheduledItemViewHolder).bind(currentItem)
            is SentPingScheduledHeader -> (holder as SentPingsScheduledHeaderItemViewHolder).bind(currentItem)
            is SentPingScheduledEmptyMessage -> {
                //do nothing
            }
            is SentPingDivider -> {
                //do nothing
            }
            else -> throw IllegalStateException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (currentList[position]) {
            is SentPingItem -> ITEM_TYPE_UNSCHEDULED
            is SentPingScheduledItem -> ITEM_TYPE_SCHEDULED
            is SentPingScheduledHeader -> ITEM_TYPE_HEADER
            is SentPingScheduledEmptyMessage -> ITEM_TYPE_EMPTY_SCHEDULED
            is SentPingDivider -> ITEM_TYPE_DIVIDER
            else -> ITEM_TYPE_UNKNOWN
        }

    fun setData(sentItems: List<SentPingItem>, scheduledItems: List<SentPingScheduledItem>, showScheduled: Boolean) {
        val scheduledHeader = listOf(getScheduledHeader(showScheduled))
        val emptyScheduledPingsMessage = SentPingScheduledEmptyMessage
        val divider = SentPingDivider

        submitList(
            if (showScheduled) {
                if (scheduledItems.isEmpty()) {
                    scheduledHeader + emptyScheduledPingsMessage + divider + sentItems
                } else {
                    scheduledHeader + scheduledItems + divider + sentItems
                }
            } else {
                scheduledHeader + divider + sentItems
            }
        )
    }

    override fun getItem(position: Int): DisplayablePingItem {
        return currentList[position]
    }

    private fun getScheduledHeader(showScheduled: Boolean): SentPingScheduledHeader {
        return currentList.filterIsInstance<SentPingScheduledHeader>()
            .firstOrNull()?.apply { expanded = showScheduled }
            .also { notifyItemChanged(0) }
            ?: SentPingScheduledHeader(showScheduled)
    }
}
