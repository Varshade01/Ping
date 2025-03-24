package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.databinding.ReceiverStatusItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader

class ReceiverStatusItemViewHolder(
    private val binding: ReceiverStatusItemBinding,
    private val imageLoader: ImageLoader,
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ReceiverStatusItem) {
        setName(item)
        binding.tvSeen.visibility = if (item.hasSeen) View.VISIBLE else View.GONE
        imageLoader.loadImage(item.imageUrl, binding.ivReceiverProfileImage, R.drawable.ic_default_user_avatar)
    }

    private fun setName(item: ReceiverStatusItem) {
        if (item.isDeleted != null && item.isDeleted!!) {
            binding.tvReceiverName.text = binding.root.resources.getString(R.string.deleted_user)
        } else {
            binding.tvReceiverName.text = item.name
        }
        binding.isOnlineSeenDialog.isVisible = item.isOnline?.status == true
    }
}
