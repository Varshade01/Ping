package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.databinding.ReceiverStatusItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader


class PingStatusReceiversAdapter(private val imageLoader: ImageLoader) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: MutableList<ReceiverStatusItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ReceiverStatusItemViewHolder(
        ReceiverStatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), imageLoader)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ReceiverStatusItemViewHolder).bind(items[position])
    }

    fun setData(items: List<ReceiverStatusItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}