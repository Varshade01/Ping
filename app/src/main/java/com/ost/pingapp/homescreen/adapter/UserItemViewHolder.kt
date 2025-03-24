package com.khrd.pingapp.homescreen.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.UserItemBinding
import com.khrd.pingapp.homescreen.fragments.MuteUserListener
import com.khrd.pingapp.homescreen.fragments.SendPingToUserListener
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class UserItemViewHolder(
    private val binding: UserItemBinding,
    private var listener: SendPingToUserListener?,
    private var searchInAllGroups: Boolean,
    private val imageLoader: ImageLoader,
    private val muteUserListener: MuteUserListener,

    ) : RecyclerView.ViewHolder(binding.root) {
    private val nestedAdapter = UserGroupsListAdapter(imageLoader)

    fun setViewHolderListener(listener: SendPingToUserListener) {
        this.listener = listener
    }

    fun setSearchInAllGroup(searchInAllGroups: Boolean) {
        this.searchInAllGroups = searchInAllGroups
    }

    fun bind(item: UserItem) {
        handleSendPingClick(item)
        binding.userName.text = item.fullname
        binding.userJob.text = item.job
        setUserGroups(item)
        setUserOnlineStatus(item)
        imageLoader.loadImage(item.photoURL, binding.userPhoto, R.drawable.ic_default_user_avatar)
        handleMuteUserState(item)
        handleMuteClick(item)
    }

    private fun handleSendPingClick(item: UserItem) {
        binding.sendPingToUser.setOnClickListener {
            listener?.onSendPingToUserClicked(item)
        }
    }

    private fun handleMuteUserState(item: UserItem) {
        val isMuted = item.muted
        binding.mutedIcon.isVisible = isMuted
        if (isMuted) {
            setMutedState()
        } else {
            setUnMutedState()
        }
    }

    private fun setUnMutedState() {
        val iconMute = binding.root.context.getDrawable(R.drawable.ic_mute_button)
        binding.muteUser.setImageDrawable(iconMute)
    }

    private fun setMutedState() {
        val iconUnMute = binding.root.context.getDrawable(R.drawable.ic_unmute_button)
        binding.muteUser.setImageDrawable(iconUnMute)
    }

    private fun handleMuteClick(item: UserItem) {
        binding.muteUser.setOnClickListener {
            muteUserListener.onMuteUserClicked(item)
        }
    }

    private fun setUserGroups(item: UserItem) {
        if (searchInAllGroups) {
            binding.userGroupsRecyclerView.visibility = View.VISIBLE
            initNestedRecyclerView()
            nestedAdapter.setData(item.groups)
        } else {
            binding.userGroupsRecyclerView.visibility = View.GONE
        }
    }

    private fun showSeenTooltip(item: UserItem) {
        binding.userPhoto.setOnClickListener {
            openUserStatusDialog(item, itemView)
        }
    }

    private fun setUserOnlineStatus(item: UserItem) {
        showSeenTooltip(item)
        if (item.isOnline?.status == true && !item.isHide) {
            binding.userOnlineStatus.visibility = View.VISIBLE
        } else {
            binding.userOnlineStatus.visibility = View.GONE
        }
    }

    private fun openUserStatusDialog(userItem: UserItem, itemView: View) {
        val action = HomescreenNavGraphDirections.showUserStatusDialog(userItem)
        findNavController(itemView).navigateSafe(action)
    }

    private fun initNestedRecyclerView() {
        binding.userGroupsRecyclerView.apply {
            adapter = nestedAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
        }
    }
}