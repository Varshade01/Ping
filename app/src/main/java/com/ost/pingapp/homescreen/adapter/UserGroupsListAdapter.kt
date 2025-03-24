package com.khrd.pingapp.homescreen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.databinding.UserGroupsItemBinding
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import javax.inject.Inject

const val ITEM_TYPE_USER_GROUPS = 1

class UserGroupsListAdapter @Inject constructor(
    private val imageLoader: ImageLoader,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _userGroupsNames: List<DatabaseGroup?> = listOf()

    var onGroupClick: (String) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_USER_GROUPS -> {
                val binding = UserGroupsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserGroupsItemViewHolder(binding, imageLoader, onGroupClick)
            }

            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = _userGroupsNames[position]
        (holder as UserGroupsItemViewHolder).bind(currentItem)
    }

    override fun getItemViewType(position: Int): Int = ITEM_TYPE_USER_GROUPS

    override fun getItemCount(): Int = _userGroupsNames.size

    fun setOnGroupClickListener(listener: (String) -> Unit) {
        onGroupClick = listener
    }

    fun setData(items: List<DatabaseGroup?>) {
        this._userGroupsNames = items
        notifyDataSetChanged()
    }
}