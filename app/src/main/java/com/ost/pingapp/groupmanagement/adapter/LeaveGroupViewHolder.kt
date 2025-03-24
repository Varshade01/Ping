package com.khrd.pingapp.groupmanagement.adapter

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.databinding.LeaveGroupItemBinding
import com.khrd.pingapp.groupmanagement.listeners.LeaveGroupListener

class LeaveGroupItemViewHolder(binding: LeaveGroupItemBinding, private val leaveGroupListener: LeaveGroupListener) :
    RecyclerView.ViewHolder(binding.root) {
    val text = binding.leaveGroupHeader
    val button = binding.leaveButton

    init {
        button.setOnClickListener {
            leaveGroupListener.onLeaveGroupAction()
        }
    }

    fun bind(item: LeaveGroupItem) {
    }

}