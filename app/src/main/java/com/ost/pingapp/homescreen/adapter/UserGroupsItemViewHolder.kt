package com.khrd.pingapp.homescreen.adapter

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.databinding.UserGroupsItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class UserGroupsItemViewHolder(
    private val binding: UserGroupsItemBinding,
    private val imageLoader: ImageLoader,
    private val onGroupClick: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(userGroup: DatabaseGroup?) {
        imageLoader.loadImage(userGroup?.photoURL, binding.userGroupImage, R.drawable.ic_group_icon)
        binding.userGroupNameItem.text = userGroup?.name
        binding.userGroupNameItem.setOnClickListener {
            onGroupClick(userGroup?.id ?: "")
        }
    }
}