package com.khrd.pingapp.groupmanagement.adapter

import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.GroupImageItemBinding
import com.khrd.pingapp.groupmanagement.listeners.UpdateGroupImageListener


class UpdateGroupImageItemViewHolder(
    private val binding: GroupImageItemBinding,
    private val updateGroupImageListener: UpdateGroupImageListener,
    private val imageLoader: ImageLoader,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnUploadGroupImage.setOnClickListener {
            updateGroupImageListener.onUpdateGroupImageAction()
        }
    }

    fun bind(item: UpdateGroupImageItem) {
        imageLoader.loadImage(item.imageUrl, binding.settingsGroupIcon, R.drawable.ic_default_group_avatar)
    }

    fun setGroupImageUrl(groupImageUrl: String?) {
        imageLoader.loadImage(groupImageUrl, binding.settingsGroupIcon, R.drawable.ic_default_group_avatar)
    }
}