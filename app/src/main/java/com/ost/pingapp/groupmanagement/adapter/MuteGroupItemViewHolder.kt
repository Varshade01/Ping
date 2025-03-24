package com.khrd.pingapp.groupmanagement.adapter

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.MuteGroupItemBinding
import com.khrd.pingapp.groupmanagement.listeners.MuteGroupListener

class MuteGroupItemViewHolder(private val binding: MuteGroupItemBinding, private val muteGroupListener: MuteGroupListener) :
    RecyclerView.ViewHolder(binding.root) {
    private val muteGroupButton = binding.muteGroupButtonItem

    fun bind(item: MuteGroupItem) {
        handleMuteClick(item)
        handleMuteUserState(item)
    }

    private fun handleMuteUserState(item: MuteGroupItem) {
        val isMuted = item.muted
        if (isMuted) {
            setMutedState()
        } else {
            setUnMutedState()
        }
    }

    private fun setUnMutedState() {
        muteGroupButton.text = binding.root.context.getString(R.string.mute_group)
    }

    private fun setMutedState() {
        muteGroupButton.text = binding.root.context.getString(R.string.unmute_group)
    }

    private fun handleMuteClick(item: MuteGroupItem) {
        muteGroupButton.setOnClickListener {
            muteGroupListener.onMuteGroupClicked(item)
        }
    }
}