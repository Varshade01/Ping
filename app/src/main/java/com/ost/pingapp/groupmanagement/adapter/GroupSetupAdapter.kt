package com.khrd.pingapp.groupmanagement.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.databinding.*
import com.khrd.pingapp.groupmanagement.listeners.*
import com.khrd.pingapp.groupmanagement.states.*
import com.khrd.pingapp.utils.imageLoader.ImageLoader

const val ITEM_TYPE_UNKNOWN = 0
const val ITEM_TYPE_CREATE_GROUP = 1
const val ITEM_TYPE_RENAME_GROUP = 2
const val ITEM_TYPE_JOIN_GROUP = 3
const val ITEM_TYPE_LEAVE_GROUP = 4
const val ITEM_TYPE_SHARE_LINK_GROUP = 5
const val ITEM_TYPE_UPDATE_GROUP_IMAGE = 6
const val ITEM_TYPE_MUTE_GROUP = 7

class GroupSetupAdapter(
    private val createGroupListener: CreateGroupListener,
    private val renameGroupListener: RenameGroupListener,
    private val joinGroupListener: JoinGroupListener,
    private val leaveGroupListener: LeaveGroupListener,
    private val shareLinkListener: ShareLinkGroupListener,
    private val muteGroupListener: MuteGroupListener,
    private val updateGroupImageListener: UpdateGroupImageListener,
    private val imageLoader: ImageLoader,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: MutableList<DisplayableItem> = mutableListOf()
    private var _updateGroupImageItemViewHolder: UpdateGroupImageItemViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_TYPE_CREATE_GROUP -> {
                val binding = CreateGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CreateGroupItemViewHolder(binding, createGroupListener)
            }
            ITEM_TYPE_UPDATE_GROUP_IMAGE -> {
                val binding = GroupImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UpdateGroupImageItemViewHolder(binding, updateGroupImageListener, imageLoader).also {
                    _updateGroupImageItemViewHolder = it
                }
            }
            ITEM_TYPE_RENAME_GROUP -> {
                val binding = RenameGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RenameGroupItemViewHolder(binding, renameGroupListener)
            }
            ITEM_TYPE_LEAVE_GROUP -> {
                val binding = LeaveGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LeaveGroupItemViewHolder(binding, leaveGroupListener)
            }
            ITEM_TYPE_JOIN_GROUP -> {
                val binding = JoinGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                JoinGroupItemViewHolder(binding, joinGroupListener)
            }
            ITEM_TYPE_SHARE_LINK_GROUP -> {
                val binding = ShareLinkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ShareLinkItemViewHolder(binding, shareLinkListener)
            }
            ITEM_TYPE_MUTE_GROUP -> {
                val binding = MuteGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MuteGroupItemViewHolder(binding, muteGroupListener)
            }
            else -> throw IllegalStateException()
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = items[position]
        when (currentItem) {
            is CreateGroupItem -> (holder as CreateGroupItemViewHolder).bind(currentItem)
            is RenameGroupItem -> (holder as RenameGroupItemViewHolder).bind(currentItem)
            is JoinGroupItem -> (holder as JoinGroupItemViewHolder).bind(currentItem)
            is LeaveGroupItem -> (holder as LeaveGroupItemViewHolder).bind(currentItem)
            is ShareLinkItem -> (holder as ShareLinkItemViewHolder).bind(currentItem)
            is MuteGroupItem -> (holder as MuteGroupItemViewHolder).bind(currentItem)
            is UpdateGroupImageItem -> (holder as UpdateGroupImageItemViewHolder).bind(currentItem)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is CreateGroupItem -> ITEM_TYPE_CREATE_GROUP
            is RenameGroupItem -> ITEM_TYPE_RENAME_GROUP
            is JoinGroupItem -> ITEM_TYPE_JOIN_GROUP
            is LeaveGroupItem -> ITEM_TYPE_LEAVE_GROUP
            is ShareLinkItem -> ITEM_TYPE_SHARE_LINK_GROUP
            is MuteGroupItem -> ITEM_TYPE_MUTE_GROUP
            is UpdateGroupImageItem -> ITEM_TYPE_UPDATE_GROUP_IMAGE
            else -> ITEM_TYPE_UNKNOWN
        }

    override fun getItemCount(): Int = items.size

    fun onCreateGroupActionChanged(action: CreateGroupState) {
        items.forEachIndexed { index, it ->
            if (it is CreateGroupItem) {
                if (action is CreateGroupEditAction) {
                    when (val validationState = action.state) {
                        GroupNameValidationState.VALID -> {
                            it.nameValidationError = validationState
                            it.isNameSaved = true
                            it.link = action.link
                        }
                        GroupNameValidationState.EMPTY_FIELD -> it.nameValidationError = validationState
                        GroupNameValidationState.TOO_LONG -> it.nameValidationError = validationState
                        GroupNameValidationState.INVALID_CHARS -> it.nameValidationError = validationState
                    }
                } else if (action is CreateGroupSaveAction) {
                    it.isNameSaved = false
                }
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    fun onRenameGroupAction(state: RenameGroupState) {
        items.forEachIndexed { index, displayableItem ->
            if (displayableItem is RenameGroupItem) {
                if (state is RenameGroupEditAction) {
                    displayableItem.isInEditState = true
                } else if (state is RenameGroupSaveAction) {
                    when (state.validation) {
                        GroupNameValidationState.VALID -> {
                            displayableItem.name = state.name.trim()
                            displayableItem.isInEditState = false
                            displayableItem.isInSavingState = true
                            displayableItem.nameValidation = GroupNameValidationState.VALID
                        }
                        GroupNameValidationState.EMPTY_FIELD -> {
                            displayableItem.isInEditState = true
                            displayableItem.nameValidation = GroupNameValidationState.EMPTY_FIELD
                        }
                        GroupNameValidationState.TOO_LONG -> {
                            displayableItem.isInEditState = true
                            displayableItem.nameValidation = GroupNameValidationState.TOO_LONG
                        }
                        GroupNameValidationState.INVALID_CHARS -> {
                            displayableItem.isInEditState = true
                            displayableItem.nameValidation = GroupNameValidationState.INVALID_CHARS
                        }
                    }
                }
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    fun onJoinGroupActionChanged(state: JoinGroupState?) {
        items.forEachIndexed { index, it ->
            if (it is JoinGroupItem) {
                when (state) {
                    is JoinGroupAction -> it.joinGroupError = JoinGroupError.LINK_VALID
                    is JoinGroupFailure -> it.joinGroupError = state.error
                    is JoinGroupWithConfirmationAction -> it.joinGroupError = JoinGroupError.LINK_VALID
                    else -> it.joinGroupError = JoinGroupError.UNKNOWN_ERROR
                }
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    fun onMuteGroupActionChanged(state: Boolean) {
        items.forEachIndexed { i, it ->
            if (it is MuteGroupItem) {
                it.muted = state
                notifyItemChanged(i)
            }
        }
    }

    fun setData(items: MutableList<DisplayableItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setGroupImageUrl(imageUrl: String?) {
        _updateGroupImageItemViewHolder?.setGroupImageUrl(imageUrl)
    }

}
