package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders

import android.os.Build
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.databinding.PingSentItemUnscheduledBinding
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.listeners.OnSeenClickedListener
import com.khrd.pingapp.utils.getRelativeDate
import io.getstream.avatarview.coil.loadImage

class SentPingsItemViewHolder(
    private val binding: PingSentItemUnscheduledBinding,
    private val listener: OnSeenClickedListener?,
    private val onPhotoClickListener: (item: SentPingItem) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    private var item: SentPingItem? = null

    init {
        setOnSeenClickListener()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(item: SentPingItem) {
        this.item = item
        setReceiverName(item)
        setGroupName()
        binding.pingSentItemUnscheduledDateTv.text = getRelativeDate(item.date)
        binding.pingSentItemUnscheduledEmojiTv.text = item.emoji
        setUserOnlineStatus(item)
        setOnPhotoClickedListener(item)
        setSeenStatus()
        setPingReceiverImage()
    }

    private fun setGroupName() {
        val isNotGroupPing = item!!.groupId.isNullOrBlank()
        binding.userGroupNameSentPing.isVisible = isNotGroupPing
        binding.userGroupNameSentPing.text = if (isNotGroupPing) item!!.groupFrom?.name else ""
    }

    private fun setUserOnlineStatus(item: SentPingItem) {
        binding.sentPingOnlineStatus.isVisible =
            item.online?.status == true && !item.receiver[0].isHide
    }

    private fun setReceiverName(item: SentPingItem) {
        binding.sentPingReceiverName.text = provideReceiverName(item)
    }

    private fun provideReceiverName(item: SentPingItem): String {
//        return item.groupId?.let { provideReceiverGroupName(item) } ?: provideReceiversNames(item)
        return if (item.groupId != null) {
            provideReceiverGroupName(item)
        } else {
            provideReceiversNames(item)
        }
    }

    private fun setOnPhotoClickedListener(item: SentPingItem) {
        binding.pingSentItemUnscheduledAvatarIv.setOnClickListener {
            onPhotoClickListener(item)
        }
    }

    private fun provideReceiversNames(item: SentPingItem): String {
        val listReceivers = mutableListOf<String>()
        item.receiver.forEach { databaseUser ->
            if (databaseUser.isDeleted == true) {
                listReceivers.add(binding.root.resources.getString(R.string.deleted_user))
            } else {
                listReceivers.add(databaseUser.fullname?.toString() ?: "")
            }
        }
        return listReceivers.joinToString()
    }

    private fun provideReceiverGroupName(item: SentPingItem) =
        item.groupFrom?.name ?: binding.root.resources.getString(R.string.deleted_group)

    private fun setOnSeenClickListener() {
        binding.tvSeen.setOnClickListener {
            item?.let {
                if (isMultipleUsersPing()) {
                    listener?.onSeenClicked(it)
                }
            }
        }
    }

    private fun isMultipleUsersPing() = item?.let { it.receiver.size > 1 } ?: false

    private fun setSeenStatus() {
        val tvSeen = binding.tvSeen
        if (item!!.views.isEmpty()) {
            tvSeen.visibility = View.GONE
        } else {
            tvSeen.visibility = View.VISIBLE
            if (isMultipleUsersPing()) {
                val seenText = binding.root.context.getString(R.string.seen_ping_status_text) + " (${item!!.views.size})"
                tvSeen.setTextColor(binding.root.context.getColor(R.color.intouch_primary_01))
                tvSeen.text = seenText
            } else {
                tvSeen.setTextColor(binding.root.context.getColor(R.color.intouch_text))
                tvSeen.text = binding.root.context.getString(R.string.seen_ping_status_text)
            }
        }
    }

    private fun setPingReceiverImage() {
        item?.let { pingItem ->
            @DrawableRes val defaultIntIcon =
                if (!pingItem.groupId.isNullOrBlank()) R.drawable.ic_default_group_avatar else R.drawable.ic_default_user_avatar
            // If it's group ping -> taking photo from group
            val listReceiversPhoto = if (!pingItem.groupId.isNullOrBlank()) {
                listOf(pingItem.groupFrom?.photoURL)
                // If it's ping to 2 or more people -> we are setting photo of first 4 people
            } else {
                val photos = pingItem.receiver.take(4).map { it.photoURL }.toMutableList()
                //If all photos is empty - set default photo, otherwise empty photos we change to default photos
                if (photos.any { !it.isNullOrEmpty() }) {
                    photos.replaceAll { if (it.isNullOrEmpty()) Constants.USER_DEFAULT_AVATAR else it }
                    photos
                } else {
                    listOf(Constants.USER_DEFAULT_AVATAR)
                }
            }
            // Int to drawable ↓
            val defaultDrawableIcon = ContextCompat.getDrawable(itemView.context, defaultIntIcon)
            binding.pingSentItemUnscheduledAvatarIv.errorPlaceholder = defaultDrawableIcon
            binding.pingSentItemUnscheduledAvatarIv.placeholder = defaultDrawableIcon
            binding.pingSentItemUnscheduledAvatarIv.loadImage(listReceiversPhoto)
        }
    }
}
