package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.databinding.GroupItemBinding
import com.khrd.pingapp.homescreen.fragments.BottomSheetListener
import com.khrd.pingapp.homescreen.fragments.GroupBottomSheetFragment.Companion.ITEM_TYPE_GROUP
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class GroupBottomSheetAdapter(
    private val currentGroupName: String,
    private val groupItems: Array<DatabaseGroup>,
    private val currentUser: DatabaseUser,
    private val imageLoader: ImageLoader,
    private val bottomSheetListener: BottomSheetListener,
) : RecyclerView.Adapter<GroupBottomSheetAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder =
        when (viewType) {
            ITEM_TYPE_GROUP -> {
                val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                GroupViewHolder(binding)
            }

            else -> throw IllegalStateException()
        }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = groupItems[position]
        setGroupImage(holder, item.photoURL)
        holder.groupName.text = item.name
        if (item.id == currentGroupName) {
            holder.groupStatus.visibility = View.VISIBLE
        } else {
            holder.groupStatus.visibility = View.GONE
        }

        holder.sendPingToGroup.setOnClickListener {
            bottomSheetListener.openSendPingDialog(groupId = item.id, isGroupPing = true)
        }

        holder.itemView.setOnClickListener {
            bottomSheetListener.passGroupId(item.id)
        }

        handleMuteGroupState(item.id, currentUser.mutedItems?.keys, holder)
        holder.muteGroupButton.setOnClickListener {
            bottomSheetListener.muteGroup(item.id, checkIfGroupMuted(item.id, currentUser.mutedItems?.keys))
        }
    }

    override fun getItemCount(): Int = groupItems.size

    override fun getItemViewType(position: Int): Int = ITEM_TYPE_GROUP

    private fun setGroupImage(holder: GroupViewHolder, imageUrl: String?) {
        imageLoader.loadImage(imageUrl, holder.groupImage, R.drawable.ic_default_group_avatar)
    }

    private fun handleMuteGroupState(groupId: String?, mutedItems: MutableSet<String>?, holder: GroupViewHolder) {
        val isMuted = mutedItems?.contains(groupId) == true
        holder.muteGroupButton.setImageResource(if (isMuted) R.drawable.ic_unmute_button else R.drawable.ic_mute_button)
        holder.mutedIcon.isVisible = isMuted
    }

    private fun checkIfGroupMuted(groupId: String?, mutedItems: MutableSet<String>?): Boolean {
        return mutedItems?.contains(groupId) == true
    }

    fun onMuteGroupActionChanged(groupId: String?, state: Boolean) {
        if (state) {
            currentUser.mutedItems?.put(groupId ?: "", "")
            groupItems.forEachIndexed { i, it ->
                if (it.id == groupId) notifyItemChanged(i)
            }
        } else {
            currentUser.mutedItems?.remove(groupId)
            groupItems.forEachIndexed { i, it ->
                if (it.id == groupId) notifyItemChanged(i)
            }
        }
    }

    class GroupViewHolder(binding: GroupItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val groupImage: ImageView = binding.groupPhoto
        val groupName: TextView = binding.recyclerGroupName
        val mutedIcon: ImageView = binding.mutedIcon
        val groupStatus: TextView = binding.groupStatus
        val sendPingToGroup: ImageView = binding.sendPingToGroupInRecycler
        val muteGroupButton: ImageView = binding.muteGroup
    }
}