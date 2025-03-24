package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.data.pings.RecurringTime
import com.khrd.pingapp.data.pings.getResId
import com.khrd.pingapp.databinding.PingSentItemScheduledBinding
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledItem
import com.khrd.pingapp.homescreen.states.CancelPingListener
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import com.khrd.pingapp.utils.getDate
import io.getstream.avatarview.coil.loadImage

class SentPingsScheduledItemViewHolder(
    private val binding: PingSentItemScheduledBinding,
    private val cancelPingListener: CancelPingListener?,
    private val onPhotoClickListener: (item: SentPingItem) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SentPingScheduledItem) {
        setReceiverName(item)
        setGroupName(item)
        setPingDate(item)
        binding.pingSentItemScheduledEmojiTv.text = item.emoji
        setUserOnlineStatus(item)
        binding.pingSentItemScheduledCancelTv.setOnClickListener {
            cancelPingListener?.onCancelPing(item.pingId, item.scheduledDate)
        }
        setPingReceiverImage(item)
        setOnPhotoClickedListener(item)
    }

    private fun setPingDate(ping: SentPingScheduledItem) {
        if (ping.recurringTime == RecurringTime.NO_REPEAT) {
            binding.pingSentItemScheduledScheduledTv.text =
                binding.root.resources.getString(R.string.scheduled_double_dot)
            binding.pingSentItemScheduledDateTv.text = getDate(ping.scheduledDate)
        } else {
            binding.pingSentItemScheduledScheduledTv.text =
                binding.root.resources.getString(R.string.repeat_double_dot)

            binding.pingSentItemScheduledDateTv.text = String.format(
                binding.root.resources.getString(R.string.schedule_recurring_ping_date),
                binding.root.context.getString(ping.recurringTime.getResId()),
                getDate(ping.scheduledDate)
            )
        }
    }

    private fun setGroupName(item: SentPingScheduledItem) {
        val isNotGroupPing = item.groupId.isNullOrBlank()
        binding.userGroupNameScheduledPing.isVisible = isNotGroupPing
        binding.userGroupNameScheduledPing.text = if (isNotGroupPing) item.groupFrom?.name else ""
    }

    private fun setUserOnlineStatus(item: SentPingScheduledItem) {
        binding.scheduledPingOnlineStatus.isVisible = item.online?.status == true
    }

    private fun setReceiverName(item: SentPingScheduledItem) {
        val listReceivers = mutableListOf<String>()

        // If one or more users are deleted
        item.receiver.forEach { userItem ->
            if (userItem.isDeleted == true) {
                listReceivers.add(binding.root.resources.getString(R.string.deleted_user))
            } else {
                listReceivers.add((userItem.fullname?.let { it.toString() } ?: ""))
            }
        }
        binding.pingSentItemScheduledReceiverTv.text = listReceivers.joinToString()

        // If all users are deleted or it is a group ping
        if (item.receiver.isNullOrEmpty()) {
            binding.pingSentItemScheduledReceiverTv.text = item.groupFrom?.name
            binding.pingSentItemScheduledReceiverTv.setTextColor(binding.root.resources.getColor(R.color.intouch_text, null))
        }
    }

    private fun setOnPhotoClickedListener(item: SentPingScheduledItem) {
        item.receiver.let { listOfReceivers ->
            if (listOfReceivers.size == 1) {
                listOfReceivers.forEach { userItem ->
                    if (userItem.isDeleted == true) {
                        binding.pingSentItemScheduledAvatarIv.setOnClickListener(null)
                    } else {
                        binding.pingSentItemScheduledAvatarIv.setOnClickListener {
                            val action = item.groupFrom?.let { dataBaseGroup ->
                                HomescreenNavGraphDirections.showUserStatusDialog(
                                    userItem,
                                    dataBaseGroup
                                )
                            }
                            action?.let { navDirections -> Navigation.findNavController(itemView).navigateSafe(navDirections) }
                        }
                    }
                }
            } else {
                binding.pingSentItemScheduledAvatarIv.setOnClickListener(null)
            }
        }
    }

    private fun setPingReceiverImage(item: SentPingScheduledItem) {
        @DrawableRes val defaultIntIcon =
            if (!item.groupId.isNullOrBlank()) R.drawable.ic_default_group_avatar else R.drawable.ic_default_user_avatar
        // If it's group ping -> taking photo from group
        val listReceiversPhoto = if (!item.groupId.isNullOrBlank()) {
            listOf(item.groupFrom?.photoURL)
            // If it's ping to 2 or more people -> we are setting photo of first 4 people
        } else {
            val photos = item.receiver.take(4).map { it.photoURL }.toMutableList()
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
        binding.pingSentItemScheduledAvatarIv.errorPlaceholder = defaultDrawableIcon
        binding.pingSentItemScheduledAvatarIv.placeholder = defaultDrawableIcon
        binding.pingSentItemScheduledAvatarIv.loadImage(listReceiversPhoto)
    }
}
