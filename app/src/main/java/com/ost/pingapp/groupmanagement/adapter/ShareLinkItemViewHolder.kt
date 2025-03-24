package com.khrd.pingapp.groupmanagement.adapter

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.databinding.JoinGroupItemBinding
import com.khrd.pingapp.databinding.ShareLinkItemBinding
import com.khrd.pingapp.groupmanagement.listeners.JoinGroupListener
import com.khrd.pingapp.groupmanagement.listeners.ShareLinkGroupListener

class ShareLinkItemViewHolder (binding: ShareLinkItemBinding, private val shareLinkListener: ShareLinkGroupListener) :
    RecyclerView.ViewHolder(binding.root) {
    private val shareButton = binding.shareLinkGroupButtonItem

    init {
        shareButton.setOnClickListener {
            shareLinkListener.onShareLinkClicked()
        }
    }

    fun bind(item: ShareLinkItem) {
    }
}